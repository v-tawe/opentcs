// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.seer;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;

/**
 * A factory for creating SeerCommAdapter instances.
 */
public class SeerCommAdapterFactory
    implements
      VehicleCommAdapterFactory {

  private final SeerCommAdapterComponentsFactory componentsFactory;
  private boolean initialized;

  @Inject
  public SeerCommAdapterFactory(SeerCommAdapterComponentsFactory componentsFactory) {
    this.componentsFactory = componentsFactory;
  }

  @Override
  public VehicleCommAdapterDescription getDescription() {
    return new SeerCommAdapterDescription();
  }

  @Override
  public boolean providesAdapterFor(@Nonnull
  Vehicle vehicle) {
    return true;
  }

  @Override
  @Nullable
  public VehicleCommAdapter getAdapterFor(@Nonnull
  Vehicle vehicle) {
    return componentsFactory.createSeerCommAdapter(vehicle);
  }

  @Override
  public void initialize() {
    initialized = true;
  }

  @Override
  public void terminate() {
    initialized = false;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }
}
