// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.seer;

import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;

/**
 * A transfer object for the Seer vehicle model.
 */
public class SeerVehicleModelTO
    extends
      VehicleProcessModelTO {

  private String loadOperation = "Load";
  private String unloadOperation = "Unload";
  private int maxFwdVelocity = 1000;
  private int maxRevVelocity = 500;
  private int maxAcceleration = 500;
  private int maxDeceleration = 500;

  /**
   * Creates a new instance.
   */
  public SeerVehicleModelTO() {
  }

  /**
   * Returns the load operation.
   *
   * @return The load operation.
   */
  public String getLoadOperation() {
    return loadOperation;
  }

  /**
   * Sets the load operation.
   *
   * @param loadOperation The load operation.
   * @return This instance.
   */
  public SeerVehicleModelTO setLoadOperation(String loadOperation) {
    this.loadOperation = loadOperation;
    return this;
  }

  /**
   * Returns the unload operation.
   *
   * @return The unload operation.
   */
  public String getUnloadOperation() {
    return unloadOperation;
  }

  /**
   * Sets the unload operation.
   *
   * @param unloadOperation The unload operation.
   * @return This instance.
   */
  public SeerVehicleModelTO setUnloadOperation(String unloadOperation) {
    this.unloadOperation = unloadOperation;
    return this;
  }

  /**
   * Returns the maximum forward velocity.
   *
   * @return The maximum forward velocity.
   */
  public int getMaxFwdVelocity() {
    return maxFwdVelocity;
  }

  /**
   * Sets the maximum forward velocity.
   *
   * @param maxFwdVelocity The maximum forward velocity.
   * @return This instance.
   */
  public SeerVehicleModelTO setMaxFwdVelocity(int maxFwdVelocity) {
    this.maxFwdVelocity = maxFwdVelocity;
    return this;
  }

  /**
   * Returns the maximum reverse velocity.
   *
   * @return The maximum reverse velocity.
   */
  public int getMaxRevVelocity() {
    return maxRevVelocity;
  }

  /**
   * Sets the maximum reverse velocity.
   *
   * @param maxRevVelocity The maximum reverse velocity.
   * @return This instance.
   */
  public SeerVehicleModelTO setMaxRevVelocity(int maxRevVelocity) {
    this.maxRevVelocity = maxRevVelocity;
    return this;
  }

  /**
   * Returns the maximum acceleration.
   *
   * @return The maximum acceleration.
   */
  public int getMaxAcceleration() {
    return maxAcceleration;
  }

  /**
   * Sets the maximum acceleration.
   *
   * @param maxAcceleration The maximum acceleration.
   * @return This instance.
   */
  public SeerVehicleModelTO setMaxAcceleration(int maxAcceleration) {
    this.maxAcceleration = maxAcceleration;
    return this;
  }

  /**
   * Returns the maximum deceleration.
   *
   * @return The maximum deceleration.
   */
  public int getMaxDeceleration() {
    return maxDeceleration;
  }

  /**
   * Sets the maximum deceleration.
   *
   * @param maxDeceleration The maximum deceleration.
   * @return This instance.
   */
  public SeerVehicleModelTO setMaxDeceleration(int maxDeceleration) {
    this.maxDeceleration = maxDeceleration;
    return this;
  }
}
