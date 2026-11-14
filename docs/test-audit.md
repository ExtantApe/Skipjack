# Test audit

Kept by this repo's owning session, per `~/Code/CLAUDE.md`, "Test self-audits". Newest first.

## 2026-11-14: first version (unit tests only)

What's new: the whole plugin. 46 unit tests, all passing. Nothing has run on a server or in a client. The listener (`HopListener`) only reads the server and applies the answer, so it has no unit test; the rules it calls do.

| Rule | Covered by |
| --- | --- |
| Jump counts only on the press, not while held or on release | `JumpEdgeTest` (press, held, release, press again, per player, forget) |
| Only on water or ice: water, ice, packed ice, blue ice, frosted ice, waterlogged, water plants; not stone, air, lava | `SupportBlocksTest`; `HopTrackerTest.refusedOnDryLandOrInTheAir` |
| Cooldown between hops, per boat, from config; a refusal doesn't start it | `HopTrackerTest.cooldownBlocksAnotherHopEvenAfterLanding`, `cooldownIsPerBoat`, `aShorterConfiguredCooldownIsHonoured`, `aRefusalDoesNotStartTheCooldown` |
| Hop height from config; clamped to safe ranges with a warning; bad types and NaN refused | `SettingsLoaderTest` (low, high, wrong type, NaN and infinity, shipped config) |
| Driver only by default; any passenger on request; permission and world still apply to passengers | `HopTrackerTest.aPassengerCannotHopByDefault`, `aPassengerCanHopWhenAnyPassengerIsOn`, `permissionAndWorldStillApplyToPassengersWhenAnyPassengerIsOn` |
| `disabled-worlds`, case-insensitive; other worlds unaffected | `HopTrackerTest.refusedInADisabledWorld`, `otherWorldsStillWorkWhenOneIsDisabled`; `SettingsLoaderTest.worldNamesMatchWithoutRegardToCase`, `aBadWorldEntryIsIgnoredWithAWarning` |
| Permission `skipjack.hop`, default true | `HopTrackerTest.refusedWithoutPermission`; `PluginYmlTest.hopPermissionIsOnForEveryoneByDefault` |
| Upward velocity only; a faster-rising boat isn't slowed | `HopTrackerTest.aBoatAlreadyRisingFasterKeepsItsOwnSpeed`, `aSinkingBoatGetsTheFullHopVelocityNotAPartialOne`. The decision returns only a vertical value, and the listener copies the old X and Z. |
| No new hop until back on water or ice after landing | `HopTrackerTest.noSecondHopUntilTheBoatHasLandedEvenAfterTheCooldown`, `landingOnAnythingElseDoesNotReleaseAnotherBoat`; `LandingTest` |
| No climbing a stack of ice or a waterfall | `HopTrackerTest.aStackOfIceCannotBeClimbed`, `aRefusedClimbDoesNotStartTheCooldownOrAwaitLanding`, `landingBackDownEndsTheClimbChain`, `aOneBlockRidgeCanStillBeHopped`, `aLongPauseStartsAFreshChainSoARealRiseIsNotBlockedForever` |
| Paper 26.1 target | `PluginYmlTest.targetsPaper261` |

Gaps skipped (and why):

- Boats, chest boats and rafts all being accepted: the listener tests `instanceof Boat`, and every boat and raft type in the API extends it. That needs a live server to prove, so it is a live test to do later.
- Real hop height, landing detection and the water-or-ice block check against real worlds: need a server and a client. None run yet.
- Plugin loading and the console warnings: need a server.
