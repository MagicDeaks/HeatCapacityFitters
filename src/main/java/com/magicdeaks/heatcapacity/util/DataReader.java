package com.magicdeaks.heatcapacity.util;

import com.magicdeaks.heatcapacity.records.HeatCapacityData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public abstract class DataReader {
    private static final Logger LOGGER = Logger.getLogger(DataReader.class.getName());

    public static double[][] readCSV(String fileName, int[] columns, int startRow) {
        String line;
        String delimiter = ",";
        Path filePath = Paths.get(fileName);
        double[][] data = new double[2][];

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            int lineCount;

            try (Stream<String> lines = Files.lines(filePath, StandardCharsets.ISO_8859_1)) {
                lineCount = (int) lines.count();
            }

            data[0] = new double[lineCount - startRow];
            data[1] = new double[lineCount - startRow];

            int i = 0;

            while ((line = br.readLine()) != null) {
                String[] row = line.split(delimiter);

                if (i >= startRow) {
                    data[0][i - startRow] = Double.parseDouble(row[columns[0]]);
                    data[1][i - startRow] = Double.parseDouble(row[columns[1]]);
                }

                i++;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading file " + fileName, e);
        }

        return data;
    }

    public static HeatCapacityData readDAT(String fileName, boolean getRaw) {
        double[][] rawData = (getRaw) ? readCSV(fileName, new int[]{7, 13}, 15) : readCSV(fileName, new int[]{7, 9}, 15);

        if (rawData[0].length != rawData[1].length || rawData[0].length % 3 != 0) {
            throw new IllegalArgumentException("Invalid data format");
        }

        double[][] data = new double[2][rawData[0].length / 3];

        int scalingFactor = (getRaw) ? 1_000_000 : 1_000;

        for (int i = 0; i < rawData[0].length / 3; i++) {
            data[0][i] = (rawData[0][3 * i] + rawData[0][3 * i + 1] + rawData[0][3 * i + 2]) / 3;
            data[1][i] = (rawData[1][3 * i] + rawData[1][3 * i + 1] + rawData[1][3 * i + 2]) / 3 / scalingFactor;
        }

        return new HeatCapacityData(data[0], data[1]);
    }

    public static HeatCapacityData readDAT(String filename) {
        return readDAT(filename, false);
    }
}
