import java.io.*;
import java.net.Socket;

public class GestorClients implements Runnable {
    private Socket client;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private ServidorXat servidor;
    private String nom;
    private boolean sortir = false;

    public GestorClients(Socket client, ServidorXat servidor) throws IOException {
        this.client = client;
        this.servidor = servidor;
        this.oos = new ObjectOutputStream(client.getOutputStream());
        this.ois = new ObjectInputStream(client.getInputStream());
    }

    public String getNom() {
        return nom;
    }

    public void enviarMissatge(String missatge) {
        try {
            oos.writeObject(missatge);
            oos.flush();
        } catch (IOException e) {
            System.out.println("Error en enviar missatge a " + nom + ": " + e.getMessage());
        }
    }

    private void processaMissatge(String missatge) {
        String codi = Missatge.getCodiMissatge(missatge);
        String[] parts = Missatge.getPartsMissatge(missatge);

        if (codi == null || parts == null)
            return;

        switch (codi) {
            case Missatge.CODI_CONECTAR:
                nom = parts[1];
                servidor.afegirClient(this);
                break;
            case Missatge.CODI_SORTIR_CLIENT:
                sortir = true;
                servidor.eliminarClient(nom);
                break;
            case Missatge.CODI_SORTIR_TOTS:
                sortir = true;
                servidor.finalitzarXat();
                break;
            case Missatge.CODI_MSG_PERSONAL:
                String destinatari = parts[1];
                String missatgePersonal = parts[2];
                servidor.enviarMissatgePersonal(destinatari, nom, missatgePersonal);
                break;
            case Missatge.CODI_MSG_GRUP:
                String missatgeGrup = parts[1];
                servidor.enviarMissatgeGrup(Missatge.getMissatgeGrup("(" + nom + "): " + missatgeGrup));
                break;
            default:
                enviarMissatge("Codi desconegut: " + codi);
        }
    }

    @Override
    public void run() {
        try {
            while (!sortir) {
                String missatge = (String) ois.readObject();
                processaMissatge(missatge);
            }
        } catch (EOFException e) {
            // Client ha tancat la connexió
        } catch (IOException | ClassNotFoundException e) {
            if (!sortir) {
                System.out.println("Error en la comunicació amb " + nom + ": " + e.getMessage());
            }
        } finally {
            try {
                if (client != null)
                    client.close();
            } catch (IOException e) {
                System.out.println("Error en tancar el socket de " + nom + ": " + e.getMessage());
            }
        }
    }
}