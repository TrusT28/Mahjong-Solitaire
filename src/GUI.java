import util.Point3D;
import util.Tile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

//Project by Rustam Fadeev
public class GUI extends JFrame {
    private final boolean GodMode = false; //set true to remove any tile, when clicking. For debugging.
    private Tile chosen = null; //Tile, which is currently chosen


    /**
     * Checks if Tile is clickable
     *
     * @param tile    the tile to be checked
     * @param builder reference to builder of current mahjong tower
     * @return true if tile tile is clickable, false otherwise
     */
    boolean isClickable(Tile tile, Builder builder) {
        builder.findClickablePositions(); //Get current list of removable/clickable positions
        Point2D p = new Point2D.Double(tile.getLocation().getX(), tile.getLocation().getY());
        return builder.getClickablePositions().contains(p)
                && builder.getMap()[tile.getLocation().getX()][tile.getLocation().getY()] == (tile.getLocation().getZ() + 1);
    }


    /**
     * Checks if 2 points are the same
     *
     * @param p1 first Point3D
     * @param p2 second Point3D
     * @return true if coordinates of both points are the same, false otherwise
     */
    boolean isSamePositions(Point3D p1, Point3D p2) {
        return (p1.getX() == p2.getX() && p1.getY() == p2.getY() && p1.getZ() == p2.getZ());
    }

    /**
     * Checks if 2 Tiles are matching
     *
     * @param t1 First tile
     * @param t2 Second tile
     * @return true if t1 and t2 are matching (i.e have the same type and can be removed), false otherwise
     */
    boolean isMatchingTile(Tile t1, Tile t2) {
        boolean match = false;
        if (t1.nameOfTile.equals(t2.nameOfTile)) match = true; //If tiles are the same
        else if (t1.id >= 0 && t1.id < 4 && t2.id >= 0 && t2.id < 4) //Unique tiles check. Flowers. Check Builder.determine_tile
            match = true;
        else if (t1.id >= 4 && t1.id < 8 && t2.id >= 4 && t2.id < 8) //Unique tiles check. Seasons. Check Builder.determine_tile
            match = true;
        return match;
    }

    /**
     * constructor that creates the gui
     *
     * @param builder reference to the builder of current mahjong tower
     */
    public GUI(Builder builder) {
        JFrame frame = new JFrame("Mahjong by Rustam Fadeev");
        frame.setBounds(100, 100, 900, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null); //Open window in center

        //Variables
        int lowX = 50; //Starting x position
        int width = 45; //Width of a tile
        int height = 54; //Height of a tile
        int lowY = 30; //Starting y position
        int centerX = (int) (lowX + (7.5 * width)); //X pos of center
        int centerY = (int) (lowY + (4.5 * height) - 15); //Y pos of center
        Tile[][][] tiles = builder.getTiles();//array of Tiles and their positions
        JLabel[][][] jlabel = new JLabel[8][15][5]; //Array of all labels
        JLabel window = new JLabel(""); //Label in case of winning
        int row, col, depth;
        //Place all Labels to their positions, according to "tiles"
        for (depth = 4; depth >= 0; depth--) { //From 4, because of Swing Drawing
            for (row = 0; row < 8; row++)
                for (col = 0; col < 15; col++) {
                    int rowT = row;
                    int colT = col;
                    int depthT = depth;
                    Tile tile = tiles[rowT][colT][depthT]; //Get tile at this position
                    if (tile != null) { //Not empty position
                        jlabel[row][col][depth] = new JLabel("");
                        if (depth == 4) //Special case: Center
                            jlabel[row][col][depth].setBounds(centerX, centerY, width, height);
                        else if (depth == 0 && row == 3 && (col == 0 || col == 13 || col == 14)) //Special case: not-in-line Tiles
                            jlabel[row][col][depth].setBounds(lowX + (width) * (col + 1), lowY + (row + 1) * (height + 3), width, height);
                        else
                            jlabel[row][col][depth].setBounds(lowX + (width) * (col + 1), lowY + (row + 1) * (height - 4), width, height);
                        jlabel[row][col][depth].addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent e) {
                                if (isClickable(tile, builder)) { //If clickable, change color
                                    if (depthT == 4 || tiles[rowT][colT][depthT + 1] == null) //If nothing is above and not top level
                                        jlabel[rowT][colT][depthT].setIcon(new ImageIcon(GUI.class.getResource(tile.pathOfChosenTile)));
                                }
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                if (chosen == null || !isSamePositions(chosen.getLocation(), tile.getLocation()))
                                    jlabel[rowT][colT][depthT].setIcon(new ImageIcon(GUI.class.getResource(tile.pathOfTile)));
                            }

                            @Override
                            public void mouseClicked(MouseEvent e) {
                                if (GodMode) { //If god mode, delete any tile on which you click
                                    jlabel[rowT][colT][depthT].setVisible(false); //Delete Label
                                    tiles[rowT][colT][depthT] = null; //Delete Tile
                                    if (depthT == 4) //Special case - Center
                                        builder.getMap()[rowT][colT] -= 5; //Delete the top tile from Map
                                    else builder.getMap()[rowT][colT]--; //Delete tile from Map. Open the tile below.
                                    builder.getClickablePositions().remove(new Point2D.Double(rowT, colT)); //Delete this pos from clickable positions
                                    builder.findClickablePositions(); //Update clickable positions
                                } else {//Not God mode
                                    System.out.println("Clicked on " + tile.nameOfTile);
                                    if (isClickable(tile, builder) && (depthT == 4 || tiles[rowT][colT][depthT + 1] == null)) { //If this tile is clickable
                                        if (chosen == null) { //No tile is chosen
                                            chosen = tile; //Make this tile be chosen
                                            jlabel[rowT][colT][depthT].setIcon(new ImageIcon(GUI.class.getResource(tile.pathOfChosenTile))); //Change icon
                                            System.out.println(" is now Chosen");
                                        } else { //There is some chosen tile already
                                            if (isSamePositions(tile.getLocation(), chosen.getLocation())) { //We clicked on the same tile
                                                chosen = null;
                                                jlabel[rowT][colT][depthT].setIcon(new ImageIcon(GUI.class.getResource(tile.pathOfTile))); //Change icon back to normal
                                                System.out.println(" unchosen, since the same");
                                            } else if (isMatchingTile(chosen, tile)) { //We found matching tiles
                                                //Delete current label
                                                jlabel[rowT][colT][depthT].setVisible(false); //Delete current label
                                                tiles[rowT][colT][depthT] = null; //Delete current tile
                                                if (depthT == 4)
                                                    builder.getMap()[rowT][colT] = 0; //Delete top tile. Special case
                                                else
                                                    builder.getMap()[rowT][colT]--; //Delete current tile from Map. "Open" the tile below
                                                builder.getClickablePositions().remove(new Point2D.Double(rowT, colT)); //Delete current tile from clickable positions

                                                //Delete chosen label
                                                jlabel[chosen.getLocation().getX()][chosen.getLocation().getY()][chosen.getLocation().getZ()].setVisible(false);//Delete chosen label
                                                tiles[chosen.getLocation().getX()][chosen.getLocation().getY()][chosen.getLocation().getZ()] = null;//delete chosen tile
                                                if (chosen.getLocation().getZ() == 4)
                                                    builder.getMap()[chosen.getLocation().getX()][chosen.getLocation().getY()] = 0; //Delete top tile from Map. Special case
                                                else
                                                    builder.getMap()[chosen.getLocation().getX()][chosen.getLocation().getY()]--; //Delete tile from Map. "Open" the tile below
                                                builder.getClickablePositions().remove(new Point2D.Double(chosen.getLocation().getX(), chosen.getLocation().getY()));//Delete tile from clickable positions
                                                builder.findClickablePositions();
                                                chosen = null;
                                                System.out.println(" deleted since matched");
                                            } else { //Non-matching tiles
                                                //put chosen icon back to normal
                                                jlabel[chosen.getLocation().getX()][chosen.getLocation().getY()][chosen.getLocation().getZ()].setIcon(new ImageIcon(GUI.class.getResource(chosen.pathOfTile)));
                                                chosen = null;
                                                System.out.println(" nothing. Chosen is null");
                                            }
                                        }
                                    }
                                }
                                //After clicking, check if some tiles are left. If not, end game
                                if (builder.isEmptyMap()) {
                                    window.setVisible(true);
                                }
                            }
                        });
                        jlabel[row][col][depth].setIcon(new ImageIcon(GUI.class.getResource(tile.pathOfTile))); //Put default icon
                        frame.getContentPane().add(jlabel[row][col][depth]); //Set Label to frame
                    }
                }
            //At each depth move tiles a little to get 3D perception
            //Not actually that good
            lowX += 5;
            lowY -= 4;
        }
        //Create Label with the text "You won!", but don't use it until every tile is deleted
        window.setBounds(centerX, 0, 100, 30);
        window.setBackground(Color.RED);
        window.setText("YOU WON!");
        frame.getContentPane().add(window);
        window.setVisible(false);

        //Create Button to reset the game
        JButton jButton = new JButton("Retry?");
        jButton.setBounds(0, 0, 100, 30);
        jButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frame.setVisible(false);
                frame.dispose(); //Destroy the JFrame object
                Main.main(null);//Call Main again
            }
        });

        frame.getContentPane().add(window);
        frame.getContentPane().add(jButton);
        frame.setVisible(true);
    }
}
