import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import javafx.geometry.Point2D;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class GameEntityFactory {
    private int BALL_SPEED = 2;

    @Spawns("ball")
    public Entity spawnBall(SpawnData data){
        return entityBuilder()
                .from(data)
                .viewWithBBox("circle.png") //new Circle(4BALL_SIZE)
                .with("velocity", new Point2D(BALL_SPEED, BALL_SPEED))
                .with(new CollidableComponent(true))
                .build();
    }
}

