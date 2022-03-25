package meta;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;

public class MetadataProvider {
    private MetadataProvider(){}
    public static Single<JsonArray> getMetadata(String keyword, Vertx vertx) {
        return vertx.eventBus().<JsonArray>rxRequest("get.function.service",
                        new JsonArray().add(new JsonObject()
                                .put("function", "getcolumnbytablenamewithclassiferbool")
                                .put("keyword", keyword)))
                .map(Message::body);
    }

    public static Single<JsonArray> getMetadataAndExtractId(String tableName, Vertx vertx) {
        return getMetadata(tableName, vertx)
                .flatMapObservable(Observable::fromIterable)
                .map(JsonObject.class::cast)
                .filter(json -> !json.getString("column_name").equals(tableName + "id"))
                .toList()
                .map(JsonArray::new);
    }
}
