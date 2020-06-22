package com.cadence.samples.move;

import com.uber.cadence.activity.Activity;
import java.util.UUID;

public class MoveOrchestrationActivitiesImpl implements MoveOrchestrationActivities {
  @Override
  public void validateInput(String input) {
    System.out.println("Validate input '" + input + "'");
  }

  @Override
  public String acquireLock(String sourceId) {
    System.out.println("Acquire lock on source(id=" + sourceId + ")");
    return UUID.randomUUID().toString();
  }

  @Override
  public void releaseLock(String lockId) {
    try {
      Thread.sleep(5000);
      System.out.println("Release lock (lockId=" + lockId + ")");
    } catch (InterruptedException e) {
      System.out.println("Thread Interrupted while releasing lock");
    }
  }

  @Override
  public Void increaseLockTimeOut(String lockId, long lockTime, long interval) {
    try {
      while (true) {
        System.out.println("Increase lock (lockId=" + lockId + ") time-out by " + lockTime + "ms");
        Activity.heartbeat("ping");
        Thread.sleep(interval);
      }
    } catch (InterruptedException e) {
      System.out.println("Workflow completed, exiting increaseLockTimeOut Activity");
    }
    return null;
  }

  @Override
  public void triggerCreateReplicaEvent(String payload) {
    System.out.println("Publish create replica event (payload=" + payload + ")");
  }

  @Override
  public void deletePassiveTarget(String targetId) {
    System.out.println("Delete target (id=" + targetId + ")");
  }

  @Override
  public void deletePassiveSource(String sourceId) {
    System.out.println("Delete source (id=" + sourceId + ")");
  }

  @Override
  public void markSourceToPassive(String sourceId) {
    System.out.println("Mark source (id=" + sourceId + ") to PASSIVE");
  }

  @Override
  public void markTargetToActive(String targetId) {
    System.out.println("Mark target (id=" + targetId + ") to ACTIVE");
  }

  @Override
  public void markSourceToActive(String sourceId) {
    System.out.println("Mark source (id=" + sourceId + ") to ACTIVE");
  }
}
