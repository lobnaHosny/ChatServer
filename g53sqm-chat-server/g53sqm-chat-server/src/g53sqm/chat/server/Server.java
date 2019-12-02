package g53sqm.chat.server;

import g53sqm.chat.server.client.Client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;


public class Server {

	private ServerSocket server;
	private Socket socket   = null;
	private DataInputStream in       =  null;
	private ArrayList<Connection> list = new ArrayList<Connection>();
	Connection c;
	String line = "";

	public Server (int port) {
		try {
			server = new ServerSocket(port);
			System.out.println("Server has been initialised on port " + port);
			//list = new ArrayList<Connection>();

				while (true){
					socket = server.accept();
					System.out.println("Client Accepted "+ socket);

					try {
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
		catch (IOException e) {
			System.err.println("error initialising server");
			e.printStackTrace();
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
		boolean result = false;
		for( Connection clientThread: list){
			if(clientThread.getState() == Connection.STATE_REGISTERED) {
				result = clientThread.getUserName().compareTo(newUser)==0;
			}
		}
		return result;
	}
	
	public void broadcastMessage(String theMessage){
		System.out.println(theMessage);
		for( Connection clientThread: list){
			clientThread.messageForConnection(theMessage + System.lineSeparator());	
		}
	}
	
	public boolean sendPrivateMessage(String message, String user) {
		for( Connection clientThread: list) {
			if(clientThread.getState() == Connection.STATE_REGISTERED) {
				if(clientThread.getUserName().compareTo(user)==0) {
					clientThread.messageForConnection(message + System.lineSeparator());
					return true;
				}
			}
		}
		return false;
	}
	
	public void removeDeadUsers(){
		Iterator<Connection> it = list.iterator();
		while (it.hasNext()) {
			Connection c = it.next();
			if(!c.isRunning())
				it.remove();
		}
	}
	
	public int getNumberOfUsers() {
		return list.size();
	}
	
	protected void finalize() throws IOException{
		server.close();
	}
		
}
