package server;

import com.google.inject.Inject;
import io.vertx.core.Promise;
import io.vertx.ext.web.openapi.ErrorType;
import io.vertx.ext.web.openapi.RouterBuilderException;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.openapi.RouterBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RouterBuilderVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(RouterBuilderVerticle.class);
    private final Handlers handlers;
    private final int port;
    private final String restUrl;

    @Inject
    public RouterBuilderVerticle(Handlers handlers, int port, String restUrl) {
        this.handlers = handlers;
        this.port = port;
        this.restUrl = restUrl;
    }

    @Override
    public void start(Promise<Void> startFuture) {
        RouterBuilder.rxCreate(vertx, restUrl)
                .subscribe(routerBuilder -> {
                            handlers.getHandlers()
                                    .forEach(pair -> routerBuilder.operation(pair.getValue0()).handler(pair.getValue1()));

                            vertx.createHttpServer()
                                    .requestHandler(routerBuilder.createRouter())
                                    .rxListen(port)
                                    .subscribe(res -> LOGGER.info(String.format("Server is running on %d", port)),
                                            LOGGER::error);
                        }
                        ,error -> {
                            LOGGER.error(error);
                            throw new RouterBuilderException(error.getMessage(), ErrorType.INVALID_SPEC, error);
                        }
                );
    }
}
