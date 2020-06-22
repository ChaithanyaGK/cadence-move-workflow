package com.cadence.samples.move;

import com.uber.cadence.activity.ActivityOptions;
import com.uber.cadence.activity.LocalActivityOptions;
import com.uber.cadence.workflow.Async;
import com.uber.cadence.workflow.CancellationScope;
import com.uber.cadence.workflow.Functions;
import com.uber.cadence.workflow.Saga;
import com.uber.cadence.workflow.Workflow;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MoveOrchestrationWorkflowImpl implements MoveOrchestrationWorkflow {
  private final ActivityOptions options =
      new ActivityOptions.Builder().setScheduleToCloseTimeout(Duration.ofHours(1)).build();

  private final LocalActivityOptions localOptions =
      new LocalActivityOptions.Builder().setScheduleToCloseTimeout(Duration.ofHours(1)).build();

  private final MoveOrchestrationActivities activities =
      Workflow.newActivityStub(MoveOrchestrationActivities.class, options);

  //  private final MoveOrchestrationActivities activities =
  //      Workflow.newLocalActivityStub(MoveOrchestrationActivities.class, localOptions);

  List<String> messageQueue = new ArrayList<>(10);
  Status status = Status.IN_PROGRESS;

  @Override
  public void move(String input) {
    // Configure SAGA to run compensation activities in parallel
    Saga.Options sagaOptions = new Saga.Options.Builder().setParallelCompensation(true).build();
    Saga saga = new Saga(sagaOptions);
    try {

      activities.validateInput(input);
      String lockId = activities.acquireLock(input);
      saga.addCompensation(activities::releaseLock, lockId);

      CancellationScope longRunningCancellationScope =
          Workflow.newCancellationScope(
              () -> Async.function(activities::increaseLockTimeOut, lockId, 100L, 800L));
      longRunningCancellationScope.run();
      saga.addCompensation((Functions.Proc) longRunningCancellationScope::cancel);

      activities.triggerCreateReplicaEvent(Workflow.getWorkflowInfo().getWorkflowId());
      saga.addCompensation(activities::deletePassiveTarget, input);

      while (true) {
        System.out.println("Waiting for create replica response...");
        Workflow.await(() -> !messageQueue.isEmpty());
        String message = messageQueue.remove(0);
        System.out.println("Create replica response: " + message);
        Status status = Status.valueOf(message);
        if (Status.FAILED.equals(status)) {
          throw new RuntimeException("Creating replica failed");
        } else if (Status.SUCCESS.equals(status)) {
          break;
        }
      }

      activities.markSourceToPassive(input);
      saga.addCompensation(activities::markSourceToActive, input);

      longRunningCancellationScope.cancel();

      activities.markTargetToActive(input);

      activities.deletePassiveSource(input);

    } catch (Exception e) {
      saga.compensate();
    }
  }

  @Override
  public void waitForReplicationEvent(String payload) {
    messageQueue.add(payload);
  }

  @Override
  public String queryProgress() {
    return status.toString();
  }
}
