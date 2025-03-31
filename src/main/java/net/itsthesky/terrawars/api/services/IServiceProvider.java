package net.itsthesky.terrawars.api.services;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Set;

/**
 * Provider for all services.
 */
public interface IServiceProvider {

    <T> void registerService(@NotNull Class<T> serviceClass,
                             @NotNull T service);

    <T> T getService(@NotNull Class<T> serviceClass);

    <T> boolean isServiceRegistered(@NotNull Class<T> serviceClass);

    <T> void unregisterService(@NotNull Class<T> serviceClass);

    @NotNull Set<Class<?>> getRegisteredServices();

    void injectServices(@NotNull String packageName) throws IOException;
}
