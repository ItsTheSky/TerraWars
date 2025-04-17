package net.itsthesky.terrawars.api.services;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.itsthesky.terrawars.util.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
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

    @NotNull GuiItem createResetButton(@NotNull Consumer<InventoryClickEvent> onClick);

    @NotNull GuiItem createAddButton(@NotNull Consumer<InventoryClickEvent> onClick);

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
    }

    @NotNull GuiItem createChatInputControl(@NotNull String instructions,
                                            @NotNull Predicate<String> validator,
                                            @NotNull InputControlData<String> inputData);

    default @NotNull GuiItem createChatInputControl(@NotNull String instructions,
                                                    @NotNull InputControlData<String> inputData) {
        return createChatInputControl(instructions, s -> true, inputData);
    }

    @NotNull GuiItem createNumericInputControl(@NotNull String instructions,
                                                       @NotNull InputControlData<Integer> inputData);

    @NotNull GuiItem createLocationInputControl(@NotNull String instructions,
                                                @NotNull Predicate<Location> validator,
                                                @NotNull InputControlData<Location> inputData);

    default @NotNull GuiItem createLocationInputControl(@NotNull String instructions,
                                                        @NotNull InputControlData<Location> inputData) {
        return createLocationInputControl(instructions, l -> true, inputData);
    }

    <T> @NotNull GuiItem createComboBoxInputControl(@NotNull List<T> options,
                                                    @NotNull InputControlData<T> inputData,
                                                    @NotNull Function<T, String> displayFunction);

    default <T> @NotNull GuiItem createComboBoxInputControl(@NotNull List<T> options,
                                                            @NotNull InputControlData<T> inputData) {
        return createComboBoxInputControl(options, inputData, Objects::toString);
    }

    @NotNull GuiItem createWorldSelectorInputControl(@Nullable Predicate<World> filter,
                                                     @NotNull InputControlData<World> inputData);

    <T> @NotNull GuiItem createChoiceInputControl(@NotNull String name,
                                                  @NotNull List<String> description,
                                                  @NotNull List<T> options,
                                                  @NotNull Function<T, ItemStack> displayFunction,
                                                  @NotNull Function<T, String> displayNameFunction,
                                                  @NotNull InputControlData<T> inputData);

    @NotNull GuiItem createSubMenuInputControl(@NotNull String name,
                                               @NotNull List<String> description,
                                               @NotNull Material material,
                                               @NotNull Consumer<InventoryClickEvent> onClick);

    //endregion

    default @NotNull PatternPane createBaseBorderPane(int height) {
        return createBaseBorderPane(height, ItemBuilder.fill());
    }

    @NotNull PatternPane createBaseBorderPane(int height, @NotNull ItemStack fill);

}
