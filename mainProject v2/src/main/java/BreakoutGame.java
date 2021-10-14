import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.input.UserAction;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BreakoutGame extends GameApplication{

    private int PADDLE_WIDTH = 150;
    private int BRICK_WIDTH = 50;

    private int PADDLE_SPEED = 5;
    private int BULLET_SPEED = 10;

    private Text playerScore;
    private Text playerLives;

    private Entity paddle;
    private Entity ball;
    private ArrayList<Entity> bricks = new ArrayList<>();
    private ArrayList<Entity> bullets = new ArrayList<>();


    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("test");
        settings.setVersion("1.0");
        settings.setWidth(1000);
        settings.setHeight(600);
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void initGame() {
        FXGL.getGameWorld().addEntityFactory(new GameEntityFactory());

        paddle = spawnPaddle(getAppWidth()/2, getAppHeight() - 50);
        ball = spawnBall(500, 300);

        for (int i = 0; i < 10; i++) {
            bricks.add(spawnBrick(100 + i*(BRICK_WIDTH + 20), getAppHeight() / 2 - 200 ));
            bricks.add(spawnBrick(100 + i*(BRICK_WIDTH + 20), getAppHeight() / 2 - 100));
        }
    }

    @Override
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

        //how can I ensure that I can't "spam" the space key?
        getInput().addAction(new UserAction("shootBullet") {
            @Override
            protected void onAction() {
                bullets.add(spawnBullet((int)(paddle.getX() + PADDLE_WIDTH/2), (int)(paddle.getY() - 100)));
            }
        }, KeyCode.SPACE);
    }

    @Override
    protected void initPhysics() {

//        getPhysicsWorld().addCollisionHandler(new CollisionHandler(Type.BRICK, Type.BALL) {
//            @Override
//            protected void onCollisionBegin(Entity brick, Entity ball) {
//                System.out.println("collision happened");
//                Point2D velocity = ball.getObject("velocity");
//                brick.removeFromWorld();
//
//                if (FXGLMath.randomBoolean()) {
//                    ball.setProperty("velocity", new Point2D(-velocity.getX(), velocity.getY()));
//                } else {
//                    ball.setProperty("velocity", new Point2D(velocity.getX(), -velocity.getY()));
//                }
//            }
//        });

//        onCollisionCollectible(Type.BALL, Type.PADDLE, (ball) -> {
//            Point2D velocity = ball.getObject("velocity");
//            if (FXGLMath.randomBoolean()) {
//                ball.setProperty("velocity", new Point2D(-velocity.getX(), -velocity.getY()));
//            } else {
//                ball.setProperty("velocity", new Point2D(velocity.getX(), -velocity.getY()));
//            }
//        });

//        onCollisionBegin(Type.BALL, Type.BRICK, (ball, brick) -> {
//            System.out.println("collision happened");
//            Point2D velocity = ball.getObject("velocity");
//            brick.removeFromWorld();
//
//            if (FXGLMath.randomBoolean()) {
//                ball.setProperty("velocity", new Point2D(-velocity.getX(), velocity.getY()));
//            } else {
//                ball.setProperty("velocity", new Point2D(velocity.getX(), -velocity.getY()));
//            }
//        });
    }

    @Override
    protected void onUpdate(double tpf) {
        Point2D velocity;
        if(getWorldProperties().getInt("lives") > 0 && bricks.size() > 0){
            velocity = ball.getObject("velocity");
            ball.translate(velocity);

            if (ball.getBottomY() == paddle.getY() && ball.getX() < paddle.getRightX() && ball.getRightX() > paddle.getX()) {
                System.out.println("happening");
                if (FXGLMath.randomBoolean()) {
                    ball.setProperty("velocity", new Point2D(-velocity.getX(), -velocity.getY()));
                } else {
                    ball.setProperty("velocity", new Point2D(velocity.getX(), -velocity.getY()));
                }
            }

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
                    ball = spawnBall((int)(Math.random() * getAppWidth()), (int)(Math.random() * getAppHeight()));
                }
            }

            brickCollidesBall();

            if(!bullets.isEmpty()){
                bulletMovements();
            }
        }
        else {
            endGame();
        }
    }

    private void endGame(){
        paddle.removeFromWorld();
        for(Entity b: bullets){
            b.removeFromWorld();
        }
        Text endText;
        if(bricks.size() ==0){
            endText = getUIFactoryService().newText("You Won :) ", Color.ORANGERED, 70);
        }
        else{
            endText = getUIFactoryService().newText("You Lost :( ", Color.ORANGERED, 70);
        }
        endText.setTranslateY(getAppHeight()/2);
        endText.setTranslateX(getAppWidth()/3);
        getGameScene().addUINodes(endText);
    }

    private void brickCollidesBall(){
        for(int i = 0; i < bricks.size(); i++){
            if (bricks.get(i).isColliding(ball)) {
                System.out.println("collision happened");

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
                    ball = spawnBall((int)(Math.random() * getAppWidth()), (int)(Math.random() * getAppHeight()));
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

                    }
                }
            }
        }
    }

    @Override
    protected void initUI() {
        Text scoreText = getUIFactoryService().newText("Score: ", Color.BLACK, 30);
        scoreText.setTranslateX(10);
        scoreText.setTranslateY(40);

        Text livesText = getUIFactoryService().newText("Lives: ", Color.BLACK, 30);
        livesText.setTranslateX(10);
        livesText.setTranslateY(70);


        playerScore = getUIFactoryService().newText(""  , Color.BLACK, 30);
        playerLives = getUIFactoryService().newText("" , Color.BLACK, 30);

        playerScore.setTranslateX(100);
        playerScore.setTranslateY(40);

        playerLives.setTranslateX(90);
        playerLives.setTranslateY(70);

        playerScore.textProperty().bind(getWorldProperties().intProperty("playerScore").asString());
        playerLives.textProperty().bind(getWorldProperties().intProperty("lives").asString());


        getGameScene().addUINodes(scoreText, playerScore, playerLives, livesText);

    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("playerScore", 0);
        vars.put("lives", 3);
    }

    private Entity spawnPaddle(int x, int y) {
        return entityBuilder()
                .at(x, y)
                //.viewWithBBox(new Rectangle(PADDLE_WIDTH, PADDLE_HEIGHT))
                .viewWithBBox("gradient_paddle.png")
                .collidable()
                .buildAndAttach();
    }

    private Entity spawnBall(int x, int y){
        return entityBuilder()
                .at(x, y)
                .viewWithBBox("circle.png") //new Circle(4BALL_SIZE)
                .with("velocity", new Point2D(BALL_SPEED, BALL_SPEED))
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }

    private Entity spawnBrick(int x, int y) {
        return entityBuilder()
                .at(x, y)
                .type(EntityType.BRICK)
                .viewWithBBox("blueBrick.png") //new Rectangle(BRICK_WIDTH, BRICK_HEIGHT, Color.RED)
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }

    private Entity spawnBullet(int x, int y){
        return entityBuilder()
                .at(x,y)
                .type(EntityType.BULLET)
                .viewWithBBox("newBullet.png")
                .with("bulletVel", new Point2D(0, -BULLET_SPEED))
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }
}
