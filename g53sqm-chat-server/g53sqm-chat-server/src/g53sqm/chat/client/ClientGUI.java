package g53sqm.chat.client;
import javax.swing.*;

public class ClientGUI {
    private int port;

        public ClientGUI() {

            this.port =9000;


            JFrame f=new JFrame("Chat");//creating instance of JFrame
            f.getContentPane().setLayout(null);
            f.setSize(500, 500);

            JTextPane chat= new JTextPane();
            chat.setBounds(25,25,350,300);

            JTextPane userList= new JTextPane();
            userList.setBounds(300,25,150,320);

            JTextField input= new JTextField();
            input.setBounds(0, 350, 400, 50);

            JButton sendBtn = new JButton("Send");
            sendBtn.setBounds(400, 400, 50, 35);

            f.add(sendBtn);//adding button in JFrame
            f.setVisible(true);//making the frame visible
        }

    public static void main(String[] args) {

            ClientGUI client = new ClientGUI();
    }

}
