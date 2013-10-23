import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;


public class BlackholeServer implements ActionListener {
	
	JFrame frame;
	JLabel label;
	JLabel secondLabel;
	JButton cancelButton;
	
	String filename;
	long filesize;
	float sent_size;
	final int port = 55555;
	Server server;
	
	DecimalFormat df = new DecimalFormat("#.##");
	Socket socket = null;
	InputStream socketStream = null;
	OutputStream fileStream = null;
	public BlackholeServer() {
		SetUpGUI();
		SetUpServer();	
	}
	public void SetUpServer() {
		// create the server object and start it
		server = new Server(16384, 2048);
		server.start();
		
		// give it the port to listen on
		try {
			server.bind(53535, 35353);
		} catch (IOException e) {
			e.printStackTrace();
			CancelStream();
		}
		// register the objects being sent
		Register.RegisterObjects(server.getKryo());
		// add the connection listener
		server.addListener(new Listener() {
			public void connected (Connection connection) {
				
			}
			public void received(Connection connection, Object object) {
				if (object instanceof Request) {
					Request request = (Request) object;
					// you got a thing
					// switch the type
					if(request.type == Request.FILENAME) {
						// you got the filename, now make the output file and open a stream to it
						filename = request.text;
						try { MakeFile(); } catch (IOException e) { e.printStackTrace(); }
						sent_size = 0.0f;
					} else if(request.type == Request.FILESIZE) {
						// you got the file size, all lights green
						filesize=Integer.parseInt(request.text);
					} else if(request.type ==  Request.FILE) {
						// we are receiving the file
						sent_size+=request.buffer.length;//BufferLength(request.buffer);
						secondLabel.setText("Recieved "+df.format(((sent_size/filesize)*100.0f))+"%");
						// write to the file. So push the incoming data into the file
					    try { fileStream.write(request.buffer); } catch (IOException e) { e.printStackTrace(); }
					}  else if(request.type ==  Request.DONE) {
						// we are done, so you should say that
						System.out.println("Client quit");
						secondLabel.setText("");
						label.setText("Waiting for Blackhole Clients");
					}
				}
			}
		});
	}
	public void MakeFile() throws IOException {
		filename = TrimString(filename);
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
	}
	public void SetUpGUI() {
		frame = new JFrame();
		frame.setLayout(new GridLayout(3,1));
		label = new JLabel("Waiting for Blackhole Clients", SwingConstants.CENTER);
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
	}
	public void CancelStream() {
		// shut down the streams, then exit
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
		// begin from the end of the string and iterate until you dont hit a space
		for(int i = in.length()-1; i > 0; i--) {
			if(in.charAt(i)!=0) {
				return in.substring(0, i+1);
			}
		}
		return "";
	}
	public int BufferLength(byte[] buffer) {
		// begin from the end of the array and iterate until you dont hit a zero
		for(int i = buffer.length-1; i>0; i--) {
			if(buffer[i]!=0) {
				return i;
			}
		}
		return 0;
	}
}
