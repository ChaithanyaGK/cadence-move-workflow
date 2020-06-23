/**
 * *********************************************************************** ADOBE CONFIDENTIAL
 * ___________________
 *
 * <p>Copyright 2020 Adobe All Rights Reserved.
 *
 * <p>NOTICE: All information contained herein is, and remains the property of Adobe and its
 * suppliers, if any. The intellectual and technical concepts contained herein are proprietary to
 * Adobe and its suppliers and are protected by all applicable intellectual property laws, including
 * trade secret and copyright laws. Dissemination of this information or reproduction of this
 * material is strictly forbidden unless prior written permission is obtained from Adobe.
 * ************************************************************************
 */
package com.cadence.samples.move;

public class MoveRequest {
  private String sourceId;
  private String targetId;

  public MoveRequest() {}

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public String getTargetId() {
    return targetId;
  }

  public void setTargetId(String targetId) {
    this.targetId = targetId;
  }
}
