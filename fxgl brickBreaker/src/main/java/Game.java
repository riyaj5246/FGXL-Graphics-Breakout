import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.EntityBuilder;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Game extends GameApplication{

    private Entity block;
    private Entity ball;
    private Entity block2;

    //entity names are block and ball, and they are different types (even though there are, currently, no differences between behaviors of both
    //all keyboard input is inside the initInput() method
    //in order to constrain bounds, add an if-statement within each segment inside the initInput() method
    //use .getY(), .getX(), .getRightX(), .getBottomY();

    public enum Type {
        BLOCK, BALL
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("test");
        settings.setVersion("1.0");
        settings.setWidth(1000);
        settings.setHeight(600);
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
    }

    @Override
    protected void initGame() {
        spawnShapes();
       // FXGL.getAssetLoader().loadTexture(block, 50, 50);


    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Up") {
            @Override
            protected void onAction() {
                if(block.getY() > 0){
                    block.translateY(-5);
                }
            }
        }, KeyCode.UP);

        getInput().addAction(new UserAction("Down") {
            @Override
            protected void onAction() {
                if(block.getBottomY() < 600){
                    block.translateY(5);
                }
            }
        }, KeyCode.DOWN);

        getInput().addAction(new UserAction("Left") {
            @Override
            protected void onAction() {
                if(block.getX() > 0){
                    block.translateX(-5);
                }
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("Right") {
            @Override
            protected void onAction() {
                if(block.getRightX() < 1000){
                    block.translateX(5);
                }
            }
        }, KeyCode.RIGHT);
        // ...

        getInput().addAction(new UserAction("Up1") {
            @Override
            protected void onAction() {
                if(ball.getY() > 0){
                    ball.translateY(-5);
                }
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Down1") {
            @Override
            protected void onAction() {
                if(ball.getBottomY() < 600){
                    ball.translateY(5);
                }
            }
        }, KeyCode.S);

        getInput().addAction(new UserAction("Left1") {
            @Override
            protected void onAction() {
                if(ball.getX() > 0){
                    ball.translateX(-5);
                }
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("Right1") {
            @Override
            protected void onAction() {
                if(ball.getRightX() < 1000){
                    ball.translateX(5);
                }
            }
        }, KeyCode.D);
    }

    @Override
    protected void initPhysics() {
        onCollisionBegin(Type.BALL, Type.BLOCK, (ball, block) -> {
            // remove the collided droplet from the game
            ball.removeFromWorld();
            block.removeFromWorld();

        });

//        PhysicsComponent physics = new PhysicsComponent();
//        physics.setBodyType(BodyType.DYNAMIC);
//
//// these are direct jbox2d objects, so we don't actually introduce new API
//        FixtureDef fd = new FixtureDef();
//        fd.setDensity(0.7f);
//        fd.setRestitution(0.3f);
//        physics.setFixtureDef(fd);
//
//        block2.addComponent(physics);
//
//        getGameWorld().addEntity(block2);
    }

    private void spawnShapes() {

        block = entityBuilder()
                .type(Type.BLOCK)
                .at(getAppWidth() / 2, getAppHeight()/100)
                .viewWithBBox("block.png")
                .collidable()
                .buildAndAttach();

        ball = entityBuilder()
                .type(Type.BALL)
                .at( 100, 100)
                .viewWithBBox("block.png")
                .collidable()
                .buildAndAttach();

//        block2 = entityBuilder()
//                .type(Type.BLOCK)
//                .at(getAppWidth() / 2, getAppHeight()/100)
//                .viewWithBBox("block.png")
//                .collidable()
//                .buildAndAttach();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
