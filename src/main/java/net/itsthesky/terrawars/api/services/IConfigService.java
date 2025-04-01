package net.itsthesky.terrawars.api.services;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface IConfigService {

    <T> @NotNull T load(@NotNull Class<T> clazz, @NotNull String path);

    <T> @NotNull T load(@NotNull Class<T> clazz, @NotNull InputStream stream);

    void save(@NotNull Object object, @NotNull String path);

    void save(@NotNull Object object, @NotNull OutputStream stream);

    @NotNull File getFile(@NotNull String... path);
}
