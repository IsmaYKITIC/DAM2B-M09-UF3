import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Fitxer {
    private String nom;
    private byte[] contingut;

    public Fitxer(String nom) {
        this.nom = nom;
    }

    public byte[] getContingut() {
        File file = new File(nom);
        if (!file.exists() || !file.canRead()) {
            System.out.println("El fitxer no existeix o no es pot llegir: " + nom);
            return null;
        }

        try {
            contingut = Files.readAllBytes(Paths.get(nom));
            return contingut;
        } catch (IOException e) {
            System.out.println("Error en llegir el fitxer: " + e.getMessage());
            return null;
        }
    }
}