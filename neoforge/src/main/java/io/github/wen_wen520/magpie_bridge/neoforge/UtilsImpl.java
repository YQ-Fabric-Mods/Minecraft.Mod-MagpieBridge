package io.github.wen_wen520.magpie_bridge.neoforge;

import java.nio.file.Path;
import net.neoforged.fml.loading.FMLPaths;

public final class UtilsImpl {
	public static Path getGameDir() {
		return FMLPaths.GAMEDIR.get();
	}

	public static Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}
}
