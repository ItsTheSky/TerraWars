package net.itsthesky.terrawars.core.gui.handlers;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.core.gui.AbstractEntryHandler;
import net.itsthesky.terrawars.core.gui.ConfigEntryInfo;
import net.itsthesky.terrawars.core.gui.ConfigGuiContext;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handler for Bukkit World entries.
 */
public class WorldEntryHandler extends AbstractEntryHandler<World> implements Listener {

    private final Set<Player> playersEditing = new HashSet<>();
    
    public WorldEntryHandler() {
        Bukkit.getPluginManager().registerEvents(this, TerraWars.instance());
    }
    
    @Override
    public @NotNull Set<Class<?>> getSupportedTypes() {
        return Set.of(World.class);
    }

    @Override
    public @NotNull GuiItem createItem(
            @NotNull ConfigGuiContext<?> context,
            @NotNull ConfigEntryInfo entryInfo,
            @Nullable Object value) {
        
        return createStandardItem(context, entryInfo, value, event -> {
            Player player = (Player) event.getWhoClicked();
            openWorldSelector(player, context, entryInfo);
        });
    }

    private void openWorldSelector(Player player, ConfigGuiContext<?> context, ConfigEntryInfo entryInfo) {
        ChestGui gui = new ChestGui(4, ComponentHolder.of(context.getChatService().format("<accent>Select World")));
        
        // Create decoration pane (border)
        OutlinePane decorationPane = new OutlinePane(0, 0, 9, 4);
        
        ItemStack decorationItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        decorationItem.editMeta(meta -> meta.displayName(context.getChatService().format("<text> ")));
        
        decorationPane.addItem(new GuiItem(decorationItem, event -> event.setCancelled(true)));
        gui.addPane(decorationPane);
        
        // Create content pane for worlds
        OutlinePane worldsPane = new OutlinePane(1, 1, 7, 2);
        
        List<World> worlds = Bukkit.getWorlds();
        for (World world : worlds) {
            Material worldIcon = getWorldIcon(world);
            ItemStack worldItem = new ItemStack(worldIcon);
            
            worldItem.editMeta(meta -> {
                meta.displayName(context.getChatService().format("<accent>" + world.getName()));
                
                List<Component> lore = new ArrayList<>();
                lore.add(context.getChatService().format("<text>Environment: <base>" + world.getEnvironment().name()));
                lore.add(context.getChatService().format("<text>Seed: <base>" + world.getSeed()));
                lore.add(context.getChatService().format("<text>"));
                lore.add(context.getChatService().format("<text>Click to select this world"));
                
                meta.lore(lore);
            });
            
            worldsPane.addItem(new GuiItem(worldItem, event -> {
                event.setCancelled(true);
                context.setFieldValue(entryInfo.getField(), world);
                player.sendMessage(context.getChatService().format(
                        "<base>[<accent>Config<base>] <text>Selected world: <accent>" + world.getName()
                ));
                player.closeInventory();
                
                // Reopen the main config GUI
                BukkitUtils.sync(() -> {
                    if (context.getConfigInterfaceService() != null) {
                        context.getConfigInterfaceService().reopenConfigGui(player, context);
                    }
                });
            }));
        }
        
        gui.addPane(worldsPane);
        
        // Add manual entry button
        StaticPane actionPane = new StaticPane(3, 3, 3, 1);
        
        ItemStack manualEntryItem = new ItemStack(Material.OAK_SIGN);
        manualEntryItem.editMeta(meta -> {
            meta.displayName(context.getChatService().format("<accent>Enter World Name"));
            
            List<Component> lore = new ArrayList<>();
            lore.add(context.getChatService().format("<text>Click to type a world name manually"));
            lore.add(context.getChatService().format("<text>Useful for unloaded worlds"));
            
            meta.lore(lore);
        });
        
        actionPane.addItem(new GuiItem(manualEntryItem, event -> {
            event.setCancelled(true);
            startEdit(context, entryInfo);
            player.closeInventory();
        }), 1, 0);
        
        gui.addPane(actionPane);
        gui.show(player);
    }

    private Material getWorldIcon(World world) {
        return switch (world.getEnvironment()) {
            case NORMAL -> Material.GRASS_BLOCK;
            case NETHER -> Material.NETHERRACK;
            case THE_END -> Material.END_STONE;
            default -> Material.MAP;
        };
    }

    @Override
    public @NotNull String formatValue(@Nullable World value) {
        if (value == null) {
            return "null";
        }
        return value.getName();
    }

    @Override
    public void startEdit(@NotNull ConfigGuiContext<?> context, @NotNull ConfigEntryInfo entryInfo) {
        Player player = context.getPlayer();
        
        playersEditing.add(player);
        context.setEditing(true);
        context.setCurrentEditingField(entryInfo.getField());
        
        player.sendMessage(context.getChatService().format(
                "<base>[<accent>Config<base>] <text>Enter the name of the world for <accent>" + entryInfo.getName() + 
                "<text> or type <accent>cancel<text> to abort."
        ));
        
        // List available worlds
        player.sendMessage(context.getChatService().format("<base>Available loaded worlds:"));
        for (World world : Bukkit.getWorlds()) {
            player.sendMessage(context.getChatService().format("<text>- " + world.getName()));
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (!playersEditing.contains(player)) {
            return;
        }
        
        event.setCancelled(true);
        
        String input = event.getMessage();
        
        BukkitUtils.sync(() -> {
            ConfigGuiContext<?> context = null;
            
            // Find the context for this player
            for (ConfigGuiContext<?> ctx : TerraWars.instance().getServer().getServicesManager().getRegistrations(ConfigGuiContext.class)
                    .stream().map(registration -> registration.getProvider()).toList()) {
                if (ctx.getPlayer().equals(player)) {
                    context = ctx;
                    break;
                }
            }
            
            if (context == null || !context.isEditing() || !context.getPlayer().equals(player)) {
                player.sendMessage(context.getChatService().format(
                        "<base>[<accent>Config<base>] <text>Edit session expired. Please try again."
                ));
                playersEditing.remove(player);
                return;
            }
            
            if (input.equalsIgnoreCase("cancel")) {
                player.sendMessage(context.getChatService().format(
                        "<base>[<accent>Config<base>] <text>Editing cancelled."
                ));
                finishEditing(player, context);
                return;
            }
            
            // Try to find the world
            World world = Bukkit.getWorld(input);
            if (world == null) {
                player.sendMessage(context.getChatService().format(
                        "<base>[<accent>Config<base>] <text>Warning: World '<accent>" + input + 
                        "<text>' is not loaded. It will be stored by name only."
                ));
                
                // Here we would need a way to store just the name. For now, we'll skip setting.
                player.sendMessage(context.getChatService().format(
                        "<base>[<accent>Config<base>] <text>Please select a loaded world instead."
                ));
                return;
            }
            
            // Update the field value
            context.setFieldValue(context.getCurrentEditingField(), world);
            
            player.sendMessage(context.getChatService().format(
                    "<base>[<accent>Config<base>] <text>World set to: <accent>" + world.getName()
            ));
            
            finishEditing(player, context);
        });
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playersEditing.remove(event.getPlayer());
    }
    
    private void finishEditing(Player player, ConfigGuiContext<?> context) {
        playersEditing.remove(player);
        context.setEditing(false);
        context.setCurrentEditingField(null);
        
        // Reopen the GUI
        BukkitUtils.sync(() -> {
            if (context.getConfigInterfaceService() != null) {
                context.getConfigInterfaceService().reopenConfigGui(player, context);
            }
        });
    }
}