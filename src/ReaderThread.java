/**
 * This thread is passed a socket that it reads from. Whenever it gets input
 * it writes it to the ChatScreen text area using the displayMessage() method.
 */

import java.io.*;
import java.net.*;
import javax.swing.*;

public class ReaderThread implements Runnable
{
	Socket server;
	BufferedReader fromServer;
	ChatScreen screen;

	public ReaderThread(Socket server, ChatScreen screen) {
		this.server = server;
		this.screen = screen;
	}

	public void run() {
		try {
			fromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));

			while (true) {
				String message = fromServer.readLine();
				System.out.println(message);
                switch (message) {
                    case "1" -> screen.displayMessage("Username already taken. Please try again.\n");
                    case "2" -> screen.displayMessage("Username contains invalid characters. Please try again.\n");
                    case "3" -> screen.displayMessage("Username is too long. Please try again.\n");
                    case "4" -> screen.displayMessage("Welcome to Zac and Landon's Chatroom");
                    default -> screen.displayMessage(message);
                }
			}
		}
		catch (IOException ioe) { System.out.println(ioe); }

	}
}
