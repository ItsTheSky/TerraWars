package net.itsthesky.terrawars.api.model.shop.items;

import lombok.Getter;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.shop.ShopItemType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Getter
public class PermanentShopItem extends AbstractShopItem {

    private final Consumer<IGamePlayer> applyItem;
    private final ItemStack display;
    public PermanentShopItem(String id, String name, ItemStack display,
                             Consumer<IGamePlayer> applyItem) {
        super(id, ShopItemType.PERMANENT, name);

        this.display = display;
        this.applyItem = applyItem;
    }

    @Override
    public @NotNull ItemStack createDisplayItem(@NotNull IGamePlayer player) {
        return display.clone();
    }
}
