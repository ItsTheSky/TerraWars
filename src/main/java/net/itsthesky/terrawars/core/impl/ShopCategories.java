package net.itsthesky.terrawars.core.impl;

import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.shop.ArmorLevel;
import net.itsthesky.terrawars.api.model.shop.ShopCategory;
import net.itsthesky.terrawars.api.model.shop.items.OneTimeShopItem;
import net.itsthesky.terrawars.api.model.shop.items.PermanentShopItem;
import net.itsthesky.terrawars.util.ItemBuilder;
import net.itsthesky.terrawars.util.Keys;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public final class ShopCategories {

    private ShopCategories() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final ShopCategory BLOCKS = new ShopCategory("Blocks",
            new ItemStack(Material.OBSIDIAN), List.of(
            new OneTimeShopItem("wool", "Wool", player ->
                    new ItemBuilder(player.getTeam().getBiome().getWoolBlock())
                            .amount(16)
                            .getItem())
                    .addPrice(Material.IRON_INGOT, 4),
            new OneTimeShopItem("bricks", "Bricks", player ->
                    new ItemStack(Material.BRICKS, 12))
                    .addPrice(Material.IRON_INGOT, 16),
            new OneTimeShopItem("planks", "Planks", player ->
                    new ItemStack(Material.OAK_PLANKS, 8))
                    .addPrice(Material.GOLD_INGOT, 4)
    ));

    public static final ShopCategory WEAPONS = new ShopCategory("Weapons",
            new ItemStack(Material.GOLDEN_SWORD), List.of(
            new OneTimeShopItem("stone_sword", "Stone Sword",
                    new ItemBuilder(Material.STONE_SWORD)
                            .setCustomData(Keys.WEAPON_KEY, PersistentDataType.BOOLEAN, true)
                            .unbreakable()
                            .getItem())
                    .addPrice(Material.IRON_INGOT, 24)
                    .setBeforeBuy(player -> player.getPlayer().getInventory().remove(Material.WOODEN_SWORD)),
            new OneTimeShopItem("iron_sword", "Iron Sword",
                    new ItemBuilder(Material.IRON_SWORD)
                            .setCustomData(Keys.WEAPON_KEY, PersistentDataType.BOOLEAN, true)
                            .unbreakable()
                            .getItem())
                    .addPrice(Material.GOLD_INGOT, 12)
                    .setBeforeBuy(player -> player.getPlayer().getInventory().remove(Material.WOODEN_SWORD)),
            new OneTimeShopItem("diamond_sword", "Diamond Sword",
                    new ItemBuilder(Material.DIAMOND_SWORD)
                            .setCustomData(Keys.WEAPON_KEY, PersistentDataType.BOOLEAN, true)
                            .unbreakable()
                            .getItem())
                    .addPrice(Material.EMERALD, 6)
                    .setBeforeBuy(player -> player.getPlayer().getInventory().remove(Material.WOODEN_SWORD)),
            new OneTimeShopItem("netherite_sword", "Netherite Sword",
                    new ItemBuilder(Material.NETHERITE_SWORD)
                            .setCustomData(Keys.WEAPON_KEY, PersistentDataType.BOOLEAN, true)
                            .unbreakable()
                            .getItem())
                    .addPrice(Material.EMERALD, 4)
                    .addPrice(Material.AMETHYST_SHARD, 2)
                    .setBeforeBuy(player -> player.getPlayer().getInventory().remove(Material.WOODEN_SWORD)),
            new OneTimeShopItem("bow", "Bow",
                    new ItemBuilder(Material.BOW)
                            .unbreakable()
                            .getItem())
                    .addPrice(Material.IRON_INGOT, 8)
                    .addPrice(Material.GOLD_INGOT, 2),
            new OneTimeShopItem("bow_mk2", "Bow Mk2",
                    new ItemBuilder(Material.BOW)
                            .enchant(Enchantment.POWER, 2)
                            .unbreakable()
                            .getItem())
                    .addPrice(Material.IRON_INGOT, 24)
                    .addPrice(Material.GOLD_INGOT, 8),

            new OneTimeShopItem("bow_mk3", "Bow Mk3",
                    new ItemBuilder(Material.BOW)
                            .enchant(Enchantment.POWER, 5)
                            .enchant(Enchantment.PUNCH, 2)
                            .enchant(Enchantment.QUICK_CHARGE, 1)
                            .unbreakable()
                            .getItem())
                    .addPrice(Material.IRON_INGOT, 32)
                    .addPrice(Material.GOLD_INGOT, 16)
                    .addPrice(Material.EMERALD, 8),

            new OneTimeShopItem("arrows", "<lang:item.minecraft.arrow>",
                    new ItemStack(Material.ARROW, 8))
                    .addPrice(Material.GOLD_INGOT, 4),

            new OneTimeShopItem("fred_the_stick", "<shade-purple:500>Fred the Stick",
                    new ItemBuilder(Material.STICK)
                            .setCustomData(Keys.WEAPON_KEY, PersistentDataType.BOOLEAN, true)
                            .enchant(Enchantment.KNOCKBACK, 10)
                            .unbreakable()
                            .getItem())
                    .addPrice(Material.EMERALD, 24)
                    .addPrice(Material.AMETHYST_SHARD, 16)
    ));

    public static final ShopCategory ARMORS = new ShopCategory("Armors",
            new ItemStack(Material.NETHERITE_LEGGINGS), List.of(

            new PermanentShopItem("chainmail_armor", "Chainmail Armor",
                    new ItemStack(Material.CHAINMAIL_CHESTPLATE),
                    player -> player.setArmorLevel(ArmorLevel.CHAINMAIL))
                    .setCanBuy(player -> player.getArmorLevel().isLowerThan(ArmorLevel.CHAINMAIL))
                    .addPrice(Material.IRON_INGOT, 48),

            new PermanentShopItem("iron_armor", "Iron Armor",
                    new ItemStack(Material.IRON_CHESTPLATE),
                    player -> player.setArmorLevel(ArmorLevel.IRON))
                    .setCanBuy(player -> player.getArmorLevel().isLowerThan(ArmorLevel.IRON))
                    .addPrice(Material.IRON_INGOT, 24)
                    .addPrice(Material.GOLD_INGOT, 12),

            new PermanentShopItem("diamond_armor", "Diamond Armor",
                    new ItemStack(Material.DIAMOND_CHESTPLATE),
                    player -> player.setArmorLevel(ArmorLevel.DIAMOND))
                    .setCanBuy(player -> player.getArmorLevel().isLowerThan(ArmorLevel.DIAMOND))
                    .addPrice(Material.IRON_INGOT, 12)
                    .addPrice(Material.GOLD_INGOT, 8)
                    .addPrice(Material.EMERALD, 6),

            new PermanentShopItem("netherite_armor", "Netherite Armor",
                    new ItemStack(Material.NETHERITE_CHESTPLATE),
                    player -> player.setArmorLevel(ArmorLevel.NETHERITE))
                    .setCanBuy(player -> player.getArmorLevel().isLowerThan(ArmorLevel.NETHERITE))
                    .addPrice(Material.EMERALD, 12)
                    .addPrice(Material.AMETHYST_SHARD, 4)

    ));

    public static final ShopCategory UTILITIES = new ShopCategory("Utilities",
            new ItemStack(Material.BOOK), List.of(
            new OneTimeShopItem("golden_apple", "Golden Apple",
                    new ItemBuilder(Material.GOLDEN_APPLE)
                            .setCustomData(Keys.WEAPON_KEY, PersistentDataType.BOOLEAN, true)
                            .unbreakable()
                            .getItem())
                    .addPrice(Material.GOLD_INGOT, 3),
            new OneTimeShopItem("ender_pearl", "Ender Pearl",
                    new ItemBuilder(Material.ENDER_PEARL)
                            .setCustomData(Keys.WEAPON_KEY, PersistentDataType.BOOLEAN, true)
                            .unbreakable()
                            .getItem())
                    .addPrice(Material.EMERALD, 2),
            new OneTimeShopItem("tnt", "TNT",
                    new ItemBuilder(Material.TNT)
                            .getItem())
                    .addPrice(Material.GOLD_INGOT, 8),
            new OneTimeShopItem("fireball", "Fireball",
                    new ItemBuilder(Material.FIRE_CHARGE)
                            .getItem())
                    .addPrice(Material.IRON_INGOT, 32)
    ));

    public static final List<ShopCategory> CATEGORIES = List.of(
            BLOCKS, WEAPONS, ARMORS, UTILITIES
    );

    public static @Nullable ItemStack buildItem(@NotNull String itemId, @NotNull IGamePlayer player) {
        for (ShopCategory category : CATEGORIES) {
            for (final var item : category.getItems()) {
                if (item.getId().equals(itemId) && item instanceof final OneTimeShopItem oneTimeShopItem) {
                    return oneTimeShopItem.getItemBuilder().apply(player);
                }
            }
        }
        return null;
    }

}
