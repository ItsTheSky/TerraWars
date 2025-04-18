package net.itsthesky.terrawars.core.gui;

import net.itsthesky.terrawars.api.gui.AbstractGUI;
import net.itsthesky.terrawars.api.gui.GUI;
import net.itsthesky.terrawars.api.model.ability.AbilityType;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.shop.ArmorLevel;
import net.itsthesky.terrawars.api.model.shop.ShopCategory;
import net.itsthesky.terrawars.api.model.shop.ShopItemType;
import net.itsthesky.terrawars.api.model.shop.items.AbstractShopItem;
import net.itsthesky.terrawars.api.model.shop.items.OneTimeShopItem;
import net.itsthesky.terrawars.api.model.shop.items.PermanentShopItem;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.core.impl.ShopCategories;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import net.itsthesky.terrawars.util.Keys;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ShopKeeperGui extends AbstractGUI {

    private static final ShopCategory ABILITY_CATEGORY =
            new ShopCategory("Abilities", new ItemStack(Material.FEATHER), null);

    private final IGame game;
    private final IGamePlayer player;
    private final Map<ShopCategory, Integer> categoryPages;
    private final Map<ShopCategory, ItemBuilder> categoryItems;
    
    private ShopCategory currentCategory;

    public ShopKeeperGui(@NotNull AbstractGUI parent, @NotNull IGame game, @NotNull IGamePlayer player) {
        super(parent, ((Game) game).getChatService().format(
                "<accent><b>→</b> <base>Item Shop", Colors.INDIGO
        ), 6);
        
        this.game = game;
        this.player = player;
        this.categoryPages = new HashMap<>();
        this.categoryItems = new HashMap<>();
        
        // Fill with empty panes
        setItems(() -> new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .getItem(), e -> e.setCancelled(true), IntStream.range(0, getInventory().getSize()).toArray());
        
        // Add border
        setItems(() -> new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .name(" ")
                .getItem(), e -> e.setCancelled(true), getBorders());
        
        // Add back button if we have a parent
        if (parent != null)
            createBackButton();
        
        // Setup category tabs at the bottom
        setupCategoryTabs();
        
        // Set default category
        setCategory(ShopCategories.CATEGORIES.getFirst());
    }
    
    public ShopKeeperGui(@NotNull IGame game, @NotNull IGamePlayer player) {
        this(null, game, player);
    }
    
    private void setupCategoryTabs() {
        int index = 0;
        for (final ShopCategory category : ShopCategories.CATEGORIES) {
            final ItemBuilder item = createCategoryItem(category);
            categoryItems.put(category, item);
            
            categoryPages.put(category, index);
            
            setItem(index + 45, () -> item.getItem(), e -> {
                e.setCancelled(true);
                setCategory(category);
                player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            });
            
            index++;
        }
        
        // Add the ability category
        final ItemBuilder abilityItem = createCategoryItem(ABILITY_CATEGORY);
        categoryItems.put(ABILITY_CATEGORY, abilityItem);
        categoryPages.put(ABILITY_CATEGORY, index);
        
        setItem(index + 45, () -> abilityItem.getItem(), e -> {
            e.setCancelled(true);
            setCategory(ABILITY_CATEGORY);
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        });
    }
    
    private ItemBuilder createCategoryItem(@NotNull ShopCategory category) {
        final List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("<accent><b>✔</b> <base>Click to open this category!");

        return new ItemBuilder(category.getIcon())
                .cleanLore()
                .lore(Colors.LIME, lore)
                .name("<accent><b>→</b> <base>" + category.getName(), Colors.SKY);
    }
    
    private void setCategory(@NotNull ShopCategory category) {
        // Reset previous category's highlight
        if (this.currentCategory != null && this.currentCategory != category) {
            this.categoryItems.get(this.currentCategory).removeGlow();
        }
        
        this.currentCategory = category;
        this.categoryItems.get(category).glow();
        
        // Clear the content area
        for (int i = 0; i < 45; i++) {
            int finalI = i;
            if (!IntStream.of(getBorders()).anyMatch(j -> j == finalI)) {
                setItem(i, null, null);
            }
        }
        
        // Populate with category items
        if (category == ABILITY_CATEGORY) {
            populateAbilityItems();
        } else {
            populateCategoryItems(category);
        }
        
        refreshInventory();
    }
    
    private void populateCategoryItems(ShopCategory category) {
        int slot = 10;
        final List<AbstractShopItem> items = category.getItems();
        
        for (AbstractShopItem shopItem : items) {
            // Skip rows that are part of the border
            if (slot % 9 == 0) slot += 2;
            if (slot % 9 == 8) slot += 2;
            if (slot >= 45) break;
            
            final AbstractShopItem finalShopItem = shopItem;
            
            final List<String> lore = new ArrayList<>();
            if (shopItem.getType() == ShopItemType.PERMANENT) {
                lore.add("<shade-violet:500><b>✔</b> <shade-violet:300>PERMANENT ITEM");
                lore.add("<shade-violet:800><i>You won't lose this item on death.</i>");
            }

            lore.add("");

            lore.add("<accent><b>→</b> <base>Price(s):");
            for (final Map.Entry<Material, Integer> entry : shopItem.getPrice().entrySet()) {
                final int amount = entry.getValue();
                final Material material = entry.getKey();

                lore.add("  <base>- <text>" + amount + "x " + "<lang:" + material.getItemTranslationKey() + ">");
            }
            lore.add("");

            final boolean canBuy = shopItem.getCanBuy().test(player);
            final boolean hasEnough = hasEnoughToBuy(shopItem);
            final boolean canReallyBuy = canBuy && hasEnough;

            if (canReallyBuy) {
                lore.add("<shade-lime:500><b>✔</b> <shade-lime:300>Click to buy this item!");
            } else {
                if (!canBuy) {
                    lore.add("<shade-red:500><b>✘</b> <shade-red:300>You cannot buy this item!");
                } else {
                    lore.add("<shade-red:500><b>✘</b> <shade-red:300>You don't have enough items to buy this!");
                }
            }
            
            final ItemBuilder itemBuilder = new ItemBuilder(shopItem.createDisplayItem(this.player))
                    .lore(Colors.BLUE, lore)
                    .name(shopItem.getName(), Colors.CYAN);
            
            setItem(slot, () -> itemBuilder.getItem(), e -> {
                e.setCancelled(true);
                
                if (!canReallyBuy) {
                    ((Game) game).getChatService().sendMessage(player.getPlayer(), IChatService.MessageSeverity.ERROR, !canBuy
                            ? "You cannot buy this item!"
                            : "You don't have enough items to buy this!");
                    BukkitUtils.playSound(player.getPlayer(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    return;
                }

                // Deduct price
                for (final Map.Entry<Material, Integer> entry : finalShopItem.getPrice().entrySet()) {
                    final Material material = entry.getKey();
                    final int amount = entry.getValue();
                    player.getPlayer().getInventory().removeItem(new ItemStack(material, amount));
                }

                finalShopItem.getBeforeBuy().accept(this.player);

                // Apply item effects
                if (finalShopItem.getType() == ShopItemType.ONE_TIME && finalShopItem instanceof final OneTimeShopItem oneTimeShopItem) {
                    final ItemBuilder givenItem = new ItemBuilder(oneTimeShopItem.getItemBuilder().apply(this.player))
                            .setCustomData(Keys.SHOP_ITEM_KEY, PersistentDataType.STRING, finalShopItem.getId());
                    player.getPlayer().getInventory().addItem(givenItem.getItem());
                } else if (finalShopItem.getType() == ShopItemType.PERMANENT && finalShopItem instanceof final PermanentShopItem permanentShopItem) {
                    permanentShopItem.getApplyItem().accept(this.player);
                }

                ((Game) game).getChatService().sendMessage(player.getPlayer(), IChatService.MessageSeverity.SUCCESS,
                        "You bought " + finalShopItem.getName() + "!");
                BukkitUtils.playSound(player.getPlayer(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

                finalShopItem.getAfterBuy().accept(this.player);

                // Refresh to update item availability
                setCategory(category);
            });
            
            slot++;
        }
    }
    
    private void populateAbilityItems() {
        int slot = 10;
        
        for (final var ability : player.getTeam().getBiome().getAvailableAbilities()) {
            // Skip rows that are part of the border
            if (slot % 9 == 0) slot += 2;
            if (slot % 9 == 8) slot += 2;
            if (slot >= 45) break;
            
            final List<String> lore = new ArrayList<>();
            if (ability.getType().equals(AbilityType.PASSIVE))
                lore.add("<shade-violet:700><b>▷</b> <shade-violet:900>PASSIVE ABILITY");
            else
                lore.add("<shade-indigo:700><b>▷</b> <shade-indigo:900>ACTIVE ABILITY");

            lore.add("");
            for (final String line : ability.getDescription())
                lore.add("<text><i>" + line);
            lore.add("");
            if (ability.getType().equals(AbilityType.ACTIVE)) {
                lore.add("<accent><b>⌚</b> <base>Cooldown: " + ability.getCooldownSeconds() + "s");
                lore.add("");
            }

            if (ability.equals(this.player.getSelectedAbility())) {
                lore.add("<shade-lime:500><b>✔</b> <shade-lime:300>You have this ability!");
            } else {
                lore.add("<shade-yellow:500><b>✔</b> <shade-yellow:300>Click to select this ability!");
            }
            
            final ItemBuilder builder = new ItemBuilder(ability.getIcon())
                    .name(ability.getDisplayName(), Colors.EMERALD)
                    .lore(Colors.EMERALD, lore);
                    
            if (this.player.getSelectedAbility() != null && this.player.getSelectedAbility().equals(ability))
                builder.glow();
            
            setItem(slot, () -> builder.getItem(), e -> {
                e.setCancelled(true);
                
                if (ability.equals(this.player.getSelectedAbility())) {
                    ((Game) game).getChatService().sendMessage(player.getPlayer(), IChatService.MessageSeverity.ERROR,
                            "You already have this ability!");
                    BukkitUtils.playSound(player.getPlayer(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    return;
                }

                ((Game) game).getChatService().sendMessage(player.getPlayer(), IChatService.MessageSeverity.SUCCESS,
                        "You selected the ability " + ability.getDisplayName() + "!");
                player.setSelectedAbility(ability);
                BukkitUtils.playSound(player.getPlayer(), Sound.ITEM_ARMOR_EQUIP_WOLF, 1, 1);

                // Refresh to update the selection
                setCategory(ABILITY_CATEGORY);
            });
            
            slot++;
        }
    }
    
    private boolean hasEnoughToBuy(@NotNull AbstractShopItem shopItem) {
        for (final Map.Entry<Material, Integer> entry : shopItem.getPrice().entrySet()) {
            final Material material = entry.getKey();
            final int amount = entry.getValue();

            if (!player.getPlayer().getInventory().contains(material, amount))
                return false;
        }
        return true;
    }

    @Override
    public @NotNull GUI createCopy(@NotNull Player player) {
        final IGamePlayer gamePlayer = game.findGamePlayer(player);
        if (gamePlayer == null)
            throw new IllegalStateException("Player is not in the game!");
            
        return new ShopKeeperGui(getParent(), game, gamePlayer);
    }
}