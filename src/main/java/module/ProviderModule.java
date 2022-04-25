package module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.ProvidesIntoSet;
import com.google.inject.name.Names;
import entities.CoursesVerticle;
import entities.EducationProcessVerticle;
import entities.ExamsVerticle;
import io.vertx.core.Verticle;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import meta.ConfigProviderVerticle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import server.Handlers;
import server.RouterBuilderVerticle;
import server.VerticleDeployer;
import services.ClassifierServiceVerticle;
import services.DataServiceVerticle;
import entities.StudentsVerticle;

import javax.inject.Named;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

public class ProviderModule extends AbstractModule {

    private static final Logger LOGGER = LogManager.getLogger(ProviderModule.class);

    private void setConfigsFile(@NotNull Path path) {
        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader(path.toFile())) {
            properties.load(fileReader);
            Names.bindProperties(binder(), properties);
            LOGGER.info("Properties loaded");
        } catch (FileNotFoundException e) {
            LOGGER.error(String.format("The configuration file %s can not be found", path.getFileName().toString()));
        } catch (IOException e) {
            LOGGER.error("I/O Exception during loading configuration");
        }
    }

    @Override
    protected void configure() {
        setConfigsFile(Path.of("src/main/resources/configs.properties"));
    }

    @Inject
    @Provides
    private static PgPool providePgPool(Vertx vertx,
                                        @Named("port") int port,
                                        @Named("host") String host,
                                        @Named("database") String database,
                                        @Named("user") String user,
                                        @Named("password") String password) {
        return PgPool.pool(vertx, setConnectionOptions(port, host, database, user, password), new PoolOptions());
    }

    @Inject
    private static PgConnectOptions setConnectionOptions(int port,
                                                         String host,
                                                         String database,
                                                         String user,
                                                         String password) {
        return new PgConnectOptions()
                .setPort(port)
                .setHost(host)
                .setDatabase(database)
                .setUser(user)
                .setPassword(password);
    }

    @Provides
    @Singleton
    public Vertx provideVertx() {
        return Vertx.vertx();
    }

    @Inject
    @Provides
    @Singleton
    public Handlers provideHandlers(Vertx vertx) {
        return new Handlers(vertx);
    }

    @Provides
    @Inject
    public VerticleDeployer provideVerticleDeployer(Vertx vertx, Set<Verticle> vertices) {
        return new VerticleDeployer(vertx, vertices);
    }

    @Inject
    @ProvidesIntoSet
    public Verticle provideRouterBuilderVerticle(
            Handlers handlers,
            @Named("serverPort") int serverPort,
            @Named("restUrl") String restUrl) {
        return new RouterBuilderVerticle(handlers, serverPort, restUrl);
    }

    @Inject
    @ProvidesIntoSet
    public Verticle provideStudentsVerticle(@Named("studentsDB") String studentsDB) {
        return new StudentsVerticle(studentsDB);
    }

    @Inject
    @ProvidesIntoSet
    public Verticle provideCoursesVerticle(@Named("coursesDB") String coursesDB) {
        return new CoursesVerticle(coursesDB);
    }

    @Inject
    @ProvidesIntoSet
    public Verticle provideExamsVerticle(@Named("examsDB") String examsDB) {
        return new ExamsVerticle(examsDB);
    }


    @Inject
    @ProvidesIntoSet
    public Verticle provideEducationProcessVerticle(@Named("educationProcessDB") String educationProcessDB) {
        return new EducationProcessVerticle(educationProcessDB);
    }

    @Inject
    @ProvidesIntoSet
    public Verticle provideDataServiceVerticle(PgPool pgPool) {
        return new DataServiceVerticle(pgPool);
    }

    @ProvidesIntoSet
    public Verticle provideClassifierServiceVerticle() {
        return new ClassifierServiceVerticle();
    }

    @Inject
    @ProvidesIntoSet
    public Verticle provideConfigProviderVerticle(@Named("configDB") String configDB) {
        return new ConfigProviderVerticle(configDB);
    }
}
