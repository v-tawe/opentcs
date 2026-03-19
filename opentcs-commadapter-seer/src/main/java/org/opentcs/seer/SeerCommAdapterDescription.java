// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.seer;

import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

/**
 * A description for the Seer comm adapter.
 */
public class SeerCommAdapterDescription
    extends
      VehicleCommAdapterDescription {

  /**
   * Creates a new instance.
   */
  public SeerCommAdapterDescription() {
  }

  @Override
  public String getDescription() {
    return "Seer Modbus Adapter";
  }

  @Override
  public boolean isSimVehicleCommAdapter() {
    return false;
  }
}
