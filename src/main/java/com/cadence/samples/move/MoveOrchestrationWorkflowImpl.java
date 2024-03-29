/*
 * MIT License
 *
 * Copyright (c) 2020 Krishna Chaithanya Ganta
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cadence.samples.move;

import com.uber.cadence.activity.ActivityOptions;
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

  private final MoveOrchestrationActivities activities =
      Workflow.newActivityStub(MoveOrchestrationActivities.class, options);

  List<String> messageQueue = new ArrayList<>(10);
  String progress = "INITIATED";

  @Override
  public String move(MoveRequest request) {
    // Configure SAGA to run compensation activities in parallel
    Saga.Options sagaOptions = new Saga.Options.Builder().setParallelCompensation(true).build();
    Saga saga = new Saga(sagaOptions);
    String result;

    try {
      appendStatus("VALIDATING_INPUT");
      activities.validateInput(request);

      appendStatus("ACQUIRING_LOCK_ON_SOURCE");
      String lockId = activities.acquireLock(request.getSourceId());
      saga.addCompensation(activities::releaseLock, lockId);

      CancellationScope longRunningCancellationScope =
          Workflow.newCancellationScope(
              () -> Async.function(activities::increaseLockTimeOut, lockId, 2000L, 100L));
      longRunningCancellationScope.run();
      saga.addCompensation((Functions.Proc) longRunningCancellationScope::cancel);

      appendStatus("CREATING_REPLICA");
      activities.triggerCreateReplicaEvent(Workflow.getWorkflowInfo().getWorkflowId());
      saga.addCompensation(activities::deletePassiveTarget, request.getTargetId());

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
        } else {
          appendStatus(message);
        }
      }

      appendStatus("MARKING_SOURCE_TO_ACTIVE");
      activities.markSourceToPassive(request.getSourceId());
      saga.addCompensation(activities::markSourceToActive, request.getSourceId());

      longRunningCancellationScope.cancel();
      activities.releaseLock(lockId);

      appendStatus("MARKING_TARGET_TO_ACTIVE");
      activities.markTargetToActive(request.getTargetId());

      appendStatus("SUCCESS");
      activities.deletePassiveSource(request.getSourceId());

      result =
          String.format(
              "Moved Source(id=%s) to Target(id=%s) successfully",
              request.getSourceId(), request.getTargetId());
      System.out.println(result);
      return result;

    } catch (Exception e) {
      saga.compensate();
      result =
          String.format(
              "Failed to Move Source(id=%s) to Target(id=%s)",
              request.getSourceId(), request.getTargetId());
      return result;
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
