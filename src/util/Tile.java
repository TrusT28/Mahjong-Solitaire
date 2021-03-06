package util;

public class Tile {
    public String nameOfTile;//Name/Number of the tile
    public String pathOfTile; //Path to image
    public String pathOfChosenTile; //Path to image "clicked"
    public int id;
    private Point3D location = null; //Location in Tiles from Builder

    public Tile(String nameOfTile, int id) {
        this.nameOfTile = nameOfTile;
        this.id = id;
        this.pathOfTile = "/resources/MJ" + nameOfTile + ".png";
        this.pathOfChosenTile = "/resources/MJ" + nameOfTile + "_c.png";
    }

    /**
     * @return Point3D location of this tile
     */
    public Point3D getLocation() {
        return location;
    }

    /**
     * @param p Point3D where the tile is located
     */
    public void setLocation(Point3D p) {
        location = p;
    }
}
