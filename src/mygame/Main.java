package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;

public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    Geometry side1, side2, side3;
    Node boxes;
    Node balls;
    Geometry paddle;
    Geometry myBall;
    float Velocity;
    Geometry ball1, ball2, ball3, ball4, ball5, ball6, ball7, ball8, ball9, ball10;
    Geometry gyArrow;
    private BulletAppState bulletAppState;
    private boolean isRunning = true;
    private boolean ballOnPaddle = true;
    float angle = 0;
    Vector3f velocity;
    private AudioNode BGM;
    private BitmapText Level, Text, Score, W, GO;
    int score = 0;

    @Override
    public void simpleInitApp() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0, 0, -1));
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        cam.setLocation(new Vector3f(0, 0, 150));
        flyCam.setEnabled(false);

        PointLight myLight = new PointLight();
        myLight.setColor(ColorRGBA.White);
        myLight.setPosition(new Vector3f(0, 2, 2));
        myLight.setRadius(20);
        rootNode.addLight(myLight);

        Quad backGround = new Quad(300, 150);
        Geometry bg = new Geometry("Quad", backGround);
        bg.setLocalTranslation(-155, -80, -5);
        Material bgmat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgmat.setTexture("ColorMap", assetManager.loadTexture("Textures/NBA.jpg"));
        bg.setMaterial(bgmat);
        rootNode.attachChild(bg);

        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        Level = new BitmapText(guiFont);
        Level.setSize(guiFont.getCharSet().getRenderedSize());
        Level.setLocalTranslation(-100, Level.getLineHeight() + 30, 0);
        Level.setSize(8f);
        Level.setText("LEVEL 1");
        rootNode.attachChild(Level);

        Text = new BitmapText(guiFont);
        Text.setSize(guiFont.getCharSet().getRenderedSize());
        Text.setLocalTranslation(-100, Text.getLineHeight() + 10, 0);
        Text.setSize(3f);
        Text.setText("Press [LEFT] and [RIGHT] to Move Paddle\n\nPress [A] and [D] to Set Launch Direction\n\nPress [SPACE] to Launch\n\nPress [P] to Pause");
        rootNode.attachChild(Text);

        Score = new BitmapText(guiFont);
        Score.setSize(guiFont.getCharSet().getRenderedSize());
        Score.setLocalTranslation(60, Score.getLineHeight() + 30, 0);
        Score.setSize(8f);
        Score.setText("SCORE: " + score);
        rootNode.attachChild(Score);

        BGM = new AudioNode(assetManager, "Sounds/HUMBLE..wav", false);
        BGM.setPositional(false);
        BGM.setLooping(true);
        BGM.setVolume(0.3f);
        BGM.play();

        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        Quad Field = new Quad(80, 100);
        Geometry geom = new Geometry("Quad", Field);
        geom.setLocalTranslation(-40, -50, 0);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Textures/Floor.jpg"));
        geom.setMaterial(mat);
        rootNode.attachChild(geom);

        boxes = new Node("boxes");

        side1 = myTop("side1", Vector3f.UNIT_Y);
        side1.setLocalTranslation(0, 52, 2);
        boxes.attachChild(side1);

        side2 = mySides("side2", Vector3f.UNIT_X);
        side2.setLocalTranslation(42, 2, 2);
        boxes.attachChild(side2);

        side3 = mySides("side3", Vector3f.UNIT_X);
        side3.setLocalTranslation(-42, 2, 2);
        boxes.attachChild(side3);

        paddle = myPaddle("paddle");
        paddle.setLocalTranslation(0, -50, 2);
        boxes.attachChild(paddle);

        balls = new Node("balls");

        ball1 = enemyBalls("ball1");
        ball1.setLocalTranslation(0, 40, 2);
        balls.attachChild(ball1);

        ball2 = enemyBalls("ball2");
        ball2.setLocalTranslation(0, 0, 2);
        balls.attachChild(ball2);

        ball3 = enemyBalls("ball3");
        ball3.setLocalTranslation(20, 20, 2);
        balls.attachChild(ball3);

        ball4 = enemyBalls("ball4");
        ball4.setLocalTranslation(-20, 20, 2);
        balls.attachChild(ball4);

        ball5 = enemyBalls("ball5");
        ball5.setLocalTranslation(0, 20, 2);
        balls.attachChild(ball5);

        myBall = myBall("myBall");
        myBall.setLocalTranslation(0, -47, 2);
        rootNode.attachChild(myBall);

        rootNode.attachChild(boxes);
        rootNode.attachChild(balls);

        RigidBodyControl scenePhy = new RigidBodyControl(0f);
        boxes.addControl(scenePhy);
        balls.addControl(scenePhy);
        myBall.addControl(scenePhy);
        bulletAppState.getPhysicsSpace().add(scenePhy);

        gyArrow = myArrow("Arrow");
        gyArrow.setLocalTranslation(0, -47, 1);
        rootNode.attachChild(gyArrow);

        initKeys();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (isRunning) {
            if (!ballOnPaddle) {
                myBall.move(velocity.mult(tpf));

                CollisionResults results1 = new CollisionResults();
                boxes.collideWith(myBall.getWorldBound(), results1);
                if (results1.size() > 0) {
                    String collideWithPlane = results1.getClosestCollision().getGeometry().getName();
                    if (collideWithPlane.equals("side2")) {
                        velocity.setX(-FastMath.abs(velocity.getX()));
                    }
                    if (collideWithPlane.equals("side3")) {
                        velocity.setX(FastMath.abs(velocity.getX()));
                    }
                    if (collideWithPlane.equals("side1")) {
                        velocity.setY(-FastMath.abs(velocity.getY()));
                    }
                    if (collideWithPlane.equals("paddle")) {
                        velocity.setY(FastMath.abs(velocity.getY()));
                    }
                }

                CollisionResults results2 = new CollisionResults();
                balls.collideWith(myBall.getWorldBound(), results2);
                if (results2.size() > 0) {
                    String closestGeom = results2.getClosestCollision().getGeometry().getName();
                    if (closestGeom.equals("ball1")) {
                        getNewVelocity(myBall, ball1);
                        balls.detachChild(ball1);
                        score++;
                        Score.setText("SCORE: " + score);
                    }
                    if (closestGeom.equals("ball2")) {
                        getNewVelocity(myBall, ball2);
                        balls.detachChild(ball2);
                        score++;
                        Score.setText("SCORE: " + score);
                    }
                    if (closestGeom.equals("ball3")) {
                        getNewVelocity(myBall, ball3);
                        balls.detachChild(ball3);
                        score++;
                        Score.setText("SCORE: " + score);
                    }
                    if (closestGeom.equals("ball4")) {
                        getNewVelocity(myBall, ball4);
                        balls.detachChild(ball4);
                        score++;
                        Score.setText("SCORE: " + score);
                    }
                    if (closestGeom.equals("ball5")) {
                        getNewVelocity(myBall, ball5);
                        balls.detachChild(ball5);
                        score++;
                        Score.setText("SCORE: " + score);
                    }
                    if (closestGeom.equals("ball6")) {
                        getNewVelocity(myBall, ball6);
                        balls.detachChild(ball6);
                        score++;
                        Score.setText("SCORE: " + score);
                    }
                    if (closestGeom.equals("ball7")) {
                        getNewVelocity(myBall, ball7);
                        balls.detachChild(ball7);
                        score++;
                        Score.setText("SCORE: " + score);
                    }
                    if (closestGeom.equals("ball8")) {
                        getNewVelocity(myBall, ball8);
                        balls.detachChild(ball8);
                        score++;
                        Score.setText("SCORE: " + score);
                    }
                    if (closestGeom.equals("ball9")) {
                        getNewVelocity(myBall, ball9);
                        balls.detachChild(ball9);
                        score++;
                        Score.setText("SCORE: " + score);
                    }
                    if (closestGeom.equals("ball10")) {
                        getNewVelocity(myBall, ball10);
                        balls.detachChild(ball10);
                        score++;
                        Score.setText("SCORE: " + score);
                    }
                }
                if (score == 5 && Level.getText().equals("LEVEL 1")) {
                    level_2();
                }
                if (score == 10 && Level.getText().equals("LEVEL 2")) {
                    win();
                }
                if (myBall.getLocalTranslation().y < -55 && score != 10) {
                    GO();
                }
            }
        }
    }

    protected void level_2() {
        Level.setText("LEVEL 2");
        score = 0;
        Score.setText("SCORE: " + score);
        paddle.setLocalTranslation(0, -50, 2);
        myBall.setLocalTranslation(0, -47, 2);
        gyArrow.setLocalTranslation(0, -47, 1);
        rootNode.attachChild(gyArrow);
        ballOnPaddle = true;

        ball1.setLocalTranslation(-20, 40, 2);
        balls.attachChild(ball1);

        ball2.setLocalTranslation(0, 40, 2);
        balls.attachChild(ball2);

        ball3.setLocalTranslation(20, 40, 2);
        balls.attachChild(ball3);

        ball4.setLocalTranslation(0, 20, 2);
        balls.attachChild(ball4);

        ball5.setLocalTranslation(-20, 10, 2);
        balls.attachChild(ball5);

        ball6 = enemyBalls("ball6");
        ball6.setLocalTranslation(20, 10, 2);
        balls.attachChild(ball6);

        ball7 = enemyBalls("ball7");
        ball7.setLocalTranslation(0, 0, 2);
        balls.attachChild(ball7);

        ball8 = enemyBalls("ball8");
        ball8.setLocalTranslation(-20, -20, 2);
        balls.attachChild(ball8);

        ball9 = enemyBalls("ball9");
        ball9.setLocalTranslation(0, -20, 2);
        balls.attachChild(ball9);

        ball10 = enemyBalls("ball10");
        ball10.setLocalTranslation(20, -20, 2);
        balls.attachChild(ball10);
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Pause") && !keyPressed) {
                isRunning = !isRunning;
            }
            if (name.equals("Launch") && !keyPressed && ballOnPaddle) {
                ballOnPaddle = !ballOnPaddle;
                launchBall();
                rootNode.detachChild(gyArrow);
            }
        }
    };

    private final AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (isRunning) {
                boolean range = true;
                if (name.equals("Right")) {
                    if (paddle.getLocalTranslation().x >= 34) {
                        range = false;
                    }
                    if (ballOnPaddle) {
                        moveInitRight(range, tpf);
                    } else {
                        movePaddleRight(range, tpf);
                    }
                }
                if (name.equals("Left")) {
                    if (paddle.getLocalTranslation().x <= -34) {
                        range = false;
                    }
                    if (ballOnPaddle) {
                        moveInitLeft(range, tpf);
                    } else {
                        movePaddleLeft(range, tpf);
                    }
                }
                boolean range2 = true;
                if (name.equals("ArrowLeft")) {
                    if (angle >= 360) {
                        range2 = false;
                    }
                    rotateArrowLeft(range2, tpf);
                }
                if (name.equals("ArrowRight")) {
                    if (angle <= 0) {
                        range2 = false;
                    }
                    rotateArrowRight(range2, tpf);
                }
            }
        }
    };

    protected Geometry myTop(String name, Vector3f n) {
        Box b = new Box(40, 2, 4);
        Geometry bg = new Geometry(name, b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Textures/Orange.png"));
        bg.setMaterial(mat);

        Quaternion q = new Quaternion();
        q.fromAxes(n.cross(Vector3f.UNIT_Z), n, Vector3f.UNIT_Z);
        bg.setLocalRotation(q);
        return bg;
    }

    protected Geometry mySides(String name, Vector3f n) {
        Box b = new Box(52, 2, 4);
        Geometry bg = new Geometry(name, b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Textures/Orange.png"));
        bg.setMaterial(mat);

        Quaternion q = new Quaternion();
        q.fromAxes(n.cross(Vector3f.UNIT_Z), n, Vector3f.UNIT_Z);
        bg.setLocalRotation(q);
        return bg;
    }

    protected Geometry myPaddle(String name) {
        Box b = new Box(6, 1, 4);
        Geometry bg = new Geometry(name, b);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Ambient", ColorRGBA.Gray);
        mat.setColor("Diffuse", ColorRGBA.Gray);
        bg.setMaterial(mat);
        return bg;
    }

    protected Geometry myBall(String name) {
        Sphere b = new Sphere(30, 30, 2f);
        Geometry bg = new Geometry(name, b);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Ambient", ColorRGBA.Red);
        mat.setColor("Diffuse", ColorRGBA.Red);
        bg.setMaterial(mat);
        return bg;
    }

    protected Geometry enemyBalls(String name) {
        Sphere b = new Sphere(30, 30, 2f);
        Geometry bg = new Geometry(name, b);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Ambient", ColorRGBA.Green);
        mat.setColor("Diffuse", ColorRGBA.Green);
        bg.setMaterial(mat);
        return bg;
    }

    protected Geometry myArrow(String name) {
        Vector3f v = new Vector3f(10, 0, 0);
        Arrow arrow = new Arrow(v);
        gyArrow = new Geometry("Y", arrow);
        Material matA = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        matA.setColor("Color", ColorRGBA.White);
        gyArrow.setMaterial(matA);
        return gyArrow;
    }

    private void initKeys() {
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("ArrowLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("ArrowRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Launch", new KeyTrigger(KeyInput.KEY_SPACE));

        inputManager.addListener(actionListener, "Pause");
        inputManager.addListener(analogListener, "Left", "Right", "ArrowLeft", "ArrowRight");
        inputManager.addListener(actionListener, "Launch");
    }

    public void movePaddleLeft(boolean range, float tpf) {
        if (range == true) {
            paddle.move(-80 * tpf, 0, 0);
        }
    }

    public void moveInitLeft(boolean range, float tpf) {
        if (range == true) {
            paddle.move(-50 * tpf, 0, 0);
            myBall.move(-50 * tpf, 0, 0);
            gyArrow.move(-50 * tpf, 0, 0);
        }
    }

    public void movePaddleRight(boolean range, float tpf) {
        if (range == true) {
            paddle.move(80 * tpf, 0, 0);
        }
    }

    public void moveInitRight(boolean range, float tpf) {
        if (range == true) {
            paddle.move(50 * tpf, 0, 0);
            myBall.move(50 * tpf, 0, 0);
            gyArrow.move(50 * tpf, 0, 0);
        }
    }

    public void rotateArrowLeft(boolean range2, float tpf) {
        if (range2 == true) {
            gyArrow.rotate(0, 0, (FastMath.PI) / 360);
            angle++;
        }
    }

    public void rotateArrowRight(boolean range2, float tpf) {
        if (range2 == true) {
            gyArrow.rotate(0, 0, -(FastMath.PI) / 360);
            angle--;
        }
    }

    public void launchBall() {
        float Vx = FastMath.cos(angle * (FastMath.PI) / 360) * 60;
        float Vy = FastMath.sin(angle * (FastMath.PI) / 360) * 60;
        velocity = new Vector3f(Vx, Vy, 0);
    }

    public void getNewVelocity(Geometry ball1, Geometry ball2) {
        Vector3f n = ball1.getLocalTranslation().subtract(ball2.getLocalTranslation()).normalize();
        float projV = velocity.dot(n);
        Vector3f tan1 = velocity.subtract(n.mult(projV));
        velocity = tan1.add(n.mult(-projV));
    }

    public void win() {
        rootNode.detachChild(paddle);
        rootNode.detachChild(myBall);

        W = new BitmapText(guiFont);
        W.setSize(guiFont.getCharSet().getRenderedSize());
        W.setLocalTranslation(-20, W.getLineHeight() - 10, 0);
        W.setSize(8f);
        W.setText("YOU WIN !");
        rootNode.attachChild(W);
    }

    public void GO() {
        rootNode.detachChild(paddle);
        rootNode.detachChild(myBall);

        GO = new BitmapText(guiFont);
        GO.setSize(guiFont.getCharSet().getRenderedSize());
        GO.setLocalTranslation(-25, GO.getLineHeight() - 10, 0);
        GO.setSize(8f);
        GO.setText("GAME  OVER !");
        rootNode.attachChild(GO);
    }
}