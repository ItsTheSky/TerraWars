package net.itsthesky.terrawars.api.model.shop.items;

import lombok.Getter;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.shop.ShopItemType;
import net.itsthesky.terrawars.util.Checks;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
public abstract class AbstractShopItem {

    private final String id;
    private final String name;
    private final ShopItemType type;

    private @NotNull Predicate<IGamePlayer> canBuy = pl -> true;
    private @NotNull Consumer<IGamePlayer> beforeBuy = pl -> {};
    private @NotNull Consumer<IGamePlayer> afterBuy = pl -> {};

    private final Map<Material, Integer> price;

    public AbstractShopItem(String id, ShopItemType type, String name) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.price = new HashMap<>();
    }

    public AbstractShopItem addPrice(Material material, int price) {
        this.price.put(material, price);
        return this;
    }

    public AbstractShopItem setCanBuy(Predicate<IGamePlayer> canBuy) {
        Checks.notNull(canBuy, "Predicate cannot be null");

        this.canBuy = canBuy;
        return this;
    }

    public AbstractShopItem setBeforeBuy(Consumer<IGamePlayer> beforeBuy) {
        Checks.notNull(beforeBuy, "Consumer cannot be null");

        this.beforeBuy = beforeBuy;
        return this;
    }

    public AbstractShopItem setAfterBuy(Consumer<IGamePlayer> afterBuy) {
        Checks.notNull(afterBuy, "Consumer cannot be null");

        this.afterBuy = afterBuy;
        return this;
    }

    public abstract @NotNull ItemStack createDisplayItem(@NotNull IGamePlayer player);
}
