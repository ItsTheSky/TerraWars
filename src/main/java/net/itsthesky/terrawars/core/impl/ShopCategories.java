package net.itsthesky.terrawars.core.impl;

import net.itsthesky.terrawars.api.model.shop.ArmorLevel;
import net.itsthesky.terrawars.api.model.shop.ShopCategory;
import net.itsthesky.terrawars.api.model.shop.ShopItemType;
import net.itsthesky.terrawars.api.model.shop.items.OneTimeShopItem;
import net.itsthesky.terrawars.api.model.shop.items.PermanentShopItem;
import net.itsthesky.terrawars.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

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
            new OneTimeShopItem("blackstone", "Blackstone", player ->
                    new ItemStack(Material.POLISHED_BLACKSTONE, 12))
                    .addPrice(Material.IRON_INGOT, 16),
            new OneTimeShopItem("planks", "Planks", player ->
                    new ItemStack(Material.OAK_PLANKS, 8))
                    .addPrice(Material.GOLD_INGOT, 4)
    ));

    public static final ShopCategory WEAPONS = new ShopCategory("Weapons",
            new ItemStack(Material.GOLDEN_SWORD), List.of(
            new OneTimeShopItem("stone_sword", "Stone Sword",
                    new ItemStack(Material.STONE_SWORD))
                    .addPrice(Material.IRON_INGOT, 24),
            new OneTimeShopItem("iron_sword", "Iron Sword",
                    new ItemStack(Material.IRON_SWORD))
                    .addPrice(Material.GOLD_INGOT, 12),
            new OneTimeShopItem("diamond_sword", "Diamond Sword",
                    new ItemStack(Material.DIAMOND_SWORD))
                    .addPrice(Material.EMERALD, 6),
            new OneTimeShopItem("netherite_sword", "Netherite Sword",
                    new ItemStack(Material.NETHERITE_SWORD))
                    .addPrice(Material.EMERALD, 4)
                    .addPrice(Material.AMETHYST_SHARD, 2),
            new OneTimeShopItem("bow", "Bow",
                    new ItemStack(Material.BOW))
                    .addPrice(Material.IRON_INGOT, 8)
                    .addPrice(Material.GOLD_INGOT, 2),
            new OneTimeShopItem("bow_mk2", "Bow Mk2",
                    new ItemBuilder(Material.BOW)
                            .enchant(Enchantment.POWER, 2)
                            .getItem())
                    .addPrice(Material.IRON_INGOT, 24)
                    .addPrice(Material.GOLD_INGOT, 8),

            new OneTimeShopItem("bow_mk3", "Bow Mk3",
                    new ItemBuilder(Material.BOW)
                            .enchant(Enchantment.POWER, 5)
                            .enchant(Enchantment.PUNCH, 2)
                            .enchant(Enchantment.QUICK_CHARGE, 1)
                            .getItem())
                    .addPrice(Material.IRON_INGOT, 32)
                    .addPrice(Material.GOLD_INGOT, 16)
                    .addPrice(Material.EMERALD, 8),

            new OneTimeShopItem("arrows", "<lang:item.minecraft.arrow>",
                    new ItemStack(Material.ARROW, 8))
                    .addPrice(Material.GOLD_INGOT, 4),

            new OneTimeShopItem("fred_the_stick", "<shade-purple:500>Fred the Stick",
                    new ItemBuilder(Material.STICK)
                            .enchant(Enchantment.KNOCKBACK, 10)
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

    public static final Set<ShopCategory> CATEGORIES = Set.of(
            BLOCKS, WEAPONS, ARMORS
    );

}
