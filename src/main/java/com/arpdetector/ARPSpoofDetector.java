package com.arpdetector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ARPSpoofDetector {

    private final Map<String, Device> arpCache = new ConcurrentHashMap<>();
    private final Set<String> trustedMacs = new HashSet<>();
    private final DeviceTableModel deviceModel;
    private final AlertListModel alertModel;
    private final javax.swing.JTextArea logArea;
    private final javax.swing.JLabel statusLabel;
    private final javax.swing.JLabel devicesCountLabel;
    private final javax.swing.JLabel alertsCountLabel;
    private final javax.swing.JFrame mainFrame;

    // Для flood-детекции
    private int packetCountLastSecond = 0;
    private long lastFloodCheckTime = System.currentTimeMillis();
    private static final int MAX_PACKETS_PER_SECOND = 50;
    private static final int FLOOD_ALERT_COOLDOWN = 60000;
    private long lastFloodAlertTime = 0;

    // Для активного сканирования
    private static final int ACTIVE_SCAN_INTERVAL = 60000; // 1 минута
    private boolean activeScanEnabled = true;

    private String gatewayIp;
    private String gatewayMac;
    private boolean isRunning = false;
    private ScheduledExecutorService scheduler;
    private boolean soundEnabled = true;

    public ARPSpoofDetector(DeviceTableModel deviceModel, AlertListModel alertModel,
                            javax.swing.JTextArea logArea, javax.swing.JLabel statusLabel,
                            javax.swing.JLabel devicesCountLabel, javax.swing.JLabel alertsCountLabel,
                            javax.swing.JFrame mainFrame) {
        this.deviceModel = deviceModel;
        this.alertModel = alertModel;
        this.logArea = logArea;
        this.statusLabel = statusLabel;
        this.devicesCountLabel = devicesCountLabel;
        this.alertsCountLabel = alertsCountLabel;
        this.mainFrame = mainFrame;

        trustedMacs.add("FF:FF:FF:FF:FF:FF");
        trustedMacs.add("00:00:00:00:00:00");
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void setActiveScanEnabled(boolean enabled) {
        this.activeScanEnabled = enabled;
    }

    public void start() {
        if (isRunning) return;

        isRunning = true;
        statusLabel.setText("🟢 ЗАЩИТА АКТИВНА");
        log("Мониторинг ARP-трафика запущен");

        detectGateway();

        scheduler = Executors.newScheduledThreadPool(3);

        // Пассивное сканирование ARP-таблицы (каждые 3 секунды)
        scheduler.scheduleAtFixedRate(this::scanARPTable, 1, 3, TimeUnit.SECONDS);

        // Проверка активности устройств (каждые 10 секунд)
        scheduler.scheduleAtFixedRate(this::checkDeviceActivity, 5, 10, TimeUnit.SECONDS);

        // Активное сканирование сети (каждую минуту)
        if (activeScanEnabled) {
            scheduler.scheduleAtFixedRate(this::activeScan, 10, ACTIVE_SCAN_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        if (!isRunning) return;

        isRunning = false;
        if (scheduler != null) {
            scheduler.shutdown();
        }
        statusLabel.setText("⏸ ЗАЩИТА ОСТАНОВЛЕНА");
        log("Мониторинг остановлен");
    }

    /**
     * Активное сканирование сети (Ping sweep) для обнаружения всех устройств
     */
    private void activeScan() {
        if (!isRunning || gatewayIp == null) return;

        log("🔍 Выполняется активное сканирование сети...");

        String networkPrefix = gatewayIp.substring(0, gatewayIp.lastIndexOf("."));
        int foundCount = 0;

        for (int i = 1; i <= 254; i++) {
            String ip = networkPrefix + "." + i;

            // Пропускаем широковещательный адрес и адрес сети
            if (ip.equals(gatewayIp)) continue;
            if (ip.endsWith(".0") || ip.endsWith(".255")) continue;

            try {
                InetAddress address = InetAddress.getByName(ip);
                // Таймаут 300 мс для быстрого сканирования
                if (address.isReachable(300)) {
                    foundCount++;
                    // После ping устройство появится в ARP-таблице
                    if (foundCount % 10 == 0) {
                        Thread.sleep(10); // Небольшая пауза, чтобы не перегружать сеть
                    }
                }
            } catch (Exception e) {
                // Игнорируем ошибки
            }
        }

        log("✅ Активное сканирование завершено. Обнаружено " + foundCount + " активных устройств.");
    }

    private void detectGateway() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                Process process = Runtime.getRuntime().exec("ipconfig");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "CP866"));
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.contains("Default Gateway") && !line.contains(":")) {
                        String[] parts = line.trim().split("\\s+");
                        for (String part : parts) {
                            if (part.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                                gatewayIp = part;
                                break;
                            }
                        }
                        if (gatewayIp != null) break;
                    }
                }
                reader.close();
            } else {
                Process process = Runtime.getRuntime().exec("route -n");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.contains("UG") || line.contains("default")) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length >= 2) {
                            if (parts[0].equals("default")) gatewayIp = parts[1];
                            else if (parts[1].equals("UG")) gatewayIp = parts[0];
                        }
                        if (gatewayIp != null) break;
                    }
                }
                reader.close();
            }

            if (gatewayIp == null) {
                gatewayIp = "192.168.1.1";
            }

            findGatewayMac();

            // Добавляем шлюз вручную, если его нет в ARP-таблице
            if (gatewayMac == null) {
                gatewayMac = "Неизвестен";
                log("⚠️ Шлюз определён (" + gatewayIp + "), но MAC-адрес не найден в ARP-таблице");
            } else {
                log("Шлюз: " + gatewayIp + " (" + gatewayMac + ")");
            }

        } catch (Exception e) {
            log("Ошибка определения шлюза: " + e.getMessage());
        }
    }

    private void findGatewayMac() {
        try {
            Process process = Runtime.getRuntime().exec("arp -a");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains(gatewayIp)) {
                    String[] parts = line.split("\\s+");
                    for (String part : parts) {
                        if (part.matches("([0-9A-Fa-f]{2}[-:]){5}[0-9A-Fa-f]{2}")) {
                            gatewayMac = part.replace('-', ':').toUpperCase();
                            break;
                        }
                    }
                    if (gatewayMac != null) break;
                }
            }
            reader.close();
        } catch (Exception e) {
            log("Ошибка получения MAC шлюза: " + e.getMessage());
        }
    }

    private void scanARPTable() {
        if (!isRunning) return;

        try {
            Process process = Runtime.getRuntime().exec("arp -a");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int packetCountThisScan = 0;

            while ((line = reader.readLine()) != null) {
                if (parseARPLine(line)) {
                    packetCountThisScan++;
                }
            }
            reader.close();

            packetCountLastSecond += packetCountThisScan;
            checkARPFllood();

            javax.swing.SwingUtilities.invokeLater(() -> {
                int fullSize = deviceModel.getFullSize();
                int displayedSize = deviceModel.getRowCount();
                if (fullSize == displayedSize) {
                    devicesCountLabel.setText("Устройств: " + fullSize);
                } else {
                    devicesCountLabel.setText("Устройств: " + fullSize + " (показано: " + displayedSize + ")");
                }
                alertsCountLabel.setText("Оповещений: " + alertModel.getSize());
            });

        } catch (Exception e) {
            log("Ошибка сканирования ARP: " + e.getMessage());
        }
    }

    private void checkARPFllood() {
        long now = System.currentTimeMillis();

        if (now - lastFloodCheckTime >= 1000) {
            if (packetCountLastSecond > MAX_PACKETS_PER_SECOND) {
                if (now - lastFloodAlertTime > FLOOD_ALERT_COOLDOWN) {
                    Alert alert = new Alert(
                            Alert.AlertType.ARP_FLOOD,
                            "Обнаружена DoS-атака! " + packetCountLastSecond + " ARP-пакетов за секунду",
                            "Локальная сеть",
                            "Норма: ≤" + MAX_PACKETS_PER_SECOND,
                            "Обнаружено: " + packetCountLastSecond
                    );
                    addAlert(alert);
                    lastFloodAlertTime = now;
                    log("⚠️ ВНИМАНИЕ: Обнаружен ARP-flood! Пакетов за секунду: " + packetCountLastSecond);
                }
            }
            packetCountLastSecond = 0;
            lastFloodCheckTime = now;
        }
    }

    private boolean parseARPLine(String line) {
        line = line.trim();
        if (line.isEmpty()) return false;

        String[] parts = line.split("\\s+");
        if (parts.length < 2) return false;

        String ip = null;
        String mac = null;

        for (String part : parts) {
            if (part.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                ip = part;
            }
            if (part.matches("([0-9A-Fa-f]{2}[-:]){5}[0-9A-Fa-f]{2}")) {
                mac = part.replace('-', ':').toUpperCase();
            }
        }

        if (ip == null || mac == null) return false;

        if (mac.equals("FF:FF:FF:FF:FF:FF") || mac.startsWith("01:00:5E") ||
                mac.startsWith("00:00:00:00:00") || mac.startsWith("33:33:")) {
            return false;
        }

        processDevice(ip, mac);
        return true;
    }

    private void processDevice(String ip, String mac) {
        Device existing = deviceModel.getDevice(ip);

        if (ip.equals(gatewayIp)) {
            if (gatewayMac != null && !gatewayMac.equals(mac) && !gatewayMac.equals("Неизвестен")) {
                Alert alert = new Alert(
                        Alert.AlertType.GATEWAY_SPOOF,
                        "Обнаружена попытка подмены шлюза!",
                        ip, gatewayMac, mac
                );
                addAlert(alert);
                log("🔴 КРИТИЧЕСКАЯ АТАКА: Подмена шлюза! Ожидался " + gatewayMac + ", получен " + mac);
            }
            gatewayMac = mac;
        }

        if (existing == null) {
            Device device = new Device(ip, mac);
            String vendor = MacVendorLookup.lookup(mac);
            device.setVendor(vendor);
            device.setStatus(Device.STATUS_ONLINE);
            device.updateLastSeen();
            device.incrementPacketCount();

            if (ip.equals(gatewayIp)) {
                device.setGateway(true);
                log("Обнаружен шлюз: " + ip + " (" + mac + ") - " + vendor);
            } else {
                log("Обнаружено новое устройство: " + ip + " (" + mac + ") - " + vendor);
            }

            deviceModel.addDevice(device);

        } else {
            existing.updateLastSeen();
            existing.incrementPacketCount();

            if (!existing.getMacAddress().equals(mac)) {
                Alert alert = new Alert(
                        Alert.AlertType.MAC_CHANGE,
                        "MAC-адрес для IP " + ip + " изменился",
                        ip, existing.getMacAddress(), mac
                );
                addAlert(alert);

                log("⚠️ ПОДОЗРИТЕЛЬНО: MAC-адрес для " + ip + " изменился с " +
                        existing.getMacAddress() + " на " + mac);

                existing.setMacAddress(mac);
                existing.setVendor(MacVendorLookup.lookup(mac));
            }

            if (!existing.getStatus().equals(Device.STATUS_SUSPICIOUS) &&
                    !existing.getStatus().equals(Device.STATUS_ATTACKER)) {
                if (!existing.getStatus().equals(Device.STATUS_ONLINE)) {
                    existing.setStatus(Device.STATUS_ONLINE);
                }
            }

            deviceModel.updateDevice(existing);
        }
    }

    private void checkDeviceActivity() {
        if (!isRunning) return;

        long now = System.currentTimeMillis();
        long timeout = 30000;

        for (Device device : deviceModel.getAllDevices()) {
            if (device.isGateway()) continue;

            if (now - device.getLastSeen() > timeout) {
                if (!device.getStatus().equals(Device.STATUS_OFFLINE)) {
                    device.setStatus(Device.STATUS_OFFLINE);
                    deviceModel.updateDevice(device);
                    log("Устройство " + device.getIpAddress() + " перешло в офлайн");
                }
            } else {
                if (device.getStatus().equals(Device.STATUS_OFFLINE)) {
                    device.setStatus(Device.STATUS_ONLINE);
                    deviceModel.updateDevice(device);
                    log("Устройство " + device.getIpAddress() + " вернулось в онлайн");
                }
            }
        }
    }

    private void addAlert(Alert alert) {
        alertModel.addAlert(alert);

        String logMessage = String.format("[%s] %s %s: %s",
                alert.getFormattedTime(),
                alert.getThreatLevel().getIcon(),
                alert.getType().getName(),
                alert.getDescription()
        );

        log(logMessage);

        if (soundEnabled && (alert.getThreatLevel() == Alert.ThreatLevel.CRITICAL ||
                alert.getThreatLevel() == Alert.ThreatLevel.HIGH)) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }

        if (alert.getThreatLevel() == Alert.ThreatLevel.CRITICAL) {
            flashWindow();
        }
    }

    private void log(String message) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date());
        javax.swing.SwingUtilities.invokeLater(() -> {
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void flashWindow() {
        new Thread(() -> {
            for (int i = 0; i < 4; i++) {
                try {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        mainFrame.toFront();
                        mainFrame.repaint();
                    });
                    Thread.sleep(200);
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        mainFrame.toBack();
                        mainFrame.repaint();
                    });
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    break;
                }
            }
            javax.swing.SwingUtilities.invokeLater(mainFrame::toFront);
        }).start();
    }

    public void exportData() {
        StringBuilder sb = new StringBuilder();
        sb.append("════════════════════════════════════════════════════════════\n");
        sb.append("              ОТЧЕТ ОБ ARP-АТАКАХ\n");
        sb.append("════════════════════════════════════════════════════════════\n");
        sb.append("Дата: ").append(new Date()).append("\n\n");

        sb.append("--- АКТИВНЫЕ УСТРОЙСТВА (").append(deviceModel.getFullSize()).append(" шт.) ---\n");
        sb.append("┌─────────────────┬─────────────────┬─────────────────────┬──────────────────┬─────────────┐\n");
        sb.append("│ IP-адрес        │ MAC-адрес       │ Производитель       │ Статус           │ Пакетов     │\n");
        sb.append("├─────────────────┼─────────────────┼─────────────────────┼──────────────────┼─────────────┤\n");

        for (Device d : deviceModel.getAllDevices()) {
            sb.append(String.format("│ %-15s │ %-17s │ %-19s │ %-16s │ %-11d │\n",
                    d.getIpAddress(), d.getMacAddress(),
                    d.getVendor().length() > 19 ? d.getVendor().substring(0, 16) + "..." : d.getVendor(),
                    d.getStatus(), d.getPacketCount()));
        }
        sb.append("└─────────────────┴─────────────────┴─────────────────────┴──────────────────┴─────────────┘\n\n");

        sb.append("--- ОПОВЕЩЕНИЯ (").append(alertModel.getSize()).append(" шт.) ---\n");
        for (Alert a : alertModel.getAlerts()) {
            sb.append(String.format("[%s] %s | %s | %s\n",
                    a.getFormattedDateTime(), a.getThreatLevel().getName(),
                    a.getType().getName(), a.getDescription()));
        }

        int critical = 0, high = 0, medium = 0, low = 0, info = 0;
        for (Alert a : alertModel.getAlerts()) {
            switch (a.getThreatLevel()) {
                case CRITICAL: critical++; break;
                case HIGH: high++; break;
                case MEDIUM: medium++; break;
                case LOW: low++; break;
                default: info++; break;
            }
        }

        sb.append("\n--- СТАТИСТИКА УГРОЗ ---\n");
        sb.append("  🔴 Критические: ").append(critical).append("\n");
        sb.append("  🟠 Высокие: ").append(high).append("\n");
        sb.append("  🟡 Средние: ").append(medium).append("\n");
        sb.append("  🟢 Низкие: ").append(low).append("\n");
        sb.append("  ℹ️ Информационные: ").append(info).append("\n");

        try {
            String filename = "arp_report_" + System.currentTimeMillis() + ".txt";
            java.io.PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter(filename));
            out.print(sb.toString());
            out.close();
            log("📁 Данные экспортированы в файл: " + filename);
        } catch (Exception e) {
            log("❌ Ошибка экспорта: " + e.getMessage());
        }
    }

    public void simulateARPAttack() {
        if (!isRunning) return;

        Alert alert = new Alert(
                Alert.AlertType.GATEWAY_SPOOF,
                "ИМИТАЦИЯ АТАКИ: Обнаружена попытка подмены шлюза!",
                gatewayIp != null ? gatewayIp : "192.168.1.1",
                gatewayMac != null ? gatewayMac : "AA:BB:CC:DD:EE:FF",
                "CC:DD:EE:FF:00:11"
        );
        addAlert(alert);

        log("🔴 ИМИТАЦИЯ: Выполнена тестовая ARP-атака!");

        if (soundEnabled) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }
}