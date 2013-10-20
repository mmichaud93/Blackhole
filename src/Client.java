import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Scanner;


public class Client {

	public static void main(String[] args) {

		new Client();

	}
	String filename = "test_song.mp3";
	String ip = "localhost";
	String port = "55555";
	Scanner keyboard;
	Socket socket;
	DecimalFormat df = new DecimalFormat("#.##");
	
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
		
		// go through every file in the current folder
		File folder = new File("./");
		if(folder.isDirectory()) {
			for(String s : folder.list()) {
				System.out.println("Trying ("+s+")");
				if(s.equals(Main.runningProgram)) {
					// it is looking at the program file now, we should skip it
					System.out.println("Is program file");
				} else if(!SendFile(s)) {
					// failed to send the file (could be a directory)
					System.out.println("Failed ("+s+")");
				} else {
					// succeeded
					System.out.println("Succeded ("+s+")");
				}
			}
		} else {
			
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// stop the program
		System.exit(0);
	}
	public boolean SendFile(String filename) {
		// open the file
		File file = new File(filename);
		
		// if the file doesnt exist, end the SendFile function
		if(!file.exists()) {
			System.out.println("("+filename+") not found");
			return false;
		}
		// if the file cant be read, end the SendFile function
		if(!file.canRead()) {
			System.out.println("Cant read ("+filename+")");
			return false;
		}
		// if the file is actually a folder, end the SendFile function
		if(file.isDirectory()) {
			System.out.println("("+filename+") is a directory");
			return false;
		}
		// get the file size so we can update the percentage sent
		long filesize = file.length();
		System.out.println("filesize = "+filesize);
		
		FileInputStream fileInputStream = null;
		OutputStream socketOutputStream = null;
		try {
			// open a connection to the destination
			socket = new Socket(ip, Integer.parseInt(port));
			// define the holder for our data
			byte[] buffer = new byte[65536];
			int number;
			// make the file input stream
			fileInputStream = new FileInputStream(file);
			// now the socket output stream
			socketOutputStream = socket.getOutputStream();
			// so basically the file goes into the program via the input stream, then out of the program by the output stream

			// send the filename first
			buffer = filename.getBytes();
			socketOutputStream.write(buffer);
			socketOutputStream.close();
			
			// open the stream
			socket = new Socket(ip, Integer.parseInt(port));
			socketOutputStream = socket.getOutputStream();
			
			// define holder for the file size sent
			float sent_size = 0.0f;
			
			// while the buffer isnt negative 1, because if it is we have hit the EOF
			while ((number = fileInputStream.read(buffer)) != -1) {
				// write the buffer to the stream (sends it on its way)
				sent_size+=number;
				System.out.println("Sent "+df.format(((sent_size/filesize)*100.0f))+"%");
			    socketOutputStream.write(buffer, 0, number);
			}

			// we are done so close things
			socketOutputStream.close();
			fileInputStream.close();
		} catch(Exception e) {
			e.printStackTrace();
			try {
				if(socketOutputStream!=null) socketOutputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if(fileInputStream!=null) fileInputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		}
		return true;
	}
}
