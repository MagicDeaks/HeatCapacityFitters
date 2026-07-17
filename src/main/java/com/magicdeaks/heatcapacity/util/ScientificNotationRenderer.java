package com.magicdeaks.heatcapacity.util;

import javax.swing.table.DefaultTableCellRenderer;
import java.text.DecimalFormat;

public class ScientificNotationRenderer extends DefaultTableCellRenderer {
    private final DecimalFormat formatter;

    public ScientificNotationRenderer() {
        this.formatter = new DecimalFormat("0.0000E0");
    }

    @Override
    protected void setValue(Object value) {
        if (value instanceof Number) {
            setText(formatter.format(value));
        } else {
            super.setValue(value);
        }
    }




}
