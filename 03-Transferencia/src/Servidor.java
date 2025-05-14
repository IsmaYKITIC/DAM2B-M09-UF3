import java.io.*;
import java.net.*;

public class Servidor {
    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    private ServerSocket serverSocket;
    private Socket socket;

    public Socket connectar() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Acceptant connexions en -> " + HOST + ":" + PORT);
        System.out.println("Esperant connexio...");
        socket = serverSocket.accept();
        System.out.println("Connexio acceptada: " + socket.getRemoteSocketAddress());
        return socket;
    }

    public void tancarConnexio(Socket socket) throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            System.out.println("Tancant connexió amb el client: " + socket.getRemoteSocketAddress());
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    public void enviarFitxers(Socket socket) throws IOException {
        try {
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            System.out.println("Esperant el nom del fitxer del client...");
            String nomFitxer = (String) input.readObject();
            System.out.println("Nomfitxer rebut: " + nomFitxer);

            if (nomFitxer == null || nomFitxer.isEmpty()) {
                System.out.println("Nom del fitxer buit o nul. Sortint...");
                return;
            }

            Fitxer fitxer = new Fitxer(nomFitxer);
            byte[] contingut = fitxer.getContingut();

            if (contingut != null) {
                System.out.println("Contingut del fitxer a enviar: " + contingut.length + " bytes");
                output.writeObject(contingut);
                output.flush();
                System.out.println("Fitxer enviat al client: " + nomFitxer);
            } else {
                System.out.println("Error llegint el fitxer del client: " + nomFitxer);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Error en llegir l'objecte rebut: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        try {
            Socket socket = servidor.connectar();
            servidor.enviarFitxers(socket);
            servidor.tancarConnexio(socket);
        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }
}