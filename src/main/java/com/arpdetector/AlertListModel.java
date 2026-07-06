package com.arpdetector;

import javax.swing.AbstractListModel;
import java.util.ArrayList;
import java.util.List;

public class AlertListModel extends AbstractListModel<Alert> {

    private final List<Alert> alerts = new ArrayList<>();
    private final int maxSize = 100;

    @Override
    public int getSize() {
        return alerts.size();
    }

    @Override
    public Alert getElementAt(int index) {
        return alerts.get(index);
    }

    public void addAlert(Alert alert) {
        alerts.add(0, alert);

        while (alerts.size() > maxSize) {
            alerts.remove(alerts.size() - 1);
        }

        fireContentsChanged(this, 0, alerts.size() - 1);
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public void clear() {
        alerts.clear();
        fireContentsChanged(this, 0, 0);
    }
}