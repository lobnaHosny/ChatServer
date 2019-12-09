package Server;


public class Runner
{
	static Server server;
	final static int PORT = 8080;
	
	public static void main(String[] args){
		server = new Server(PORT);
	}
	
	
}
