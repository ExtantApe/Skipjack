package com.extantape.skipjack.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Reads config.yml into {@link Settings}. Bad or out-of-range values are replaced and reported, never guessed at. */
public final class SettingsLoader {
    public record Result(Settings settings, List<String> warnings) {
    }

    private SettingsLoader() {
    }

    /** A null section (no config at all) gives the defaults. */
    public static Result load(ConfigurationSection config) {
        List<String> warnings = new ArrayList<>();
        if (config == null) return new Result(Settings.defaults(), warnings);

        double cooldown = number(config, "cooldown-seconds", Settings.DEFAULT_COOLDOWN_SECONDS,
                Settings.MIN_COOLDOWN_SECONDS, Settings.MAX_COOLDOWN_SECONDS, warnings);
        double velocity = number(config, "hop-velocity", Settings.DEFAULT_HOP_VELOCITY,
                Settings.MIN_HOP_VELOCITY, Settings.MAX_HOP_VELOCITY, warnings);

        boolean any = false;
        if (config.contains("any-passenger")) {
            if (config.isBoolean("any-passenger")) {
                any = config.getBoolean("any-passenger");
            } else {
                warnings.add("any-passenger must be true or false, so it stays false (driver only).");
            }
        }

        Set<String> worlds = new HashSet<>();
        if (config.contains("disabled-worlds")) {
            if (config.isList("disabled-worlds")) {
                for (Object entry : config.getList("disabled-worlds", List.of())) {
                    if (entry instanceof String name && !name.isBlank()) {
                        worlds.add(name.trim().toLowerCase(Locale.ROOT));
                    } else {
                        warnings.add("disabled-worlds has an entry that is not a world name (" + entry + "), so it is ignored.");
                    }
                }
            } else {
                warnings.add("disabled-worlds must be a list of world names, so no world is disabled.");
            }
        }

        boolean land = true;
        if (config.contains("hop-from-land")) {
            if (config.isBoolean("hop-from-land")) land = config.getBoolean("hop-from-land");
            else warnings.add("hop-from-land must be true or false, so it stays true.");
        }
        Settings settings = new Settings(Math.round(cooldown * 1000), velocity, any, worlds, land);
        return new Result(settings, warnings);
    }

    private static double number(ConfigurationSection config, String key, double fallback, double min, double max,
                                 List<String> warnings) {
        if (!config.contains(key)) return fallback;
        Object raw = config.get(key);
        if (!(raw instanceof Number n) || Double.isNaN(n.doubleValue()) || Double.isInfinite(n.doubleValue())) {
            warnings.add(key + " must be a number, so it uses the default " + fallback + ".");
            return fallback;
        }
        double value = n.doubleValue();
        if (value < min) {
            warnings.add(key + " " + value + " is below " + min + ", so it uses " + min + ".");
            return min;
        }
        if (value > max) {
            warnings.add(key + " " + value + " is above " + max + ", so it uses " + max + ".");
            return max;
        }
        return value;
    }
}
