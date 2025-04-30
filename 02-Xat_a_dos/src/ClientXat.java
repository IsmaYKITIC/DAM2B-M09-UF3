import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientXat {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connecta() throws IOException {
        socket = new Socket(ServidorXat.HOST, ServidorXat.PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        System.out.println("Client connectat a " + ServidorXat.HOST + ":" + ServidorXat.PORT);
        System.out.println("Flux d'entrada i sortida creat.");
    }

    public void enviarMissatge(String missatge) throws IOException {
        System.out.println("Enviant missatge: " + missatge);
        out.writeObject(missatge);
        out.flush();
    }

    public void tancarClient() throws IOException {
        System.out.println("Tancant client...");
        socket.close();
        System.out.println("Client tancat.");
    }

    public static void main(String[] args) {
        ClientXat client = new ClientXat();
        try {
            client.connecta();
            FilLectorCX fil = new FilLectorCX(client.in);
            fil.start();
            System.out.println("Fil de lectura iniciat");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Escriu el teu nom: ");
            String nom = scanner.nextLine();
            client.enviarMissatge(nom);

            String missatge;
            do {
                System.out.print("Missatge ('sortir' per tancar): ");
                missatge = scanner.nextLine();
                client.enviarMissatge(missatge);
            } while (!missatge.equalsIgnoreCase(ServidorXat.MSG_SORTIR));

            scanner.close();
            client.tancarClient();
            System.out.println("El servidor ha tancat la connexió.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
