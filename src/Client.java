import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;


public class Client implements ActionListener {

	//public static void main(String[] args) {

		//new Client();

	//}
	String filename = "test_song.mp3";
	String ip = "localhost";
	String port = "55555";
	Scanner keyboard;
	Socket socket;
	DecimalFormat df = new DecimalFormat("#.##");
	JLabel label;
	JLabel percentageLabel;
	JLabel fileLabel;
	JLabel errorLabel;
	JButton cancelButton;
	public Client() {
		//keyboard = new Scanner(System.in);
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
		
		JFrame frame = new JFrame();
		frame.setLayout(new GridLayout(5,1));
		label = new JLabel("HELLO WORLD", SwingConstants.CENTER);
		label.setPreferredSize(new Dimension(300, 30));
		percentageLabel = new JLabel("", SwingConstants.CENTER);
		percentageLabel.setPreferredSize(new Dimension(300, 30));
		fileLabel = new JLabel("", SwingConstants.CENTER);
		fileLabel.setPreferredSize(new Dimension(300, 30));
		errorLabel = new JLabel("", SwingConstants.CENTER);
		errorLabel.setPreferredSize(new Dimension(300, 30));
		JPanel buttonFrame = new JPanel();
		buttonFrame.setLayout(new GridLayout(1,3));
		cancelButton = new JButton("Cancel");
		cancelButton.setPreferredSize(new Dimension(100, 60));
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		buttonFrame.add(new JSeparator(JSeparator.HORIZONTAL));
		buttonFrame.add(cancelButton);
		buttonFrame.add(new JSeparator(JSeparator.HORIZONTAL));
		
		frame.setTitle("Blackhole Client");
		frame.setPreferredSize(new Dimension(400, 120));
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.add(label);
		frame.add(percentageLabel);
		frame.add(fileLabel);
		frame.add(errorLabel);
		frame.add(buttonFrame);
		frame.pack();
		frame.setVisible(true);
		
		// go through every file in the current folder
		File folder = new File("./");
		if(folder.isDirectory()) {
			for(String s : folder.list()) {
				Print("Trying ("+s+")");
				if(s.equals(Main.runningProgram)) {
					// it is looking at the program file now, we should skip it
					Print("Is program file");
				} else if(!SendFile(s)) {
					// failed to send the file (could be a directory)
					Print("Failed ("+s+")");
				} else {
					// succeeded
					Print("Succeded ("+s+")");
				}
			}
		} else {
			
		}
		try {
			CancelStream();
		} catch (Exception e) {
			PrintError(e.toString());
			e.printStackTrace();
		}
		// stop the program
		frame.setVisible(false);
		System.exit(0);
	}
	public void Print(String s) {
		label.setText(s);
	}
	public void PrintPercentage(String s) {
		percentageLabel.setText(s);
	}
	public void CancelStream() {
		try {
			if(socketOutputStream!=null)
				socketOutputStream.close();
			if(fileInputStream!=null)
				fileInputStream.close();
			if(socket!=null)
				socket.close();
		} catch (IOException e) {
			PrintError(e.toString());
			e.printStackTrace();
		}
		System.exit(0);
	}
	public void actionPerformed(ActionEvent e) {
	    if ("cancel".equals(e.getActionCommand())) {
	    	CancelStream();
	    }
	} 
	/*public void PrintFile(String s) {
		fileLabel.setText(s);
	}*/
	public void PrintError(String s) {
		errorLabel.setText(s);
	}
	FileInputStream fileInputStream = null;
	OutputStream socketOutputStream = null;
	public boolean SendFile(String filename) {
		// open the file
		File file = new File(filename);
		//PrintFile(filename);
		// if the file doesnt exist, end the SendFile function
		if(!file.exists()) {
			//PrintError("("+filename+") not found");
			return false;
		}
		// if the file cant be read, end the SendFile function
		if(!file.canRead()) {
			//PrintError("Cant read ("+filename+")");
			return false;
		}
		// if the file is actually a folder, end the SendFile function
		if(file.isDirectory()) {
			//PrintError("("+filename+") is a directory");
			return false;
		}
		// get the file size so we can update the percentage sent
		long filesize = file.length();
		//Print("filesize = "+filesize);
		
		try {
			// define the holder for our data
			byte[] buffer = new byte[65536];
			int number;
			// make the file input stream
			fileInputStream = new FileInputStream(file);
			
			// open a connection to the destination
			socket = new Socket(ip, Integer.parseInt(port));
			// now the socket output stream
			socketOutputStream = socket.getOutputStream();
			// so basically the file goes into the program via the input stream, then out of the program by the output stream

			// send the filename first
			buffer = filename.getBytes();
			socketOutputStream.write(buffer);
			socketOutputStream.close();

			// open a connection to the destination
			socket = new Socket(ip, Integer.parseInt(port));
			// now the socket output stream
			socketOutputStream = socket.getOutputStream();

			// send the file size second
			ByteBuffer dbuf = ByteBuffer.allocate(16);
			dbuf.putLong(filesize);
			buffer = dbuf.array();
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
				PrintPercentage("Sent "+df.format(((sent_size/filesize)*100.0f))+"%");
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
				PrintError(e1.toString());
				e1.printStackTrace();
			}
			try {
				if(fileInputStream!=null) fileInputStream.close();
			} catch (IOException e1) {
				PrintError(e1.toString());
				e1.printStackTrace();
			}
			return false;
		}
		return true;
	}
}
