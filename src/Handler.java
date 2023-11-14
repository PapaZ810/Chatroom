import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Handler
{
	private ArrayList<BufferedWriter> clients;

	/**
	 * this method is invoked by a separate thread
	 */
	public void process(Socket client, ArrayList<BufferedWriter> clients) throws java.io.IOException {
		BufferedWriter toClient = null;
		BufferedReader fromClient = null;
		String message = null;
		String username = null;

		try {
			toClient = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));

			while (true) {
				message = fromClient.readLine();
				System.out.println(message);
				if (message.substring(0, message.indexOf("<")).equals("user")) {
					username = message.substring(message.indexOf("<"), message.indexOf(">"));
					if(username.contains("<") || username.contains(">") || username.contains(",")) {
						toClient.write(2);
						toClient.flush();
					}
					else if (clients.contains(username)) {
						toClient.write(1);
						toClient.flush();
					}
					else if (username.length() > 20) {
						toClient.write(3);
						toClient.flush();
					} else {
						clients.add(toClient);
						toClient.write(4);
						toClient.flush();
					}

				}
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