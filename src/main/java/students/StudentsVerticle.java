package students;

import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

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
    }

    private void getAllStudents(Message<JsonArray> msg) {
        getAllStudentsRequest()
                .subscribe(msg::reply,
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
}
