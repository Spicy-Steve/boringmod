package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public class PlayerDataStorage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final File playerDir;
    protected final DataFixer fixerUpper;

    public PlayerDataStorage(LevelStorageSource.LevelStorageAccess p_78430_, DataFixer p_78431_) {
        this.fixerUpper = p_78431_;
        this.playerDir = p_78430_.getLevelPath(LevelResource.PLAYER_DATA_DIR).toFile();
        this.playerDir.mkdirs();
    }

    public void save(Player p_78434_) {
        try {
            CompoundTag compoundtag = p_78434_.saveWithoutId(new CompoundTag());
            Path path = this.playerDir.toPath();
            Path path1 = Files.createTempFile(path, p_78434_.getStringUUID() + "-", ".dat");
            NbtIo.writeCompressed(compoundtag, path1);
            Path path2 = path.resolve(p_78434_.getStringUUID() + ".dat");
            Path path3 = path.resolve(p_78434_.getStringUUID() + ".dat_old");
            Util.safeReplaceFile(path2, path1, path3);
            net.neoforged.neoforge.event.EventHooks.firePlayerSavingEvent(p_78434_, playerDir, p_78434_.getStringUUID());
        } catch (Exception exception) {
            LOGGER.warn("Failed to save player data for {}", p_78434_.getName().getString());
        }
    }

    @Nullable
    public CompoundTag load(Player p_78436_) {
        CompoundTag compoundtag = null;

        try {
            File file1 = new File(this.playerDir, p_78436_.getStringUUID() + ".dat");
            if (file1.exists() && file1.isFile()) {
                compoundtag = NbtIo.readCompressed(file1.toPath(), NbtAccounter.unlimitedHeap());
            }
        } catch (Exception exception) {
            LOGGER.warn("Failed to load player data for {}", p_78436_.getName().getString());
        }

        if (compoundtag != null) {
            int i = NbtUtils.getDataVersion(compoundtag, -1);
            compoundtag = DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, compoundtag, i);
            p_78436_.load(compoundtag);
        }
        net.neoforged.neoforge.event.EventHooks.firePlayerLoadingEvent(p_78436_, playerDir, p_78436_.getStringUUID());

        return compoundtag;
    }

    public String[] getSeenPlayers() {
        String[] astring = this.playerDir.list();
        if (astring == null) {
            astring = new String[0];
        }

        for(int i = 0; i < astring.length; ++i) {
            if (astring[i].endsWith(".dat")) {
                astring[i] = astring[i].substring(0, astring[i].length() - 4);
            }
        }

        return astring;
    }

    public File getPlayerDataFolder() {
        return playerDir;
    }
}
