package com.magicdeaks.heatcapacity.models;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ParameterTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Parameter", "Initial Guess", "Lower Bound", "Upper Bound", "Fixed"};
    private final List<Object[]> data = new ArrayList<>();

    public void addParameter(String name, double initial, double lower, double upper, boolean isFixed) {
        data.add(new Object[]{name, initial, lower, upper, isFixed});
        // Notify the table that a new row has been added so it can repaint
        fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    public void clearAll() {
        int size = data.size();
        if (size > 0) {
            data.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    // --- CRITICAL FIX 1: Strict Column Class Mapping ---
    @Override
    public Class<?> getColumnClass(int c) {
        return switch (c) {
            case 0 -> String.class;   // Parameter Name -> StringRenderer
            case 4 -> Boolean.class;  // Fixed -> CheckboxRenderer
            default -> Double.class;  // Init, Lower, Upper -> DoubleRenderer
        };
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col != 0; // Everything is editable except the Parameter Name
    }

    // --- CRITICAL FIX 2: Strict Setter Validation ---
    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col >= 1 && col <= 3) {
            try {
                if (value instanceof String) {
                    data.get(row)[col] = Double.parseDouble((String) value);
                } else if (value instanceof Number) {
                    data.get(row)[col] = ((Number) value).doubleValue();
                }
            } catch (NumberFormatException e) {
                // Ignore invalid user text inputs (e.g., letters in a number field)
                System.err.println("Ignored invalid numeric input.");
                return;
            }
        } else {
            data.get(row)[col] = value;
        }
        fireTableCellUpdated(row, col);
    }

    // --- CRITICAL FIX 3: Strict Getter Output ---
    @Override
    public Object getValueAt(int row, int col) {
        Object val = data.get(row)[col];

        if (col >= 1 && col <= 3) {
            if (val instanceof Number) {
                return ((Number) val).doubleValue();
            } else if (val instanceof String) {
                try {
                    return Double.parseDouble((String) val);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
            return 0.0;
        }

        return val;
    }

    // --- Array Extraction Helpers ---
    public double[] getColumnDataAsDouble(int col) {
        double[] colData = new double[getRowCount()];
        for (int i = 0; i < getRowCount(); i++) {
            colData[i] = (Double) getValueAt(i, col);
        }
        return colData;
    }

    public boolean[] getColumnDataAsBoolean(int col) {
        boolean[] colData = new boolean[getRowCount()];
        for (int i = 0; i < getRowCount(); i++) {
            colData[i] = (Boolean) getValueAt(i, col);
        }
        return colData;
    }
}
