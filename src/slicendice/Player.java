package slicendice;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.util.Random;

public class Player {
    
    public int xPos;
    public int yPos;
    public double doubleXPos;
    public double doubleYPos;
    public String playerName;
    public boolean isAlive = true;
    public boolean wonGame = false;
    
    private int direction;
    private int viewWidth;
    private int viewHeight;
    private boolean mapCentered;
    private Point stabPoint;
    private Color c;
    private int nameWidth;
    private Polygon triangle;
    private Font font;
    private int fontSize = Main.circleRad;
    private Random r = new Random();
    private int health = Main.initHealth;
    private int speedMult = 2;
    private int dirnMult = 2;
    private int[] pathDeltaX;
    private int[] pathDeltaY;
    private int pathOn = 0;
    private int pathDirn = 0;
    private int totalDeltaX = 0;
    private int totalDeltaY = 0;
    
    public Player(int x, int y, String n, boolean m){
        xPos = x;
        yPos = y;
        doubleXPos = x;
        doubleYPos = y;
        playerName = n;
        direction = 0;
        viewWidth = Main.screenWidth;
        viewHeight = Main.screenHeight;
        mapCentered = m;
        c = new Color(r.nextInt(150)+100,r.nextInt(250),r.nextInt(150)+100);
        triangle = new Polygon();
        triangle.addPoint(xPos-Main.circleRad, yPos);
        triangle.addPoint(xPos, yPos-Main.triangleHeight);
        triangle.addPoint(xPos+Main.circleRad, yPos);
        stabPoint = new Point(xPos, yPos-Main.triangleHeight);
        nameWidth = -1;
        font = new Font("Arial",Font.BOLD,fontSize);
    }
    
    public Player(String[] s, int ox, int oy){
        mapCentered = false; 
        playerName = s[0];
        xPos = Integer.parseInt(s[1]);
        yPos = Integer.parseInt(s[2]);
        direction = Integer.parseInt(s[3]);
        int otherOX = Integer.parseInt(s[4]);
        int otherOY = Integer.parseInt(s[5]);
        xPos = (xPos+otherOX)-ox;
        yPos = (yPos+otherOY)-oy;
        doubleXPos = xPos;
        doubleYPos = yPos;
        triangle = new Polygon();
        triangle.addPoint(xPos-Main.circleRad, yPos);
        triangle.addPoint(xPos, yPos-Main.triangleHeight);
        triangle.addPoint(xPos+Main.circleRad, yPos);
        stabPoint = new Point(xPos, yPos-Main.triangleHeight);
        c = new Color(Integer.parseInt(s[6]),Integer.parseInt(s[7]),Integer.parseInt(s[8]));
        changePosition(0, 0, true);
        nameWidth = -1;
        font = new Font("Arial",Font.BOLD,fontSize);
        health = Integer.parseInt(s[9]);
        gotHit(0);
    }
    
    public void playerInteractions(Player other){
        if(rectIntersect(xPos-Main.triangleHeight, yPos-Main.triangleHeight, Main.triangleHeight*2, Main.triangleHeight*2, other.xPos-Main.triangleHeight, other.yPos-Main.triangleHeight, Main.triangleHeight*2, Main.triangleHeight*2)){
            boolean stabPointInt = false;
            boolean circlesTouch = false;
            if(distSquared(xPos, yPos, other.xPos, other.yPos)<=Main.circleRad*Main.circleRad*4)
                circlesTouch = true;
            if(distSquared(other.stabPoint.x, other.stabPoint.y, xPos, yPos)<=Main.circleRad*Main.circleRad)
                stabPointInt = true;
            if(stabPointInt || circlesTouch){
                if(stabPointInt){
                    changePosition((other.stabPoint.x-other.xPos)*2, (other.stabPoint.y-other.yPos)*2, true);
                    gotHit(Main.hitDamage);
                    //System.out.println("Hit at "+xPos+","+yPos+","+other.xPos+","+other.yPos+",");
                }
                else{
                    double cVal = Math.sqrt(distSquared(xPos, yPos, other.xPos, other.yPos));
                    double zVal = Math.sqrt(Main.circleRad*Main.circleRad*4);
                    int xSep = (int) ((((Math.abs(xPos-other.xPos)*zVal)/cVal)-Math.abs(xPos-other.xPos))/2);
                    int ySep = (int) ((((Math.abs(yPos-other.yPos)*zVal)/cVal)-Math.abs(yPos-other.yPos))/2);
                    if(xPos>other.xPos){
                        changePosition(xSep, 0, true);
                        other.changePosition(-xSep, 0, true);
                    }
                    else{
                        changePosition(-xSep, 0, true);
                        other.changePosition(xSep, 0, true);
                    }
                    if(yPos>other.yPos){
                        changePosition(0, ySep, true);
                        other.changePosition(0, -ySep, true);
                    }
                    else{
                        changePosition(0, -ySep, true);
                        other.changePosition(0, ySep, true);
                    }
                }
            }
        }
    }
    
    public void gotHit(int d){
        health-=d;
        if(health <= 0){
            isAlive = false;
            c = Color.GRAY;
        }
    }
    
    public void newGame(){
        health = Main.initHealth;
        isAlive = true;
        c = new Color(r.nextInt(100)+150,r.nextInt(100)+150,r.nextInt(100)+150);
    }
    
    public boolean rectIntersect(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2){
        return (x1<=x2+w2 && x1+w1>=x2 && y1<=y2+h2 && y1+h1>=y2);
    }
    
    public int distSquared(int x1, int y1, int x2, int y2){
        return (x1-x2)*(x1-x2)+(y1-y2)*(y1-y2);
    }
    
    public void drawCharacter(Graphics g){
        triangle = new Polygon();
        triangle.addPoint(xPos-Main.circleRad, yPos);
        triangle.addPoint(xPos, yPos-Main.triangleHeight);
        triangle.addPoint(xPos+Main.circleRad, yPos);
        if(fontSize != Main.circleRad){
            fontSize = Main.circleRad;
            font = new Font("Arial",Font.BOLD,fontSize);
            nameWidth = -1;
        }
        
        Graphics2D g2d = (Graphics2D)g;
        AffineTransform old = g2d.getTransform();
        g2d.rotate(Math.toRadians(direction), xPos, yPos);
        
        g.setColor(c);
        g.fillOval(xPos-Main.circleRad, yPos-Main.circleRad, Main.circleRad*2, Main.circleRad*2);
        
        g.fillPolygon(triangle);
        
        g2d.setTransform(old);
        
        //player name
        g.setFont(font);
        if(nameWidth == -1)
            nameWidth = (int) g.getFontMetrics().getStringBounds(playerName, g).getWidth();
        
        g.setColor(Color.WHITE);
        g.fillRect(xPos-nameWidth/2, yPos-(Main.circleRad*2+Main.circleRad/2+fontSize), nameWidth, (int) (fontSize*1.2));
                
        g.setColor(Color.BLACK);
        g.drawString(playerName, xPos-nameWidth/2, yPos-(Main.circleRad*2+Main.circleRad/2));
        
        //Health bar
        g.setColor(Color.BLACK);
        g.fillRect(xPos-Main.circleRad, yPos-Main.healthBarWidth/2, Main.circleRad*2, Main.healthBarWidth);
        
        g.setColor(new Color(255-Main.map(health,0,Main.initHealth,0,255), Main.map(health,0,Main.initHealth,0,255), 0));
        g.fillRect(xPos-Main.circleRad, yPos-(Main.healthBarWidth/2-2), (int) (((double)(health)/Main.initHealth)*Main.circleRad*2), Main.healthBarWidth-4);
        
        //g.fillOval(stabPoint.x, stabPoint.y, 4, 4);
        
    }
    
    //No Path
    public void updatePosition(boolean lk, boolean rk, boolean wk, boolean sk, boolean ak, boolean dk){
        if(isAlive){
            if(wk)
                speedMult = 4;
            if(sk)
                speedMult = 1;
            if((sk && wk) || (!sk && !wk))
                speedMult = 2;
            if(dk)
                dirnMult = 4;
            if(ak)
                dirnMult = 1;
            if((ak && dk) || (!ak && !dk))
                dirnMult = 2;
            if(lk)
                direction+=Main.dirSlowSpeed*dirnMult;
            if(rk)
                direction-=Main.dirSlowSpeed*dirnMult;
            changePosition(Math.cos(Math.toRadians(direction-90))*Main.moveSlowSpeed*speedMult, Math.sin(Math.toRadians(direction-90))*Main.moveSlowSpeed*speedMult, true);
        }
    }
    
    //Has Path
    public void updatePosition(){
        if(isAlive){
            direction = (int) Math.toDegrees(Math.atan2(pathDeltaX[pathOn], -pathDeltaY[pathOn]));
            updatePosition(false,false,false,false,false,false);
            if(Math.abs(totalDeltaX)>=Math.abs(pathDeltaX[pathOn]) && Math.abs(totalDeltaY)>=Math.abs(pathDeltaY[pathOn])){
                changePosition(pathDeltaX[pathOn]-totalDeltaX, pathDeltaY[pathOn]-totalDeltaY, true);
                pathOn++;
                if(pathOn>=pathDeltaX.length)
                    pathOn = 0;
                totalDeltaX = 0;
                totalDeltaY = 0;
            }
        }
    }
    
    public void setPath(int[] xp, int[] yp){
        pathDeltaX = new int[xp.length];
        System.arraycopy(xp, 0, pathDeltaX, 0, xp.length);
        pathDeltaY = new int[yp.length];
        System.arraycopy(yp, 0, pathDeltaY, 0, yp.length);
    }
    
    public void setPosistion(int x, int y, boolean pc){
        changePosition(x-xPos, y-yPos, pc);
    }
    
    public void changePosition(double cx, double cy, boolean pc){
        doubleXPos+=cx;
        doubleYPos+=cy;
        int actualCX = ((int)doubleXPos) - xPos;
        int actualCY = ((int)doubleYPos) - yPos;
        
        triangle.translate(actualCX, actualCY);
        xPos+=actualCX;
        yPos+=actualCY;
        if(pc && !mapCentered){
            totalDeltaX+=actualCX;
            totalDeltaY+=actualCY;
        }
        stabPoint.move((int) (xPos+Main.triangleHeight*Math.cos(Math.toRadians(direction-90))), (int) (yPos+Main.triangleHeight*Math.sin(Math.toRadians(direction-90))));
        if(mapCentered){
            if(xPos<Main.circleRad){
                setPosistion(Main.circleRad, yPos, pc);
            }
            if(xPos>viewWidth-Main.circleRad)
                setPosistion(viewWidth-Main.circleRad, yPos, pc);
            if(yPos<Main.circleRad)
                setPosistion(xPos, Main.circleRad, pc);
            if(yPos>viewHeight-Main.circleRad)
                setPosistion(xPos, viewHeight-Main.circleRad, pc);
        }
    }
    
    public void changeName(String s){
        playerName = s;
        nameWidth = -1;
    }
    
    public String getInfoString(int ox, int oy){
        return playerName+","+xPos+","+yPos+","+direction+","+ox+","+oy+","+c.getRed()+","+c.getGreen()+","+c.getBlue()+","+health;
    }
}
