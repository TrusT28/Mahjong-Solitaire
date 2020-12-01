import util.Point3D;
import util.Tile;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

//Project by Rustam Fadeev
public class Builder {
    //Notes:
    //In this implementation The top-center tile is located on level 1 (depth 0), position (0,14), but in GUI it is depicted on top (lvl 5).
    //Run "check_Map" to see how Map looks like. It will help understanding the code. Notice that (0,14)=5
    //----------
    private final int[][] Map = new int[8][15]; //Map of the Tower. Levels 1-5. Need in GUI
    private final Tile[][][] Tiles = new Tile[8][15][5]; //Location of tiles on each level. Need in GUI
    private final boolean[][][] placingPositions = new boolean[8][15][5]; //Boolean Map for each level. Useful for "Microsoft" Algorithm. Need in GUI
    private final List<Point2D> clickablePositions = new ArrayList<>(); //Dynamic Array of clickable positions
    private final List<Point3D> accessibleForPlacingPositions = new ArrayList<>(); //Dynamic Array of accessible positions for placing a Tile
    private final int[] listOfTiles = new int[42];//List of Types of tiles. Check "determine_tile" for details
    private static final Point2D.Double[] specialPositions = {
            //Special positions, which need unique checking
            //Since I use 1 tile - 1 square style, tiles which are placed not-in-line require unique checking
            new Point2D.Double(3, 1), new Point2D.Double(4, 1),
            new Point2D.Double(3, 6), new Point2D.Double(4, 6),
            new Point2D.Double(3, 7), new Point2D.Double(4, 7),
            new Point2D.Double(3, 12), new Point2D.Double(4, 12),
            new Point2D.Double(3, 13)
    };

    /**
     * @return return int[8][15] Map of current tower
     */
    public int[][] getMap() {
        return Map;
    }

    /**
     * @return return ArrayList of all currently clickable positions
     */
    public List<Point2D> getClickablePositions() {
        return clickablePositions;
    }

    /**
     * @return Tiles array, containing info about what tile is located at pos [x][y][z]
     */
    public Tile[][][] getTiles() {
        return Tiles;
    }


    /**
     * Place tiles to positions, to use them in GUI
     * Uses randomness. "Microsoft Algorithm" from wiki. Can be unsolvable
     * Fills Tile[][][] tiles
     */
    void placeTilesRandomly() {

        //Starting from the bottom, pick 2 random positions and 2 random matching tile.
        //Place these tiles to these positions
        //If no more positions on depth n left, go to depth n+1
        //It can happen that 1st position is on depth n and 2nd is on depth n+1

        int depth = 0; //Level 1
        findAccessiblePositions(depth); //Find all accessible positions at current depth
        while (depth < 4) { //Until 4, because the only accessible position at depth 4 will be filled during depth=3
            //Find 2 positions at random
            final Point3D p1 = findRandomPositions(); //Find first random position
            if (accessibleForPlacingPositions.size() == 0) { //If there is no accessible positions on this depth, go up
                depth++; //Go level up
                findAccessiblePositions(depth);//Find position there
            }
            final Point3D p2 = findRandomPositions(); //Find second random position
            Tile[] random_tiles = findTwoRandomTile();//Find two random matching tiles to place there

            random_tiles[0].setLocation(p1); //set location where first tile will be
            random_tiles[1].setLocation(p2);//set location where second tile will be
            Tiles[p1.getX()][p1.getY()][p1.getZ()] = random_tiles[0]; //Set Tile at chosen location
            Tiles[p2.getX()][p2.getY()][p2.getZ()] = random_tiles[1];
        }
    }

    /**
     * Places tiles, such that Mahjong is 100% solvable.
     * Place tiles to positions, to draw them in GUI.
     * Recommended to use this method, instead of placeTilesRandomly
     * Fills Tile[][][] Tiles
     */
    void placeTilesSolvable() {
        //100% solvable, because this method, basically, solves Mahjong backwards
        //We construct Tiles Array using only positions, on which player can click
        //
        //Find at random 2 positions and 2 matching tiles. Place the matching tiles into these 2 locations
        //these 2 positions are picked at random from the set of currently clickable positions
        while (!isEmptyMap()) {//Until Board is filled completely (i.e Map = 0)
            //Find 2 random clickable positions
            final Point3D p1 = findRandomClickablePosition(); //Find first random clickable position
            final Point3D p2 = findRandomClickablePosition(); //Find second random clickable position
            Tile[] randomTile = findTwoRandomTile();//Find 2 random matching tiles to place there
            randomTile[0].setLocation(p1); //set location where random tile 1 will be
            randomTile[1].setLocation(p2);//set location where random tile 2 will be
            Tiles[p1.getX()][p1.getY()][p1.getZ()] = randomTile[0];
            Tiles[p2.getX()][p2.getY()][p2.getZ()] = randomTile[1];
        }
    }


    /**
     * @param i an index of "listOfTiles"
     * @return Given an index (0-41), return specific Tile type
     * <p>
     * 0-3 Flowers || 4-7 Seasons || 8-11 Wind || 12-14 Dragon
     * 15-23 Copper|| 24-32 Bamboo|| 33-41 Numbers
     */
    Tile determineTile(int i) {
        //0-3 Flowers || 4-7 Seasons || 8-11 Wind || 12-14 Dragon
        //15-23 Copper|| 24-32 Bamboo|| 33-41 Numbers
        Tile t = new Tile("", -1);
        if (i >= 0 && i < 4) { //Flowers (4)
            t = new Tile("f" + (i + 1), i);
        } else if (i >= 4 && i < 8) { //Seasons (4)
            t = new Tile("s" + ((i % 4) + 1), i);
        } else if (i >= 8 && i < 12) { //Wind (4)
            t = new Tile("w" + ((i % 8) + 1), i);
        } else if (i >= 12 && i < 15) { //Dragon (3)
            t = new Tile("d" + ((i % 12) + 1), i);
        } else if (i >= 15 && i < 24) { //Copper (9)
            t = new Tile("c" + ((i % 15) + 1), i);
        } else if (i >= 24 && i < 33) { //Bamboo(9)
            t = new Tile("b" + ((i % 24) + 1), i);
        } else if (i >= 33 && i < 42) //Numbers (9)
            t = new Tile("n" + ((i % 33) + 1), i);
        if (t.id == -1) System.out.println("ERROR: unknown tile");
        return t;
    }

    /**
     * Find completely random position, based on "accessiblePositions" List
     *
     * @return Point3D which represents position at boolean[][][] placingPosition
     */
    Point3D findRandomPositions() {
        Random random = new Random();
        Point3D p = accessibleForPlacingPositions.get(random.nextInt(accessibleForPlacingPositions.size())); //Get random accessible position
        placingPositions[p.getX()][p.getY()][p.getZ()] = false; //Remove position from placing positions
        accessibleForPlacingPositions.remove(p);
        return p;
    }

    /**
     * Find clickable position at random, based on "clickable_pos" List.
     *
     * @return Point3D which represents position at boolean[][][] placing_pos
     */
    Point3D findRandomClickablePosition() {
        findClickablePositions(); //Find all currently clickable positions, based on Map
        Random random = new Random();
        int i = random.nextInt(clickablePositions.size()); //get random index
        Point2D p_temp = clickablePositions.get(i); //Get random clickable position
        Point3D p = new Point3D(//Make Point3D out if it
                (int) p_temp.getX(),
                (int) p_temp.getY(),
                Map[(int) p_temp.getX()][(int) p_temp.getY()] - 1);
        placingPositions[p.getX()][p.getY()][p.getZ()] = false; //Remove position from positions, where we can place something
        clickablePositions.remove(p_temp);//Remove position from "clickable" positions
        if (p.getX() == 0 && p.getY() == 14)
            Map[p.getX()][p.getY()] = 0; //Special case. (0,14) is a top-center, thus Map[0][14]=5. Set to 0 to remove
        else Map[p.getX()][p.getY()]--; //Remove current tile and "open" tile at lower level
        return p;
    }


    /**
     * Map[x][y] = 0 means 0 tiles can be placed at (x,y)
     *
     * @return true if Map is array of 0's, false otherwise
     */
    public boolean isEmptyMap() {
        //Check if all positions are filled (i.e all 0)
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 15; j++)
                if (Map[i][j] > 0) return false;
        return true;
    }

    /**
     * @return Tile[2] containing 2 random matching tiles, accessible for placing
     */
    Tile[] findTwoRandomTile() {
        //Dynamic Array of currently accessible tiles to place
        List<Tile> accessibleForPlacingTiles = new ArrayList<>(); //Delete all accessible tiles to generate them again next time
        Tile[] randomTiles = new Tile[2]; //Array of 2 tiles
        Random random = new Random();
        //Update list of possible tiles
        for (int i = 0; i < listOfTiles.length; i++)
            if (listOfTiles[i] > 0) //More than 2 tiles are still available
                accessibleForPlacingTiles.add(determineTile(i)); //Add it to the list
        //Get random tile
        int i = random.nextInt(accessibleForPlacingTiles.size());//Generate random index
        Tile randomTile = accessibleForPlacingTiles.get(i); //Get random tile
        Tile randomTile2;
        if (randomTile.id >= 0 && randomTile.id < 8) { //Unique tile positions. Can't take 2, since there is only 1
            listOfTiles[randomTile.id]--; //Delete found tile
            int lo, up;
            if (randomTile.id < 4) { //Position of flowers
                lo = 0;
                up = 4;
            } else { //Position of Seasons
                lo = 4;
                up = 8;
            }
            //To randomly find another matching unique tile create array of possible tiles
            List<Integer> temp = new ArrayList<>();
            for (int j = lo; j < up; j++)
                if (j != randomTile.id && listOfTiles[j] > 0)
                    temp.add(j);
            i = random.nextInt(temp.size()); //Get random index of the other matching tile
            randomTile2 = determineTile(temp.get(i));//Get another unique matching tile
            listOfTiles[randomTile2.id]--; //Delete second tile from list
        } else { //Take 2 copies
            listOfTiles[randomTile.id] -= 2; //Delete 2 copies of the tile, which we will place
            randomTile2 = new Tile(randomTile.nameOfTile, randomTile.id); //Create copy of the tile
        }
        randomTiles[0] = randomTile;
        randomTiles[1] = randomTile2;
        return randomTiles;
    }

    /**
     * Check if position (row,column) is a special position, which you need to treat differently.
     *
     * @param row    x coordinate of position you want to check
     * @param column y coordinate of position you want to check
     * @return true if (row,column) is a special position, which you need to treat differently, false otherwise
     */
    static boolean isSpecialPosition(int row, int column) {
        Point2D point = new Point2D.Double(row, column);
        for (Point2D.Double specialPosition : specialPositions) if (point.equals(specialPosition)) return true;
        return false;
    }


    /**
     * Check if index is inside of Map
     *
     * @param row    x coordinate of position to check if inside the Map
     * @param column y coordinate of position to check if inside the Map
     * @return true if (row,column) is inside of Map, false otherwise
     */
    boolean isInside(int row, int column) {
        return (row < Map.length && column < Map[0].length
                && row >= 0 && column >= 0);
    }


    /**
     * Build Map. Don't change
     */
    void buildMap() {
        //Fill in positions for tiles in the middle of tower
        for (int pos = 1; pos < 5; pos++)
            for (int row = pos - 1; row < (9 - pos); row++)
                for (int column = pos + 2; column < (12 - pos); column++)
                    Map[row][column] = pos;
        //Fill in specific positions
        for (int column = 1; column < 13; column++)
            if (column == 1 || column == 12)
                for (int row = 0; row < 8; row++) {
                    if (row == 1 || row == 2 || row == 5 || row == 6)
                        Map[row][column] = 0;
                    else Map[row][column] = 1;
                }
            else if (column == 2 || column == 11)
                for (int row = 0; row < 8; row++) {
                    if (row == 1 || row == 6)
                        Map[row][column] = 0;
                    else Map[row][column] = 1;
                }
        Map[3][0] = 1;
        Map[3][13] = 1;
        Map[3][14] = 1;
        Map[0][14] = 5; //(0,14) is an improvised center. The top tile should be in center
    }


    /**
     * Method that sets positions accessible for tiles
     * Fills boolean[][][] placing_pos
     * Don't change anything
     */
    void setPositions() {
        for (int depth = 0; depth < 5; depth++) {
            if (depth == 0) {
                for (int row = 0; row < 8; row++)
                    for (int col = 0; col < 15; col++)
                        placingPositions[row][col][depth] = true;
                for (int i = 0; i < 8; i++)
                    if (i == 0 || i == 7 || i == 4) {
                        placingPositions[i][0][depth] = false;
                        placingPositions[i][13][depth] = false;
                        placingPositions[i][14][depth] = false;
                    }
                for (int i = 11; i < 15; i++) {
                    placingPositions[1][i][depth] = false;
                    placingPositions[6][i][depth] = false;
                    if (i > 11) {
                        placingPositions[2][i][depth] = false;
                        placingPositions[5][i][depth] = false;
                    }
                }
                for (int i = 0; i < 3; i++) {
                    placingPositions[1][i][depth] = false;
                    placingPositions[6][i][depth] = false;
                    if (i < 2) {
                        placingPositions[2][i][depth] = false;
                        placingPositions[5][i][depth] = false;
                    }
                }
            } //Fill in First level
            if (depth == 1) {
                for (int row = 0; row < 8; row++)
                    for (int col = 0; col < 15; col++) {
                        placingPositions[row][col][depth] = col > 3 && col < 10 && row > 0 && row < 7;
                    }
            }
            if (depth == 2) {
                for (int row = 0; row < 8; row++)
                    for (int col = 0; col < 15; col++) {
                        placingPositions[row][col][depth] = col > 4 && col < 9 && row > 1 && row < 6;
                    }
            }
            if (depth == 3) {
                for (int row = 0; row < 8; row++)
                    for (int col = 0; col < 15; col++) {
                        placingPositions[row][col][depth] = col > 5 && col < 8 && row > 2 && row < 5;
                    }
            }
            if (depth == 4) {
                for (int row = 0; row < 8; row++)
                    for (int col = 0; col < 15; col++) {
                        placingPositions[row][col][depth] = (row == 0 && col == 14);//(0,14) is pseudo-center
                    }
            }
        }
    }


    /**
     * Fills ArrayList accessible_pos
     * depth is a coordinate of placing_pos array (from 0 to 4)
     *
     * @param depth find all positions at given depth(0-4), where you can possibly place a Tile
     */
    void findAccessiblePositions(int depth) {
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 15; col++)
                if (placingPositions[row][col][depth])
                    accessibleForPlacingPositions.add(new Point3D(row, col, depth));
    }

    /**
     * outputs the result to ListPoint2D clickable_pos
     * Adds positions, which are clickable to ArrayList clickable_pos
     * Based on int[][] Map
     */
    public void findClickablePositions() {
        //Find all currently clickable positions, based on Map
        //Position is clickable, if either from left or right there is no obstacle
        //Adjacent position "p_adj" is an "obstacle" if the tile on "p_adj" is on the same level or higher
        //Thus, if there is no tile or tile on the lower level, the position is clickable
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 15; j++)
                if (Map[i][j] > 0) { //There is a tile at positions i,j at level=Map[i][j]
                    int obstacle = 0;
                    if (isSpecialPosition(i, j)) { //Tile, which is adjacent to special not-in-line tiles
                        if (j == 1 && Map[3][0] > 0) //Tile at (3,0) is still not removed, thus this pos is also irremovable
                            continue;
                        else if ((j == 6 || j == 7) && Map[0][14] == 5) //Tile at (0,14) is still not removed,
                            // thus this pos is also irremovable
                            continue;
                        else if (j == 12 && Map[3][13] > 0)
                            continue;
                        else if ((j != 1 && j != 6 && j != 7) && Map[3][14] > 0)
                            continue;
                    }
                    if (isInside(i, j - 1) && Map[i][j - 1] >= Map[i][j]) obstacle++; //Obstacle from left
                    if (isInside(i, j + 1) && Map[i][j + 1] >= Map[i][j]) obstacle++; //Obstacle from right
                    if (obstacle < 2 && !clickablePositions.contains(new Point2D.Double(i, j))) //Obstacles from both sides
                        clickablePositions.add(new Point2D.Double(i, j));
                }
    }


    /**
     * @param depth Draw Map at given depth (0-4)
     */
    void drawMapAtGivenDepth(int depth) {
        for (int i = 0; i < 8; i++) {
            System.out.println();
            for (int j = 0; j < 15; j++) {
                if (placingPositions[i][j][depth]) System.out.print(1);
                else System.out.print(0);
            }
        }
    }


    /**
     * Prints every currently clickable position
     * For debugging
     */
    public void printAllClickablePositions() {
        findClickablePositions();
        for (Point2D clickablePosition : clickablePositions) {
            System.out.println(clickablePosition);
        }
    }


    /**
     * Draw Map to standard output to understand it better
     */
    public void drawMap() {
        for (int i = 0; i < 8; i++) {
            System.out.println();
            for (int j = 0; j < 15; j++) {
                System.out.print(Map[i][j]);
            }
        }
        System.out.println();
    }

    Builder() {
        buildMap();
        Arrays.fill(listOfTiles, 4);//Every tile is used 4 times
        for (int i = 0; i <= 7; i++)
            listOfTiles[i] = 1; //First 8 positions are tiles which can be used only once
        setPositions();
        placeTilesSolvable();
        buildMap();
    }
}
