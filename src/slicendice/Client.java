package slicendice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

//Client, connects to server and displays messages in textBox
public class Client{
    //Client variables
    private BufferedReader clientIn;
    public static PrintWriter clientOut;
    JFrame frame = new JFrame("Client");
    String text = "";
    DrawPanel drawPanel;
    String tempName = "";
    String autoIpAdress = "10.3.11.102";

    public Client(){
        // Layout GUI

        drawPanel = new DrawPanel(Main.screenPlusMessage,Main.screenHeight);
        
        frame.setContentPane(drawPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(10, 10);
        frame.setVisible(true);
        frame.pack();
    }

    public void runClient() {
        ClientThread ct = new ClientThread();
        ExecutorService ex = Executors.newCachedThreadPool();
        ex.execute(ct);
        ex.execute(drawPanel);

        ex.shutdown();
    }

    //Runnable client class to constantly get data from server
    private class ClientThread implements Runnable{

        public ClientThread(){}

        @Override
        public void run() {
            // Make connection and initialize streams
            String serverAddress = getServerAddress();
            Socket socket = null;
            try {
                socket = new Socket(serverAddress, Main.PORT);
            } catch (IOException e) {System.out.println(e);}
            try {
                //recieves messages from server
                clientIn = new BufferedReader(
                    new InputStreamReader(
                        socket.getInputStream()
                    )
                );
            } catch (IOException e) {System.out.println(e);}
            try {
                //sends messages to server
                clientOut = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {System.out.println(e);}

            // Process all messages from server, according to the protocol.
            while (true) {
                String allLines = "";
                try {
                    allLines = clientIn.readLine();
                } catch (IOException e) {System.out.println(e);}
//                System.out.println(allLines);
                if(allLines == null) continue;
                allLines = Main.decodeLine(allLines);
                if (allLines.startsWith("SUBMITNAME")) {
                    tempName = getName();
                    clientOut.println(tempName);
                } else if (allLines.startsWith("NAMEACCEPTED")) {
                    drawPanel.clientName = tempName;
                    System.out.println("name:"+drawPanel.clientName);
                    drawPanel.character.changeName(drawPanel.clientName);
                    drawPanel.textField.setEditable(true);
                } else if (allLines.startsWith("MESSAGE")) {
                    drawPanel.messageArea.setText(allLines.substring(8));
                    //text = allLines.substring(8);
                    //System.out.println(text);
                }
                else if (allLines.startsWith("SQUARE")){
                    if(!drawPanel.drawingSharedObject){
                        drawPanel.settingSharedObject = true;
                        drawPanel.players.clear();
                        String actualText = allLines.substring(7);   
                        String[] squareInfos = actualText.split("\n");
                        for(int i = 0; i < squareInfos.length; i++){
                            String[] parameters = squareInfos[i].split(",");
                            drawPanel.players.add(new Player(parameters, drawPanel.map.offsetX, drawPanel.map.offsetY));
                        }
                        drawPanel.settingSharedObject = false;
                        drawPanel.drawingSharedObject = true;
                    }
                }
                else if (allLines.startsWith("NEWGAME")){
                    drawPanel.character.newGame();
                    drawPanel.messageArea.setText(allLines.substring(15));
                }
            }
        }
        
        //prompt for ip adress and user name
        private String getServerAddress() {
            if(autoIpAdress.equals(""))
                return JOptionPane.showInputDialog(null,"Enter IP Address of the Server:","Welcome to the Chatter",JOptionPane.QUESTION_MESSAGE);
            else
                return autoIpAdress;
                
        }

        //prompt for name
        private String getName() {
            return JOptionPane.showInputDialog(null,"Choose a screen name:","Screen name selection",JOptionPane.PLAIN_MESSAGE);
        }

    }
}