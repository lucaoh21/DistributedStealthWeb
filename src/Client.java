import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	private static Socket socket;
	private static BufferedReader in;
	private static PrintWriter out;
	
	public static void main(String[] args) {
		String ipAddress = "139.140.219.73"; 
		int port = 8080;
		
		try {
			socket = new Socket(ipAddress, port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			
			out.write("Hello this is a really long          message that has no meaning");
			
			String message = in.readLine();
			System.out.println("IN:" + message);
			
		} catch (UnknownHostException e) {
			System.out.println(ipAddress + " is unknown.");
			System.exit(1);
		} catch (IOException e) {
			System.out.println("IO connection to " + ipAddress + " did not work.");
			System.exit(1);
		}
		
	}

}
