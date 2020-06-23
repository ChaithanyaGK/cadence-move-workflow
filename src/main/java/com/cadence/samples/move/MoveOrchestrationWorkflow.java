package com.cadence.samples.move;

import static com.cadence.samples.move.MoveOrchestrationSaga.TASK_LIST;

import com.uber.cadence.workflow.QueryMethod;
import com.uber.cadence.workflow.SignalMethod;
import com.uber.cadence.workflow.WorkflowMethod;

public interface MoveOrchestrationWorkflow {

  @WorkflowMethod(executionStartToCloseTimeoutSeconds = 3600, taskList = TASK_LIST)
  void move(MoveRequest request);

  /** Receives replication status through an external signal. */
  @SignalMethod
  void waitForReplicationEvent(String payload);

  /** Returns move operation status. */
  @QueryMethod
  String queryProgress();
}
