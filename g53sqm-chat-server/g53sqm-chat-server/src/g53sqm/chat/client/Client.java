package g53sqm.chat.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private String host;
    private int port;
    Socket socket = null;
    private BufferedReader in   = null;
    private PrintWriter out    = null;

    public Client(String host, int port) {
        this.host = "127.0.0.1";
        this.port = port;

        try{
            socket = new Socket(host,port);
            System.out.println("Connected");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(socket.getOutputStream(),true);

        }catch (UnknownHostException u){
            System.out.println(u);
        }catch (IOException i){
            System.out.println(i);
        }

        try{
            in.close();
            out.close();
            socket.close();
        }catch (IOException i){
            System.out.println(i);
        }
    }

    public static void main(String args[]){
        Client client= new Client("localhost", 9000);
    }
}
