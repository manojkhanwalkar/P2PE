package kms;


import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;


public class KMSApplication extends Application<KMSConfiguration> {
    public static void main(String[] args) throws Exception {
        new KMSApplication().run(args);
    }

    @Override
    public String getName() {
        return "Slacker Application";
    }

    @Override
    public void initialize(Bootstrap<KMSConfiguration> bootstrap) {

    //    IDStatusPollManager.getInstance().start();

        // nothing to do yet
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