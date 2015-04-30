package slicendice;

import javax.swing.JOptionPane;

public class Main
{
  // Declare instance variables here...
    public static int PORT = 9001;
  
    public static int circleRad = 30;
    public static int triangleHeight = 70;
    public static int dirSlowSpeed = 3;
    public static int moveSlowSpeed = 3;
    public static int initHealth = 80;
    public static int healthBarWidth = 8;
    public static int hitDamage = 10;

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
    
    public static int map(int x, int in_min, int in_max, int out_min, int out_max){
        int ret = (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
        if(ret > out_max)
            return out_max;
        if(ret < out_min)
            return out_min;
        return ret;
    }
   
    public static String decodeLine(String inLines){
        String temp = "";
        String[] lineSplit = inLines.split("~n`");
        if(inLines.startsWith("SQUARE")){
            temp = "SQUARE ";
            String subbed = lineSplit[0].substring(7);
            String[] globalVars = subbed.split(",");
            Main.circleRad = Integer.parseInt(globalVars[0]);
            Main.triangleHeight = Integer.parseInt(globalVars[1]);
            Main.dirSlowSpeed = Integer.parseInt(globalVars[2]);
            Main.moveSlowSpeed = Integer.parseInt(globalVars[3]);
            Main.initHealth = Integer.parseInt(globalVars[4]);
            Main.healthBarWidth = Integer.parseInt(globalVars[5]);
            Main.hitDamage = Integer.parseInt(globalVars[6]);
            for(int i = 1; i < lineSplit.length; i++)
                temp+=lineSplit[i]+"\n";
            return temp;
        }
        else{
            for(int i = 0; i < lineSplit.length; i++)
                temp+=lineSplit[i]+"\n";
            return temp;
        }
    }
}
