package service;


import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;


public class SampleApplication extends Application<SampleConfiguration> {
    public static void main(String[] args) throws Exception {
        new SampleApplication().run(args);
    }

    @Override
    public String getName() {
        return "Slacker Application";
    }

    @Override
    public void initialize(Bootstrap<SampleConfiguration> bootstrap) {


    }

    @Override
    public void run(SampleConfiguration configuration,
                    Environment environment) {
        final SampleResource resource = new SampleResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );


        environment.jersey().register(resource);


}

}