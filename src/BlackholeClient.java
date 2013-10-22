import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.esotericsoftware.kryonet.Client;


public class BlackholeClient implements ActionListener {

	//public static void main(String[] args) {

		//new Client();

	//}
	String filename = "test_song.mp3";
	String ip = "localhost";
	String port = "55555";
	Scanner keyboard;
	Socket socket;
	DecimalFormat df = new DecimalFormat("#.##");
	JFrame frame;
	JLabel label;
	JLabel percentageLabel;
	JLabel fileLabel;
	JLabel errorLabel;
	JButton cancelButton;
	Client client;
	public BlackholeClient() {
		
		// set up the gui, duh
		SetUpGUI();
		SetUpClient();
		
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
		
		// we are done here so tell the server we are done
		Request done_request = new Request();
		done_request.type=Request.DONE;
		client.sendTCP(done_request);
		
		// shut down
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
	public void SetUpClient() {
		// set up the client
		client = new Client();
		client.start();
		
		// find all of the servers. We dont do anything with this yet, but we will
		List<InetAddress> addressess = client.discoverHosts(35353, 2000);
		try {
			// connect to the server
			client.connect(5000, addressess.get(0), 53535, 35353);
		} catch (IOException e2) { e2.printStackTrace(); }
		// register the transfer objects
		Register.RegisterObjects(client.getKryo());
	}
	public void SetUpGUI() {
		frame = new JFrame();
		frame.setLayout(new GridLayout(5,1));
		label = new JLabel("Looking for Blackhole Servers", SwingConstants.CENTER);
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
	}
	public void Print(String s) {
		label.setText(s);
	}
	public void PrintPercentage(String s) {
		percentageLabel.setText(s);
	}
	public void CancelStream() {
		// shut it down
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
		frame.setVisible(false);
		System.exit(0);
	}
	public void actionPerformed(ActionEvent e) {
	    if ("cancel".equals(e.getActionCommand())) {
	    	CancelStream();
	    }
	} 
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
		try {
			fileInputStream = new FileInputStream(file);
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
		// get the file size so we can update the percentage sent
		long filesize = file.length();
		//Print("filesize = "+filesize);
		
		// send the file name
		Request filename_request = new Request();
		filename_request.type=Request.FILENAME;
		filename_request.text=filename;
		client.sendTCP(filename_request);
		
		
		// send the filesize
		Request filesize_request = new Request();
		filesize_request.type=Request.FILESIZE;
		filesize_request.text=filesize+"";
		client.sendTCP(filesize_request);
		
		// set up for file transfer
		float sent_size = 0.0f;
		byte[] buffer = new byte[256];
		int number;
		Request file_request = new Request();
		// while the buffer isnt negative 1, because if it is we have hit the EOF
		try {
			while ((number = fileInputStream.read(buffer)) != -1) {
				// write the buffer to the stream (sends it on its way)
				sent_size+=number;
				PrintPercentage("Sent "+df.format(((sent_size/filesize)*100.0f))+"%");
				
				// send the buffer data
				file_request.type=Request.FILE;
				file_request.buffer=buffer;
				client.sendTCP(file_request);
			}
		} catch (IOException e) { e.printStackTrace(); }
		return true;
	}
}
