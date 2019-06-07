import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Hashtable;

public class Client_connect implements Runnable {
    private long id;
    private Socket clientsocket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean warunek = true;
    private Client_connect partner;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());


    Client_connect(ServerSocket serversocket, int clientID) throws IOException {
        clientsocket = serversocket.accept();
        in = new DataInputStream(clientsocket.getInputStream());
        out = new DataOutputStream(clientsocket.getOutputStream());
        id = clientID;

        sendMessage("Przypisz_ID", "Przypisz_ID", "", id); //nadanie i wysłanie identyfikatora sesji

        System.out.println("Polaczono klient " + id);

    }

    public void makePartner(Client_connect partner) {
        this.partner = partner;
    }   //

    public Client_connect get() {                       //połączenie

        return this;
    }

    public Socket socket() {                            //dobranie socketu

        return clientsocket;
    }

    public void sendMessage(String operacja, String status, String msg, long ID) throws IOException {             // wysyłanie wiadomości

        out.write(GenerateText(operacja, status, ID, msg).getBytes());

    }

    public String GenerateText(String operation, String status, long sessionid, String msg) {              //generowanie tekstu
        String text = "";
        text += "Operacja-)" + operation + "(|";
        text += "Status-)" + status + "(|";
        text += "Identyfikator-)" + sessionid + "(|";
        text += "Wiadomosc-)" + msg + "(|";
        return text;

    }
    public long getID(){                                        // pozyskanie ID klienta
        return id;
    }

    //operacje wykonywalne

    public void execute(String operacja, String odpowiedz, String msg, long ID) {
        switch(operacja){
            case "Invite":
            {
                    try {
                        partner.sendMessage("Invite", odpowiedz, "", partner.getID());
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
            }
            break;
            case "Send":
            {
                try {
                    partner.sendMessage("Send", "Send", msg, partner.getID());
                }
                catch (IOException e){
                    System.out.println(e.getMessage());
                }
            }
            break;
            case "Disconnect":
            {
                try {
                    partner.sendMessage("Disconnect", "Disconnect", "", partner.getID());
                }
                catch (IOException e){
                    System.out.println(e.getMessage());
                }
            }
            break;
            case "Exit":
            {
                warunek = false;
                System.out.println("Klient "+id+" sie rozlaczyl.\n");
            }
            break;
        }
    }

    // dekodowanie tektu na znaki ASCI

    public void decode(String text) {
        String[] linie = text.split("\\(\\|");
        Hashtable<String, String> klucz = new Hashtable<>();
        for(String ln:linie){
            String[] temp = ln.split("-\\)");
            if(temp.length==2)
            klucz.put(temp[0],temp[1]);
        }
        execute(klucz.get("Operacja"),klucz.get("Status"),klucz.get("Wiadomosc"),Integer.parseInt(klucz.get("Identyfikator")));
    }


    @Override
    public void run() {
        byte[] Buffer = new byte[256];
        try {
            while (warunek) {
                in.read(Buffer);
                decode(new String(Buffer));
            }
        } catch (IOException e) {}

    }
}
