package com.magicdeaks.heatcapacity;

public enum MidTHeatCapacityModel implements  HeatCapacityModel {
    ORTHO_0(0),
    ORTHO_1(0, 1),
    ORTHO_2(0, 1, 2),
    ORTHO_3(0, 1, 2, 3),
    ORTHO_4(0, 1, 2, 3, 4),
    ORTHO_5(0, 1, 2, 3, 4, 5),
    ORTHO_6(0, 1, 2, 3, 4, 5, 6),
    ORTHO_7(0, 1, 2, 3, 4, 5, 6, 7),
    ORTHO_8(0, 1, 2, 3, 4, 5, 6, 7, 8),
    ORTHO_9(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
    ORTHO_10(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    private final double[] powers;

    MidTHeatCapacityModel(double... powers) {
        this.powers = powers;
    }

    public double[] getPowers() { return powers; }
}
