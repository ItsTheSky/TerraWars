package net.itsthesky.terrawars.core.gui;

import net.itsthesky.terrawars.api.gui.AbstractGUI;
import net.itsthesky.terrawars.api.gui.AbstractPaginationGUI;
import net.itsthesky.terrawars.api.gui.GUI;
import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.itsthesky.terrawars.api.services.IBiomeService;
import net.itsthesky.terrawars.api.services.base.Inject;
import net.itsthesky.terrawars.core.gui.sub.BiomeAbilitySubGui;
import net.itsthesky.terrawars.core.impl.game.Game;
import net.itsthesky.terrawars.core.impl.game.GameWaitingData;
import net.itsthesky.terrawars.util.BukkitUtils;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import net.itsthesky.terrawars.util.Pagination;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class BiomeVotingGui extends AbstractPaginationGUI<IBiome> {

    @Inject private IBiomeService biomeService;

    private final Game game;
    private final GameWaitingData waitingData;

    public BiomeVotingGui(@Nullable AbstractGUI parent, @NotNull Game game,
                          @NotNull GameWaitingData waitingData) {
        super(parent, BukkitUtils.chat().format(
                "<accent><b>→</b> <base>Biome Voting", Colors.PURPLE
        ), 6);

        this.waitingData = waitingData;
        this.game = game;
        this.game.getServiceProvider().inject(this);

        init(new Pagination<>(4*7, biomeService.getAvailableBiomes()));

        setItems(ItemBuilder::fill, e -> e.setCancelled(true), getBorders());
    }

    public BiomeVotingGui(@NotNull Game game, @NotNull GameWaitingData waitingData) {
        this(null, game, waitingData);
    }

    @Override
    public void refresh(@NotNull Player player) {
        setItems(ItemBuilder::air, null, getInnerBorder()); // Clear inside

        createPreviousButton(player, 48);
        createNextButton(player, 50);

        final var biomes = getPlayerPagination(player);
        int slot = 10;

        for (IBiome biome : biomes) {
            if (slot % 9 == 0) slot += 2;
            if (slot % 9 == 8) slot += 2;
            if (slot >= 45) break;

            setItem(slot, () -> {
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
                final var viewer = getInventory().getViewers().isEmpty() ? null : getInventory().getViewers().get(0);
                final var viewerUuid = viewer != null ? viewer.getUniqueId() : null;
                final var hasVoted = viewerUuid != null && waitingData.getPlayerVotes().containsKey(viewerUuid)
                        && waitingData.getPlayerVotes().get(viewerUuid).equals(biome);

                if (hasVoted) {
                    lore.add("<shade-lime:500><b>✔</b> <shade-lime:300>You voted for this biome!");
                } else {
                    lore.add("<shade-yellow:500><b>✔</b> <shade-yellow:300>Click to vote for this biome!");
                }

                lore.add("<shade-blue:500><b>ℹ</b> <shade-blue:300>Right-click to view abilities");

                final var builder = new ItemBuilder(biome.getMainBlock())
                        .name("<accent><b>»</b> <base>" + biome.getName(), biome.getScheme())
                        .lore(biome.getScheme(), lore);

                if (hasVoted)
                    builder.glow();

                return builder.getItem();
            }, e -> {
                e.setCancelled(true);

                if (e.getClick() == ClickType.RIGHT) {
                    final var gui = new BiomeAbilitySubGui(this, biome);
                    gui.open(player);
                    return;
                }

                // Handle vote
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

                refreshInventory();
            });

            slot++;
        }
    }

    @Override
    public @NotNull GUI createCopy(@NotNull Player player) {
        return new BiomeVotingGui(getParent(), game, waitingData);
    }
}
