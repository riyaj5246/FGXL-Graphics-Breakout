import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.input.UserAction;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Game extends GameApplication{

    //preset variables representing game settings (only ball speed changes)
    private int PADDLE_WIDTH = 150;
    private int BRICK_WIDTH = 50;
    private int PADDLE_SPEED = 5;
    private int BALL_SPEED = 2;
    private int BULLET_SPEED = 10;

    //Text variables to store game info --> linked to gameWorld UI
    private Text playerScore;
    private Text playerLives;
    private Text playerBullet;
    private Text levelText;

    private boolean isBulletEnabled = true; //enables shooting of bullets
    private boolean isBallEnabled = false; //enables ball movement
    private boolean isChangeLevelEnabled = false; //enables the keys 'R' and 'N' to change level
    private boolean isGameEnded = true; //enables ending game and resetting screen

    private Entity paddle; //moves left/right and hits ball
    private Entity ball; //moves all across the screen and collides with all entities
    private ArrayList<Entity> bricks = new ArrayList<>(); //list of bricks which are destroyed in collision with ball
    private ArrayList<Entity> bullets = new ArrayList<>(); //list of bullets which shoot from paddle

    private ArrayList<Levels> levels = new ArrayList<>(); //contains 5 Levels objects w/ each level
    private int currentLevel = 0; //index of starting level in levels ArrayList
    private Text endText; //text displayed at the end of a round, either to level up, end, or repeat

    //initializes different entity types
    public enum Type {
        PADDLE, BALL, BRICK, BULLET
    }

    @Override //sets initialization settings - title, version, width, height
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Breakout Game");
        settings.setVersion("1.0");
        settings.setWidth(1000);
        settings.setHeight(600);
        settings.setApplicationMode(ApplicationMode.RELEASE);
    }

    @Override //initializes game by setting levels, spawning a ball, bricks, paddle, and bullets
    protected void initGame() {
        Entity backdrop = addBackground();

        paddle = spawnPaddle(getAppWidth()/2, getAppHeight() - 50);

        for (int i = 0; i < 5; i++) {
            levels.add(new Levels(i + 1));
        }

        currentLevel = 0;
        int x = currentLevel;
        ball = spawnBall(500, 500,levels.get(currentLevel).getBallSpeed());

        for(int i = 0; i < levels.get(x).returnX().size(); i++){
            bricks.add(spawnBrick(levels.get(x).returnX().get(i), levels.get(x).returnY().get(i)));
        }
        getWorldProperties().increment("bullets", levels.get(currentLevel).getNumBullets());

    }

    @Override //controls all key inputs (LEFT, RIGHT, SPACE, ENTER, R, N)
    protected void initInput() {
        getInput().addAction(new UserAction("paddleLeft") {
            @Override
            protected void onAction() {
                if(paddle.getX() > 0){
                    paddle.translateX(-PADDLE_SPEED);
                }
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("paddleRight") {
            @Override
            protected void onAction() {
                if(paddle.getRightX() < 1000){
                    paddle.translateX(PADDLE_SPEED);
                }
            }
        }, KeyCode.RIGHT);
        // ...

        getInput().addAction(new UserAction("shootBullet") {
            @Override
            protected void onAction() {
                if(isBulletEnabled && getWorldProperties().getInt("bullets") > 0){
                    isBulletEnabled = false;
                    getWorldProperties().increment("bullets", -1);
                    bullets.add(spawnBullet((int)(paddle.getX() + PADDLE_WIDTH/2), (int)(paddle.getY() - 100)));

                    getGameTimer().runOnceAfter(() -> {
                        isBulletEnabled = true;
                    }, Duration.seconds(1));
                }
            }
        }, KeyCode.SPACE);

        getInput().addAction(new UserAction("enableBallMovement") {
            @Override
            public void onAction(){
                isBallEnabled = true;
            }
        }, KeyCode.ENTER);

        getInput().addAction(new UserAction("restartLevel") {
            @Override
            public void onAction(){
                if(isChangeLevelEnabled){
                    setLevel();
                    ball = spawnBall((int)((Math.random() * (getAppWidth()) - 200) + 100), (int)(Math.random() * (getAppHeight() - 500) ) + 300, levels.get(currentLevel).getBallSpeed());
                    getWorldProperties().setValue("lives", 3);

                    setNode(2);
                    isGameEnded = true;
                    isChangeLevelEnabled = false;
                }
            }
        }, KeyCode.R);

        getInput().addAction(new UserAction("nextLevel") {
            @Override
            public void onAction(){
                if(isChangeLevelEnabled && currentLevel != 4){
                    System.out.println("level up!!!");
                    setNode(2);
                    currentLevel++;
                    getWorldProperties().increment("level", +1);
                    getWorldProperties().setValue("lives", 3);

                    getGameScene().clearUINodes();
                    initUI();

                    setLevel();

                    ball = spawnBall((int)((Math.random() * (getAppWidth()) - 200) + 100), (int)(Math.random() * (getAppHeight() - 500) ) + 300, levels.get(currentLevel).getBallSpeed());

                    isGameEnded = true;
                    isChangeLevelEnabled = false;
                }
            }
        }, KeyCode.N);
    }

    //sets game when leveling up or playing a level (used every time except initialization)
    private void setLevel(){
        int x = currentLevel;

        for(int i = 0; i < bricks.size(); i++){
            bricks.get(i).removeFromWorld();
        }
        bricks.clear();

        for(int i = 0; i < levels.get(x).returnX().size(); i++){
            //System.out.println("brick" + levels.get(x).returnX().get(i) + ", " + levels.get(x).returnY().get(i));
            bricks.add(spawnBrick(levels.get(x).returnX().get(i), levels.get(x).returnY().get(i)));
        }
        getWorldProperties().setValue("bullets", levels.get(currentLevel).getNumBullets());
    }

    @Override //controls velocity and movements of the ball, and resulting 
    protected void onUpdate(double tpf) {
      //  System.out.println("collision function");
        Point2D velocity;
        if(getWorldProperties().getInt("lives") > 0 && bricks.size() > 0){
            if(isBallEnabled){
                velocity = ball.getObject("velocity");
                ball.translate(velocity);

                if(ball.isColliding(paddle)){
                    System.out.println("happening");
                    if (FXGLMath.randomBoolean()) {
                        ball.setProperty("velocity", new Point2D(-velocity.getX(), -velocity.getY()));
                    } else {
                        ball.setProperty("velocity", new Point2D(velocity.getX(), -velocity.getY()));
                    }
                }

//                if (ball.getBottomY() == paddle.getY() && ball.getX() < paddle.getRightX() && ball.getRightX() > paddle.getX()) {
//                    System.out.println("happening");
//                    if (FXGLMath.randomBoolean()) {
//                        ball.setProperty("velocity", new Point2D(-velocity.getX(), -velocity.getY()));
//                    } else {
//                        ball.setProperty("velocity", new Point2D(velocity.getX(), -velocity.getY()));
//                    }
//                }

                if (ball.getX() <= 0) {
                    ball.setProperty("velocity", new Point2D(-velocity.getX(), velocity.getY()));
                }

                if (ball.getRightX() >= getAppWidth()) {
                    ball.setProperty("velocity", new Point2D(-velocity.getX(), velocity.getY()));
                }

                if (ball.getY() <= 0) {
                    ball.setProperty("velocity", new Point2D(velocity.getX(), -velocity.getY()));
                }

                if (ball.getBottomY() >= getAppHeight()) {
                    getWorldProperties().increment("lives", -1);
                    ball.removeFromWorld();
                    if(getWorldProperties().getInt("lives") > 0 ){
                        isBallEnabled = false;
                        ball = spawnBall((int)((Math.random() * (getAppWidth()) - 200) + 100), (int)(Math.random() * (getAppHeight() - 500) ) + 300, levels.get(currentLevel).getBallSpeed());
                    }
                }

                brickCollidesBall();

                if(!bullets.isEmpty()){
                    bulletMovements();
                }
            }
        }
        else {
            if(isGameEnded){
                endGame();
                isGameEnded = false;
            }
        }
    }

    //called when either all lives are lost or all bricks are gone --> restarts settings and displays message
    private void endGame(){
        System.out.println("continuously");
        for(Entity b: bullets){
            b.removeFromWorld();
        }
        bullets.clear();

        ball.removeFromWorld();
        isBallEnabled = false;
        if(bricks.size() ==0){
            if(currentLevel !=4){
                endText = getUIFactoryService().newText("You Won :) Press 'N' for next level", Color.ORANGERED, 50);
                isChangeLevelEnabled = true;
            }
            else{
                endText = getUIFactoryService().newText("You Won and Have Completed All Levels! ", Color.ORANGERED, 50);
            }
        }
        else{
            endText = getUIFactoryService().newText("You Lost :( Press 'R' To Retry", Color.ORANGERED, 60);
            isChangeLevelEnabled = true;
        }
        endText.setVisible(true);
        endText.setTranslateY(getAppHeight()/2);
        endText.setTranslateX(getAppWidth()/10);

        setNode(1);
    }

    //called to add/remove the end-of-game text
    private void setNode(int option){
        if(option ==1){
            getGameScene().addUINode(endText);
        }
        else if(option ==2){
            //System.out.println(endText);
           // System.out.println(getGameScene().getUINodes());
           // System.out.println("---");
            getGameScene().removeUINode(endText);
          //  System.out.println(getGameScene().getUINodes());

        }
    }

    //collision code for ball and brick --> ball changes velocity, brick is removed, points are added
    private void brickCollidesBall(){
        for(int i = 0; i < bricks.size(); i++){
            if (bricks.get(i).isColliding(ball)) {
                //System.out.println("collision happened");
                Point2D speed = ball.getObject("velocity");
                bricks.get(i).removeFromWorld();
                bricks.remove(i);

                getWorldProperties().increment("playerScore", +1);

                if (FXGLMath.randomBoolean()) {
                    ball.setProperty("velocity", new Point2D(-speed.getX(), speed.getY()));
                }
                else {
                    ball.setProperty("velocity", new Point2D(speed.getX(), -speed.getY()));
                }
            }
        }
    }

    //configures bullet movements and ensures that necessary steps take place in case of collision
    private void bulletMovements(){
        Point2D bulletVel;
        for(int i = 0; i < bullets.size(); i++){
            bulletVel = bullets.get(i).getObject("bulletVel");
            bullets.get(i).translate(bulletVel);

            if(bullets.get(i).isColliding(ball)){
                getWorldProperties().increment("lives", -1);

                bullets.get(i).removeFromWorld();
                bullets.remove(i);

                ball.removeFromWorld();
                if(getWorldProperties().getInt("lives") > 0 ){
                    ball = spawnBall((int)(Math.random() * getAppWidth()), (int)(Math.random() * getAppHeight()),levels.get(currentLevel).getBallSpeed());
                }
            }
            else {
                for (int j = 0; j < bricks.size(); j++) {

                    if (bullets.get(i).isColliding(bricks.get(j))) {
                        bricks.get(j).removeFromWorld();
                        bricks.remove(j);

                        bullets.get(i).removeFromWorld();
                        bullets.remove(i);
                        getWorldProperties().increment("playerScore", +1);
                        break;
                    }
                }
            }
        }
    }

    @Override //sets text for score, lives, bullets, and level
    protected void initUI()  {
        Text scoreText = getUIFactoryService().newText("Bricks: ", Color.YELLOWGREEN, 30);
        scoreText.setTranslateX(10);
        scoreText.setTranslateY(40);

        Text livesText = getUIFactoryService().newText("Lives: ", Color.YELLOWGREEN, 30);
        livesText.setTranslateX(10);
        livesText.setTranslateY(70);

        Text bulletsText = getUIFactoryService().newText("Bullets: ", Color.YELLOWGREEN, 30);
        bulletsText.setTranslateX(10);
        bulletsText.setTranslateY(100);

        String levelsString = "Level: " + (currentLevel + 1);
        levelText = getUIFactoryService().newText(levelsString, Color.YELLOWGREEN, 30);
        levelText.setTranslateX(850);
        levelText.setTranslateY(40);

        playerScore = getUIFactoryService().newText(""  , Color.YELLOWGREEN, 30);
        playerScore.setTranslateX(110);
        playerScore.setTranslateY(40);

        playerLives = getUIFactoryService().newText("" , Color.YELLOWGREEN, 30);
        playerLives.setTranslateX(90);
        playerLives.setTranslateY(70);

        playerBullet = getUIFactoryService().newText("" , Color.YELLOWGREEN, 30);
        playerBullet.setTranslateX(120);
        playerBullet.setTranslateY(100);

        playerScore.textProperty().bind(getWorldProperties().intProperty("playerScore").asString());
        playerLives.textProperty().bind(getWorldProperties().intProperty("lives").asString());
        playerBullet.textProperty().bind(getWorldProperties().intProperty("bullets").asString());

        getGameScene().addUINodes(scoreText, playerScore, playerLives, livesText, bulletsText, playerBullet, levelText);
    }

    @Override //initializes variables needed to track score, lives, bullets, and level
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("playerScore", 0);
        vars.put("lives", 3);
        vars.put("bullets", 0);
        vars.put("level", 1);
    }

    //method to create and add paddle to world
    private Entity spawnPaddle(int x, int y) {
        return entityBuilder()
                .at(x, y)
                .type(Type.PADDLE)
                //.viewWithBBox(new Rectangle(PADDLE_WIDTH, PADDLE_HEIGHT))
                .viewWithBBox("gradient_paddle.png")
                .collidable()
                .buildAndAttach();
    }

    //method to add plain black backdrop to world
    private Entity addBackground(){
        return entityBuilder()
                .at(0, 0)
                .viewWithBBox(new Rectangle(1000, 600, Color.BLACK))
                .buildAndAttach();


    }

    //method needed to spawn a new ball
    private Entity spawnBall(int x, int y, double ballSpeed){
        return entityBuilder()
                .at(x, y)
                .type(Type.BALL)
                .viewWithBBox("circle.png") //new Circle(4BALL_SIZE)
                .with("velocity", new Point2D(ballSpeed, ballSpeed))
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }

    //method needed to create and add brick to world
    private Entity spawnBrick(int x, int y) {
        return entityBuilder()
                .at(x, y)
                .type(Type.BRICK)
                .viewWithBBox("blueBrick.png") //new Rectangle(BRICK_WIDTH, BRICK_HEIGHT, Color.RED)
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }

    //method to spawn and add bullets to world
    private Entity spawnBullet(int x, int y){
        return entityBuilder()
                .at(x,y)
                .type(Type.BULLET)
                .viewWithBBox("newBullet.png")
                .with("bulletVel", new Point2D(0, -BULLET_SPEED))
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
