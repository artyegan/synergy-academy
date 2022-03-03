package services;

import com.google.inject.Inject;
import io.reactivex.Observable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import querries.SqlQueries;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DataServiceVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(DataServiceVerticle.class);
    private final PgPool pgPool;

    @Inject
    public DataServiceVerticle(PgPool pgPool) {
        this.pgPool = pgPool;
    }

    @Override
    public void start() {
        vertx.eventBus().consumer("get.all.service", this::getAllHandler);
        vertx.eventBus().consumer("get.id.service", this::getIdHandler);
    }

    private void getAllHandler(Message<JsonArray> msg) {
        List<String> columnNames = new ArrayList<>();
        String keyword = msg.body()
                .getJsonObject(0)
                .getString("keyword");

        pgPool.preparedQuery(SqlQueries.getAllQuery(keyword))
                .rxExecute()
                .map(rowSet -> {
                    columnNames.addAll(rowSet.columnsNames());
                    return rowSet;
                })
                .flatMapObservable(Observable::fromIterable)
                .map(row -> this.addRowToJson(columnNames, row))
                .toList()
                .subscribe(list -> {
                    LOGGER.info("Got all " + keyword + " from db");
                    msg.reply(new JsonArray(list));
                }, error -> {
                    LOGGER.error(error);
                    msg.fail(500, error.getMessage());
                });
    }

    private void getIdHandler(Message<JsonObject> msg) {
        String keyword = msg.body().getString("keyword");
        String id = msg.body().getString("id");

        pgPool.preparedQuery(SqlQueries.getIdQuery(keyword, id))
                .rxExecute()
                .map(rowSet -> {
                    JsonObject jsonObject = new JsonObject();

                    for (Row row : rowSet) {
                        jsonObject = addRowToJson(rowSet.columnsNames(), row);
                    }

                    return jsonObject;
                })
                .subscribe(json -> {
                            LOGGER.info("Got " + keyword + " by id");
                            msg.reply(json);
                        },
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private JsonObject addRowToJson(@NotNull List<String> columnNames, Row row) {
        JsonObject jsonObject = new JsonObject();

        columnNames.forEach(columnName -> {

            if (row.getValue(columnName) instanceof LocalDateTime) {
                jsonObject.put(columnName, formatDateTime((LocalDateTime) row.getValue(columnName)));
            } else {
                jsonObject.put(columnName, row.getValue(columnName));
            }
        });

        return jsonObject;
    }

    private String formatDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }
}
