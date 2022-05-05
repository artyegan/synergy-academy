package entities;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReportingVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(ReportingVerticle.class);

    @Override
    public void start() {
        vertx.eventBus().consumer("get.reports.piechart", this::getReportsPieChart);
        vertx.eventBus().consumer("get.reports.table", this::getReportsTable);
        vertx.eventBus().consumer("get.reports.columnchart", this::getReportsColumnChart);
        vertx.eventBus().consumer("get.reports.histogram", this::getReportsHistogram);
    }

    private void getReportsPieChart(Message<JsonArray> msg) {
        getReportsWithFunctionRequest(
                msg.body()
                .add(new JsonObject()
                .put("function", "analytics_number_of_courses_by_office"))
        )
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private void getReportsTable(Message<JsonArray> msg) {
        getReportsWithFunctionRequest(
                msg.body()
                        .add(new JsonObject()
                                .put("function", "analytics_top_50_max_grades_in_all_courses"))
        )
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private void getReportsColumnChart(Message<JsonArray> msg) {
        getReportsWithFunctionRequest(
                msg.body()
                        .add(new JsonObject()
                                .put("function", "analytics_student_grades_by_exam"))
        )
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private void getReportsHistogram(Message<JsonArray> msg) {
        getReportsWithFunctionRequest(
                msg.body()
                        .add(new JsonObject()
                                .put("function", "analytics_number_of_students_by_universities"))
        )
                .subscribe(
                        msg::reply,
                        error -> {
                            LOGGER.error(error);
                            msg.fail(500, error.getMessage());
                        });
    }

    private Single<JsonArray> getReportsWithFunctionRequest(JsonArray msgBody) {
        return vertx.eventBus().<JsonArray>rxRequest("get.function.service", msgBody)
                .map(Message::body);
    }
}
