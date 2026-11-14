package com.extantape.skipjack;

import com.extantape.skipjack.config.Settings;
import com.extantape.skipjack.config.SettingsLoader;
import com.extantape.skipjack.listen.HopListener;
import org.bukkit.plugin.java.JavaPlugin;

/** Skipjack: press jump in a boat for a small hop. See README.md. */
public final class SkipjackPlugin extends JavaPlugin {
    private volatile Settings settings = Settings.defaults();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        SettingsLoader.Result loaded = SettingsLoader.load(getConfig());
        settings = loaded.settings();
        for (String warning : loaded.warnings()) getLogger().warning(warning);
        HopListener listener = new HopListener(this, () -> settings);
        getServer().getPluginManager().registerEvents(listener, this);
        listener.startLandDriving();
    }
}
