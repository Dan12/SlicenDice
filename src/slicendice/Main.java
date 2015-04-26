package slicendice;

import javax.swing.JOptionPane;

public class Main
{
  // Declare instance variables here...
    public static int PORT = 9001;
  
    public static int circleRad = 30;
    public static int triangleHeight = 70;
    public static int dirSlowSpeed = 4;
    public static int moveSlowSpeed = 5;
    public static int initHealth = 100;
    public static int healthBarWidth = 8;

    long duration = 0;
    
    public static int screenPlusMessage = 1400;
    public static int screenWidth = 1100;
    public static int screenHeight = 800;
    
    public static void main(String[] args){
        Main m = new Main();
        m.Start();
    }
    
    public Main(){}
    
    public void Start(){
        int result = JOptionPane.showOptionDialog(null, "Is this the Server", "Server?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.NO_OPTION);
        //No-1,Yes-0
        System.out.println(result);
        if(result == 0){
            Server server = new Server();
            server.runServer();
        }
        else{
            Client client = new Client();
            client.runClient();
        }
    }
   
    public static String decodeLine(String inLines){
        String temp = "";
        String[] lineSplit = inLines.split("~n`");
        for(int i = 0; i < lineSplit.length; i++)
            temp+=lineSplit[i]+"\n";
        return temp;
    }
}
