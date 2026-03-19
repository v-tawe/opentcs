// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.seer;

import org.opentcs.data.model.Vehicle;

/**
 * A factory for creating SeerCommAdapter components.
 */
public interface SeerCommAdapterComponentsFactory {

  /**
   * Creates a new SeerCommAdapter for the given vehicle.
   *
   * @param vehicle The vehicle.
   * @return A new SeerCommAdapter for the given vehicle.
   */
  SeerCommAdapter createSeerCommAdapter(Vehicle vehicle);
}
