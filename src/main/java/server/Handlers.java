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
    private final Vertx vertx;
    private static final String VALUE = "value";
    private static final String FILTERCOLUMN = "filterColumn";
    private static final String ID = "id";
    private static final String DATA = "data";

    @Inject
    public Handlers(Vertx vertx) {
        this.vertx = vertx;
    }

    public List<Pair<String, Handler<RoutingContext>>> getHandlers() {
        List<Pair<String, Handler<RoutingContext>>> handlersList = new ArrayList<>();

        handlersList.add(Pair.with("getAllStudents", this::getAllStudents));
        handlersList.add(Pair.with("addStudent", this::addStudent));
        handlersList.add(Pair.with("getStudentById", this::getStudentById));
        handlersList.add(Pair.with("updateStudentById", this::updateStudentById));
        handlersList.add(Pair.with("getAllCourses", this::getAllCourses));
        handlersList.add(Pair.with("addCourse", this::addCourse));
        handlersList.add(Pair.with("getCourseById", this::getCourseById));
        handlersList.add(Pair.with("updateCourseById", this::updateCourseById));
        handlersList.add(Pair.with("getExamsByCourseId", this::getExamsByCourseId));
        handlersList.add(Pair.with("getStudentsByCourseId", this::getStudentsByCourseId));
        handlersList.add(Pair.with("getEducationProcessByCourseId", this::getEducationProcessByCourseId));
        handlersList.add(Pair.with("getEducationProcessById", this::getEducationProcessById));
        handlersList.add(Pair.with("getGradesByEducationProcessId", this::getGradesByEducationProcessId));
        handlersList.add(Pair.with("addExam", this::addExam));
        handlersList.add(Pair.with("getExamById", this::getExamById));
        handlersList.add(Pair.with("updateExamById", this::updateExamById));
        handlersList.add(Pair.with("getStudentsByExamId", this::getStudentsByExamId));
        handlersList.add(Pair.with("getResultsByExamId", this::getResultsByExamId));
        handlersList.add(Pair.with("getReportsWithFunction", this::getReportsWithFunction));
        handlersList.add(Pair.with("getClassifier", this::getClassifier));
        handlersList.add(Pair.with("getConfig", this::getConfig));

        return handlersList;
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
                        new JsonObject().put(DATA, context.getBodyAsJson()))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getStudentById(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.students.filter", new JsonArray().add(new JsonObject()
                        .put(VALUE, context.pathParam("studentId"))
                        .put(FILTERCOLUMN, "studentid")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void updateStudentById(RoutingContext context) {
        vertx.eventBus()
                .<JsonObject>rxRequest("update.students.id",
                        new JsonObject()
                                .put(ID, context.pathParam("studentId"))
                                .put(DATA, context.getBodyAsJson()))
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
                        new JsonObject().put(DATA, context.getBodyAsJson()))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getCourseById(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.courses.filter", new JsonArray().add(new JsonObject()
                        .put(VALUE, context.pathParam("courseId"))
                        .put(FILTERCOLUMN, "courseid")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void updateCourseById(RoutingContext context) {
        vertx.eventBus()
                .<JsonObject>rxRequest("update.courses.id",
                        new JsonObject()
                                .put(ID, context.pathParam("courseId"))
                                .put(DATA, context.getBodyAsJson()))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getExamsByCourseId(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.exams.filter", new JsonArray().add(new JsonObject()
                        .put(VALUE, context.pathParam("courseId"))
                        .put(FILTERCOLUMN, "courseid")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getStudentsByCourseId(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.students.function", new JsonArray().add(new JsonObject()
                        .put("keyword", context.pathParam("courseId"))
                        .put("function", "getstudentsbycourseid")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getReportsWithFunction(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.reports", new JsonArray())
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getEducationProcessByCourseId(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.educationprocess.function", new JsonArray().add(new JsonObject()
                        .put("keyword", context.pathParam("courseId"))
                        .put("function", "geteducationprocesssubjects")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getEducationProcessById(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.educationprocess.function", new JsonArray().add(new JsonObject()
                        .put("keyword", context.pathParam("educationProcessId"))
                        .put("function", "geteducationprocesssubjectbyid")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getGradesByEducationProcessId(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.educationprocess.function", new JsonArray().add(new JsonObject()
                        .put("keyword", context.pathParam("educationProcessId"))
                        .put("function", "geteducationprocessgradesbyid")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void addExam(RoutingContext context) {
        vertx.eventBus()
                .<JsonObject>rxRequest("add.exam",
                        new JsonObject().put(DATA, context.getBodyAsJson()))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getExamById(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.exams.filter", new JsonArray().add(new JsonObject()
                        .put(VALUE, context.pathParam("examId"))
                        .put(FILTERCOLUMN, "courseexamid")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getStudentsByExamId(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.students.function", new JsonArray().add(new JsonObject()
                        .put("keyword", context.pathParam("examId"))
                        .put("function", "getstudentsbyexamid")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void getResultsByExamId(RoutingContext context) {
        vertx.eventBus()
                .<JsonArray>rxRequest("get.results.function", new JsonArray().add(new JsonObject()
                        .put("keyword", context.pathParam("examId"))
                        .put("function", "getresultsbyexamid")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }

    private void updateExamById(RoutingContext context) {
        vertx.eventBus()
                .<JsonObject>rxRequest("update.exams.id",
                        new JsonObject()
                                .put(ID, context.pathParam("examId"))
                                .put(DATA, context.getBodyAsJson()))
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
                        .put(VALUE, context.pathParam("type"))
                        .put(FILTERCOLUMN, "type")))
                .subscribe(
                        result -> addResponseHeaders(context).end(result.body().encodePrettily()),
                        error -> context.response().setStatusCode(500).end(error.getMessage())
                );
    }
}
