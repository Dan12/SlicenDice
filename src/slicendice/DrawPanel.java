package slicendice;

//Runnable JPanel Class

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class DrawPanel extends JPanel implements Runnable,KeyListener{
        
    private boolean lAK = false;
    private boolean rAK = false;
    private boolean wAK = false;
    private boolean sAK = false;
    private boolean aAK = false;
    private boolean dAK = false;
    
    public TiledMap map;
    
    public ArrayList<Player> players;
    
    private ArrayList<Player> AIs;
    
    public Player character;
    
    private long duration = 0;
    
    public boolean settingSharedObject = false;
    public boolean drawingSharedObject = false;
    
    public String clientName;
    
    private int sleepTime = 10;
    
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
    JScrollPane scrollArea;

    public DrawPanel(int w, int h){
        super.setOpaque(true);
        super.setPreferredSize(new Dimension(w, h));
        super.setBackground(new Color(225, 225, 225));
        addKeyListener(this);
        super.setFocusable(true);
        super.setLayout(null);
        
        textField.setEditable(false);
        messageArea.setEditable(false);
        messageArea.setFocusable(false);
        // Add Listeners
        textField.addActionListener(new ActionListener() {
            //textfield enter key pressed
            public void actionPerformed(ActionEvent e) {
                Client.clientOut.println(textField.getText());
                System.out.println(textField.getText());
                textField.setText("");
                DrawPanel.this.requestFocusInWindow();
            }
        });
        
        super.addMouseListener(new MouseListener()
        {
            @Override
            public void mousePressed(MouseEvent e){
                DrawPanel.this.requestFocusInWindow();
            }
            
            @Override
            public void mouseClicked(MouseEvent e){}
            @Override
            public void mouseReleased(MouseEvent e){}
            @Override
            public void mouseEntered(MouseEvent e){}
            @Override
            public void mouseExited(MouseEvent e){}
        });
        
        scrollArea = new JScrollPane(messageArea);
        scrollArea.setFocusable(false);
        textField.setBounds(Main.screenWidth, 0, Main.screenPlusMessage-Main.screenWidth, 30);
        scrollArea.setBounds(Main.screenWidth+5, 30, Main.screenPlusMessage-Main.screenWidth-10, Main.screenHeight-30);
        
        super.add(scrollArea);
        super.add(textField);
        
        String filePath = new File("").getAbsolutePath();
        //map = new TiledMap(filePath.concat("/src/src/res"), "desert-tiled.txt");
        map = new TiledMap(filePath.concat("/src/res"), "test.txt");

        character = new Player(200, 200, "-", true);
        players = new ArrayList<Player>();

        AIs = new ArrayList<Player>();
        Player temp = new Player(400, 600, "AI 1", false);
        temp.setPath(new int[]{400,0,-400,0}, new int[]{0,400,0,-400});
        //AIs.add(temp);
    }


    @Override
    public void run(){
        while(true){
            try {
              if(duration < sleepTime)
                  Thread.sleep(sleepTime-duration);
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
            long start = System.nanoTime();
            while(settingSharedObject){System.out.println("Waiting in run");}
            super.repaint();
            long end = System.nanoTime();
            duration = ((end-start)/1000000);
//            System.out.println("Duration: "+duration);
        }
    }

    public void actions(){
        character.updatePosition(lAK, rAK, wAK, sAK, aAK, dAK);
        
        for(Player p : AIs)
            p.updatePosition();

        if((character.xPos>=Main.screenWidth/2 && map.mapPixelWidth-map.offsetX>Main.screenWidth) || (character.xPos<=Main.screenWidth/2 && map.offsetX>0)){
            int mapOffXInit = map.offsetX;
            map.changeOffset(character.xPos-Main.screenWidth/2, 0);
            character.setPosistion(Main.screenWidth/2, character.yPos, false);   
            for (Player AI : AIs) 
                AI.changePosition(mapOffXInit-map.offsetX, 0, false);
            for (Player player : players)
                player.changePosition(mapOffXInit-map.offsetX, 0, false);
        }   
        if((character.yPos>=Main.screenHeight/2 && map.mapPixelHeight-map.offsetY>Main.screenHeight) || (character.yPos<=Main.screenHeight/2 && map.offsetY>0)){
            int mapOffYInit = map.offsetY;
            map.changeOffset(0, character.yPos-Main.screenHeight/2);
            character.setPosistion(character.xPos, Main.screenHeight/2, false);   
            for (Player AI : AIs)
                AI.changePosition(0, mapOffYInit-map.offsetY, false);
            for (Player player : players)
                player.changePosition(0, mapOffYInit-map.offsetY, false);
        }
    }

    // Perform any custom painting (if necessary) in this method
    @Override  
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        map.drawMap(g);

        while(settingSharedObject){System.out.println("Waiting in Paint");}
        drawingSharedObject = true;
        for (Player player : players) {
            if (!player.playerName.equals(clientName)) {
                player.drawCharacter(g);
            }
        }

        for (Player AI : AIs)
            AI.drawCharacter(g);

        character.drawCharacter(g);

        g.setColor(Color.BLACK);
        g.fillRect(Main.screenWidth, 0, Main.screenPlusMessage-Main.screenWidth, Main.screenHeight);

        boolean onlyOneLeft = true;
        
        for(Player AI : AIs){
            if(AI.isAlive && character.isAlive){
                character.playerInteractions(AI);
                AI.playerInteractions(character);
            }
        }

        for (Player player : players) {
            if (!player.playerName.equals(clientName) && player.isAlive) {
                onlyOneLeft = false;
            }
            if (character.isAlive && player.isAlive && !player.playerName.equals(clientName)) {
                character.playerInteractions(player);
                if (!character.isAlive) {
                    Client.clientOut.println(character.playerName+" slain by " + player.playerName);
                }
            }
        }
        
        actions();
        drawingSharedObject = false;

        if(!onlyOneLeft)
            character.wonGame = false;

        if(onlyOneLeft && players.size() > 1 && !character.wonGame){
            Client.clientOut.println("GAMEOVER "+character.playerName+" won the match");
            character.wonGame = true;
        }

        if(clientName != null )
          Client.clientOut.println("SQUARE "+character.getInfoString(map.offsetX, map.offsetY));
    }

    @Override
    public void keyTyped(KeyEvent e){}

    @Override
    public void keyPressed(KeyEvent e){
        System.out.println(e.getKeyCode());
        if(e.getKeyCode() == 39)
            lAK = true;
        if(e.getKeyCode() == 37)
            rAK = true;
        if(e.getKeyCode() == 87)
            wAK = true;
        if(e.getKeyCode() == 83)
            sAK = true;
        if(e.getKeyCode() == 65)
            aAK = true;
        if(e.getKeyCode() == 68)
            dAK = true;
    }

    @Override
    public void keyReleased(KeyEvent e){
        if(e.getKeyCode() == 39)
            lAK = false;
        if(e.getKeyCode() == 37)
            rAK = false;
        if(e.getKeyCode() == 87)
            wAK = false;
        if(e.getKeyCode() == 83)
            sAK = false;
        if(e.getKeyCode() == 65)
            aAK = false;
        if(e.getKeyCode() == 68)
            dAK = false;
    }
}
