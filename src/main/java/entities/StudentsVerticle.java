package entities;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

import static meta.MetadataProvider.getMetadata;
import static meta.MetadataProvider.getMetadataAndExtractId;

public class StudentsVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(StudentsVerticle.class);
    private final String studentsDB;

    private static final String KEYWORD = "keyword";
    private static final String METADATA = "metadata";

    @Inject
    public StudentsVerticle(String studentsDB) {
        this.studentsDB = studentsDB;
    }

    @Override
    public void start() {
        vertx.eventBus().consumer("get.students.all", this::getAllStudents);
        vertx.eventBus().consumer("add.student", this::addStudent);
        vertx.eventBus().consumer("get.students.filter", this::getStudentByFilter);
        vertx.eventBus().consumer("update.students.id", this::updateStudentById);
        vertx.eventBus().consumer("get.students.function", this::getStudentsWithFunction);
    }

    private void getAllStudents(Message<JsonArray> msg) {
        getMetadata(studentsDB, vertx)
                .map(metadata ->
                    new JsonArray().add(
                            new JsonObject()
                                    .put(METADATA, metadata)
                                    .put(KEYWORD, studentsDB)
                    )
                )
                .flatMap(this::getAllStudentsRequest)
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private void addStudent(Message<JsonObject> msg) {
        getMetadataAndExtractId(studentsDB, vertx)
                .map(metadata ->
                        msg.body().put(METADATA, metadata)
                                .put(KEYWORD, studentsDB))
                .flatMap(this::addStudentRequest)
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private void getStudentByFilter(Message<JsonArray> msg) {
        getMetadata(studentsDB, vertx)
                .map(metadata ->
                        msg.body().getJsonObject(0).put(METADATA, metadata)
                                .put(KEYWORD, studentsDB))
                .flatMap(this::getStudentsWithFilterRequest)
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private void getStudentsWithFunction(Message<JsonArray> msg) {
                getStudentsWithFunctionRequest(msg.body())
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private void updateStudentById(Message<JsonObject> msg) {
        getMetadataAndExtractId(studentsDB, vertx)
                .map(metadata ->
                        msg.body().put(METADATA, metadata)
                                .put(KEYWORD, studentsDB))
                .flatMap(this::updateStudentByIdRequest)
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private Single<JsonArray> getAllStudentsRequest(JsonArray msgBody) {
        return vertx.eventBus().<JsonArray>rxRequest("get.all.service", msgBody)
                .map(Message::body);
    }

    private Single<JsonArray> getStudentsWithFilterRequest(JsonObject msgBody) {
        return vertx.eventBus().<JsonArray>rxRequest("get.filter.service", new JsonArray().add(msgBody))
                .map(Message::body);
    }

    private Single<JsonObject> updateStudentByIdRequest(JsonObject msgBody) {
        return vertx.eventBus().<JsonObject>rxRequest("update.id.service", msgBody)
                .map(Message::body);
    }

    private Single<JsonObject> addStudentRequest(JsonObject msgBody) {
        return vertx.eventBus().<JsonObject>rxRequest("add.service", msgBody)
                .map(Message::body);
    }

    private Single<JsonArray> getStudentsWithFunctionRequest(JsonArray msgBody) {
        return vertx.eventBus().<JsonArray>rxRequest("get.function.service", msgBody)
                .map(Message::body);
    }
}
