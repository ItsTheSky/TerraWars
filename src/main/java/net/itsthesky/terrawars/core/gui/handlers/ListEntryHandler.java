package net.itsthesky.terrawars.core.gui.handlers;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.itsthesky.terrawars.api.services.IConfigInterfaceService;
import net.itsthesky.terrawars.core.gui.AbstractEntryHandler;
import net.itsthesky.terrawars.core.gui.ConfigEntryInfo;
import net.itsthesky.terrawars.core.gui.ConfigGuiContext;
import net.itsthesky.terrawars.core.gui.EntryHandlerManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Handler for List entries.
 */
public class ListEntryHandler extends AbstractEntryHandler<List<?>> {

    private final IConfigInterfaceService configInterfaceService;
    
    public ListEntryHandler(IConfigInterfaceService configInterfaceService) {
        this.configInterfaceService = configInterfaceService;
    }
    
    @Override
    public @NotNull Set<Class<?>> getSupportedTypes() {
        return Set.of(List.class, ArrayList.class, Collection.class);
    }
    
    @Override
    public boolean supports(@NotNull Class<?> type) {
        return List.class.isAssignableFrom(type) || type.isArray();
    }

    @Override
    public @NotNull GuiItem createItem(
            @NotNull ConfigGuiContext<?> context,
            @NotNull ConfigEntryInfo entryInfo,
            @Nullable Object value) {
        
        return createStandardItem(context, entryInfo, value, event -> {
            Player player = (Player) event.getWhoClicked();
            openListEditor(player, context, entryInfo, (List<?>) value);
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void openListEditor(Player player, ConfigGuiContext<?> context, ConfigEntryInfo entryInfo, List<?> list) {
        if (list == null) {
            // Initialize the list if it's null
            list = new ArrayList<>();
            context.setFieldValue(entryInfo.getField(), list);
        }
        
        final List<?> finalList = list;
        
        // Try to determine the generic type of the list
        Class<?> elementType = Object.class;
        try {
            Type genericType = entryInfo.getField().getGenericType();
            if (genericType instanceof ParameterizedType parameterizedType) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length > 0 && typeArguments[0] instanceof Class) {
                    elementType = (Class<?>) typeArguments[0];
                }
            }
        } catch (Exception e) {
            // Default to Object if we can't determine the type
            e.printStackTrace();
        }
        
        final Class<?> finalElementType = elementType;
        
        ChestGui gui = new ChestGui(6, ComponentHolder.of(context.getChatService().format("<accent>Edit List: " + entryInfo.getName())));
        
        // Create decoration pane (border)
        OutlinePane decorationPane = new OutlinePane(0, 0, 9, 6);
        ItemStack decorationItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        decorationItem.editMeta(meta -> meta.displayName(context.getChatService().format("<text> ")));
        
        decorationPane.addItem(new GuiItem(decorationItem, event -> event.setCancelled(true)));
        gui.addPane(decorationPane);
        
        // Create content pane for list items
        PaginatedPane itemsPane = new PaginatedPane(1, 1, 7, 4);
        
        // Convert the list to a list of items
        List<GuiItem> guiItems = new ArrayList<>();
        for (int i = 0; i < finalList.size(); i++) {
            Object item = finalList.get(i);
            final int index = i;
            
            Material icon = Material.PAPER;
            
            if (finalElementType.isEnum()) {
                icon = Material.NAME_TAG;
            } else if (Number.class.isAssignableFrom(finalElementType) || isPrimitiveNumber(finalElementType)) {
                icon = Material.GOLDEN_APPLE;
            } else if (finalElementType == Boolean.class || finalElementType == boolean.class) {
                icon = item != null && (Boolean) item ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
            } else if (finalElementType == String.class) {
                icon = Material.OAK_SIGN;
            }
            
            ItemStack itemStack = new ItemStack(icon);
            
            itemStack.editMeta(meta -> {
                meta.displayName(context.getChatService().format("<accent>Item #" + (index + 1)));
                
                List<Component> lore = new ArrayList<>();
                lore.add(context.getChatService().format("<text>Type: <base>" + finalElementType.getSimpleName()));
                
                if (item == null) {
                    lore.add(context.getChatService().format("<text>Value: <base>null"));
                } else {
                    lore.add(context.getChatService().format("<text>Value: <base>" + item));
                }
                
                lore.add(context.getChatService().format("<text>"));
                lore.add(context.getChatService().format("<accent>Left-click: <text>Edit this item"));
                lore.add(context.getChatService().format("<accent>Drop key (Q): <text>Remove this item"));
                
                meta.lore(lore);
            });
            
            guiItems.add(new GuiItem(itemStack, event -> {
                event.setCancelled(true);
                
                switch (event.getClick()) {
                    case LEFT -> {
                        // Edit this item
                        editListItem(player, context, entryInfo, finalList, index, finalElementType);
                    }
                    case DROP -> {
                        // Remove this item
                        ((List) finalList).remove(index);
                        
                        // Reopen the list editor
                        openListEditor(player, context, entryInfo, finalList);
                    }
                }
            }));
        }
        
        itemsPane.populateWithGuiItems(guiItems);
        gui.addPane(itemsPane);
        
        // Create navigation pane
        if (itemsPane.getPages() > 1) {
            StaticPane navigationPane = new StaticPane(2, 5, 5, 1);
            
            // Previous page button
            ItemStack prevItem = new ItemStack(Material.ARROW);
            prevItem.editMeta(meta -> meta.displayName(context.getChatService().format("<accent>Previous Page")));
            
            navigationPane.addItem(new GuiItem(prevItem, event -> {
                event.setCancelled(true);
                itemsPane.setPage(itemsPane.getPage() - 1);
                if (itemsPane.getPage() < 0) {
                    itemsPane.setPage(itemsPane.getPages() - 1);
                }
                
                // Update the GUI
                gui.update();
            }), 1, 0);
            
            // Page indicator
            ItemStack pageItem = new ItemStack(Material.PAPER);
            pageItem.editMeta(meta -> {
                meta.displayName(context.getChatService().format("<text>Page <accent>" + (itemsPane.getPage() + 1) + 
                                                              "<text> of <accent>" + itemsPane.getPages()));
            });
            
            navigationPane.addItem(new GuiItem(pageItem, event -> event.setCancelled(true)), 2, 0);
            
            // Next page button
            ItemStack nextItem = new ItemStack(Material.ARROW);
            nextItem.editMeta(meta -> meta.displayName(context.getChatService().format("<accent>Next Page")));
            
            navigationPane.addItem(new GuiItem(nextItem, event -> {
                event.setCancelled(true);
                itemsPane.setPage(itemsPane.getPage() + 1);
                if (itemsPane.getPage() >= itemsPane.getPages()) {
                    itemsPane.setPage(0);
                }
                
                // Update the GUI
                gui.update();
            }), 3, 0);
            
            gui.addPane(navigationPane);
        }
        
        // Create action pane (add, back)
        StaticPane actionPane = new StaticPane(7, 5, 2, 1);
        
        // Add button
        boolean canAdd = true;
        if (entryInfo.getMaxSize() > 0 && finalList.size() >= entryInfo.getMaxSize()) {
            canAdd = false;
        }
        
        ItemStack addItem = new ItemStack(canAdd ? Material.EMERALD : Material.BARRIER);
        final boolean finalCanAdd = canAdd;
        
        addItem.editMeta(meta -> {
            meta.displayName(context.getChatService().format(finalCanAdd ? "<accent>Add Item" : "<base>Cannot Add More Items"));
            
            List<Component> lore = new ArrayList<>();
            
            if (entryInfo.getMinSize() >= 0) {
                lore.add(context.getChatService().format("<text>Minimum items: <base>" + entryInfo.getMinSize()));
            }
            
            if (entryInfo.getMaxSize() >= 0) {
                lore.add(context.getChatService().format("<text>Maximum items: <base>" + entryInfo.getMaxSize()));
            }
            
            lore.add(context.getChatService().format("<text>Current items: <base>" + finalList.size()));
            
            if (!finalCanAdd) {
                lore.add(context.getChatService().format("<text>"));
                lore.add(context.getChatService().format("<base>Maximum size reached!"));
            }
            
            meta.lore(lore);
        });
        
        actionPane.addItem(new GuiItem(addItem, event -> {
            event.setCancelled(true);
            
            if (!finalCanAdd) {
                return;
            }
            
            // Create a new item
            try {
                Object newItem = null;
                
                if (finalElementType.isPrimitive()) {
                    if (finalElementType == boolean.class) {
                        newItem = false;
                    } else if (finalElementType == int.class) {
                        newItem = 0;
                    } else if (finalElementType == long.class) {
                        newItem = 0L;
                    } else if (finalElementType == float.class) {
                        newItem = 0.0f;
                    } else if (finalElementType == double.class) {
                        newItem = 0.0;
                    }
                } else if (finalElementType.isEnum() && finalElementType.getEnumConstants().length > 0) {
                    newItem = finalElementType.getEnumConstants()[0];
                }
                
                ((List) finalList).add(newItem);
                
                // Reopen the list editor
                openListEditor(player, context, entryInfo, finalList);
            } catch (Exception e) {
                player.sendMessage(context.getChatService().format(
                        "<base>[<accent>Config<base>] <text>Failed to create new item: " + e.getMessage()
                ));
                e.printStackTrace();
            }
        }), 0, 0);
        
        // Back button
        ItemStack backItem = new ItemStack(Material.ARROW);
        backItem.editMeta(meta -> meta.displayName(context.getChatService().format("<accent>Back to Config")));
        
        actionPane.addItem(new GuiItem(backItem, event -> {
            event.setCancelled(true);
            
            // Check minimum size
            if (entryInfo.getMinSize() >= 0 && finalList.size() < entryInfo.getMinSize()) {
                player.sendMessage(context.getChatService().format(
                        "<base>[<accent>Config<base>] <text>List must have at least <accent>" + 
                        entryInfo.getMinSize() + "<text> items!"
                ));
                return;
            }
            
            // Go back to the main config GUI
            if (context.getConfigInterfaceService() != null) {
                context.getConfigInterfaceService().reopenConfigGui(player, context);
            } else {
                player.closeInventory();
            }
        }), 1, 0);
        
        gui.addPane(actionPane);
        gui.show(player);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void editListItem(Player player, ConfigGuiContext<?> context, ConfigEntryInfo entryInfo, 
                             List<?> list, int index, Class<?> elementType) {
        
        Object item = list.get(index);
        
        // Create a field proxy to edit this list item
        class ListItemFieldProxy {
            @SuppressWarnings("unused")
            private Object value;
            
            public ListItemFieldProxy(Object initialValue) {
                this.value = initialValue;
            }
        }
        
        final ListItemFieldProxy proxy = new ListItemFieldProxy(item);
        
        try {
            final Field valueField = ListItemFieldProxy.class.getDeclaredField("value");
            
            // Create a config entry info for this field
            final ConfigEntryInfo proxyEntryInfo = new ConfigEntryInfo(valueField, 
                new EntryDetailsAdapter("List Item #" + (index + 1), "Edit this item", entryInfo.getIcon()));
            
            // Open a config GUI for this proxy
            configInterfaceService.openConfigGui(player, proxy, "Edit List Item #" + (index + 1), result -> {
                // When the item is saved, update the list
                try {
                    boolean accessible = valueField.canAccess(result);
                    if (!accessible) {
                        valueField.setAccessible(true);
                    }
                    
                    Object newValue = valueField.get(result);
                    
                    if (!accessible) {
                        valueField.setAccessible(false);
                    }
                    
                    // Update the list item
                    ((List) list).set(index, newValue);
                    
                    // Reopen the list editor
                    openListEditor(player, context, entryInfo, list);
                } catch (Exception e) {
                    player.sendMessage(context.getChatService().format(
                            "<base>[<accent>Config<base>] <text>Failed to save list item: " + e.getMessage()
                    ));
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            player.sendMessage(context.getChatService().format(
                    "<base>[<accent>Config<base>] <text>Failed to edit list item: " + e.getMessage()
            ));
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull String formatValue(@Nullable List<?> value) {
        if (value == null) {
            return "null";
        }
        
        return value.size() + " items";
    }

    @Override
    public void startEdit(@NotNull ConfigGuiContext<?> context, @NotNull ConfigEntryInfo entryInfo) {
        // Lists are edited through a dedicated GUI, not through chat input
    }
    
    private boolean isPrimitiveNumber(Class<?> type) {
        return type == int.class || type == long.class || type == float.class || 
               type == double.class || type == short.class || type == byte.class;
    }
    
    /**
     * Adapter for EntryDetails to use with proxy fields.
     */
    private static class EntryDetailsAdapter implements net.itsthesky.terrawars.core.config.EntryDetails {
        
        private final String name;
        private final String description;
        private final Material icon;
        
        public EntryDetailsAdapter(String name, String description, Material icon) {
            this.name = name;
            this.description = description;
            this.icon = icon;
        }
        
        @Override
        public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return net.itsthesky.terrawars.core.config.EntryDetails.class;
        }
        
        @Override
        public @NotNull String name() {
            return name;
        }
        
        @Override
        public @NotNull String description() {
            return description;
        }
        
        @Override
        public @NotNull Material icon() {
            return icon;
        }
        
        @Override
        public boolean isRequired() {
            return false;
        }
        
        @Override
        public int min() {
            return -1;
        }
        
        @Override
        public int max() {
            return -1;
        }
    }
}