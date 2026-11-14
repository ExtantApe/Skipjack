package com.extantape.skipjack.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsLoaderTest {
    private static SettingsLoader.Result load(String yaml) throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(yaml);
        return SettingsLoader.load(config);
    }

    @Test
    void noConfigGivesTheDefaults() {
        SettingsLoader.Result r = SettingsLoader.load(null);
        assertEquals(Settings.defaults(), r.settings());
        assertTrue(r.warnings().isEmpty());
    }

    @Test
    void theShippedConfigLoadsToTheDefaultsWithoutWarnings() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        try (var in = SettingsLoaderTest.class.getResourceAsStream("/config.yml")) {
            config.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        SettingsLoader.Result r = SettingsLoader.load(config);
        assertEquals(Settings.defaults(), r.settings());
        assertTrue(r.warnings().isEmpty());
        assertEquals(1200, r.settings().cooldownMillis());
        assertFalse(r.settings().anyPassenger());
    }

    @Test
    void validValuesAreUsed() throws Exception {
        SettingsLoader.Result r = load("cooldown-seconds: 3\nhop-velocity: 0.4\nany-passenger: true\ndisabled-worlds: [Creative, world_nether]");
        assertEquals(3000, r.settings().cooldownMillis());
        assertEquals(0.4, r.settings().hopVelocity());
        assertTrue(r.settings().anyPassenger());
        assertEquals(Set.of("creative", "world_nether"), r.settings().disabledWorlds());
        assertTrue(r.warnings().isEmpty());
    }

    @Test
    void tooLowValuesAreRaisedWithAWarning() throws Exception {
        SettingsLoader.Result r = load("cooldown-seconds: 0\nhop-velocity: -1");
        assertEquals(200, r.settings().cooldownMillis());
        assertEquals(Settings.MIN_HOP_VELOCITY, r.settings().hopVelocity());
        assertEquals(2, r.warnings().size());
    }

    @Test
    void tooHighValuesAreLoweredWithAWarning() throws Exception {
        SettingsLoader.Result r = load("cooldown-seconds: 9999\nhop-velocity: 5");
        assertEquals(10_000, r.settings().cooldownMillis());
        assertEquals(Settings.MAX_HOP_VELOCITY, r.settings().hopVelocity());
        assertEquals(2, r.warnings().size());
    }

    @Test
    void wrongTypesFallBackToDefaultsWithAWarning() throws Exception {
        SettingsLoader.Result r = load("cooldown-seconds: fast\nhop-velocity: [1]\nany-passenger: maybe\ndisabled-worlds: lobby");
        assertEquals(Settings.defaults(), r.settings());
        assertEquals(4, r.warnings().size());
    }

    @Test
    void notANumberAndInfinityAreRefused() throws Exception {
        SettingsLoader.Result r = load("cooldown-seconds: .nan\nhop-velocity: .inf");
        assertEquals(Settings.defaults(), r.settings());
        assertEquals(2, r.warnings().size());
    }

    @Test
    void aBadWorldEntryIsIgnoredWithAWarning() throws Exception {
        SettingsLoader.Result r = load("disabled-worlds: [lobby, 5, '']");
        assertEquals(Set.of("lobby"), r.settings().disabledWorlds());
        assertEquals(2, r.warnings().size());
    }

    @Test
    void worldNamesMatchWithoutRegardToCase() throws Exception {
        Settings s = load("disabled-worlds: [Lobby]").settings();
        assertTrue(s.isWorldDisabled("lobby"));
        assertTrue(s.isWorldDisabled("LOBBY"));
        assertFalse(s.isWorldDisabled("world"));
    }

    @Test
    void hoppingFromLandIsOnByDefaultAndCanBeTurnedOff() throws Exception {
        assertTrue(Settings.defaults().fromLand());
        assertFalse(load("hop-from-land: false").settings().fromLand());
        SettingsLoader.Result bad = load("hop-from-land: maybe");
        assertTrue(bad.settings().fromLand());
        assertFalse(bad.warnings().isEmpty());
    }

    @Test
    void landSpeedIsWaterSpeedByDefaultAndTakesANumberOrVanilla() throws Exception {
        assertEquals(Settings.WATER_SPEED, Settings.defaults().landSpeed());
        assertEquals(Settings.WATER_SPEED, load("land-speed: water").settings().landSpeed());
        assertEquals(5.5, load("land-speed: 5.5").settings().landSpeed());
        assertEquals(0, load("land-speed: vanilla").settings().landSpeed());
        assertEquals(Settings.MAX_LAND_SPEED, load("land-speed: 99").settings().landSpeed());
        assertFalse(load("land-speed: 99").warnings().isEmpty());
        assertEquals(Settings.WATER_SPEED, load("land-speed: zoom").settings().landSpeed());
    }
}
