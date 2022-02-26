package server;

import com.google.inject.Inject;
import io.vertx.core.Promise;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.openapi.RouterBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;

public class RouterBuilderVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(RouterBuilderVerticle.class);
    private final Handlers handlers;
    private final int port;

    @Inject
    public RouterBuilderVerticle(Handlers handlers, int port) {
        this.handlers = handlers;
        this.port = port;
    }

    @Override
    public void start(Promise<Void> startFuture) {
        RouterBuilder.rxCreate(vertx, "src/main/resources/rest.yaml")
                .subscribe(routerBuilder -> {

                            handlers.getHandlers()
                                    .forEach(pair -> routerBuilder.operation(pair.getValue0()).handler(pair.getValue1()));

                            vertx.createHttpServer()
                                    .requestHandler(routerBuilder.createRouter())
                                    .rxListen(port)
                                    .subscribe(res -> LOGGER.info("Server is running on " + port),
                                            LOGGER::error);
                        }
                        ,error -> {
                            LOGGER.error(error);
                            throw new RuntimeException(error);
                        }
                );
    }
}
