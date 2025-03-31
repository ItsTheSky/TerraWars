package net.itsthesky.terrawars;

import com.google.common.collect.Sets;
import net.itsthesky.terrawars.api.services.base.IServiceProvider;
import net.itsthesky.terrawars.api.services.base.Service;
import net.itsthesky.terrawars.util.Checks;
import net.itsthesky.terrawars.util.Classes;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServiceProvider implements IServiceProvider {

    private final TerraWars plugin;
    private final Map<Class<?>, Object> services;

    /**
     * Construct a new ServiceProvider. This should only be called once,
     * as the whole dependency injection system is based on this class.
     * @param plugin The main plugin instance.
     */
    public ServiceProvider(TerraWars plugin) {
        this.plugin = plugin;
        this.services = new HashMap<>();

        registerService(IServiceProvider.class, this);
    }

    @Override
    public <T> void registerService(@NotNull Class<T> serviceClass, @NotNull T service) {
        Checks.notNull(serviceClass, "Service class cannot be null");
        Checks.notNull(service, "Service cannot be null");
        Checks.isTrue(serviceClass != IServiceProvider.class, "Cannot register IServiceProvider as a service");

        if (services.containsKey(serviceClass))
            throw new IllegalArgumentException("Service already registered: " + serviceClass.getName());

        services.put(serviceClass, service);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(@NotNull Class<T> serviceClass) {
        Checks.notNull(serviceClass, "Service class cannot be null");
        Checks.isTrue(serviceClass != IServiceProvider.class, "Cannot get IServiceProvider as a service");

        if (!services.containsKey(serviceClass))
            throw new IllegalArgumentException("Service not registered: " + serviceClass.getName());

        return (T) services.get(serviceClass);
    }

    @Override
    public <T> boolean isServiceRegistered(@NotNull Class<T> serviceClass) {
        Checks.notNull(serviceClass, "Service class cannot be null");
        Checks.isTrue(serviceClass != IServiceProvider.class, "Cannot check IServiceProvider as a service");

        return services.containsKey(serviceClass);
    }

    @Override
    public <T> void unregisterService(@NotNull Class<T> serviceClass) {
        Checks.notNull(serviceClass, "Service class cannot be null");
        Checks.isTrue(serviceClass != IServiceProvider.class, "Cannot unregister IServiceProvider as a service");

        if (!services.containsKey(serviceClass))
            throw new IllegalArgumentException("Service not registered: " + serviceClass.getName());

        services.remove(serviceClass);
    }

    @Override
    public @NotNull Set<Class<?>> getRegisteredServices() {
        return Sets.newHashSet(services.keySet());
    }

    @Override
    public void injectServices(@NotNull String packageName) throws IOException {
        Checks.notNull(packageName, "Package name cannot be null");

        // First phase: scan the package for classes with @Service annotation
        Set<Class<?>> classes = Classes.getClasses(packageName);
        Map<Class<?>, Object> newServices = new HashMap<>();

        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Service.class)) {
                try {
                    Service serviceAnnotation = clazz.getAnnotation(Service.class);

                    // When default, we'll check for the first interface implemented
                    Class<?> serviceInterface = serviceAnnotation.value();
                    if (serviceInterface == Void.class) {
                        Class<?>[] interfaces = clazz.getInterfaces();
                        if (interfaces.length > 0) {
                            serviceInterface = interfaces[0];
                        } else {
                            plugin.getLogger().warning("Service class " + clazz.getName() + " does not specify an interface and does not implement any interfaces");
                            continue;
                        }
                    }

                    if (isServiceRegistered(serviceInterface)) {
                        plugin.getLogger().warning("Service already registered: " + serviceInterface.getName());
                        continue;
                    }

                    final var pluginConstructor = Classes.getNullableDeclaredConstructor(clazz, TerraWars.class);
                    final var emptyConstructor = Classes.getNullableDeclaredConstructor(clazz);

                    Object instance;
                    if (pluginConstructor != null) {
                        instance = pluginConstructor.newInstance(plugin);
                    } else if (emptyConstructor != null) {
                        instance = emptyConstructor.newInstance();
                    } else {
                        plugin.getLogger().warning("No suitable constructor found for service class " + clazz.getName());
                        continue;
                    }

                    // Register the service
                    services.put(serviceInterface, instance);
                    newServices.put(serviceInterface, instance);

                    plugin.getLogger().info("Registered service: " + serviceInterface.getName() + " with implementation: " + clazz.getName());

                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to register service: " + clazz.getName() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        // Second phase: inject dependencies into the registered services
        for (Map.Entry<Class<?>, Object> entry : newServices.entrySet()) {
            Object service = entry.getValue();
            injectDependencies(service);
        }
    }

    private void injectDependencies(Object serviceInstance) {
        Class<?> serviceClass = serviceInstance.getClass();

        for (Field field : serviceClass.getDeclaredFields()) {
            Class<?> dependencyType = field.getType();

            try {
                if (!isServiceRegistered(dependencyType))
                    continue;

                Object dependency = getService(dependencyType);

                boolean wasAccessible = field.canAccess(serviceInstance);
                field.setAccessible(true);

                field.set(serviceInstance, dependency);

                field.setAccessible(wasAccessible);

                plugin.getLogger().info("Injected " + dependencyType.getName() + " into " + serviceClass.getName());

            } catch (Exception e) {
                plugin.getLogger().severe("Failed to inject dependency of type " + dependencyType.getName()
                        + " into " + serviceClass.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
