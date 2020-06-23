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

import com.uber.cadence.activity.Activity;
import java.util.UUID;

public class MoveOrchestrationActivitiesImpl implements MoveOrchestrationActivities {
  @Override
  public void validateInput(MoveRequest request) {
    System.out.println("Validate Move Request '" + request + "'");
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
    int i = 0;
    try {
      while (true) {
        Activity.heartbeat("ping");
        if (i % 6 == 0) {
          System.out.println(
              "Increase lock (lockId=" + lockId + ") time-out by " + lockTime + "ms");
        }
        i++;
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
