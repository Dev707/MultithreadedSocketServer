
import java.net.*;
import java.io.*;
import java.util.logging.*;

public class TCPClient {

    // i had to make them static to make them accessible by the reply() method
    // to achieve the project's requirement of enabling the client to exit even
    //while waiting for the server response
    static Socket socket;
    static DataOutputStream outStream;
    static BufferedReader br;
    static String clientMessage = "";
    static String serverMessage = "";
    static DataInputStream inStream;

    public static void main(String[] args) throws Exception {
        try {

            // creating client socket to connect to port 8888
            int port = 8888;
            socket = new Socket("127.0.0.1", port);
            System.out.println("Connecting to server with port number [" + port + "]");
        } catch (ConnectException e) {
            System.out.println("Server is not accepting connections because it "
                    + "is not running or stacked (Connection refused: connect)");
            System.out.println(e.getMessage());
            System.exit(0);
        }
        System.out.println("Connected! [ You can exit the connection at any time"
                + " using ('quit', 'exit') ]");
        System.out.println("Enter \u001B[33m'help'\u001B[0m or \u001B[33m'h'\u001B[0m"
                + " for a list of built-in commands\nNote* [ All commands must end with \u001B[33m'#'\u001B[0m ]");
        try {
            // server response getter
            inStream = new DataInputStream(socket.getInputStream());
            // client request dispatcher
            outStream = new DataOutputStream(socket.getOutputStream());
            // read client entry
            br = new BufferedReader(new InputStreamReader(System.in));
            // starting while loop for the connection
            while (true) {
                // thread for user responses to be flexible and do not have to wait for
                // the server to send a message to be able to exit the program, 
                //user can exit at any point of time
                // by writing one of the following list ['exit', 'quit']
                final Thread outThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            // calling the method of user requests
                            reply();
                        } catch (IOException ex) {
                            Logger.getLogger(TCPClient.class.getName())
                                    .log(Level.SEVERE, null, ex);
                        }
                    }
                };
                // starting thread
                outThread.start();
                // listening to server messages
                serverMessage = inStream.readUTF();
                // printing server messages
                System.out.print(serverMessage);
            }
        } catch (Exception e) {
            // any exception occurs, just quit the program i.e. close connection
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    public static void reply() throws IOException {
        //System.out.println("1- "+serverMessage);
        //System.out.println("2- "+clientMessage);
        // if the server sent any message BUT that message then take user input
        //if (!serverMessage.matches("User \\d\\d?\\d?>> Closing Connection...")) {
        // reading user input
        clientMessage = br.readLine().toLowerCase();
        // exiting even while the client is waiting for a message from the server
        if (clientMessage.equalsIgnoreCase("exit")
                || clientMessage.equalsIgnoreCase("quit")) {
            System.out.print("Exitting...");
            socket.close();
            return;
        }
        if (clientMessage.replace("08:", "").replace("#", "").trim().equalsIgnoreCase("logout")) {
            System.out.print("Logout...");
            socket.close();
            return;
        }
        // dispatching request to the server
        outStream.writeUTF(clientMessage);
        outStream.flush();
        //}
//        // if the user chosed ( n ) and the server sent that specific message, then close the connection
//        if ( serverMessage.matches("User \\d\\d?\\d?>> Closing Connection...")) {
//            socket.close();
//        }
    }

}
