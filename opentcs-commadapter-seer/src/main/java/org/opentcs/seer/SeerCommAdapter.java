// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.seer;

import static java.util.Objects.requireNonNull;

import com.google.inject.assistedinject.Assisted;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.beans.PropertyChangeEvent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.net.TCPMasterConnection;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.util.ExplainedBoolean;
import org.opentcs.util.MapValueExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link VehicleCommAdapter} that communicates with a physical AGV via Modbus TCP.
 */
public class SeerCommAdapter
    extends
      BasicVehicleCommAdapter {

  /**
   * The name of the load handling device set by this adapter.
   */
  public static final String LHD_NAME = "default";
  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(SeerCommAdapter.class);
  /**
   * An error code indicating that there's a conflict between a load operation and the vehicle's
   * current load state.
   */
  private static final String LOAD_OPERATION_CONFLICT = "cannotLoadWhenLoaded";
  /**
   * An error code indicating that there's a conflict between an unload operation and the vehicle's
   * current load state.
   */
  private static final String UNLOAD_OPERATION_CONFLICT = "cannotUnloadWhenNotLoaded";
  /**
   * The time (in ms) of a single communication step.
   */
  private static final int COMMUNICATION_PERIOD = 1000;
  /**
   * The timeout (in ms) for Modbus transactions.
   */
  private static final int MODBUS_TIMEOUT = 5000;
  /**
   * This instance's configuration.
   */
  private final SeerVehicleConfiguration configuration;
  /**
   * Extracts values from maps.
   */
  private final MapValueExtractor mapValueExtractor;
  /**
   * Indicates whether the vehicle communication is running or not.
   */
  private volatile boolean isCommunicationRunning;
  /**
   * The vehicle to this comm adapter instance.
   */
  private final Vehicle vehicle;
  /**
   * The vehicle's load state.
   */
  private LoadState loadState = LoadState.EMPTY;
  /**
   * Whether the seer adapter is initialized or not.
   */
  private boolean initialized;
  /**
   * Modbus TCP connection.
   */
  private TCPMasterConnection connection;
  /**
   * Modbus TCP transaction.
   */
  private ModbusTCPTransaction transaction;

  /**
   * Creates a new instance.
   *
   * @param configuration This class's configuration.
   * @param mapValueExtractor Extracts values from maps.
   * @param vehicle The vehicle this adapter is associated with.
   * @param kernelExecutor The kernel's executor.
   */
  @Inject
  public SeerCommAdapter(
      SeerVehicleConfiguration configuration,
      MapValueExtractor mapValueExtractor,
      @Assisted
      Vehicle vehicle,
      @org.opentcs.customizations.kernel.KernelExecutor
      ScheduledExecutorService kernelExecutor
  ) {
    super(
        new SeerVehicleModel(vehicle),
        configuration.commandQueueCapacity(),
        configuration.rechargeOperation(),
        kernelExecutor
    );
    this.vehicle = requireNonNull(vehicle, "vehicle");
    this.configuration = requireNonNull(configuration, "configuration");
    this.mapValueExtractor
        = requireNonNull(mapValueExtractor, "mapValueExtractor");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    super.initialize();

    String initialPos = vehicle.getProperties().get(SeerAdapterConstants.PROPKEY_INITIAL_POSITION);
    if (initialPos != null) {
      initVehiclePosition(initialPos);
    }
    getProcessModel().setState(Vehicle.State.IDLE);
    getProcessModel().setLoadHandlingDevices(
        Arrays.asList(new LoadHandlingDevice(LHD_NAME, false))
    );
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    super.terminate();
    initialized = false;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    super.propertyChange(evt);

    if (!((evt.getSource()) instanceof SeerVehicleModel)) {
      return;
    }
    if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.LOAD_HANDLING_DEVICES.name()
    )) {
      if (!getProcessModel().getLoadHandlingDevices().isEmpty()
          && getProcessModel().getLoadHandlingDevices().get(0).isFull()) {
        loadState = LoadState.FULL;
      }
      else {
        loadState = LoadState.EMPTY;
      }
    }
  }

  @Override
  public synchronized void enable() {
    if (isEnabled()) {
      return;
    }
    super.enable();

    // 在单独的线程中执行连接操作，避免阻塞主线程
    getExecutor().submit(() -> {
      try {
        connectModbus();
        // 连接成功后，启动状态轮询和命令处理
        startStatusPolling();
//        if (!isCommunicationRunning && (!getSentCommands().isEmpty() || !getUnsentCommands().isEmpty())) {
//          getExecutor().submit(this::processCommand);
//        }
      }
      catch (Exception e) {
        LOG.error("Failed to connect to vehicle: {}", e.getMessage(), e);
        // 连接失败后，更新状态为 ERROR
        getProcessModel().setState(Vehicle.State.ERROR);
      }
    });
  }

  @Override
  public synchronized void disable() {
    if (!isEnabled()) {
      return;
    }
    disconnectModbus();
    // 重置通信运行标志，确保命令处理线程能够退出
    isCommunicationRunning = false;
    super.disable();
  }

  @Override
  public SeerVehicleModel getProcessModel() {
    return (SeerVehicleModel) super.getProcessModel();
  }

  @Override
  public void sendCommand(MovementCommand cmd) {
    requireNonNull(cmd, "cmd");

    // 将命令添加到未发送队列
    getUnsentCommands().add(cmd);

    // 启动命令处理线程（如果尚未启动）
    if (!isCommunicationRunning && isInitialized() && isVehicleConnected()) {
      getExecutor().submit(this::processCommand);
    }
  }

  public void onVehiclePaused(boolean paused) {
    getProcessModel().setVehiclePaused(paused);
  }

  public void processMessage(
      @Nonnull
      VehicleCommAdapterMessage message
  ) {
    // 处理适配器消息
  }

  public void initVehiclePosition(String newPos) {
    getExecutor().submit(() -> getProcessModel().setPosition(newPos));
  }

  @Override
  public ExplainedBoolean canProcess(TransportOrder order) {
    requireNonNull(order, "order");

    return canProcess(
        order.getFutureDriveOrders().stream()
            .map(driveOrder -> driveOrder.getDestination().getOperation())
            .collect(Collectors.toList())
    );
  }

  private ExplainedBoolean canProcess(List<String> operations) {
    requireNonNull(operations, "operations");

    LOG.debug("{}: Checking processability of {}...", getName(), operations);
    boolean canProcess = true;
    String reason = "";

    boolean loaded = loadState == LoadState.FULL;
    for (String op : operations) {
      if (loaded) {
        if (op.startsWith(getProcessModel().getLoadOperation())) {
          canProcess = false;
          reason = LOAD_OPERATION_CONFLICT;
          break;
        }
        else if (op.startsWith(getProcessModel().getUnloadOperation())) {
          loaded = false;
        }
      }
      else if (op.startsWith(getProcessModel().getLoadOperation())) {
        loaded = true;
      }
      else if (op.startsWith(getProcessModel().getUnloadOperation())) {
        canProcess = false;
        reason = UNLOAD_OPERATION_CONFLICT;
        break;
      }
    }
    if (!canProcess) {
      LOG.debug("{}: Cannot process {}, reason: '{}'", getName(), operations, reason);
    }
    return new ExplainedBoolean(canProcess, reason);
  }

  @Override
  protected void connectVehicle() {

  }

  @Override
  protected void disconnectVehicle() {
  }

  @Override
  protected boolean isVehicleConnected() {
    // 非同步检查连接状态，避免线程阻塞
    return connection != null && connection.isConnected();
  }

  @Override
  protected VehicleProcessModelTO createCustomTransferableProcessModel() {
    return new SeerVehicleModelTO()
        .setLoadOperation(getProcessModel().getLoadOperation())
        .setMaxAcceleration(getProcessModel().getMaxAcceleration())
        .setMaxDeceleration(getProcessModel().getMaxDecceleration())
        .setMaxFwdVelocity(getProcessModel().getMaxFwdVelocity())
        .setMaxRevVelocity(getProcessModel().getMaxRevVelocity())
        .setUnloadOperation(getProcessModel().getUnloadOperation());
  }

  /**
   * Connects to the vehicle via Modbus TCP.
   */
  private void connectModbus()
      throws Exception {
    String ipAddress = vehicle.getProperties().get(SeerAdapterConstants.PROPKEY_MODBUS_IP);
    int port = Integer.parseInt(
        vehicle.getProperties().get(SeerAdapterConstants.PROPKEY_MODBUS_PORT)
    );
    if (ipAddress == null || ipAddress.isEmpty()) {
      throw new IllegalArgumentException("Modbus IP address or Port is not set");
    }
    connection = new TCPMasterConnection(java.net.InetAddress.getByName(ipAddress));
    connection.setPort(port);
    connection.setTimeout(MODBUS_TIMEOUT);
    connection.connect();
    transaction = new ModbusTCPTransaction(connection);
    transaction.setRetries(3);

    LOG.info("Connected to AGV at {}:{}", ipAddress, port);
  }

  /**
   * Disconnects from the vehicle.
   */
  private void disconnectModbus() {
    if (connection != null) {
      connection.close();
      connection = null;
      transaction = null;
    }
    LOG.info("Disconnected from AGV");
  }

  /**
   * Starts polling the vehicle status.
   */
  private void startStatusPolling() {
    getExecutor().submit(() -> {
      while (isInitialized() && isVehicleConnected()) {
        try {
          updateVehicleStatus();
          Thread.sleep(COMMUNICATION_PERIOD);
        }
        catch (InterruptedException e) {
          LOG.warn("Status polling interrupted: {}", e.getMessage());
          Thread.currentThread().interrupt();
          break;
        }
        catch (Exception e) {
          LOG.error("Error polling vehicle status: {}", e.getMessage(), e);
          try {
            Thread.sleep(3000); // 发生异常时，延长睡眠时间
          }
          catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            break;
          }
        }
      }
    });
  }

  /**
   * 将两个 16 位寄存器的值转换为一个 32 位浮点数
   * 
   * @param reg1 第一个寄存器的值（高16位）
   * @param reg2 第二个寄存器的值（低16位）
   * @return 转换后的浮点数
   */
  private static float _toFloat(int reg1, int reg2) {
    // 1. 创建 4 字节缓冲区，指定小端序（Little-Endian）
    ByteBuffer buffer = ByteBuffer.allocate(4)
        .order(ByteOrder.LITTLE_ENDIAN);

    // 2. 写入低16位（reg1）：按小端序写入2字节（offset=0）
    // 注意：Java的short是16位有符号，但这里按无符号处理（& 0xFFFF确保无符号）
    buffer.putShort(0, (short) (reg1 & 0xFFFF));

    // 3. 写入高16位（reg2）：按小端序写入2字节（offset=2）
    buffer.putShort(2, (short) (reg2 & 0xFFFF));

    // 4. 重置缓冲区指针到起始位置，准备读取
    buffer.rewind();

    // 5. 以小端序读取4字节并解析为单精度浮点数（float）
    return buffer.getFloat();
  }


  /**
   * Updates the vehicle status from Modbus registers.
   */
  private void updateVehicleStatus() {
    if (!isVehicleConnected()) {
      return;
    }

    try {
      // 读取 AGV 状态 (假设状态在输入寄存器 0-1)
      ReadInputRegistersRequest req = new ReadInputRegistersRequest(0, 43);
      transaction.setRequest(req);
      transaction.execute();

      var res = (ReadInputRegistersResponse) transaction.getResponse();
      if (res != null) {
        // x - float32 [0001, 0002]
        float x = _toFloat(res.getRegisterValue(0), res.getRegisterValue(1));
        float y = _toFloat(res.getRegisterValue(2), res.getRegisterValue(3));
        float z = 0;
        float r = _toFloat(res.getRegisterValue(4), res.getRegisterValue(5));
        // 0-无，1-等待执行，2-正在执行，3-暂停，4-到达，5-失败，6-取消，7-超时
        int state = res.getRegisterValue(8);
        int battery = res.getRegisterValue(12);
        // 更新状态
        switch (state) {
          case 0:
            getProcessModel().setState(Vehicle.State.IDLE);
            break;
          case 1:
          case 2:
          case 3:
            getProcessModel().setState(Vehicle.State.CHARGING);
            break;
          case 5:
            getProcessModel().setState(Vehicle.State.ERROR);
            break;
          default:
            // TODO
            getProcessModel().setState(Vehicle.State.EXECUTING);
            getProcessModel().setState(Vehicle.State.UNKNOWN);
            break;
        }
        // 更新电量
        getProcessModel().setEnergyLevel(battery);
        // 更新位置
        getProcessModel().setPose(
            getProcessModel().getPose().withPosition(new Triple((long) x, (long) y, (long) z))
        );
      }
    }
    catch (Exception e) {
      LOG.warn("Error updating vehicle status: {}", e.getMessage());
      // 通信失败，不抛出异常，让轮询继续
    }
  }

  /**
   * Sends a movement command to the AGV via Modbus.
   */
  private void sendMovementCommand(MovementCommand cmd)
      throws ModbusException {
    if (!isVehicleConnected()) {
      throw new IllegalStateException("Not connected to vehicle");
    }

    Step step = cmd.getStep();
    if (step != null) {
      try {
        // 假设目标点 ID 存储在保持寄存器 0-1
        String pointName = step.getDestinationPoint().getName();
        int targetPointId = Integer.parseInt(pointName.replaceAll("\\D", ""));

        net.wimpi.modbus.procimg.Register[] registers = new net.wimpi.modbus.procimg.Register[2];
        registers[0] = new net.wimpi.modbus.procimg.SimpleRegister(1); // 命令类型: 移动
        registers[1] = new net.wimpi.modbus.procimg.SimpleRegister(targetPointId); // 目标点 ID
        WriteMultipleRegistersRequest req = new WriteMultipleRegistersRequest(0, registers);
        req.setUnitID(1);

        transaction.setRequest(req);
        transaction.execute();

        LOG.info("Sent movement command to point: {}", pointName);
      }
      catch (NumberFormatException e) {
        LOG.error("Invalid point name format: {}", step.getDestinationPoint().getName(), e);
        throw new ModbusException(
            "Invalid point name format: " + step.getDestinationPoint().getName()
        );
      }
      catch (Exception e) {
        LOG.error("Failed to send movement command: {}", e.getMessage(), e);
        throw e;
      }
    }
  }

  /**
   * Processes movement commands from the queue.
   */
  private void processCommand() {
    if (isCommunicationRunning) {
      return; // 已经有线程在处理命令，避免重复处理
    }

    isCommunicationRunning = true;
    LOG.debug("Starting command processing loop");

    try {
      while (isInitialized() && isVehicleConnected() && (!getSentCommands().isEmpty()
          || !getUnsentCommands().isEmpty())) {
        // 处理未发送的命令
        while (!getUnsentCommands().isEmpty()) {
          MovementCommand cmd = getUnsentCommands().poll();
          if (cmd != null) {
            try {
              sendMovementCommand(cmd);
              getSentCommands().add(cmd);
              getProcessModel().setState(Vehicle.State.EXECUTING);
            }
            catch (Exception e) {
              LOG.error("Failed to send movement command: {}", e.getMessage(), e);
              getProcessModel().commandFailed(cmd);
            }
          }
        }

        // 检查是否有命令需要处理
        if (!getSentCommands().isEmpty()) {
          MovementCommand currentCommand = getSentCommands().peek();
          // 等待命令完成 (通过状态轮询检测)
          boolean commandCompleted = false;
          int maxRetries = 30; // 最多等待 30 秒
          int retryCount = 0;

          while (isInitialized() && isVehicleConnected() && retryCount < maxRetries) {
            try {
              Thread.sleep(COMMUNICATION_PERIOD);
              retryCount++;

              // 检查是否完成
              if (getProcessModel().getState() == Vehicle.State.IDLE) {
                if (Objects.equals(getSentCommands().peek(), currentCommand)) {
                  getProcessModel().commandExecuted(getSentCommands().poll());
                  LOG.debug("Command executed: {}", currentCommand);
                }
                else {
                  LOG.warn(
                      "{}: Processed command not oldest in sent queue: {} != {}",
                      getName(),
                      currentCommand,
                      getSentCommands().peek()
                  );
                }
                commandCompleted = true;
                break;
              }
            }
            catch (InterruptedException e) {
              LOG.warn("Command processing interrupted: {}", e.getMessage());
              Thread.currentThread().interrupt();
              break;
            }
            catch (Exception e) {
              LOG.error("Error processing command: {}", e.getMessage(), e);
              break;
            }
          }

          if (!commandCompleted) {
            LOG.warn("Command timed out: {}", currentCommand);
            if (Objects.equals(getSentCommands().peek(), currentCommand)) {
              getProcessModel().commandFailed(getSentCommands().poll());
            }
          }
        }
        else {
          // 没有命令需要处理，短暂休眠后继续检查
          try {
            Thread.sleep(COMMUNICATION_PERIOD);
          }
          catch (InterruptedException e) {
            LOG.warn("Command processing interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
            break;
          }
        }
      }
    }
    finally {
      isCommunicationRunning = false;
      LOG.debug("Command processing loop ended");
      getProcessModel().setState(Vehicle.State.IDLE);
    }
  }

  /**
   * The vehicle's possible load states.
   */
  private enum LoadState {
    EMPTY,
    FULL;
  }
}
