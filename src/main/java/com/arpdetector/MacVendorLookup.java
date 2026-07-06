package com.arpdetector;

import java.util.HashMap;
import java.util.Map;

public class MacVendorLookup {

    private static final Map<String, String> vendors = new HashMap<>();

    static {
        vendors.put("58:D9:D5", "ASUSTeK");
        vendors.put("5C:E0:C5", "Dell Inc.");
        vendors.put("CC:F9:E4", "HP Inc.");
        vendors.put("D4:12:43", "Intel Corp.");
        vendors.put("16:FC:65", "Samsung Electronics");
        vendors.put("64:90:C1", "LG Electronics");
        vendors.put("00:1A:2B", "Raspberry Pi Foundation");
        vendors.put("00:11:22", "Cisco Systems");
        vendors.put("00:50:56", "VMware Inc.");
        vendors.put("00:0C:29", "VMware Inc.");
        vendors.put("08:00:27", "Oracle VirtualBox");
        vendors.put("AA:BB:CC", "Generic Device");
        vendors.put("7A:79:19", "Hamachi VPN");
        vendors.put("02:00:00", "Radmin VPN");
        vendors.put("00:14:22", "Apple Inc.");
        vendors.put("00:16:CB", "Apple Inc.");
        vendors.put("00:25:00", "Apple Inc.");
        vendors.put("BC:51:FE", "Xiaomi Communications");
        vendors.put("34:CE:00", "Xiaomi Communications");
        vendors.put("04:E5:58", "Samsung Electronics");
        vendors.put("F8:16:54", "Samsung Electronics");
        vendors.put("B8:27:EB", "Raspberry Pi Foundation");
        vendors.put("DC:A6:32", "Raspberry Pi Foundation");
        vendors.put("E4:5F:01", "TP-Link Technologies");
        vendors.put("50:C7:BF", "TP-Link Technologies");
        vendors.put("70:4D:7B", "Huawei Technologies");
    }

    public static String lookup(String mac) {
        if (mac == null || mac.isEmpty()) return "Неизвестно";

        String normalizedMac = mac.toUpperCase();
        String prefix;

        if (normalizedMac.length() >= 8) {
            prefix = normalizedMac.substring(0, 8);
        } else {
            return "Неизвестно";
        }

        return vendors.getOrDefault(prefix, "Неизвестно");
    }
}