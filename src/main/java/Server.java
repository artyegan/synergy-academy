import com.google.inject.Guice;
import com.google.inject.Injector;

import module.ProviderModule;
import server.VerticleDeployer;

public class Server {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ProviderModule());

        var verticleDeployer = injector.getInstance(VerticleDeployer.class);

        verticleDeployer.deployVertices();
    }
}
