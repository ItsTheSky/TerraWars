package net.itsthesky.terrawars;

import com.google.common.collect.Sets;
import net.itsthesky.terrawars.api.services.base.IService;
import net.itsthesky.terrawars.api.services.base.IServiceProvider;
import net.itsthesky.terrawars.api.services.base.Service;
import net.itsthesky.terrawars.util.Checks;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
    public void injectServices(@NotNull String basePackage, String... subPackages) throws IOException {
        Checks.notNull(basePackage, "Base package name cannot be null");

        // Convertir les packages en format chemin
        for (int i = 0; i < subPackages.length; i++)
            subPackages[i] = subPackages[i].replace('.', '/') + "/";
        basePackage = basePackage.replace('.', '/') + "/";

        // Obtenir le fichier JAR du plugin
        JarFile jar = new JarFile(getPluginFile());
        List<String> classNames = new ArrayList<>();
        List<Class<?>> classes = new ArrayList<>();

        try {
            // Première phase: scanner le JAR pour trouver les classes
            plugin.getLogger().info("Scanning package " + basePackage.replace('/', '.') + " for services...");

            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.startsWith(basePackage) && name.endsWith(".class") && !name.endsWith("package-info.class")) {
                    boolean load = subPackages.length == 0; // Si aucun sous-package spécifié, charger toutes les classes

                    for (String sub : subPackages) {
                        if (name.startsWith(sub, basePackage.length())) {
                            load = true;
                            break;
                        }
                    }

                    if (load) {
                        String className = name.replace('/', '.').substring(0, name.length() - ".class".length());
                        classNames.add(className);
                    }
                }
            }

            // Trier les noms de classe de manière insensible à la casse
            classNames.sort(String::compareToIgnoreCase);
            plugin.getLogger().info("Found " + classNames.size() + " potential service classes");

            // Deuxième phase: charger les classes et filtrer les services
            for (String className : classNames) {
                try {
                    Class<?> clazz = Class.forName(className, true, plugin.getClass().getClassLoader());
                    classes.add(clazz);
                } catch (ClassNotFoundException | NoClassDefFoundError ex) {
                    plugin.getLogger().warning("Cannot load class " + className + ": " + ex.getMessage());
                } catch (ExceptionInInitializerError err) {
                    plugin.getLogger().warning("Class " + className + " generated an exception while loading: " + err.getCause().getMessage());
                }
            }

            // Troisième phase: enregistrer les services et traiter les dépendances
            Map<Class<?>, Object> newServices = new HashMap<>();

            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(Service.class)) {
                    try {
                        Service serviceAnnotation = clazz.getAnnotation(Service.class);

                        // Trouver l'interface de service
                        Class<?> serviceInterface = serviceAnnotation.value();
                        if (serviceInterface == Void.class) {
                            // Si aucune interface n'est spécifiée, utiliser la première interface implémentée
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

                        // Instancier le service
                        final var pluginConstructor = getNullableDeclaredConstructor(clazz, TerraWars.class);
                        final var emptyConstructor = getNullableDeclaredConstructor(clazz);

                        Object instance;
                        if (pluginConstructor != null) {
                            pluginConstructor.setAccessible(true);
                            instance = pluginConstructor.newInstance(plugin);
                        } else if (emptyConstructor != null) {
                            emptyConstructor.setAccessible(true);
                            instance = emptyConstructor.newInstance();
                        } else {
                            plugin.getLogger().warning("No suitable constructor found for service class " + clazz.getName());
                            continue;
                        }

                        // Enregistrer le service
                        services.put(serviceInterface, instance);
                        newServices.put(serviceInterface, instance);

                        plugin.getLogger().info("Registered service: " + serviceInterface.getName() + " with implementation: " + clazz.getName());

                    } catch (Exception e) {
                        Throwable throwable;
                        if (e.getMessage() == null && e.getCause() != null)
                            throwable = e.getCause();
                        else
                            throwable = e;

                        plugin.getLogger().severe("Failed to register service: " + clazz.getName() + " - " + throwable.getMessage());
                        throwable.printStackTrace();
                    }
                }
            }

            // Quatrième phase: injecter les dépendances
            for (Map.Entry<Class<?>, Object> entry : newServices.entrySet()) {
                injectDependencies(entry.getValue());
            }

            // Cinquième phase: initialiser les services
            for (Map.Entry<Class<?>, Object> entry : newServices.entrySet()) {
                Object service = entry.getValue();
                if (service instanceof IServiceProvider)
                    continue;

                if (service instanceof final IService serv)
                    serv.init();
            }

            plugin.getLogger().info("Service injection completed. Total registered services: " + newServices.size());

        } finally {
            try {
                jar.close();
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to close JAR file: " + e.getMessage());
            }
        }
    }

    /**
     * Récupère le fichier JAR du plugin
     */
    private File getPluginFile() {
        try {
            Method method = plugin.getClass().getMethod("getFile");
            return (File) method.invoke(plugin);
        } catch (Exception e) {
            // Fallback pour obtenir le fichier JAR à partir du ClassLoader
            URL url = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
            try {
                return new File(url.toURI());
            } catch (Exception ex) {
                throw new RuntimeException("Cannot locate plugin JAR file", ex);
            }
        }
    }

    /**
     * Récupère un constructeur avec les paramètres spécifiés, retourne null s'il n'existe pas
     */
    private <T> java.lang.reflect.Constructor<T> getNullableDeclaredConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Override
    public void disableServices() {
        for (Object service : services.values())
            if (service instanceof IService iService)
                iService.destroy();
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
