package net.itsthesky.terrawars.core.gui;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import net.itsthesky.terrawars.api.model.ability.IAbility;
import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.itsthesky.terrawars.api.services.IBaseGuiControlsService;
import net.itsthesky.terrawars.api.services.IBiomeService;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.core.gui.panes.BiomeAbilityPane;
import net.itsthesky.terrawars.core.impl.game.GameWaitingData;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BiomeVotingGui extends ChestGui {

    @Inject private IChatService chatService;
    @Inject private IBaseGuiControlsService baseGuiControlsService;
    @Inject private IBiomeService biomeService;

    private final Game game;
    private final StaticPane biomesPane;
    private final GameWaitingData waitingData;

    public BiomeVotingGui(@NotNull Game game, @NotNull GameWaitingData waitingData) {
        super(5, ComponentHolder.of(game.getChatService().format(
                "<accent><b>→</b> <base>Biome Voting", Colors.PURPLE
        )));

        this.waitingData = waitingData;
        this.game = game;
        this.game.getServiceProvider().inject(this);
        
        setOnGlobalClick(event -> event.setCancelled(true));
        
        addPane(baseGuiControlsService.createBaseBorderPane(5));
        
        biomesPane = new StaticPane(1, 1, 7, 3);
        addPane(biomesPane);
        
        refreshBiomesPane();
    }
    
    public void refreshBiomesPane() {
        biomesPane.clear();
        
        final var biomes = biomeService.getAvailableBiomes();
        int index = 0;
        
        for (IBiome biome : biomes) {
            final var row = index / 7;
            final var col = index % 7;
            final var slot = Slot.fromXY(col, row);
            
            final var votes = waitingData.getVoteCount().getOrDefault(biome, 0);
            final var lore = new ArrayList<String>();
            lore.add("");
            for (String desc : biome.getDescription()) {
                lore.add("<i><text>" + desc);
            }
            lore.add("");
            lore.add("<accent>• <text>Votes: <base>" + votes);
            lore.add("");
            
            // Check if player already voted for this biome
            final var viewer = getViewers().isEmpty() ? null : getViewers().get(0);
            final var viewerUuid = viewer != null ? viewer.getUniqueId() : null;
            final var hasVoted = viewerUuid != null && waitingData.getPlayerVotes().containsKey(viewerUuid)
                    && waitingData.getPlayerVotes().get(viewerUuid).equals(biome);
            
            if (hasVoted) {
                lore.add("<shade-lime:500><b>✔</b> <shade-lime:300>You voted for this biome!");
            } else {
                lore.add("<shade-yellow:500><b>✔</b> <shade-yellow:300>Click to vote for this biome!");
            }
            
            lore.add("<shade-blue:500><b>ℹ</b> <shade-blue:300>Right-click to view abilities");
            
            final var item = new ItemBuilder(biome.getMainBlock())
                    .name("<accent><b>»</b> <base>" + biome.getName(), biome.getScheme())
                    .lore(biome.getScheme(), lore)
                    .getItem();
            
            if (hasVoted) {
                new ItemBuilder(item).glow();
            }
            
            biomesPane.addItem(new GuiItem(item, event -> {
                if (event.getClick() == ClickType.RIGHT) {
                    // Open abilities view
                    showBiomeAbilities(biome, (Player) event.getWhoClicked());
                    return;
                }
                
                // Handle vote
                final var player = (Player) event.getWhoClicked();
                final var uuid = player.getUniqueId();
                
                // Remove vote from previously voted biome if any
                if (waitingData.getPlayerVotes().containsKey(uuid)) {
                    final var previousBiome = waitingData.getPlayerVotes().get(uuid);
                    waitingData.getVoteCount().put(previousBiome, waitingData.getVoteCount().getOrDefault(previousBiome, 1) - 1);
                }
                
                // Add vote to new biome
                waitingData.getPlayerVotes().put(uuid, biome);
                waitingData.getVoteCount().put(biome, waitingData.getVoteCount().getOrDefault(biome, 0) + 1);
                
                // Broadcast vote
                game.broadcastMessage(biome.getScheme(), 
                        "<shade-lime:500>✔ <base>" + player.getName() + " <text>voted for the <accent>" + biome.getName() + " <text>biome!");
                
                // Update the GUI for all viewers
                refreshBiomesPane();
                update();
                
            }), slot);
            
            index++;
        }
    }
    
    private void showBiomeAbilities(IBiome biome, Player player) {
        final var abilitiesPane = new BiomeAbilityPane(this, biome, game);
        abilitiesPane.show(player);
    }
}