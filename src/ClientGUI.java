import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
/*  */
/**
 * The ClientGUI class is responsible for creating the graphical user interface (GUI) of the chat client. 
 * It extends the JFrame class to create a window that contains a log area to display messages, an input field to type messages, 
 * and several buttons to perform actions such as sending messages, requesting the list of active users, and disconnecting from the server.
 *   
 * The class also handles the communication between the client and the server by opening a socket connection and creating
 * input and output streams. 
 *   
 * It creates a new thread to read input from the server and append it to the log area.
 * The class contains three methods to handle the three actions performed by the buttons: sendMessage() to send messages,
 * activeUsersRequest() to request the list of active users, and disconnectUser() to disconnect from the server. 
 *   
 * It also contains a method to connect to the server and start the communication.
 */
public class ClientGUI extends JFrame {
    private JTextArea logArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton ActiveUsersButton;
    private JButton DisconnectButton;
    private BufferedReader in;
    private BufferedWriter out;
    
    /**
     * This Constructor implements the client GUI for "JOLT".
     *
     * It includes a non-editable display area for messages, a user input text box, and three buttons:
     * "Send", "Active Users", and "Leave". It also establishes a connection with the server and sends/receives messages from it.
     */
    public ClientGUI() {
        super("JOLT"); // Client-Server Chat Program Name

        logArea = new JTextArea();  // non-editable display area for messages
        logArea.setEditable(false); 

        inputField = new JTextField(20); // User input text box

        sendButton = new JButton("Login");
        ActiveUsersButton = new JButton("Active Users"); //Buttons 
        DisconnectButton = new JButton("Leave");
        
        ActiveUsersButton.setEnabled(false);

        sendButton.addActionListener(new ActionListener() {  // when Send is clicked, message gets send and outputted onto displayArea
            public void actionPerformed(ActionEvent e) {
                sendMessage();
                sendButton.setText("Send");
                if (!ActiveUsersButton.isEnabled()){
                    ActiveUsersButton.setEnabled(true); 
                }
            }
        });
        
        ActiveUsersButton.addActionListener(new ActionListener() {  // when Active Users is clicked, it shows the active users
            public void actionPerformed(ActionEvent e) {
                activeUsersRequest();
            }
        });

        DisconnectButton.addActionListener(new ActionListener() {  // when Leave is clicked, user leaves the chat
            public void actionPerformed(ActionEvent e) {
                disconnectUser();
                System.exit(ABORT);
            }

        });

        JPanel inputPanel = new JPanel();  // New Panel with all feature incorparated 
        inputPanel.add(inputField);
        inputPanel.add(sendButton);
        inputPanel.add(ActiveUsersButton);
        inputPanel.add(DisconnectButton);
        inputPanel.add(DisconnectButton);

        JScrollPane scrollPane = new JScrollPane(logArea);  // Scrollable pane to read messages

        add(scrollPane, BorderLayout.CENTER);  //Allignment
        add(inputPanel, BorderLayout.SOUTH);


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(550, 650);
        setVisible(true);

        connectToServer();  // Connect Client to the server

    }
    
    /**
     * This Method establishes a connection to the server using a socket with the IP address and port number specified.
     *
     * Creates a new thread that reads input from an input stream and appends each line of input to the log area (logArea).
     * 
     * If an IOException occurs while reading input, it is printed to standard error output.
     * If an IOException occurs while establishing the connection, a message is printed to standard output
     * and the program is terminated.
     */
    private void connectToServer() {
        try {
            Socket socket = new Socket("25.52.120.239", 1234); // New client Socket created 
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Input stream
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // Output stream
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("Cannnot connect because server does not exist");  // If Client tries to connect to non-existant Server
            System.exit(ABORT);
        }

        new Thread(new Runnable() {
            public void run() {
                String inputLine;
                try {
                    while ((inputLine = in.readLine()) != null) {  //This code creates a new thread that reads input from an 
                        logArea.append(inputLine + "\n");          //input stream (in), and appends each line of input to a log area (logArea).
                    }
                } catch (IOException e) {
                    // e.printStackTrace();

                }
            }
        }).start();
    }
    
    /**
     * This Methoud sends a request to the server to retrieve a list of active users and displays the response in the GUI.
     * The method sends the message "/active_users" to the server and reads the response from the input stream.
     * If the connection to the server fails or an I/O exception occurs, the method prints the stack trace to the console.
    */
    private void activeUsersRequest() {
        String message = "/active_users"; // input message to be sent
        try {
            out.write(message + "\n"); // write the message to the output 
            out.flush(); // flush
            inputField.setText(""); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   
   /**
     * This Method sends a "/Disconnect:" message to the server to disconnect the user from the chat.
     * 
     * This Method writes the message to the output stream and flushes it, then clears the input field.
     * If an IOException occurs, it prints the stack trace.
    */
    private void disconnectUser() {
        
        try {
            String message = "/Disconnect:"; // Input message to be sent
            out.write(message + "\n"); // Write the message to the output 
            out.flush(); 
            inputField.setText(""); // Remove remaining text form input area
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   
    /**
     * This method sends a message to the server using the output stream.
     * If the input field is not empty, the message is written to the output stream and flushed,
     * then the input field is cleared.
     */
     private void sendMessage() {
        String message = inputField.getText(); // Input message to be sent
        if (!message.isEmpty()) { 
            try {
                out.write(message + "\n"); // Write the message to the output 
                out.flush();
                inputField.setText(""); // Remove remaining text form input area
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Main method to Create a new Client
     */
    public static void main(String[] args) {
        new ClientGUI(); 
    }
}





