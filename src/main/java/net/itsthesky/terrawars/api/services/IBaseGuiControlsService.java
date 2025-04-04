package net.itsthesky.terrawars.api.services;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Provides basic controls for GUI elements.
 */
public interface IBaseGuiControlsService {

    //region (Very) Basic Controls

    @NotNull GuiItem createBackButton(@NotNull Consumer<InventoryClickEvent> onClick);

    @NotNull GuiItem createNextButton(@NotNull Consumer<InventoryClickEvent> onClick);

    @NotNull GuiItem createPreviousButton(@NotNull Consumer<InventoryClickEvent> onClick);

    @NotNull GuiItem createCloseButton(@NotNull Consumer<InventoryClickEvent> onClick);

    @NotNull GuiItem createConfirmButton(@NotNull Consumer<InventoryClickEvent> onClick);

    //endregion

    //region Input Controls

    @Getter
    @Setter
    @AllArgsConstructor
    final class InputControlData<T> {
        private final @NotNull Material material;
        private final @Nullable T defaultValue;
        private @Nullable T currentValue;
        private final @NotNull String name;
        private final @NotNull List<String> description;
        private final @NotNull BiConsumer<T, InputControlData<T>> onInput;
        private final @NotNull BiConsumer<InventoryClickEvent, InputControlData<T>> onCancel;
        private final @Nullable BiConsumer<InventoryClickEvent, InputControlData<T>> onReset;
    }

    @NotNull GuiItem createChatInputControl(@NotNull String instructions,
                                            @NotNull Predicate<String> validator,
                                            @NotNull InputControlData<String> inputData);

    default @NotNull GuiItem createChatInputControl(@NotNull String instructions,
                                                    @NotNull InputControlData<String> inputData) {
        return createChatInputControl(instructions, s -> true, inputData);
    }

    @NotNull GuiItem createLocationInputControl(@NotNull String instructions,
                                                @NotNull InputControlData<Location> inputData);

    <T> @NotNull GuiItem createComboBoxInputControl(@NotNull List<T> options,
                                                    @NotNull InputControlData<T> inputData,
                                                    @NotNull Function<T, String> displayFunction);

    default <T> @NotNull GuiItem createComboBoxInputControl(@NotNull List<T> options,
                                                            @NotNull InputControlData<T> inputData) {
        return createComboBoxInputControl(options, inputData, Objects::toString);
    }

    //endregion

}
