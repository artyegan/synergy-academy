package server;

import com.google.inject.Inject;
import io.vertx.core.Promise;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.openapi.RouterBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RouterBuilderVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(RouterBuilderVerticle.class);
    private final Handlers handlers;

    @Inject
    public RouterBuilderVerticle(Handlers handlers) {
        this.handlers = handlers;
    }

    @Override
    public void start(Promise<Void> startFuture) {
        RouterBuilder.rxCreate(vertx, "src/main/resources/rest.yaml")
                .subscribe(routerBuilder -> {

                            handlers.getHandlers()
                                    .forEach(pair -> routerBuilder.operation(pair.getValue0()).handler(pair.getValue1()));

                            vertx.createHttpServer()
                                    .requestHandler(routerBuilder.createRouter())
                                    .rxListen(8080)
                                    .subscribe(res -> LOGGER.info("Server is running on 8080"),
                                            LOGGER::error);
                        }
                        ,error -> {
                            LOGGER.error(error);
                            throw new RuntimeException(error);
                        }
                );
    }
}
