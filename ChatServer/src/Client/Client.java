package Client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client
{
    private static DataOutputStream dos;
    private static DataInputStream dis;
    final static int ServerPort = 8080;

    public static void main(String args[])
    {
        Scanner scn = new Scanner(System.in);

        // getting localhost ip
        try {
            InetAddress ip = InetAddress.getByName("localhost");


        // establish the connection
        Socket s = new Socket("127.0.0.1", ServerPort);


        // obtaining input and out streams
        dis = new DataInputStream(s.getInputStream());
        dos = new DataOutputStream(s.getOutputStream());


        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                while (true) {

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

        Thread readMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {

                while (true) {
                    try {
                        // read the message sent to this client
                        if(dis.available()>0){
                            String msg = dis.readUTF();
                            System.out.println(msg);
                            if(msg.equals("QUIT")){
                                s.close();
                                dis.close();
                                break;
                            }
                        }

                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                }
            }
        });
       readMessage.start();
       sendMessage.start();


        } catch (Exception e) {
            System.err.println("wrong port");
            //e.printStackTrace();
        }


    }
}
