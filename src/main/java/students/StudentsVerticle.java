package students;

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

    @Inject
    public StudentsVerticle(String studentsDB) {
        this.studentsDB = studentsDB;
    }

    @Override
    public void start() {
        vertx.eventBus().consumer("get.students.all", this::getAllStudents);
        vertx.eventBus().consumer("add.student", this::addStudent);
        vertx.eventBus().consumer("get.students.id", this::getStudentByFilter);
        vertx.eventBus().consumer("update.students.id", this::updateStudentById);

    }

    private void getAllStudents(Message<JsonArray> msg) {
        getMetadata(studentsDB, vertx)
                .map(metadata ->
                    new JsonArray().add(
                            new JsonObject()
                                    .put("metadata", metadata)
                                    .put("keyword", studentsDB)
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
                        msg.body().put("metadata", metadata)
                                .put("keyword", studentsDB))
                .flatMap(this::addStudentRequest)
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private void getStudentByFilter(Message<JsonObject> msg) {
        getMetadata(studentsDB, vertx)
                .map(metadata ->
                        msg.body().put("metadata", metadata)
                                .put("keyword", studentsDB))
                .flatMap(this::getStudentByIdRequest)
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
                        msg.body().put("metadata", metadata)
                                .put("keyword", studentsDB))
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

    private Single<JsonObject> getStudentByIdRequest(JsonObject msgBody) {
        return vertx.eventBus().<JsonObject>rxRequest("get.filter.service", msgBody)
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
}
