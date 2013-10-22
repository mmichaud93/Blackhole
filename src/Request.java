
public class Request {
	// object to be passed between the server and the client
	public String text;
	public int type;
	public byte[] buffer = new byte[256];
	
	public final static int FILENAME = 1000;
	public final static int FILESIZE = 2000;
	public final static int FILE = 3000;
	public final static int DONE = 4000;
}	
