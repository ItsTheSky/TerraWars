package net.itsthesky.terrawars.core.gui;

import net.itsthesky.terrawars.core.config.EntryDetails;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Information about a configuration entry.
 * This class holds the field, entry details, and provides utility methods
 * for working with configuration entries.
 */
public class ConfigEntryInfo {

    private final Field field;
    private final EntryDetails details;
    
    public ConfigEntryInfo(Field field, EntryDetails details) {
        this.field = field;
        this.details = details;
    }
    
    /**
     * Gets the name of this entry.
     */
    public String getName() {
        return details.name();
    }
    
    /**
     * Gets the description of this entry.
     */
    public String getDescription() {
        return details.description();
    }
    
    /**
     * Gets the icon material of this entry.
     */
    public Material getIcon() {
        return details.icon();
    }
    
    /**
     * Checks if this entry is required.
     */
    public boolean isRequired() {
        return details.isRequired();
    }
    
    /**
     * Gets the minimum size for list entries.
     */
    public int getMinSize() {
        return details.min();
    }
    
    /**
     * Gets the maximum size for list entries.
     */
    public int getMaxSize() {
        return details.max();
    }
    
    /**
     * Gets the field associated with this entry.
     */
    public Field getField() {
        return field;
    }
    
    /**
     * Gets the entry details annotation.
     */
    public EntryDetails getDetails() {
        return details;
    }
    
    /**
     * Gets a list of formatted description lines, properly split to fit in a lore.
     * @param maxLineLength The maximum length of each line.
     * @return A list of description lines.
     */
    public List<String> getFormattedDescription(int maxLineLength) {
        if (details.description().isEmpty()) {
            return List.of();
        }
        
        // Split the description into words
        String[] words = details.description().split("\\s+");
        
        // Combine words into lines of appropriate length
        StringBuilder currentLine = new StringBuilder();
        return Arrays.stream(words).collect(Collectors.groupingBy(word -> {
            if (currentLine.length() + word.length() + 1 > maxLineLength) {
                currentLine.setLength(0);
                currentLine.append(word);
                return currentLine.toString();
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
                return currentLine.toString();
            }
        })).keySet().stream().toList();
    }
}