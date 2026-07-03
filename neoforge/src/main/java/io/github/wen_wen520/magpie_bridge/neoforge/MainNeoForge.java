package io.github.wen_wen520.magpie_bridge.neoforge;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import me.shedaniel.autoconfig.AutoConfigClient;

import io.github.wen_wen520.magpie_bridge.Main;
import io.github.wen_wen520.magpie_bridge.MainSettings;

@Mod(Main.MOD_ID)
public final class MainNeoForge {

	public MainNeoForge(ModContainer container) {

		Main.init();

		// Register Settings Entry Point
		container.registerExtensionPoint(
				IConfigScreenFactory.class,
				(IConfigScreenFactory) (modContainer, parent) -> AutoConfigClient.getConfigScreen(MainSettings.class, parent).get()
		);
	}

	public static void LoadResource() {}

}
