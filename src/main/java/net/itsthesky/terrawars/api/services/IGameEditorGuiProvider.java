package net.itsthesky.terrawars.api.services;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import net.itsthesky.terrawars.core.config.GameConfig;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IGameEditorGuiProvider {

    @NotNull ChestGui createGameEditorGui(@NotNull GameConfig config);

}
