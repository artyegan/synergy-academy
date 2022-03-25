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

public class ExamsVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(ExamsVerticle.class);
    private final String examsDB;

    private static final String KEYWORD = "keyword";
    private static final String METADATA = "metadata";

    @Inject
    public ExamsVerticle(String examsDB) {
        this.examsDB = examsDB;
    }

    @Override
    public void start() {
        vertx.eventBus().consumer("get.exams.filter", this::getExamsByFilter);
        vertx.eventBus().consumer("add.exam", this::addExam);
        vertx.eventBus().consumer("update.exams.id", this::updateExamById);
    }

    private void getExamsByFilter(Message<JsonArray> msg) {
        getMetadata(examsDB, vertx)
                .map(metadata ->
                        msg.body().getJsonObject(0).put(METADATA, metadata)
                                .put(KEYWORD, examsDB))
                .flatMap(this::getExamsWithFilterRequest)
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private void addExam(Message<JsonObject> msg) {
        getMetadataAndExtractId(examsDB, vertx)
                .map(metadata ->
                        msg.body().put(METADATA, metadata)
                                .put(KEYWORD, examsDB))
                .flatMap(this::addExamRequest)
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private void updateExamById(Message<JsonObject> msg) {
        getMetadataAndExtractId(examsDB, vertx)
                .map(metadata ->
                        msg.body().put(METADATA, metadata)
                                .put(KEYWORD, examsDB))
                .flatMap(this::updateExamByIdRequest)
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

    private Single<JsonObject> addExamRequest(JsonObject msgBody) {
        return vertx.eventBus().<JsonObject>rxRequest("add.service", msgBody)
                .map(Message::body);
    }

    private Single<JsonObject> updateExamByIdRequest(JsonObject msgBody) {
        return vertx.eventBus().<JsonObject>rxRequest("update.id.service", msgBody)
                .map(Message::body);
    }
}
