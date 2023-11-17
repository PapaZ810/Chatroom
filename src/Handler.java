import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDateTime;

public class Handler
{
	private HashMap<String, BufferedWriter> clients;

	/**
	 * this method is invoked by a separate thread
	 */
	public void process(Socket client, HashMap<String, BufferedWriter> clients) throws java.io.IOException {
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
                switch (message.substring(0, message.indexOf("<"))) {
                    case "user" -> {
                        username = message.substring(message.indexOf("<") + 1, message.indexOf(">"));
						System.out.println(username);
                        if (username.contains("<") || username.contains(">") || username.contains(",")) {
                            toClient.write("2\n");
                        } else if (clients.containsKey(username)) {
                            toClient.write("1\n");
                        } else if (username.length() > 20) {
                            toClient.write("3\n");
                        } else {
                            clients.put(username, toClient);
                            toClient.write("4\n");
                        }
                        toClient.flush();
                    }
                    case "broadcast" -> {
                        for (String key : clients.keySet()) {
                            clients.get(key).write(message + "\n");
                            clients.get(key).flush();
                        }
                    }
                    case "private" -> {
                        String sender = message.substring(message.indexOf("<") + 1, message.indexOf(","));
                        String time = message.substring(message.indexOf(",") + 1, message.lastIndexOf(","));
                        String recipient = message.substring(message.lastIndexOf(",") + 1, message.indexOf(">"));
                        if (clients.containsKey(recipient)) {
                            clients.get(recipient).write(message + "\n");
                            clients.get(recipient).flush();
                        } else {
                            clients.get(sender).write("9\n");
                            clients.get(sender).flush();
                        }
                    }
                    case "ls" -> {
                        String sender = message.substring(message.indexOf("<") + 1, message.indexOf(">"));
                        ArrayList<String> keys = new ArrayList<>(clients.keySet());
                        clients.get(sender).write("userlist<");
                        for (String key : keys) {
                            clients.get(sender).write(key + ",");
                        }
                        clients.get(sender).write(">\n");
                        clients.get(sender).flush();
                    }
                    case "exit" -> {
                        String sender = message.substring(message.indexOf("<") + 1, message.indexOf(">"));
                        clients.remove(sender).close();
                        for (String key : clients.keySet()) {
                            clients.get(key).write("broadcast<" + sender + "," + LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute() + "," + sender + " has left the chatroom.>\n");
                            clients.get(key).flush();
                        }
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
            if (fromClient != null)
                fromClient.close();
		}
	}
}