package com.magicdeaks.heatcapacity;

public enum LowTHeatCapacityModel {
    // ==========================================
    // 1. BASE MODELS (No Schottky contributions)
    // ==========================================

    // Pure Lattice (Debye expansions)
    LATTICE_1(3),
    LATTICE_2(3, 5),
    LATTICE_3(3, 5, 7),
    LATTICE_4(3, 5, 7, 9),

    // Electronic (Sommerfeld) + Lattice
    LINEAR_1(1, 3),
    LINEAR_2(1, 3, 5),
    LINEAR_3(1, 3, 5, 7),
    LINEAR_4(1, 3, 5, 7, 9),

    // ==========================================
    // 2. FIRST-ORDER SCHOTTKY (T^-2 only)
    // ==========================================

    // Standard Schottky + Lattice
    SCHOTTKY_1(-2, 3),
    SCHOTTKY_2(-2, 3, 5),
    SCHOTTKY_3(-2, 3, 5, 7),
    SCHOTTKY_4(-2, 3, 5, 7, 9),

    // Standard Schottky + Electronic + Lattice
    SCHOTTKY_LINEAR_1(-2, 1, 3),
    SCHOTTKY_LINEAR_2(-2, 1, 3, 5),
    SCHOTTKY_LINEAR_3(-2, 1, 3, 5, 7),
    SCHOTTKY_LINEAR_4(-2, 1, 3, 5, 7, 9),

    // ==========================================
    // 3. SECOND-ORDER SCHOTTKY (T^-2, T^-3)
    // ==========================================

    // Extended Schottky + Lattice
    SCHOTTKY_EXP2_1(-2, -3, 3),
    SCHOTTKY_EXP2_2(-2, -3, 3, 5),
    SCHOTTKY_EXP2_3(-2, -3, 3, 5, 7),
    SCHOTTKY_EXP2_4(-2, -3, 3, 5, 7, 9),

    // Extended Schottky + Electronic + Lattice
    SCHOTTKY_EXP2_LINEAR_1(-2, -3, 1, 3),
    SCHOTTKY_EXP2_LINEAR_2(-2, -3, 1, 3, 5),
    SCHOTTKY_EXP2_LINEAR_3(-2, -3, 1, 3, 5, 7),
    SCHOTTKY_EXP2_LINEAR_4(-2, -3, 1, 3, 5, 7, 9),

    // ==========================================
    // 4. THIRD-ORDER SCHOTTKY (T^-2, T^-3, T^-4)
    // ==========================================

    // Full Schottky tail + Lattice
    SCHOTTKY_EXP3_1(-2, -3, -4, 3),
    SCHOTTKY_EXP3_2(-2, -3, -4, 3, 5),
    SCHOTTKY_EXP3_3(-2, -3, -4, 3, 5, 7),
    SCHOTTKY_EXP3_4(-2, -3, -4, 3, 5, 7, 9),

    // Full Schottky tail + Electronic + Lattice
    SCHOTTKY_EXP3_LINEAR_1(-2, -3, -4, 1, 3),
    SCHOTTKY_EXP3_LINEAR_2(-2, -3, -4, 1, 3, 5),
    SCHOTTKY_EXP3_LINEAR_3(-2, -3, -4, 1, 3, 5, 7),
    SCHOTTKY_EXP3_LINEAR_4(-2, -3, -4, 1, 3, 5, 7, 9);

    private final double[] powers;

    LowTHeatCapacityModel(double... powers) {
        this.powers = powers;
    }

    public double[] getPowers() { return powers; }
}
