import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
	
	final int port = 55555;

	public static void main(String[] args) {
		new Server();
	}
	
	public Server() {
		try {
			ServerSocket server_socket = new ServerSocket(port);
			boolean running = true;
			while(running) {
				Socket socket = server_socket.accept();
			
				// get information about where something is coming from
				String recieving_address = socket.getLocalAddress().toString();
				int recieving_port = socket.getLocalPort();
				
				// define the byte buffer
				byte[] buffer = new byte[65536];
				int number;

				// get the input stream from the socket
				InputStream socketStream= socket.getInputStream();
				// reference the file and make sure it exists
				File f=new File("output.txt");
				if(!f.exists())
					f.createNewFile();

				// get the output stream for te file
				OutputStream fileStream=new FileOutputStream(f);
				// so now data flows into the prgram through the socket and out of the program into the file

				// while the socket isnt reading a -1
				while ((number = socketStream.read(buffer)) != -1) {
					// write to the file. So push the incoming data into the file
				    fileStream.write(buffer,0,number);
				}

				// close everything down
				fileStream.close();
				socketStream.close();
				socket.close();
			}
			
			server_socket.close();
		}
		catch(Exception e) {
			System.out.print("Problem with starting the blackhole: "+e.toString());
		}
	}

}
