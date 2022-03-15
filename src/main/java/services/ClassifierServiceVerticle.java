package services;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static meta.MetadataProvider.getMetadata;

public class ClassifierServiceVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(ClassifierServiceVerticle.class);

    @Override
    public void start() {
        vertx.eventBus().consumer("get.classifier", this::getClassifier);
    }

    private void getClassifier(Message<JsonArray> msg) {
        String classifierTableName = msg.body().getJsonObject(0).getString("keyword");

        getMetadata(classifierTableName, vertx)
                .map(metadata ->
                        new JsonArray().add(
                                new JsonObject()
                                        .put("metadata", metadata)
                                        .put("keyword", classifierTableName)
                        )
                )
                .flatMap(this::getClassifierRequest)
                .subscribe(msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private Single<JsonArray> getClassifierRequest(JsonArray classifierTableName) {
        return vertx.eventBus()
                .<JsonArray>rxRequest("get.all.service", classifierTableName)
                .map(Message::body);
    }
}
