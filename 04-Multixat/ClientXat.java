import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientXat {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean sortir = false;
    private String nomUsuari;

    public void connecta() throws IOException {
        socket = new Socket("localhost", 9999);
        oos = new ObjectOutputStream(socket.getOutputStream());
        System.out.println("Client connectat a localhost:9999");
        System.out.println("Flux d'entrada i sortida creat.");
    }

    public void tancarClient() {
        try {
            if (ois != null)
                ois.close();
            if (oos != null)
                oos.close();
            if (socket != null)
                socket.close();
            System.out.println("Flux d'entrada tancat.");
            System.out.println("Flux de sortida tancat.");
            System.out.println("Tancant client...");
        } catch (IOException e) {
            System.out.println("Error tancant client: " + e.getMessage());
        }
    }

    public void enviarMissatge(String missatge) throws IOException {
        oos.writeObject(missatge);
        oos.flush();
        System.out.println("Enviant missatge: " + missatge);
    }

    public void ajuda() {
        System.out.println("--------------------");
        System.out.println("Comandes disponibles:");
        System.out.println("1.- Conectar al servidor (primer pass obligatori)");
        System.out.println("2.- Enviar missatge personal");
        System.out.println("3.- Enviar missatge al grup");
        System.out.println("4.- (o línia en blanc)-> Sortir del client");
        System.out.println("5.- Finalitzar tothom");
        System.out.println("--------------------");
    }

    public String getLinea(Scanner sc, String msg, boolean obligatori) {
        String input;
        do {
            System.out.print(msg);
            input = sc.nextLine();
            if (!obligatori && input.isEmpty()) {
                return input;
            }
        } while (input.trim().isEmpty());
        return input;
    }

    public void executa() {
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("DEBUG: Iniciant rebuda de missatges...");

            while (!sortir) {
                try {
                    String missatgeRebut = (String) ois.readObject();
                    String codi = Missatge.getCodiMissatge(missatgeRebut);
                    String[] parts = Missatge.getPartsMissatge(missatgeRebut);

                    if (codi == null || parts == null)
                        continue;

                    switch (codi) {
                        case Missatge.CODI_SORTIR_TOTS:
                            sortir = true;
                            System.out.println("Tancant tots els clients.");
                            break;
                        case Missatge.CODI_MSG_PERSONAL:
                            if (parts.length >= 4) { // Ahora esperamos 4 partes: codi, destinatari, remitent, missatge
                                String remitent = parts[2];
                                String missatge = parts[3];
                                System.out.println("Missatge de (" + remitent + "): " + missatge);
                            }
                            break;
                        case Missatge.CODI_MSG_GRUP:
                            if (parts.length >= 2) {
                                System.out.println(parts[1]);
                            }
                            break;
                        default:
                            System.out.println("Codi desconegut: " + missatgeRebut);
                    }
                } catch (EOFException e) {
                    System.out.println("Error rebent missatge. Sortint...");
                    sortir = true;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!sortir) {
                System.out.println("Error en rebre missatges: " + e.getMessage());
            }
        } finally {
            tancarClient();
        }
    }

    public static void main(String[] args) {
        ClientXat client = new ClientXat();
        try {
            client.connecta();

            // Fil per rebre missatges
            new Thread(() -> client.executa()).start();

            Scanner sc = new Scanner(System.in);
            client.ajuda();

            while (!client.sortir) {
                String opcio = client.getLinea(sc, "Opció (1-5): ", false);

                if (opcio.isEmpty()) {
                    opcio = "4"; // Tractar línia buida com a sortir
                }

                switch (opcio) {
                    case "1":
                        String nom = client.getLinea(sc, "Introdueix el nom: ", true);
                        client.nomUsuari = nom;
                        client.enviarMissatge(Missatge.getMissatgeConectar(nom));
                        client.ajuda();
                        break;
                    case "2":
                        String dest = client.getLinea(sc, "Destinatari: ", true);
                        String msg = client.getLinea(sc, "Missatge a enviar: ", true);
                        // Ahora enviamos: CODI#destinatari#remitent#missatge
                        client.enviarMissatge(
                                Missatge.CODI_MSG_PERSONAL + "#" + client.nomUsuari + "#" + msg);
                        client.ajuda();
                        break;
                    case "3":
                        String msgGrup = client.getLinea(sc, "Missatge: ", true);
                        client.enviarMissatge(Missatge.getMissatgeGrup("(" + client.nomUsuari + "): " + msgGrup));
                        client.ajuda();
                        break;
                    case "4":
                        client.enviarMissatge(Missatge.getMissatgeSortirClient("Adéu"));
                        client.sortir = true;
                        break;
                    case "5":
                        client.enviarMissatge(Missatge.getMissatgeSortirTots("Adéu"));
                        client.sortir = true;
                        break;
                    default:
                        client.ajuda();
                }
            }
            sc.close();
        } catch (IOException e) {
            System.out.println("Error en el client: " + e.getMessage());
        }
    }
}