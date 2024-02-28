
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.html.InlineView;

/**
 * This class creates a server object that has a client handler which keeps track of all the clients and their actions.
 * The server starts each client on its own thread so that they can send and receive messages concurrently
 * 
 * The backbone of this class is written with chat GPT and a youtube video called: "Java Socket Programming - Multiple Clients Chat"
 * We then refined the code as to meet the project specifications
 */
public class server {

    private List<ClientHandler> clients = new ArrayList<>(); // all current clients connected to the server
    private ServerSocket serverSocket;  // new serverSocket

    /**
     * This Constructor starts the server and clients
     * @param port is the port to which the sockets connect 
     * 
     */
    public server(int port) {  
        
        try {
            serverSocket = new ServerSocket(port); // Creating new serverSocket 
            System.out.println("Server started on port " + port);
            
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Creates new socket for client 
                ClientHandler client = new ClientHandler(clientSocket); // Creates new client to add to the server
                clients.add(client); // Adds to current users arraylist
                System.out.println("New socket connected: " + clientSocket.getInetAddress());
                client.start(); // Threading begins
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    /** 
     * This method sends a list of all online clients to the user that requests it
     * @param client the client that has requested the active users list
     */
    public void sendActiveUsers(ClientHandler client) {

        StringBuilder sb = new StringBuilder();  // creates new StringBuilders
        sb.append("____Online Users_____" + "\n\n");
        for (ClientHandler c : clients) {    
            if(c.clientName != null){
                sb.append(c.clientName);    // loops through all the ClientHandler objects in the clients collection 
                sb.append("\n");            // and appends the clientName field of each object to the StringBuilder.
            }
        } 
        sb.append("______________________" + "\n\n");
        try {
            client.out.write(sb.toString());
            client.out.flush();  // flushes
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * this class Creates the ClientHandler
     * 
     */
    private class ClientHandler extends Thread {  //
        private Socket clientSocket;
        private BufferedReader in;
        private BufferedWriter out;
        private String clientName;

        /**
         * This Constructor gives the client handler the correct socket
         * @param socket that needs to be used
         */
        public ClientHandler(Socket socket) {
            clientSocket = socket; // Connects client to server socket
        }
        
        /**
         *  This Method reads messages from clients and broadcasts them to all connected clients, and also handles
         *  requests for lists of active users and whisper messages. 
         *
         */
        public void run() { 
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // input bufferreader
                out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())); // output bufferreader
                
                
                clientName = in.readLine(); // Take first line of text as the clients name
                /////////
                if (!clientName.equals("/Disconnect:")){
                    broadcast(clientName + " has joined the chat."); // Broadcasts it out
                } else {
                        clients.remove(this);
                        clientSocket.close();   //Closes all Sockets and BufferReaders
                        in.close();
                        out.close();
                }

                String inputLine;

                try {
                    
                    while ((inputLine = in.readLine()) != null) {
        
                        if (inputLine.startsWith("/active_users")) {    //list of active users is requested
                            sendActiveUsers(this);
                        } else if(inputLine.startsWith("/Whisper:")){   //Whisper message is requested
                            
                            int index = inputLine.indexOf(" ",10);
                            String user = inputLine.substring(10, index);  // Uses SubString to find username of reciever and the message to be sent 
                            String messagetosend = "Whisper message recieved from "+clientName + ":" +inputLine.substring(index);
                            broadcastWhisper(clientName,user, messagetosend); // Broadcasts the Whisper
    
                        }else if(inputLine.startsWith("/Disconnect:")){
                            broadcast(clientName + " has left the chat.");  //Broadcasts that Client has left the server
                            clients.remove(this);
                            clientSocket.close();  //Closes all Sockets and BufferReaders
                            in.close();   
                            out.close();
                        }
                        else {     //normal groupchat message is sent and broadcasted
                            System.out.println(clientName + ": " + inputLine);
                            broadcast(clientName + ": " + inputLine);
                        }
                    
                    }


                } catch (IOException e) {
                    // TODO: handle exception
                    if (!clientName.equals("/Disconnect:")){
                    System.out.println(clientName +" left the chat");
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * This Method sends a private message form one user to another.
         * @param sender of the private message
         * @param username receiver of the whisper
         * @param message that is whispered
         */
        private void broadcastWhisper(String sender, String username, String message) {

            for (ClientHandler client : clients) { // loops through all clients and finds the client that needs to receive the message
            
                if (client.clientName.equals(username)){
                    
                    try {
                            client.out.write(message + "\n");
                            client.out.flush(); // flush
                            return;
                    } catch (IOException e) {
                            e.printStackTrace();
                    }
                }

            }
            message = "Username could not be found";             //if the user is not found the sender is notified
            broadcastWhisper(sender, sender, message);

        }

        /**
         * This Method broadcasts the message to all the other members of the groupchat
         * @param message that is broadcasted
         */
        private void broadcast(String message) {
            for (ClientHandler client : clients) { // loops through all clients 
            
            if (client != this){
                try {
                        client.out.write(message + "\n"); // write message to all other clients in chat
                        client.out.flush(); // flush
                } catch (IOException e) {
                        e.printStackTrace();
                }
            }else{
                try {
                    client.out.write(message + "\n");
                    client.out.flush(); 
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
            }
        }
       
    
    }
    
    /**
     * Main Method to initilally run the server with a unique port number
     */

    public static void main(String[] args) {
        int port = 1234;
        new server(port);  // new server
    }
}
