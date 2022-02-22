package server;

import com.google.inject.Inject;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class Handlers {
    private List<Pair<String, Handler<RoutingContext>>> handlers;
    private final Vertx vertx;

    @Inject
    public Handlers(Vertx vertx) {
        this.vertx = vertx;
    }

    public List<Pair<String, Handler<RoutingContext>>> getHandlers() {
        handlers = new ArrayList<>();

        handlers.add(Pair.with("getStudents", this::getStudents));

        return handlers;
    }

    private void getStudents(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.students.all", "")
                .subscribe(
                        result -> context.end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }
}
