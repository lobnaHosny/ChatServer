package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client
{
    private static DataOutputStream dos;
    private static DataInputStream dis;
    final static int ServerPort = 9000;
    Socket s = null;
    String finalMessage = "";
    public Client(int ServerPort)
    {
        Scanner scn = new Scanner(System.in);

        // getting localhost ip
        try {
            InetAddress ip = InetAddress.getByName("127.0.0.1");


            // establish the connection
            s = new Socket(ip, ServerPort);

            // obtaining input and out streams
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
            if(dis != null){
                sendMessage(scn);
                readMessage();
            }

        } catch (Exception e) {
            System.err.println("wrong port");
        }


    }


    public void sendMessage(Scanner scn){
        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                while (scn.hasNext()) {
                    // read the message to deliver.
                    String msg = scn.nextLine();
                    try {
                        // write on the output stream
                        dos.writeUTF(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        sendMessage.start();
    }
    public String readMessage(){
        Thread readMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                while (true) {
                    try {
                        // read the message sent to this client
                        if(dis.available()>0 && dis!=null){
                            String msg = dis.readUTF();
                            finalMessage = msg;
                            if(!msg.equals("Update List")){
                                System.out.println(msg);
                            }
                            if(msg.equals("QUIT")){
                                s.close();
                                dis.close();
                                dos.close();
                                System.exit(0);
                                //break;
                            }
                        }
                    } catch (IOException e) {
                        //System.err.println(e);
                        e.printStackTrace();
                    }
                }
            }
        });
        readMessage.start();
        return finalMessage;
    }


    public static void main(String args[])
    {
        Client client = new Client(ServerPort);

    }
}
