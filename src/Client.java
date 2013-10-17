import java.util.Scanner;


public class Client {

	public static void main(String[] args) {

		new Client();

	}
	String filename = null;
	String ip = "";
	String port = null;
	Scanner keyboard;
	public Client() {
		keyboard = new Scanner(System.in);
		// input the filename
		// input the target ip address
		// input the port
		while(filename==null) {
			System.out.print("Filename: ");
			filename = keyboard.next();
		}
		do {
			System.out.print("IP Address: ");
			ip = keyboard.next();
		} while(ip==null || !Validate.IsValidIP(ip));
		do {
			System.out.print("Port: ");
			port = keyboard.next();
		} while(port==null || !Validate.IsValidPort(port));
		// open a connection to the destination
	}

}
