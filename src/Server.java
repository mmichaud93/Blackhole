import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;


public class Server implements ActionListener {
	
	final int port = 55555;
	JLabel label;
	JLabel secondLabel;
	JButton cancelButton;
	String filename;
	long filesize;
	DecimalFormat df = new DecimalFormat("#.##");
	//public static void main(String[] args) {
		//new Server();
	//}
	Socket socket = null;
	InputStream socketStream = null;
	OutputStream fileStream = null;
	public Server() {
		try {
			ServerSocket server_socket = new ServerSocket(port);
			boolean running = true;
			JFrame frame = new JFrame();
			frame.setLayout(new GridLayout(3,1));
			label = new JLabel("", SwingConstants.CENTER);
			secondLabel = new JLabel("", SwingConstants.CENTER);
			JPanel buttonFrame = new JPanel();
			buttonFrame.setLayout(new GridLayout(1,3));
			cancelButton = new JButton("Cancel");
			cancelButton.setPreferredSize(new Dimension(100, 60));
			cancelButton.setActionCommand("cancel");
			cancelButton.addActionListener(this);
			buttonFrame.add(new JSeparator(JSeparator.HORIZONTAL));
			buttonFrame.add(cancelButton);
			buttonFrame.add(new JSeparator(JSeparator.HORIZONTAL));
			frame.setTitle("Blackhole Server");
			frame.setPreferredSize(new Dimension(400, 120));
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			frame.add(label);
			frame.add(secondLabel);
			frame.add(buttonFrame);
			frame.pack();
			frame.setVisible(true);
			while(running) {
				label.setText("Waiting for input");
				secondLabel.setText("");
				socket = server_socket.accept();
				
				// define the byte buffer
				byte[] buffer = new byte[65536];
				int number;

				// get the input stream from the socket
				socketStream= socket.getInputStream();
				
				// the first thing we get will be the filename
				filename = "";
				// while the socket isnt reading a -1
				while ((number = socketStream.read(buffer)) != -1) {
					// write to the filename.
					filename += new String(buffer);
				}
				filename = TrimString(filename);
				socketStream.close();
				
				// wait for the data
				socket = server_socket.accept();
				socketStream= socket.getInputStream();
				
				// the second thing we get will be the file size
				filesize = 0;
				// while the socket isnt reading a -1
				while ((number = socketStream.read(buffer)) != -1) {
					// write to the int.
					ByteBuffer wrapped = ByteBuffer.wrap(buffer);
					filesize =  wrapped.getLong();
				}
				socketStream.close();
				
				// wait for the data
				socket = server_socket.accept();
				socketStream= socket.getInputStream();
				
				// reference the file and make sure it doesnt exist
				File f=new File(Main.programPath+filename);
				if(f.exists()) {
					// it does so try <filename>(<n>) until you find a file that doesnt exist
					int n = 1;
					String newName;
					do {
						n++;
						int loc = filename.indexOf(".");
						newName = filename.substring(0, loc)+"("+n+")"+filename.substring(loc);
						f=new File(Main.programPath+newName);
						//System.out.println(newName);
					} while(f.exists());
					filename = newName;
				}
				label.setText(filename);
				// create the new file
				f.createNewFile();
				// get the output stream for the file
				fileStream=new FileOutputStream(f);
				// so now data flows into the program through the socket and out of the program into the file
				float sent_size = 0.0f;
				// while the socket isnt reading a -1
				while ((number = socketStream.read(buffer)) != -1) {
					sent_size+=number;
					secondLabel.setText("Recieved "+df.format(((sent_size/filesize)*100.0f))+"%");
					// write to the file. So push the incoming data into the file
				    fileStream.write(buffer,0,number);
				}

				// close everything down
				fileStream.close();
				socketStream.close();
				socket.close();
			}
			frame.setVisible(false);
			server_socket.close();
		}
		catch(Exception e) {
			label.setText(Main.programPath+filename);
			secondLabel.setText(e.toString());//e.getStackTrace()[e.getStackTrace().length-3].toString());
			e.printStackTrace();
		}
	}
	public void CancelStream() {
		try {
			if(socketStream!=null)
				socketStream.close();
			if(fileStream!=null)
				fileStream.close();
			if(socket!=null)
				socket.close();
		} catch (IOException e) {
			secondLabel.setText(e.toString());
			e.printStackTrace();
		}
		System.exit(0);
	}
	public void actionPerformed(ActionEvent e) {
	    if ("cancel".equals(e.getActionCommand())) {
	    	CancelStream();
	    }
	} 
	public String TrimString(String in) {
		for(int i = in.length()-1; i > 0; i--) {
			if(in.charAt(i)!=0) {
				return in.substring(0, i+1);
			}
		}
		return "";
	}
}
