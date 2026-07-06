package com.arpdetector;

public class Device {
    private String ipAddress;
    private String macAddress;
    private String vendor;
    private String hostname;
    private String status;
    private boolean isGateway;
    private long lastSeen;
    private int packetCount;

    public static final String STATUS_ONLINE = "🟢 ОНЛАЙН";
    public static final String STATUS_OFFLINE = "⚪ ОФЛАЙН";
    public static final String STATUS_SUSPICIOUS = "🟡 ПОДОЗРИТЕЛЬНЫЙ";
    public static final String STATUS_ATTACKER = "🔴 АТАКУЮЩИЙ";

    public Device(String ipAddress, String macAddress) {
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.vendor = "Неизвестно";
        this.hostname = "";
        this.status = STATUS_OFFLINE;
        this.isGateway = false;
        this.lastSeen = System.currentTimeMillis();
        this.packetCount = 0;
    }

    // Геттеры и сеттеры
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isGateway() { return isGateway; }
    public void setGateway(boolean gateway) { isGateway = gateway; }

    public long getLastSeen() { return lastSeen; }
    public void updateLastSeen() { this.lastSeen = System.currentTimeMillis(); }

    public int getPacketCount() { return packetCount; }
    public void setPacketCount(int packetCount) { this.packetCount = packetCount; }
    public void incrementPacketCount() { this.packetCount++; }

    public String getGatewayIcon() {
        return isGateway ? "🌐 " : "";
    }

    @Override
    public String toString() {
        return String.format("%s%-15s | %-17s | %-12s | %s",
                getGatewayIcon(), ipAddress, macAddress, vendor, status);
    }
}