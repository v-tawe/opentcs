// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.seer;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures/binds the Seer communication adapters of the openTCS kernel.
 */
public class SeerCommAdapterModule
    extends
      KernelInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(SeerCommAdapterModule.class);

  /**
   * Creates a new instance.
   */
  public SeerCommAdapterModule() {
  }

  @Override
  protected void configure() {
    try {
      SeerVehicleConfiguration configuration = getConfigBindingProvider().get(
          SeerVehicleConfiguration.PREFIX,
          SeerVehicleConfiguration.class
      );

      if (!configuration.enable()) {
        LOG.info("Seer driver disabled by configuration.");
        return;
      }

      bind(SeerVehicleConfiguration.class).toInstance(configuration);

      install(new FactoryModuleBuilder().build(SeerCommAdapterComponentsFactory.class));

      vehicleCommAdaptersBinder().addBinding().to(SeerCommAdapterFactory.class);
    }
    catch (Exception e) {
      LOG.warn(
          "Failed to load Seer vehicle configuration, using default values: {}", e.getMessage()
      );
    }
  }

}
