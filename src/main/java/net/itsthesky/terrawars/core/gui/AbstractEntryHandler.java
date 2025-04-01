package net.itsthesky.terrawars.core.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.util.Colors;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Abstract implementation of EntryHandler with common functionality.
 * @param <T> The type this handler processes
 */
public abstract class AbstractEntryHandler<T> implements EntryHandler<T> {

    private static final int MAX_DESCRIPTION_LINE_LENGTH = 50;
    
    /**
     * Creates a GuiItem with standard behavior for configuration entries.
     *
     * @param context The GUI context
     * @param entryInfo The entry information
     * @param value The current value
     * @param leftClickAction Action to perform on left click
     * @return The created GuiItem
     */
    protected GuiItem createStandardItem(
            @NotNull ConfigGuiContext<?> context,
            @NotNull ConfigEntryInfo entryInfo,
            @Nullable Object value,
            @NotNull Consumer<InventoryClickEvent> leftClickAction) {
        
        final IChatService chatService = context.getChatService();
        final ItemStack itemStack = new ItemStack(entryInfo.getIcon());
        
        itemStack.editMeta(meta -> {
            // Format the display name based on status
            String nameFormat = entryInfo.isRequired() ? "<accent>%s" : "<base>%s";
            
            // Add an asterisk to required fields
            String requiredMarker = entryInfo.isRequired() ? " *" : "";
            
            meta.displayName(chatService.format(String.format(nameFormat, entryInfo.getName() + requiredMarker)));
            
            // Create the lore
            List<Component> lore = new ArrayList<>();
            
            // Field type
            lore.add(chatService.format("<text>Type: <accent>" + entryInfo.getField().getType().getSimpleName()));
            
            // Description (if any)
            if (!entryInfo.getDescription().isEmpty()) {
                lore.add(chatService.format("<text>"));
                
                for (String descLine : entryInfo.getFormattedDescription(MAX_DESCRIPTION_LINE_LENGTH)) {
                    lore.add(chatService.format("<text>" + descLine));
                }
            }
            
            // Current value
            lore.add(chatService.format("<text>"));
            
            if (value == null) {
                lore.add(chatService.format("<base>Current: <text>Not set"));
            } else {
                String formattedValue = formatValue((T) value);
                lore.add(chatService.format("<base>Current: <text>" + formattedValue));
            }
            
            // Instructions
            lore.add(chatService.format("<text>"));
            lore.add(chatService.format("<accent>Left-click: <text>Edit value"));
            lore.add(chatService.format("<accent>Drop key (Q): <text>Reset to default"));
            
            meta.lore(lore);
        });
        
        return new GuiItem(itemStack, event -> {
            event.setCancelled(true);
            
            switch (event.getClick()) {
                case LEFT -> leftClickAction.accept(event);
                case DROP -> {
                    resetToDefault(context, entryInfo);
                    event.getInventory().setItem(event.getSlot(), 
                        createStandardItem(context, entryInfo, 
                            context.getFieldValue(entryInfo.getField()), 
                            leftClickAction).getItem());
                }
            }
        });
    }
    
    /**
     * Splits a text into multiple lines for display in a lore.
     *
     * @param text The text to split
     * @param maxLineLength The maximum length of each line
     * @return A list of lines
     */
    protected List<String> splitTextIntoLines(String text, int maxLineLength) {
        List<String> lines = new ArrayList<>();
        
        if (text.length() <= maxLineLength) {
            lines.add(text);
            return lines;
        }
        
        // Split the text on spaces
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLineLength) {
                // Line would be too long, start a new line
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                
                // If the word itself is too long, split it
                if (word.length() > maxLineLength) {
                    int startIndex = 0;
                    while (startIndex < word.length()) {
                        int endIndex = Math.min(startIndex + maxLineLength, word.length());
                        lines.add(word.substring(startIndex, endIndex));
                        startIndex = endIndex;
                    }
                } else {
                    currentLine.append(word);
                }
            } else {
                // Add the word to the current line
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        
        // Add the last line if it's not empty
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines;
    }
}