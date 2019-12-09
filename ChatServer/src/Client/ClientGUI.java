package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientGUI{

    private static DataOutputStream dos;
    private static DataInputStream dis;
    final static int ServerPort = 8080;
    static ArrayList<String> broadcastMessageList;
        public static void main(String[] args){
           broadcastMessageList = new ArrayList<>();
            try {


            // establish the connection
            Socket s = new Socket("127.0.0.1", ServerPort);

            // obtaining input and out streams
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());


            //Register user form
            JFrame registerFrame = new JFrame("Register");
            registerFrame.getContentPane().setLayout(null);
            registerFrame.setSize(450,100);

            JLabel stateMsg = new JLabel("");
            stateMsg.setBounds(0,0,400,20);


            JTextField username = new JTextField();
            username.setBounds(10,20,200,20);

            JButton connectButton = new JButton("Connect");
            connectButton.setBounds(210,20,100,20);

            JFrame chatFrame=new JFrame("Chat");
            chatFrame.getContentPane().setLayout(null);
            chatFrame.setSize(500, 500);

            JTextPane chatView= new JTextPane();
            chatView.setBounds(10,10,375,325);
            chatView.setEditable(false);


            JTextPane userField= new JTextPane();
            userField.setBounds(390,10,100,325);
            userField.setBackground(Color.LIGHT_GRAY);

            JScrollPane userList= new JScrollPane(userField);
            userList.setBounds(390,10,100,325);

            /*JTextField usernameObject = new JTextField();
            usernameObject.setBounds(390,10,100,20);*/

            /*Textfield for sending message*/
            JTextArea inputBox= new JTextArea();
            inputBox.setBounds(10, 360, 375, 100);

            JButton sendButton = new JButton("Send");
            sendButton.setBounds(375, 385, 100, 40);



            registerFrame.add(stateMsg);
            registerFrame.add(username);
            registerFrame.add(connectButton);
            registerFrame.setVisible(true);
            registerFrame.setResizable(false);

            chatFrame.add(chatView);
            chatFrame.add(userList);
            chatFrame.add(inputBox);
            chatFrame.add(sendButton);
            chatFrame.setResizable(false);



            connectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //Get input from userRegister frame to register user
                    Thread registerUser = new Thread(new Runnable()
                    {
                        @Override
                        public void run() {


                            // read the message to deliver.
                            String msg = username.getText();

                            try {


                                // write on the output stream
                                if(!msg.isEmpty()){
                                    dos.writeUTF("IDEN "+msg);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    });
                    registerUser.start();
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
                                stateMsg.setText(msg);
                                System.out.println(msg);

                                String chat = "";
                                if(msg.equals("QUIT")){
                                    s.close();
                                    dis.close();
                                    dos.close();
                                    break;
                                }
                                if(msg.equals("OK Welcome to the chat server "+ username.getText())){
                                    registerFrame.setVisible(false);
                                    chatFrame.setTitle(username.getText());
                                    chatFrame.setVisible(true);



                                }
                                if(msg.contains("Broadcast from")){

                                    broadcastMessageList.add(msg);

                                }
                                if(msg.contains("CONNNECTED") || msg.contains("DISCONNECTED")){
                                    dos.writeUTF("LIST");
                                    String numberOnlineUsers = dis.readUTF();
                                    userField.setText(numberOnlineUsers);
                                }

                                for(String bMessage: broadcastMessageList){
                                    chat = chat + bMessage;

                                }
                                chatView.setText(chat);
                            }

                        } catch (IOException e) {

                            e.printStackTrace();
                        }
                    }
                }
            });
            readMessage.start();


            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Thread sendMessage = new Thread(new Runnable()
                    {
                        @Override
                        public void run() {


                            // read the message to deliver.
                            String msg = inputBox.getText();

                            try {


                                // write on the output stream
                                if(!msg.isEmpty()){
                                    dos.writeUTF("HAIL "+msg);
                                    inputBox.setText(null);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    });
                    sendMessage.start();
                }

            });
            /*Some piece of code*/
            chatFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    if (JOptionPane.showConfirmDialog(chatFrame,
                            "Are you sure you want to close this window?", "Close Window?",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                    {
                        //System.exit(0);
                        try {
                            dos.writeUTF("QUIT");
                            System.exit(0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            }catch (Exception e){
                System.err.println("Invalid Port");
                //e.printStackTrace();
            }

    }



    ////////////////////////////////////////

}
