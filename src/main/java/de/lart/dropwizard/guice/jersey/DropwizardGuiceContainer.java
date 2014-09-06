package de.lart.dropwizard.guice.jersey;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;

import com.google.inject.Injector;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;

import io.dropwizard.setup.Environment;

public class DropwizardGuiceContainer
    extends GuiceContainer
{
    private final Provider<Environment> environmentProvider;

    @Inject
    public DropwizardGuiceContainer(final Injector injector,
                                    final Provider<Environment> environmentProvider)
    {
        super(injector);
        this.environmentProvider = checkNotNull(environmentProvider, "environmentProvider is null");
    }

    @Override
    protected ResourceConfig getDefaultResourceConfig(final Map<String, Object> props,
                                                      final WebConfig webConfig)
        throws ServletException
    {
        Environment environment = environmentProvider.get();
        return environment.jersey().getResourceConfig();
    }
}
