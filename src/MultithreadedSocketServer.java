
import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class MultithreadedSocketServer {

    public static void main(String[] args) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]");
        try {
            // server socket with port 8888
            int port = 8888;
            ServerSocket server = new ServerSocket(port);// server, 8888

            // setting user id
            int userID = 0;
            new FileOutputStream("users.txt", true).close();
            new FileOutputStream("quizid.txt", true).close();
            //new FileOutputStream("users.txt", true).close();
            // welcoming message
            System.out.println("Online Qize Appliction Started ...");
            System.out.println("Server Log:");
            // while loop to always accept incoming client connections to the server
            while (true) {
                // starting user id at 1
                userID++;
                // server accept the client connection request
                Socket serverClient = server.accept();
                // for server log
                System.out.println(ServerClientThread.ANSI_CYAN + formatter.format(new Date())
                        + " User No: " + userID + " Established Connection!"
                        + ServerClientThread.ANSI_RESET);
                // creating a new User
                User newUser = new User(userID);
                // send the request to be handled by a separate thread
                ServerClientThread sct = new ServerClientThread(serverClient, newUser);
                // starting the thread
                sct.start();
            }
        } catch (Exception e) {
            System.out.println(formatter.format(new Date()) + " " + e.getMessage());
        }
    }
}
