package net.itsthesky.terrawars.core.services;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.IConfigInterfaceService;
import net.itsthesky.terrawars.api.services.base.IService;
import net.itsthesky.terrawars.api.services.base.IServiceProvider;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.api.services.base.Service;
import net.itsthesky.terrawars.core.gui.ConfigGuiContext;
import net.itsthesky.terrawars.core.gui.EntryHandlerManager;
import net.itsthesky.terrawars.core.gui.handlers.*;
import net.itsthesky.terrawars.core.gui.handlers.specific.GameSizeEntryHandler;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConfigInterfaceService implements IConfigInterfaceService, IService {

    @Inject private IChatService chatService;
    @Inject private IServiceProvider serviceProvider;

    private final Map<UUID, ConfigGuiContext<?>> activeContexts = new HashMap<>();
    private final EntryHandlerManager entryHandlerManager = new EntryHandlerManager();

    @Override
    public void init() {
        // Register standard handlers
        entryHandlerManager.registerHandler(new StringEntryHandler());
        entryHandlerManager.registerHandler(new NumberEntryHandler());
        entryHandlerManager.registerHandler(new BooleanEntryHandler());
        entryHandlerManager.registerHandler(new EnumEntryHandler());
        entryHandlerManager.registerHandler(new LocationEntryHandler());
        entryHandlerManager.registerHandler(new WorldEntryHandler());
        entryHandlerManager.registerHandler(new ListEntryHandler(this));

        // Register more specialized handlers
        entryHandlerManager.registerHandler(new GameSizeEntryHandler());
    }

    @Override
    public <T> void openConfigGui(@NotNull Player player, @NotNull T object, @NotNull ConfigSaveCallback<T> saveCallback) {
        openConfigGui(player, object, "Configure " + object.getClass().getSimpleName(), saveCallback);
    }

    @Override
    public <T> void openConfigGui(@NotNull Player player, @NotNull T object, @NotNull String title, @NotNull ConfigSaveCallback<T> saveCallback) {
        final Component parsedTitle = chatService.format("<accent>" + title);

        // Create a new context for this configuration session
        ConfigGuiContext<T> context = new ConfigGuiContext<>(serviceProvider, player, object, saveCallback, entryHandlerManager);
        context.setConfigInterfaceService(this);
        activeContexts.put(player.getUniqueId(), context);

        // Build and show the main GUI
        ChestGui gui = buildMainGui(context, parsedTitle);
        gui.show(player);
    }

    private <T> ChestGui buildMainGui(ConfigGuiContext<T> context, Component title) {
        final int rows = 6;
        ChestGui gui = new ChestGui(rows, ComponentHolder.of(title));
        context.setCurrentGui(gui);

        // Create decoration pane (border)
        OutlinePane decorationPane = createDecorationPane(rows);
        gui.addPane(decorationPane);

        // Create paginated pane for entries
        PaginatedPane entriesPane = createEntriesPane(context, rows);
        gui.addPane(entriesPane);

        // Create navigation pane (if needed)
        if (entriesPane.getPages() > 1) {
            StaticPane navigationPane = createNavigationPane(gui, entriesPane, rows);
            gui.addPane(navigationPane);
        }

        // Create action pane (save, cancel, etc.)
        StaticPane actionPane = createActionPane(gui, context, rows);
        gui.addPane(actionPane);

        // Add close listener
        gui.setOnClose(event -> {
            if (true)
                return;
            if (context.isConfirmationShown() || context.isEditing()) {
                return;
            }

            // Show confirmation dialog if the user tries to close without saving
            BukkitUtils.sync(() -> {
                context.setConfirmationShown(true);
                openConfirmationGui((Player) event.getPlayer(), context, title);
            });
        });

        return gui;
    }

    private OutlinePane createDecorationPane(int rows) {
        OutlinePane decorationPane = new OutlinePane(0, 0, 9, rows);
        decorationPane.setPriority(Pane.Priority.LOWEST);

        ItemStack decorationItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        Component name = chatService.format("<text> ");
        decorationItem.editMeta(meta -> meta.displayName(name));

        decorationPane.addItem(new GuiItem(decorationItem));

        return decorationPane;
    }

    private <T> PaginatedPane createEntriesPane(ConfigGuiContext<T> context, int rows) {
        PaginatedPane entriesPane = new PaginatedPane(1, 1, 7, rows - 2);
        entriesPane.populateWithGuiItems(context.createEntryItems());

        return entriesPane;
    }

    private StaticPane createNavigationPane(Gui gui, PaginatedPane entriesPane, int rows) {
        StaticPane navigationPane = new StaticPane(2, rows - 1, 5, 1);

        // Previous page button
        ItemStack prevItem = new ItemStack(Material.ARROW);
        Component prevName = chatService.format("<accent>Previous Page");
        prevItem.editMeta(meta -> meta.displayName(prevName));

        navigationPane.addItem(new GuiItem(prevItem, event -> {
            event.setCancelled(true);
            entriesPane.setPage(entriesPane.getPage() - 1);
            if (entriesPane.getPage() < 0) {
                entriesPane.setPage(entriesPane.getPages() - 1);
            }

            // Update the GUI
            gui.update();
        }), 1, 0);

        // Page indicator
        ItemStack pageItem = new ItemStack(Material.PAPER);
        navigationPane.addItem(new GuiItem(pageItem, event -> {
            event.setCancelled(true);
            // Just show current page, do nothing on click
            pageItem.editMeta(meta -> {
                meta.displayName(chatService.format("<text>Page <accent>" + (entriesPane.getPage() + 1) +
                        "<text> of <accent>" + entriesPane.getPages()));
            });
        }), 2, 0);

        // Next page button
        ItemStack nextItem = new ItemStack(Material.ARROW);
        Component nextName = chatService.format("<accent>Next Page");
        nextItem.editMeta(meta -> meta.displayName(nextName));

        navigationPane.addItem(new GuiItem(nextItem, event -> {
            event.setCancelled(true);
            entriesPane.setPage(entriesPane.getPage() + 1);
            if (entriesPane.getPage() >= entriesPane.getPages()) {
                entriesPane.setPage(0);
            }

            // Update the GUI
            gui.update();
        }), 3, 0);

        return navigationPane;
    }

    private <T> StaticPane createActionPane(Gui gui, ConfigGuiContext<T> context, int rows) {
        StaticPane actionPane = new StaticPane(7, rows - 1, 2, 1);

        // Save button
        ItemStack saveItem = new ItemStack(Material.EMERALD);
        Component saveName = chatService.format("<accent>Save Configuration");
        saveItem.editMeta(meta -> {
            meta.displayName(saveName);
            meta.lore(context.createSaveButtonLore());
        });

        actionPane.addItem(new GuiItem(saveItem, event -> {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();

            // Check if all required fields are completed
            if (!context.validateRequiredFields()) {
                // Show error message
                player.sendMessage(chatService.format("<base>[<accent>Config<base>] <text>Cannot save: some required fields are not set!"));

                // Highlight required fields
                context.highlightRequiredFields();
                gui.update();
                return;
            }

            // All requirements met, save the configuration
            context.save();
            player.sendMessage(chatService.format("<base>[<accent>Config<base>] <text>Configuration saved successfully."));
            player.closeInventory();
            activeContexts.remove(player.getUniqueId());
        }), 0, 0);

        // Cancel button
        ItemStack cancelItem = new ItemStack(Material.BARRIER);
        Component cancelName = chatService.format("<base>Cancel");
        cancelItem.editMeta(meta -> meta.displayName(cancelName));

        actionPane.addItem(new GuiItem(cancelItem, event -> {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();
            activeContexts.remove(player.getUniqueId());
        }), 1, 0);

        return actionPane;
    }

    @Override
    public void reopenConfigGui(@NotNull Player player, @NotNull ConfigGuiContext<?> context) {
        final Component parsedTitle = chatService.format("<accent>Configure " + context.getObject().getClass().getSimpleName());
        ChestGui gui = buildMainGui(context, parsedTitle);
        gui.show(player);
    }

    private <T> void openConfirmationGui(Player player, ConfigGuiContext<T> context, Component originalTitle) {
        ChestGui confirmGui = new ChestGui(3, ComponentHolder.of(chatService.format("<base>Confirm Exit")));

        // Create confirmation message
        OutlinePane messagePane = new OutlinePane(0, 0, 9, 1);
        ItemStack messageItem = new ItemStack(Material.PAPER);
        messageItem.editMeta(meta -> {
            meta.displayName(chatService.format("<text>Are you sure you want to exit?"));
            meta.lore(List.of(
                    chatService.format("<base>Any unsaved changes will be lost."),
                    chatService.format("<text>")
            ));
        });
        messagePane.addItem(new GuiItem(messageItem, event -> event.setCancelled(true)));
        messagePane.setRepeat(true);
        confirmGui.addPane(messagePane);

        // Create buttons
        StaticPane buttonsPane = new StaticPane(2, 1, 5, 1);

        // Continue editing button
        ItemStack continueItem = new ItemStack(Material.EMERALD_BLOCK);
        continueItem.editMeta(meta -> meta.displayName(chatService.format("<accent>Continue Editing")));
        buttonsPane.addItem(new GuiItem(continueItem, event -> {
            event.setCancelled(true);
            context.setConfirmationShown(false);

            // Return to the original GUI
            ChestGui mainGui = buildMainGui(context, originalTitle);
            mainGui.show(player);
        }), 1, 0);

        // Exit button
        ItemStack exitItem = new ItemStack(Material.REDSTONE_BLOCK);
        exitItem.editMeta(meta -> meta.displayName(chatService.format("<base>Exit without Saving")));
        buttonsPane.addItem(new GuiItem(exitItem, event -> {
            event.setCancelled(true);
            player.closeInventory();
            activeContexts.remove(player.getUniqueId());
        }), 3, 0);

        confirmGui.addPane(buttonsPane);
        confirmGui.show(player);
    }

    /**
     * Helper method to create lists of colored lore text
     */
    public static List<Component> createLore(IChatService chatService, String... lines) {
        return Arrays.stream(lines)
                .map(chatService::format)
                .collect(Collectors.toList());
    }
}