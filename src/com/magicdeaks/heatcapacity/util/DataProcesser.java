package com.magicdeaks.heatcapacity.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public abstract class DataProcesser {
    private static final Map<String, Double> ATOMIC_WEIGHTS = new HashMap<>();

    static {
        // Period 1
        ATOMIC_WEIGHTS.put("H", 1.008);
        ATOMIC_WEIGHTS.put("He", 4.0026);

        // Period 2
        ATOMIC_WEIGHTS.put("Li", 6.94);
        ATOMIC_WEIGHTS.put("Be", 9.0122);
        ATOMIC_WEIGHTS.put("B", 10.81);
        ATOMIC_WEIGHTS.put("C", 12.011);
        ATOMIC_WEIGHTS.put("N", 14.007);
        ATOMIC_WEIGHTS.put("O", 15.999);
        ATOMIC_WEIGHTS.put("F", 18.998);
        ATOMIC_WEIGHTS.put("Ne", 20.180);

        // Period 3
        ATOMIC_WEIGHTS.put("Na", 22.990);
        ATOMIC_WEIGHTS.put("Mg", 24.305);
        ATOMIC_WEIGHTS.put("Al", 26.982);
        ATOMIC_WEIGHTS.put("Si", 28.085);
        ATOMIC_WEIGHTS.put("P", 30.974);
        ATOMIC_WEIGHTS.put("S", 32.06);
        ATOMIC_WEIGHTS.put("Cl", 35.45);
        ATOMIC_WEIGHTS.put("Ar", 39.95);

        // Period 4
        ATOMIC_WEIGHTS.put("K", 39.098);
        ATOMIC_WEIGHTS.put("Ca", 40.078);
        ATOMIC_WEIGHTS.put("Sc", 44.956);
        ATOMIC_WEIGHTS.put("Ti", 47.867);
        ATOMIC_WEIGHTS.put("V", 50.942);
        ATOMIC_WEIGHTS.put("Cr", 51.996);
        ATOMIC_WEIGHTS.put("Mn", 54.938);
        ATOMIC_WEIGHTS.put("Fe", 55.845);
        ATOMIC_WEIGHTS.put("Co", 58.933);
        ATOMIC_WEIGHTS.put("Ni", 58.693);
        ATOMIC_WEIGHTS.put("Cu", 63.546);
        ATOMIC_WEIGHTS.put("Zn", 65.38);
        ATOMIC_WEIGHTS.put("Ga", 69.723);
        ATOMIC_WEIGHTS.put("Ge", 72.630);
        ATOMIC_WEIGHTS.put("As", 74.922);
        ATOMIC_WEIGHTS.put("Se", 78.971);
        ATOMIC_WEIGHTS.put("Br", 79.904);
        ATOMIC_WEIGHTS.put("Kr", 83.798);

        // Period 5
        ATOMIC_WEIGHTS.put("Rb", 85.468);
        ATOMIC_WEIGHTS.put("Sr", 87.62);
        ATOMIC_WEIGHTS.put("Y", 88.906);
        ATOMIC_WEIGHTS.put("Zr", 91.224);
        ATOMIC_WEIGHTS.put("Nb", 92.906);
        ATOMIC_WEIGHTS.put("Mo", 95.95);
        ATOMIC_WEIGHTS.put("Tc", 98.0); // Radioactive, most stable isotope
        ATOMIC_WEIGHTS.put("Ru", 101.07);
        ATOMIC_WEIGHTS.put("Rh", 102.91);
        ATOMIC_WEIGHTS.put("Pd", 106.42);
        ATOMIC_WEIGHTS.put("Ag", 107.87);
        ATOMIC_WEIGHTS.put("Cd", 112.41);
        ATOMIC_WEIGHTS.put("In", 114.82);
        ATOMIC_WEIGHTS.put("Sn", 118.71);
        ATOMIC_WEIGHTS.put("Sb", 121.76);
        ATOMIC_WEIGHTS.put("Te", 127.60);
        ATOMIC_WEIGHTS.put("I", 126.90);
        ATOMIC_WEIGHTS.put("Xe", 131.29);

        // Period 6
        ATOMIC_WEIGHTS.put("Cs", 132.91);
        ATOMIC_WEIGHTS.put("Ba", 137.33);
        ATOMIC_WEIGHTS.put("La", 138.91);
        ATOMIC_WEIGHTS.put("Ce", 140.12);
        ATOMIC_WEIGHTS.put("Pr", 140.91);
        ATOMIC_WEIGHTS.put("Nd", 144.24);
        ATOMIC_WEIGHTS.put("Pm", 145.0); // Radioactive
        ATOMIC_WEIGHTS.put("Sm", 150.36);
        ATOMIC_WEIGHTS.put("Eu", 151.96);
        ATOMIC_WEIGHTS.put("Gd", 157.25);
        ATOMIC_WEIGHTS.put("Tb", 158.93);
        ATOMIC_WEIGHTS.put("Dy", 162.50);
        ATOMIC_WEIGHTS.put("Ho", 164.93);
        ATOMIC_WEIGHTS.put("Er", 167.26);
        ATOMIC_WEIGHTS.put("Tm", 168.93);
        ATOMIC_WEIGHTS.put("Yb", 173.05);
        ATOMIC_WEIGHTS.put("Lu", 174.97);
        ATOMIC_WEIGHTS.put("Hf", 178.49);
        ATOMIC_WEIGHTS.put("Ta", 180.95);
        ATOMIC_WEIGHTS.put("W", 183.84);
        ATOMIC_WEIGHTS.put("Re", 186.21);
        ATOMIC_WEIGHTS.put("Os", 190.23);
        ATOMIC_WEIGHTS.put("Ir", 192.22);
        ATOMIC_WEIGHTS.put("Pt", 195.08);
        ATOMIC_WEIGHTS.put("Au", 196.97);
        ATOMIC_WEIGHTS.put("Hg", 200.59);
        ATOMIC_WEIGHTS.put("Tl", 204.38);
        ATOMIC_WEIGHTS.put("Pb", 207.2);
        ATOMIC_WEIGHTS.put("Bi", 208.98);
        ATOMIC_WEIGHTS.put("Po", 209.0); // Radioactive
        ATOMIC_WEIGHTS.put("At", 210.0); // Radioactive
        ATOMIC_WEIGHTS.put("Rn", 222.0); // Radioactive

        // Period 7 (Actinides and Transactinides - All Radioactive)
        ATOMIC_WEIGHTS.put("Fr", 223.0);
        ATOMIC_WEIGHTS.put("Ra", 226.0);
        ATOMIC_WEIGHTS.put("Ac", 227.0);
        ATOMIC_WEIGHTS.put("Th", 232.04);
        ATOMIC_WEIGHTS.put("Pa", 231.04);
        ATOMIC_WEIGHTS.put("U", 238.03);
        ATOMIC_WEIGHTS.put("Np", 237.0);
        ATOMIC_WEIGHTS.put("Pu", 244.0);
        ATOMIC_WEIGHTS.put("Am", 243.0);
        ATOMIC_WEIGHTS.put("Cm", 247.0);
        ATOMIC_WEIGHTS.put("Bk", 247.0);
        ATOMIC_WEIGHTS.put("Cf", 251.0);
        ATOMIC_WEIGHTS.put("Es", 252.0);
        ATOMIC_WEIGHTS.put("Fm", 257.0);
        ATOMIC_WEIGHTS.put("Md", 258.0);
        ATOMIC_WEIGHTS.put("No", 259.0);
        ATOMIC_WEIGHTS.put("Lr", 266.0);
        ATOMIC_WEIGHTS.put("Rf", 267.0);
        ATOMIC_WEIGHTS.put("Db", 268.0);
        ATOMIC_WEIGHTS.put("Sg", 269.0);
        ATOMIC_WEIGHTS.put("Bh", 270.0);
        ATOMIC_WEIGHTS.put("Hs", 270.0);
        ATOMIC_WEIGHTS.put("Mt", 278.0);
        ATOMIC_WEIGHTS.put("Ds", 281.0);
        ATOMIC_WEIGHTS.put("Rg", 282.0);
        ATOMIC_WEIGHTS.put("Cn", 285.0);
        ATOMIC_WEIGHTS.put("Nh", 286.0);
        ATOMIC_WEIGHTS.put("Fl", 289.0);
        ATOMIC_WEIGHTS.put("Mc", 290.0);
        ATOMIC_WEIGHTS.put("Lv", 293.0);
        ATOMIC_WEIGHTS.put("Ts", 294.0);
        ATOMIC_WEIGHTS.put("Og", 294.0);
    }

    /**
     * Calculates the molecular weight of a given chemical formula.
     *
     * @param formula The chemical formula as a String (e.g., "H2O", "Ca(OH)2")
     * @return The total molecular weight in g/mol
     */
    public static double getMolecularWeight(String formula) {
        Stack<Double> stack = new Stack<>();
        // Use a single-element array to pass the index by reference
        int[] pos = {0};
        int n = formula.length();

        while (pos[0] < n) {
            char c = formula.charAt(pos[0]);

            if (c == '(') {
                stack.push(-1.0);
                pos[0]++;
            } else if (c == ')') {
                double groupWeight = 0;
                while (!stack.isEmpty() && stack.peek() != -1.0) {
                    groupWeight += stack.pop();
                }
                stack.pop(); // Remove the '(' marker
                pos[0]++;

                // Call the extracted helper method
                int count = parseMultiplier(formula, pos);
                stack.push(groupWeight * count);

            } else if (Character.isUpperCase(c)) {
                StringBuilder element = new StringBuilder();
                element.append(c);
                pos[0]++;

                while (pos[0] < n && Character.isLowerCase(formula.charAt(pos[0]))) {
                    element.append(formula.charAt(pos[0]));
                    pos[0]++;
                }

                String elStr = element.toString();
                if (!ATOMIC_WEIGHTS.containsKey(elStr)) {
                    throw new IllegalArgumentException("Unknown element: " + elStr);
                }

                // Call the extracted helper method
                int count = parseMultiplier(formula, pos);
                stack.push(ATOMIC_WEIGHTS.get(elStr) * count);

            } else {
                throw new IllegalArgumentException("Invalid character encountered: " + c);
            }
        }

        double totalWeight = 0;
        while (!stack.isEmpty()) {
            totalWeight += stack.pop();
        }

        return totalWeight;
    }

    /**
     * Helper method to parse numeric multipliers and update the string index.
     */
    private static int parseMultiplier(String formula, int[] pos) {
        int count = 0;
        int n = formula.length();
        while (pos[0] < n && Character.isDigit(formula.charAt(pos[0]))) {
            count = count * 10 + (formula.charAt(pos[0]) - '0');
            pos[0]++;
        }
        return count == 0 ? 1 : count; // Default to 1 if no numbers were parsed
    }

    public static double[] scaleHeatCapacity(double[] heatCapacities, double molecularWeight) {
        double[] scaledHeatCapacities = new double[heatCapacities.length];

        for (int i = 0; i < heatCapacities.length; i++) {
            scaledHeatCapacities[i] = heatCapacities[i] / molecularWeight;
        }

        return scaledHeatCapacities;
    }

    public static double[][] subtractCopper(double[][] data, double massCopper) {
        double[][] result = new double[2][data[0].length];

        for (int i = 0; i < data[0].length; i++) {
            result[1][i] = data[1][i] - copperHeatCapacity(data[0][i], massCopper);
        }

        return result;
    }

    private static double copperHeatCapacity(double temperature, double massCopper) {
        double molesCopper = massCopper / ATOMIC_WEIGHTS.get("Cu");
        double heatCapacity;

        if (temperature < 40.5843) {
            heatCapacity = 0.000692858819282776 * temperature
                    + 0.0000475948636951698 * Math.pow(temperature, 3)
                    - 1.04180866056848E-09 * Math.pow(temperature, 5)
                    + 1.26371655110613E-10 * Math.pow(temperature, 7)
                    - 2.53014563096065E-13 * Math.pow(temperature, 9)
                    + 2.25138590822923E-16 * Math.pow(temperature, 11)
                    - 1.07090804759429E-19 * Math.pow(temperature, 13)
                    + 2.6444890044424E-23 * Math.pow(temperature, 15)
                    - 2.66258434671265E-27 * Math.pow(temperature, 17);
        } else {
            heatCapacity = 7.49698353726173
                    - 0.815860283884158 * temperature
                    + 0.0326991063439956 * temperature * temperature
                    - 0.000519689428865549 * Math.pow(temperature, 3)
                    + 4.81457234263551E-06 * Math.pow(temperature, 4)
                    - 0.0000000286851860256 * Math.pow(temperature, 5)
                    + 1.13310041876692E-10 * Math.pow(temperature, 6)
                    - 2.95384552740994E-13 * Math.pow(temperature, 7)
                    + 4.88678166369441E-16 * Math.pow(temperature, 8)
                    - 4.64923935272884E-19 * Math.pow(temperature, 9)
                    + 1.93717962359027E-22 * Math.pow(temperature, 10);
        }

        heatCapacity *= molesCopper;

        return heatCapacity;
    }
}
