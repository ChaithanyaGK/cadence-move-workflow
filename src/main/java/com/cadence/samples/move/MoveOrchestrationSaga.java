package com.cadence.samples.move;

import com.uber.cadence.worker.Worker;

public class MoveOrchestrationSaga {
  static final String DOMAIN = "sample";
  static final String TASK_LIST = "Move";

  public static void main(String[] args) {
    // Get worker to poll the common task list.
    Worker.Factory factory = new Worker.Factory(DOMAIN);
    final Worker workerForCommonTaskList = factory.newWorker(TASK_LIST);
    workerForCommonTaskList.registerWorkflowImplementationTypes(
        MoveOrchestrationWorkflowImpl.class);
    MoveOrchestrationActivities moveOrchestrationActivities = new MoveOrchestrationActivitiesImpl();
    workerForCommonTaskList.registerActivitiesImplementations(moveOrchestrationActivities);

    // Start all workers created by this factory.
    factory.start();
    System.out.println("Worker started for task list: " + TASK_LIST);
  }
}
