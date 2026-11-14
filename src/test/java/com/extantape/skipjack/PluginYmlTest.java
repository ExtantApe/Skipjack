package com.extantape.skipjack;

import com.extantape.skipjack.listen.HopListener;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PluginYmlTest {
    private static YamlConfiguration pluginYml() throws Exception {
        YamlConfiguration yml = new YamlConfiguration();
        yml.options().pathSeparator('/');
        try (var in = PluginYmlTest.class.getResourceAsStream("/paper-plugin.yml")) {
            yml.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return yml;
    }

    @Test
    void hopPermissionIsOnForEveryoneByDefault() throws Exception {
        assertEquals("true", String.valueOf(pluginYml().get("permissions/" + HopListener.PERMISSION + "/default")));
    }

    @Test
    void targetsPaper261() throws Exception {
        assertEquals("26.1", pluginYml().getString("api-version"));
    }
}
