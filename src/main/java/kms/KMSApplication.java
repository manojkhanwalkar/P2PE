package kms;


import common.AppConfiguration;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;


public class KMSApplication extends Application<KMSConfiguration> {
    public static void main(String[] args) throws Exception {
        new KMSApplication().run(args);
    }

    @Override
    public String getName() {
        return "KMS";
    }

    @Override
    public void initialize(Bootstrap<KMSConfiguration> bootstrap) {

    //    IDStatusPollManager.getInstance().start();

        AppConfiguration.enableEnvironmentSubstitution(bootstrap);
    }

    @Override
    public void run(KMSConfiguration configuration,
                    Environment environment) {
        final KMSResource resource = new KMSResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );


        environment.jersey().register(resource);


}

}