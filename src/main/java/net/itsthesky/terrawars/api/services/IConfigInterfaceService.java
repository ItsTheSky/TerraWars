package net.itsthesky.terrawars.api.services;

import net.itsthesky.terrawars.core.gui.ConfigGuiContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Service for managing configuration interfaces.
 * Provides methods to open configuration GUIs for different types of objects.
 */
public interface IConfigInterfaceService {

    /**
     * Opens a configuration GUI for the specified object.
     *
     * @param player The player to open the GUI for
     * @param object The object to configure
     * @param saveCallback Callback to call when the configuration is saved
     * @param <T> The type of the object to configure
     */
    <T> void openConfigGui(@NotNull Player player, @NotNull T object, @NotNull ConfigSaveCallback<T> saveCallback);

    /**
     * Opens a configuration GUI for the specified object with a title.
     *
     * @param player The player to open the GUI for
     * @param object The object to configure
     * @param title The title of the GUI
     * @param saveCallback Callback to call when the configuration is saved
     * @param <T> The type of the object to configure
     */
    <T> void openConfigGui(@NotNull Player player, @NotNull T object, @NotNull String title, @NotNull ConfigSaveCallback<T> saveCallback);

    /**
     * Reopens a configuration GUI using an existing context.
     * @param player The player to open the GUI for
     * @param context The existing context
     */
    void reopenConfigGui(@NotNull Player player, @NotNull ConfigGuiContext<?> context);

    /**
     * Callback to call when a configuration is saved.
     *
     * @param <T> The type of the object being configured
     */
    @FunctionalInterface
    interface ConfigSaveCallback<T> {
        void onSave(@NotNull T object);
    }
}