package net.itsthesky.terrawars.core.gui.handlers;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.core.gui.AbstractEntryHandler;
import net.itsthesky.terrawars.core.gui.ConfigEntryInfo;
import net.itsthesky.terrawars.core.gui.ConfigGuiContext;
import net.itsthesky.terrawars.util.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handler for location entries.
 */
public class LocationEntryHandler extends AbstractEntryHandler<Location> implements Listener {

    private final Map<Player, ConfigGuiContext<?>> playersEditing = new HashMap<>();

    public LocationEntryHandler() {
        Bukkit.getPluginManager().registerEvents(this, TerraWars.instance());
    }
    
    @Override
    public @NotNull Set<Class<?>> getSupportedTypes() {
        return Set.of(Location.class);
    }

    @Override
    public @NotNull GuiItem createItem(
            @NotNull ConfigGuiContext<?> context,
            @NotNull ConfigEntryInfo entryInfo,
            @Nullable Object value) {
        
        return createStandardItem(context, entryInfo, value, event -> {
            Player player = (Player) event.getWhoClicked();
            startEdit(context, entryInfo);
            player.closeInventory();
        });
    }

    @Override
    public @NotNull String formatValue(@Nullable Location value) {
        if (value == null) {
            return "null";
        }
        
        return String.format("%s (%.1f, %.1f, %.1f)", 
            value.getWorld().getName(),
            value.getX(), value.getY(), value.getZ()
        );
    }

    @Override
    public void startEdit(@NotNull ConfigGuiContext<?> context, @NotNull ConfigEntryInfo entryInfo) {
        Player player = context.getPlayer();

        playersEditing.put(player, context);
        context.setEditing(true);
        context.setCurrentEditingField(entryInfo.getField());
        
        player.sendMessage(context.getChatService().format(
                "<base>[<accent>Config<base>] <text>Move to the desired location for <accent>" + entryInfo.getName() + 
                "<text> and type <accent>save<text> in chat when ready."
        ));
        player.sendMessage(context.getChatService().format(
                "<text>Or type <accent>cancel<text> to abort."
        ));
        
        // If there's a current value, teleport the player there
        Location currentLocation = (Location) context.getFieldValue(entryInfo.getField());
        if (currentLocation != null) {
            player.teleport(currentLocation);
            player.sendMessage(context.getChatService().format(
                    "<base>[<accent>Config<base>] <text>Teleported to current location. Move around and type <accent>save<text> to update."
            ));
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (!playersEditing.containsKey(player)) {
            return;
        }
        
        event.setCancelled(true);
        
        String input = event.getMessage();
        
        BukkitUtils.sync(() -> {
            final var context = playersEditing.get(player);
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
            
            if (input.equalsIgnoreCase("save")) {
                // Get the player's current location
                Location location = player.getLocation();
                
                // Update the field value
                context.setFieldValue(context.getCurrentEditingField(), location);
                
                player.sendMessage(context.getChatService().format(
                        "<base>[<accent>Config<base>] <text>Location saved: <accent>" + formatValue(location)
                ));
                
                finishEditing(player, context);
            } else {
                player.sendMessage(context.getChatService().format(
                        "<base>[<accent>Config<base>] <text>Type <accent>save<text> to save your current location or <accent>cancel<text> to abort."
                ));
            }
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