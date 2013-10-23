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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;


public class BlackholeClient implements ActionListener {

	String filename = "";
	String ip = "";
	String port = "";
	Scanner keyboard;
	Socket socket;
	DecimalFormat df = new DecimalFormat("#.##");
	JFrame frame;
	JLabel label;
	JLabel percentageLabel;
	JButton cancelButton;
	Client client;
	InetAddress address;
	public BlackholeClient() {
		
		// set up the gui, duh
		SetUpGUI();
		// set up the client that will scan for servers
		client = new Client();//16384, 1024
		client.start();
		// find all of the servers.
		do {
			List<InetAddress>addresses = client.discoverHosts(35353, 1000);
			for(InetAddress adrs : addresses) {
				if(!adrs.getHostAddress().equals("127.0.0.1")) {
					address = adrs;
					break;
				}
			}
		} while(address==null);
		
		
		if(address==null) {
			Print("No address found");
		} else {
			try {
				// connect to the server
				client.update(5000);
				client.connect(15000, address, 53535);
			} catch (IOException e2) { e2.printStackTrace(); System.out.println("Could not connect to Server."); }
			
			// register the transfer objects
			Register.RegisterObjects(client.getKryo());
			
			// go through every file in the current folder
			File folder = new File("./");
			if(folder.isDirectory()) {
				for(String s : folder.list()) {
					Print("Trying ("+s+")");
					if(s.equals(Main.runningProgram)) {
						// it is looking at the program file now, we should skip it
						Print("Is program file");
					} else {
						// send the file to every server
						boolean success = SendFile(s);
						if(!success) {
							// failed to send the file (could be a directory)
							Print("Failed ("+s+")");
						} else {
							// succeeded
							Print("Succeded ("+s+")");
						}
					}
				}
			}
			// we are done here so tell the servers we are done
			Request done_request = new Request();
			done_request.type=Request.DONE;
			client.sendTCP(done_request);

			// shut down
			try {
				CancelStream();
			} catch (Exception e) {
				//PrintError(e.toString());
				e.printStackTrace();
			}
			// stop the program
			frame.setVisible(false);
			System.exit(0);
		}
	}
	
	class SendFileThread extends Thread {
		public InetAddress address;
		public Client client;
		@Override
		public void run() {
			
		}
		public void InitClient() {
			//client = SetUpClient(address);
		}
		public void SendFilename(String filename) {
			// send the file name
			Request filename_request = new Request();
			filename_request.type=Request.FILENAME;
			filename_request.text=filename;
			client.sendTCP(filename_request);
		}
		public void SendFilesize(long filesize) {
			// send the filesize
			Request filesize_request = new Request();
			filesize_request.type=Request.FILESIZE;
			filesize_request.text=filesize+"";
			client.sendTCP(filesize_request);
		}
		public void SendBuffer(byte[] buffer) {
			// send the buffer data
			Request file_request = new Request();
			file_request.type=Request.FILE;
			file_request.buffer=buffer;
			//client.sendUDP(file_request);
			client.sendTCP(file_request);
		}
		public void SendDone() {
			// send done
			Request done_request = new Request();
			done_request.type=Request.DONE;
			client.sendTCP(done_request);
		}
	}
	
	public Client SetUpClient(InetAddress address) {
		// set up the client
		Client client = new Client(16384, 2048);
		client.start();
		client.addListener(new Listener() {
            public void connected (Connection connection) {
                    System.out.println("Connected");
            }
		});
		client.setIdleThreshold((float) 0.5);
		try {
			// connect to the server
			client.update(5000);
			client.connect(5000, "10.0.0.10", 53535);
		} catch (IOException e2) { e2.printStackTrace(); }
		// register the transfer objects
		Register.RegisterObjects(client.getKryo());
		return client;
	}
	public void SetUpGUI() {
		frame = new JFrame();
		frame.setLayout(new GridLayout(3,1));
		label = new JLabel("Looking for Blackhole Servers", SwingConstants.CENTER);
		label.setPreferredSize(new Dimension(300, 30));
		percentageLabel = new JLabel("", SwingConstants.CENTER);
		percentageLabel.setPreferredSize(new Dimension(300, 30));
		//fileLabel = new JLabel("", SwingConstants.CENTER);
		//fileLabel.setPreferredSize(new Dimension(300, 30));
		//errorLabel = new JLabel("", SwingConstants.CENTER);
		//errorLabel.setPreferredSize(new Dimension(300, 30));
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
		//frame.add(fileLabel);
		//frame.add(errorLabel);
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
			//PrintError(e.toString());
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
		System.out.println("ERROR: "+s);
	}
	FileInputStream fileInputStream = null;
	OutputStream socketOutputStream = null;
	public boolean SendFile(String filename) {
		// open the file
		File file = new File(filename);
		//PrintFile(filename);
		
		// if the file doesnt exist, end the SendFile function
		if(!file.exists()) {
			PrintError("("+filename+") not found");
			return false;
		}
		// if the file cant be read, end the SendFile function
		if(!file.canRead()) {
			PrintError("Cant read ("+filename+")");
			return false;
		}
		// if the file is actually a folder, end the SendFile function
		if(file.isDirectory()) {
			PrintError("("+filename+") is a directory");
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
		
		// iterate through the servers and send the iflename and size
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
		byte[] buffer = new byte[Main.BUFFER_SIZE];
		int number;
		// while the buffer isnt negative 1, because if it is we have hit the EOF
		try {
			while ((number = fileInputStream.read(buffer)) != -1) {
				// write the buffer to the stream (sends it on its way)
				sent_size+=number;
				PrintPercentage("Sent "+df.format(((sent_size/filesize)*100.0f))+"%");
				
				// send the buffer data
				Request file_request = new Request();
				file_request.type=Request.FILE;
				file_request.buffer=buffer;
				//client.sendUDP(file_request);
				client.sendTCP(file_request);
			}
		} catch (IOException e) { e.printStackTrace(); }
		return true;
	}
	
}
