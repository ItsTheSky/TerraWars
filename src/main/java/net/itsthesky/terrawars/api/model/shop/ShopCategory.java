package net.itsthesky.terrawars.api.model.shop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.itsthesky.terrawars.api.model.shop.items.AbstractShopItem;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor
@Getter
public class ShopCategory {

    private final String name;
    private final ItemStack icon;
    private final List<AbstractShopItem> items;

}
