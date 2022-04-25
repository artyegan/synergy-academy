package entities;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

public class EducationProcessVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(EducationProcessVerticle.class);
    private final String educationProcessDB;

    private static final String KEYWORD = "keyword";
    private static final String METADATA = "metadata";

    @Inject
    public EducationProcessVerticle(String educationProcessDB) {
        this.educationProcessDB = educationProcessDB;
    }

    @Override
    public void start() {
        vertx.eventBus().<JsonArray>consumer("get.educationprocess.function").handler(this::getEducationProcessWithFunction);
    }

    private void getEducationProcessWithFunction(Message<JsonArray> msg) {
        getEducationProcessWithFunctionRequest(msg.body())
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private Single<JsonArray> getEducationProcessWithFunctionRequest(JsonArray msgBody) {
        return vertx.eventBus().<JsonArray>rxRequest("get.function.service", msgBody)
                .map(Message::body);
    }
}
