import java.net.*;

public class Connection implements Runnable
{
	private Socket	client;
	private static Handler handler = new Handler();

	public Connection(Socket client) {
		this.client = client;
	}

	/**
	 * This method runs in a separate thread.
	 */
	public void run() { 
		try {
			handler.process(client);
		}
		catch (java.io.IOException ioe) {
			System.err.println(ioe);
		}
	}
}