package server;

import com.google.inject.Inject;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.http.HttpServerResponse;
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

        handlers.add(Pair.with("getAllStudents", this::getAllStudents));
        handlers.add(Pair.with("getStudentById", this::getStudentById));
        handlers.add(Pair.with("getAllCourses", this::getAllCourses));

        return handlers;
    }

    private HttpServerResponse addResponseHeaders(RoutingContext context) {
        return context.response().setStatusCode(200).setStatusMessage("OK")
                .putHeader("content-type", "application/json")
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .putHeader("Access-Control-Allow-Credentials", "true");
    }

    private void getAllStudents(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.students.all", "")
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getStudentById(RoutingContext context) {
        vertx.eventBus()
                .<JsonObject>rxRequest("get.students.id", new JsonObject().put("studentId", context.pathParam("studentId")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getAllCourses(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.courses.all", "")
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }
}
