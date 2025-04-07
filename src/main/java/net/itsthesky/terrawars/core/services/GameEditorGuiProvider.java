package net.itsthesky.terrawars.core.services;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.generator.GameGeneratorType;
import net.itsthesky.terrawars.api.services.IBaseGuiControlsService;
import net.itsthesky.terrawars.api.services.IBiomeService;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.IGameEditorGuiProvider;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.api.services.base.Service;
import net.itsthesky.terrawars.core.config.GameConfig;
import net.itsthesky.terrawars.core.config.GameGeneratorConfig;
import net.itsthesky.terrawars.core.config.GameTeamConfig;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class GameEditorGuiProvider implements IGameEditorGuiProvider {

    @Inject
    private IChatService chatService;
    @Inject
    private IBaseGuiControlsService baseGuiControlsService;
    @Inject
    private IBiomeService biomeService;

    @Override
    public @NotNull ChestGui createGameEditorGui(@NotNull GameConfig config) {

        final var rows = 4;
        final var chestGui = new ChestGui(rows, ComponentHolder.of(chatService.format(
                "<accent><b>→</b> <base>Game Configuration Editor", Colors.FUCHSIA
        )));
        final var configItemPane = new StaticPane(1, 1, 7, rows - 2);
        final var controlsPane = new StaticPane(0, rows - 1, 9, 1);
        final var decoPane = baseGuiControlsService.createBaseBorderPane(rows);

        populateConfigItemPane(configItemPane, config);
        populateTeamConfigItemPane(configItemPane, config);

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
                        null, config.getWorld(),
                        "Game World",
                        List.of("In what world should the game be played."),
                        (world, inputData) -> {
                            inputData.setCurrentValue(world);
                            config.setWorld(world);
                            config.save();
                        }
                )), Slot.fromIndex(0));

        pane.addItem(baseGuiControlsService.createLocationInputControl("Move to the lobby location, then <base>right click the emerald<text>!",
                new IBaseGuiControlsService.InputControlData<>(
                        Material.RED_BED,
                        null, config.getLobby(),
                        "Game Lobby Location",
                        List.of("The lobby location, where players will spawn", "first, before the game starts."),
                        (location, inputData) -> {
                            inputData.setCurrentValue(location);
                            config.setLobby(location);
                            config.save();
                        }
                )), Slot.fromIndex(1));

        pane.addItem(baseGuiControlsService.createComboBoxInputControl(Arrays.stream(IGame.GameSize.values()).toList(),
                new IBaseGuiControlsService.InputControlData<>(
                        Material.NOTE_BLOCK,
                        IGame.GameSize.SOLO, config.getGameSize(),
                        "Game Size",
                        List.of("The amount of players <accent>per team<text>.", "You can't change the amount of teams (4)."),
                        (gameSize, inputData) -> {
                            inputData.setCurrentValue(gameSize);
                            config.setGameSize(gameSize);
                            config.save();
                        }
                ), size -> switch (size) {
                    case SOLO -> "Solo (1v1v1v1)";
                    case DUO -> "Duo (2v2v2v2)";
                    case SQUAD -> "Squad (4v4v4v4)";
                }), Slot.fromIndex(2));

        pane.addItem(baseGuiControlsService.createSubMenuInputControl(
                "Configure Generators",
                List.of("Edit the generators configuration", "for the game."),
                Material.IRON_BLOCK,
                evt -> {
                    final var generatorGui = createGeneratorsConfigGui(
                            (ChestGui) Objects.requireNonNull(evt.getInventory().getHolder()),
                            config);
                    generatorGui.show(evt.getWhoClicked());
                }
        ), Slot.fromIndex(3));
    }

    private void populateTeamConfigItemPane(@NotNull final StaticPane pane, @NotNull final GameConfig config) {
        int x = 0;
        final var wools = new Material[]{
                Material.LIGHT_BLUE_WOOL,
                Material.YELLOW_WOOL,
                Material.RED_WOOL,
                Material.GREEN_WOOL
        };
        for (final var team : config.getTeams()) {
            final var slot = Slot.fromXY(x, 1);

            int finalX = x;
            final var item = baseGuiControlsService.createSubMenuInputControl(
                    "Configure Team #" + (x + 1),
                    List.of("Edit the team configuration", "for team #" + (x + 1)),
                    wools[x],
                    evt -> {
                        final var teamGui = createTeamConfigGui(
                                (ChestGui) Objects.requireNonNull(evt.getInventory().getHolder()),
                                config, team, finalX + 1);
                        teamGui.show(evt.getWhoClicked());
                    }
            );

            pane.addItem(item, slot);
            x++;
        }
    }

    private @NotNull ChestGui createTeamConfigGui(@NotNull ChestGui main,
                                                  @NotNull GameConfig gameConfig,
                                                  @NotNull GameTeamConfig teamConfig,
                                                  int teamIndex) {
        final var rows = 3;
        final var chestGui = new ChestGui(rows, ComponentHolder.of(chatService.format(
                "<accent><b>→</b> <base>Team #"+ teamIndex +" Editor", Colors.FUCHSIA
        )));
        final var configItemPane = new StaticPane(1, 1, 7, rows - 2);
        final var controlsPane = new StaticPane(0, rows - 1, 9, 1);
        final var decoPane = baseGuiControlsService.createBaseBorderPane(rows);

        // add team config items (directly this time :>)
        configItemPane.addItem(baseGuiControlsService.createLocationInputControl(
                "Move to the spawn location, then <base>right click the emerald<text>!",
                new IBaseGuiControlsService.InputControlData<>(
                        Material.GRASS_BLOCK,
                        null, teamConfig.getSpawnLocation(),
                        "Spawn Location",
                        List.of("The location where the team will spawn", "in the game."),
                        (location, inputData) -> {
                            inputData.setCurrentValue(location);
                            teamConfig.setSpawnLocation(location);
                            gameConfig.save();
                        }
                )
        ), Slot.fromIndex(0));

        configItemPane.addItem(baseGuiControlsService.createLocationInputControl(
                "Move to the nexus location, then <base>right click the emerald<text> (you may click on a <accent>block<text> too)!",
                new IBaseGuiControlsService.InputControlData<>(
                        Material.END_CRYSTAL,
                        null, teamConfig.getNexusLocation(),
                        "Nexus Location",
                        List.of("The location where the nexus will be placed", "in the game. It'll be replaced", "by an end crystal on start."),
                        (location, inputData) -> {
                            inputData.setCurrentValue(location);
                            teamConfig.setNexusLocation(location);
                            gameConfig.save();
                        }
                )
        ), Slot.fromIndex(1));

        configItemPane.addItem(baseGuiControlsService.createLocationInputControl(
                "Move to the nexus location, then <base>right click the emerald<text>!",
                new IBaseGuiControlsService.InputControlData<>(
                        Material.IRON_BLOCK,
                        null, teamConfig.getGeneratorLocation(),
                        "Generator Location",
                        List.of("The location where the generator will be placed", "in the game. Try planning a little", "upper the actual generator."),
                        (location, inputData) -> {
                            inputData.setCurrentValue(location);
                            teamConfig.setGeneratorLocation(location);
                            gameConfig.save();
                        }
                )
        ), Slot.fromIndex(2));

        final List<IBiome> biomes = biomeService.getAvailableBiomes();
        configItemPane.addItem(baseGuiControlsService.createChoiceInputControl("Select Biome", List.of(
                "The biome of the team, to be used", "in the game."
        ), biomes, value -> {
            final var lore = new ArrayList<String>();
            lore.add("");
            for (final var line : value.getDescription())
                lore.add("<i><text>" + line);
            lore.add("");
            lore.add("<text>Click to select this biome.");
            return new ItemBuilder(value.getMainBlock())
                    .name("<accent><b>✏</b> <base>" + value.getName(), value.getScheme())
                    .lore(value.getScheme(), lore)
                    .getItem();
        }, IBiome::getName, new IBaseGuiControlsService.InputControlData<>(
                Material.CHERRY_SAPLING,
                null, biomeService.getBiome(teamConfig.getBiomeId()),
                "Biome",
                List.of("The biome of the team, to be used", "in the game."),
                (biome, inputData) -> {
                    inputData.setCurrentValue(biome);
                    teamConfig.setBiomeId(biome == null ? null : biome.getId());
                    gameConfig.save();
                }
        )), Slot.fromIndex(3));

        // add base controls
        controlsPane.addItem(baseGuiControlsService.createBackButton(evt -> main.show(evt.getWhoClicked())),
                Slot.fromIndex(0));

        chestGui.addPane(decoPane);
        chestGui.addPane(configItemPane);
        chestGui.addPane(controlsPane);

        return chestGui;
    }

    private @NotNull GuiItem createGeneratorItem(@NotNull GameGeneratorConfig generator,
                                                 @NotNull GameConfig gameConfig,
                                                 @NotNull ChestGui main,
                                                 @NotNull Slot slot,
                                                 @NotNull StaticPane pane,
                                                 int i) {
        final var material = switch (generator.getGeneratorType()) {
            case BASE -> Material.IRON_BLOCK;
            case DIAMOND -> Material.DIAMOND_BLOCK;
            case EMERALD -> Material.EMERALD_BLOCK;
            case AMETHYST -> Material.AMETHYST_BLOCK;
        };

        return baseGuiControlsService.createSubMenuInputControl(
                "Configure Generator #" + (i + 1),
                List.of("Edit the generator configuration", "for generator #" + (i + 1)),
                material,
                evt -> {
                    if (evt.getClick() == ClickType.DROP) {
                        gameConfig.getGenerators().remove(generator);
                        gameConfig.save();

                        final var generatorGui = createGeneratorsConfigGui(
                                main, gameConfig);
                        generatorGui.show(evt.getWhoClicked());
                        return;
                    }

                    final var gui = (ChestGui) Objects.requireNonNull(evt.getInventory().getHolder());
                    final var generatorGui = createGeneratorConfigGui(
                            gui, gameConfig, generator, slot, pane, i + 1);
                    generatorGui.show(evt.getWhoClicked());
                }
        );
    }

    private @NotNull ChestGui createGeneratorsConfigGui(@NotNull ChestGui main,
                                                       @NotNull GameConfig gameConfig) {
        final var generators = gameConfig.getGenerators();
        final int rows = Math.max(3, generators.size() /  + 2);
        final var chestGui = new ChestGui(rows, ComponentHolder.of(chatService.format(
                "<accent><b>→</b> <base>Generator Editor", Colors.FUCHSIA
        )));

        final var generatorsPane = new StaticPane(1, 1, 7, rows - 2);
        final var decoPane = baseGuiControlsService.createBaseBorderPane(rows);
        final var controlsPane = new StaticPane(0, rows - 1, 9, 1);

        for (int i = 0; i < generators.size(); i++) {
            final var generator = generators.get(i);
            final var slot = Slot.fromIndex(i);
            final var item = createGeneratorItem(generator, gameConfig, main, slot, generatorsPane, i);
            generatorsPane.addItem(item, slot);
        }

        // add base controls
        controlsPane.addItem(baseGuiControlsService.createBackButton(evt -> main.show(evt.getWhoClicked())),
                Slot.fromIndex(0));
        controlsPane.addItem(baseGuiControlsService.createAddButton(evt -> {
            gameConfig.getGenerators().add(new GameGeneratorConfig());
            gameConfig.save();

            final var gui = createGeneratorsConfigGui(
                    main, gameConfig);
            gui.show(evt.getWhoClicked());
        }), Slot.fromIndex(8));

        chestGui.addPane(decoPane);
        chestGui.addPane(generatorsPane);
        chestGui.addPane(controlsPane);

        return chestGui;
    }

    private @NotNull ChestGui createGeneratorConfigGui(@NotNull ChestGui parent,
                                                       @NotNull GameConfig gameConfig,
                                                       @NotNull GameGeneratorConfig generatorConfig,
                                                       @NotNull Slot slot,
                                                       @NotNull StaticPane pane,
                                                       int generatorIndex) {
        final var rows = 3;
        final var chestGui = new ChestGui(rows, ComponentHolder.of(chatService.format(
                "<accent><b>→</b> <base>Generator #" + generatorIndex + " Editor", Colors.FUCHSIA
        )));
        final var configItemPane = new StaticPane(1, 1, 7, rows - 2);
        final var controlsPane = new StaticPane(0, rows - 1, 9, 1);
        final var decoPane = baseGuiControlsService.createBaseBorderPane(rows);

        configItemPane.addItem(baseGuiControlsService.createLocationInputControl(
                "Move to the generator location, then <base>right click the emerald<text>!",
                new IBaseGuiControlsService.InputControlData<>(
                        Material.COMPASS,
                        null, generatorConfig.getGeneratorLocation(),
                        "Generator Location",
                        List.of("The location where the generator will be placed", "in the game. Try planning a little", "upper the actual generator."),
                        (location, inputData) -> {
                            inputData.setCurrentValue(location);
                            generatorConfig.setGeneratorLocation(location);
                            gameConfig.save();
                        }
                )
        ), Slot.fromIndex(0));

        configItemPane.addItem(baseGuiControlsService.createComboBoxInputControl(GameGeneratorType.CONFIGURABLE_TYPES,
                new IBaseGuiControlsService.InputControlData<>(
                        Material.BRICK,
                        GameGeneratorType.DIAMOND, generatorConfig.getGeneratorType(),
                        "Generator Type",
                        List.of("The type of generator", "to be used in the game."),
                        (genType, inputData) -> {
                            inputData.setCurrentValue(genType);
                            generatorConfig.setGeneratorType(genType);
                            gameConfig.save();

                            final var newItem = createGeneratorItem(generatorConfig, gameConfig, parent, slot, pane, generatorIndex - 1);
                            pane.removeItem(slot);
                            pane.addItem(newItem, slot);

                            parent.update();
                        }
                ), genType -> switch (genType) {
                    case BASE -> "Base (Iron/Gold)";
                    case DIAMOND -> "Diamond";
                    case EMERALD -> "Emerald";
                    case AMETHYST -> "Amethyst";
                }), Slot.fromIndex(1));

        // add base controls
        controlsPane.addItem(baseGuiControlsService.createBackButton(evt -> parent.show(evt.getWhoClicked())),
                Slot.fromIndex(0));

        chestGui.addPane(decoPane);
        chestGui.addPane(configItemPane);
        chestGui.addPane(controlsPane);

        return chestGui;
    }
}
