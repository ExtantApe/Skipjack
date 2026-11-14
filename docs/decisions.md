# Decisions

Dated decisions for this repo, newest last.

- 2026-11-14: created from the ops starter kit, area `minecraft-plugins`.
- 2026-11-14: **Our hop only.** Skipjack adds its own small hop and does not depend on, or work with, Better Boat Movement. The owner chose this over building on that plugin.
- 2026-11-14: **Name: Skipjack (Recommended).** The owner picked Skipjack from the options offered.
- 2026-11-14: Owner's spec for the first version: jump press read from `PlayerInputEvent` (press edge only, vanilla clients, no per-move listener); only from water or ice; cooldown and hop height in `config.yml`, clamped with a warning; boats, chest boats, bamboo rafts and chest rafts; driver only by default with a setting for any passenger; `disabled-worlds`; permission `skipjack.hop`, default true; upward velocity only; no new hop until the boat is back on water or ice.
- 2026-11-14: How the no-climbing rule was read (a choice made while building, for the owner to confirm): landing alone doesn't stop hopping up a stack of ice, because each layer is ice again. So hops in a row are also capped at 1.5 blocks above where the first started (`HopTracker.MAX_CLIMB`), and that count resets after 10 seconds without a hop or on settling back near the start. The two numbers are constants, not config.
