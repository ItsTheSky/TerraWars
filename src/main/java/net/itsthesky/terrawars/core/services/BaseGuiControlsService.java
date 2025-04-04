package net.itsthesky.terrawars.core.services;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.api.services.IBaseGuiControlsService;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.base.Service;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
public class BaseGuiControlsService implements IBaseGuiControlsService {

    @Inject private IChatService chatService;

    public BaseGuiControlsService(TerraWars plugin) {
        plugin.getServer().getPluginManager().registerEvents(new ControlsListener(), plugin);
    }

    @Override
    public @NotNull GuiItem createBackButton(@NotNull Consumer<InventoryClickEvent> onClick) {
        return new GuiItem(new ItemBuilder(Material.ARROW)
                .name("<accent><b>»</b> <base>Back", Colors.ORANGE)
                .lore(Colors.ORANGE, "<text>Go back to the previous menu/page.")
                .getItem(), onClick);
    }

    @Override
    public @NotNull GuiItem createNextButton(@NotNull Consumer<InventoryClickEvent> onClick) {
        return new GuiItem(new ItemBuilder(Material.ARROW)
                .name("<accent><b>→</b> <base>Next", Colors.EMERALD)
                .lore(Colors.EMERALD, "<text>Go to the next menu/page.")
                .getItem(), onClick);
    }

    @Override
    public @NotNull GuiItem createPreviousButton(@NotNull Consumer<InventoryClickEvent> onClick) {
        return new GuiItem(new ItemBuilder(Material.ARROW)
                .name("<accent><b>←</b> <base>Previous", Colors.EMERALD)
                .lore(Colors.EMERALD, "<text>Go to the previous menu/page.")
                .getItem(), onClick);
    }

    @Override
    public @NotNull GuiItem createCloseButton(@NotNull Consumer<InventoryClickEvent> onClick) {
        return new GuiItem(new ItemBuilder(Material.BARRIER)
                .name("<accent><b>✖</b> <base>Close", Colors.RED)
                .lore(Colors.AMBER, "<text>Close the menu.")
                .getItem(), onClick);
    }

    @Override
    public @NotNull GuiItem createConfirmButton(@NotNull Consumer<InventoryClickEvent> onClick) {
        return new GuiItem(new ItemBuilder(Material.LIME_WOOL)
                .name("<accent><b>✔</b> <base>Confirm", Colors.LIME)
                .lore(Colors.LIME, "<text>Confirm the action.")
                .getItem(), onClick);
    }

    @Override
    public @NotNull GuiItem createResetButton(@NotNull Consumer<InventoryClickEvent> onClick) {
        return new GuiItem(new ItemBuilder(Material.RED_WOOL)
                .name("<accent><b>✖</b> <base>Reset", Colors.ROSE)
                .lore(Colors.ROSE, "<text>Reset the value to default.")
                .getItem(), onClick);
    }

    private final Map<UUID, Consumer<AsyncChatEvent>> waitingForChat = new HashMap<>();

    private <T> String format(@Nullable T value, @Nullable Function<T, String> displayFunction) {
        if (value == null)
            return "None";

        if (displayFunction != null)
            return displayFunction.apply(value);

        return value.toString();
    }

    private <T> String format(@Nullable T value) {
        return format(value, null);
    }

    private <T> List<String> buildChatLore(InputControlData<T> inputData) {
        final var lore = new ArrayList<String>();
        lore.add("");
        for (String line : inputData.getDescription())
            lore.add("<i><text>" + line);
        lore.add("");

        lore.add("[emerald]<accent>• <text>Current value: <base>" + format(inputData.getCurrentValue() != null ? inputData.getCurrentValue() : inputData.getDefaultValue()));
        lore.add("[emerald]<accent>• <text>Default value: <base>" + format(inputData.getDefaultValue() != null ? inputData.getDefaultValue() : "None"));

        lore.add("");
        lore.add("[green]<accent><b>✏</b> <base>Click to change this value!");
        lore.add("<shade-slate:600><i>You can <b>drop</b> the item to reset the value!</i>");
        return lore;
    }

    private <T> List<String> buildComboBoxLore(List<T> values,
                                               InputControlData<T> inputData,
                                               Function<T, String> displayFunction) {
        final var lore = new ArrayList<String>();
        lore.add("");
        for (String line : inputData.getDescription())
            lore.add("<i><text>" + line);
        lore.add("");

        final var currentValue = inputData.getCurrentValue() != null ? inputData.getCurrentValue() : inputData.getDefaultValue();
        for (final T value : values) {
            if (value.equals(currentValue))
                lore.add("[emerald]  <accent><b>→</b> <base>" + format(value, displayFunction));
            else
                lore.add("[slate]  <base>→ <text>" + format(value, displayFunction));
        }

        if (inputData.getCurrentValue() == null) {
            lore.add("");
            lore.add("[red]<accent>• <text>Current value is not set!");
        }

        lore.add("");
        lore.add("[green]<accent><b>✏</b> <base>Click to change this value!");
        lore.add("<shade-slate:600><i>You can <b>drop</b> the item to reset the value!</i>");
        return lore;
    }

    private List<String> buildLocationLore(InputControlData<Location> inputData) {
        final var lore = new ArrayList<String>();
        lore.add("");
        for (String line : inputData.getDescription())
            lore.add("<i><text>" + line);
        lore.add("");

        final var currentValue = inputData.getCurrentValue() != null ? inputData.getCurrentValue() : inputData.getDefaultValue();
        if (currentValue != null) {
            lore.add("[emerald]<accent>• <text>Current value: <base>" + currentValue.getWorld().getName() + " " + currentValue.getBlockX() + ", " + currentValue.getBlockY() + ", " + currentValue.getBlockZ());
        } else {
            lore.add("[red]<accent>• <text>Current value is not set!");
        }

        lore.add("");
        lore.add("[green]<accent><b>✏</b> <base>Click to change this value!");
        lore.add("<shade-slate:600><i>You can <b>drop</b> the item to reset the value!</i>");
        return lore;
    }

    private List<String> buildWorldSelectorLore(InputControlData<World> inputData) {
        final var lore = new ArrayList<String>();
        lore.add("");
        for (String line : inputData.getDescription())
            lore.add("<i><text>" + line);
        lore.add("");

        final var currentValue = inputData.getCurrentValue() != null ? inputData.getCurrentValue() : inputData.getDefaultValue();
        if (currentValue != null) {
            lore.add("[emerald]<accent>• <text>Current world: <base>" + currentValue.getName());
        } else {
            lore.add("[red]<accent>• <text>Current world is not set!");
        }

        lore.add("");
        lore.add("[green]<accent><b>✏</b> <base>Click to change this value!");
        lore.add("<shade-slate:600><i>You can <b>drop</b> the item to reset the value!</i>");
        return lore;
    }

    @Override
    public @NotNull GuiItem createChatInputControl(@NotNull String instructions, @NotNull Predicate<String> validator, @NotNull InputControlData<String> inputData) {
        final var item = new ItemBuilder(inputData.getMaterial())
                .name("<accent><b>✏</b> <base>" + inputData.getName(), Colors.YELLOW)
                .lore(Colors.YELLOW, buildChatLore(inputData).toArray(new String[0]));

        final Consumer<InventoryClickEvent> consumer = event -> {
            final @NotNull var gui = (ChestGui) Objects.requireNonNull(event.getInventory().getHolder());

            if (event.getClick().equals(ClickType.DROP)) {
                event.setCancelled(true);
                inputData.getOnInput().accept(inputData.getDefaultValue(), inputData);
                item.lore(Colors.YELLOW, buildChatLore(inputData).toArray(new String[0]));
                gui.update();
                return;
            }

            event.getWhoClicked().closeInventory();
            chatService.sendMessage(event.getWhoClicked(), IChatService.MessageSeverity.INFO, instructions);

            waitingForChat.put(event.getWhoClicked().getUniqueId(), asyncChatEvent -> BukkitUtils.sync(() -> {
                final var msg = chatService.unformat(asyncChatEvent.message());
                if (msg.equalsIgnoreCase("cancel")) {
                    chatService.sendMessage(asyncChatEvent.getPlayer(), IChatService.MessageSeverity.INFO, "Input cancelled.");
                    gui.show(asyncChatEvent.getPlayer());
                    return;
                }

                if (validator.test(msg)) {
                    inputData.getOnInput().accept(msg, inputData);
                    item.lore(Colors.YELLOW, buildChatLore(inputData).toArray(new String[0]));
                    gui.update();

                } else {
                    chatService.sendMessage(asyncChatEvent.getPlayer(), IChatService.MessageSeverity.ERROR, "Invalid input.");
                }

                gui.show(asyncChatEvent.getPlayer());
            }));
        };

        return new GuiItem(item.getItem(), consumer);
    }

    @Override
    public @NotNull GuiItem createNumericInputControl(@NotNull String instructions, @NotNull InputControlData<Integer> inputData) {
        final var item = new ItemBuilder(inputData.getMaterial())
                .name("<accent><b>✏</b> <base>" + inputData.getName(), Colors.YELLOW)
                .lore(Colors.YELLOW, buildChatLore(inputData).toArray(new String[0]));

        final Consumer<InventoryClickEvent> consumer = event -> {
            final @NotNull var gui = (ChestGui) Objects.requireNonNull(event.getInventory().getHolder());

            if (event.getClick().equals(ClickType.DROP)) {
                event.setCancelled(true);
                inputData.getOnInput().accept(inputData.getDefaultValue(), inputData);
                item.lore(Colors.YELLOW, buildChatLore(inputData).toArray(new String[0]));
                gui.update();
                return;
            }

            event.getWhoClicked().closeInventory();
            chatService.sendMessage(event.getWhoClicked(), IChatService.MessageSeverity.INFO, instructions);

            waitingForChat.put(event.getWhoClicked().getUniqueId(), asyncChatEvent -> BukkitUtils.sync(() -> {
                final var msg = chatService.unformat(asyncChatEvent.message());
                if (msg.equalsIgnoreCase("cancel")) {
                    chatService.sendMessage(asyncChatEvent.getPlayer(), IChatService.MessageSeverity.INFO, "Input cancelled.");
                    gui.show(asyncChatEvent.getPlayer());
                    return;
                }

                try {
                    final var value = Integer.parseInt(msg);
                    inputData.getOnInput().accept(value, inputData);
                    item.lore(Colors.YELLOW, buildChatLore(inputData).toArray(new String[0]));
                    gui.update();
                } catch (NumberFormatException e) {
                    chatService.sendMessage(asyncChatEvent.getPlayer(), IChatService.MessageSeverity.ERROR, "Invalid input (must be a number).");
                }

                gui.show(asyncChatEvent.getPlayer());
            }));
        };

        return new GuiItem(item.getItem(), consumer);
    }

    private final Map<UUID, Consumer<PlayerInteractEvent>> waitingForLocation = new HashMap<>();
    private final Map<UUID, List<ItemStack>> locationHotbars = new HashMap<>();

    @Override
    public @NotNull <T> GuiItem createComboBoxInputControl(@NotNull List<T> options,
                                                           @NotNull InputControlData<T> inputData,
                                                           @NotNull Function<T, String> displayFunction) {
        final var item = new ItemBuilder(inputData.getMaterial())
                .name("<accent><b>✏</b> <base>" + inputData.getName(), Colors.YELLOW)
                .lore(Colors.YELLOW, buildComboBoxLore(options, inputData, displayFunction).toArray(new String[0]));

        final Consumer<InventoryClickEvent> consumer = event -> {
            final @NotNull var gui = (ChestGui) Objects.requireNonNull(event.getInventory().getHolder());
            event.setCancelled(true);
            if (event.getClick().equals(ClickType.DROP)) {
                inputData.getOnInput().accept(inputData.getDefaultValue(), inputData);
                item.lore(Colors.YELLOW, buildComboBoxLore(options, inputData, displayFunction).toArray(new String[0]));
                gui.update();
                return;
            }

            T newValue;
            if (inputData.getCurrentValue() == null) {
                newValue = options.get(0);
            } else {
                final var currentIndex = options.indexOf(inputData.getCurrentValue());
                if (currentIndex == -1) {
                    newValue = options.get(0);
                } else {
                    newValue = options.get((currentIndex + 1) % options.size());
                }
            }

            inputData.getOnInput().accept(newValue, inputData);
            item.lore(Colors.YELLOW, buildComboBoxLore(options, inputData, displayFunction).toArray(new String[0]));
            gui.update();
        };

        return new GuiItem(item.getItem(), consumer);
    }

    @Override
    public @NotNull GuiItem createLocationInputControl(@NotNull String instructions, @NotNull InputControlData<Location> inputData) {
        final var item = new ItemBuilder(inputData.getMaterial())
                .name("<accent><b>✏</b> <base>" + inputData.getName(), Colors.YELLOW)
                .lore(Colors.YELLOW, buildLocationLore(inputData).toArray(new String[0]));

        final Consumer<InventoryClickEvent> consumer = event -> {
            final @NotNull var gui = (ChestGui) Objects.requireNonNull(event.getInventory().getHolder());
            event.setCancelled(true);
            if (event.getClick().equals(ClickType.DROP)) {
                inputData.getOnInput().accept(inputData.getDefaultValue(), inputData);
                item.lore(Colors.YELLOW, buildLocationLore(inputData).toArray(new String[0]));
                gui.update();
                return;
            }

            if (event.getWhoClicked() instanceof Player player) {
                final var hotbar = new ArrayList<ItemStack>();
                for (int i = 0; i < 9; i++)
                    hotbar.add(player.getInventory().getItem(i));
                locationHotbars.put(player.getUniqueId(), hotbar);
            }
            for (int i = 0; i < 9; i++)
                event.getWhoClicked().getInventory().setItem(i, new ItemBuilder(Material.EMERALD)
                        .name("<accent><b>✔</b> <base>Click to set location", Colors.GREEN)
                        .lore(Colors.GREEN, "<text>Right click a block to set the location.")
                        .getItem());

            event.getWhoClicked().closeInventory();
            chatService.sendMessage(event.getWhoClicked(), IChatService.MessageSeverity.INFO, instructions);

            waitingForLocation.put(event.getWhoClicked().getUniqueId(), playerInteractEvent -> BukkitUtils.sync(() -> {
                final var player = playerInteractEvent.getPlayer();
                if (playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    final var block = playerInteractEvent.getClickedBlock();
                    if (block != null) {
                        final var location = block.getLocation();
                        inputData.getOnInput().accept(location, inputData);
                        item.lore(Colors.YELLOW, buildLocationLore(inputData).toArray(new String[0]));
                        gui.update();
                    }
                } else if (playerInteractEvent.getAction() == Action.RIGHT_CLICK_AIR) {
                    final var location = player.getLocation();
                    inputData.getOnInput().accept(location, inputData);
                    item.lore(Colors.YELLOW, buildLocationLore(inputData).toArray(new String[0]));
                    gui.update();
                }

                for (int i = 0; i < 9; i++)
                    player.getInventory().setItem(i, locationHotbars.get(player.getUniqueId()).get(i));
                locationHotbars.remove(player.getUniqueId());

                gui.show(player);
            }));
        };

        return new GuiItem(item.getItem(), consumer);
    }

    @Override
    public @NotNull GuiItem createWorldSelectorInputControl(@Nullable Predicate<World> filter,
                                                            @NotNull InputControlData<World> inputData) {
        final var mainItem = new ItemBuilder(inputData.getMaterial())
                .name("<accent><b>✏</b> <base>" + inputData.getName(), Colors.YELLOW)
                .lore(Colors.YELLOW, buildWorldSelectorLore(inputData).toArray(new String[0]));

        final Consumer<InventoryClickEvent> mainConsumer = event -> {
            event.setCancelled(true);
            final @NotNull var gui = (ChestGui) Objects.requireNonNull(event.getInventory().getHolder());

            if (event.getClick().equals(ClickType.DROP)) {
                inputData.getOnInput().accept(inputData.getDefaultValue(), inputData);
                mainItem.lore(Colors.YELLOW, buildWorldSelectorLore(inputData).toArray(new String[0]));
                gui.update();
                return;
            }

            final int rows = Math.max(3, Bukkit.getWorlds().size() / 9 + 2);
            final var chestGui = new ChestGui(rows, ComponentHolder.of(chatService.format(
                    "<accent>Select World", Colors.GREEN
            )));

            final var worldsListPane = new StaticPane(1, 1, 7, rows - 2);
            final var controlsPane = new StaticPane(0, rows - 1, 9, 1);
            final var decoPane = createBaseBorderPane(rows);

            final var worlds = Bukkit.getWorlds();
            int index = -1;
            for (final var world : worlds) {
                if (filter != null && !filter.test(world))
                    continue;

                index++;
                final var mat = switch (world.getEnvironment()) {
                    case NETHER -> Material.NETHERRACK;
                    case THE_END -> Material.END_STONE;
                    default -> Material.GRASS_BLOCK;
                };

                final var item = new ItemBuilder(mat)
                        .name("<accent><b>✏</b> <base>" + world.getName(), Colors.YELLOW)
                        .lore(Colors.YELLOW, "<text>Click to select this world.")
                        .getItem();

                final Consumer<InventoryClickEvent> consumer = evt -> {
                    event.setCancelled(true);
                    inputData.getOnInput().accept(world, inputData);
                    mainItem.lore(Colors.YELLOW, buildWorldSelectorLore(inputData).toArray(new String[0]));
                    gui.update();
                    gui.show(event.getWhoClicked());
                };

                worldsListPane.addItem(new GuiItem(item, consumer), Slot.fromIndex(index));
            }

            // controls
            controlsPane.addItem(createBackButton(evt -> {
                evt.setCancelled(true);
                gui.show(event.getWhoClicked());
            }), Slot.fromIndex(0));
            controlsPane.addItem(createResetButton(evt -> {
                evt.setCancelled(true);
                inputData.getOnInput().accept(inputData.getDefaultValue(), inputData);
                mainItem.lore(Colors.YELLOW, buildWorldSelectorLore(inputData).toArray(new String[0]));
                gui.update();
            }), Slot.fromIndex(4));

            chestGui.addPane(decoPane);
            chestGui.addPane(worldsListPane);
            chestGui.addPane(controlsPane);

            chestGui.show(event.getWhoClicked());
        };

        return new GuiItem(mainItem.getItem(), mainConsumer);
    }

    @Override
    public @NotNull GuiItem createSubMenuInputControl(@NotNull String name, @NotNull List<String> description, @NotNull Material material, @NotNull Consumer<InventoryClickEvent> onClick) {
        final var lore = new ArrayList<String>();
        lore.add("");
        for (String line : description)
            lore.add("<i><text>" + line);
        lore.add("");
        lore.add("[emerald]<accent>• <text>Click to open the sub-menu.");

        final var item = new ItemBuilder(material)
                .name("<accent><b>✏</b> <base>" + name, Colors.BLUE)
                .lore(Colors.BLUE, lore)
                .getItem();

        final Consumer<InventoryClickEvent> consumer = event -> {
            event.setCancelled(true);
            onClick.accept(event);
        };

        return new GuiItem(item, consumer);
    }

    public class ControlsListener implements Listener {

        @EventHandler
        public void onChat(AsyncChatEvent event) {
            final var uuid = event.getPlayer().getUniqueId();
            if (waitingForChat.containsKey(uuid)) {
                event.setCancelled(true);
                final var consumer = waitingForChat.remove(uuid);
                consumer.accept(event);
            }
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            final var uuid = event.getPlayer().getUniqueId();
            waitingForChat.remove(uuid);
            waitingForLocation.remove(uuid);
        }

        @EventHandler
        public void onInvClick(InventoryClickEvent event) {
            if (!waitingForLocation.containsKey(event.getWhoClicked().getUniqueId()))
                return;

            event.setCancelled(true);
        }

        @EventHandler
        public void onEmeraldClick(PlayerInteractEvent event) {
            if (event.getItem() == null || event.getItem().getType() != Material.EMERALD)
                return;
            if (!waitingForLocation.containsKey(event.getPlayer().getUniqueId()))
                return;

            event.setCancelled(true);
            final var consumer = waitingForLocation.remove(event.getPlayer().getUniqueId());
            consumer.accept(event);
        }

    }

    @Override
    public @NotNull PatternPane createBaseBorderPane(int height) {
        final List<String> lines = new ArrayList<>();
        lines.add("111111111");
        for (int i = 0; i < height - 2; i++)
            lines.add("100000001");
        lines.add("111111111");
        final var pattern = new Pattern(lines.toArray(new String[0]));
        final var pane = new PatternPane(0, 0, 9, height, Pane.Priority.LOWEST, pattern);
        pane.bindItem('1', new GuiItem(ItemBuilder.fill(), e -> e.setCancelled(true)));
        return pane;
    }
}
