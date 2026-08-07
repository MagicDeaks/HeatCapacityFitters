package com.magicdeaks.heatcapacity.io;

import com.magicdeaks.heatcapacity.session.AnalysisSession;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;

public abstract class FileSystem {
    public static void writeFile(AnalysisSession session, String path) {
        try (FileOutputStream fos = new FileOutputStream(path);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(session);
            System.out.println("SESSION written to " + path);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Optional<AnalysisSession> readFile(String path) {
        try (FileInputStream fis = new FileInputStream(path);
                ObjectInputStream ois = new ObjectInputStream(fis)) {
            AnalysisSession session = (AnalysisSession) ois.readObject();
            System.out.println("Loaded SESSION: " + path);
            return Optional.of(session);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
}
