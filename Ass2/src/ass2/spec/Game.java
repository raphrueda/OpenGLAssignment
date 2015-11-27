package ass2.spec;

import com.jogamp.opengl.util.FPSAnimator;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Timer;
import java.util.TimerTask;


/**
 * @author malcolmr
 * editted and refactored by Raphael Rueda
 */
public class Game extends JFrame implements GLEventListener, KeyListener{

    private Terrain myTerrain;

    //Positional fields
    private double currPos[] = {0.0, 0.0};
    private double camPos[] = {0.0, 5.0};
    private double forward[] = {0.0, -1.0, 0.0};
    private double rotateY = 0;

    //Sun fields
    private double sunRot = 0;
    private Timer timer;

    //Textures fields
    private String grass = "grass.jpg";
    private String grassExt = "jpg";
    private String bark = "bark.jpg";
    private String barkExt = "jpg";
    private String tree = "tree.jpg";
    private String treeExt = "jpg";
    private String road = "road.jpg";
    private String roadExt = "jpg";
    private String dirt = "dirt.jpg";
    private String dirtExt = "jpg";
    MyTexture myTextures[];

    public Game(Terrain terrain) {
        super("Assignment 2");
        myTerrain = terrain;
    }

    /**
     * Run the game.
     *
     */
    public void run() {
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLJPanel panel = new GLJPanel();
        panel.addGLEventListener(this);
        panel.addKeyListener(this);

        // Add an animator to call 'display' at 60fps
        FPSAnimator animator = new FPSAnimator(60);
        animator.add(panel);
        animator.start();

        getContentPane().add(panel);
        setSize(800, 600);
        setVisible(true);

        //Timer for the sun
        timer = new Timer();
        //Rotate the sun vector 10 degrees every second
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                sunRot -= 1;
                sunRot = sunRot % 360;
                System.out.println(sunRot);
            }
        }, 0, 100);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Load a level file and display it.
     *
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File(args[0]));
        Game game = new Game(terrain);
        game.run();
    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();

        //Cylinder variables
        int ang,i;
        int delang = 10;
        double x1,x2,z1,z2,x3,z3;
        double r[] = {0.15,0.15};
        double y[] = {0.0,1.3};

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        GLU glu = new GLU();

        double height = myTerrain.altitude(currPos[0], currPos[1]);

        glu.gluLookAt(camPos[0], 5.0 + height, camPos[1], currPos[0], height, currPos[1], 0.0, 1.0, 0.0);

        //Set up lighting w.r.t. "time of day"
        gl.glPushMatrix();
        gl.glRotated(sunRot, 0, 0, 1);
        setLighting(gl);
        gl.glPopMatrix();

        //Draw the avatar/ball
        gl.glPushMatrix();
        gl.glTranslated(currPos[0],height+.5,currPos[1]);
        gl.glRotated(-rotateY, 0, 1, 0);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, myTextures[4].getTextureId());
        GLUquadric quadric = glu.gluNewQuadric();
        glu.gluQuadricTexture(quadric, true);
        glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
        glu.gluSphere(quadric, .5, 10, 10);
        gl.glPopMatrix();

        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);

        gl.glBindTexture(GL2.GL_TEXTURE_2D, myTextures[0].getTextureId());

        //Grass lighting
        float matAmb[] = {1f, 1f, 1f, 1.0f};
        float matSpe[] = {.0f, .0f, .0f, 1.0f};
        float matPho[] = {16.0f};

        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, matAmb, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, matSpe, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, matPho, 0);

        //Draw each terrain triangle
        for (int z = 0; z < myTerrain.size().getHeight() - 1; z++) {
            for (int x = 0; x < myTerrain.size().getWidth() - 1; x++) {
                double height0 = myTerrain.getGridAltitude(x, z);
                double height1 = myTerrain.getGridAltitude(x, z + 1);
                double height2 = myTerrain.getGridAltitude(x + 1, z);
                gl.glBegin(GL2.GL_TRIANGLES);
                {
                    double p0[] = {x, height0, z};
                    double p1[] = {x, height1, z + 1};
                    double p2[] = {x + 1, height2, z};
                    gl.glNormal3dv(normal(p0, p1, p2), 0);
                    gl.glTexCoord2d(0,1); gl.glVertex3dv(p0, 0);
                    gl.glTexCoord2d(0,0); gl.glVertex3dv(p1, 0);
                    gl.glTexCoord2d(1,1); gl.glVertex3dv(p2, 0);
                }
                gl.glEnd();
                height0 = height2;
                height2 = myTerrain.getGridAltitude(x + 1, z + 1);
                gl.glBegin(GL2.GL_TRIANGLES);
                {
                    double p0[] = {x + 1, height0, z};
                    double p1[] = {x, height1, z + 1};
                    double p2[] = {x + 1, height2, z + 1};
                    gl.glNormal3dv(normal(p0, p1, p2), 0);
                    gl.glTexCoord2d(1,1); gl.glVertex3dv(p0, 0);
                    gl.glTexCoord2d(0,0); gl.glVertex3dv(p1, 0);
                    gl.glTexCoord2d(1,0); gl.glVertex3dv(p2, 0);
                }
                gl.glEnd();
            }
        }

        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);

        //Draw each tree
        for (Tree t : myTerrain.trees()) {
            matAmb[0] = 0.47f;
            matAmb[1] = 0.39f;
            matAmb[2] = 0.32f;

            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, matAmb, 0);

            for (i = 0; i < r.length - 1; i++) {
                gl.glBindTexture(GL2.GL_TEXTURE_2D, myTextures[1].getTextureId());
                gl.glBegin(GL2.GL_TRIANGLE_STRIP);

                //Draw trunk
                for (ang = 0; ang <= 360; ang += delang) {
                    x1 = r[i] * Math.cos((double) ang * 2.0 * Math.PI / 360.0);
                    x2 = r[i + 1] * Math.cos((double) ang * 2.0 * Math.PI / 360.0);
                    z1 = r[i] * Math.sin((double) ang * 2.0 * Math.PI / 360.0);
                    z2 = r[i + 1] * Math.sin((double) ang * 2.0 * Math.PI / 360.0);

                    x3 = r[i] * Math.cos((double) (ang + delang) * 2.0 * Math.PI / 360.0);
                    z3 = r[i] * Math.sin((double) (ang + delang) * 2.0 * Math.PI / 360.0);

                    double v1[] = new double[3];
                    v1[0] = x2 - x1;                    // difference between p2 and p1
                    v1[1] = y[i + 1] - y[i];
                    v1[2] = z2 - z1;

                    double v2[] = new double[3];
                    v2[0] = x3 - x1;                    // difference between p3 and p1
                    v2[1] = y[i + 1] - y[i];
                    v2[2] = z3 - z1;

                    double normal[] = new double[3];

                    normCrossProd(v1, v2, normal);    // normalized (unit) cross product
                    //Each pair of vertices ends up with the face normal of
                    //the first face it is attached to. A bit dodgy but looks ok.

                    gl.glNormal3dv(normal, 0);
                    gl.glTexCoord2d(0,0); gl.glVertex3d(x1 + t.getPosition()[0], y[i] + t.getPosition()[1], z1 + t.getPosition()[2]);
                    gl.glTexCoord2d(1,1); gl.glVertex3d(x2 + t.getPosition()[0], y[i + 1] + t.getPosition()[1], z2 + t.getPosition()[2]);

                }
                gl.glEnd();

                //Draw leaves
                gl.glPushMatrix();
                {

                    matAmb[0] = 0.38f;
                    matAmb[1] = 0.63f;
                    matAmb[2] = 0.35f;

                    gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, matAmb, 0);
                    gl.glTranslated(t.getPosition()[0], t.getPosition()[1] + 1.3, t.getPosition()[2]);

                    gl.glBindTexture(GL2.GL_TEXTURE_2D, myTextures[2].getTextureId());
                    glu.gluQuadricTexture(quadric, true);
                    glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
                    glu.gluSphere(quadric, .85, 6, 6);
                }
                gl.glPopMatrix();
            }
        }

        gl.glBindTexture(GL2.GL_TEXTURE_2D, myTextures[3].getTextureId());

        //Draw each road
        for (Road road : myTerrain.roads()) {
            matAmb[0] = 0.3f;
            matAmb[1] = 0.3f;
            matAmb[2] = 0.3f;

            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, matAmb, 0);

            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

            gl.glBegin(GL2.GL_QUADS);
            {

                double roadHeight = myTerrain.getGridAltitude((int) road.point(0)[0], (int) road.point(0)[1]) + .01;
                double j = 0;
                while (j < road.size() - .01) {
                    double p0[] = {road.point(j)[0], roadHeight, road.point(j)[1]};
                    double p1[] = {road.point(j + .01)[0], roadHeight, road.point(j + .01)[1]};
                    double p2[] = {road.controlPoint(road.numControlPoints() - 1)[0], roadHeight, road.controlPoint(road.numControlPoints() - 1)[1]};
                    if (j != road.size() - .02) {
                        p2[0] = road.point(j + .02)[0];
                        p2[2] = road.point(j + .02)[1];
                    }

                    double slope[] = {(p1[0] - p0[0]), (p1[2] - p0[2])};
                    double norm[] = {slope[1], -slope[0], 0};          //extra zero so we can use the normalise function

                    norm = normalise(norm);

                    double pA[] = {p0[0] + (road.width() / 2) * norm[0], roadHeight, p0[2] + (road.width() / 2) * norm[1]};
                    double pB[] = {p0[0] + (road.width() / 2) * -norm[0], roadHeight, p0[2] + (road.width() / 2) * -norm[1]};

                    slope[0] = p2[0] - p1[0];
                    slope[1] = p2[2] - p1[2];

                    norm[0] = slope[1];
                    norm[1] = -slope[0];

                    norm = normalise(norm);

                    double pD[] = {p1[0] + (road.width() / 2) * norm[0], roadHeight, p1[2] + (road.width() / 2) * norm[1]};
                    double pC[] = {p1[0] + (road.width() / 2) * -norm[0], roadHeight, p1[2] + (road.width() / 2) * -norm[1]};

                    gl.glTexCoord2d(0,0); gl.glVertex3dv(pA, 0);
                    gl.glTexCoord2d(.1,0); gl.glVertex3dv(pB, 0);

                    gl.glTexCoord2d(.1,.1); gl.glVertex3dv(pC, 0);
                    gl.glTexCoord2d(0,.1); gl.glVertex3dv(pD, 0);
                    j = j + .01;
                    j = round(j, 2);
                    if (j == road.size() - .01) {
                        slope[0] = p1[0] - p2[0];
                        slope[1] = p1[1] - p2[1];

                        norm[0] = slope[1];
                        norm[1] = -slope[0];

                        norm = normalise(norm);

                        double pF[] = {p2[0] + (road.width() / 2) * norm[0], roadHeight, p2[2] + (road.width() / 2) * norm[1]};
                        double pE[] = {p2[0] + (road.width() / 2) * -norm[0], roadHeight, p2[2] + (road.width() / 2) * -norm[1]};

                        gl.glVertex3dv(pD, 0);
                        gl.glVertex3dv(pC, 0);
                        gl.glVertex3dv(pF, 0);
                        gl.glVertex3dv(pE, 0);

                    }
                }
            }
            gl.glEnd();
        }

        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);

        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        //Set up avatar and camera positions
        currPos[0] = (myTerrain.size().getWidth()-1)/2;
        currPos[1] = (myTerrain.size().getHeight()-1)/2;

        camPos[0] = (myTerrain.size().getWidth()-1)/2;
        camPos[1] = ((myTerrain.size().getHeight()-1)/2) + 5;

        //Import textures
        myTextures = new MyTexture[5];
        myTextures[0] = new MyTexture(gl, grass, grassExt);
        myTextures[1] = new MyTexture(gl, bark, barkExt);
        myTextures[2] = new MyTexture(gl, tree, treeExt);
        myTextures[3] = new MyTexture(gl, road, roadExt);
        myTextures[4] = new MyTexture(gl, dirt, dirtExt);

        gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        gl.glEnable(GL2.GL_DEPTH_TEST);

        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glCullFace(GL2.GL_BACK);

        gl.glEnable(GL2.GL_LIGHTING);

        gl.glEnable(GL2.GL_TEXTURE_2D);

        gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);

        gl.glEnable(GL2.GL_NORMALIZE);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width,
                        int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        GLU glu = new GLU();
        glu.gluPerspective(60.0, (float)width/(float)height, 0.5, 50.0);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    public void setLighting(GL2 gl){
        //Sunlight
        float amb0[] = { 0.0f, 0.0f, 0.0f, 1.0f };
        float dif0[] = { 0.7f, 0.7f, 0.7f, 1.0f };
        float spe0[] = { 1.0f, 1.0f, 1.0f, 1.0f };
        //Global aesthetic light
        float amb1[] = { 0.2f, 0.2f, 0.2f, 1.0f };
        float dif1[] = { 0.1f, 0.1f, 0.1f, 1.0f };
        float spe1[] = { 0.1f, 0.1f, 0.1f, 1.0f };

        float glo[] = { 0.1f, 0.1f, 0.1f, 1.0f };

        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, amb0, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, dif0, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, spe0, 0);

        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, amb1, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, dif1, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, spe1, 0);

        float sun[] = myTerrain.getSunlight();
        float pos0[] = {sun[0], sun[1], sun[2], 0};
        float pos1[] = {0, 1, 0, 0};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, pos0, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, pos1, 0);

        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_LIGHT1);

        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, glo, 0);
    }

    double [] cross(double u[], double v[]){
        double crossProduct[] = new double[3];
        crossProduct[0] = u[1]*v[2] - u[2]*v[1];
        crossProduct[1] = u[2]*v[0] - u[0]*v[2];
        crossProduct[2] = u[0]*v[1] - u[1]*v[0];
        return crossProduct;
    }

    //Find normal for planar polygon
    public double[] normal(double[] p0, double p1[], double p2[]){
        double [] u = {p1[0] - p0[0], p1[1] - p0[1], p1[2] - p0[2]};
        double [] v = {p2[0] - p0[0], p2[1] - p0[1], p2[2] - p0[2]};
        double [] normal = cross(u,v);
        return normalise(normal);
    }


    double [] normalise(double [] n){
        double  mag = getMagnitude(n);
        double norm[] = {n[0]/mag,n[1]/mag,n[2]/mag};
        return norm;
    }

    double getMagnitude(double [] n){
        double mag = n[0]*n[0] + n[1]*n[1] + n[2]*n[2];
        mag = Math.sqrt(mag);
        return mag;
    }

    void normCrossProd(double v1[], double v2[], double out[])
    {
        out[0] = v1[1]*v2[2] - v1[2]*v2[1];
        out[1] = v1[2]*v2[0] - v1[0]*v2[2];
        out[2] = v1[0]*v2[1] - v1[1]*v2[0];
        normalise(out);
    }

    public double round(double value, int places){
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void keyPressed(KeyEvent ev) {
        switch (ev.getKeyCode()) {
            case KeyEvent.VK_W:
                currPos[0] += .1 * forward[0];
                currPos[1] += .1 * forward[1];

                currPos[0] = round(currPos[0], 3);
                currPos[1] = round(currPos[1], 3);

                camPos[0] += .1 * forward[0];
                camPos[1] += .1 * forward[1];

                camPos[0] = round(camPos[0], 3);
                camPos[1] = round(camPos[1], 3);

                /*System.out.println(rotateY + "| now facing:{" + forward[0] + "," + forward[1] + "} | " +
                        "camPos:{" + camPos[0] + "," + camPos[1] + "} | " +
                        "currPos:{" + currPos[0] + "," + currPos[1] + "}");*/
                break;
            case KeyEvent.VK_S:
                currPos[0] -= .1 * forward[0];
                currPos[1] -= .1 * forward[1];
                currPos[0] = round(currPos[0], 3);
                currPos[1] = round(currPos[1], 3);

                camPos[0] -= .1 * forward[0];
                camPos[1] -= .1 * forward[1];

                camPos[0] = round(camPos[0], 3);
                camPos[1] = round(camPos[1], 3);

                /*System.out.println(rotateY + "| now facing:{" + forward[0] + "," + forward[1] + "} | " +
                        "camPos:{" + camPos[0] + "," + camPos[1] + "} | " +
                        "currPos:{" + currPos[0] + "," + currPos[1] + "}");*/
                break;
            case KeyEvent.VK_A:
                //c-=.1;
                rotateY -= 5;
                if(rotateY > 180) rotateY -= 360;
                camPos[0] = currPos[0] + (5 * Math.cos((rotateY+90)*Math.PI/180));
                camPos[1] = currPos[1] + (5 * Math.sin((rotateY+90)*Math.PI/180));
                camPos[0] = round(camPos[0], 3);
                camPos[1] = round(camPos[1], 3);

                forward[0] = currPos[0] - camPos[0];
                forward[1] = currPos[1] - camPos[1];
                forward = normalise(forward);
                forward[0] = round(forward[0],3);
                forward[1] = round(forward[1],3);
                /*System.out.println(rotateY + "| now facing:{" + forward[0]+"," + forward[1]+"} | " +
                        "camPos:{" + camPos[0] + "," + camPos[1] + "} | " +
                        "currPos:{" + currPos[0] + "," + currPos[1]+ "}");*/
                break;
            case KeyEvent.VK_D:
                //c+=.1;
                rotateY += 5;
                if(rotateY < -180) rotateY += 360;
                camPos[0] = currPos[0] + (5 * Math.cos((rotateY+90)*Math.PI/180));
                camPos[1] = currPos[1] + (5 * Math.sin((rotateY+90)*Math.PI/180));
                camPos[0] = round(camPos[0], 3);
                camPos[1] = round(camPos[1], 3);

                forward[0] = currPos[0] - camPos[0];
                forward[1] = currPos[1] - camPos[1];

                forward = normalise(forward);

                forward[0] = round(forward[0],3);
                forward[1] = round(forward[1],3);

                /*System.out.println(rotateY + "| now facing:{" + forward[0]+"," + forward[1]+"} | " +
                        "camPos:{" + camPos[0] + "," + camPos[1] + "} | " +
                        "currPos:{" + currPos[0] + "," + currPos[1]+ "}");*/
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }
}

