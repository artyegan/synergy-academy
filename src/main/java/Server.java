import com.google.inject.Guice;

import module.ProviderModule;
import server.VerticleDeployer;

public class Server {
    public static void main(String[] args) {
        Guice.createInjector(new ProviderModule())
        .getInstance(VerticleDeployer.class)
                .deployVertices();
    }
}
