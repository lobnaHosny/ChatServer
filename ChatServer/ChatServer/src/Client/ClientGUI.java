package Client;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
    static String keyword = "";
    static String sendkeyword = "";
    static String pmUser="", finalMessage ="";
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
            * Send message
            * */
            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendMsg();
                }
            });



            //Close dialog for chatframe
            chatFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    if (JOptionPane.showConfirmDialog(chatFrame,
                            "Are you sure you want to close this window?", "Close Window?",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                    {

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

    //Create Chatting Room
    private void createChatFrame() {

        //Frame holds all components for chatting with other user
        chatFrame = new JFrame("Chat");
        chatFrame.getContentPane().setLayout(null);
        chatFrame.setSize(500, 500);

        //White box to display received messages
        chatView = new JTextPane();
        chatView.setBounds(10,10,375,325);
        chatView.setEditable(false);

        //Scroll Pane that holds list of users
        userList = new JScrollPane();
        userList.setBounds(390,10,100,325);


        //Textfield for sending message
        inputBox = new JTextPane();
        inputBox.setBounds(10, 360, 375, 75);
        inputBox.setEditable(true);

        //Sending message button
        sendButton = new JButton("Send");
        sendButton.setBounds(400, 385, 80, 40);

        //Set text field to be scrollable
        scrollPane  = new JScrollPane(inputBox);
        scrollPane.setBounds(10, 360, 375, 75);

        //Set chatView to be scrollable view
        chatViewScroll  = new JScrollPane(chatView);
        chatViewScroll.setBounds(10,10,375,325);


        chatFrame.add(scrollPane);
        chatFrame.add(chatViewScroll);
        chatFrame.add(userList);
        chatFrame.add(sendButton);
        chatFrame.setResizable(false);

        //Set chatview to display broadcast view
        chatList();
    }


    //Register user form
    public void createRegisterFrame(){

        //Frame holds all components for registering user
        registerFrame = new JFrame("Register");
        registerFrame.getContentPane().setLayout(null);
        registerFrame.setSize(450,100);

        //StateMsg to display error messages from server
        stateMsg = new JLabel("");
        stateMsg.setBounds(0,0,400,20);

        //input text field to get username typed by user
        username = new JTextField();
        username.setBounds(10,20,200,20);

        //button to register user
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
            //Check if inputbox is not empty
            if(!msg.isEmpty()){
                //According to selected user it sends private message or broadcast message
                if(sendkeyword.equals("MESG ")){
                    msg = pmUser +" "+msg;
                    sendCommandToServer(sendkeyword,msg);
                }else if(sendkeyword.equals("HAIL ")){
                    sendCommandToServer(sendkeyword,msg);
                }
                //Sets input box to empty after sending message
                inputBox.setText(null);
            }

        }

    });
    sendPMMessage.start();
}

/*
* Read message sent from server
* and accordingly takes an action
*
* */
public String readMessage(){
    Thread readMessage = new Thread(new Runnable()
    {
        @Override
        public void run() {
            while (true) {
                try {
                    // read the message sent to this client
                    if(dis.available()>0){

                        String msg = dis.readUTF();
                        finalMessage = msg;

                        /*
                        * Saves all private messages in a list
                        * and gets the senders name
                        * Notifies current user with new message from in broadcast mode
                        * */
                        if(msg.contains("PM from")){

                            //get username
                            String messageStart = msg.substring(8);
                            String user = messageStart.substring(0,messageStart.indexOf(" "));
                            //Notifies current user with new message with username
                            chatBroadcastList.add("New message from " + user);
                            //stores all private messages in arraylist
                            pmChatList.add(msg);
                            //private messages are displayed
                            chatPMList();
                        }

                        /*
                        * List of online users updates when server sends Update List
                        * */
                        if(msg.equals("Update List")){
                            keyword = "LIST";
                            sendCommandToServer(keyword,"");
                        }

                        /*
                        *Displays error messages (if any) while registering user
                        *Close dialog once user registered and opens chatting room
                        * */
                        if(keyword.equals("IDEN ") || registerFrame.isVisible()){
                            stateMsg.setText(msg);

                            if(msg.equals("OK Welcome to the chat server "+ username.getText())){
                                registerFrame.setVisible(false);
                                chatFrame.setTitle(username.getText());
                                chatFrame.setVisible(true);
                            }
                        }


                        /*
                        * Adds new message to list and displays all broadcasted messages
                        * */
                        if((sendkeyword.equals("HAIL ") || msg.contains("Broadcast from"))){
                            chatBroadcastList.add(msg);
                            chatList();
                        }
                        /*
                        * Update list of online user by recieving keyword LIST
                        * */
                        if(keyword.equals("LIST")){
                            if(!msg.contains("Broadcast from") && !msg.contains("PM from") && !msg.contains("OK your message has been sent")){
                                updateList(msg);
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
    return finalMessage;
}
/*
* Gets list of users and adds them to scrollable list field
* */
private void updateList(String listUser){
    String[] lines = listUser.split("\\r?\\n");
    list = new JList<String>(lines);
    list.addListSelectionListener(new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if(!list.getValueIsAdjusting()){
                //By user selecting themselves the messages will be broadcasted
                if(list.getSelectedValue().equals(username.getText())){
                    sendkeyword = "HAIL ";
                    chatList();
                }
                //By user selecting any other registered user
                else if(!list.getSelectedValue().equals(username.getText())){
                    sendkeyword = "MESG ";
                    pmUser = list.getSelectedValue().trim();
                    chatPMList();
                }
            }

        }
    });
    userList.setViewportView(list);
}

        /*
        * Send command to server by keyword and msg
        * Msg can be empty in 3 CASES:
        * STAT
        * LIST
        * QUIT
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
