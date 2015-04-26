//package src.danweberrpg;
package slicendice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Server{
        /**
     * The set of all names of clients in the chat room.  Maintained
     * so that we can check that new clients are not registering name
     * already in use.
     */
    private HashSet<String> names = new HashSet<String>();

    /**
     * The set of all the print writers for all the clients.  This
     * set is kept so we can easily broadcast messages.
     */
    private HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    JFrame frame = new JFrame("Server");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
    private String textLog = "MESSAGE ";
    private int playerDataRecieved = 0;
    private String dataOutLine = "SQUARE ";
    private ArrayList<Handler> handlers = new ArrayList<Handler>();

    public Server(){
        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void runServer() {
        System.out.println("The chat server is running.");
        
        //Print possible adresses
        try {
            System.out.println(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {System.out.println(e);}
        Enumeration en = null;
        try{
            en = NetworkInterface.getNetworkInterfaces();
        }catch (SocketException e){System.out.println(e);}
        while(en.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) en.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = (InetAddress) ee.nextElement();
                System.out.println(i.getHostAddress());
            }
        }
        
        //new socket
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(Main.PORT);
        } catch (IOException e) {System.out.println(e);}
        
        //everytime new socket recieved, spawn new handler
        try {
            new Sender().start();
            while (true) {
                try {
                    handlers.add(new Handler(listener.accept()));
                    handlers.get(handlers.size()-1).start();
                } catch (IOException e) {System.out.println(e);}
            }
        } finally {
            try {
                listener.close();
            } catch (IOException e) {System.out.println(e);}
        }
    }

    //send stickfigure data to all clients
    private class Sender extends Thread{
        public Sender(){}
        
        public void run(){
            while(true){
                try {
                    Thread.sleep(0, 500);
                } catch (InterruptedException e) {System.out.println(e);}
                if(names.size() > 0 && playerDataRecieved >= names.size() && dataOutLine.split("~n`").length >= names.size()){
                    for (PrintWriter writer : writers) {
                        writer.println(dataOutLine);
                    }  
                    playerDataRecieved = 0;
                    dataOutLine = "SQUARE ";
                }
            }
        }
    }
    
    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
    private class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader handlerIn;
        private PrintWriter handlerOut;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
        public void run() {
            try {

                // Create character streams for the socket.
                handlerIn = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                handlerOut = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while (true) {
                    handlerOut.println("SUBMITNAME");
                    name = handlerIn.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        //this needs work
                        if (!names.contains(name) && !name.contains("~n`")) {
                            names.add(name);
                            break;
                        }
                    }
                }

                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                handlerOut.println("NAMEACCEPTED");
                writers.add(handlerOut);
                handlerOut.println(textLog);

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = handlerIn.readLine();
//                    System.out.println(input);
                    if (input == null) {
                        return;
                    }
                    if(input.startsWith("SQUARE")){
                        if(!dataOutLine.contains(name)){
                            dataOutLine+=input.substring(7)+"~n`";
                            playerDataRecieved++;                    
                        }
                    }
                    else if(input.startsWith("GAMEOVER")){
                        textLog+= input.substring(9)+"~n`";
                        for (PrintWriter writer : writers) {
                            writer.println("NEWGAME"+textLog);
                        }  
                        messageArea.setText(Main.decodeLine(textLog.substring(8)));
                    }
                    else{
                        textLog+= name + ": " + input+"~n`";
                        //send message recieved to every client
                        for (PrintWriter writer : writers) {
                            writer.println(textLog);
                        }  
                        messageArea.setText(Main.decodeLine(textLog.substring(8)));
                    }
                }
            } catch (IOException e) {System.out.println(e);} 
            finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) {
                    names.remove(name);
                }
                if (handlerOut != null) {
                    writers.remove(handlerOut);
                }
                handlers.remove(this);
                try {
                    socket.close();
                } catch (IOException e) {System.out.println(e);}
            }
        }
    }
}