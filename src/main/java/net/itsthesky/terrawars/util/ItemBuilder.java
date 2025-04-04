package net.itsthesky.terrawars.util;

import lombok.Getter;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.base.IServiceProvider;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.UseCooldownComponent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    @Inject
    private IChatService chatService;

    @Getter
    private final ItemStack item;

    public static ItemStack fill() {
        return new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .name(" ")
                .flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
                .item;
    }
    
    public ItemBuilder(Material material) {
        IServiceProvider.instance().inject(this);

        this.item = new ItemStack(material);
    }

    public ItemBuilder(ItemStack item) {
        IServiceProvider.instance().inject(this);

        this.item = item;
    }

    public ItemBuilder name(String name, List<TextColor> scheme) {
        return name(chatService.format(name, scheme));
    }

    public ItemBuilder name(String name) {
        return name(name, Colors.YELLOW);
    }
    
    public ItemBuilder name(Component name) {
        item.editMeta(meta -> {
            if (meta == null)
                return;
            meta.displayName(name);
        });
        return this;
    }

    public ItemBuilder lore(List<TextColor> scheme, String... lore) {
        List<Component> components = new ArrayList<>();
        for (String line : lore)
            components.add(chatService.format(line, scheme));
        return lore(components);
    }

    public ItemBuilder lore(List<TextColor> scheme, List<String> lore) {
        List<Component> components = new ArrayList<>();
        for (String line : lore)
            components.add(chatService.format(line, scheme));
        return lore(components);
    }

    public ItemBuilder lore(String... lore) {
        return lore(Colors.SLATE, lore);
    }
    
    public ItemBuilder lore(Component... lore) {
        return lore(Arrays.asList(lore));
    }
    
    public ItemBuilder lore(List<Component> lore) {
        item.editMeta(meta -> {
            if (meta == null)
                return;
            meta.lore(lore);
        });
        return this;
    }
    
    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }
    
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        item.addUnsafeEnchantment(enchantment, level);
        return this;
    }
    
    public ItemBuilder flags(ItemFlag... flags) {
        item.editMeta(meta -> {
            if (meta == null)
                return;
            meta.addItemFlags(flags);
        });
        return this;
    }
    
    public ItemBuilder glow() {
        item.editMeta(meta -> {
            if (meta == null)
                return;

            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
        return this;
    }

    public ItemBuilder cleanLore() {
        return flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
    }

    public ItemBuilder unbreakable() {
        item.editMeta(meta -> {
            if (meta == null)
                return;
            meta.setUnbreakable(true);
        });
        return this;
    }

    public ItemBuilder noMovement() {
        item.editMeta(meta -> {
            if (meta == null)
                return;

            meta.getPersistentDataContainer().set(Keys.NO_MOVE_KEY, PersistentDataType.BOOLEAN, true);
        });
        return this;
    }

    public ItemBuilder setCustomModelData(int data) {
        item.editMeta(meta -> {
            if (meta == null)
                return;
            meta.setCustomModelData(data);
        });
        return this;
    }

    public <P, C> ItemBuilder setCustomData(NamespacedKey key, PersistentDataType<P, C> type, @Nullable C value) {
        item.editMeta(meta -> {
            if (meta == null)
                return;

            if (value == null)
                meta.getPersistentDataContainer().remove(key);
            else
                meta.getPersistentDataContainer().set(key, type, value);
        });
        return this;
    }

    public ItemBuilder withCooldown(int seconds, @Nullable NamespacedKey group) {
        item.editMeta(meta -> {
            if (meta == null)
                return;

            final var cooldown = meta.getUseCooldown();
            cooldown.setCooldownSeconds(seconds);
            if (group != null)
                cooldown.setCooldownGroup(group);
            meta.setUseCooldown(cooldown);
        });
        return this;
    }

    public static class ItemListener implements Listener {

        @EventHandler
        public void onItemMove(@NotNull InventoryClickEvent event) {
            if (event.getCurrentItem() == null)
                return;

            if (event.getCurrentItem().getType() == Material.AIR)
                return;

            if (event.getCurrentItem().getPersistentDataContainer().has(Keys.NO_MOVE_KEY, PersistentDataType.BOOLEAN))
                event.setCancelled(true);
        }

        @EventHandler
        public void onItemMove(@NotNull InventoryMoveItemEvent event) {
            if (event.getItem() == null || event.getItem().getType() == Material.AIR)
                return;

            if (event.getItem().getPersistentDataContainer().has(Keys.NO_MOVE_KEY, PersistentDataType.BOOLEAN))
                event.setCancelled(true);
        }

        @EventHandler
        public void onItemDrop(PlayerDropItemEvent event) {
            if (event.getItemDrop() == null || event.getItemDrop().getItemStack().getType() == Material.AIR)
                return;

            if (event.getItemDrop().getItemStack().getPersistentDataContainer().has(Keys.NO_MOVE_KEY, PersistentDataType.BOOLEAN))
                event.setCancelled(true);
        }

        @EventHandler
        public void onItemInteract(@NotNull PlayerInteractEvent event) {
            if (event.getItem() == null || event.getItem().getType() == Material.AIR)
                return;

            if (event.getItem().getPersistentDataContainer().has(Keys.NO_MOVE_KEY, PersistentDataType.BOOLEAN))
                event.setCancelled(true);
        }
    }
}