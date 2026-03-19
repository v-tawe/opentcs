// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.seer;

/**
 * Constants for the Seer comm adapter.
 */
public interface SeerAdapterConstants {

  /**
   * The key for the vehicle property defining the initial position.
   */
  String PROPKEY_INITIAL_POSITION = "seer:initialPosition";
  /**
   * The key for the vehicle property defining the Modbus IP address.
   */
  String PROPKEY_MODBUS_IP = "seer:modbusIp";
  /**
   * The key for the vehicle property defining the Modbus port.
   */
  String PROPKEY_MODBUS_PORT = "seer:modbusPort";
}
