package services;

import com.google.inject.Inject;
import io.reactivex.Observable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import querries.StudentsQuerries;

public class StudentsServiceVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(StudentsServiceVerticle.class);
    private final PgPool pgPool;

    @Inject
    public StudentsServiceVerticle(PgPool pgPool) {
        this.pgPool = pgPool;
    }

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer("get.students.service.all", this::getAllStudents);
    }

    private void getAllStudents(Message<JsonArray> msg) {
        pgPool.preparedQuery(StudentsQuerries.getAllStudentsQuerry())
                .rxExecute()
                .flatMapObservable(Observable::fromIterable)
                .map(row -> new JsonObject()
                        .put("studentid", row.getInteger("studentid"))
                        .put("firstname", row.getString("firstname"))
                        .put("lastname", row.getString("lastname"))
                        .put("email", row.getString("email"))
                        .put("fullname", row.getString("fullname"))
                        .put("phonenumber", row.getString("phonenumber"))
                        .put("address", row.getString("address"))
                        .put("universityid", row.getInteger("universityid"))
                        .put("gpa", row.getInteger("gpa"))
                        .put("whatprogramminglanguagesdoyouknow", row.getString("whatprogramminglanguagesdoyouknow"))
                        .put("educationdepartmentadmissionandgraduationyear", row.getString("educationdepartmentadmissionandgraduationyear"))
                        .put("othercoursesattended", row.getString("othercoursesattended"))
                        .put("haveyoueverparticipatedinprogramming", row.getString("haveyoueverparticipatedinprogramming"))
                        .put("doyouhaveworkexperience", row.getString("doyouhaveworkexperience"))
                        .put("howdidyoufindid", row.getInteger("howdidyoufindid"))
                        .put("stateid", row.getInteger("stateid")))
                .toList()
                .subscribe(list -> {
                    LOGGER.info("Get All Students");
                    msg.reply(new JsonArray(list));
                }, error -> {
                    LOGGER.error(error);
                    msg.fail(500, error.getMessage());
                });
    }
}
