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
import queries.SqlQueries;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DataServiceVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(DataServiceVerticle.class);
    private final PgPool pgPool;

    private static final String KEYWORD = "keyword";
    private static final String METADATA = "metadata";

    @Inject
    public DataServiceVerticle(PgPool pgPool) {
        this.pgPool = pgPool;
    }

    @Override
    public void start() {
        vertx.eventBus().consumer("get.all.service", this::getAllHandler);
        vertx.eventBus().consumer("get.filter.service", this::getFilterHandler);
        vertx.eventBus().consumer("update.id.service", this::updateIdHandler);
        vertx.eventBus().consumer("get.function.service", this::getFunctionHandler);
        vertx.eventBus().consumer("add.service", this::addHandler);
    }

    private void getAllHandler(Message<JsonArray> msg) {
        List<String> columnNames = new ArrayList<>();

        String keyword = msg.body().getJsonObject(0).getString(KEYWORD);
        JsonArray metadata = msg.body().getJsonObject(0).getJsonArray(METADATA);

        pgPool.preparedQuery(SqlQueries.selectQuery(metadata, keyword).getQuery())
                .rxExecute()
                .map(rowSet -> {
                    columnNames.addAll(rowSet.columnsNames());
                    return rowSet;
                })
                .flatMapObservable(Observable::fromIterable)
                .map(row -> this.addRowToJson(columnNames, row))
                .toList()
                .subscribe(list -> {
                    LOGGER.info(String.format("Get all %s from db", keyword));
                    msg.reply(new JsonArray(list));
                }, error -> {
                    LOGGER.error(error);
                    msg.fail(500, error.getMessage());
                });
    }

    private void getFilterHandler(Message<JsonArray> msg) {
        List<String> columnNames = new ArrayList<>();

        JsonObject msgBody = msg.body().getJsonObject(0);

        JsonArray metadata = msgBody.getJsonArray(METADATA);
        String keyword = msgBody.getString(KEYWORD);
        String filterColumn = msgBody.getString("filterColumn");
        String value = msgBody.getString("value");

        pgPool.preparedQuery(SqlQueries.selectQueryFilter(metadata, keyword, filterColumn, value).getQuery())
                .rxExecute()
                .map(rowSet -> {
                    columnNames.addAll(rowSet.columnsNames());
                    return rowSet;
                })
                .flatMapObservable(Observable::fromIterable)
                .map(row -> this.addRowToJson(columnNames, row))
                .toList()
                .subscribe(json -> {
                            LOGGER.info(String.format("Get %s by %s", keyword, filterColumn));
                            msg.reply(new JsonArray(json));
                        },
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private void getFunctionHandler(Message<JsonArray> msg) {
        List<String> columnNames = new ArrayList<>();
        String keyword = msg.body()
                .getJsonObject(0)
                .getString(KEYWORD);
        String function = msg.body()
                .getJsonObject(0)
                .getString("function");
        pgPool.preparedQuery(SqlQueries.getFunctionQuery(function, keyword).getQuery())
                .rxExecute()
                .map(rowSet -> {
                    columnNames.addAll(rowSet.columnsNames());
                    return rowSet;
                })
                .flatMapObservable(Observable::fromIterable)
                .map(row -> this.addRowToJson(columnNames, row))
                .toList()
                .subscribe(list -> {
                    LOGGER.info(String.format("Get data with function from %s(possibly metadata)", keyword));
                    msg.reply(new JsonArray(list));
                }, error -> {
                    LOGGER.error(error);
                    msg.fail(500, error.getMessage());
                });
    }

    private void updateIdHandler(Message<JsonObject> msg) {
        pgPool.preparedQuery(SqlQueries.updateQuery(
                        msg.body().getJsonArray(METADATA),
                        msg.body().getString("id"),
                        msg.body().getString(KEYWORD),
                        msg.body().getJsonObject("data")).getQuery())
                .rxExecute()
                .subscribe(res -> {
                    LOGGER.info(
                            String.format(
                                    "%s %s updated", msg.body().getString(KEYWORD), msg.body().getString("id")));
                    msg.reply(new JsonObject().put("msg", msg.body().getString(KEYWORD) + " " +
                                    msg.body().getString("id") +
                                    " updated"));
                        }, error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        }
                );
    }

    private void addHandler(Message<JsonObject> msg) {
        pgPool.preparedQuery(SqlQueries.insertQuery(
                        msg.body().getJsonArray(METADATA),
                        msg.body().getString(KEYWORD),
                        msg.body().getJsonObject("data")).getQuery())
                .rxExecute()
                .flatMap(rows -> pgPool.preparedQuery(SqlQueries.getFunctionQuery("getmaxid", msg.body().getString(KEYWORD))
                        .getQuery())
                        .rxExecute())
                .map(rows -> rows.iterator().next().getInteger(0))
                .subscribe(res -> {
                            LOGGER.info(String.format("Data inserted in %s", msg.body().getString(KEYWORD)));
                            msg.reply(new JsonObject().put("id", res));
                        }, error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        }
                );
    }

    private JsonObject addRowToJson(@NotNull List<String> columnNames, Row row) {
        JsonObject jsonObject = new JsonObject();

        columnNames.forEach(columnName -> {

            if (row.getValue(columnName) instanceof LocalDateTime localDateTime) {
                jsonObject.put(columnName,
                        localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else if (row.getValue(columnName) instanceof LocalDate localDate) {
                jsonObject.put(columnName,
                        localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                jsonObject.put(columnName, row.getValue(columnName));
            }
        });

        return jsonObject;
    }
}
