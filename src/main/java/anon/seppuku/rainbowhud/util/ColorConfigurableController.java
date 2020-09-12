package me.anon.seppuku.rainbowhud.util;

import java.util.List;
import java.util.Iterator;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.impl.management.ModuleManager;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.impl.config.ColorConfig;
import me.rigamortis.seppuku.impl.management.ConfigManager;

public final class ColorConfigurableController {
    private static boolean firstRun = true;

    private ColorConfigurableController() {} // "static"

    public static void disableColorConfigurable() {
        // Get configurables
        ConfigManager cfgMan = Seppuku.INSTANCE.getConfigManager();
        List<Configurable> configurables = cfgMan.getConfigurableList();
        Iterator<Configurable> cit = configurables.iterator();

        // Disable color configurable and try saving
        boolean saved = false;
        while(cit.hasNext()) {
            Configurable cfg = cit.next();
            if(cfg instanceof ColorConfig) {
                if(!saved) {
                    saved = true;
                    cfg.save();
                }

                cit.remove();
            }
        }

        cfgMan.setConfigurableList(configurables);

        // Load all configs as this breaks the loadAll thread on the first run
        if(firstRun) {
            cfgMan.loadAll();
            firstRun = false;
        }
    }

    public static void enableColorConfigurable() {
        // Get modules
        List<Module> modules = Seppuku.INSTANCE.getModuleManager().getModuleList();
        Iterator<Module> it = modules.iterator();

        // Reset each module's colors
        while(it.hasNext()) {
            it.next().setColor(-1);
        }

        // Get configurables
        ConfigManager cfgMan = Seppuku.INSTANCE.getConfigManager();
        List<Configurable> configurables = cfgMan.getConfigurableList();
        Iterator<Configurable> cit = configurables.iterator();

        // Check that the color configurable is not enabled
        while(cit.hasNext()) {
            Configurable cfg = cit.next();
            if(cfg instanceof ColorConfig) {
                Seppuku.INSTANCE.errorChat("RainbowHud: Color configurable already added. Color configuration possibly corrupted");
                return;
            }
        }

        // Re-enable color configurable and load colors
        ColorConfig colorCfg = new ColorConfig();
        colorCfg.load();
        configurables.add(colorCfg);
        cfgMan.setConfigurableList(configurables);
    }
}
