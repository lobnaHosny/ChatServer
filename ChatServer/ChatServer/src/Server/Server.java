package Server;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;


public class Server {

	private ServerSocket server;
	Socket socket   = null;
	private ArrayList<Connection> list = new ArrayList<Connection>();
	Connection c;


	//Server constructor gets port number as input.
	public Server (int port) {
		try {
			server = new ServerSocket(port);
			System.out.println("Server has been initialised on port " + port);

		}catch (Exception e) {
			System.err.println("error initialising server");

		}


	}

	public void listenClient(){
		while (true){
			try {
				socket = server.accept();
			    System.out.println("Client Accepted "+ socket);


				// obtain input and output streams
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				c = new Connection(socket,this,dis,dos );
				Thread t = new Thread(c);

				t.start();
				list.add(c);
			} catch (IOException e) {

				e.printStackTrace();
			}

		}
	}

	public ArrayList<String> getUserList() {
		ArrayList<String> userList = new ArrayList<String>();
		for( Connection clientThread: list){
			if(clientThread.getState() == Connection.STATE_REGISTERED) {
				userList.add(clientThread.getUserName());
			}
		}
		return userList;
	}
	
	public boolean doesUserExist(String newUser) {
		for( Connection clientThread: list){
			if(clientThread.getState() == Connection.STATE_REGISTERED) {
				if(clientThread.getUserName().equals(newUser)){
					return true;
				}
			}
		}
		return false;
	}


	public void broadcastMessage(String theMessage){
		for( Connection clientThread: list){
			if(clientThread.getState() == Connection.STATE_REGISTERED){
				clientThread.messageForConnection(theMessage + System.lineSeparator());
			}
		}
	}
	
	public boolean sendPrivateMessage(String message, String user) {
		for( Connection clientThread: list) {
			if(clientThread.getState() == Connection.STATE_REGISTERED) {
				if(clientThread.getUserName().equals(user)) {
					clientThread.sendOverConnection(message);
					return true;
				}
			}

		}
		return false;
	}
	
	public void removeDeadUsers(){
		list.removeIf(c -> !c.isRunning());
	}
	//Get number of registered users
	public int getNumberOfUsers() {
		int counter = 0;
		for( Connection clientThread: list){
			if(clientThread.getState() == Connection.STATE_REGISTERED){
				counter++;
			}
		}
		return counter;
	}

	//Get number of connected users to server
	public int connectedNumberOfUsers(){
		return list.size();
	}
	protected void finalize() throws IOException{
		server.close();
	}
		
}
