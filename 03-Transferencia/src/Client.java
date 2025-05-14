import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String DIR_ARRIBADA = "/tmp";
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    public void connectar() throws IOException {
        socket = new Socket("localhost", 9999);
        System.out.println("Connectant a -> localhost:9999");
        System.out.println("Connexio acceptada: " + socket.getRemoteSocketAddress());
        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());
    }

    public void tancarConnexio() throws IOException {
        if (output != null)
            output.close();
        if (input != null)
            input.close();
        if (socket != null && !socket.isClosed()) {
            socket.close();
            System.out.println("Connexio tancada.");
        }
    }

    public void rebreFitxers() throws IOException {
        try {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Nom del fitxer a rebre ('sortir' per sortir): ");
                String nomFitxer = scanner.nextLine();

                if ("sortir".equalsIgnoreCase(nomFitxer)) {
                    System.out.println("Sortint...");
                    break;
                }

                output.writeObject(nomFitxer);
                output.flush();

                System.out.print("Nom del fitxer a guardar: ");
                String nomGuardar = DIR_ARRIBADA + "/" + new File(nomFitxer).getName();
                System.out.println(nomGuardar);

                byte[] contingut = (byte[]) input.readObject();

                try (FileOutputStream fos = new FileOutputStream(nomGuardar)) {
                    fos.write(contingut);
                    System.out.println("Fitxer rebut i guardat com: " + nomGuardar);
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Error en llegir l'objecte rebut: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.connectar();
            client.rebreFitxers();
            client.tancarConnexio();
        } catch (IOException e) {
            System.out.println("Error en el client: " + e.getMessage());
        }
    }
}