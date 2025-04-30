import java.io.*;
import java.net.*;

public class ServidorXat {
    public static final int PORT = 9999;
    public static final String HOST = "localhost";
    public static final String MSG_SORTIR = "sortir";

    private ServerSocket serverSocket;

    public void iniciarServidor() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciat a " + HOST + ":" + PORT);
    }

    public void pararServidor() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
            System.out.println("Servidor aturat.");
        }
    }

    public String getNom(ObjectInputStream in) throws IOException, ClassNotFoundException {
        System.out.print("Rebut: Escriu el teu nom: ");
        String nom = (String) in.readObject();
        System.out.println("Nom rebut: " + nom);
        return nom;
    }

    public static void main(String[] args) {
        ServidorXat servidor = new ServidorXat();
        try {
            servidor.iniciarServidor();
            Socket clientSocket = servidor.serverSocket.accept();
            System.out.println("Client connectat: " + clientSocket.getInetAddress());

            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

            String nomClient = servidor.getNom(in);

            FilServidorXat fil = new FilServidorXat(in, nomClient);
            fil.start();
            System.out.println("Fil de xat creat.");
            System.out.println("Fil de " + nomClient + " iniciat");

            BufferedReader consola = new BufferedReader(new InputStreamReader(System.in));
            String missatge;
            do {
                System.out.print("Missatge ('sortir' per tancar): ");
                missatge = consola.readLine();
                out.writeObject(missatge);
                out.flush();
            } while (!missatge.equalsIgnoreCase(MSG_SORTIR));

            fil.join();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                servidor.pararServidor();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
