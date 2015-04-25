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
        c = new Color(r.nextInt(100)+150,r.nextInt(100)+150,r.nextInt(100)+150);
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
    }
    
    public void playerInteractions(Player other){
        if(rectIntersect(xPos-Main.circleRad, yPos-Main.triangleHeight, Main.circleRad*2, Main.circleRad+Main.triangleHeight, other.xPos-Main.circleRad, other.yPos-Main.triangleHeight, Main.circleRad*2, Main.circleRad+Main.triangleHeight)){
            if(distSquared(xPos, yPos, other.xPos, other.yPos)<=Main.circleRad*Main.circleRad*4){
                
            }
        }
    }
    
    public boolean rectIntersect(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2){
        return (x1<=x2+w2 && x1+w1>=x2 && y1<=y2+h2 && y1+h1>=y2);
    }
    
    public int distSquared(int x1, int y1, int x2, int y2){
        return (x1-x2)*(x1-x2)+(y1-y2)*(y1-y2);
    }
    
    public void drawCharacter(Graphics g){
        Graphics2D g2d = (Graphics2D)g;
        AffineTransform old = g2d.getTransform();
        g2d.rotate(Math.toRadians(direction), xPos, yPos);
        
        g.setColor(c);
        g.fillOval(xPos-Main.circleRad, yPos-Main.circleRad, Main.circleRad*2, Main.circleRad*2);
        
        g.fillPolygon(triangle);
        
        g2d.setTransform(old);
        
        g.setFont(font);
        if(nameWidth == -1)
            nameWidth = (int) g.getFontMetrics().getStringBounds(playerName, g).getWidth();
        
        g.setColor(Color.WHITE);
        g.fillRect(xPos-nameWidth/2, yPos-(Main.circleRad*2+Main.circleRad/2+fontSize), nameWidth, (int) (fontSize*1.2));
                
        g.setColor(Color.BLACK);
        g.drawString(playerName, xPos-nameWidth/2, yPos-(Main.circleRad*2+Main.circleRad/2));
        
    }
    
    //No Path
    public void updatePosition(boolean lk, boolean rk, boolean uk, boolean dk, boolean ct){
        if(lk){
            if(ct)
                direction+=Main.controlDirSpeed;
            else
                direction+=Main.dirSpeed;
        }
        if(rk){
            if(ct)
                direction-=Main.controlDirSpeed;
            else
                direction-=Main.dirSpeed;
        }
        if(uk){
            if(ct)
                changePosition(Math.cos(Math.toRadians(direction-90))*Main.controlMoveSpeed, Math.sin(Math.toRadians(direction-90))*Main.controlMoveSpeed);
            else
                changePosition(Math.cos(Math.toRadians(direction-90))*Main.moveSpeed, Math.sin(Math.toRadians(direction-90))*Main.moveSpeed); 
        }
        if(dk){
            if(ct)
                changePosition(-Math.cos(Math.toRadians(direction-90))*Main.controlMoveSpeed, -Math.sin(Math.toRadians(direction-90))*Main.controlMoveSpeed);
            else
                changePosition(-Math.cos(Math.toRadians(direction-90))*Main.moveSpeed, -Math.sin(Math.toRadians(direction-90))*Main.moveSpeed); 
        }
    }
    
    //Has Path
    public void updatePosition(){
    
    }
    
    public void setPath(int[] xp, int[] yp){
    
    }
    
    public void setPosistion(int x, int y){
        changePosition(x-xPos, y-yPos);
    }
    
    public void changePosition(double cx, double cy){
        doubleXPos+=cx;
        doubleYPos+=cy;
        int actualCX = ((int)doubleXPos) - xPos;
        int actualCY = ((int)doubleYPos) - yPos;
        
        triangle.translate(actualCX, actualCY);
        xPos+=actualCX;
        yPos+=actualCY;
        if(mapCentered){
            if(xPos<0){
                System.out.println("Hey");
                setPosistion(0, yPos);
            }
            if(xPos>viewWidth)
                setPosistion(viewWidth, yPos);
            if(yPos<0)
                setPosistion(xPos, 0);
            if(yPos>viewHeight)
                setPosistion(xPos, viewHeight);
        }
    }
    
    public void changeName(String s){
        playerName = s;
        nameWidth = -1;
    }
    
    public String getInfoString(int ox, int oy){
        return playerName+","+xPos+","+yPos+","+direction+","+ox+","+oy;
    }
}
