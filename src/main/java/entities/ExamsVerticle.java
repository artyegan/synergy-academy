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

public class ExamsVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(ExamsVerticle.class);
    private final String examsDB;

    @Inject
    public ExamsVerticle(String examsDB) {
        this.examsDB = examsDB;
    }

    @Override
    public void start() {
        vertx.eventBus().consumer("get.exams.filter", this::getExamsByFilter);
    }

    private void getExamsByFilter(Message<JsonArray> msg) {
        getMetadata(examsDB, vertx)
                .map(metadata ->
                        msg.body().getJsonObject(0).put("metadata", metadata)
                                .put("keyword", examsDB))
                .flatMap(this::getExamsWithFilterRequest)
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private Single<JsonArray> getExamsWithFilterRequest(JsonObject msgBody) {
        return vertx.eventBus().<JsonArray>rxRequest("get.filter.service", new JsonArray().add(msgBody))
                .map(Message::body);
    }
}
