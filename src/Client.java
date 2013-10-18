import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;


public class Client {

	public static void main(String[] args) {

		new Client();

	}
	String filename = "test.txt";
	String ip = "localhost";
	String port = "55555";
	Scanner keyboard;
	public Client() {
		keyboard = new Scanner(System.in);
		// input the filename
		// input the target ip address
		// input the port
		
		// this is all commented to keep the defaults
		/*while(filename==null) {
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
		} while(port==null || !Validate.IsValidPort(port));*/
		
		// make sure the file exists
		File file = new File(filename);
		if(!file.exists()) {
			System.out.println("File ("+filename+") not found");
			System.exit(0);
		}
		// open a connection to the destination
		try {
			Socket socket = new Socket(ip, Integer.parseInt(port));
			
			// define the holder for our data
			byte[] buffer = new byte[65536];
			int number;
			// make the file input stream
			FileInputStream fileInputStream = new FileInputStream(file);
			// now the socket output stream
			OutputStream socketOutputStream = socket.getOutputStream();
			// so basically the file goes into the program via the input stream, then out of the program by the output stream

			// while the buffer isnt negative 1, because if it is we have hit the EOF
			while ((number = fileInputStream.read(buffer)) != -1) {
				// write the buffer to the stream (sends it on its way)
			    socketOutputStream.write(buffer, 0, number);
			}

			// we are done so close things
			socketOutputStream.close();
			fileInputStream.close();
			socket.close();
		} catch(Exception e) {
			System.out.println("There was a problem starting the connection, maybe the server isnt running? : "+e.toString());
		}
	}

}
