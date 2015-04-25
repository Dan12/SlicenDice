package slicendice;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class TiledMap {
    private String imageLocation;
    private BufferedImage tiledImage;
    private BufferedImage[][] tilesImages;
    private int tileWidth;
    private int tileHeight;
    private int rows;
    private int cols;
    private int mapTileWidth;
    private int mapTileHeight;
    private Tile[][] tiles;
    int offsetX;
    int offsetY;
    private int tilePadding;
    private int tileScale;
    int mapPixelWidth;
    int mapPixelHeight;
    
    public TiledMap(String fileLocation, String textName){
        File inFile = new File(fileLocation+"/"+textName);
        
        Scanner inLine = null;
        Scanner inLineLines = null;
        try {inLine = new Scanner(inFile); inLineLines = new Scanner(inFile);} catch (FileNotFoundException ex) {}
        
        int lines = 0;
        while(inLineLines.hasNext()){
            lines++;
            System.out.println(inLineLines.nextLine());
        }
        
        String[] linesIn = new String[lines];
        
        for(int i = 0; i < lines; i++){
            linesIn[i] = inLine.nextLine();
        }
        
        String[] properties = linesIn[0].substring(1, linesIn[0].length()-1).split(",");
        
        for(int i = 0; i < properties.length; i++){
            String[] pair = properties[i].split("=");
            if(pair[0].equals("file-name")) imageLocation = fileLocation+"/"+pair[1];
            else if(pair[0].equals("tile-width")) tileWidth = Integer.parseInt(pair[1]);
            else if(pair[0].equals("tile-height")) tileHeight = Integer.parseInt(pair[1]);
            else if(pair[0].equals("tile-padding")) tilePadding = Integer.parseInt(pair[1]);
            else if(pair[0].equals("tile-scale")) tileScale = Integer.parseInt(pair[1]);
            else if(pair[0].equals("rows")) rows = Integer.parseInt(pair[1]);
            else if(pair[0].equals("cols")) cols = Integer.parseInt(pair[1]);
            else if(pair[0].equals("map-tile-width")) mapTileWidth = Integer.parseInt(pair[1]);
            else if(pair[0].equals("map-tile-height")) mapTileHeight = Integer.parseInt(pair[1]);
        }
        
        try {
            tiledImage = ImageIO.read(new File(imageLocation));
        } catch (IOException ex) {}
        
        boolean[][] saveTiles = new boolean[rows][cols];
        tilesImages = new BufferedImage[rows][cols];
        
        mapPixelHeight = mapTileWidth*tileHeight*tileScale;
        mapPixelWidth = mapTileHeight*tileWidth*tileScale;
        
        tiles = new Tile[mapTileHeight][mapTileWidth];
        
        for(int i = 1; i < lines; i++){
            properties = linesIn[i].substring(1, linesIn[i].length()-1).split(",");
            if(properties[0].equals("t")){
                String[] tileProp = new String[4];
                for(int j = 1; j < properties.length; j++){
                    String[] pair = properties[j].split("=");
                    tileProp[j-1] = pair[1];
                }
                Tile temp = new Tile(tileProp);
                tiles[temp.getRow()][temp.getCol()] = temp;
                saveTiles[temp.tRow()][temp.tCol()] = true;
            }
            else if(properties[0].equals("g")){
                String[] gVals = new String[6];
                for(int j = 1; j < properties.length; j++){
                    String[] pair = properties[j].split("=");
                    gVals[j-1] = pair[1];
                }
                int sX = Integer.parseInt(gVals[2]);
                int sY = Integer.parseInt(gVals[3]);
                int eX = Integer.parseInt(gVals[4])+1;
                int eY = Integer.parseInt(gVals[5])+1;
                for(int r = 0; r < eY-sY; r++){
                    for(int c = 0; c < eX-sX; c++){
                        String[] tileProp = new String[4];
                        tileProp[0] = gVals[0]; tileProp[1] = gVals[1];
                        tileProp[2] = (sY+r)+"";
                        tileProp[3] = (sX+c)+"";
                        Tile temp = new Tile(tileProp);
                        tiles[temp.getRow()][temp.getCol()] = temp;
                        saveTiles[temp.tRow()][temp.tCol()] = true;
                    }
                }
            }
        }
        
        for(int r = 0; r < rows; r++){
            for(int c = 0; c < cols; c++){
                if(saveTiles[r][c])
                    tilesImages[r][c] = tiledImage.getSubimage(c*tileWidth+tilePadding*(c+1), r*tileHeight+tilePadding*(r+1),tileWidth,tileHeight);
            }
        }
    }
    
    public void drawMap(Graphics g){
        for(int r = 0; r < tiles.length; r++){
            for(int c = 0; c < tiles[0].length; c++){
                if((c+1)*tileWidth*tileScale >= offsetX && (r+1)*tileHeight*tileScale >= offsetY && c*tileWidth*tileScale <= offsetX+Main.screenWidth && r*tileHeight*tileScale <= offsetY+Main.screenHeight){
                    if(tiles[r][c]!=null){
                        //System.out.println(tiles[r][c]);
                        g.drawImage(tilesImages[tiles[r][c].tRow()][tiles[r][c].tCol()], tiles[r][c].getCol()*tileWidth*tileScale-offsetX, tiles[r][c].getRow()*tileHeight*tileScale-offsetY, tileWidth*tileScale, tileHeight*tileScale, null);
                    }
                    else{
                        g.fillRect(c*tileWidth*tileScale-offsetX, r*tileHeight*tileScale-offsetY, tileWidth*tileScale, tileHeight*tileScale);
                    }
                }
            }
        }
    }
    
    public void changeOffset(int cx, int cy){
        offsetX+=cx;
        offsetY+=cy;
        if(offsetX<0)
            offsetX = 0;
        if(offsetY<0)
            offsetY = 0;
        if(mapPixelWidth-offsetX<Main.screenWidth)
            offsetX = mapPixelWidth-Main.screenWidth;
        if(mapPixelHeight-offsetY<Main.screenHeight)
            offsetY = mapPixelWidth-Main.screenHeight;
    }
    
    private class Tile{
        private int tileRow;
        private int tileCol;
        private int mapRow;
        private int mapCol;
        
        public Tile(String[] p){
            tileRow = Integer.parseInt(p[0])-1;
            tileCol = Integer.parseInt(p[1])-1;
            mapRow = Integer.parseInt(p[2])-1;
            mapCol = Integer.parseInt(p[3])-1;
        }
        
        public int getRow(){
            return mapRow;
        }
        
        public int getCol(){
            return mapCol;
        }
        
        public int tRow(){
            return tileRow;
        }
        
        public int tCol(){
            return tileCol;
        }
        
        @Override
        public String toString(){
            return tileCol+","+tileRow+","+mapCol+","+mapRow;
        }
    }
}
