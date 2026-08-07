package com.magicdeaks.heatcapacity.io;

import com.magicdeaks.heatcapacity.session.AnalysisSession;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class FileSystem {
    public static void writeFile(AnalysisSession session) {
        try (FileOutputStream fos = new FileOutputStream("save.wtf");
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(session);
            System.out.println("SESSION written to save.wtf");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readFile(String path) {
        try (FileInputStream fis = new FileInputStream(path);
                ObjectInputStream ois = new ObjectInputStream(fis)) {
            AnalysisSession session = (AnalysisSession) ois.readObject();
            System.out.println("Loaded SESSION: " + session.getMolecularWeight());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
