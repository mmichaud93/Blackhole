import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
				
				// define the byte buffer
				byte[] buffer = new byte[65536];
				int number;

				// get the input stream from the socket
				InputStream socketStream= socket.getInputStream();
				
				// the first thing we get will be the filename
				String filename = "";
				// while the socket isnt reading a -1
				while ((number = socketStream.read(buffer)) != -1) {
					// write to the file. So push the incoming data into the filename
					filename += new String(buffer);
				}
				//System.out.println(filename);
				socketStream.close();
				
				// wait for the data
				socket = server_socket.accept();
				socketStream= socket.getInputStream();
				
				// reference the file and make sure it doesnt exist
				File f=new File(filename);
				if(f.exists()) {
					// it does so try <filename>(<n>) until you find a file that doesnt exist
					int n = 1;
					do {
						n++;
						int loc = filename.indexOf(".");
						String newName = filename.substring(0, loc)+"("+n+")"+filename.substring(loc);
						f=new File(newName);
						System.out.println(newName);
					} while(f.exists());
				}
				
				// create the new file
				f.createNewFile();
				// get the output stream for the file
				OutputStream fileStream=new FileOutputStream(f);
				// so now data flows into the program through the socket and out of the program into the file

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
			e.printStackTrace();
		}
	}

}
