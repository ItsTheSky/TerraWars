package net.itsthesky.terrawars.api.model.shop.items;

import lombok.Getter;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.shop.ShopItemType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Getter
public class OneTimeShopItem extends AbstractShopItem {

    private final Function<IGamePlayer, ItemStack> itemBuilder;
    public OneTimeShopItem(String id, String name,
                           Function<IGamePlayer, ItemStack> itemBuilder) {
        super(id, ShopItemType.ONE_TIME, name);
        this.itemBuilder = itemBuilder;
    }

    public OneTimeShopItem(String id, String name,
                           ItemStack item) {
        this(id, name, player -> item.clone());
    }

    @Override
    public @NotNull ItemStack createDisplayItem(@NotNull IGamePlayer player) {
        return itemBuilder.apply(player);
    }
}
