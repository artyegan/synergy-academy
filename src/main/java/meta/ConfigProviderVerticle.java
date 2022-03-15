package meta;

import com.google.inject.Inject;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static meta.MetadataProvider.getMetadata;

public class ConfigProviderVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(ConfigProviderVerticle.class);

    private final String configDB;

    @Inject
    public ConfigProviderVerticle(String configDB) {
        this.configDB = configDB;
    }

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer("get.config", this::getConfig);
    }

    private void getConfig(Message<JsonObject> msg) {
        getMetadata(configDB, vertx)
                .map(metadata ->
                        msg.body().put("metadata", metadata)
                                .put("keyword", configDB))
                .flatMap(this::getConfigRequest)
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private Single<JsonObject> getConfigRequest(JsonObject msgBody) {
        return vertx.eventBus().<JsonObject>rxRequest("get.filter.service", msgBody)
                .map(Message::body);
    }
}
