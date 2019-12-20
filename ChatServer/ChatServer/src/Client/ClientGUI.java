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

    Socket s;
    private static DataOutputStream dos;
    private static DataInputStream dis;
    final static int ServerPort = 9000;
    static JTextPane userField;
    static String keyword = "";
    static String sendkeyword = "";
    static String pmUser="";
    final static ArrayList<String> pmChatList = new ArrayList<>();
    final static ArrayList<String> chatBroadcastList = new ArrayList<>();
    JScrollPane userList;
    JTextPane inputBox;
    JScrollPane scrollPane,chatViewScroll;
    static JButton sendButton, sendPMButton,connectButton;
    JList<String> list;
    JTextPane chatView;
    JFrame chatFrame,registerFrame;
    JLabel stateMsg;
    JTextField username;
    public ClientGUI(int ServerPort)
    {

        try {


            // establish the connection
            s = new Socket("127.0.0.1", ServerPort);

            // obtaining input and out streams
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());

            if(dis!=null){
                createRegisterFrame();
                createChatFrame();
                readMessage();
            }


            /*
            * Register Username Button
            * */
            connectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //Get input from userRegister frame to register user
                    String usernameInput = username.getText();
                    keyword = "IDEN ";
                    sendCommandToServer(keyword,usernameInput);
                }
            });

            /*
            * Send a broadcast message
            * */
            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendMsg();
                }
            });
            /*
            * Send a private message Button
            * */
            sendPMButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendMsg();

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
                        sendCommandToServer(keyword,"");
                        System.exit(-1);

                    }
                }
            });

        }catch (Exception e){
            System.err.println("Invalid Port");
        }


    }

    private void createChatFrame() {
        chatFrame = new JFrame("Chat");
        chatFrame.getContentPane().setLayout(null);
        chatFrame.setSize(500, 500);

        chatView = new JTextPane();
        chatView.setBounds(10,10,375,325);
        chatView.setEditable(false);




        userField = new JTextPane();
        userField.setBounds(390,10,100,325);
        userField.setBackground(Color.LIGHT_GRAY);
        userField.setEditable(false);

        userList = new JScrollPane(userField);
        userList.setBounds(390,10,100,325);


        /*Textfield for sending message*/
        inputBox = new JTextPane();
        inputBox.setBounds(10, 360, 375, 75);
        inputBox.setEditable(true);

        sendButton = new JButton("Send");
        sendButton.setBounds(400, 385, 80, 40);



        sendPMButton = new JButton("Send PM");
        sendPMButton.setBounds(400, 385, 80, 40);
        sendPMButton.setVisible(false);

        scrollPane  = new JScrollPane(inputBox);
        scrollPane.setBounds(10, 360, 375, 75);


        chatViewScroll  = new JScrollPane(chatView);
        chatViewScroll.setBounds(10,10,375,325);





        chatFrame.add(scrollPane);
        chatFrame.add(chatViewScroll);
        chatFrame.add(userList);
        chatFrame.add(sendButton);
        chatFrame.add(sendPMButton);
        chatFrame.setResizable(false);

        chatList();
    }


    public void createRegisterFrame(){

        //Register user form
        registerFrame = new JFrame("Register");
        registerFrame.getContentPane().setLayout(null);
        registerFrame.setSize(450,100);

        stateMsg = new JLabel("");
        stateMsg.setBounds(0,0,400,20);


        username = new JTextField();
        username.setBounds(10,20,200,20);

        connectButton = new JButton("Connect");
        connectButton.setBounds(210,20,100,20);

        registerFrame.add(stateMsg);
        registerFrame.add(username);
        registerFrame.add(connectButton);
        registerFrame.setVisible(true);
        registerFrame.setResizable(false);
    }
/*
* Send message Broadcast and Private Messages
* */
public void sendMsg(){
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
                    sendCommandToServer(sendkeyword,msg);
                }else if(sendkeyword.equals("HAIL ")){
                    sendCommandToServer(sendkeyword,msg);
                }
                inputBox.setText(null);
            }

        }

    });
    sendPMMessage.start();
}
public void readMessage(){
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

                            String messageStart = msg.substring(8);
                            String user = messageStart.substring(0,messageStart.indexOf(" "));

                            System.out.println(user);
                            chatBroadcastList.add("New message from " + user);
                            pmChatList.add(msg);
                            chatPMList();
                        }

                        if(msg.equals("Update List")){
                            keyword = "LIST";
                            sendCommandToServer(keyword,"");

                        }

                        if(keyword.equals("IDEN ") || registerFrame.isVisible()){
                            stateMsg.setText(msg);

                            if(msg.equals("OK Welcome to the chat server "+ username.getText())){
                                registerFrame.setVisible(false);
                                chatFrame.setTitle(username.getText());
                                chatFrame.setVisible(true);

                            }

                        }
                        if((sendkeyword.equals("HAIL ") || msg.contains("Broadcast from"))){

                            String[] lines = msg.split("\\r?\\n");
                            for(String line: lines){

                                if(line.contains("Broadcast from")){
                                    chatBroadcastList.add(line);
                                }

                            }
                            chatList();
                        }



                        if(keyword.equals("LIST")){

                            if(!msg.contains("Broadcast from") && !msg.contains("PM from") && !msg.contains("OK your message has been sent")){
                                String[] lines = msg.split("\\r?\\n");
                                list = new JList<String>(lines);
                                list.addListSelectionListener(new ListSelectionListener() {
                                    @Override
                                    public void valueChanged(ListSelectionEvent e) {
                                        if(!list.getValueIsAdjusting()){


                                            if(list.getSelectedValue().equals(username.getText())){
                                                sendPMButton.setVisible(false);
                                                sendButton.setVisible(true);
                                                sendkeyword = "HAIL ";
                                                chatList();
                                            }else if(!list.getSelectedValue().equals(username.getText())){
                                                sendPMButton.setVisible(true);
                                                sendButton.setVisible(false);
                                                sendkeyword = "MESG ";
                                                pmUser = list.getSelectedValue().trim();
                                                chatPMList();
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
}

        /*
        * Send command to server by keyword and msg
        * */
    public static void sendCommandToServer(String keyword, String msg)
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
    /*
    * Return list of messages send to broadcast
    * */
    public void chatList(){
        String chatFiltered ="";
        for(String bMsg: chatBroadcastList){
            if(bMsg.contains("Broadcast from ")){
                chatFiltered += bMsg + "\n";
            }
            if(bMsg.contains("New message")){
                chatFiltered += bMsg + "\n";
            }
        }

        chatView.setText(chatFiltered);
        //return chatFiltered;
    }
    /*
    * Returns relevant Private messages for each user according to selected user
    * */
    public void chatPMList(){
        String pmFiltered ="";
        for(String pmMsg: pmChatList){
            if(pmMsg.contains("PM from "+ list.getSelectedValue())){
                pmFiltered += pmMsg + "\n";
            }
        }

        //return pmFiltered;
        chatView.setText(pmFiltered);
    }




    public static void main(String[] args) {

        ClientGUI clientGUI = new ClientGUI(ServerPort);


    }
}
