package courses;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

public class CoursesVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(CoursesVerticle.class);
    private final String coursesDB;

    @Inject
    public CoursesVerticle(String coursesDB) {
        this.coursesDB = coursesDB;
    }

    @Override
    public void start() {
        vertx.eventBus().consumer("get.courses.all", this::getAllCourses);
    }

    private void getAllCourses(Message<JsonArray> msg) {
        getAllCoursesRequest()
                .subscribe(msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private Single<JsonArray> getAllCoursesRequest() {
        return vertx.eventBus().<JsonArray>rxRequest("get.all.service",
                        new JsonArray()
                                .add(new JsonObject()
                                        .put("keyword", coursesDB)))
                .map(Message::body);
    }
}
