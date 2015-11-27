package ass2.spec;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 * Editted and refactored by Raphael Rueda
 */
public class Terrain {

    private Dimension mySize;
    private double[][] myAltitude;
    private List<Tree> myTrees;
    private List<Road> myRoads;
    private float[] mySunlight;

    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth) {
        mySize = new Dimension(width, depth);
        myAltitude = new double[width][depth];
        myTrees = new ArrayList<Tree>();
        myRoads = new ArrayList<Road>();
        mySunlight = new float[3];
    }
    
    public Terrain(Dimension size) {
        this(size.width, size.height);
    }

    public Dimension size() {
        return mySize;
    }

    public List<Tree> trees() {
        return myTrees;
    }

    public List<Road> roads() {
        return myRoads;
    }

    public float[] getSunlight() {
        return mySunlight;
    }

    /**
     * Set the sunlight direction. 
     * 
     * Note: the sun should be treated as a directional light, without a position
     * 
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        mySunlight[0] = dx;
        mySunlight[1] = dy;
        mySunlight[2] = dz;        
    }
    
    /**
     * Resize the terrain, copying any old altitudes. 
     * 
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        mySize = new Dimension(width, height);
        double[][] oldAlt = myAltitude;
        myAltitude = new double[width][height];
        
        for (int i = 0; i < width && i < oldAlt.length; i++) {
            for (int j = 0; j < height && j < oldAlt[i].length; j++) {
                myAltitude[i][j] = oldAlt[i][j];
            }
        }
    }

    /**
     * Get the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
        return myAltitude[x][z];
    }

    /**
     * Set the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, double h) {
        myAltitude[x][z] = h;
    }

    /**
     * Get the altitude at an arbitrary point. 
     * Non-integer points should be interpolated from neighbouring grid points
     * 
     * TO BE COMPLETED
     * 
     * @param x
     * @param z
     * @return
     */
    public double altitude(double x, double z) {
        if(x > mySize.getWidth()-1 || x < 0 || z > mySize.getHeight()-1 || z < 0) return 0;
        int xInt = (int) x;
        int zInt = (int) z;
        x = round(x, 2);
        z = round(z, 2);
        if(x == xInt && z == zInt) return getGridAltitude(xInt, zInt);
        double UL[] = {xInt, zInt, getGridAltitude(xInt,zInt)};         //Up Left corner (x, z, height)
        double UR[] = {xInt+1, zInt, getGridAltitude(xInt+1,zInt)};     //Up Right corner (x, z, height)
        double DL[] = {xInt, zInt+1, getGridAltitude(xInt,zInt+1)};     //Down Left corner (x, z, height)
        double DR[] = {xInt+1, zInt+1, getGridAltitude(xInt+1,zInt+1)}; //Down Right corner (x, z, height)
        if(x == xInt){          //along grid vertical
            return (UL[2]*(DL[1]-z) + DL[2]*(z-UL[1]));   //XX[2] are the heights to interpolate between
        } else if(z == zInt){   //along grid horizontal     and X-Y are the lengths for interpolation
            return (UL[2]*(UR[0]-x) + UR[2]*(x-UL[0]));
        } else if(x+z == DL[0]+DL[1]){ //along the diagonal
            return (DL[2]*(Math.sqrt(2*Math.pow(z-zInt, 2)))/Math.sqrt(2) +
                    UR[2]*(Math.sqrt(2*Math.pow(x-xInt, 2)))/Math.sqrt(2));
        } else {
            double xDiag = DL[0] + DL[1] - z;
            double diagHeight = (DL[2]*(Math.sqrt(2*Math.pow(z-zInt, 2)))/Math.sqrt(2) +
                    UR[2]*(Math.sqrt(2*Math.pow(xDiag-xInt, 2)))/Math.sqrt(2));
            if((z-DL[1])/(x-DL[0]) < -1){   //point lies in triangle:DL UL UR
                double leftHeight = (UL[2]*(DL[1]-z) + DL[2]*(z-UL[1]));
                return (leftHeight*(xDiag-x)/(xDiag-xInt) + diagHeight*(x-xInt)/(xDiag-xInt));
            } else {                        //point lies in triangle:DL DR UR
                double rightHeight = (UR[2]*(DR[1]-z) + DR[2]*(z-UR[1]));
                return (rightHeight*(x-xDiag)/(UR[0]-xDiag) + diagHeight*(UR[0]-x)/(UR[0]-xDiag));
            }
        }
    }

    /**
     * Add a tree at the specified (x,z) point. 
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     * 
     * @param x
     * @param z
     */
    public void addTree(double x, double z) {
        double y = altitude(x, z);
        Tree tree = new Tree(x, y, z);
        myTrees.add(tree);
    }


    /**
     * Add a road. 
     * 
     * @param width
     * @param spine
     */
    public void addRoad(double width, double[] spine) {
        Road road = new Road(width, spine);
        myRoads.add(road);        
    }

    public double round(double value, int places){
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
