package net.mehvahdjukaar.mysticaloaktree.integration.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.mehvahdjukaar.mysticaloaktree.ClientConfigs;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ClientConfigs.CONFIG_SPEC::makeScreen;
    }
}