package net.itsthesky.terrawars.core.services;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import net.itsthesky.terrawars.api.services.ISchemService;
import net.itsthesky.terrawars.api.services.base.Service;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class SchemService implements ISchemService {

    @Override
    public void pasteSchematic(@NotNull String name, @NotNull Location center, boolean ignoreAir) {
        // Chemin du fichier schematic dans le dossier plugins/FastAsyncWorldEdit/schematics
        File fichierSchematic = new File("plugins/FastAsyncWorldEdit/schematics", name);

        // Vérifier si le fichier existe
        if (!fichierSchematic.exists()) {
            System.out.println("Le schematic " + name + " n'existe pas!");
            return;
        }

        // Convertir la location Bukkit en BlockVector3 de WorldEdit
        BlockVector3 position = BlockVector3.at(center.getX(), center.getY(), center.getZ());

        // Adapter le monde Bukkit pour WorldEdit
        World weWorld = BukkitAdapter.adapt(center.getWorld());

        try {
            // Détecter le format du schematic automatiquement
            ClipboardFormat format = ClipboardFormats.findByFile(fichierSchematic);

            if (format == null)
                throw new IOException("Format de fichier non pris en charge ou non détecté.");

            Clipboard clipboard;

            // Lire le schematic
            try (ClipboardReader reader = format.getReader(new FileInputStream(fichierSchematic))) {
                clipboard = reader.read();
            }

            // Créer une session d'édition
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                // Préparer l'opération de collage
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(position)
                        .ignoreAirBlocks(ignoreAir)
                        .copyEntities(true)
                        .build();

                // Exécuter l'opération
                Operations.complete(operation);
                // Vider la file d'attente pour appliquer les changements
                editSession.flushQueue();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement ou du collage du schematic: " + e.getMessage());
        }
    }
}
