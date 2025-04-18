package net.itsthesky.terrawars.core.impl.game;

import lombok.Getter;
import lombok.Setter;
import net.itsthesky.terrawars.api.model.ability.AbilityType;
import net.itsthesky.terrawars.api.model.ability.ActiveAbility;
import net.itsthesky.terrawars.api.model.ability.IAbility;
import net.itsthesky.terrawars.api.model.ability.PassiveAbility;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.model.shop.ArmorLevel;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.core.gui.BiomeVotingGui;
import net.itsthesky.terrawars.core.gui.TeamSelectionGui;
import net.itsthesky.terrawars.util.*;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class GamePlayer implements IGamePlayer {

    private final OfflinePlayer offlinePlayer;
    private final Game game;
    private final GamePlayerListener listener;
    private IGameTeam team;

    private @Nullable ArmorLevel armorLevel;
    private GamePlayerState state;

    private boolean isRespawning = false;
    private @Nullable IAbility lastSelectedAbility;
    private @Nullable IAbility selectedAbility;
    private BukkitTask updatePlayerTask;
    private BukkitTask respawnTask;

    public GamePlayer(OfflinePlayer player, Game game) {
        this.offlinePlayer = player;
        this.game = game;

        this.state = GamePlayerState.WAITING;

        this.team = null;
        this.selectedAbility = null;
        this.updatePlayerTask = null;

        BukkitUtils.registerListener(listener = new GamePlayerListener());
    }


    @Override
    public @NotNull IGameTeam getTeam() throws IllegalStateException {
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
            this.selectedAbility.removeCooldown(this);

            this.lastSelectedAbility = this.selectedAbility;
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

        if (lastSelectedAbility == null) {
            final var availableAbilities = this.team.getBiome().getAvailableAbilities();
            if (!availableAbilities.isEmpty()) {
                final var firstAbility = availableAbilities.get(0);
                setSelectedAbility(firstAbility);
            }
        } else {
            setSelectedAbility(lastSelectedAbility);
        }

        setupHotbar(true);
        if (this.armorLevel == null)
            setArmorLevel(ArmorLevel.LEATHER);
        else
            refreshArmor();

        if (this.updatePlayerTask == null)
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
            if (!isOnline() || !isInGame())
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
                        .setCustomData(Keys.WEAPON_KEY, PersistentDataType.BOOLEAN, true)
                        .lore(Colors.ORANGE, "<text>Default melee weapon")
                        .getItem());
            } else if (foundMeleeWeapons.size() > 1 &&
                    foundMeleeWeapons.contains(Material.WOODEN_SWORD)) {
                player.getInventory().remove(Material.WOODEN_SWORD);
            }

            // Check if below Y is below 115
            if (player.getLocation().getY() < 115) {
                player.setHealth(0);
            }
        }, 0, 20);
    }

    public void waitingSetup() {
        if (!isOnline())
            return;

        final var player = Objects.requireNonNull(this.offlinePlayer.getPlayer());
        player.teleport(game.getLobbyLocation());
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.setLevel(0);
        player.setExp(0);

        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(0);

        player.getInventory().setItem(3, new ItemBuilder(Material.OAK_SAPLING)
                .name("<accent>⋆ <text>Vote for <base>Biomes <accent>⋆", Colors.AMBER)
                .noMovement()
                .addInteraction("vote-biome", event -> {
                    if (!event.getAction().isRightClick())
                        return;

                    event.setCancelled(true);
                    final var gui = new BiomeVotingGui(game, game.getWaitingData());
                    gui.open(event.getPlayer());
                })
                .getItem());

        player.getInventory().setItem(5, new ItemBuilder(Material.CYAN_BANNER)
                .name("<accent>⋆ <text>Select your <base>Team <accent>⋆", Colors.YELLOW)
                .noMovement()
                .addInteraction("select-team", event -> {
                    if (!event.getAction().isRightClick())
                        return;

                    event.setCancelled(true);
                    final var gui = new TeamSelectionGui(game, game.getWaitingData());
                    gui.show(event.getPlayer());
                })
                .getItem());
    }

    @Override
    public void refreshArmor() {
        Checks.notNull(this.team, "Player is not in a team (game hasn't started yet)");
        if (!isOnline() || !isInGame())
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
    public boolean isInGame() {
        return this.state == GamePlayerState.TEAM;
    }

    @Override
    public void cleanup() {
        if (!isOnline())
            return;

        final var player = Objects.requireNonNull(this.offlinePlayer.getPlayer());

        player.getInventory().clear();
        player.getEnderChest().clear();
        player.getPersistentDataContainer().remove(Keys.ARMOR_LEVEL_KEY);

        if (this.updatePlayerTask != null) {
            this.updatePlayerTask.cancel();
            this.updatePlayerTask = null;
        }

        if (respawnTask != null) {
            respawnTask.cancel();
            respawnTask = null;
        }

        if (this.listener != null)
            BukkitUtils.unregisterListener(this.listener);
    }

    public class GamePlayerListener implements Listener {

        @EventHandler
        public void onItemRightClick(@NotNull PlayerInteractEvent event) {
            if (event.getPlayer() != offlinePlayer)
                return;
            if (!event.getAction().isRightClick())
                return;
            if (isRespawning)
                return;

            final var item = event.getItem();
            if (item == null || selectedAbility == null || !selectedAbility.getType().equals(AbilityType.ACTIVE))
                return;

            final var ability = (ActiveAbility) selectedAbility;

            if (item.getPersistentDataContainer().getOrDefault(Keys.WEAPON_KEY, PersistentDataType.BOOLEAN, false)) {
                if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock() != null)
                    if (event.getClickedBlock().getType().equals(Material.CHEST) ||
                            event.getClickedBlock().getType().equals(Material.ENDER_CHEST))
                        return;

                ability.use(GamePlayer.this, game);
                event.setCancelled(true);
            }
        }


        @EventHandler
        public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
            if (event.getEntity() != offlinePlayer || !isInGame() || isRespawning)
                return;

            event.setCancelled(true);
            getPlayer().setHealth(20);

            final var player = event.getPlayer();
            isRespawning = true;

            event.setKeepInventory(true);
            player.setGameMode(GameMode.SPECTATOR);
            player.setAllowFlight(true);
            player.setFlying(true);

            player.teleport(game.getLobbyLocation());
            player.getInventory().clear();

            game.broadcastMessage(IChatService.MessageSeverity.ERROR,
                    "<accent>" + player.getName() + "<text> has died!");

            final var countdown = new AtomicInteger(game.getConfig().getDeathCooldown());
            respawnTask = BukkitUtils.runTaskTimer(() -> {
                final var remaining = countdown.getAndDecrement();
                if (remaining <= 0) {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.getInventory().clear();
                    player.teleport(team.getSpawnLocation());
                    isRespawning = false;
                    setup();

                    respawnTask.cancel();
                    respawnTask = null;
                } else {
                    game.getChatService().sendTitle(new IChatService.TitleBuilder()
                            .audience(player)
                            .title("<shade-red:600><b>You died!")
                            .subtitle("<text>Respawning in <accent>" + remaining + "s")
                            .scheme(Colors.ORANGE)
                            .time(0, 1100, 0));
                }
            }, 5, 20);
        }

        @EventHandler
        public void onSaturationChange(@NotNull FoodLevelChangeEvent event) {
            if (event.getEntity() != offlinePlayer)
                return;

            event.setFoodLevel(20);
        }

        // Prevent Events
        @EventHandler
        public void onTryCraft(@NotNull CraftItemEvent event) {
            if (event.getViewers().stream().anyMatch(v -> v.getUniqueId().equals(offlinePlayer.getUniqueId()))
                    && isOnline())
                event.setCancelled(true);
        }

        @EventHandler
        public void onTryEquipArmor(@NotNull PlayerInteractEvent event) {
            if (event.getPlayer() != offlinePlayer)
                return;
            if (!event.getAction().isRightClick())
                return;
            final var heldItem = event.getItem();
            if (heldItem == null || !heldItem.getType().name().toLowerCase().contains("armor"))
                return;

            if (event.getHand() == EquipmentSlot.HAND)
                event.setCancelled(true);
        }
    }
}
