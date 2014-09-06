package de.lart.dropwizard.guice;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Binder;
import com.google.inject.Module;

import org.junit.Test;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;

public class TestGuiceBundle
{
    @Inject
    public Provider<SpecialConfiguration> configProvider;

    @Inject
    public Provider<Environment> environmentProvider;


    @Test
    public void testBasicInjection()
        throws Exception
    {
        final GuiceBundle<SpecialConfiguration> bundle = GuiceBundle.defaultBuilder(SpecialConfiguration.class)
            .modules(new Module() {
                @Override
                public void configure(Binder binder) {
                    binder.requestInjection(TestGuiceBundle.this);
                }
            })
            .build();

        final Application<SpecialConfiguration> application = new Application<SpecialConfiguration>() {

            @Override
            public void initialize(Bootstrap<SpecialConfiguration> bootstrap)
            {
            }

            @Override
            public void run(SpecialConfiguration configuration, Environment environment) throws Exception
            {
            }
        };

        final Bootstrap<SpecialConfiguration> bootstrap = new Bootstrap<>(application);
        bundle.initialize(bootstrap);

        assertNotNull(configProvider);
        assertNotNull(environmentProvider);

        // final SpecialConfiguration expectedConfiguration = new SpecialConfiguration();

        EnvironmentCommand<SpecialConfiguration> command = new EnvironmentCommand<SpecialConfiguration>(application, "test", "test") {

            @Override
            protected void run(Environment expectedEnvironment, Namespace namespace, SpecialConfiguration expectedConfiguration) throws Exception
            {
                bundle.run(expectedConfiguration, expectedEnvironment);
                assertSame(expectedConfiguration, configProvider.get());
                assertSame(expectedEnvironment, environmentProvider.get());
            }
        };

        command.run(bootstrap, new Namespace(ImmutableMap.<String, Object>of()));

    }

    public static class SpecialConfiguration extends Configuration
    {
    }
}
