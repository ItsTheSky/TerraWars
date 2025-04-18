package net.itsthesky.terrawars.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import lombok.Getter;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.base.IServiceProvider;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class ItemBuilder {

     private static final Map<String, Consumer<PlayerInteractEvent>> INTERACT_ACTIONS = new HashMap<>();

    @Inject private IChatService chatService;

    @Getter
    private final ItemStack item;

    public static ItemStack fill() {
        return new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .name(" ")
                .flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
                .item;
    }

    public static ItemStack air() {
        return new ItemBuilder(Material.AIR).item;
    }
    
    public ItemBuilder(Material material) {
        IServiceProvider.instance().inject(this);

        this.item = new ItemStack(material);
    }

    public ItemBuilder(ItemStack item) {
        IServiceProvider.instance().inject(this);

        this.item = item;
    }

    public static @NotNull ItemStack createEmpty() {
        return new ItemStack(Material.AIR);
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

    public ItemBuilder addInteraction(@NotNull String interId, @Nullable Consumer<PlayerInteractEvent> action) {
        if (action == null) {
            INTERACT_ACTIONS.remove(interId);
            return this;
        }

        INTERACT_ACTIONS.put(interId, action);
        item.editMeta(meta -> {
            if (meta == null)
                return;
            final var container = meta.getPersistentDataContainer().getOrDefault(Keys.INTERACTION_KEY,
                    PersistentDataType.TAG_CONTAINER, meta.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer());
            container.set(new NamespacedKey(Keys.NAMESPACE, interId), PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(Keys.INTERACTION_KEY, PersistentDataType.TAG_CONTAINER, container);
        });
        return this;
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

    public ItemBuilder removeGlow() {
        item.editMeta(meta -> {
            if (meta == null)
                return;

            meta.removeEnchant(Enchantment.LURE);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
        return this;
    }

    public ItemBuilder cleanLore() {
        return flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_DYE, ItemFlag.HIDE_STORED_ENCHANTS);
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
        return setCustomData(Keys.NO_MOVE_KEY, PersistentDataType.BOOLEAN, true);
    }

    public ItemBuilder destroyOnDrop() {
        return setCustomData(Keys.DESTROY_ON_DROP_KEY, PersistentDataType.BOOLEAN, true);
    }

    public ItemBuilder setCustomModelData(int data) {
        item.editMeta(meta -> {
            if (meta == null)
                return;
            meta.setCustomModelData(data);
        });
        return this;
    }

    public ItemBuilder withCustomTexture(@NotNull String texture) {
        if (!texture.startsWith("ey"))
            throw new IllegalArgumentException("Invalid texture string: " + texture);
        if (!item.getType().equals(Material.PLAYER_HEAD))
            throw new IllegalArgumentException("Item type must be PLAYER_HEAD to set a custom texture.");

        item.editMeta(SkullMeta.class, meta -> {
            if (meta == null)
                return;

            // calculate an UUID based on the texture:
            final var uuid = UUID.nameUUIDFromBytes(texture.getBytes());
            final var playerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16));
            playerProfile.setProperty(new ProfileProperty("textures", texture));
            meta.setPlayerProfile(playerProfile);
        });

        return this;
    }

    public ItemBuilder withOwner(@NotNull OfflinePlayer owner) {
        if (!item.getType().equals(Material.PLAYER_HEAD))
            throw new IllegalArgumentException("Item type must be PLAYER_HEAD to set a custom texture.");

        item.editMeta(SkullMeta.class, meta -> {
            if (meta == null)
                return;

            meta.setOwningPlayer(owner);
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

    public ItemBuilder withLeatherArmorColor(TextColor color) {
        item.editMeta(LeatherArmorMeta.class, meta -> {
            if (meta == null)
                return;

            meta.setColor(BukkitUtils.convertColor(color));
        });
        return this;
    }

    public ItemBuilder setType(Material paneMaterial) {
        item.setType(paneMaterial);
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

            if (event.getItem().getPersistentDataContainer().has(Keys.INTERACTION_KEY, PersistentDataType.TAG_CONTAINER)) {
                final var container = event.getItem().getPersistentDataContainer().get(Keys.INTERACTION_KEY, PersistentDataType.TAG_CONTAINER);
                for (var entry : container.getKeys()) {
                    final var action = INTERACT_ACTIONS.get(entry.getKey());
                    System.out.println("Found action for " + entry.getKey() + ": " + action);
                    if (action != null)
                        action.accept(event);
                }
            }
        }

        @EventHandler
        public void onItemDropDestroy(@NotNull PlayerDropItemEvent event) {
            final var item = event.getItemDrop();
            if (item == null)
                return;

            if (item.getItemStack().getPersistentDataContainer().getOrDefault(Keys.DESTROY_ON_DROP_KEY, PersistentDataType.BOOLEAN, false)) {
                item.remove();
                event.setCancelled(true);
            }
        }
    }
}