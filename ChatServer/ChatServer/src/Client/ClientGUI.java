package Client;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientGUI extends JFrame{

    private static DataOutputStream dos;
    private static DataInputStream dis;
    final static int ServerPort = 8080;
    static JTextPane userField;
    static String keyword = "";
    static String sendkeyword = "";
    static String pmUser="";
    static String currentNumberUsers = "0";
    String chatBroadcast = "";
    final static ArrayList<String> pmChatList = new ArrayList<>();
    final static ArrayList<String> chatBroadcastList = new ArrayList<>();

    JTextPane inputBox;
    JScrollPane scrollPane;
    static JButton sendButton;
    static JButton sendPMButton;
    JList<String> list;
    public ClientGUI()
    {

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

            JFrame chatFrame = new JFrame("Chat");
            chatFrame.getContentPane().setLayout(null);
            chatFrame.setSize(500, 500);





            JTextPane chatView= new JTextPane();
            chatView.setBounds(10,10,375,325);
            chatView.setEditable(false);




            userField = new JTextPane();
            userField.setBounds(390,10,100,325);
            userField.setBackground(Color.LIGHT_GRAY);
            userField.setEditable(false);

            JScrollPane userList = new JScrollPane(userField);
            userList.setBounds(390,10,100,325);

            /*JTextField usernameObject = new JTextField();
            usernameObject.setBounds(390,10,100,20);*/

            /*Textfield for sending message*/
            inputBox = new JTextPane();
            inputBox.setBounds(10, 360, 375, 75);
            inputBox.setEditable(true);

            sendButton = new JButton("Send");
            sendButton.setBounds(400, 385, 80, 40);

            chatView.setText(chatList());

            sendPMButton = new JButton("Send PM");
            sendPMButton.setBounds(400, 385, 80, 40);
            sendPMButton.setVisible(false);

            scrollPane  = new JScrollPane(inputBox);
            scrollPane.setBounds(10, 360, 375, 75);


            JScrollPane chatViewScroll  = new JScrollPane(chatView);
            chatViewScroll.setBounds(10,10,375,325);



            registerFrame.add(stateMsg);
            registerFrame.add(username);
            registerFrame.add(connectButton);
            registerFrame.setVisible(true);
            registerFrame.setResizable(false);

            chatFrame.add(scrollPane);
            chatFrame.add(chatViewScroll);
            chatFrame.add(userList);
            chatFrame.add(sendButton);
            chatFrame.add(sendPMButton);
            chatFrame.setResizable(false);


            connectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //Get input from userRegister frame to register user
                    String usernmaeInput = username.getText();
                    keyword = "IDEN ";
                    regiserUser(keyword,usernmaeInput);
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

                                if(msg.contains("PM from")){
                                    System.out.println(msg);
                                    String messageStart = msg.substring(8);
                                    String user = messageStart.substring(0,messageStart.indexOf(" "));

                                    System.out.println(user);
                                    chatBroadcastList.add("New message from " + user);
                                    pmChatList.add(msg);
                                    chatView.setText(chatPMList());
                                }

                                if(msg.contains("Update List")){
                                    keyword = "LIST";
                                    regiserUser(keyword,"");

                                }

                                if(keyword.equals("IDEN ") || registerFrame.isVisible()){
                                    stateMsg.setText(msg);

                                    if(msg.equals("OK Welcome to the chat server "+ username.getText())){
                                        registerFrame.setVisible(false);
                                        chatFrame.setTitle(username.getText());
                                        chatFrame.setVisible(true);

                                    }
                                    System.out.println(msg);

                                }
                                if((sendkeyword.equals("HAIL ") || msg.contains("Broadcast from"))){

                                    String[] lines = msg.split("\\r?\\n");
                                    chatBroadcast ="";
                                    for(String line: lines){

                                        if(line.contains("Broadcast from")){
                                            chatBroadcastList.add(line);
                                            chatBroadcast = chatBroadcast + line+"\n";
                                        }


                                    }
                                    chatView.setText(chatList());
                                }



                                if(keyword.equals("LIST")){

                                    if(!msg.contains("Broadcast from") && !msg.contains("PM from") && !msg.contains("OK your message has been sent")){
                                        String[] lines = msg.split("\\r?\\n");
                                        list = new JList<String>(lines);
                                        list.addListSelectionListener(new ListSelectionListener() {
                                            @Override
                                            public void valueChanged(ListSelectionEvent e) {
                                                if(!list.getValueIsAdjusting()){
                                                    //sendkeyword ="";

                                                    if(list.getSelectedValue().equals(username.getText())){
                                                        sendPMButton.setVisible(false);
                                                        sendButton.setVisible(true);
                                                        sendkeyword = "HAIL ";
                                                        chatView.setText(chatList());
                                                    }else if(!list.getSelectedValue().equals(username.getText())){
                                                        sendPMButton.setVisible(true);
                                                        sendButton.setVisible(false);
                                                        sendkeyword = "MESG ";
                                                        pmUser = list.getSelectedValue().trim();
//
                                                        chatView.setText(chatPMList());

                                                    }
                                                }

                                            }
                                        });
                                        userList.setViewportView(list);

                                    }

                                }



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

                            // write on the output stream

                            if(!msg.isEmpty()){
                                if(sendkeyword.equals("HAIL ")){
                                    regiserUser(sendkeyword,msg);
                                }
                                inputBox.setText(null);
                            }

                        }

                    });
                    sendMessage.start();

                }
            });
            sendPMButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Thread sendPMMessage = new Thread(new Runnable()
                    {
                        @Override
                        public void run() {
                            // read the message to deliver.
                            String msg = inputBox.getText();

                            // write on the output stream

                            if(!msg.isEmpty()){

                                if(sendkeyword.equals("MESG ")){
                                    msg = pmUser +" "+msg;
                                    regiserUser(sendkeyword,msg);

                                }
                                inputBox.setText(null);
                            }

                        }

                    });
                    sendPMMessage.start();

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
                        keyword ="QUIT";
                        regiserUser(keyword,"");
                        System.exit(-1);



                    }
                }
            });

        }catch (Exception e){
            System.err.println("Invalid Port");
            //e.printStackTrace();
        }


    }





    public static void regiserUser(String keyword,String msg)
    {
        Thread registerUser = new Thread(new Runnable()
        {
            @Override
            public void run() {

                try {
                    // write on the output stream
                    if (!keyword.isEmpty()) {
                        dos.writeUTF(keyword + msg);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
        registerUser.start();

    }
    public String chatList(){
        String chatFiltered ="";
        for(String bMsg: chatBroadcastList){
            if(bMsg.contains("Broadcast from ")){
                chatFiltered += bMsg + "\n";
            }
            if(bMsg.contains("New message")){
                chatFiltered += bMsg + "\n";
            }
        }

        return chatFiltered;
    }
    public String chatPMList(){
        String pmFiltered ="";
        for(String pmMsg: pmChatList){
            if(pmMsg.contains("PM from "+ list.getSelectedValue())){
                pmFiltered += pmMsg + "\n";
            }
        }

        return pmFiltered;
    }




    public static void main(String[] args) {



        ClientGUI clientGUI = new ClientGUI();


    }
}
