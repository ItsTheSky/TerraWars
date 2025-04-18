package net.itsthesky.terrawars.core.gui;

import net.itsthesky.terrawars.api.gui.AbstractGUI;
import net.itsthesky.terrawars.api.gui.GUI;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.core.impl.game.GameWaitingData;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class TeamSelectionGui extends AbstractGUI {

    @Inject private IChatService chatService;
    
    private final Game game;
    private final GameWaitingData waitingData;
    private final Map<Integer, ItemBuilder> teamItems;

    private static final Material[] TEAM_MATERIALS = {
            Material.RED_WOOL,
            Material.BLUE_WOOL,
            Material.GREEN_WOOL,
            Material.YELLOW_WOOL
    };

    public TeamSelectionGui(@Nullable AbstractGUI parent, @NotNull Game game, @NotNull GameWaitingData waitingData) {
        super(parent, BukkitUtils.chat().format(
                "<accent><b>→</b> <base>Team Selection", Colors.GREEN
        ), 6);

        this.waitingData = waitingData;
        this.game = game;
        this.teamItems = new HashMap<>();
        
        // Inject services
        this.game.getServiceProvider().inject(this);
        
        // Initialize team player lists if empty
        if (waitingData.getTeamPlayers().isEmpty()) {
            for (int i = 0; i < 4; i++) {
                waitingData.getTeamPlayers().add(new ArrayList<>());
            }
        }
        
        // Base GUI setup
        setupLayout();
        
        // Populate with data
        refreshTeamSelection();
    }

    public TeamSelectionGui(@NotNull Game game, @NotNull GameWaitingData waitingData) {
        this(null, game, waitingData);
    }
    
    private void setupLayout() {
        // Add border glass
        setItems(ItemBuilder::fill, e -> e.setCancelled(true), getBorders());
        setItems(() -> new ItemStack(Material.LIME_STAINED_GLASS_PANE), e -> e.setCancelled(true), getCorners());

        createBackButton();
    }

    public void refreshTeamSelection() {
        // Clear existing items in the inner area
        for (int i = 0; i < getInventory().getSize(); i++) {
            int finalI = i;
            if (!IntStream.of(getBorders()).anyMatch(j -> j == finalI)) {
                setItem(i, null, null);
            }
        }
        
        // Add team slots
        for (int i = 0; i < 4; i++) {
            final var teamSlot = ((i + 1) * 9) + 1;
            
            final var maxPlayersPerTeam = game.getSize().getPlayerPerTeam();
            final var currentTeamPlayers = this.waitingData.getTeamPlayers().get(i);
            
            // Create team icon with information
            createTeamIcon(i, teamSlot, maxPlayersPerTeam, currentTeamPlayers);
            
            // Create player slots for this team
            createPlayerSlots(i, teamSlot, maxPlayersPerTeam, currentTeamPlayers);
        }
        
        refreshInventory();
    }
    
    private void createTeamIcon(int teamIndex, int teamSlot, int maxPlayersPerTeam, List<IGamePlayer> currentTeamPlayers) {
        // Create team lore with information
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
        
        // Get current player viewing the inventory
        final var viewer = getInventory().getViewers().isEmpty() ? null : getInventory().getViewers().get(0);
        final var viewerUuid = viewer != null ? viewer.getUniqueId() : null;
        
        // Check if player is in this team
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
        
        // lore.add("<shade-blue:500><b>ℹ</b> <shade-blue:300>Shift-click to view team info");
        
        // Create the team icon item
        final var teamIcon = new ItemBuilder(TEAM_MATERIALS[teamIndex])
                .name("<accent><b>»</b> <base>Team " + (teamIndex + 1), Colors.GREEN)
                .lore(Colors.EMERALD, lore);
        
        if (inTeam) {
            teamIcon.glow();
        }
        
        teamItems.put(teamIndex, teamIcon);
        
        // Set the item in the GUI with click handler
        setItem(teamSlot, teamIcon::getItem, event -> {
            event.setCancelled(true);
            final var player = (Player) event.getWhoClicked();
            final var playerUuid = player.getUniqueId();
            
            // Handle team leave (right-click)
            if (event.getClick() == ClickType.RIGHT && inTeam) {
                // Remove player from team
                this.waitingData.getTeamPlayers().get(teamIndex).removeIf(p -> 
                        p.getPlayer().getUniqueId().equals(playerUuid));
                
                // Broadcast leave message
                game.broadcastMessage(Colors.RED,
                        "<shade-red:500>✘ <base>" + player.getName() + " <text>left Team " + (teamIndex + 1));
                
                // Play sound
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.8f);
                
                // Refresh GUI
                refreshTeamSelection();
                return;
            }
            
            // Handle team join (left-click)
            if ((event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT) && !inTeam && !teamFull) {
                // First check if player is in another team
                for (int j = 0; j < 4; j++) {
                    this.waitingData.getTeamPlayers().get(j).removeIf(p -> 
                            p.getPlayer().getUniqueId().equals(playerUuid));
                }
                
                // Add player to team
                final var gamePlayer = game.findGamePlayer(player);
                if (gamePlayer != null) {
                    this.waitingData.getTeamPlayers().get(teamIndex).add(gamePlayer);
                    
                    // Broadcast join message
                    game.broadcastMessage(Colors.EMERALD,
                            "<shade-lime:500>✔ <base>" + player.getName() + " <text>joined Team " + (teamIndex + 1));
                    
                    // Play sound
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                    
                    // Refresh GUI
                    refreshTeamSelection();
                }
                return;
            }
            
            // Handle team info view (shift-click)
            if (event.getClick() == ClickType.SHIFT_RIGHT) {
                // TODO: Implement team info view if needed
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            }
        });
    }
    
    private void createPlayerSlots(int teamIndex, int teamSlot, int maxPlayersPerTeam, List<IGamePlayer> currentTeamPlayers) {
        // Calculate the starting slot for player slots (below the team icon)
        
        for (int j = 0; j < maxPlayersPerTeam; j++) {
            final int playerSlot = teamSlot + j + 1;
            
            if (j < currentTeamPlayers.size()) {
                // Show player head
                final var teamPlayer = currentTeamPlayers.get(j);
                final var playerItem = new ItemBuilder(Material.PLAYER_HEAD)
                        .name("<base>" + teamPlayer.getPlayer().getName(), Colors.SKY)
                        .lore(Colors.SKY, "<text>Member of Team " + (teamIndex + 1))
                        .withOwner(teamPlayer.getPlayer())
                        .getItem();
                
                setItem(playerSlot, () -> playerItem, e -> e.setCancelled(true));
            } else {
                // Show empty slot
                final var emptySlotItem = new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .name("<shade-slate:600><i>Empty Slot</i>", Colors.SLATE)
                        .getItem();
                
                setItem(playerSlot, () -> emptySlotItem, e -> e.setCancelled(true));
            }
        }
    }

    @Override
    public @NotNull GUI createCopy(@NotNull Player player) {
        return new TeamSelectionGui(getParent(), game, waitingData);
    }
}