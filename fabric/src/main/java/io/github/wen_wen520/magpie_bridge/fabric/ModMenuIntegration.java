package io.github.wen_wen520.magpie_bridge.fabric;

import me.shedaniel.autoconfig.AutoConfigClient;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import io.github.wen_wen520.magpie_bridge.MainSettings;

public class ModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> AutoConfigClient.getConfigScreen(MainSettings.class, parent).get();
	}

}
