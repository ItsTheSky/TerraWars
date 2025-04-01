package net.itsthesky.terrawars.core.gui.handlers.specific;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.core.gui.ConfigEntryInfo;
import net.itsthesky.terrawars.core.gui.ConfigGuiContext;
import net.itsthesky.terrawars.core.gui.handlers.EnumEntryHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Specialized handler for GameSize enums.
 * This provides better visualization with custom icons.
 */
public class GameSizeEntryHandler extends EnumEntryHandler {

    @Override
    public @NotNull Set<Class<?>> getSupportedTypes() {
        return Set.of(IGame.GameSize.class);
    }
    
    @Override
    public @NotNull GuiItem createItem(
            @NotNull ConfigGuiContext<?> context,
            @NotNull ConfigEntryInfo entryInfo,
            @Nullable Object value) {
        
        IGame.GameSize gameSize = (IGame.GameSize) value;
        Material material = getMaterialForGameSize(gameSize);
        ItemStack itemStack = new ItemStack(material);
        
        itemStack.editMeta(meta -> {
            // Format the display name based on status
            String nameFormat = entryInfo.isRequired() ? "<accent>%s" : "<base>%s";
            
            // Add an asterisk to required fields
            String requiredMarker = entryInfo.isRequired() ? " *" : "";
            
            meta.displayName(context.getChatService().format(String.format(nameFormat, entryInfo.getName() + requiredMarker)));
            
            // Create the lore
            List<Component> lore = new ArrayList<>();
            
            // Field type
            lore.add(context.getChatService().format("<text>Type: <accent>GameSize"));
            
            // Description (if any)
            if (!entryInfo.getDescription().isEmpty()) {
                lore.add(context.getChatService().format("<text>"));
                
                for (String descLine : entryInfo.getFormattedDescription(30)) {
                    lore.add(context.getChatService().format("<text>" + descLine));
                }
            }
            
            // Current value
            lore.add(context.getChatService().format("<text>"));
            
            if (gameSize == null) {
                lore.add(context.getChatService().format("<base>Current: <text>Not set"));
            } else {
                lore.add(context.getChatService().format("<base>Current: <accent>" + gameSize.name()));
                lore.add(context.getChatService().format("<text>Players per team: <base>" + gameSize.getPlayerPerTeam()));
            }
            
            // Available options
            lore.add(context.getChatService().format("<text>"));
            lore.add(context.getChatService().format("<base>Options:"));
            
            for (IGame.GameSize size : IGame.GameSize.values()) {
                if (size.equals(gameSize)) {
                    lore.add(context.getChatService().format("<accent>» " + size.name() + " (" + size.getPlayerPerTeam() + " players)"));
                } else {
                    lore.add(context.getChatService().format("<text>  " + size.name() + " (" + size.getPlayerPerTeam() + " players)"));
                }
            }
            
            // Instructions
            lore.add(context.getChatService().format("<text>"));
            lore.add(context.getChatService().format("<accent>Left-click: <text>Cycle through options"));
            lore.add(context.getChatService().format("<accent>Drop key (Q): <text>Reset to default"));
            
            meta.lore(lore);
        });
        
        return new GuiItem(itemStack, event -> {
            event.setCancelled(true);
            
            switch (event.getClick()) {
                case LEFT -> {
                    // Find the next enum value in the cycle
                    IGame.GameSize nextValue = getNextGameSize(gameSize);
                    context.setFieldValue(entryInfo.getField(), nextValue);
                    
                    // Update the item to show the new value
                    event.getInventory().setItem(event.getSlot(), 
                        createItem(context, entryInfo, nextValue).getItem());
                }
                case DROP -> {
                    resetToDefault(context, entryInfo);
                    event.getInventory().setItem(event.getSlot(), 
                        createItem(context, entryInfo, 
                            context.getFieldValue(entryInfo.getField())).getItem());
                }
            }
        });
    }
    
    private IGame.GameSize getNextGameSize(IGame.GameSize currentSize) {
        if (currentSize == null) {
            return IGame.GameSize.SOLO;
        }
        
        return switch (currentSize) {
            case SOLO -> IGame.GameSize.DUO;
            case DUO -> IGame.GameSize.SQUAD;
            case SQUAD -> IGame.GameSize.SOLO;
        };
    }
    
    private Material getMaterialForGameSize(IGame.GameSize gameSize) {
        if (gameSize == null) {
            return Material.BARRIER;
        }
        
        return switch (gameSize) {
            case SOLO -> Material.PLAYER_HEAD;
            case DUO -> Material.BEACON;
            case SQUAD -> Material.DRAGON_EGG;
        };
    }
}