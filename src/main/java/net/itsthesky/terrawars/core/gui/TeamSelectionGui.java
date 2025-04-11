package net.itsthesky.terrawars.core.gui;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import lombok.Getter;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.services.IBaseGuiControlsService;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.core.impl.game.GameWaitingData;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TeamSelectionGui extends ChestGui {

    @Inject private IChatService chatService;
    @Inject private IBaseGuiControlsService baseGuiControlsService;
    
    private final Game game;
    private final GameWaitingData waitingData;

    public TeamSelectionGui(@NotNull Game game, @NotNull GameWaitingData waitingData) {
        super(5, ComponentHolder.of(game.getChatService().format(
                "<accent><b>→</b> <base>Team Selection", Colors.GREEN
        )));

        this.waitingData = waitingData;
        this.game = game;
        this.game.getServiceProvider().inject(this);
        
        // Initialize team player lists
        if (waitingData.getTeamPlayers().isEmpty()) {
            for (int i = 0; i < 4; i++) {
                waitingData.getTeamPlayers().add(new ArrayList<>());
            }
        }
        
        setOnGlobalClick(event -> event.setCancelled(true));
        
        addPane(baseGuiControlsService.createBaseBorderPane(5));
        
        refreshTeamSelectionPane();
        
        // Create auto refresh task
        Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("TerraWars"), () -> {
            if (getViewers().isEmpty()) return;
            refreshTeamSelectionPane();
            update();
        }, 20L, 20L);
    }

    private static final Material[] TEAM_MATERIALS = {
            Material.RED_WOOL,
            Material.BLUE_WOOL,
            Material.GREEN_WOOL,
            Material.YELLOW_WOOL
    };
    
    public void refreshTeamSelectionPane() {
        // Clear existing panes except border
        for (int i = 0; i < getPanes().size(); i++) {
            final var pane = getPanes().get(i);
            if (pane instanceof OutlinePane)
                continue;
            getPanes().remove(pane);
            i--;
        }
        
        // Create team selection panes
        final var teamsPane = new StaticPane(1, 1, 7, 3);
        addPane(teamsPane);
        
        // Add team slots
        for (int i = 0; i < 4; i++) {
            final var teamSlot = Slot.fromXY(i * 2, 0);
            
            final var maxPlayersPerTeam = game.getSize().getPlayerPerTeam();
            final var currentTeamPlayers = this.waitingData.getTeamPlayers().get(i);
            
            // Create team icon
            final var lore = new ArrayList<String>();
            lore.add("");
            lore.add("<accent>• <text>Biome: <base>Unknown for now");
            lore.add("<accent>• <text>Players: <base>" + currentTeamPlayers.size() + "/" + maxPlayersPerTeam);
            lore.add("");
            
            // Add player names
            if (currentTeamPlayers.isEmpty()) {
                lore.add("<shade-slate:600><i>No players yet</i>");
            } else {
                for (IGamePlayer player : currentTeamPlayers) {
                    lore.add("<shade-lime:500>• <shade-lime:300>" + player.getPlayer().getName());
                }
            }
            
            lore.add("");
            
            // Check if player is in this team
            final var viewer = getViewers().isEmpty() ? null : getViewers().get(0);
            final var viewerUuid = viewer != null ? viewer.getUniqueId() : null;
            final var inTeam = viewerUuid != null && currentTeamPlayers.stream()
                    .anyMatch(p -> p.getPlayer().getUniqueId().equals(viewerUuid));
            
            // Check if team is full
            final var teamFull = currentTeamPlayers.size() >= maxPlayersPerTeam;
            
            if (inTeam) {
                lore.add("<shade-lime:500><b>✔</b> <shade-lime:300>You are in this team!");
                lore.add("<shade-red:500><b>✘</b> <shade-red:300>Right-click to leave");
            } else if (teamFull) {
                lore.add("<shade-red:500><b>✘</b> <shade-red:300>This team is full!");
            } else {
                lore.add("<shade-lime:500><b>✔</b> <shade-lime:300>Click to join this team!");
            }
            
            lore.add("<shade-blue:500><b>ℹ</b> <shade-blue:300>Shift-click to view team info");
            
            final var teamIcon = new ItemBuilder(TEAM_MATERIALS[i])
                    .name("<accent><b>»</b> <base>Team " + (i + 1), Colors.GREEN)
                    .lore(Colors.EMERALD, lore)
                    .getItem();
            
            if (inTeam)
                new ItemBuilder(teamIcon).glow();

            int finalI = i;
            teamsPane.addItem(new GuiItem(teamIcon, event -> {
                final var player = (Player) event.getWhoClicked();
                final var playerUuid = player.getUniqueId();

                // Handle team leave
                if (event.getClick() == ClickType.RIGHT && inTeam) {
                    // Remove player from team
                    this.waitingData.getTeamPlayers().get(finalI).removeIf(p -> p.getPlayer().getUniqueId().equals(playerUuid));
                    
                    // Broadcast leave
                    game.broadcastMessage(Colors.RED,
                            "<shade-red:500>✘ <base>" + player.getName() + " <text>left Team " + (finalI + 1));
                    
                    refreshTeamSelectionPane();
                    update();
                    return;
                }
                
                // Handle team join
                if (event.getClick() == ClickType.LEFT && !inTeam && !teamFull) {
                    // First check if player is in another team
                    for (int j = 0; j < 4; j++) {
                        this.waitingData.getTeamPlayers().get(j).removeIf(p -> p.getPlayer().getUniqueId().equals(playerUuid));
                    }
                    
                    // Add player to team
                    final var gamePlayer = game.findGamePlayer(player);
                    if (gamePlayer != null) {
                        this.waitingData.getTeamPlayers().get(finalI).add(gamePlayer);
                        
                        // Broadcast join
                        game.broadcastMessage(Colors.EMERALD,
                                "<shade-lime:500>✔ <base>" + player.getName() + " <text>joined Team " + (finalI + 1));
                        
                        refreshTeamSelectionPane();
                        update();
                    }
                }
            }), teamSlot);
            
            // Create player slots for this team
            for (int j = 0; j < maxPlayersPerTeam; j++) {
                final var playerSlot = Slot.fromXY(i * 2, j + 1);
                
                if (j < currentTeamPlayers.size()) {
                    // Show player head
                    final var teamPlayer = currentTeamPlayers.get(j);
                    final var playerItem = new ItemBuilder(Material.PLAYER_HEAD)
                            .name("<base>" + teamPlayer.getPlayer().getName(), Colors.SKY)
                            .getItem();
                    
                    teamsPane.addItem(new GuiItem(playerItem, event -> {}), playerSlot);
                } else {
                    // Show empty slot
                    final var emptySlotItem = new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                            .name("<shade-slate:600><i>Empty Slot</i>", Colors.SLATE)
                            .getItem();
                    
                    teamsPane.addItem(new GuiItem(emptySlotItem, event -> {}), playerSlot);
                }
            }
        }
    }

}