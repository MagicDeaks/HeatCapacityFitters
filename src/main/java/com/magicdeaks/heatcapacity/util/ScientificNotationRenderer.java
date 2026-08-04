package com.magicdeaks.heatcapacity.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;

import javax.swing.table.DefaultTableCellRenderer;

public class ScientificNotationRenderer extends DefaultTableCellRenderer {
    private final DecimalFormat sciFormatter;
    private final MathContext mc;

    public ScientificNotationRenderer() {
        this.sciFormatter = new DecimalFormat("0.0000E0");
        this.mc = new MathContext(5);
    }

    @Override
    protected void setValue(Object value) {
        if (value instanceof Number) {
            if ((double) value >= 0.01 || (double) value <= -0.01) {
                BigDecimal bd = new BigDecimal(Double.toString((double) value));

                setText(new BigDecimal(bd.toString(), mc).toPlainString());
            } else {
                setText(sciFormatter.format(value));
            }
        } else {
            super.setValue(value);
        }
    }
}
