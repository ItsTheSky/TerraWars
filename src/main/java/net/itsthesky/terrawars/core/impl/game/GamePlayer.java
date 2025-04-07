package net.itsthesky.terrawars.core.impl.game;

import lombok.Getter;
import lombok.Setter;
import net.itsthesky.terrawars.api.model.ability.AbilityType;
import net.itsthesky.terrawars.api.model.ability.IAbility;
import net.itsthesky.terrawars.api.model.ability.PassiveAbility;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.model.shop.ArmorLevel;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.util.*;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter @Setter
public class GamePlayer implements IGamePlayer {

    private final OfflinePlayer offlinePlayer;
    private final Game game;
    private @Nullable IGameTeam team;

    private ArmorLevel armorLevel = ArmorLevel.LEATHER;
    private GamePlayerState state;

    private @Nullable IAbility selectedAbility;
    private BukkitTask updatePlayerTask;

    public GamePlayer(OfflinePlayer player, Game game) {
        this.offlinePlayer = player;
        this.game = game;

        this.state = GamePlayerState.WAITING;

        this.team = null;
        this.selectedAbility = null;
        this.updatePlayerTask = null;
    }


    @Override
    public @Nullable IGameTeam getTeam() throws IllegalStateException {
        if (team == null)
            throw new IllegalStateException("Game has not started yet, player is not in a team.");

        return team;
    }

    @Override
    public void setTeam(@Nullable IGameTeam team) throws IllegalStateException {
        Checks.notNull(team, "Team cannot be null");

        this.team = team;
    }

    @Override
    public void setState(@NotNull GamePlayerState state) {
        Checks.notNull(state, "State cannot be null");

        this.state = state;
    }

    private static final int ABILITY_SLOT = 8;

    @Override
    public void setSelectedAbility(@Nullable IAbility ability) {
        // Unregister the previous ability's listener if it was passive
        if (this.selectedAbility != null) {
            if (this.selectedAbility.getType() == AbilityType.PASSIVE)
                ((PassiveAbility) this.selectedAbility).unregisterListener(this);

            this.selectedAbility.onDeselect(this);
        }

        this.selectedAbility = ability;

        // Register the new ability's listener if it is passive
        if (this.selectedAbility != null) {
            if (this.selectedAbility.getType() == AbilityType.PASSIVE)
                ((PassiveAbility) this.selectedAbility).registerListener(this, this.game);

            this.selectedAbility.onSelect(this);
        }

        setupHotbar(false);
    }

    @Override
    public void setupHotbar(boolean clear) {
        if (!isOnline())
            return;

        final var player = this.offlinePlayer.getPlayer();
        if (clear)
            player.getInventory().clear();

        if (this.selectedAbility == null) {
            player.getInventory().setItem(ABILITY_SLOT, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                    .noMovement()
                    .name("<accent><b>No ability selected", Colors.RED)
                    .lore(Colors.RED, "<text>Select your ability at a <base>shopkeeper<text>!")
                    .getItem());
        } else {
            player.getInventory().setItem(ABILITY_SLOT,
                    selectedAbility.buildHotBarItem(this));
        }
    }

    @Override
    public void setup() {
        if (!isOnline())
            return;

        final var player = Objects.requireNonNull(this.offlinePlayer.getPlayer());
        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        player.setLevel(0);
        player.setExp(0);

        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(0);

        final var availableAbilities = this.team.getBiome().getAvailableAbilities();
        if (availableAbilities.isEmpty()) {
            game.getChatService().sendMessage(player, IChatService.MessageSeverity.WARNING, "No abilities available for this team.");
        } else {
            final var firstAbility = availableAbilities.get(0);
            setSelectedAbility(firstAbility);
            game.getChatService().sendMessage(player, IChatService.MessageSeverity.SUCCESS,
                    "Selected the <accent>" + firstAbility.getDisplayName() + "<text> ability!");
        }

        setupHotbar(true);
        setArmorLevel(ArmorLevel.LEATHER);

        this.updatePlayerTask = createUpdatePlayerTask();
    }

    private static final Set<Material> MELEE_MATERIALS = Set.of(
            Material.WOODEN_SWORD,
            Material.STONE_SWORD,
            Material.IRON_SWORD,
            Material.DIAMOND_SWORD,
            Material.NETHERITE_SWORD
    );

    private BukkitTask createUpdatePlayerTask() {
        return BukkitUtils.runTaskTimer(() -> {
            if (!isOnline())
                return;
            final var player = Objects.requireNonNull(this.offlinePlayer.getPlayer());

            // Check if there's any melee (swords). If not, add a wooden sword
            List<Material> foundMeleeWeapons = new ArrayList<>();
            for (final var mat : MELEE_MATERIALS) {
                if (player.getInventory().contains(mat))
                    foundMeleeWeapons.add(mat);
                if (player.getItemOnCursor().getType() == mat)
                    foundMeleeWeapons.add(mat);
            }

            if (foundMeleeWeapons.isEmpty()) {
                player.getInventory().addItem(new ItemBuilder(Material.WOODEN_SWORD)
                        .cleanLore()
                        .unbreakable()
                        .destroyOnDrop()
                        .lore(Colors.ORANGE, "<text>Default melee weapon")
                        .getItem());
            } else if (foundMeleeWeapons.size() > 1 &&
                    foundMeleeWeapons.contains(Material.WOODEN_SWORD)) {
                player.getInventory().remove(Material.WOODEN_SWORD);
            }
        }, 20, 20);
    }

    @Override
    public void refreshArmor() {
        Checks.notNull(this.team, "Player is not in a team (game hasn't started yet)");
        if (!isOnline())
            return;

        final var color = this.team.getBiome().getColor();

        final Map<EquipmentSlot, Material> armors = Map.of(
                EquipmentSlot.HEAD, Material.LEATHER_HELMET,
                EquipmentSlot.CHEST, Material.LEATHER_CHESTPLATE,
                EquipmentSlot.LEGS, this.armorLevel.getLeggings(),
                EquipmentSlot.FEET, this.armorLevel.getBoots()
        );

        for (Map.Entry<EquipmentSlot, Material> entry : armors.entrySet()) {
            final var armor = entry.getValue();
            final var slot = entry.getKey();

            final var builder = new ItemBuilder(armor)
                    .cleanLore()
                    .unbreakable()
                    .noMovement();

            if ((slot != EquipmentSlot.LEGS && slot != EquipmentSlot.FEET)
                    || this.armorLevel.equals(ArmorLevel.LEATHER))
                builder.withLeatherArmorColor(color);

            getPlayer().getInventory().setItem(slot, builder.getItem());
        }
    }

    @Override
    public void setArmorLevel(@NotNull ArmorLevel level) {
        Checks.notNull(level, "Armor level cannot be null");
        Checks.notNull(this.team, "Player is not in a team (game hasn't started yet)");

        this.getPlayer().getPersistentDataContainer().set(Keys.ARMOR_LEVEL_KEY, PersistentDataType.INTEGER, level.ordinal());
        this.armorLevel = level;
        refreshArmor();
    }

    @Override
    public @NotNull ArmorLevel getArmorLevel() {
        if (this.armorLevel == null) {
            final var level = this.getPlayer().getPersistentDataContainer().get(Keys.ARMOR_LEVEL_KEY, PersistentDataType.INTEGER);
            if (level == null)
                return ArmorLevel.LEATHER;
            return ArmorLevel.values()[level];
        }

        return this.armorLevel;
    }

    @Override
    public void cleanup() {
        if (!isOnline())
            return;

        final var player = Objects.requireNonNull(this.offlinePlayer.getPlayer());
        player.getInventory().clear();
        player.getPersistentDataContainer().remove(Keys.ARMOR_LEVEL_KEY);

        if (this.updatePlayerTask != null) {
            this.updatePlayerTask.cancel();
            this.updatePlayerTask = null;
        }
    }
}
