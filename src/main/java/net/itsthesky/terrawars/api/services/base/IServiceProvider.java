package net.itsthesky.terrawars.api.services.base;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Set;


/**
 * Provider for all services.
 */
public interface IServiceProvider {

    /**
     * Registers a service implementation with its corresponding interface class.
     *
     * @param serviceClass The class representing the service interface
     * @param service The implementation of the service to register
     * @param <T> The type of the service
     */
    <T> void registerService(@NotNull Class<T> serviceClass,
                             @NotNull T service);

    /**
     * Retrieves a registered service implementation by its interface class.
     *
     * @param serviceClass The class representing the service interface
     * @param <T> The type of the service
     * @return The implementation of the requested service
     * @throws IllegalArgumentException If the service is not registered
     */
    <T> T getService(@NotNull Class<T> serviceClass);

    /**
     * Checks if a service is registered with the provider.
     *
     * @param serviceClass The class representing the service interface
     * @param <T> The type of the service
     * @return True if the service is registered, false otherwise
     */
    <T> boolean isServiceRegistered(@NotNull Class<T> serviceClass);

    /**
     * Unregisters a service from the provider.
     *
     * @param serviceClass The class representing the service interface
     * @param <T> The type of the service
     * @throws IllegalArgumentException If the service is not registered
     */
    <T> void unregisterService(@NotNull Class<T> serviceClass);

    /**
     * Gets a set of all registered service interface classes.
     *
     * @return A set containing all registered service interface classes
     */
    @NotNull Set<Class<?>> getRegisteredServices();

    /**
     * Disables all registered services by calling their destroy method if they implement IService.
     */
    void disableServices();

    /**
     * Scans a package for classes with the @Service annotation, instantiates them,
     * registers them with the provider, and injects dependencies.
     *
     * @param packageName The name of the package to scan
     * @param subPackages Optional sub-packages to scan
     * @throws IOException If an error occurs during package scanning
     */
    void injectServices(@NotNull String packageName, String... subPackages) throws IOException;

    /**
     * Inject services into an instance.
     */
    <T> void inject(@NotNull T instance);
}