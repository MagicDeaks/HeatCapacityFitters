package com.magicdeaks.heatcapacity.models;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class PolynomialTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Fit", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "RMS"};
    private final List<Object[]> data = new ArrayList<>();

    public void addFit(String degree, double[] coeffs) {
        double[] clone = coeffs.clone();

        data.add(new Object[]{degree, clone});

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

        if (col == 0) {
            rowData[0] = value;
            return;
        }

        if (col > 0) {
            double[] number = (double[]) rowData[1];

            if (col - 1 >= number.length) {
                throw new IndexOutOfBoundsException("Column index out of bounds: " + col);
            }
            number[col - 1] = (double) value;
            rowData[1] = number;
        }
        fireTableCellUpdated(row, col);
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (col > 0) {
            int index = col - 1;
            double[] val = (double[]) data.get(row)[1];

            return val[index];
        }

        return data.get(row)[0];
    }

    public double[] getColumnDataAsDouble(int col) {
        double[] colData = new double[getRowCount()];
        for (int i = 0; i < getRowCount(); i++) {
            colData[i] = (Double) getValueAt(i, col);
        }
        return colData;
    }


}
