package de.lart.dropwizard.guice;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Provider;

public class SettableProvider<T> implements Provider<T>
{
    private AtomicReference<T> holder = new AtomicReference<>();

    @Override
    public T get()
    {
        T environment = holder.get();
        checkState(environment != null, "injected value is null. Replace direct injection with Provider<T>!");

        return environment;
    }

    public void setValue(T environment)
    {
        holder.set(checkNotNull(environment, "environment is null"));
    }
}
