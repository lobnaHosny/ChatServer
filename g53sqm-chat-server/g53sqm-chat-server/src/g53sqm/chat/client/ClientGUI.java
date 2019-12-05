package g53sqm.chat.client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientGUI{
    private int port;
    private Socket server;
    JFrame registerFrame;
    JFrame chatFrame;
    JTextPane chatView;
    JTextPane userField;
    JTextArea inputBox;
    private String message = " ";
    private String name;
    BufferedReader input;
    PrintWriter output;

        public ClientGUI() {



            this.port =9000;

            JFrame registerFrame= new JFrame("Register");
            registerFrame.getContentPane().setLayout(null);
            registerFrame.setSize(300,100);

            JTextField username = new JTextField(this.name);
            username.setBounds(10,20,200,20);

            JButton connectButton = new JButton("Connect");
            connectButton.setBounds(210,20,80,20);

            JFrame chatFrame=new JFrame("Chat");
            chatFrame.getContentPane().setLayout(null);
            chatFrame.setSize(500, 500);

            JTextPane chatView= new JTextPane();
            chatView.setBounds(10,10,375,325);

            JTextPane userField= new JTextPane();
            userField.setBounds(390,10,100,325);
            userField.setBackground(Color.LIGHT_GRAY);

            JScrollPane userList= new JScrollPane(userField);
            userList.setBounds(390,10,100,325);

            JTextField usernameObject = new JTextField(this.name);
            usernameObject.setBounds(390,10,100,20);

            JTextArea inputBox= new JTextArea();
            inputBox.setBounds(10, 360, 375, 100);

            JButton sendButton = new JButton("Send");
            sendButton.setBounds(420, 385, 50, 35);

            registerFrame.add(username);
            registerFrame.add(connectButton);
            registerFrame.setVisible(true);
            registerFrame.setResizable(false);
            chatFrame.add(chatView);
            chatFrame.add(userList);
            chatFrame.add(usernameObject);
            chatFrame.add(inputBox);
            chatFrame.add(sendButton);
            chatFrame.setVisible(true);
            chatFrame.setResizable(false);

            connectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try{
                        name= username.getText();
                        server= new Socket("127.0.0.1", port);
                        input= new BufferedReader(new InputStreamReader(server.getInputStream()));
                        output= new PrintWriter(server.getOutputStream(), true);

                        output.println(username);

                        registerFrame.setVisible(false);
                        registerFrame.dispose();
                    }catch (Exception ee){

                    }
                }
            });

            //when send button is clicked
            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendMessage();
                }
            });
        }

        //method to send messages
        public void sendMessage(){
            try{
                String message = inputBox.getText();
                if (message.equals(" ")){
                    return;
                }
                this.message= message;
                output.println(message);
                inputBox.setText(null);
            } catch (Exception e){
                System.exit(0);
            }
        }

    public static void main(String[] args) {

            ClientGUI client = new ClientGUI();
    }

}
