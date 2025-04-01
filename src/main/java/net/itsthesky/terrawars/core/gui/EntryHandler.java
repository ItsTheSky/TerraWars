package net.itsthesky.terrawars.core.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Handles the creation of GUI items for a specific type.
 * @param <T> The type this handler processes
 */
public interface EntryHandler<T> {

    /**
     * Gets the types this handler supports.
     * @return A set of types this handler can handle
     */
    @NotNull Set<Class<?>> getSupportedTypes();

    /**
     * Checks if this handler supports the given type.
     * @param type The type to check
     * @return True if this handler supports the type, false otherwise
     */
    default boolean supports(@NotNull Class<?> type) {
        return getSupportedTypes().stream().anyMatch(supportedType -> supportedType.isAssignableFrom(type));
    }

    /**
     * Creates a GUI item for the given entry.
     * @param context The GUI context
     * @param entryInfo The entry information
     * @param value The current value of the entry
     * @return The created GUI item
     */
    @NotNull GuiItem createItem(@NotNull ConfigGuiContext<?> context,
                                @NotNull ConfigEntryInfo entryInfo,
                                @Nullable Object value);

    /**
     * Resets the value of the entry to its default value.
     * @param context The GUI context
     * @param entryInfo The entry information
     */
    default void resetToDefault(@NotNull ConfigGuiContext<?> context, @NotNull ConfigEntryInfo entryInfo) {
        try {
            // Get the default value from a new instance
            Object defaultInstance = entryInfo.getField().getDeclaringClass().getDeclaredConstructor().newInstance();
            Field field = entryInfo.getField();

            boolean accessible = field.canAccess(defaultInstance);
            if (!accessible) {
                field.setAccessible(true);
            }

            Object defaultValue = field.get(defaultInstance);

            if (!accessible) {
                field.setAccessible(false);
            }

            // Set the value in the current instance
            context.setFieldValue(entryInfo.getField(), defaultValue);
        } catch (Exception e) {
            // If an error occurs, just log it
            e.printStackTrace();
        }
    }

    /**
     * Formats a value for display in the GUI.
     * @param value The value to format
     * @return A string representation of the value
     */
    @NotNull String formatValue(@Nullable T value);

    /**
     * Starts the editing process for the given entry.
     * @param context The GUI context
     * @param entryInfo The entry information
     */
    void startEdit(@NotNull ConfigGuiContext<?> context, @NotNull ConfigEntryInfo entryInfo);
}