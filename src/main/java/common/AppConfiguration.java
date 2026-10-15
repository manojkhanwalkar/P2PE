package common;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import jakarta.validation.constraints.NotEmpty;

/** Configuration shared by the KMS and sample service applications. */
public class AppConfiguration extends Configuration {

    /**
     * Resolves {@code ${ENV_VAR}} references in the YAML, so secrets such as the keystore password come from the
     * environment instead of the repository. Strict: startup fails if a referenced variable is unset.
     */
    public static void enableEnvironmentSubstitution(Bootstrap<?> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(true)));
    }

    @NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }
}