package net.itsthesky.terrawars.api.gui;

import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractGUI implements GUI {

    private final HashMap<Integer, Consumer<InventoryClickEvent>> consumers;
    private final HashMap<Integer, Supplier<ItemStack>> items;
    private final Inventory inventory;
    private final @Nullable AbstractGUI parent;
    private final Map<Integer, BukkitTask> refreshTasks;

    private Component title;

    public AbstractGUI(@Nullable AbstractGUI parent, final @NotNull Component title, int rows) {
        this(parent, title, rows, null);
    }

    public AbstractGUI(@Nullable AbstractGUI parent, final @NotNull Component title, int rows, @Nullable InventoryType inventoryType) {
        this.parent = parent;
        consumers = new HashMap<>();
        items = new HashMap<>();
        refreshTasks = new HashMap<>();
        if (rows > 0 && rows <= 6)
            rows = rows * 9;
        if (inventoryType == null)
            this.inventory = Bukkit.createInventory(this, rows, title);
        else
            this.inventory = Bukkit.createInventory(this, inventoryType, title);

        setTitle(title);
    }

    public void setTitle(Component title) {
        getInventory().getViewers().forEach(player -> BukkitUtils.updateTitle((Player) player, title));
        this.title = title;
    }

    public void createBackButton() {
        createBackButton(0);
    }

    public void createBackButton(int slot) {
        if (parent != null)
            setItem(slot, () -> new ItemBuilder(Material.ARROW)
                    .name("<accent><bold>« <base>Retour", Colors.RED)
                    .getItem(), e -> {
                e.setCancelled(true);
                openNew((Player) e.getWhoClicked(), parent);
            });
    }

    public void openNew(final @NotNull Player player, @NotNull AbstractGUI gui) {
        final AbstractGUI copy = (AbstractGUI) gui.createCopy(player);
        if (copy instanceof final AbstractPaginationGUI<?> paginationGUI) {
            paginationGUI.usePagination(gui);
            copy.open(player);
            paginationGUI.setTitle(gui.title);
            paginationGUI.refresh(player);
        } else
            copy.open(player);
    }

    @Nullable
    @Override
    public AbstractGUI getParent() {
        return parent;
    }

    @Override
    public void setItem(int slot, @Nullable Supplier<ItemStack> item, @Nullable Consumer<InventoryClickEvent> consumer) {
        inventory.setItem(slot, item == null ? null : (item.get() == null ? null : item.get()));
        if (consumer == null)
            consumers.remove(slot);
        else
            consumers.put(slot, consumer);

        if (item == null)
            items.remove(slot);
        else
            items.put(slot, item);
    }

    @Override
    public void setupRefresh(int slot, long delay) {
        if (this.refreshTasks.containsKey(slot)) {
            this.refreshTasks.get(slot).cancel();
            this.refreshTasks.remove(slot);
        }

        refreshTasks.put(slot, Bukkit.getScheduler().runTaskTimerAsynchronously(JavaPlugin.getProvidingPlugin(TerraWars.class), () -> {
            if (inventory.getViewers().isEmpty())
                return;
            inventory.setItem(slot, items.get(slot).get());
        }, delay, delay));
    }

    public static void later(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getProvidingPlugin(TerraWars.class), runnable, delay);
    }

    public static void laterAsync(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(JavaPlugin.getProvidingPlugin(TerraWars.class), runnable, delay);
    }

    public static void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(TerraWars.class), runnable);
    }

    public static void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getProvidingPlugin(TerraWars.class), runnable);
    }

    public static BukkitTask setupTimer(Runnable runnable, long delay) {
        return Bukkit.getScheduler().runTaskTimer(JavaPlugin.getProvidingPlugin(TerraWars.class), runnable, 0, delay);
    }

    public static BukkitTask setupTimerAsync(Runnable runnable, long delay) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(JavaPlugin.getProvidingPlugin(TerraWars.class), runnable, 0, delay);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void refreshSlot(int slot) {
        if (!items.containsKey(slot))
            return;

        final ItemStack newItem = items.get(slot).get();
        if (newItem == null)
            return;

        // check if the item is the same
        if (inventory.getItem(slot) != null && inventory.getItem(slot).isSimilar(newItem))
            return;

        // replace the values of the old item with the new one
        inventory.getItem(slot).setAmount(newItem.getAmount());
        inventory.getItem(slot).setType(newItem.getType());
        inventory.getItem(slot).setItemMeta(newItem.getItemMeta());
    }

    public void refreshInventory() {
        for (int i = 0; i < inventory.getSize(); i++)
            refreshSlot(i);
    }

    @Override
    public void open(Player... players) {
        for (Player player : players)
            player.openInventory(getInventory());
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public static class InventoryListener implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void onInventoryClick(InventoryClickEvent event) {
            boolean isInside = event.getView().getTopInventory() == event.getClickedInventory();
            if (event.getView().getTopInventory().getHolder() instanceof final AbstractGUI gui) {
                final Consumer<InventoryClickEvent> consumer = gui.consumers.getOrDefault(event.getSlot(), null);
                if (consumer != null && isInside)
                    consumer.accept(event);
                gui.onClick(event, isInside);
            }
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onInventoryOpen(InventoryOpenEvent event) {
            if (event.getInventory().getHolder() instanceof final AbstractGUI gui)
                gui.onOpen(event);
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onInventoryClose(InventoryCloseEvent event) {
            if (event.getInventory().getHolder() instanceof final AbstractGUI gui) {
                gui.onClose(event);
                gui.refreshTasks.values().forEach(BukkitTask::cancel);
            }
        }

    }

    public List<Integer> getListBorder() {
        final List<Integer> border = new ArrayList<>();
        for (int i : getBorders())
            border.add(i);
        return border;
    }

}
