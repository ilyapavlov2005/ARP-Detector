package com.arpdetector;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Alert {
    private String id;
    private AlertType type;
    private String description;
    private String ipAddress;
    private String expectedMac;
    private String actualMac;
    private ThreatLevel threatLevel;
    private Date timestamp;
    private boolean acknowledged;

    public enum AlertType {
        MAC_CHANGE("MAC изменился", "MAC-адрес для IP изменился"),
        GATEWAY_SPOOF("Подмена шлюза", "Попытка подмены шлюза"),
        MULTIPLE_MACS("Множественные MAC", "Один IP у нескольких MAC"),
        SUSPICIOUS_DEVICE("Подозрительное устройство", "Обнаружено подозрительное устройство"),
        ARP_FLOOD("ARP-флуд", "Слишком много ARP-запросов");

        private final String name;
        private final String description;

        AlertType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    public enum ThreatLevel {
        INFO("ℹ️", "Инфо", "#3498db"),
        LOW("🟢", "Низкий", "#2ecc71"),
        MEDIUM("🟡", "Средний", "#f1c40f"),
        HIGH("🟠", "Высокий", "#e67e22"),
        CRITICAL("🔴", "Критический", "#e74c3c");

        private final String icon;
        private final String name;
        private final String color;

        ThreatLevel(String icon, String name, String color) {
            this.icon = icon;
            this.name = name;
            this.color = color;
        }

        public String getIcon() { return icon; }
        public String getName() { return name; }
        public String getColor() { return color; }
    }

    public Alert(AlertType type, String description, String ipAddress,
                 String expectedMac, String actualMac) {
        this.id = java.util.UUID.randomUUID().toString().substring(0, 8);
        this.type = type;
        this.description = description;
        this.ipAddress = ipAddress;
        this.expectedMac = expectedMac;
        this.actualMac = actualMac;
        this.timestamp = new Date();
        this.acknowledged = false;
        this.threatLevel = calculateThreatLevel(type);
    }

    private ThreatLevel calculateThreatLevel(AlertType type) {
        switch (type) {
            case GATEWAY_SPOOF: return ThreatLevel.CRITICAL;
            case MULTIPLE_MACS: return ThreatLevel.HIGH;
            case MAC_CHANGE: return ThreatLevel.MEDIUM;
            case ARP_FLOOD: return ThreatLevel.MEDIUM;
            default: return ThreatLevel.LOW;
        }
    }

    public String getId() { return id; }
    public AlertType getType() { return type; }
    public String getDescription() { return description; }
    public String getIpAddress() { return ipAddress; }
    public String getExpectedMac() { return expectedMac; }
    public String getActualMac() { return actualMac; }
    public ThreatLevel getThreatLevel() { return threatLevel; }
    public Date getTimestamp() { return timestamp; }
    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }

    public String getFormattedTime() {
        return new SimpleDateFormat("HH:mm:ss").format(timestamp);
    }

    public String getFormattedDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp);
    }

    @Override
    public String toString() {
        return String.format("%s [%s] %s - %s",
                threatLevel.getIcon(), getFormattedTime(), type.getName(), description);
    }
}