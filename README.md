# Skipjack

Skipjack for Paper: press jump in a boat for a small hop over a block or ice ridge. Vanilla clients, no speed or damage change.

**Status:** built and unit-tested (46 tests of the rules, the config and the plugin file), but not yet run on a server or in a real client. The hop height in particular is a calculated guess, so expect to tune `hop-velocity` once it has been tried.

- Requires Paper 26.1 or newer and Java 25.

## What it does

Press jump while driving a boat and the boat hops. It works in boats, chest boats, bamboo rafts and chest rafts. The hop only adds upward speed. Sideways speed stays exactly as it was, and nothing about damage changes.

A boat can hop only when it is touching water or ice: water (source or flowing), ice, packed ice, blue ice or frosted ice at its base or just under it. Waterlogged blocks and water plants count as water.

A few rules keep it from becoming a way to climb:

- **Cooldown.** 1.2 seconds between hops by default.
- **Landing.** After a hop, the boat can't hop again until it is back on water or ice, even once the cooldown is over.
- **Climb limit.** Hops in a row can't take a boat more than 1.5 blocks above where the first one started, so stacked ice or a waterfall can't be hopped up. The count starts over after 10 seconds without a hop, or when the boat settles back near where it started.

Only the driver (the first passenger) can hop by default. Jump is read from the server's player input event, which a vanilla client sends, so no client mod is needed and there is no per-move listener.

## Settings

In `plugins/Skipjack/config.yml`. Values outside the safe range are moved to the nearest limit, and a warning goes to the console.

| Setting | Default | Allowed | What it does |
| --- | --- | --- | --- |
| `cooldown-seconds` | 1.2 | 0.2 to 10 | Wait between hops |
| `hop-velocity` | 0.32 | 0.1 to 0.6 | Upward speed in blocks per tick. 0.32 should clear about one block |
| `any-passenger` | false | true or false | Let any passenger hop, not only the driver |
| `disabled-worlds` | none | list of world names | Worlds where boats can't hop |

There is no reload command yet. Restart the server after changing the config.

## Permission

`skipjack.hop`, on for everyone by default. Take it away to stop a player hopping.

## Building

You need Java 25.

```
JAVA_HOME=/opt/homebrew/opt/openjdk@25 ./gradlew build
```

The jar is `build/libs/Skipjack-0.1.0.jar`.

## License

Not decided yet. Private repos carry no license.
