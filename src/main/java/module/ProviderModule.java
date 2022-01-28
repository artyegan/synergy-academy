package module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.ProvidesIntoSet;
import io.vertx.core.Verticle;
import io.vertx.reactivex.core.Vertx;
import server.Handlers;
import server.RouterBuilderVerticle;
import server.VerticleDeployer;

import java.util.Set;

public class ProviderModule extends AbstractModule {
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
}
