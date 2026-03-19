// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.seer;

import com.google.inject.name.Named;
import jakarta.inject.Inject;
import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Configuration for the Seer vehicle adapter.{@link SeerCommAdapter}.
 */
@ConfigurationPrefix(SeerVehicleConfiguration.PREFIX)
public interface SeerVehicleConfiguration {

  /**
   * The prefix for configuration entries.
   */
  public static final String PREFIX = "seer";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to enable to register/enable the seer driver.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "0_enable"
  )
  boolean enable();

  @ConfigurationEntry(
      type = "Integer",
      description = "The adapter's command queue capacity.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "1_attributes_1"
  )
  int commandQueueCapacity();

  @ConfigurationEntry(
      type = "String",
      description = "The string to be treated as a recharge operation.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "1_attributes_2"
  )
  String rechargeOperation();

  @ConfigurationEntry(
      type = "Double",
      description = "The rate at which the vehicle recharges in percent per second.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "1_attributes_3"
  )
  double rechargePercentagePerSecond();

}
