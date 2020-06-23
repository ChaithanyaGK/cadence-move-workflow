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
