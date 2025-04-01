package net.itsthesky.terrawars.core.gui.handlers;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.itsthesky.terrawars.core.gui.AbstractEntryHandler;
import net.itsthesky.terrawars.core.gui.ConfigEntryInfo;
import net.itsthesky.terrawars.core.gui.ConfigGuiContext;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handler for boolean entries.
 */
public class BooleanEntryHandler extends AbstractEntryHandler<Boolean> {

    @Override
    public @NotNull Set<Class<?>> getSupportedTypes() {
        return Set.of(boolean.class, Boolean.class);
    }

    @Override
    public @NotNull GuiItem createItem(
            @NotNull ConfigGuiContext<?> context,
            @NotNull ConfigEntryInfo entryInfo,
            @Nullable Object value) {
        
        boolean boolValue = value != null && (boolean) value;
        Material material = boolValue ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
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
            lore.add(context.getChatService().format("<text>Type: <accent>Boolean"));
            
            // Description (if any)
            if (!entryInfo.getDescription().isEmpty()) {
                lore.add(context.getChatService().format("<text>"));
                
                for (String descLine : entryInfo.getFormattedDescription(30)) {
                    lore.add(context.getChatService().format("<text>" + descLine));
                }
            }
            
            // Current value
            lore.add(context.getChatService().format("<text>"));
            
            String valueText = boolValue ? "<accent>Enabled" : "<base>Disabled";
            lore.add(context.getChatService().format("<base>Current: " + valueText));
            
            // Instructions
            lore.add(context.getChatService().format("<text>"));
            lore.add(context.getChatService().format("<accent>Left-click: <text>Toggle value"));
            lore.add(context.getChatService().format("<accent>Drop key (Q): <text>Reset to default"));
            
            meta.lore(lore);
        });
        
        return new GuiItem(itemStack, event -> {
            event.setCancelled(true);
            
            switch (event.getClick()) {
                case LEFT -> {
                    // Toggle the boolean value
                    Boolean newValue = value == null || !(boolean) value;
                    context.setFieldValue(entryInfo.getField(), newValue);
                    
                    // Update the item to show the new value
                    event.getInventory().setItem(event.getSlot(), 
                        createItem(context, entryInfo, newValue).getItem());
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

    @Override
    public @NotNull String formatValue(@Nullable Boolean value) {
        if (value == null) {
            return "null";
        }
        return value ? "true" : "false";
    }

    @Override
    public void startEdit(@NotNull ConfigGuiContext<?> context, @NotNull ConfigEntryInfo entryInfo) {
        // For booleans, we don't need a text input, as clicking toggles the value directly
        // This method is required by the interface but not used for this handler
    }
}