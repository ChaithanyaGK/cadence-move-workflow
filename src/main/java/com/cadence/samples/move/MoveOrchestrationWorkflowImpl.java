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
  String progress = "INITIATED";

  @Override
  public void move(MoveRequest request) {
    // Configure SAGA to run compensation activities in parallel
    Saga.Options sagaOptions = new Saga.Options.Builder().setParallelCompensation(true).build();
    Saga saga = new Saga(sagaOptions);

    try {
      appendStatus("VALIDATING_INPUT");
      activities.validateInput(request);

      appendStatus("ACQUIRING_LOCK_ON_SOURCE");
      String lockId = activities.acquireLock(request.getSourceId());
      saga.addCompensation(activities::releaseLock, lockId);

      CancellationScope longRunningCancellationScope =
          Workflow.newCancellationScope(
              () -> Async.function(activities::increaseLockTimeOut, lockId, 500L, 100L));
      longRunningCancellationScope.run();
      saga.addCompensation((Functions.Proc) longRunningCancellationScope::cancel);

      activities.triggerCreateReplicaEvent(Workflow.getWorkflowInfo().getWorkflowId());
      saga.addCompensation(activities::deletePassiveTarget, request.getTargetId());
      appendStatus("CREATING_REPLICA");

      while (true) {
        System.out.println("Waiting for create replica response...");
        Workflow.await(() -> !messageQueue.isEmpty());
        String message = messageQueue.remove(0);
        System.out.println("Create replica response: " + message);
        if ("FAILED".equals(message)) {
          appendStatus("CREATE_REPLICA_FAILED");
          throw new RuntimeException("Creating replica failed");
        } else if ("SUCCESS".equals(message)) {
          appendStatus("REPLICA_IN_SYNC");
          break;
        }
      }

      activities.markSourceToPassive(request.getSourceId());
      saga.addCompensation(activities::markSourceToActive, request.getSourceId());

      longRunningCancellationScope.cancel();
      activities.releaseLock(lockId);

      appendStatus("MARKING_TARGET_TO_ACTIVE");
      activities.markTargetToActive(request.getTargetId());

      appendStatus("SUCCESS");
      activities.deletePassiveSource(request.getSourceId());

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
    return progress.toString();
  }

  void appendStatus(String status) {
    this.progress = this.progress + "  ->  " + status;
  }
}
