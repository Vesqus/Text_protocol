import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Random;

public class Serwer {


    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    public void startConnection(int port) //rozpoczecie połacznia przez odpowiedni port
    {
        try
        {
            serverSocket =new ServerSocket(port);

            Client_connect c1= new Client_connect(serverSocket,generateID());
            Client_connect c2=new Client_connect(serverSocket,generateID());
            serverSocket.close();
            c1.makePartner(c2.get());
            c2.makePartner(c1.get());

            Thread t1=new Thread(c1);
            Thread t2=new Thread(c2);

            t1.start();
            t2.start();

            t1.interrupt();
            t2.interrupt();
        }
        catch(IOException e)
        {

        }

    }


    public int generateID()     //generowanie ID klienta
    {
        Random zmienna=new Random();
        return zmienna.nextInt(510)+1;

    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }


public static void main(String[] args)
{
    Serwer server=new Serwer();
    server.startConnection(1203);

}

}
