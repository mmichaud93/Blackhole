import java.net.URI;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class Main {
	public static String runningProgram = "Blackhole.jar";
	public static String programPath = "./";

	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		// get the name of the programming running the blackhole so we arent also sending that over
		try {
			URI uri = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI();
			runningProgram = uri.getPath();
			programPath = runningProgram.substring(1, runningProgram.lastIndexOf('/')+1);
			runningProgram = runningProgram.substring(runningProgram.lastIndexOf('/')+1);
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		// if the running program name is null or empty, set the default
		if(runningProgram == null || runningProgram.length()==0) {
			runningProgram = "Blackhole.jar";
		}
		// ask the user if they want to start a client or a server
		JFrame frame = new JFrame();

        Object[] options = {"Client",
                            "Server"};
        int n = JOptionPane.showOptionDialog(frame,
            "Select one:",
            "Blackhole",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]);
        switch(n) {
        case 0:
        	// launch client
        	new BlackholeClient();
        	break;
        case 1:
        	// launch server
        	new BlackholeServer();
        	break;
        }
	}
}
