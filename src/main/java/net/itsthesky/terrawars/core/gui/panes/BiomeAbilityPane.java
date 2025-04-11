package net.itsthesky.terrawars.core.gui.panes;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import net.itsthesky.terrawars.api.model.ability.AbilityType;
import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.itsthesky.terrawars.api.services.IBaseGuiControlsService;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BiomeAbilityPane extends ChestGui {

    @Inject private IChatService chatService;
    @Inject private IBaseGuiControlsService baseGuiControlsService;
    
    private final ChestGui parentGui;
    private final IBiome biome;
    private final Game game;

    public BiomeAbilityPane(@NotNull ChestGui parentGui, @NotNull IBiome biome, @NotNull Game game) {
        super(4, ComponentHolder.of(game.getChatService().format(
                "<accent><b>→</b> <base>" + biome.getName() + " <shade-indigo:500>Abilities",
                biome.getScheme()
        )));
        
        this.parentGui = parentGui;
        this.biome = biome;
        this.game = game;
        
        game.getServiceProvider().inject(this);
        
        setOnGlobalClick(event -> event.setCancelled(true));
        
        addPane(baseGuiControlsService.createBaseBorderPane(4));
        
        // Create abilities pane
        final var abilitiesPane = new StaticPane(1, 1, 7, 2);
        addPane(abilitiesPane);
        
        // Add abilities
        int index = 0;
        for (final var ability : biome.getAvailableAbilities()) {
            final var row = index / 7;
            final var col = index % 7;
            final var slot = Slot.fromXY(col, row);
            
            final var lore = new ArrayList<String>();
            lore.add("");
            
            // Add ability type
            if (ability.getType().equals(AbilityType.PASSIVE))
                lore.add("<shade-violet:700><b>▷</b> <shade-violet:900>PASSIVE ABILITY");
            else
                lore.add("<shade-indigo:700><b>▷</b> <shade-indigo:900>ACTIVE ABILITY");
            
            lore.add("");
            
            // Add description
            for (final var line : ability.getDescription())
                lore.add("<text><i>" + line);
            
            lore.add("");
            
            // Add cooldown for active abilities
            if (ability.getType().equals(AbilityType.ACTIVE)) {
                lore.add("<accent><b>⌚</b> <base>Cooldown: " + ability.getCooldownSeconds() + "s");
            }
            
            final var item = new ItemBuilder(ability.getIcon())
                    .name("<accent><b>»</b> <base>" + ability.getDisplayName(), biome.getScheme())
                    .lore(biome.getScheme(), lore)
                    .getItem();
            
            abilitiesPane.addItem(new GuiItem(item, event -> {}), slot);
            
            index++;
        }
        
        // Add back button
        final var controlsPane = new StaticPane(0, 3, 9, 1);
        addPane(controlsPane);
        
        controlsPane.addItem(baseGuiControlsService.createBackButton(event -> {
            parentGui.show(event.getWhoClicked());
        }), Slot.fromIndex(4));
    }
}