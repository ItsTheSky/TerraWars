package net.itsthesky.terrawars.core.gui;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.shop.ShopCategory;
import net.itsthesky.terrawars.api.model.shop.ShopItemType;
import net.itsthesky.terrawars.api.model.shop.items.AbstractShopItem;
import net.itsthesky.terrawars.api.model.shop.items.OneTimeShopItem;
import net.itsthesky.terrawars.api.model.shop.items.PermanentShopItem;
import net.itsthesky.terrawars.api.services.IBaseGuiControlsService;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.core.impl.ShopCategories;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import net.itsthesky.terrawars.util.Keys;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShopKeeperGui extends ChestGui {

    @Inject
    private IBaseGuiControlsService baseGuiControlsService;
    @Inject
    private IChatService chatService;
    private final IGame game;
    private final IGamePlayer player;
    private final Map<ShopCategory, Integer> categoryPages;
    private final Map<ShopCategory, ItemBuilder> categoryItems;
    private final PaginatedPane contentPane;
    private final StaticPane categoriesPane;

    public ShopKeeperGui(@NotNull IGame game, @NotNull IGamePlayer player) {
        super(6, ComponentHolder.of(BukkitUtils.chat().format(
                "<accent><b>→</b> <base>Item Shop", Colors.INDIGO
        )));
        ((Game) game).getServiceProvider().inject(this);

        this.game = game;
        this.player = player;
        this.categoryPages = new HashMap<>();
        this.categoryItems = new HashMap<>();

        addPane(baseGuiControlsService.createBaseBorderPane(6));
        addPane(this.contentPane = new PaginatedPane(1, 1, 7, 4));
        addPane(this.categoriesPane = new StaticPane(1, 5, 9, 1));

        int index = 0;
        for (final var category : ShopCategories.CATEGORIES) {
            final var item = createCategoryItem(category);
            categoriesPane.addItem(item, Slot.fromIndex(index));

            final var pane = createCategoryPane(category);
            contentPane.addPage(pane);

            categoryPages.put(category, index);
            index++;
        }

        selectFirstCategory();
    }

    private void selectFirstCategory() {
        if (categoryPages.isEmpty()) return;
        final var firstCategory =
                categoryPages.keySet().iterator().next();
        setCategory(firstCategory);
    }

    private void setCategory(@NotNull ShopCategory category) {
        this.contentPane.setPage(categoryPages.get(category));
        this.update();
    }

    private Pane createCategoryPane(@NotNull ShopCategory category) {
        final var pane = new StaticPane(Slot.fromXY(0, 0), 7, 4);
        int slotIndex = 0;

        for (final var shopItem : category.getItems()) {
            final var slot = Slot.fromIndex(slotIndex);

            final var lore = new ArrayList<String>();
            if (shopItem.getType() == ShopItemType.PERMANENT) {
                lore.add("<shade-violet:500><b>✔</b> <shade-violet:300>PERMANENT ITEM");
                lore.add("<shade-violet:800><i>You won't lose this item on death.</i>");
            }

            lore.add("");

            lore.add("<accent><b>→</b> <base>Price(s):");
            for (final var entry : shopItem.getPrice().entrySet()) {
                final var amount = entry.getValue();
                final var material = entry.getKey();

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

            final var item = new ItemBuilder(shopItem.createDisplayItem(this.player))
                    .lore(Colors.BLUE, lore)
                    .name(shopItem.getName(), Colors.CYAN)
                    .getItem();

            pane.addItem(new GuiItem(item, evt -> {
                evt.setCancelled(true);
                if (!canReallyBuy) {
                    chatService.sendMessage(player.getPlayer(), IChatService.MessageSeverity.ERROR, !canBuy
                            ? "You cannot buy this item!"
                            : "You don't have enough items to buy this!");
                    BukkitUtils.playSound(player.getPlayer(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    return;
                }

                for (final var entry : shopItem.getPrice().entrySet()) {
                    final var material = entry.getKey();
                    final var amount = entry.getValue();

                    player.getPlayer().getInventory().removeItem(new ItemStack(material, amount));
                }

                if (shopItem.getType() == ShopItemType.ONE_TIME && shopItem instanceof final OneTimeShopItem oneTimeShopItem) {
                    final var givenItem = new ItemBuilder(oneTimeShopItem.getItemBuilder().apply(this.player))
                            .setCustomData(Keys.SHOP_ITEM_KEY, PersistentDataType.STRING, shopItem.getId());
                    player.getPlayer().getInventory().addItem(givenItem.getItem());
                } else if (shopItem.getType() == ShopItemType.PERMANENT && shopItem instanceof final PermanentShopItem permanentShopItem) {
                    permanentShopItem.getApplyItem().accept(this.player);
                }

                chatService.sendMessage(player.getPlayer(), IChatService.MessageSeverity.SUCCESS,
                        "You bought " + shopItem.getName() + "!");
                BukkitUtils.playSound(player.getPlayer(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

                final var gui = new ShopKeeperGui(game, player);
                gui.setCategory(category);
                gui.show(player.getPlayer());
            }), slot);

            slotIndex++;
        }

        for (int i = slotIndex; i < 6 * 4; i++) {
            final var slot = Slot.fromIndex(i);
            pane.addItem(new GuiItem(ItemBuilder.createEmpty()), slot);
        }

        return pane;
    }

    private GuiItem createCategoryItem(@NotNull ShopCategory category) {
        final var lore = new ArrayList<String>();
        lore.add("");
        lore.add("<accent><b>→ </b> <base>Click to open this category!");
        lore.add("");

        final var item = new ItemBuilder(category.getIcon())
                .cleanLore()
                .lore(Colors.TEAL, lore)
                .name("<accent><b>→</b> <base>" + category.getName(), Colors.TEAL)
                .getItem();

        return new GuiItem(item, evt -> setCategory(category));
    }

    private boolean hasEnoughToBuy(@NotNull AbstractShopItem shopItem) {
        for (final var entry : shopItem.getPrice().entrySet()) {
            final var material = entry.getKey();
            final var amount = entry.getValue();

            if (!player.getPlayer().getInventory().contains(material, amount))
                return false;
        }
        return true;
    }

}
