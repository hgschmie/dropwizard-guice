package de.lart.dropwizard.guice;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class GuiceBundle<T extends Configuration> implements ConfiguredBundle<T>
{
    private static final Logger LOG = LoggerFactory.getLogger(GuiceBundle.class);

    public static final <U extends Configuration> Builder<U> defaultBuilder(Class<U> configClass) {
        return new Builder<>(configClass);
    }

    private final Class<T> configClass;
    private final ImmutableSet<Module> guiceModules;
    private final ImmutableSet<Package> autoconfigPackages;
    private final Stage guiceStage;

    private final SettableProvider<Environment> environmentProvider = new SettableProvider<>();
    private final SettableProvider<T> configurationProvider = new SettableProvider<>();

    private GuiceBundle(final Class<T> configClass,
                        final ImmutableSet<Module> guiceModules,
                        final ImmutableSet<Package> autoConfigPackages,
                        final Stage guiceStage)
    {
        this.configClass = configClass;
        this.guiceModules = guiceModules;
        this.autoconfigPackages = autoConfigPackages;
        this.guiceStage = guiceStage;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
        final Injector injector = Guice.createInjector(guiceStage, ImmutableSet.<Module>builder()
            .addAll(guiceModules)
            .add(new Module() {
                @Override
                public void configure(final Binder binder) {
                    binder.bind(Environment.class).toProvider(environmentProvider);
                    binder.bind(configClass).toProvider(configurationProvider);
                }
            })
            .build());
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception
    {
        configurationProvider.setValue(configuration);
        environmentProvider.setValue(environment);
    }


    public static class Builder<U extends Configuration>
    {
        private final Class<U> configClass;
        private final ImmutableSet.Builder<Package> autoConfigPackages = ImmutableSet.builder();
        private final ImmutableSet.Builder<Module> guiceModules = ImmutableSet.builder();
        private Stage guiceStage = Stage.PRODUCTION;

        private Builder(Class<U> configClass)
        {
            this.configClass = configClass;
        }

        public Builder<U> autoConfig(Package ... packages)
        {
            autoConfigPackages.add(packages);
            return this;
        }

        public Builder<U> autoConfig(String ... packageNames)
        {
            for (String packageName : packageNames) {
                Package pkg = Package.getPackage(packageName);
                checkState(pkg != null, "Package %s not available", packageName);
                autoConfigPackages.add(pkg);
            }
            return this;
        }

        public Builder<U> stage(Stage guiceStage)
        {
            checkNotNull(guiceStage, "guiceStage is null");
            if (guiceStage != Stage.PRODUCTION) {
                LOG.warn("Guice should only ever run in PRODUCTION mode except for testing!");
            }
            this.guiceStage = guiceStage;
            return this;
        }

        public Builder<U> modules(Module ... modules)
        {
            guiceModules.add(modules);
            return this;
        }


        public GuiceBundle<U> build()
        {
            return new GuiceBundle<U>(configClass,
                            guiceModules.build(),
                            autoConfigPackages.build(),
                            guiceStage);
        }
    }
}
