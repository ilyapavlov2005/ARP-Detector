package com.arpdetector;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class DeviceTableModel extends AbstractTableModel {

    private final String[] columns = {"IP-адрес", "MAC-адрес", "Производитель", "Имя", "Статус"};
    private final List<Device> allDevices = new ArrayList<>();
    private final List<Device> displayDevices = new ArrayList<>();

    private String filterText = "";
    private String filterStatus = "Все";

    @Override
    public int getRowCount() {
        return displayDevices.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= displayDevices.size()) return null;
        Device device = displayDevices.get(rowIndex);

        switch (columnIndex) {
            case 0: return device.getGatewayIcon() + device.getIpAddress();
            case 1: return device.getMacAddress();
            case 2: return device.getVendor();
            case 3: return device.getHostname();
            case 4: return device.getStatus();
            default: return null;
        }
    }

    public void setFilter(String text, String status) {
        this.filterText = text == null ? "" : text.toLowerCase().trim();
        this.filterStatus = status == null ? "Все" : status;
        applyFilter();
    }

    private void applyFilter() {
        displayDevices.clear();

        for (Device device : allDevices) {
            boolean matchesText = true;
            boolean matchesStatus = true;

            if (!filterText.isEmpty()) {
                matchesText = device.getIpAddress().toLowerCase().contains(filterText) ||
                        device.getMacAddress().toLowerCase().contains(filterText) ||
                        device.getVendor().toLowerCase().contains(filterText);
            }

            if (!filterStatus.equals("Все")) {
                matchesStatus = device.getStatus().contains(filterStatus);
            }

            if (matchesText && matchesStatus) {
                displayDevices.add(device);
            }
        }

        fireTableDataChanged();
    }

    public void addDevice(Device device) {
        allDevices.add(device);
        applyFilter();
    }

    public void updateDevice(Device device) {
        for (int i = 0; i < allDevices.size(); i++) {
            if (allDevices.get(i).getIpAddress().equals(device.getIpAddress())) {
                allDevices.set(i, device);
                break;
            }
        }
        applyFilter();
    }

    public Device getDevice(String ip) {
        for (Device device : allDevices) {
            if (device.getIpAddress().equals(ip)) {
                return device;
            }
        }
        return null;
    }

    public Device getDeviceAtRow(int row) {
        if (row >= 0 && row < displayDevices.size()) {
            return displayDevices.get(row);
        }
        return null;
    }

    public List<Device> getAllDevices() {
        return new ArrayList<>(allDevices);
    }

    public int getFullSize() {
        return allDevices.size();
    }

    public void clear() {
        allDevices.clear();
        displayDevices.clear();
        fireTableDataChanged();
    }
}