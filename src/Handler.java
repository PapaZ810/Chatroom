import java.net.*;
import java.io.*;

public class Handler
{

	/**
	 * this method is invoked by a separate thread
	 */
	public void process(Socket client) throws java.io.IOException {
		DataOutputStream toClient = null;
		int count = 0;

		try {
			toClient = new DataOutputStream(client.getOutputStream());

			while (true) {
				String message = "[" + count + "]\n";	
				toClient.writeBytes(message);
				
				try {
					Thread.sleep(5000);
				}
				catch (InterruptedException ie) { }

				count++;
			}
		}
		catch (IOException ioe) {
			System.err.println(ioe);
		}
		finally {
			// close streams and socket
			if (toClient != null)
				toClient.close();
		}
	}
}