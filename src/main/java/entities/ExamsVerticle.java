package entities;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.web.client.WebClient;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

import java.time.Instant;

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
        vertx.eventBus().consumer("get.results.function", this::getResultsWithFunction);
        vertx.eventBus().consumer("update.results.classmarker", this::updateResultsClassmarker);
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

    private void getResultsWithFunction(Message<JsonArray> msg) {
        getResultsWithFunctionRequest(msg.body())
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

    private void updateResultsClassmarker(Message<JsonArray> msg) {
        WebClient client = WebClient.create(vertx);

        String md5Hex = DigestUtils
                .md5Hex("o6GCc7OmwAkNCtqVF7qVjpqRJ0IuPrDI" +
                        "yf2cnoKd5dXGf9aUv1gtjt8xeNIGcOQP7sPCXHLZ" +
                        Instant.now().getEpochSecond());

        client.getAbs("https://api.classmarker.com/v1/links/1217900/tests/1938918/recent_results.json")
                .addQueryParam("api_key", "o6GCc7OmwAkNCtqVF7qVjpqRJ0IuPrDI")
                .addQueryParam("signature", md5Hex)
                .addQueryParam("timestamp", String.valueOf(Instant.now().getEpochSecond()))
                .addQueryParam("finishedAfterTimestamp", "1650049943")
                .rxSend().subscribe(
                        res -> {
                            var arr = res.bodyAsJsonObject().getJsonArray("results");
                            var results = new JsonArray();
                            System.out.println(res);

                            for (int i = 0; i < arr.size(); ++i) {
                                var result = arr.getJsonObject(i).getJsonObject("result");

                                results.add(new JsonObject()
                                        .put("examId", msg.body().getJsonObject(0).getString("keyword"))
                                        .put("email", result.getString("email"))
                                        .put("grade", result.getString("points_scored")));
                            }

                            msg.body().getJsonObject(0).put("results", results);

                            vertx.eventBus().rxRequest("update.function.service", msg.body())
                                    .subscribe(ress -> msg.reply(ress.body()),
                                            LOGGER::error);
                        },
                        LOGGER::error
                );


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


    private Single<JsonArray> getResultsWithFunctionRequest(JsonArray msgBody) {
        return vertx.eventBus().<JsonArray>rxRequest("get.function.service", msgBody)
                .map(Message::body);
    }
}
