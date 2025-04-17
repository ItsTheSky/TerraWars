package net.itsthesky.terrawars.core.gui;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import lombok.Getter;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.model.upgrade.ITeamUpgrade;
import net.itsthesky.terrawars.api.model.upgrade.UpgradeCategory;
import net.itsthesky.terrawars.api.services.IBaseGuiControlsService;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.core.impl.upgrade.TeamUpgrades;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * GUI for team upgrades, displaying available categories and upgrades.
 * The GUI shows different categories at the bottom, each with their own set of upgrades.
 * Categories can be locked if the team doesn't have enough captured nodes.
 */
public class TeamUpgradeGui extends ChestGui {

    @Inject
    private IChatService chatService;
    @Inject
    private IBaseGuiControlsService baseGuiControlsService;

    private final Game game;
    private final IGamePlayer player;
    private final IGameTeam team;

    private final Map<UpgradeCategory, List<ITeamUpgrade>> categoryUpgrades;
    private final StaticPane contentPane;
    private final StaticPane categoriesPane;
    private final ItemBuilder outlineItemBuilder;

    @Getter
    private UpgradeCategory currentCategory;

    /**
     * Creates a new TeamUpgradeGui.
     *
     * @param game   The game instance
     * @param player The player viewing the GUI
     */
    public TeamUpgradeGui(@NotNull Game game, @NotNull IGamePlayer player,
                          @Nullable UpgradeCategory category) {
        super(6, ComponentHolder.of(BukkitUtils.chat().format(
                "<accent><b>→</b> <base>Team Upgrades", Colors.EMERALD
        )));

        this.game = game;
        this.player = player;
        this.team = player.getTeam();
        this.categoryUpgrades = new HashMap<>();
        this.currentCategory = category != null ? category : UpgradeCategory.EMBER;

        // Inject services
        game.getServiceProvider().inject(this);

        // Configure GUI behavior
        setOnBottomClick(evt -> evt.setCancelled(true));

        // Initialize upgrade categories and their upgrades
        initializeUpgrades();

        // Add panes
        this.outlineItemBuilder = new ItemBuilder(this.currentCategory.getPaneMaterial());
        addPane(baseGuiControlsService.createBaseBorderPane(6, this.outlineItemBuilder.getItem()));
        addPane(this.contentPane = new StaticPane(1, 1, 7, 4));
        addPane(this.categoriesPane = new StaticPane(1, 5, 7, 1));

        // Setup category buttons at the bottom
        setupCategoryButtons();
    }

    /**
     * Initializes all available upgrades by category.
     */
    private void initializeUpgrades() {
        // Initialize the map for each category
        for (UpgradeCategory category : UpgradeCategory.values()) {
            categoryUpgrades.put(category, new ArrayList<>());
        }

        // Add each upgrade to its respective category
        // For now, we only have one upgrade in the Ember category
        categoryUpgrades.get(UpgradeCategory.EMBER).add(TeamUpgrades.GENERATOR_SPEED);

        // In a real implementation, you would add more upgrades here
        // For example:
        // categoryUpgrades.get(UpgradeCategory.CRYSTAL).add(TeamUpgrades.PLAYER_SPEED);
        // categoryUpgrades.get(UpgradeCategory.CELESTIAL).add(TeamUpgrades.ARMOR_RESISTANCE);
    }

    /**
     * Sets up the category buttons at the bottom of the GUI.
     */
    private void setupCategoryButtons() {
        final var categories = UpgradeCategory.values();

        for (int i = 0; i < categories.length; i++) {
            final var category = categories[i];
            final var slot = Slot.fromIndex(i);

            final var isUnlocked = isCategoryUnlocked(category);
            final var item = createCategoryItem(category, isUnlocked);

            categoriesPane.addItem(new GuiItem(item, evt -> {
                evt.setCancelled(true);

                // Skip if category is locked
                if (!isUnlocked) {
                    final var requiredNodes = category.getRequiredCapturedNodes();
                    final var currentNodes = team.getCapturedNodes().size();

                    chatService.sendMessage(player.getPlayer(), IChatService.MessageSeverity.ERROR,
                            "This category requires <accent>" + requiredNodes + " captured nodes<text>. " +
                                    "You currently have <accent>" + currentNodes + " nodes<text>.");

                    BukkitUtils.playSound(player.getPlayer(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
                    return;
                }

                setCategory(category);
                BukkitUtils.playSound(player.getPlayer(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.2f);
            }), slot);
        }
    }

    /**
     * Creates an item representing an upgrade category.
     *
     * @param category   The upgrade category
     * @param isUnlocked Whether the category is unlocked for the player's team
     * @return The category item
     */
    private ItemStack createCategoryItem(UpgradeCategory category, boolean isUnlocked) {
        final var lore = new ArrayList<String>();

        // Add category description
        lore.add("");
        for (String line : category.getDescription()) {
            lore.add("<i><text>" + line);
        }
        lore.add("");

        // Add information about nodes requirement
        final var requiredNodes = category.getRequiredCapturedNodes();
        final var currentNodes = team.getCapturedNodes().size();

        if (isUnlocked) {
            lore.add("<shade-lime:500><b>✔</b> <shade-lime:300>Unlocked - " +
                    currentNodes + "/" + requiredNodes + " nodes captured");
        } else {
            lore.add("<shade-red:500><b>✘</b> <shade-red:300>Locked - Requires " +
                    requiredNodes + " captured nodes");
            lore.add("<shade-red:300>You currently have " + currentNodes + " nodes");
        }

        // Count available upgrades
        final var upgrades = categoryUpgrades.get(category);
        lore.add("");
        lore.add("<accent>• <text>Available upgrades: <base>" + upgrades.size());

        // Build the item
        final var builder = new ItemBuilder(isUnlocked ? Material.PLAYER_HEAD : Material.BARRIER);
        if (isUnlocked)
            builder.withCustomTexture(category.getIconTexture());
        // For testing: use the actual material even when locked

        builder.name("<accent><b>»</b> <base>" + category.getName() + " Tier", Colors.BLUE)
                .lore(Colors.BLUE, lore);

        // Highlight current category
        if (category == currentCategory) {
            builder.glow();
        }

        return builder.getItem();
    }

    /**
     * Checks if a category is unlocked based on the required captured nodes.
     *
     * @param category The category to check
     * @return true if the category is unlocked, false otherwise
     */
    private boolean isCategoryUnlocked(UpgradeCategory category) {
        final var requiredNodes = category.getRequiredCapturedNodes();
        final var currentNodes = team.getCapturedNodes().size();

        return currentNodes >= requiredNodes;
    }

    /**
     * Sets the current category and updates the content pane.
     *
     * @param category The category to display
     */
    public void setCategory(UpgradeCategory category) {
        final var newGui = new TeamUpgradeGui(game, player, category);
        newGui.show(player.getPlayer());
    }

    /**
     * Updates the content pane with upgrades from the current category.
     */
    private void updateContentPane() {
        contentPane.clear();

        final var upgrades = categoryUpgrades.get(currentCategory);
        if (upgrades.isEmpty()) {
            // Show a message if no upgrades are available
            final var item = new ItemBuilder(Material.BARRIER)
                    .name("<accent><b>✘</b> <base>No upgrades available", Colors.RED)
                    .lore(Colors.RED, "<text>This category doesn't have any upgrades yet")
                    .getItem();

            contentPane.addItem(new GuiItem(item, evt -> evt.setCancelled(true)), 3, 2);
            return;
        }

        // Display the upgrades
        int index = 0;
        for (ITeamUpgrade upgrade : upgrades) {
            final var row = index / 7;
            final var col = index % 7;

            contentPane.addItem(createUpgradeItem(upgrade), col, row);
            index++;
        }
    }

    /**
     * Creates an item representing a team upgrade.
     *
     * @param upgrade The team upgrade
     * @return A GuiItem for the upgrade
     */
    private GuiItem createUpgradeItem(ITeamUpgrade upgrade) {
        final var currentLevel = team.getUpgradeLevel(upgrade);
        final var maxLevel = upgrade.getMaxLevel();
        final var nextLevel = Math.min(currentLevel + 1, maxLevel);

        final var lore = new ArrayList<String>();

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

            final var costs = upgrade.getCosts(team, nextLevel);
            final var canAfford = canAffordUpgrade(costs);

            for (Map.Entry<Material, Integer> cost : costs.entrySet()) {
                final var material = cost.getKey();
                final var amount = cost.getValue();
                final var playerAmount = countMaterial(material);

                final var colorPrefix = playerAmount >= amount ? "<shade-lime:500>" : "<shade-red:500>";
                lore.add("  " + colorPrefix + "- " + amount + "x <lang:" + material.getItemTranslationKey() + "> " +
                        "(" + playerAmount + "/" + amount + ")");
            }

            // Add call to action
            lore.add("");
            if (canAfford) {
                lore.add("<shade-lime:500><b>✔</b> <shade-lime:300>Click to purchase this upgrade!");
            } else {
                lore.add("<shade-red:500><b>✘</b> <shade-red:300>You can't afford this upgrade!");
            }
        } else {
            // Max level reached
            lore.add("");
            lore.add("<shade-yellow:500><b>⚠</b> <shade-yellow:300>Maximum level reached!");
        }

        // Create the item
        final var item = new ItemBuilder(upgrade.getIcon())
                .name("<accent><b>»</b> <base>" + upgrade.getName(), Colors.EMERALD)
                .lore(Colors.EMERALD, lore)
                .getItem();

        // Return the final GuiItem with click behavior
        return new GuiItem(item, evt -> {
            evt.setCancelled(true);

            // Check if max level
            if (currentLevel >= maxLevel) {
                chatService.sendMessage(player.getPlayer(), IChatService.MessageSeverity.WARNING,
                        "This upgrade is already at its maximum level!");
                BukkitUtils.playSound(player.getPlayer(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.8f);
                return;
            }

            // Check if can afford
            final var costs = upgrade.getCosts(team, nextLevel);
            if (!canAffordUpgrade(costs)) {
                chatService.sendMessage(player.getPlayer(), IChatService.MessageSeverity.ERROR,
                        "You don't have enough resources for this upgrade!");
                BukkitUtils.playSound(player.getPlayer(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
                return;
            }

            // Purchase the upgrade
            purchaseUpgrade(upgrade, nextLevel, costs);

            // Show success message
            chatService.sendMessage(player.getPlayer(), IChatService.MessageSeverity.SUCCESS,
                    "Successfully purchased the <accent>" + upgrade.getName() + "<text> upgrade (Level " + nextLevel + ")!");
            BukkitUtils.playSound(player.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.2f);

            // Refresh the GUI
            final var refreshedGui = new TeamUpgradeGui(game, player, currentCategory);
            refreshedGui.show(player.getPlayer());
        });
    }

    /**
     * Checks if the player can afford an upgrade based on its costs.
     *
     * @param costs The resource costs of the upgrade
     * @return true if the player can afford the upgrade, false otherwise
     */
    private boolean canAffordUpgrade(Map<Material, Integer> costs) {
        for (Map.Entry<Material, Integer> cost : costs.entrySet()) {
            final var material = cost.getKey();
            final var requiredAmount = cost.getValue();
            final var playerAmount = countMaterial(material);

            if (playerAmount < requiredAmount) {
                return false;
            }
        }
        return true;
    }

    /**
     * Counts how many of a specific material the player has in their inventory.
     *
     * @param material The material to count
     * @return The amount of the material in the player's inventory
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
     * Purchases an upgrade by deducting resources and applying the upgrade.
     *
     * @param upgrade The upgrade to purchase
     * @param level   The level to purchase
     * @param costs   The costs of the upgrade
     */
    private void purchaseUpgrade(ITeamUpgrade upgrade, int level, Map<Material, Integer> costs) {
        // Deduct resources from player's inventory
        for (Map.Entry<Material, Integer> cost : costs.entrySet()) {
            final var material = cost.getKey();
            final var amount = cost.getValue();

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
                final var teamMember = teamPlayer.getPlayer();

                // Display title to team members
                game.getChatService().sendTitle(new IChatService.TitleBuilder()
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
        game.broadcastMessage(team.getColorScheme(),
                "<accent>" + player.getPlayer().getName() + "<text> purchased the upgrade <accent>" +
                        upgrade.getName() + "<text> Level " + level + "!",
                team, null);
    }
}