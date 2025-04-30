import java.io.*;

public class FilServidorXat extends Thread {
    private ObjectInputStream in;
    private String nomClient;

    public FilServidorXat(ObjectInputStream in, String nomClient) {
        this.in = in;
        this.nomClient = nomClient;
    }

    public void run() {
        try {
            String missatge;
            do {
                missatge = (String) in.readObject();
                System.out.println("Rebut: " + missatge);
            } while (!missatge.equalsIgnoreCase(ServidorXat.MSG_SORTIR));
            System.out.println("Fil de xat finalitzat.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
