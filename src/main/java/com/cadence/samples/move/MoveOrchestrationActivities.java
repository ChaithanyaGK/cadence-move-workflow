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

import com.uber.cadence.activity.ActivityMethod;
import com.uber.cadence.common.MethodRetry;

public interface MoveOrchestrationActivities {

  @ActivityMethod
  void validateInput(MoveRequest request);

  @ActivityMethod
  String acquireLock(String sourceId);

  @ActivityMethod
  void releaseLock(String lockId);

  @ActivityMethod(heartbeatTimeoutSeconds = 2)
  @MethodRetry(
    initialIntervalSeconds = 1,
    backoffCoefficient = 1,
    maximumIntervalSeconds = 60,
    expirationSeconds = 300,
    maximumAttempts = 5
  )
  Void increaseLockTimeOut(String lockId, long lockTime, long interval);

  @ActivityMethod
  void triggerCreateReplicaEvent(String payload);

  @ActivityMethod
  void deletePassiveTarget(String targetId);

  @ActivityMethod
  void deletePassiveSource(String sourceId);

  @ActivityMethod
  void markSourceToPassive(String sourceId);

  @ActivityMethod
  void markTargetToActive(String targetId);

  @ActivityMethod
  void markSourceToActive(String sourceId);
}
