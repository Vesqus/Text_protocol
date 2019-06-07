import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Scanner;

public class Client implements Runnable {

    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private long id;
    private boolean warunek = true;
    private static boolean connected = false;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());


    Client(String ip, int port) {
        try {
            System.out.println("Oczekuje na polaczenie");
            clientSocket = new Socket(ip, port);
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }


    }


    public String GenerateText(String operation, String status, long sessionid, String msg) {
        String text = "";
        text += "Czas-)" + timestamp.getTime() + "(|";
        text += "Operacja-)" + operation + "(|";
        text += "Status-)" + status + "(|";
        text += "Identyfikator-)" + sessionid + "(|";
        text += "Wiadomosc-)" + msg + "(|";
        return text;
    }

    public void execute(String operacja, String Status, String msg, long ID) {
        switch (operacja) {
            case "Invite": {
                if (Status.equals("Invite")) {      //zaproszenie drugiego użytkownika
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("Czy przyjmujesz zaproszenie od drugiego klienta? t/n ");
                    String odp = scanner.nextLine();
                    if (odp.equals("t")) {
                        try {
                            connected = true;
                            sendMessage("Invite", "Accept", "");    //zaakceptowanie zaproszenia
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    } else {
                        try {
                            sendMessage("Invite", "Reject", "");    //odrzucenie zaproszenia
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
                if (Status.equals("Accept")) {
                    connected = true;
                    System.out.println("Partner zaakceptowal polaczenie");
                }
                if (Status.equals("Reject")) {
                    System.out.println("Partner odrzucił polaczenie");
                }
                System.out.println("jestemTu");
            }
            break;
            case ("Przypisz_ID"): {
                id = ID;
            }
            break;
            case ("Send"):{
                System.out.println("\t"+msg);
            }
            break;
            case "Disconnect":
            {
                connected = false;
                System.out.println("Drugi klient rozłączył się");
            }
            break;
            default:
                System.out.println("Otrzymano niepoprawny typ operacji");
                break;
        }
    }

    //dekodowanie tektu

    public void decode(String text) {
        String[] linie = text.split("\\(\\|");
        Hashtable<String, String> klucz = new Hashtable<>();
        for (String ln : linie) {
            String[] temp = ln.split("-\\)");
            if(temp.length==2)
            klucz.put(temp[0],temp[1]);
        }
        execute(klucz.get("Operacja"), klucz.get("Status"), klucz.get("Wiadomosc"), Integer.parseInt(klucz.get("Identyfikator")));
    }


    public void sendMessage(String Operacja, String Status, String slowo) throws IOException {

        out.write(GenerateText(Operacja, Status, id, slowo).getBytes());

    }

    public void executeOrder66(String Order){
        try {
            switch (Order) {
                case "/invite": {
                    sendMessage("Invite", "Invite", "");
                }
                break;
                case "/disconnect":{
                    connected = false;
                    sendMessage("Disconnect","Disconnect","");
                    System.out.println("Komunikacja z drugim klientem została zakończona.\n" );
                }
                break;
                case "/exit": {
                    warunek = false;
                    sendMessage("Exit", "Exit", "");
                    System.out.println("Opuszczono serwer.\n");

                }
                break;
                default:{
                    System.out.println("Błędna komenda.\n");
                }
            }
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    // ustalanie połącznia miedzy klientami

    public static void main(String args[]) throws InterruptedException, IOException {
        Client k1 = new Client("192.168.1.66", 1203);
        Thread t1 = new Thread(k1);
        t1.start();
        boolean running = true;
        Scanner scanner = new Scanner(System.in);
        String typed;
        while (running) {
            typed = scanner.nextLine();
            if(typed.startsWith("/")) {
                if(typed.equals("/exit")) {
                    running = false;
                }
                k1.executeOrder66(typed);
            }
            else if (connected)
            {
                k1.sendMessage("Send","Send",typed);
            }
            else
            {
                System.out.println("Brak połączenia z drugim klientem, należy nawiązać połączenie.\n");
            }
        }
        //t1.join();

    }


    @Override
    public void run() {
        byte[] Buffer = new byte[256];
        try {
            while (warunek) {
                if(in.available()>0)
                {
                    in.read(Buffer);
                    decode(new String(Buffer));
                }
            }
        }
        catch (IOException e) {
        }
    }

}
