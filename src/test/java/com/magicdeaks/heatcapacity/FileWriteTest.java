package com.magicdeaks.heatcapacity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.magicdeaks.heatcapacity.io.FileSystem;
import com.magicdeaks.heatcapacity.models.CompositeSpecificHeatModel;
import com.magicdeaks.heatcapacity.models.CompositeSpecificHeatModel.SpecialFitModel;
import com.magicdeaks.heatcapacity.models.HighTSpecificHeatModel;
import com.magicdeaks.heatcapacity.models.HighTSpecificHeatModel.HighTFitModel;
import com.magicdeaks.heatcapacity.records.FitResult;
import com.magicdeaks.heatcapacity.records.HeatCapacityData;
import com.magicdeaks.heatcapacity.records.ThermFunctions;
import com.magicdeaks.heatcapacity.session.AnalysisSession;
import com.magicdeaks.heatcapacity.util.Deviations;

import org.junit.jupiter.api.Test;

public class FileWriteTest {
    @Test
    public void readAndWriteSessionAtoms() {
        AnalysisSession initSession = new AnalysisSession();
        double atoms = 14.082;
        String path = "tmp/atomsTest.wtf";

        initSession.setAtoms(atoms);

        FileSystem.writeFile(initSession, path);
        AnalysisSession readSession = FileSystem.readFile(path).orElseThrow();

        assertEquals(initSession.getAtoms(), readSession.getAtoms());
    }

    @Test
    public void readAndWriteSessionMW() {
        AnalysisSession initSession = new AnalysisSession();
        double mw = 249.289;
        String path = "tmp/mwTest.wtf";

        initSession.setMolecularWeight(mw);

        FileSystem.writeFile(initSession, path);
        AnalysisSession readSession = FileSystem.readFile(path).orElseThrow();

        assertEquals(initSession.getMolecularWeight(), readSession.getMolecularWeight());
    }

    @Test
    public void readAndWriteSessionLowTFit() {
        AnalysisSession initSession = new AnalysisSession();
        Deviations deviations = new Deviations(new double[][] {{1.2, 3.4, 7.8}, {4.0, 8.3, 1.9}});
        String path = "tmp/lowFitTest.wtf";

        initSession.setLowTFit(new FitResult(new double[] {0, 2, 7}, 2.108, 17, deviations));

        FileSystem.writeFile(initSession, path);
        AnalysisSession readSession = FileSystem.readFile(path).orElseThrow();

        assertEquals(initSession.getLowTFit(), readSession.getLowTFit());
    }

    @Test
    public void readAndWriteSessionLowTModel() {
        AnalysisSession initSession = new AnalysisSession();
        SpecialFitModel[] terms =
                new SpecialFitModel[] {
                    SpecialFitModel.GAP,
                    SpecialFitModel.SCHOTTKY,
                    SpecialFitModel.LATTICE_3,
                    SpecialFitModel.LATTICE_11
                };
        CompositeSpecificHeatModel model = new CompositeSpecificHeatModel(terms);
        String path = "tmp/lowModelTest.wtf";

        initSession.setLowTModel(model);

        FileSystem.writeFile(initSession, path);
        AnalysisSession readSession = FileSystem.readFile(path).orElseThrow();

        assert initSession.getLowTModel().getTerms().equals(readSession.getLowTModel().getTerms());
    }

    @Test
    public void readAndWriteSessionMidTFit() {
        AnalysisSession initSession = new AnalysisSession();
        Deviations deviations = new Deviations(new double[][] {{1.2, 3.4, 7.8}, {4.0, 8.3, 1.9}});
        String path = "tmp/midFitTest.wtf";

        FitResult[] fits =
                new FitResult[] {
                    new FitResult(new double[] {0, 2, 7}, 2.108, 17, deviations),
                    new FitResult(new double[] {1, 1, 6}, 0.019, 10),
                    new FitResult(new double[] {3, 8, 0}, 3.127, deviations),
                    new FitResult(new double[] {1, 0, 1}, 13.08),
                };

        initSession.setMidTFit(fits);

        FileSystem.writeFile(initSession, path);
        AnalysisSession readSession = FileSystem.readFile(path).orElseThrow();

        assertEquals(initSession.getMidTFit().length, readSession.getMidTFit().length);

        for (int i = 0; i < initSession.getMidTFit().length; i++) {
            assertEquals(initSession.getMidTFit()[i], readSession.getMidTFit()[i]);
        }
    }

    @Test
    public void readAndWriteSessionMidTModel() {
        AnalysisSession initSession = new AnalysisSession();
        int[][] models = new int[11][];
        String path = "tmp/midModelTest.wtf";

        for (int i = 0; i < models.length; i++) {
            models[i] = new int[i + 1];
            for (int j = 0; j < i + 1; j++) {
                models[i][j] = j;
            }
        }

        initSession.setMidTModel(models);

        FileSystem.writeFile(initSession, path);
        AnalysisSession readSession = FileSystem.readFile(path).orElseThrow();

        assertArrayEquals(initSession.getMidTModel(), readSession.getMidTModel());
    }

    @Test
    public void readAndWriteSessionHighTFit() {
        AnalysisSession initSession = new AnalysisSession();
        Deviations deviations = new Deviations(new double[][] {{1.2, 3.4, 7.8}, {4.0, 8.3, 1.9}});
        String path = "tmp/highFitTest.wtf";

        initSession.setHighTFit(new FitResult(new double[] {0, 2, 7}, 2.108, 17, deviations));

        FileSystem.writeFile(initSession, path);
        AnalysisSession readSession = FileSystem.readFile(path).orElseThrow();

        assertEquals(initSession.getHighTFit(), readSession.getHighTFit());
    }

    @Test
    public void readAndWriteSessionHighTModel() {
        AnalysisSession initSession = new AnalysisSession();
        HighTFitModel[] terms =
                new HighTFitModel[] {
                    HighTFitModel.DEBYE,
                    HighTFitModel.EINSTEIN,
                    HighTFitModel.EINSTEIN,
                    HighTFitModel.SQUARE,
                    HighTFitModel.LINEAR
                };
        HighTSpecificHeatModel model = new HighTSpecificHeatModel(terms);
        String path = "tmp/highModelTest.wtf";

        initSession.setHighTModel(model);

        FileSystem.writeFile(initSession, path);
        AnalysisSession readSession = FileSystem.readFile(path).orElseThrow();

        assert initSession
                .getHighTModel()
                .getTerms()
                .equals(readSession.getHighTModel().getTerms());
    }

    @Test
    public void readAndWriteSessionMidTSelect() {
        AnalysisSession initSession = new AnalysisSession();
        String path = "tmp/midTSelectTest.wtf";

        initSession.setMidTSelect(7);

        FileSystem.writeFile(initSession, path);
        AnalysisSession readSession = FileSystem.readFile(path).orElseThrow();

        assertEquals(initSession.getMidTSelect(), readSession.getMidTSelect());
    }

    @Test
    public void readAndWriteSessionThermFunc() {
        AnalysisSession initSession = new AnalysisSession();
        String path = "tmp/thermFuncTest.wtf";
        int size = 100;
        double[] temp = new double[size];
        double[] hc = new double[size];
        double[] enth = new double[size];
        double[] entr = new double[size];
        double[] gibbs = new double[size];

        for (int i = 0; i < size; i++) {
            temp[i] = Math.random();
            hc[i] = Math.random();
            enth[i] = Math.random();
            entr[i] = Math.random();
            gibbs[i] = Math.random();
        }

        ThermFunctions func = new ThermFunctions(temp, hc, enth, entr, gibbs);
        initSession.setThermFunctions(func);

        FileSystem.writeFile(initSession, path);
        AnalysisSession readSession = FileSystem.readFile(path).orElseThrow();

        assertArrayEquals(
                initSession.getThermFunctions().temperatures(),
                readSession.getThermFunctions().temperatures());
        assertArrayEquals(
                initSession.getThermFunctions().heatCapacities(),
                readSession.getThermFunctions().heatCapacities());
        assertArrayEquals(
                initSession.getThermFunctions().enthalpies(),
                readSession.getThermFunctions().enthalpies());
        assertArrayEquals(
                initSession.getThermFunctions().entropies(),
                readSession.getThermFunctions().entropies());
        assertArrayEquals(
                initSession.getThermFunctions().gibbs(), readSession.getThermFunctions().gibbs());
    }

    @Test
    public void readAndWriteSessionRawData() {
        AnalysisSession initSession = new AnalysisSession();
        String path = "tmp/rawDataTest.wtf";
        int size = 100;
        double[] temps = new double[size];
        double[] hc = new double[size];

        for (int i = 0; i < size; i++) {
            temps[i] = Math.random();
            hc[i] = Math.random();
        }

        HeatCapacityData data = new HeatCapacityData(temps, hc);
        initSession.setRawData(data);

        FileSystem.writeFile(initSession, path);
        AnalysisSession readSession = FileSystem.readFile(path).orElseThrow();

        assertArrayEquals(
                initSession.getRawData().temperatures(), readSession.getRawData().temperatures());
        assertArrayEquals(
                initSession.getRawData().heatCapacities(),
                readSession.getRawData().heatCapacities());
    }

    @Test
    public void readAndWriteSessionMolecularFormula() {
        AnalysisSession initSession = new AnalysisSession();
        String path = "tmp/formulaTest.wtf";
        String formula = "SmOHCO3";

        initSession.setMolecularFormula(formula);

        FileSystem.writeFile(initSession, path);
        AnalysisSession readSession = FileSystem.readFile(path).orElseThrow();

        assertEquals(initSession.getMolecularFormula(), readSession.getMolecularFormula());
    }

    @Test
    public void readAndWriteSessionPath() {
        AnalysisSession initSession = new AnalysisSession();
        String path = "tmp/pathTest.wtf";
        String datPath = "random file.dat";

        initSession.setPath(datPath);

        FileSystem.writeFile(initSession, path);
        AnalysisSession readSession = FileSystem.readFile(path).orElseThrow();

        assertEquals(initSession.getPath(), readSession.getPath());
    }

    @Test
    public void readAndWriteSessionOverlaps() {
        AnalysisSession initSession = new AnalysisSession();
        String path = "tmp/overlapsTest.wtf";
        double[] overlaps = {12.47, 52.68};

        initSession.setOverlaps(overlaps);

        FileSystem.writeFile(initSession, path);
        AnalysisSession readSession = FileSystem.readFile(path).orElseThrow();

        assertArrayEquals(initSession.getOverlaps(), readSession.getOverlaps());
    }
}
