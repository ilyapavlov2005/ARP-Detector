package com.arpdetector;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.ArrayList;

public class MainWindow extends JFrame {

    private DeviceTableModel deviceModel;
    private AlertListModel alertModel;
    private ARPSpoofDetector detector;

    private JLabel statusLabel;
    private JLabel interfaceLabel;
    private JLabel gatewayLabel;
    private JLabel devicesCountLabel;
    private JLabel alertsCountLabel;
    private JTextArea logArea;
    private JTable devicesTable;
    private JList<Alert> alertsList;

    // Кнопки
    private JButton startBtn;
    private JButton pauseBtn;
    private JButton exportBtn;
    private JButton testBtn;
    private JButton statsBtn;
    private JButton settingsBtn;
    private JButton clearAlertsBtn;
    private JButton resetFilterBtn;

    private JCheckBox soundCheckBox;
    private JCheckBox activeScanCheckBox;

    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;

    // Цвета кнопок
    // ========== ПРИЯТНЫЕ, СПОКОЙНЫЕ ЦВЕТА КНОПОК ==========
    // Современные насыщенные цвета
    // Пастельная палитра
    private static final Color COLOR_START = new Color(129, 199, 132);    // Светло-зелёный
    private static final Color COLOR_PAUSE = new Color(239, 83, 80);      // Светло-красный
    private static final Color COLOR_EXPORT = new Color(66, 165, 245);    // Светло-синий
    private static final Color COLOR_TEST = new Color(66, 165, 245);      // Светло-фиолетовый
    private static final Color COLOR_STATS = new Color(66, 165, 245);     // Светло-синий
    private static final Color COLOR_SETTINGS = new Color(66, 165, 245); // Светло-серо-синий
    private static final Color COLOR_CLEAR = new Color(120, 144, 156);     // Светло-коричневый
    private static final Color COLOR_RESET = new Color(120, 144, 156);    // Светло-серо-синий

// И для текста на светлых кнопках используй ТЁМНЫЙ цвет

    private static final Color COLOR_DISABLED = new Color(158, 158, 158); // Светло-серый

    // Цвета для статусов
    private static final Color COLOR_STATUS_ONLINE = new Color(56, 142, 60);
    private static final Color COLOR_STATUS_OFFLINE = new Color(117, 117, 117);
    private static final Color COLOR_STATUS_SUSPICIOUS = new Color(255, 160, 0);
    private static final Color COLOR_STATUS_ATTACKER = new Color(211, 47, 47);
    private static final Color COLOR_GATEWAY = new Color(21, 101, 192);
    private static final Color COLOR_ROW_EVEN = new Color(255, 255, 255);
    private static final Color COLOR_ROW_ODD = new Color(248, 250, 252);
    private static final Color COLOR_SELECTED = new Color(227, 242, 253);

    public MainWindow() {
        setTitle("🛡️ ARP Spoofing Detector - Обнаружение ARP-атак");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 650));

        initComponents();
        setupLayout();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (detector != null) detector.stop();
                System.exit(0);
            }
        });

        detector = new ARPSpoofDetector(deviceModel, alertModel, logArea,
                statusLabel, devicesCountLabel, alertsCountLabel, this);
    }

    private void initComponents() {
        deviceModel = new DeviceTableModel();
        alertModel = new AlertListModel();

        createStatusComponents();
        createDeviceTable();
        createAlertsList();
        createLogArea();
        createButtons();
    }

    private void createStatusComponents() {
        statusLabel = new JLabel("🟡 НЕ ЗАПУЩЕН");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        interfaceLabel = new JLabel("Интерфейс: Определяется...");
        gatewayLabel = new JLabel("Шлюз: Определяется...");
        devicesCountLabel = new JLabel("0");
        alertsCountLabel = new JLabel("0");
    }

    private void createDeviceTable() {
        devicesTable = new JTable(deviceModel);
        devicesTable.setRowHeight(28);
        devicesTable.setFont(new Font("Consolas", Font.PLAIN, 12));
        devicesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        devicesTable.getTableHeader().setBackground(new Color(240, 242, 245));
        devicesTable.setSelectionBackground(COLOR_SELECTED);
        devicesTable.setShowGrid(true);
        devicesTable.setGridColor(new Color(224, 224, 224));

        devicesTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        devicesTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        devicesTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        devicesTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        devicesTable.getColumnModel().getColumn(4).setPreferredWidth(110);

        devicesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Consolas", Font.PLAIN, 12));

                if (!isSelected) {
                    setBackground(row % 2 == 0 ? COLOR_ROW_EVEN : COLOR_ROW_ODD);
                } else {
                    setBackground(COLOR_SELECTED);
                }

                if (column == 4) {
                    String status = (String) value;
                    if (status != null) {
                        if (status.contains("ОНЛАЙН")) setForeground(COLOR_STATUS_ONLINE);
                        else if (status.contains("ШЛЮЗ")) {
                            setForeground(COLOR_GATEWAY);
                            setText("🌐 " + status);
                        }
                        else if (status.contains("ПОДОЗРИТЕЛЬНЫЙ")) setForeground(COLOR_STATUS_SUSPICIOUS);
                        else if (status.contains("АТАКУЮЩИЙ")) {
                            setForeground(COLOR_STATUS_ATTACKER);
                            setFont(new Font("Consolas", Font.BOLD, 12));
                        }
                        else if (status.contains("ОФЛАЙН")) setForeground(COLOR_STATUS_OFFLINE);
                    }
                } else if (column == 0) {
                    String ip = (String) value;
                    if (ip != null && ip.startsWith("🌐")) {
                        setForeground(COLOR_GATEWAY);
                        setFont(new Font("Consolas", Font.BOLD, 12));
                    } else {
                        setForeground(new Color(33, 33, 33));
                    }
                } else {
                    setForeground(new Color(33, 33, 33));
                }
                return c;
            }
        });
    }

    private void createAlertsList() {
        alertsList = new JList<>(alertModel);
        alertsList.setCellRenderer(new AlertListCellRenderer());
        alertsList.setFont(new Font("Consolas", Font.PLAIN, 12));
        alertsList.setFixedCellHeight(36);
        alertsList.setSelectionBackground(new Color(227, 242, 253));

        alertsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    Alert selected = alertsList.getSelectedValue();
                    if (selected != null) showAlertDetails(selected);
                }
            }
        });
    }

    private void createLogArea() {
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 35, 40));
        logArea.setForeground(new Color(220, 225, 230));
        logArea.setCaretColor(new Color(100, 200, 100));
        logArea.setLineWrap(true);
    }

    /**
     * Создание кнопок с разными цветами:
     * - СТАРТ - зелёный
     * - ПАУЗА - красный
     * - Остальные - синий (COLOR_DEFAULT)
     */
    private void createButtons() {
        startBtn = createStyledButton("▶ СТАРТ", COLOR_START);
        pauseBtn = createStyledButton("⏸ ПАУЗА", COLOR_PAUSE);
        exportBtn = createStyledButton("📁 ЭКСПОРТ", COLOR_EXPORT);
        testBtn = createStyledButton("🎯 ТЕСТ", COLOR_TEST);
        statsBtn = createStyledButton("📊 СТАТИСТИКА", COLOR_STATS);
        settingsBtn = createStyledButton("⚙ НАСТРОЙКИ", COLOR_SETTINGS);
        clearAlertsBtn = createStyledButton("🗑 ОЧИСТИТЬ", COLOR_CLEAR);
        resetFilterBtn = createStyledButton("⟳ Сброс", COLOR_RESET);

        soundCheckBox = new JCheckBox("🔊 Звук");
        soundCheckBox.setSelected(true);
        soundCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        soundCheckBox.setBackground(Color.WHITE);
        soundCheckBox.setFocusPainted(false);

        activeScanCheckBox = new JCheckBox("🌐 Активное сканирование");
        activeScanCheckBox.setSelected(true);
        activeScanCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        activeScanCheckBox.setBackground(Color.WHITE);
        activeScanCheckBox.setFocusPainted(false);

        pauseBtn.setEnabled(false);

        startBtn.addActionListener(e -> {
            detector.setActiveScanEnabled(activeScanCheckBox.isSelected());
            detector.start();
            startBtn.setEnabled(false);
            pauseBtn.setEnabled(true);
        });

        pauseBtn.addActionListener(e -> {
            detector.stop();
            startBtn.setEnabled(true);
            pauseBtn.setEnabled(false);
        });

        exportBtn.addActionListener(e -> detector.exportData());
        testBtn.addActionListener(e -> detector.simulateARPAttack());
        statsBtn.addActionListener(e -> showStatistics());
        settingsBtn.addActionListener(e -> showSettings());
        clearAlertsBtn.addActionListener(e -> {
            alertModel.clear();
            alertsCountLabel.setText("0");
            log("Журнал оповещений очищен");
        });
        resetFilterBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
        });
        soundCheckBox.addActionListener(e -> detector.setSoundEnabled(soundCheckBox.isSelected()));
        activeScanCheckBox.addActionListener(e -> {
            if (detector != null) detector.setActiveScanEnabled(activeScanCheckBox.isSelected());
        });
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);

        // Отключаем все эффекты рисования, которые могут мешать
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);

        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Устанавливаем границу вручную
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Принудительная перерисовка
        btn.repaint();

        // Эффект наведения
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
                btn.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
                btn.repaint();
            }
        });

        btn.addPropertyChangeListener("enabled", evt -> {
            if (btn.isEnabled()) {
                btn.setBackground(bgColor);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(COLOR_DISABLED);
                btn.setForeground(new Color(200, 200, 200));
            }
            btn.repaint();
        });

        return btn;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel filterLabel = new JLabel("🔍 Поиск:");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterDevices(); }
            public void removeUpdate(DocumentEvent e) { filterDevices(); }
            public void insertUpdate(DocumentEvent e) { filterDevices(); }
        });

        statusFilterCombo = new JComboBox<>(new String[]{"Все", "ОНЛАЙН", "ОФЛАЙН", "ПОДОЗРИТЕЛЬНЫЙ", "АТАКУЮЩИЙ"});
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusFilterCombo.addActionListener(e -> filterDevices());

        panel.add(filterLabel);
        panel.add(searchField);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(new JLabel("Статус:"));
        panel.add(statusFilterCombo);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(resetFilterBtn);

        return panel;
    }

    private void filterDevices() {
        String text = searchField.getText();
        String status = (String) statusFilterCombo.getSelectedItem();
        deviceModel.setFilter(text, status);

        int fullSize = deviceModel.getFullSize();
        int displayedSize = deviceModel.getRowCount();

        if (fullSize == displayedSize) {
            devicesCountLabel.setText("Устройств: " + fullSize);
        } else {
            devicesCountLabel.setText("Устройств: " + fullSize + " (показано: " + displayedSize + ")");
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = createStatusPanel();
        add(topPanel, BorderLayout.NORTH);

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplit.setDividerLocation(650);
        centerSplit.setResizeWeight(0.6);
        centerSplit.setBorder(null);

        centerSplit.setLeftComponent(createDevicesPanel());
        centerSplit.setRightComponent(createAlertsPanel());

        add(centerSplit, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        panel.setBackground(new Color(250, 250, 252));

        JPanel statusGrid = new JPanel(new GridLayout(2, 3, 30, 15));
        statusGrid.setBackground(new Color(250, 250, 252));

        statusGrid.add(createStatusCard("📊 СТАТУС", statusLabel, true));
        statusGrid.add(createStatusCard("📱 УСТРОЙСТВА", devicesCountLabel, false));
        statusGrid.add(createStatusCard("⚠️ ОПОВЕЩЕНИЯ", alertsCountLabel, false));
        statusGrid.add(createStatusCard("🌐 ИНТЕРФЕЙС", interfaceLabel, false));
        statusGrid.add(createStatusCard("🚪 ШЛЮЗ", gatewayLabel, false));

        panel.add(statusGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatusCard(String title, JLabel valueLabel, boolean isMain) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(250, 250, 252));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLabel.setForeground(new Color(120, 120, 130));

        valueLabel.setFont(new Font("Segoe UI", isMain ? Font.BOLD : Font.BOLD, isMain ? 18 : 14));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDevicesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 210), 1),
                "📱 АКТИВНЫЕ УСТРОЙСТВА",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(80, 80, 90)
        ));

        panel.add(createFilterPanel(), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(devicesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scrollPane.getViewport().setBackground(COLOR_ROW_EVEN);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAlertsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 210), 1),
                "⚠️ ОПОВЕЩЕНИЯ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(80, 80, 90)
        ));

        JScrollPane scrollPane = new JScrollPane(alertsList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(clearAlertsBtn);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 210), 1),
                "📋 ЖУРНАЛ СОБЫТИЙ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(80, 80, 90)
        ));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(0, 140));
        logScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        logPanel.add(logScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(startBtn);
        buttonPanel.add(pauseBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(testBtn);
        buttonPanel.add(statsBtn);
        buttonPanel.add(settingsBtn);
        buttonPanel.add(soundCheckBox);
        buttonPanel.add(activeScanCheckBox);

        panel.add(logPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private String createAsciiBar(String label, int value, int total, int width) {
        int barLength = total > 0 ? (value * width / total) : 0;
        String bar = "█".repeat(Math.max(0, barLength)) + "░".repeat(Math.max(0, width - barLength));
        String percentage = total > 0 ? String.format("%.1f%%", (value * 100.0 / total)) : "0%";
        return String.format(" │ %-12s │ %-" + width + "s │ %4d (%s) │", label, bar, value, percentage);
    }

    private void showStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════════════════════\n");
        sb.append("                                    📊 СТАТИСТИКА\n");
        sb.append("═══════════════════════════════════════════════════════════════════════════════\n\n");

        sb.append("📱 Устройства в сети: ").append(deviceModel.getFullSize()).append("\n");
        sb.append("⚠️ Оповещений за сессию: ").append(alertModel.getSize()).append("\n");
        sb.append("🟢 Состояние защиты: ").append(statusLabel.getText()).append("\n");
        sb.append("🌐 Активное сканирование: ").append(activeScanCheckBox.isSelected() ? "ВКЛ" : "ВЫКЛ").append("\n\n");

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
        int totalAlerts = critical + high + medium + low + info;

        sb.append("📊 РАСПРЕДЕЛЕНИЕ ПО УРОВНЯМ УГРОЗ:\n");
        sb.append(" ┌─────────────────────────────────────────────────────────────────────────────┐\n");
        sb.append(createAsciiBar("🔴 Критические", critical, totalAlerts, 45)).append("\n");
        sb.append(createAsciiBar("🟠 Высокие", high, totalAlerts, 45)).append("\n");
        sb.append(createAsciiBar("🟡 Средние", medium, totalAlerts, 45)).append("\n");
        sb.append(createAsciiBar("🟢 Низкие", low, totalAlerts, 45)).append("\n");
        sb.append(createAsciiBar("ℹ️ Информационные", info, totalAlerts, 45)).append("\n");
        sb.append(" └─────────────────────────────────────────────────────────────────────────────┘\n\n");

        if (totalAlerts > 0) {
            sb.append("⏱️ АКТИВНОСТЬ ПО ЧАСАМ:\n");
            sb.append(" ┌─────────────────────────────────────────────────────────────────────────────┐\n");
            int[] hourStats = new int[24];
            for (Alert a : alertModel.getAlerts()) {
                hourStats[a.getTimestamp().getHours()]++;
            }
            int maxHour = 0;
            for (int h : hourStats) if (h > maxHour) maxHour = h;
            for (int hour = 0; hour < 24; hour++) {
                int barLength = maxHour > 0 ? (hourStats[hour] * 40 / maxHour) : 0;
                String bar = "█".repeat(Math.max(0, barLength));
                sb.append(String.format(" │ %02d:00 │ %-40s │ %3d │\n", hour, bar, hourStats[hour]));
            }
            sb.append(" └─────────────────────────────────────────────────────────────────────────────┘\n\n");
        }

        if (deviceModel.getFullSize() > 0) {
            sb.append("📡 САМЫЕ АКТИВНЫЕ УСТРОЙСТВА:\n");
            sb.append(" ┌─────────────────────────────────────────────────────────────────────────────┐\n");
            List<Device> sortedDevices = new ArrayList<>(deviceModel.getAllDevices());
            sortedDevices.sort((d1, d2) -> Integer.compare(d2.getPacketCount(), d1.getPacketCount()));
            int topCount = Math.min(5, sortedDevices.size());
            for (int i = 0; i < topCount; i++) {
                Device d = sortedDevices.get(i);
                sb.append(String.format(" │ %d. %-15s │ %-17s │ %-20s │ %d пакетов │\n",
                        i+1, d.getIpAddress(), d.getMacAddress(),
                        d.getVendor().length() > 20 ? d.getVendor().substring(0, 17) + "..." : d.getVendor(),
                        d.getPacketCount()));
            }
            sb.append(" └─────────────────────────────────────────────────────────────────────────────┘\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.setEditable(false);
        textArea.setBackground(new Color(30, 35, 40));
        textArea.setForeground(new Color(220, 225, 230));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 550));

        JOptionPane.showMessageDialog(this, scrollPane, "Статистика", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSettings() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JCheckBox autoStartCheck = new JCheckBox("Автоматический запуск при старте");
        JCheckBox soundAlertsCheck = new JCheckBox("Звуковые оповещения");
        soundAlertsCheck.setSelected(soundCheckBox.isSelected());
        JCheckBox activeScanCheck = new JCheckBox("Активное сканирование сети (ping sweep)");
        activeScanCheck.setSelected(activeScanCheckBox.isSelected());

        JTextField scanIntervalField = new JTextField("3", 5);
        JTextField timeoutField = new JTextField("30", 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(autoStartCheck, gbc);
        gbc.gridy = 1;
        panel.add(soundAlertsCheck, gbc);
        gbc.gridy = 2;
        panel.add(activeScanCheck, gbc);

        gbc.gridy = 3;
        panel.add(new JLabel("Интервал сканирования (сек):"), gbc);
        gbc.gridx = 1;
        panel.add(scanIntervalField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Таймаут офлайн (сек):"), gbc);
        gbc.gridx = 1;
        panel.add(timeoutField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(new JLabel("ℹ️ Активное сканирование помогает обнаружить больше устройств"), gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Настройки",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            detector.setSoundEnabled(soundAlertsCheck.isSelected());
            detector.setActiveScanEnabled(activeScanCheck.isSelected());
            activeScanCheckBox.setSelected(activeScanCheck.isSelected());
            log("Настройки сохранены");
        }
    }

    private void showAlertDetails(Alert alert) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(alert.getThreatLevel().getIcon() + " " + alert.getType().getName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        switch (alert.getThreatLevel()) {
            case CRITICAL: titleLabel.setForeground(COLOR_STATUS_ATTACKER); break;
            case HIGH: titleLabel.setForeground(COLOR_STATUS_SUSPICIOUS); break;
            default: titleLabel.setForeground(COLOR_STATUS_ONLINE);
        }

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setBackground(panel.getBackground());
        detailsArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        detailsArea.setText(String.format(
                "Время: %s\nУровень: %s\nТип: %s\n\nОписание: %s\n\nIP-адрес: %s\nОжидаемый MAC: %s\nФактический MAC: %s",
                alert.getFormattedDateTime(), alert.getThreatLevel().getName(),
                alert.getType().getName(), alert.getDescription(), alert.getIpAddress(),
                alert.getExpectedMac() != null ? alert.getExpectedMac() : "Н/Д",
                alert.getActualMac() != null ? alert.getActualMac() : "Н/Д"
        ));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(detailsArea, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Закрыть");
        closeBtn.setBackground(COLOR_DISABLED);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(closeBtn);
            if (w != null) w.dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(this, "Детали оповещения", true);
        dialog.setContentPane(panel);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void log(String message) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        javax.swing.SwingUtilities.invokeLater(() -> {
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private static class AlertListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Alert) {
                Alert alert = (Alert) value;
                setText(alert.toString());
                setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
                if (!isSelected) {
                    switch (alert.getThreatLevel()) {
                        case CRITICAL:
                            setForeground(new Color(211, 47, 47));
                            setBackground(new Color(253, 237, 237));
                            break;
                        case HIGH:
                            setForeground(new Color(255, 160, 0));
                            setBackground(new Color(255, 248, 225));
                            break;
                        case MEDIUM:
                            setForeground(new Color(230, 140, 0));
                            setBackground(new Color(255, 250, 235));
                            break;
                        case LOW:
                            setForeground(new Color(56, 142, 60));
                            setBackground(new Color(232, 245, 233));
                            break;
                        default:
                            setForeground(new Color(100, 100, 100));
                            setBackground(Color.WHITE);
                    }
                } else {
                    setBackground(new Color(227, 242, 253));
                }
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
            }
            return c;
        }
    }
}