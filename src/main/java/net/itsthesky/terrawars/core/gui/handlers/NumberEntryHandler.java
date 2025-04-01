package net.itsthesky.terrawars.core.gui.handlers;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.core.gui.AbstractEntryHandler;
import net.itsthesky.terrawars.core.gui.ConfigEntryInfo;
import net.itsthesky.terrawars.core.gui.ConfigGuiContext;
import net.itsthesky.terrawars.util.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Handler for numeric types (int, long, double, float).
 */
public class NumberEntryHandler extends AbstractEntryHandler<Number> implements Listener {

    private final Set<Player> playersEditing = new HashSet<>();
    
    public NumberEntryHandler() {
        Bukkit.getPluginManager().registerEvents(this, TerraWars.instance());
    }
    
    @Override
    public @NotNull Set<Class<?>> getSupportedTypes() {
        return Set.of(
            int.class, Integer.class,
            long.class, Long.class,
            double.class, Double.class,
            float.class, Float.class
        );
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
    public @NotNull String formatValue(@Nullable Number value) {
        if (value == null) {
            return "null";
        }
        return value.toString();
    }

    @Override
    public void startEdit(@NotNull ConfigGuiContext<?> context, @NotNull ConfigEntryInfo entryInfo) {
        Player player = context.getPlayer();
        
        playersEditing.add(player);
        context.setEditing(true);
        context.setCurrentEditingField(entryInfo.getField());
        
        player.sendMessage(context.getChatService().format(
                "<base>[<accent>Config<base>] <text>Enter a new numeric value for <accent>" + entryInfo.getName() + 
                "<text> or type <accent>cancel<text> to abort."
        ));
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
            
            // Parse the input as a number
            try {
                Object number = parseNumber(input, context.getCurrentEditingField().getType());
                
                // Update the field value
                context.setFieldValue(context.getCurrentEditingField(), number);
                
                player.sendMessage(context.getChatService().format(
                        "<base>[<accent>Config<base>] <text>Value set to: <accent>" + number
                ));
                
                finishEditing(player, context);
            } catch (NumberFormatException e) {
                player.sendMessage(context.getChatService().format(
                        "<base>[<accent>Config<base>] <text>Invalid number format. Please try again."
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
    
    private Object parseNumber(String input, Class<?> targetType) {
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(input);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(input);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(input);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(input);
        }
        throw new IllegalArgumentException("Unsupported number type: " + targetType.getName());
    }
}