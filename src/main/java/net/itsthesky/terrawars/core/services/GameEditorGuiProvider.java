package net.itsthesky.terrawars.core.services;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.services.IBaseGuiControlsService;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.IGameEditorGuiProvider;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.api.services.base.Service;
import net.itsthesky.terrawars.core.config.GameConfig;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Service
public class GameEditorGuiProvider implements IGameEditorGuiProvider {

    @Inject
    private IChatService chatService;
    @Inject
    private IBaseGuiControlsService baseGuiControlsService;

    @Override
    public @NotNull ChestGui createGameEditorGui(@NotNull GameConfig config) {

        final var rows = 3;
        final var chestGui = new ChestGui(rows, ComponentHolder.of(chatService.format(
                "<accent>Game Configuration Editor", Colors.FUCHSIA
        )));
        final var configItemPane = new StaticPane(1, 1, 7, rows - 2);
        final var controlsPane = new StaticPane(0, rows - 1, 9, 1);
        final var decoPane = baseGuiControlsService.createBaseBorderPane(rows);

        populateConfigItemPane(configItemPane, config);

        // add base controls
        controlsPane.addItem(baseGuiControlsService.createCloseButton(evt -> evt.getWhoClicked().closeInventory()),
                Slot.fromIndex(4));

        chestGui.addPane(decoPane);
        chestGui.addPane(configItemPane);
        chestGui.addPane(controlsPane);

        return chestGui;
    }

    private void populateConfigItemPane(@NotNull final StaticPane pane, @NotNull final GameConfig config) {
        pane.addItem(baseGuiControlsService.createWorldSelectorInputControl(w -> true,
                new IBaseGuiControlsService.InputControlData<>(
                        Material.ACACIA_SAPLING,
                        null, null,
                        "Game World",
                        List.of("In what world should the game be played."),
                        (world, inputData) -> {
                            inputData.setCurrentValue(world);
                            config.setWorld(world);
                        },
                        (evt, inputData) -> {},
                        (evt, inputData) -> {
                            inputData.setCurrentValue(null);
                            config.setWorld(null);
                        }
                )), Slot.fromIndex(0));

        pane.addItem(baseGuiControlsService.createLocationInputControl("Move to the lobby location, then <base>right click the emerald<text>!",
                new IBaseGuiControlsService.InputControlData<>(
                        Material.RED_BED,
                        null, null,
                        "Game Lobby Location",
                        List.of("The lobby location, where players will spawn", "first, before the game starts."),
                        (location, inputData) -> {
                            inputData.setCurrentValue(location);
                            config.setLobby(location);
                        },
                        (location, inputData) -> {},
                        (location, inputData) -> {
                            inputData.setCurrentValue(null);
                            config.setLobby(null);
                        }
                )), Slot.fromIndex(1));

        pane.addItem(baseGuiControlsService.createComboBoxInputControl(Arrays.stream(IGame.GameSize.values()).toList(),
                new IBaseGuiControlsService.InputControlData<>(
                        Material.NOTE_BLOCK,
                        IGame.GameSize.SOLO, IGame.GameSize.SOLO,
                        "Game Size",
                        List.of("The amount of players <accent>per team<text>.", "You can't change the amount of teams (4)."),
                        (gameSize, inputData) -> {
                            inputData.setCurrentValue(gameSize);
                            config.setGameSize(gameSize);
                        },
                        (evt, inputData) -> { },
                        (evt, inputData) -> {
                            inputData.setCurrentValue(IGame.GameSize.SOLO);
                            config.setGameSize(IGame.GameSize.SOLO);
                        }
                ), size -> switch (size) {
                    case SOLO -> "Solo (1v1v1v1)";
                    case DUO -> "Duo (2v2v2v2)";
                    case SQUAD -> "Squad (4v4v4v4)";
                }), Slot.fromIndex(2));

        pane.addItem(baseGuiControlsService.createSubMenuInputControl(
                "Edit Teams",
                List.of("Edit the teams of the game."),
                Material.IRON_SWORD,
                event -> {

                }
        ), Slot.fromIndex(3));
    }
}
