package net.itsthesky.terrawars.core.gui.handlers;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.itsthesky.terrawars.core.gui.AbstractEntryHandler;
import net.itsthesky.terrawars.core.gui.ConfigEntryInfo;
import net.itsthesky.terrawars.core.gui.ConfigGuiContext;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Handler for enumeration entries.
 */
public class EnumEntryHandler extends AbstractEntryHandler<Enum<?>> {

    private static final int MAX_COMBO_BOX_VALUES = 10;

    @Override
    public @NotNull Set<Class<?>> getSupportedTypes() {
        return Set.of(Enum.class);
    }

    @Override
    public boolean supports(@NotNull Class<?> type) {
        return type.isEnum();
    }

    @Override
    public @NotNull GuiItem createItem(
            @NotNull ConfigGuiContext<?> context,
            @NotNull ConfigEntryInfo entryInfo,
            @Nullable Object value) {

        Class<?> enumType = entryInfo.getField().getType();
        Object[] enumConstants = enumType.getEnumConstants();

        // If there are few options, use combo box style (click to cycle)
        if (enumConstants.length <= MAX_COMBO_BOX_VALUES) {
            return createComboBoxItem(context, entryInfo, value, enumType, enumConstants);
        } else {
            // For many options, use standard item with chat input
            return createStandardItem(context, entryInfo, value, event -> {
                Player player = (Player) event.getWhoClicked();
                startEdit(context, entryInfo);
                player.closeInventory();
            });
        }
    }

    private GuiItem createComboBoxItem(
            ConfigGuiContext<?> context,
            ConfigEntryInfo entryInfo,
            Object currentValue,
            Class<?> enumType,
            Object[] enumConstants) {

        ItemStack itemStack = new ItemStack(entryInfo.getIcon());
        Enum<?> currentEnum = (Enum<?>) currentValue;

        itemStack.editMeta(meta -> {
            // Format the display name based on status
            String nameFormat = entryInfo.isRequired() ? "<accent>%s" : "<base>%s";
            
            // Add an asterisk to required fields
            String requiredMarker = entryInfo.isRequired() ? " *" : "";
            
            meta.displayName(context.getChatService().format(String.format(nameFormat, entryInfo.getName() + requiredMarker)));
            
            // Create the lore
            List<Component> lore = new ArrayList<>();
            
            // Field type
            lore.add(context.getChatService().format("<text>Type: <accent>" + enumType.getSimpleName()));
            
            // Description (if any)
            if (!entryInfo.getDescription().isEmpty()) {
                lore.add(context.getChatService().format("<text>"));
                
                for (String descLine : entryInfo.getFormattedDescription(30)) {
                    lore.add(context.getChatService().format("<text>" + descLine));
                }
            }
            
            // Current value
            lore.add(context.getChatService().format("<text>"));
            lore.add(context.getChatService().format("<base>Current: <text>" + formatValue(currentEnum)));
            
            // Available options
            lore.add(context.getChatService().format("<text>"));
            lore.add(context.getChatService().format("<base>Options:"));
            
            for (Object enumConstant : enumConstants) {
                Enum<?> enumValue = (Enum<?>) enumConstant;
                
                if (enumValue.equals(currentEnum)) {
                    lore.add(context.getChatService().format("<accent>» " + enumValue.name()));
                } else {
                    lore.add(context.getChatService().format("<text>  " + enumValue.name()));
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
                    Enum<?> nextValue = getNextEnumValue(enumConstants, currentEnum);
                    context.setFieldValue(entryInfo.getField(), nextValue);
                    
                    // Update the item to show the new value
                    event.getInventory().setItem(event.getSlot(),
                        createComboBoxItem(context, entryInfo, nextValue, enumType, enumConstants).getItem());
                    context.getCurrentGui().update();
                }
                case DROP -> {
                    resetToDefault(context, entryInfo);
                    event.getInventory().setItem(event.getSlot(),
                        createComboBoxItem(context, entryInfo,
                            context.getFieldValue(entryInfo.getField()),
                            enumType, enumConstants).getItem());
                    context.getCurrentGui().update();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E>> E getNextEnumValue(Object[] enumConstants, Enum<?> currentValue) {
        if (currentValue == null && enumConstants.length > 0) {
            return (E) enumConstants[0];
        }
        
        int currentIndex = -1;
        for (int i = 0; i < enumConstants.length; i++) {
            if (enumConstants[i].equals(currentValue)) {
                currentIndex = i;
                break;
            }
        }
        
        int nextIndex = (currentIndex + 1) % enumConstants.length;
        return (E) enumConstants[nextIndex];
    }

    @Override
    public @NotNull String formatValue(@Nullable Enum<?> value) {
        if (value == null) {
            return "null";
        }
        return value.name();
    }

    @Override
    public void startEdit(@NotNull ConfigGuiContext<?> context, @NotNull ConfigEntryInfo entryInfo) {
        Player player = context.getPlayer();
        Class<?> enumType = entryInfo.getField().getType();
        Object[] enumConstants = enumType.getEnumConstants();
        
        context.setEditing(true);
        context.setCurrentEditingField(entryInfo.getField());
        
        player.sendMessage(context.getChatService().format(
                "<base>[<accent>Config<base>] <text>Enter a value for <accent>" + entryInfo.getName() + 
                "<text> or type <accent>cancel<text> to abort."
        ));
        
        // Display available options
        player.sendMessage(context.getChatService().format("<base>Available options:"));
        
        for (Object enumConstant : enumConstants) {
            player.sendMessage(context.getChatService().format("<text>- " + ((Enum<?>)enumConstant).name()));
        }
        
        // Start a chat listener to catch the player's input
        BukkitUtils.async(() -> {
            try {
                // This is a placeholder; in a real implementation, we would listen for player chat here
            } catch (Exception e) {
                player.sendMessage(context.getChatService().format(
                        "<base>[<accent>Config<base>] <text>An error occurred: " + e.getMessage()
                ));
                context.setEditing(false);
                context.setCurrentEditingField(null);
            }
        });
    }
}