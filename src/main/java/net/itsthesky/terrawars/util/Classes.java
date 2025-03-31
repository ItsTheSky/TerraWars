package net.itsthesky.terrawars.util;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Classes {

    public static Set<Class<?>> getClasses(String packageName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        Set<Class<?>> classes = new HashSet<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();
            
            if (protocol.equals("file")) {
                findClasses(new File(resource.getFile()), packageName, classes);
            } else if (protocol.equals("jar")) {
                String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
                JarFile jar = new JarFile(jarPath);
                Enumeration<JarEntry> entries = jar.entries();
                
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    
                    if (name.startsWith(path) && name.endsWith(".class")) {
                        String className = name.replace('/', '.').substring(0, name.length() - 6);
                        try {
                            Class<?> clazz = Class.forName(className);
                            classes.add(clazz);
                        } catch (ClassNotFoundException e) {
                            // Ignorer les classes qui ne peuvent pas être chargées
                        }
                    }
                }
                
                jar.close();
            }
        }
        
        return classes;
    }

    private static void findClasses(File directory, String packageName, Set<Class<?>> classes) {
        if (!directory.exists()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findClasses(file, packageName + "." + file.getName(), classes);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        classes.add(clazz);
                    } catch (ClassNotFoundException e) {
                        // Ignorer les classes qui ne peuvent pas être chargées
                    }
                }
            }
        }
    }

    public static <T> @Nullable Constructor<T> getNullableDeclaredConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}