
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

class ServerClientThread extends Thread {

    // Data Members
    Socket serverClient; // client socket
    User user;// client object
    public static final String ANSI_GREEN = "\u001B[32m";// green color is for server messages
    public static final String ANSI_CYAN = "\u001B[36m";// cyan color is for server messages
    public static final String ANSI_RED = "\u001B[31m";// red color is for server detected errors
    public static final String ANSI_YELLOW = "\u001B[33m";// yellow color is for help message
    public static final String ANSI_RESET = "\u001B[0m";// resetting color
    // client messages will have a white foreground
    SimpleDateFormat formatter = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]");
    String quizname = "";

    // Constructor
    ServerClientThread(Socket serverClient, User user) {
        this.serverClient = serverClient;
        this.user = user;
    }

    // running thread method
    @Override
    public void run() {
        try {
            // client request getter
            DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
            // server response dispatcher
            DataOutputStream outStream = new DataOutputStream(serverClient.getOutputStream());
            // client and server messages
            String clientMessage = "", serverMessage = "";

            // starting the while loop of the connection
            while (true) {
                serverMessage = "";
                serverMessage = ANSI_CYAN + "Server>> Enter your command: " + ANSI_RESET;
                outStream.writeUTF(serverMessage);
                outStream.flush();
                /////////////////////
                clientMessage = inStream.readUTF().trim();
                //System.out.println("Test 1");
                if (clientMessage == null || clientMessage.equalsIgnoreCase("help") || clientMessage.equalsIgnoreCase("h")) {
                    serverMessage = help(serverMessage);
                } else {
                    try {
                        //System.out.println("Test 2");
                        switch (input(clientMessage).substring(0, 2)) {
                            case "01":
                                serverMessage = Register(input(clientMessage).substring(3));
                                break;
                            case "02":
                                //System.out.println("Test");
                                if (user.getUserName() == null) {
                                    serverMessage = login(input(clientMessage).substring(3));
                                    if (user.getUserName() != null) {
                                        serverMessage += (ANSI_CYAN + "Server>> Welcome Back " + user.getUserName() + "\n" + ANSI_RESET);
                                    }
                                } else {
                                    serverMessage += ("\n" + ANSI_RED + "Server>> You can not login to account you already logged in " + user.getUserName()
                                            + "\n" + ANSI_RED + "Enter 'help' or 'h' for a list of built-in commands.\n" + ANSI_RESET);
                                }
                                break;
                            case "03":
                                serverMessage = generate_new_quiz(input(clientMessage).substring(3));
                                break;
                            case "04":
                                serverMessage = new_MCQ(input(clientMessage).substring(3));
                                break;
                            case "05":
                                serverMessage = listquizzes(input(clientMessage).substring(3));
                                break;
                            case "06":
                                serverMessage = chooseQuiz(input(clientMessage).substring(3), outStream, inStream);
                                break;
                            case "07":
                                serverMessage = ANSI_RED + "Server>> Error: You should choice the quiz at first\n" + ANSI_RESET;
                                break;
                            default:
                                // if the client enterd wrong input
                                serverMessage = ANSI_RED + "Server>> Error: You Must Enter command in correct format. Enter 'help' or 'h' for a list of built-in commands.\n" + ANSI_RESET;
                                break;
                        }
                    } catch (Exception e) {
                        // if the client typed wrong foramt
                        serverMessage = ANSI_RED + "Server>> Error: You Must Enter command in correct format. Enter 'help' or 'h' for a list of built-in commands.\n" + ANSI_RESET;
                    }
                }
                // dispatching response to the client
                outStream.writeUTF(serverMessage);
                outStream.flush();
                // first time enter
                serverMessage = "";
                clientMessage = "";
            }
        } catch (Exception e) {
            // any exception occurs, just quit the program i.e. close connection
            //System.out.println(e.getMessage());
            System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: " + user.getUserID() + " Closed Connection!" + ANSI_RESET);
        }
    }

    private String chooseQuiz(String clientMessage, DataOutputStream outStream, DataInputStream inStream) throws FileNotFoundException, IOException {
        String serverMessage = "";
        if (user.getRole() != null && user.getRole().equalsIgnoreCase("student")) {
            String[] x = clientMessage.split("\\s*;\\s*");
            if (x.length == 1) {
                File quizes = new File("quizid.txt");
                Scanner sc = new Scanner(quizes);
                //System.out.println((int) x[0].trim().charAt(0) - 97);
                for (int i = 0; sc.hasNext(); i++) {
                    String[] temp = sc.nextLine().split("   ");
                    if (i == (int) x[0].trim().charAt(0) - 97) {
                        quizname = temp[0];
                        serverMessage += ANSI_CYAN + "----------------------------------------\n" + ANSI_RESET;
                        serverMessage += ANSI_CYAN + "Quiz name: " + (temp[0]) + "      Topic: " + temp[1] + "\n" + ANSI_RESET;
                        serverMessage += ANSI_CYAN + "----------------------------------------\n" + ANSI_RESET;
                    }
                }
                File MCQ = new File(quizname + ".txt");
                int score = 0;
                Scanner sc_MCQ = new Scanner(MCQ);
                while (sc_MCQ.hasNext()) {
                    String[] temp = sc_MCQ.nextLine().split("   ");
                    serverMessage += ANSI_CYAN + temp[0] + " : " + temp[1] + ", " + temp[2] + ", " + temp[3] + ", " + temp[4] + "\n" + ANSI_CYAN + "Your is answer: ";
                    // send the question
                    outStream.writeUTF(serverMessage);
                    outStream.flush();
                    serverMessage = "";
                    clientMessage = inStream.readUTF().trim();
//                    System.out.println(input(clientMessage).substring(3).trim());
//                    System.out.println(temp[Integer.parseInt(temp[5].trim())].trim());
                    if (input(clientMessage).substring(3).trim().equalsIgnoreCase(temp[Integer.parseInt(temp[5].trim())].trim())) {
                        score++;
                        serverMessage = ANSI_GREEN + "Correct\n";
                        outStream.writeUTF(serverMessage);
                        outStream.flush();
                    } else {
                        serverMessage = ANSI_RED + "not Correct\n";
                        outStream.writeUTF(serverMessage);
                        outStream.flush();
                    }
                }
                // last thing return, all questions done.
                serverMessage = ANSI_CYAN + "----------------------------------------\n" + ANSI_RESET;
                serverMessage += ANSI_CYAN + "Done. your score is " + score + "\n" + ANSI_RESET;
                System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                        + user.getUserID() + " completed quiz successfully and his/her score " + score + ANSI_RESET);
            } else {
                System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                        + user.getUserID() + " Enterd invalid format to answer the MCQ" + ANSI_RESET);
                serverMessage += ANSI_RED + "Server>> 07: Invalid format!\n" + ANSI_RESET;
            }
        } else {
            // Khalid TODO fix the bellow
            System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                    + user.getUserID() + " tried to answer the MCQ "
                    + (user.getRole() != null ? "when his/her role is " + user.getRole() : "without loged in to account") + ANSI_RESET);
            serverMessage = ANSI_RED + "Server>> 05: Insufficient permission\n" + ANSI_RESET;
        }
        return serverMessage;
    }

    public String listquizzes(String clientMessage) throws FileNotFoundException {
        String serverMessage = "";
        if (user.getRole() != null && user.getRole().equalsIgnoreCase("student")) {
            String[] x = clientMessage.split("\\s*;\\s*");
            if (x.length == 1) {
                try {
                    File quizes = new File("quizid.txt");
                    Scanner sc = new Scanner(quizes);
                    serverMessage += ANSI_CYAN + "05: ";
                    for (int i = 0; sc.hasNext(); i++) {
                        String[] temp = sc.nextLine().split("   ");
                        serverMessage += ANSI_CYAN + (char) (97 + i) + ") " + temp[0] + (sc.hasNext() ? " ; " : "");
                    }
                    serverMessage += "\n" + ANSI_RESET;
                    System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                            + user.getUserID() + " Listed all available quizzes successfully" + ANSI_RESET);
                } catch (Exception e) {
                    System.out.println(ANSI_RED + formatter.format(new Date()) + " User No: "
                            + user.getUserID() + " Faced error while view the available quizzes!" + ANSI_RESET);
                }
            } else {
                System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                        + user.getUserID() + " Enterd invalid format to list all quizes" + ANSI_RESET);
                serverMessage += ANSI_RED + "Server>> 05: Invalid format!\n" + ANSI_RESET;
            }
        } else {
            System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                    + user.getUserID() + " tried to view the available quizzes "
                    + (user.getRole() != null ? "when his/her role is " + user.getRole() : "without loged in to account") + ANSI_RESET);
            serverMessage = ANSI_RED + "Server>> 05: Insufficient permission\n" + ANSI_RESET;
        }
        return serverMessage;
    }

    public String new_MCQ(String clientMessage) throws FileNotFoundException {
        String serverMessage = "";
        if (user.getRole() != null && user.getRole().equalsIgnoreCase("teacher")) {
            if (!quizname.equals("")) {

                String[] x = clientMessage.split("\\s*;\\s*");
                if (x.length == 6) {
                    //String[] data = DB_Checker(quizname, "quizid.txt");
                    try {
                        //user.register(x[0], x[1], x[2]);
                        //serverMessage = user.getUserID() + "-" + user.getUserName() + "-" + user.getRole() + "-" + user.getPassword() + "\n";
                        Writer MCQ;
                        MCQ = new BufferedWriter(new FileWriter(quizname + ".txt", true));  //clears file every time
                        MCQ.append(x[0].trim() + "    " + x[1].trim() + "    " + x[2].trim() + "    " + x[3].trim() + "    " + x[4].trim() + "    " + x[5].trim() + "\n");
                        MCQ.flush();
                        MCQ.close();
                        System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                                + user.getUserID() + " generate a new quiz: " + x[0] + ANSI_RESET);
                        serverMessage += ANSI_CYAN + "Server>> 04: a MCQ inserted successfully\n" + ANSI_RESET;

                    } catch (Exception e) {
                        System.out.println(ANSI_RED + formatter.format(new Date()) + " User No: "
                                + user.getUserID() + " Faced error while inserting MCQ!" + ANSI_RESET);
                    }
                } else {
                    System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                            + user.getUserID() + " Enterd invalid format to insert MCQ" + ANSI_RESET);
                    serverMessage = ANSI_RED + "Server>> 04: Invalid format\n" + ANSI_RESET;
                }
            } else {
                System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                        + user.getUserID() + " Tried to insert MCQ without generate a quiz" + ANSI_RESET);
                serverMessage = ANSI_RED + "Server>> 04: You should generate a quiz at first\n" + ANSI_RESET;
            }
        } else {
            System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                    + user.getUserID() + " Tried to insert MCQ "
                    + (user.getRole() != null ? "when his/her role is " + user.getRole() : "without loged in to account") + ANSI_RESET);
            serverMessage = ANSI_RED + "Server>> 04: Insufficient permission\n" + ANSI_RESET;
        }
        return serverMessage;
    }

    public String generate_new_quiz(String clientMessage) throws FileNotFoundException {
        String serverMessage = "";
        if (user.getRole() != null && user.getRole().equalsIgnoreCase("teacher")) {
            String[] x = clientMessage.split("\\s*;\\s*");
            if (x.length == 2) {
                String[] data = DB_Checker(x[0].trim(), "quizid.txt");
                try {
                    //user.register(x[0], x[1], x[2]);
                    //serverMessage = user.getUserID() + "-" + user.getUserName() + "-" + user.getRole() + "-" + user.getPassword() + "\n";
                    if (data == null) {
                        File file = new File(x[0].trim() + ".txt");
                        if (file.createNewFile()) {
                            Writer quiz;
                            quiz = new BufferedWriter(new FileWriter("quizid.txt", true));  //clears file every time
                            quiz.append(x[0].trim() + "    " + x[1].trim() + "\n");
                            quiz.flush();
                            quiz.close();
                            quizname = x[0].trim();
                            System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                                    + user.getUserID() + " generate a new quiz: " + x[0].trim() + ANSI_RESET);
                            serverMessage += ANSI_CYAN + "Server>> 03: a new quiz is generated successfully\n" + ANSI_RESET;
                        }
                    } else {
                        System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                                + user.getUserID() + " tried to generate a new quiz which already avalible with same name " + x[0] + ANSI_RESET);
                        serverMessage += ANSI_RED + "Server>> 03: This quiz is already generated\n" + ANSI_RESET;
                    }
                } catch (Exception e) {
                    System.out.println(ANSI_RED + formatter.format(new Date()) + " User No: "
                            + user.getUserID() + " Faced error while generate a new quiz!" + ANSI_RESET);
                }
            } else {
                System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                        + user.getUserID() + " Enterd invalid format to generate a new quiz" + ANSI_RESET);
                serverMessage = ANSI_RED + "Server>> 03: Invalid format\n" + ANSI_RESET;
            }
        } else {
            System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                    + user.getUserID() + " tried to generate a new quiz "
                    + (user.getRole() != null ? "when his/her role is " + user.getRole() : "without loged in to account") + ANSI_RESET);
            serverMessage = ANSI_RED + "Server>> 03: Insufficient permission\n" + ANSI_RESET;
        }
        return serverMessage;
    }

    public String login(String clientMessage) throws FileNotFoundException {
        String serverMessage = "";
        String[] x = clientMessage.split("\\s*;\\s*");
        //System.out.println(data[0] + " + " + data[2] + "    " + x[0] + " + " + x[1]);
        if (x.length == 2) {
            String[] data = DB_Checker(x[0].trim(), "users.txt");
            try {
                if (data[0].equalsIgnoreCase(x[0].trim()) && data[2].equalsIgnoreCase(x[1].trim())) {
                    user.login(data[0], data[1], data[2]);
                    System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                            + user.getUserID() + " Login account successful" + ANSI_RESET);
                    serverMessage += ANSI_CYAN + "Server>> 02: login successful\n" + ANSI_RESET;
                } else {
                    System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                            + user.getUserID() + " Enterd incorrect password" + ANSI_RESET);
                    serverMessage += ANSI_RED + "Server>> 02: incorrect password!\n" + ANSI_RESET;
                }
            } catch (Exception e) {
                System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                        + user.getUserID() + " Enterd incorrect user name" + ANSI_RESET);
                serverMessage += ANSI_RED + "Server>> 02: incorrect user name!\n" + ANSI_RESET;
            }
        } else {
            System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                    + user.getUserID() + " Enterd invalid format" + ANSI_RESET);
            serverMessage += ANSI_RED + "Server>> 02: Invalid format!\n" + ANSI_RESET;
        }

        return serverMessage;
    }

    public String Register(String clientMessage) throws FileNotFoundException, IOException {
        String serverMessage = "";
        String[] x = clientMessage.split("\\s*;\\s*");

        String[] data = DB_Checker(x[0].trim(), "users.txt");
        if (x.length == 3) {
            try {
                if (x[1].trim().equalsIgnoreCase("student") || x[1].trim().equalsIgnoreCase("teacher")) {
                    //user.register(x[0], x[1], x[2]);
                    //serverMessage = user.getUserID() + "-" + user.getUserName() + "-" + user.getRole() + "-" + user.getPassword() + "\n";
                    if (data == null) {
                        Writer Users;
                        Users = new BufferedWriter(new FileWriter("users.txt", true));  //clears file every time
                        Users.append(x[0].trim() + "    " + x[1].trim() + "    " + x[2].trim() + "\n");
                        Users.flush();
                        Users.close();
                        System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                                + user.getUserID() + " Registr new account successful" + ANSI_RESET);
                        serverMessage += ANSI_CYAN + "Server>> 01: Registration successful\n" + ANSI_RESET;
                    } else {
                        System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                                + user.getUserID() + " tried to register " + x[0].trim() + " as a new account, which already registered" + ANSI_RESET);
                        serverMessage += ANSI_RED + "Server>> 01: This account \"" + x[0].trim() + "\" already registered\n" + ANSI_RESET;
                    }
                } else {
                    System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                            + user.getUserID() + " Enterd invalid format to regster" + ANSI_RESET);
                    serverMessage = ANSI_RED + "Server>> 01: Invalid format [Only Student/Teacher accepted as role]\n" + ANSI_RESET;
                }
            } catch (Exception e) {
                System.out.println(ANSI_RED + formatter.format(new Date()) + " User No: "
                        + user.getUserID() + " Faced error while registering a new account!" + ANSI_RESET);
            }
        } else {
            System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                    + user.getUserID() + " Enterd invalid format to regster" + ANSI_RESET);
            serverMessage = ANSI_RED + "Server>> 01: Invalid format\n" + ANSI_RESET;
        }
        return serverMessage;
    }

    public String input(String input) {
        String output = "";
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) != '#') {
                output += (input.charAt(i));
            } else {
                return output;
            }
        }
        return null;
    }

    public String[] DB_Checker(String lookingfor, String filename) throws FileNotFoundException {
        File users = new File(filename);
        Scanner sc = new Scanner(users);

        while (sc.hasNext()) {
            String[] split = sc.nextLine().split("    ");
            if (split[0].equals(lookingfor)) {
                //System.out.println(split[0] + "   " + username);
                //System.out.println(split[1] + "   " + split[2]);
                return split;
            }
        }
        return null;
    }

    public String help(String serverMessage) {
        System.out.println(ANSI_CYAN + formatter.format(new Date()) + " User No: "
                + user.getUserID() + " Asked for help" + ANSI_RESET);
        if (user.getUserName() == null) {
            serverMessage = ANSI_CYAN + "Server>> " + ANSI_YELLOW + "Help: At first you should to login/register by sending the command in the following format\n\t"
                    + ANSI_YELLOW + "For register enter:\n\t"
                    + ANSI_YELLOW + "01: username ; role ; password #\n\t"
                    + ANSI_YELLOW + "For login enter:\n\t"
                    + ANSI_YELLOW + "02: user ; password #\n" + ANSI_RESET;
        } else if (user.getRole().equalsIgnoreCase("teacher")) {
            serverMessage = ANSI_CYAN + "Server>> " + ANSI_YELLOW + "Help: You Must sending the command in the following format\n\t"
                    + ANSI_YELLOW + "For generate a new quiz enter:\n\t"
                    + ANSI_YELLOW + "03: quizname; topic #\n\t"
                    + ANSI_YELLOW + "For create a MCQ enter:\n\t"
                    + ANSI_YELLOW + "04: question; option1 ; option 2 ; option 3; option 4 ; correctchoice number #\n\t" + ANSI_RESET;
            serverMessage += ANSI_YELLOW + "For logout enter:\n\t"
                    + ANSI_YELLOW + "08: logout #\n" + ANSI_RESET;
        } else if (user.getRole().equalsIgnoreCase("student")) {
            serverMessage = ANSI_CYAN + "Server>> " + ANSI_YELLOW + "Help: You Must sending the command in the following format\n\t"
                    + ANSI_YELLOW + "For view the available quizzes enter:\n\t"
                    + ANSI_YELLOW + "05: listquizzes #\n\t"
                    + ANSI_YELLOW + "For select the quiz after last command enter:\n\t"
                    + ANSI_YELLOW + "06: a#\n\t"
                    + ANSI_YELLOW + "For select the correct answer after last command enter:\n\t"
                    + ANSI_YELLOW + "07: option 1 #\n\t" + ANSI_RESET;
            serverMessage += ANSI_YELLOW + "For logout enter:\n\t"
                    + ANSI_YELLOW + "08: logout #\n" + ANSI_RESET;
        }
        return serverMessage;
    }

}
