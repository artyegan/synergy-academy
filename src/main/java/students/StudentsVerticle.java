package students;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

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
        vertx.eventBus().consumer("get.students.id", this::getStudentById);
        vertx.eventBus().consumer("update.students.id", this::updateStudentById);

    }

    private void getAllStudents(Message<JsonArray> msg) {
        getAllStudentsRequest()
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private void getStudentById(Message<JsonObject> msg) {
        getStudentByIdRequest(msg.body().getString("studentId"))
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private void updateStudentById(Message<JsonObject> msg) {
        getStudentMetadata()
                .flatMapObservable(Observable::fromIterable)
                .map(obj -> (JsonObject) obj)
                .filter(json -> !json.getString("column_name").equals("studentid"))
                .toList()
                .map(JsonArray::new)
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

    private Single<JsonArray> getAllStudentsRequest() {
        return vertx.eventBus().<JsonArray>rxRequest("get.all.service",
                        new JsonArray()
                                .add(new JsonObject()
                                        .put("keyword", studentsDB)))
                .map(Message::body);
    }

    private Single<JsonObject> getStudentByIdRequest(String id) {
        return vertx.eventBus().<JsonObject>rxRequest("get.id.service",
                        new JsonObject()
                                .put("keyword", studentsDB)
                                .put("id", id))
                .map(Message::body);
    }

    private Single<JsonObject> updateStudentByIdRequest(JsonObject msgBody) {
        return vertx.eventBus().<JsonObject>rxRequest("update.id.service", msgBody)
                .map(Message::body);
    }

    private Single<JsonArray> getStudentMetadata() {
        return vertx.eventBus().<JsonArray>rxRequest("get.metadata.service",
                        new JsonArray().add(new JsonObject()
                                .put("function", "getcolumnbytablenamewithclassiferbool")
                                .put("keyword", studentsDB)))
                .map(Message::body);
    }
}
