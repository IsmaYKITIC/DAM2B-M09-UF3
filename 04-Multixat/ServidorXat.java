import java.io.*;
import java.net.*;
import java.util.Hashtable;

public class ServidorXat {
    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    private static final String MSG_SORTIR = "sortir";
    private Hashtable<String, GestorClients> clients = new Hashtable<>();
    private boolean sortir = false;
    private ServerSocket serverSocket;

    public void servidorAEscoltar() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor iniciat a " + HOST + ":" + PORT);

            while (!sortir) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connectat: " + clientSocket.getInetAddress());
                GestorClients gestor = new GestorClients(clientSocket, this);
                new Thread(gestor).start();
            }
        } catch (IOException e) {
            if (!sortir) {
                System.out.println("Error al servidor: " + e.getMessage());
            }
        } finally {
            pararServidor();
        }
    }

    public void pararServidor() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error en tancar el servidor: " + e.getMessage());
        }
    }

    public void finalitzarXat() {
        enviarMissatgeGrup(Missatge.getMissatgeSortirTots(MSG_SORTIR));
        clients.clear();
        sortir = true;
        pararServidor();
        System.out.println("Tancant tots els clients.");
        System.out.println("DEBUG: multicast sortir");
    }

    public synchronized void afegirClient(GestorClients client) {
        clients.put(client.getNom(), client);
        enviarMissatgeGrup(Missatge.getMissatgeGrup("Entra: " + client.getNom()));
        System.out.println(client.getNom() + " connectat.");
        System.out.println("DEBUG: multicast Entra: " + client.getNom());
    }

    public synchronized void eliminarClient(String nom) {
        if (clients.containsKey(nom)) {
            clients.remove(nom);
            enviarMissatgeGrup(Missatge.getMissatgeGrup("Surt: " + nom));
            System.out.println("DEBUG: multicast Surt: " + nom);
        }
    }

    public synchronized void enviarMissatgeGrup(String missatge) {
        for (GestorClients client : clients.values()) {
            client.enviarMissatge(missatge);
        }
    }

    public synchronized void enviarMissatgePersonal(String destinatari, String remitent, String missatge) {
        GestorClients client = clients.get(destinatari);
        if (client != null) {

            client.enviarMissatge(Missatge.getMissatgePersonal(remitent, missatge));
            System.out.println("Missatge personal per (" + destinatari + ") de (" + remitent + "): " + missatge);
        }
    }

    public static void main(String[] args) {
        ServidorXat servidor = new ServidorXat();
        servidor.servidorAEscoltar();
    }
}