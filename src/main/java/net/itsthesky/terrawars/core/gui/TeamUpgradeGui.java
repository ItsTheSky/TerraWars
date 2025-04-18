package net.itsthesky.terrawars.core.gui;

import net.itsthesky.terrawars.api.gui.AbstractGUI;
import net.itsthesky.terrawars.api.gui.GUI;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.model.upgrade.ITeamUpgrade;
import net.itsthesky.terrawars.api.model.upgrade.UpgradeCategory;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.core.impl.upgrade.TeamUpgrades;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class TeamUpgradeGui extends AbstractGUI {

    private final IGame game;
    private final IGamePlayer player;
    private final IGameTeam team;
    private final Map<UpgradeCategory, List<ITeamUpgrade>> categoryUpgrades;
    private final Map<UpgradeCategory, ItemBuilder> categoryButtons;
    
    private UpgradeCategory currentCategory;

    public TeamUpgradeGui(@Nullable AbstractGUI parent, @NotNull IGame game, @NotNull IGamePlayer player, @Nullable UpgradeCategory initialCategory) {
        super(parent, BukkitUtils.chat().format(
                "<accent><b>→</b> <base>Team Upgrades", Colors.EMERALD
        ), 6);
        
        this.game = game;
        this.player = player;
        this.team = player.getTeam();
        this.categoryUpgrades = new HashMap<>();
        this.categoryButtons = new HashMap<>();
        this.currentCategory = initialCategory != null ? initialCategory : UpgradeCategory.EMBER;
        
        // Initialize all upgrade categories
        initializeUpgrades();
        
        // Set up the GUI layout
        setupLayout();
        
        // Set the content for the initial category
        setCategory(this.currentCategory);
    }
    
    public TeamUpgradeGui(@NotNull IGame game, @NotNull IGamePlayer player, @Nullable UpgradeCategory initialCategory) {
        this(null, game, player, initialCategory);
    }
    
    /**
     * Initializes all available upgrades by category.
     */
    private void initializeUpgrades() {
        // Initialize categories map
        for (UpgradeCategory category : UpgradeCategory.values())
            categoryUpgrades.put(category, new ArrayList<>());

        for (final var upgrade : TeamUpgrades.getUpgrades())
            categoryUpgrades.get(upgrade.getCategory()).add(upgrade);
    }
    
    /**
     * Sets up the basic GUI layout with borders and category buttons
     */
    private void setupLayout() {
        // Add border
        final ItemStack borderItem = new ItemBuilder(currentCategory.getPaneMaterial())
                .name(" ")
                .getItem();
                
        setItems(() -> borderItem, e -> e.setCancelled(true), getBorders());
        
        // Add back button if we have a parent
        if (getParent() != null) {
            createBackButton();
        }
    }
    
    /**
     * Sets up the category selection buttons at the bottom of the GUI
     */
    private void populateCategoryButtons() {

        int slot = 45;
        for (final UpgradeCategory category : UpgradeCategory.values()) {
            slot++;

            final boolean isUnlocked = isCategoryUnlocked(category);
            final ItemBuilder itemBuilder = createCategoryButton(category, isUnlocked);

            categoryButtons.put(category, itemBuilder);

            setItem(slot, itemBuilder::getItem, e -> {
                e.setCancelled(true);

                // Skip if category is locked
                if (!isUnlocked) {
                    final int requiredNodes = category.getRequiredCapturedNodes();
                    final int currentNodes = team.getCapturedNodes().size();

                    ((Game) game).getChatService().sendMessage(player.getPlayer(), IChatService.MessageSeverity.ERROR,
                            "This category requires <accent>" + requiredNodes + " captured nodes<text>. " +
                                    "You currently have <accent>" + currentNodes + " nodes<text>.");

                    BukkitUtils.playSound(player.getPlayer(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
                    return;
                }

                // Switch to the selected category
                setCategory(category);
                BukkitUtils.playSound(player.getPlayer(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.2f);
            });
        }
    }
    
    /**
     * Creates an ItemBuilder for a category button
     */
    private ItemBuilder createCategoryButton(UpgradeCategory category, boolean isUnlocked) {
        final List<String> lore = new ArrayList<>();
        
        // Add category description
        lore.add("");
        for (String line : category.getDescription()) {
            lore.add("<i><text>" + line);
        }
        lore.add("");
        
        // Add information about nodes requirement
        final int requiredNodes = category.getRequiredCapturedNodes();
        final int currentNodes = team.getCapturedNodes().size();
        
        if (isUnlocked) {
            lore.add("<shade-lime:500><b>✔</b> <shade-lime:300>Unlocked - " +
                    currentNodes + "/" + requiredNodes + " nodes captured");
        } else {
            lore.add("<shade-red:500><b>✘</b> <shade-red:300>Locked - Requires " +
                    requiredNodes + " captured nodes");
            lore.add("<shade-red:300>You currently have " + currentNodes + " nodes");
        }
        
        // Count available upgrades
        final List<ITeamUpgrade> upgrades = categoryUpgrades.get(category);
        lore.add("");
        lore.add("<accent>• <text>Available upgrades: <base>" + upgrades.size());
        
        // Create the item
        final ItemBuilder builder;
        if (isUnlocked) {
            builder = new ItemBuilder(Material.PLAYER_HEAD)
                    .withCustomTexture(category.getIconTexture());
        } else {
            builder = new ItemBuilder(Material.BARRIER);
        }
        
        builder.name("<accent><b>»</b> <base>" + category.getName() + " Tier", Colors.BLUE)
               .lore(Colors.BLUE, lore);
        
        return builder;
    }
    
    /**
     * Checks if a category is unlocked based on required captured nodes
     */
    private boolean isCategoryUnlocked(UpgradeCategory category) {
        final int requiredNodes = category.getRequiredCapturedNodes();
        final int currentNodes = team.getCapturedNodes().size();
        
        return currentNodes >= requiredNodes;
    }
    
    /**
     * Sets the active category and updates the GUI content
     */
    private void setCategory(UpgradeCategory category) {
        // Highlight the selected category button
        for (Map.Entry<UpgradeCategory, ItemBuilder> entry : categoryButtons.entrySet()) {
            if (entry.getKey() == category) {
                entry.getValue().glow();
            } else {
                entry.getValue().removeGlow();
            }
        }
        
        this.currentCategory = category;
        
        // Update the border color to match the category
        final ItemStack borderItem = new ItemBuilder(category.getPaneMaterial())
                .name(" ")
                .getItem();
                
        setItems(() -> borderItem, e -> e.setCancelled(true), getBorders());
        
        // Clear the content area
        for (int i = 0; i < 45; i++) {
            int finalI = i;
            if (!IntStream.of(getBorders()).anyMatch(j -> j == finalI)) {
                setItem(i, null, null);
            }
        }
        
        // Populate the content area with upgrades
        populateUpgrades(category);
        populateCategoryButtons();
        
        refreshInventory();
    }
    
    /**
     * Populates the GUI with upgrades from the given category
     */
    private void populateUpgrades(UpgradeCategory category) {
        final List<ITeamUpgrade> upgrades = categoryUpgrades.get(category);
        
        if (upgrades.isEmpty()) {
            // Show a message if no upgrades are available
            final ItemStack noUpgradesItem = new ItemBuilder(Material.BARRIER)
                    .name("<accent><b>✘</b> <base>No upgrades available", Colors.RED)
                    .lore(Colors.RED, "<text>This category doesn't have any upgrades yet")
                    .getItem();
                    
            setItem(22, () -> noUpgradesItem, e -> e.setCancelled(true));
            return;
        }
        
        // Display the upgrades in the center of the GUI
        int slot = 10;
        for (ITeamUpgrade upgrade : upgrades) {
            // Skip slots at the edge
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot++;
            if (slot >= 45) break;

            final ItemBuilder upgradeItem = createUpgradeItem(upgrade);
            
            setItem(slot, upgradeItem::getItem, e -> {
                e.setCancelled(true);
                handleUpgradeClick(upgrade);
            });
            
            slot++;
        }
    }
    
    /**
     * Creates an ItemBuilder for an upgrade
     */
    private ItemBuilder createUpgradeItem(ITeamUpgrade upgrade) {
        final int currentLevel = team.getUpgradeLevel(upgrade);
        final int maxLevel = upgrade.getMaxLevel();
        final int nextLevel = Math.min(currentLevel + 1, maxLevel);
        
        final List<String> lore = new ArrayList<>();
        
        // Add main description
        lore.add("");
        for (String line : upgrade.getDescription()) {
            lore.add("<text><i>" + line);
        }
        lore.add("");
        
        // Add current level information
        lore.add("<accent>• <text>Current level: <base>" + currentLevel + "/" + maxLevel);
        
        // If not maxed, add next level benefits
        if (currentLevel < maxLevel) {
            lore.add("");
            lore.add("<accent>• <text>Next level benefits:");
            lore.addAll(upgrade.buildDescription(team, currentLevel));
            
            // Add costs
            lore.add("");
            lore.add("<accent>• <text>Cost:");
            
            final Map<Material, Integer> costs = upgrade.getCosts(team, nextLevel);
            final boolean canAfford = canAffordUpgrade(costs);
            final boolean hasRequiredUpgrades = hasRequiredUpgrades(upgrade);
            
            for (Map.Entry<Material, Integer> cost : costs.entrySet()) {
                final Material material = cost.getKey();
                final int amount = cost.getValue();
                final int playerAmount = countMaterial(material);
                
                final String colorPrefix = playerAmount >= amount ? "<shade-lime:500>" : "<shade-red:500>";
                lore.add("  " + colorPrefix + "- " + amount + "x <lang:" + material.getItemTranslationKey() + "> " +
                        "(" + playerAmount + "/" + amount + ")");
            }

            // Add requirements
            if (!upgrade.getRequiredUpgrades().isEmpty()) {
                lore.add("");
                lore.add("<accent>• <text>Required upgrades:");
                for (Map.Entry<ITeamUpgrade, Integer> entry : upgrade.getRequiredUpgrades().entrySet()) {
                    final ITeamUpgrade requiredUpgrade = entry.getKey();
                    final int requiredLevel = entry.getValue();
                    final int playerLevel = team.getUpgradeLevel(requiredUpgrade);

                    final String colorPrefix = playerLevel >= requiredLevel ? "<shade-lime:500>" : "<shade-red:500>";
                    lore.add("  " + colorPrefix + "- " + requiredUpgrade.getName() + " (required: " +
                            requiredLevel + ", current: " + playerLevel + ")");
                }
            }
            
            // Add call to action
            lore.add("");
            if (!hasRequiredUpgrades) {
                lore.add("<shade-red:500><b>✘</b> <shade-red:300>You don't meet the requirements!");
            } else if (!canAfford) {
                lore.add("<shade-red:500><b>✘</b> <shade-red:300>You can't afford this upgrade!");
            } else {
                lore.add("<shade-lime:500><b>✔</b> <shade-lime:300>Click to purchase this upgrade!");
            }
        } else {
            // Max level reached
            lore.add("");
            lore.add("<shade-yellow:500><b>⚠</b> <shade-yellow:300>Maximum level reached!");
        }
        
        return new ItemBuilder(upgrade.getIcon())
                .name("<accent><b>»</b> <base>" + upgrade.getName(), Colors.EMERALD)
                .lore(Colors.EMERALD, lore);
    }
    
    /**
     * Handles the click action on an upgrade item
     */
    private void handleUpgradeClick(ITeamUpgrade upgrade) {
        final int currentLevel = team.getUpgradeLevel(upgrade);
        final int maxLevel = upgrade.getMaxLevel();
        final int nextLevel = Math.min(currentLevel + 1, maxLevel);
        
        // Check if max level
        if (currentLevel >= maxLevel) {
            ((Game) game).getChatService().sendMessage(player.getPlayer(), IChatService.MessageSeverity.WARNING,
                    "This upgrade is already at its maximum level!");
            BukkitUtils.playSound(player.getPlayer(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.8f);
            return;
        }

        // check if has required upgrades
        if (!hasRequiredUpgrades(upgrade)) {
            ((Game) game).getChatService().sendMessage(player.getPlayer(), IChatService.MessageSeverity.ERROR,
                    "You don't meet the requirements for this upgrade!");
            BukkitUtils.playSound(player.getPlayer(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }
        
        // Check if can afford
        final Map<Material, Integer> costs = upgrade.getCosts(team, nextLevel);
        if (!canAffordUpgrade(costs)) {
            ((Game) game).getChatService().sendMessage(player.getPlayer(), IChatService.MessageSeverity.ERROR,
                    "You don't have enough resources for this upgrade!");
            BukkitUtils.playSound(player.getPlayer(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }
        
        // Purchase the upgrade
        purchaseUpgrade(upgrade, nextLevel, costs);
        
        // Show success message
        ((Game) game).getChatService().sendMessage(player.getPlayer(), IChatService.MessageSeverity.SUCCESS,
                "Successfully purchased the <accent>" + upgrade.getName() + "<text> upgrade (Level " + nextLevel + ")!");
        BukkitUtils.playSound(player.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.2f);
        
        // Refresh the GUI to update the upgrade & related items
        refreshInventory();
    }
    
    /**
     * Checks if the player can afford an upgrade based on its costs
     */
    private boolean canAffordUpgrade(Map<Material, Integer> costs) {
        for (Map.Entry<Material, Integer> cost : costs.entrySet()) {
            final Material material = cost.getKey();
            final int requiredAmount = cost.getValue();
            final int playerAmount = countMaterial(material);
            
            if (playerAmount < requiredAmount) {
                return false;
            }
        }
        return true;
    }

    private boolean hasRequiredUpgrades(ITeamUpgrade upgrade) {
        for (Map.Entry<ITeamUpgrade, Integer> entry : upgrade.getRequiredUpgrades().entrySet()) {
            final ITeamUpgrade requiredUpgrade = entry.getKey();
            final int requiredLevel = entry.getValue();
            final int playerLevel = team.getUpgradeLevel(requiredUpgrade);

            if (playerLevel < requiredLevel) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Counts how many of a specific material the player has in their inventory
     */
    private int countMaterial(Material material) {
        int count = 0;
        for (ItemStack item : player.getPlayer().getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }
    
    /**
     * Purchases an upgrade by deducting resources and applying the upgrade
     */
    private void purchaseUpgrade(ITeamUpgrade upgrade, int level, Map<Material, Integer> costs) {
        // Deduct resources from player's inventory
        for (Map.Entry<Material, Integer> cost : costs.entrySet()) {
            final Material material = cost.getKey();
            final int amount = cost.getValue();
            
            // Remove the required amount of the material
            int remaining = amount;
            for (ItemStack item : player.getPlayer().getInventory().getContents()) {
                if (item != null && item.getType() == material) {
                    if (item.getAmount() <= remaining) {
                        remaining -= item.getAmount();
                        player.getPlayer().getInventory().remove(item);
                    } else {
                        item.setAmount(item.getAmount() - remaining);
                        remaining = 0;
                    }
                    
                    if (remaining <= 0) {
                        break;
                    }
                }
            }
        }
        
        // Apply the upgrade
        upgrade.applyUpgrade(player, team, level);
        
        // Create upgrade effect for the entire team
        for (IGamePlayer teamPlayer : team.getPlayers()) {
            if (teamPlayer.isOnline()) {
                final Player teamMember = teamPlayer.getPlayer();
                
                // Display title to team members
                ((Game) game).getChatService().sendTitle(new IChatService.TitleBuilder()
                        .audience(teamMember)
                        .title("<shade-emerald:500>Team Upgrade Unlocked!")
                        .subtitle("<base>" + upgrade.getName() + " <text>Level " + level)
                        .scheme(team.getColorScheme())
                        .time(500, 2000, 500));
                
                // Play sound for team members
                if (!Objects.equals(teamMember, player.getPlayer())) {
                    BukkitUtils.playSound(teamMember, Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.0f);
                }
            }
        }
        
        // Broadcast message to the team
        ((Game) game).broadcastMessage(team.getColorScheme(),
                "<accent>" + player.getPlayer().getName() + "<text> purchased the upgrade <accent>" +
                        upgrade.getName() + "<text> Level " + level + "!",
                team, null);
        
        // Refresh this GUI and category
        this.setCategory(currentCategory);
    }

    @Override
    public @NotNull GUI createCopy(@NotNull Player player) {
        final IGamePlayer gamePlayer = game.findGamePlayer(player);
        if (gamePlayer == null)
            throw new IllegalStateException("Player is not in the game!");
            
        return new TeamUpgradeGui(getParent(), game, gamePlayer, currentCategory);
    }
}