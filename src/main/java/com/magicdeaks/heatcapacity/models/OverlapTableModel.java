package com.magicdeaks.heatcapacity.models;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class OverlapTableModel extends AbstractTableModel {
    private final String[] columnNames = {"T (K)", "C", "dC", "ddC"};
    private final List<Object[]> data = new ArrayList<>();

    public void addOverlap(double[] overlap) {
        double[] clone = overlap.clone();

        data.add(new Object[]{clone});

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

    @Override
    public Class<?> getColumnClass(int c) {
        return (c == 0) ? String.class : Double.class;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (row < 0 || row >= data.size()) {
            throw new IndexOutOfBoundsException("Row index out of bounds: " + row);
        }

        Object[] rowData = data.get(row);

        double[] number = (double[]) rowData[0];

        if (col - 1 >= number.length) {
            throw new IndexOutOfBoundsException("Column index out of bounds: " + col);
        }

        number[col - 1] = (double) value;
        rowData[0] = number;

        fireTableCellUpdated(row, col);
    }

    @Override
    public Object getValueAt(int row, int col) {
        try {
            double[] val = (double[]) data.get(row)[0];
            return val[col];
        } catch (Exception e) {
            System.out.println(e);
        }
        return 0;
    }

    public double[] getColumnDataAsDouble(int col) {
        double[] colData = new double[getRowCount()];
        for (int i = 0; i < getRowCount(); i++) {
            colData[i] = (Double) getValueAt(i, col);
        }
        return colData;
    }


}
