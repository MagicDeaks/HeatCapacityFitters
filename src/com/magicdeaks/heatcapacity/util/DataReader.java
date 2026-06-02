package com.magicdeaks.heatcapacity.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public abstract class DataReader {
    private static final Logger LOGGER = Logger.getLogger(DataReader.class.getName());

    public static double[][] readCSV(String fileName) {
        String line;
        String delimiter = ",";
        Path filePath = Paths.get(fileName);
        double[][] data = new double[2][];

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            int lineCount;

            try (Stream<String> lines = Files.lines(filePath)) {
                lineCount = (int) lines.count();
            }

            data[0] = new double[lineCount];
            data[1] = new double[lineCount];

            int i = 0;

            while ((line = br.readLine()) != null) {
                String[] row = line.split(delimiter);
                if (row.length > 2) { throw new IllegalArgumentException("Invalid data format"); }

                data[0][i] = Double.parseDouble(row[0]);
                data[1][i] = Double.parseDouble(row[1]);

                i++;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading file " + fileName, e);
        }

        return data;
    }
}
