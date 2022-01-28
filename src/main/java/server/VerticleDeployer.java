package server;

import com.google.inject.Inject;
import io.reactivex.Observable;
import io.vertx.core.Verticle;
import io.vertx.reactivex.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class VerticleDeployer {
    private static final Logger LOGGER = LogManager.getLogger(VerticleDeployer.class);

    private final Vertx vertx;
    private final Set<Verticle> vertices;

    @Inject
    public VerticleDeployer(Vertx vertx, Set<Verticle> vertices) {
        this.vertx = vertx;
        this.vertices = vertices;
    }

    public void deployVertices() {
        Observable.fromIterable(vertices)
                .flatMapSingle(vertx::rxDeployVerticle)
                .ignoreElements()
                .subscribe(
                        () -> LOGGER.info("Verticle Deployment Succeed"),
                        LOGGER::error
                );
    }
}
