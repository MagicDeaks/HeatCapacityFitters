package com.magicdeaks.heatcapacity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.magicdeaks.heatcapacity.io.FileSystem;
import com.magicdeaks.heatcapacity.models.CompositeSpecificHeatModel;
import com.magicdeaks.heatcapacity.models.CompositeSpecificHeatModel.SpecialFitModel;
import com.magicdeaks.heatcapacity.records.FitResult;
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
}
