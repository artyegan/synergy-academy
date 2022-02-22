package module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.ProvidesIntoSet;
import io.vertx.core.Verticle;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import server.Handlers;
import server.RouterBuilderVerticle;
import server.VerticleDeployer;
import services.StudentsServiceVerticle;
import students.StudentsVerticle;

import java.util.Set;

public class ProviderModule extends AbstractModule {

    @Provides
    @Inject
    private static PgPool providePgPool(Vertx vertx) {
        return PgPool.pool(vertx, setConnectionOptions(), new PoolOptions());
    }

    private static PgConnectOptions setConnectionOptions() {
        return new PgConnectOptions()
                .setPort(5432)
                .setHost("localhost")
                .setDatabase("academy_phase1")
                .setUser("postgres")
                .setPassword("postgres");
    }

    @Provides
    @Singleton
    public Vertx provideVertx() {
        return Vertx.vertx();
    }

    @Provides
    @Inject
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
    public Verticle provideRouterBuilderVerticle(Handlers handlers) {
        return new RouterBuilderVerticle(handlers);
    }

    @ProvidesIntoSet
    public Verticle provideStudentsVerticle() {
        return new StudentsVerticle();
    }

    @Inject
    @ProvidesIntoSet
    public Verticle provideStudentsServiceVerticle(PgPool pgPool) {
        return new StudentsServiceVerticle(pgPool);
    }
}
