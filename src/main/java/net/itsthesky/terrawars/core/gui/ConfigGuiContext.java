package net.itsthesky.terrawars.core.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import lombok.Getter;
import lombok.Setter;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.IConfigInterfaceService;
import net.itsthesky.terrawars.api.services.base.IServiceProvider;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.core.config.EntryDetails;
import net.itsthesky.terrawars.util.Colors;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Context for a configuration GUI session.
 * This class manages the object being configured, the fields, and the state of the configuration.
 */

@Setter
@Getter
public class ConfigGuiContext<T> {

    @Inject
    private IChatService chatService;

    private final Player player;
    private final T object;
    private final IConfigInterfaceService.ConfigSaveCallback<T> saveCallback;
    private final EntryHandlerManager entryHandlerManager;
    private final List<ConfigEntryInfo> entryInfos = new ArrayList<>();

    private boolean confirmationShown = false;
    private boolean editing = false;
    private Field currentEditingField = null;
    private IConfigInterfaceService configInterfaceService;
    private Gui currentGui;

    public ConfigGuiContext(IServiceProvider serviceProvider, Player player, T object,
                            IConfigInterfaceService.ConfigSaveCallback<T> saveCallback,
                            EntryHandlerManager entryHandlerManager) {
        serviceProvider.inject(this);

        this.player = player;
        this.object = object;
        this.saveCallback = saveCallback;
        this.entryHandlerManager = entryHandlerManager;

        // Analyze the object and collect field information
        analyzeObject();
    }

    private void analyzeObject() {
        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            EntryDetails details = field.getAnnotation(EntryDetails.class);
            if (details != null) {
                // This field has EntryDetails annotation, create entry info
                ConfigEntryInfo entryInfo = new ConfigEntryInfo(field, details);
                entryInfos.add(entryInfo);
            }
        }

        // Sort by required first, then alphabetically by name
        entryInfos.sort((a, b) -> {
            if (a.isRequired() != b.isRequired()) {
                return a.isRequired() ? -1 : 1;
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });
    }

    /**
     * Creates GUI items for all entries in this context
     */
    public List<GuiItem> createEntryItems() {
        List<GuiItem> items = new ArrayList<>();

        for (ConfigEntryInfo entryInfo : entryInfos) {
            EntryHandler<?> handler = entryHandlerManager.getHandlerForType(entryInfo.getField().getType());
            if (handler == null) {
                // No handler for this type, create a placeholder item
                items.add(createPlaceholderItem(entryInfo));
                continue;
            }

            // Get the current value of the field
            Object value = getFieldValue(entryInfo.getField());

            // Create the GUI item for this entry
            GuiItem item = handler.createItem(this, entryInfo, value);
            items.add(item);
        }

        return items;
    }

    private GuiItem createPlaceholderItem(ConfigEntryInfo entryInfo) {
        ItemStack itemStack = new ItemStack(entryInfo.getIcon());
        itemStack.editMeta(meta -> {
            meta.displayName(chatService.format("<base>" + entryInfo.getName() + " <text>(Unsupported)"));

            List<Component> lore = new ArrayList<>();
            lore.add(chatService.format("<text>Type: <accent>" + entryInfo.getField().getType().getSimpleName()));
            lore.add(chatService.format("<base>No handler available for this type"));

            meta.lore(lore);
        });

        return new GuiItem(itemStack, event -> event.setCancelled(true));
    }

    /**
     * Creates lore for the save button based on configuration state
     */
    public List<Component> createSaveButtonLore() {
        List<Component> lore = new ArrayList<>();

        // Count missing required fields
        long missingRequiredFields = entryInfos.stream()
                .filter(ConfigEntryInfo::isRequired)
                .filter(info -> getFieldValue(info.getField()) == null)
                .count();

        lore.add(chatService.format("<text>Click to save your configuration"));

        if (missingRequiredFields > 0) {
            lore.add(chatService.format("<base>"));
            lore.add(chatService.format("<base>Warning: <text>" + missingRequiredFields + " required fields not set"));
        } else {
            lore.add(chatService.format("<base>"));
            lore.add(chatService.format("<accent>All required fields are set"));
        }

        return lore;
    }

    /**
     * Validates if all required fields are set
     */
    public boolean validateRequiredFields() {
        return entryInfos.stream()
                .filter(ConfigEntryInfo::isRequired)
                .allMatch(info -> {
                    Object value = getFieldValue(info.getField());

                    // Special check for lists
                    if (value instanceof List<?> list) {
                        int min = info.getDetails().min();
                        int max = info.getDetails().max();

                        // Check min/max constraints if they're specified
                        if (min >= 0 && list.size() < min) {
                            return false;
                        }

                        if (max >= 0 && list.size() > max) {
                            return false;
                        }
                    }

                    return value != null;
                });
    }

    /**
     * Highlights required fields that are not set
     */
    public void highlightRequiredFields() {
        // Just mark them for now, the GUI renderer will handle the visual indication
        // by changing the item appearance
    }

    /**
     * Saves the configuration by calling the save callback
     */
    public void save() {
        saveCallback.onSave(object);
    }

    /**
     * Gets the current value of a field from the object
     */
    public Object getFieldValue(Field field) {
        try {
            boolean accessible = field.canAccess(object);
            if (!accessible) {
                field.setAccessible(true);
            }

            Object value = field.get(object);

            if (!accessible) {
                field.setAccessible(false);
            }

            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sets the value of a field in the object
     */
    public void setFieldValue(Field field, Object value) {
        try {
            boolean accessible = field.canAccess(object);
            if (!accessible) {
                field.setAccessible(true);
            }

            field.set(object, value);

            if (!accessible) {
                field.setAccessible(false);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}