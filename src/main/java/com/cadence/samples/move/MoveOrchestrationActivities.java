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
