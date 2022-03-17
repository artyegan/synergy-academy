package courses;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

import static meta.MetadataProvider.getMetadata;

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
        vertx.eventBus().consumer("get.courses.id", this::getCoursesByFilter);
    }

    private void getAllCourses(Message<JsonArray> msg) {
        getMetadata(coursesDB, vertx)
                .map(metadata ->
                        new JsonArray().add(
                                new JsonObject()
                                        .put("metadata", metadata)
                                        .put("keyword", coursesDB)
                        )
                )
                .flatMap(this::getAllCoursesRequest)
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private void getCoursesByFilter(Message<JsonArray> msg) {
        getMetadata(coursesDB, vertx)
                .map(metadata ->
                        msg.body().getJsonObject(0).put("metadata", metadata)
                                .put("keyword", coursesDB))
                .flatMap(this::getCoursesWithFilterRequest)
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private Single<JsonArray> getAllCoursesRequest(JsonArray msgBody) {
        return vertx.eventBus().<JsonArray>rxRequest("get.all.service", msgBody)
                .map(Message::body);
    }

    private Single<JsonArray> getCoursesWithFilterRequest(JsonObject msgBody) {
        return vertx.eventBus().<JsonArray>rxRequest("get.filter.service", new JsonArray().add(msgBody))
                .map(Message::body);
    }
}
