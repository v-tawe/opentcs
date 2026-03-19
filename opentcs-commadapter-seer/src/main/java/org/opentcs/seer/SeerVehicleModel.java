// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.seer;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleProcessModel;

/**
 * A vehicle model for the Seer comm adapter.
 */
public class SeerVehicleModel
    extends
      VehicleProcessModel {

  private String loadOperation = "Load";
  private String unloadOperation = "Unload";
  private int maxFwdVelocity = 1000;
  private int maxRevVelocity = 500;
  private int maxAcceleration = 500;
  private int maxDecceleration = 500;
  private boolean vehiclePaused;

  /**
   * Creates a new instance.
   *
   * @param vehicle The vehicle this model is for.
   */
  public SeerVehicleModel(Vehicle vehicle) {
    super(vehicle);
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
   */
  public void setLoadOperation(String loadOperation) {
    this.loadOperation = loadOperation;
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
   */
  public void setUnloadOperation(String unloadOperation) {
    this.unloadOperation = unloadOperation;
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
   */
  public void setMaxFwdVelocity(int maxFwdVelocity) {
    this.maxFwdVelocity = maxFwdVelocity;
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
   */
  public void setMaxRevVelocity(int maxRevVelocity) {
    this.maxRevVelocity = maxRevVelocity;
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
   */
  public void setMaxAcceleration(int maxAcceleration) {
    this.maxAcceleration = maxAcceleration;
  }

  /**
   * Returns the maximum deceleration.
   *
   * @return The maximum deceleration.
   */
  public int getMaxDecceleration() {
    return maxDecceleration;
  }

  /**
   * Sets the maximum deceleration.
   *
   * @param maxDecceleration The maximum deceleration.
   */
  public void setMaxDecceleration(int maxDecceleration) {
    this.maxDecceleration = maxDecceleration;
  }

  /**
   * Returns whether the vehicle is paused.
   *
   * @return Whether the vehicle is paused.
   */
  public boolean isVehiclePaused() {
    return vehiclePaused;
  }

  /**
   * Sets whether the vehicle is paused.
   *
   * @param vehiclePaused Whether the vehicle is paused.
   */
  public void setVehiclePaused(boolean vehiclePaused) {
    this.vehiclePaused = vehiclePaused;
  }

  /**
   * Vehicle model attributes used as property change event names.
   */
  public enum Attribute {
    ;
  }
}
