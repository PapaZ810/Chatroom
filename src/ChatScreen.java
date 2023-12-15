/**
 * This program is a rudimentary demonstration of Swing GUI programming.
 * Note, the default layout manager for JFrames is the border layout. This
 * enables us to position containers using the coordinates South and Center.
 * <p>
 * Usage:
 * java ChatScreen
 * <p>
 * When the user enters text in the textfield, it is displayed backwards
 * in the display area.
 */

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;


public class ChatScreen extends JFrame implements ActionListener, KeyListener {
    private JButton sendButton;
    private JButton exitButton;
    private JButton chatToggle;
    private JButton listUpdateButton;
    //private JComboBox<String> userDropdown;
    private JTextField sendText;
    private JTextArea displayArea;
    private static BufferedWriter toServer;
    public static String username;
    private static Boolean isPrivate;
    private static Vector<String> userList;
    private final JPanel userListPanel;
    private static String recipient;

    public static final int PORT = 5045;

    public ChatScreen() {
        /**
         * a panel used for placing components
         */
        JPanel p = new JPanel();

        Border etched = BorderFactory.createEtchedBorder();
        Border titled = BorderFactory.createTitledBorder(etched, "Enter Message Here ...");
        p.setBorder(titled);

        /**
         * set up all the components
         */
        sendText = new JTextField(30);
        sendButton = new JButton("Send");
        exitButton = new JButton("Exit");
        chatToggle = new JButton("Private");
        listUpdateButton = new JButton("Update List");
        //userDropdown = new JComboBox<>(userList);

        /**
         * register the listeners for the different button clicks
         */
        sendText.addKeyListener(this);
        sendButton.addActionListener(this);
        exitButton.addActionListener(this);
        chatToggle.addActionListener(this);
        listUpdateButton.addActionListener(this);

        /**
         * add the components to the panel
         */
        p.add(chatToggle);
        //p.add(userDropdown);
        p.add(sendText);
        p.add(sendButton);
        p.add(listUpdateButton);
        p.add(exitButton);

        /**
         * add the panel to the "south" end of the container
         */
        getContentPane().add(p, "South");

        /**
         * add the text area for displaying output. Associate
         * a scrollbar with this text area. Note we add the scrollpane
         * to the container, not the text area
         */
        displayArea = new JTextArea(15, 50);
        displayArea.setEditable(false);
        displayArea.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(displayArea);
        getContentPane().add(scrollPane, "West");

        userListPanel = new JPanel();
        Border userListBorder = BorderFactory.createEtchedBorder();
        userListPanel.setBorder(userListBorder);
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));

        // Add the new user list panel to the container
        getContentPane().add(userListPanel, "East");

        /**
         * set the title and size of the frame
         */
        setTitle("Chatroom");
        pack();

        setVisible(true);
        sendText.requestFocus();

        /** anonymous inner class to handle window closing events */
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });

        recipient = username;
        isPrivate = false;

    }

    /**
     * Displays a message
     */
    public void displayMessage(String message) {
        displayArea.append(message + "\n");
    }

    /**
     * This gets the text the user entered and outputs it
     * in the display area.
     */
    public void displayText() {
        String message = sendText.getText().trim();
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        try {
            if (isPrivate) {
                toServer.write("private<" + username + "," + recipient + "," + currentTime + "," + message + ">\n");
            } else {
                toServer.write("broadcast<" + username + "," + currentTime + "," + message + ">\n");
            }
            toServer.flush();
        } catch (IOException ioe) {
            System.out.println(ioe);
        }

        sendText.setText("");
        sendText.requestFocus();
    }


    /**
     * This method responds to action events .... i.e. button clicks
     * and fulfills the contract of the ActionListener interface.
     */
    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();

        if (source == sendButton) {
            displayText();
        } else if (source == chatToggle) {
            isPrivate = !isPrivate;
            if (isPrivate) {
                chatToggle.setText("Public");
            } else {
                chatToggle.setText("Private");
            }
        } else if (source == exitButton) {
            try {
                toServer.write("exit<" + username + ">\n");
                toServer.flush();
                toServer.close();
            } catch (IOException e) {
                System.out.println(e);
            }
            System.exit(0);
        } else if (source == listUpdateButton) {
            updateUserList();
        }
    }

    public void updateUserList() {
        // Clear existing components
        userListPanel.removeAll();
		/*try{
			toServer.write("ls<" + username + ">\n");
			toServer.flush();
		} catch(IOException e) {
			System.out.println(e);
		}*/
        // Add clickable labels for each user in the updated list
        for (String user : userList) {
            JLabel userLabel = new JLabel(user);
            userLabel.setForeground(Color.BLUE); //labelColors.get(user));
            userLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            userLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleUserClick(userLabel.getText());
                }
            });
            userListPanel.add(userLabel);
        }

        // Repaint and revalidate the panel to reflect changes
        userListPanel.repaint();
        userListPanel.revalidate();
    }

    // Method to handle user clicks
    private void handleUserClick(String clickedUser) {
        // Handle the click action (e.g., initiate private chat with the clicked user)
        recipient = clickedUser;

        Component[] components = userListPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                if (label.getText().equals(clickedUser)) {
                    label.setForeground(Color.RED);
                } else {
                    label.setForeground(Color.BLUE);
                }
            }
        }
    }

    /**
     * These methods respond to keystroke events and fulfills
     * the contract of the KeyListener interface.
     */

    /**
     * This is invoked when the user presses
     * the ENTER key.
     */
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            displayText();
    }

    /** Not implemented */
    public void keyReleased(KeyEvent e) {
    }

    /** Not implemented */
    public void keyTyped(KeyEvent e) {
    }

    public void setUserList(Vector<String> userVector) {
        userList = userVector;
    }


    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                System.out.println("Usage: java ChatScreen <host> <username>");
                System.exit(0);
            }
            Socket annoying = new Socket(args[0], PORT);
            ChatScreen win = new ChatScreen();

            username = args[1];
            win.displayMessage("My name is " + username);
            System.out.println();

            toServer = new BufferedWriter(new OutputStreamWriter(annoying.getOutputStream()));

            Thread ReaderThread = new Thread(new ReaderThread(annoying, win));
            Thread UpdateThread = new Thread(new UpdateThread(annoying, win));

            ReaderThread.start();
            UpdateThread.start();

            toServer.write("user<" + username + ">\n");
            toServer.flush();
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }
}

class UpdateThread implements Runnable {
    Socket server;
    ChatScreen screen;
    BufferedWriter toServer;

    public UpdateThread(Socket server, ChatScreen screen) {
        this.server = server;
        this.screen = screen;
    }

    public void run() {
        try {
            toServer = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));

            while (true) {
                toServer.write("ls<" + screen.username + ">\n");
                toServer.flush();
                Thread.sleep(5000);
                screen.updateUserList();
            }
        } catch (IOException e) {
            System.out.println(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
