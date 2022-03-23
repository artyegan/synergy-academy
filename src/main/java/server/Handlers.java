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
        handlers.add(Pair.with("addStudent", this::addStudent));
        handlers.add(Pair.with("getStudentById", this::getStudentById));
        handlers.add(Pair.with("updateStudentById", this::updateStudentById));
        handlers.add(Pair.with("getAllCourses", this::getAllCourses));
        handlers.add(Pair.with("addCourse", this::addCourse));
        handlers.add(Pair.with("getCourseById", this::getCourseById));
        handlers.add(Pair.with("updateCourseById", this::updateCourseById));
        handlers.add(Pair.with("getExamsByCourseId", this::getExamsByCourseId));
        handlers.add(Pair.with("addExam", this::addExam));
        handlers.add(Pair.with("getExamById", this::getExamById));
        handlers.add(Pair.with("updateExamById", this::updateExamById));
        handlers.add(Pair.with("getClassifier", this::getClassifier));
        handlers.add(Pair.with("getConfig", this::getConfig));

        return handlers;
    }

    private HttpServerResponse addResponseHeaders(RoutingContext context) {
        return context.response().setStatusCode(200).setStatusMessage("OK")
                .putHeader("content-type", "application/json")
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT")
                .putHeader("Access-Control-Allow-Credentials", "true");
    }

    private void getAllStudents(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.students.all", new JsonArray())
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void addStudent(RoutingContext context) {
        vertx.eventBus()
                .<JsonObject>rxRequest("add.student",
                        new JsonObject().put("data", context.getBodyAsJson()))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getStudentById(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.students.filter", new JsonArray().add(new JsonObject()
                        .put("value", context.pathParam("studentId"))
                        .put("filterColumn", "studentid")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void updateStudentById(RoutingContext context) {
        vertx.eventBus()
                .<JsonObject>rxRequest("update.students.id",
                        new JsonObject()
                                .put("id", context.pathParam("studentId"))
                                .put("data", context.getBodyAsJson()))
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

    private void addCourse(RoutingContext context) {
        vertx.eventBus()
                .<JsonObject>rxRequest("add.course",
                        new JsonObject().put("data", context.getBodyAsJson()))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getCourseById(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.courses.filter", new JsonArray().add(new JsonObject()
                        .put("value", context.pathParam("courseId"))
                        .put("filterColumn", "courseid")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void updateCourseById(RoutingContext context) {
        vertx.eventBus()
                .<JsonObject>rxRequest("update.courses.id",
                        new JsonObject()
                                .put("id", context.pathParam("courseId"))
                                .put("data", context.getBodyAsJson()))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getExamsByCourseId(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.exams.filter", new JsonArray().add(new JsonObject()
                        .put("value", context.pathParam("courseId"))
                        .put("filterColumn", "courseid")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void addExam(RoutingContext context) {
        vertx.eventBus()
                .<JsonObject>rxRequest("add.exam",
                        new JsonObject().put("data", context.getBodyAsJson()))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getExamById(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.exams.filter", new JsonArray().add(new JsonObject()
                        .put("value", context.pathParam("examId"))
                        .put("filterColumn", "courseexamid")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void updateExamById(RoutingContext context) {
        vertx.eventBus()
                .<JsonObject>rxRequest("update.exams.id",
                        new JsonObject()
                                .put("id", context.pathParam("examId"))
                                .put("data", context.getBodyAsJson()))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getClassifier(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.classifier",
                        new JsonArray().add(
                                new JsonObject()
                                        .put("keyword", "c_" + context.pathParam("tableName")))
                )
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getConfig(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.config", new JsonArray().add(new JsonObject()
                        .put("value", context.pathParam("type"))
                        .put("filterColumn", "type")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }
}
