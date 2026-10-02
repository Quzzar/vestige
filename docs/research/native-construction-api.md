# Native construction and companion storage APIs

Inspected 2026-10-01 against Minecraft **1.21.1** and NeoForge **21.1.72**, as pinned in [gradle.properties](../../gradle.properties). This is source research for implementations behind `SpellWorld.manifest`; it does not add runtime behavior or establish gameplay verification.

The workable seam is one owned `ManifestationHandle` per construction, illusion, or stored companion. That handle owns positions/entity UUIDs, original state, associated visual entities, and an idempotent close operation. Keep these mechanics in world adapters rather than spell-specific runtime branches. The existing runtime already closes the backing handle before running end effects, and suppresses end effects for server stop, unavailable owner, or removed backing. [SpellWorld](../../src/main/java/com/quzzar/vestige/magic/runtime/SpellWorld.java), [SpellRuntime](../../src/main/java/com/quzzar/vestige/magic/runtime/SpellRuntime.java:345)

## Primary source identity

The inspected patched source archive is [sourcesWithNeoForge_0b61ac7a4c2b6df40ed4d1659d0cb88716834c25_output.zip](/Users/quzzar/.gradle/caches/neoformruntime/intermediate_results/sourcesWithNeoForge_0b61ac7a4c2b6df40ed4d1659d0cb88716834c25_output.zip), SHA-256 `490b2ea70a0c8a9d05d4db006c6a3af1b793c2cff3b4ec7e0dab0d75939b55ae`. Its inspected NeoForge event/hooks/snapshot members byte-match the [pinned 21.1.72 source JAR](/Users/quzzar/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/21.1.72/a518151fcf88e5bda178895a6fba9493999686b2/neoforge-21.1.72-sources.jar).

Clickable method citations below point to temporary extracted copies of those primary archive members. If temporary files are cleared, the archive member names and line numbers still identify the evidence. No source files were copied into the mod.

## Construction, permissions, and restoration

These public calls are available:

```java
BlockSnapshot before = BlockSnapshot.create(level.dimension(), level, pos, flags);
boolean changed = level.setBlock(pos, authoredState, flags);
boolean restored = before.restore(flags);
// Explicit destination variant:
boolean restoredThere = before.restoreToLocation(level, pos, flags);
```

`BlockSnapshot.create` captures block state and block-entity NBT. Restoration reloads that NBT and marks the block entity changed. Prefer rejecting block entities for temporary construction/passages: restoring their NBT is possible, but suppressing drops does not prevent every block-specific removal side effect or external automation interaction. A snapshot does not capture arbitrary scheduled work or neighboring state. [BlockSnapshot.java:60](/private/tmp/vestige-native-api/net/neoforged/neoforge/common/util/BlockSnapshot.java:60), [BlockSnapshot.java:153](/private/tmp/vestige-native-api/net/neoforged/neoforge/common/util/BlockSnapshot.java:153), [Level.java:233](/private/tmp/vestige-native-api/net/minecraft/world/level/Level.java:233)

`Level.setBlock` does **not** automatically emit player placement/break protection events. For a player-authored world change, check `level.mayInteract(player, pos)`; on ServerLevel this covers spawn protection and world border. For removing an existing block, the standard public permission path is:

```java
var permission = CommonHooks.fireBlockBreak(
    level, player.gameMode.getGameModeForPlayer(), player, pos, oldState);
if (permission.isCanceled()) return false;
```

This hook checks the standard tool, adventure-mode, and game-master-block restrictions and posts `BlockEvent.BreakEvent`. Native spell outcomes still need their own eligibility rules, including eligible material, loaded chunks, build height, ownership conflicts, and occupancy. [CommonHooks.java:565](/private/tmp/vestige-native-api/net/neoforged/neoforge/common/CommonHooks.java:565), [ServerPlayerGameMode.java:250](/private/tmp/vestige-native-api/net/minecraft/server/level/ServerPlayerGameMode.java:250), [ServerLevel.java:806](/private/tmp/vestige-native-api/net/minecraft/server/level/ServerLevel.java:806)

The public placement hooks are `EventHooks.onBlockPlace(caster, before, face)` and `EventHooks.onMultiBlockPlace(caster, snapshots, face)`; **true means canceled**. They inspect the current placed state and the original snapshot. The standard item-placement transaction temporarily captures snapshots, defers notifications, posts the event, and reverses snapshots when canceled. Spell construction should offer a similarly bounded transaction rather than writing blocks and hoping protection listeners run. Do not leave the shared `captureBlockSnapshots` or `restoringBlockSnapshots` flags altered; restore prior values in `finally`. NPC placement needs an explicit policy because event state selection differs for non-player placers. [EventHooks.java:186](/private/tmp/vestige-native-api/net/neoforged/neoforge/event/EventHooks.java:186), [BlockEvent.java:104](/private/tmp/vestige-native-api/net/neoforged/neoforge/event/level/BlockEvent.java:104), [CommonHooks.java:594](/private/tmp/vestige-native-api/net/neoforged/neoforge/common/CommonHooks.java:594)

Use bounded footprints. Before placing solid geometry, check `level.hasChunkAt(pos)`, `level.isOutsideBuildHeight(pos)`, border/permission, and `level.isUnobstructed(state, pos, CollisionContext.empty())`. For a moved creature's destination, derive its translated bounding box and use `level.noCollision(creature, proposedBox)`; that checks block/entity collisions and border collision. Safe passage closure additionally needs an explicit escape-position search before restoring stone around occupants. [LevelReader.java:192](/private/tmp/vestige-native-api/net/minecraft/world/level/LevelReader.java:192), [Level.java:233](/private/tmp/vestige-native-api/net/minecraft/world/level/Level.java:233), [CollisionGetter.java:29](/private/tmp/vestige-native-api/net/minecraft/world/level/CollisionGetter.java:29)

Store an ownership record keyed by dimension and immutable position, with original snapshot, authored state, owning manifestation, and destruction status. Restore only positions whose current state still matches the owned authored state or an explicitly recorded destructive removal. Overlapping casts need an explicit rejection or stack policy. State comparison alone cannot detect another actor replacing a block with the identical state, so intercept ordinary placement/modification paths and document unsupported direct mutations by other mods.

## Harvesting and explosions

`BlockEvent.BreakEvent` can prevent ordinary player harvesting. `BlockDropsEvent` can suppress all items, XP, and `spawnAfterBreak` on the standard block-drop path:

```java
@SubscribeEvent
public static void drops(BlockDropsEvent event) {
    if (ownedConjuredPosition(event.getLevel(), event.getPos()))
        event.setCanceled(true);
}
```

`ownedConjuredPosition` is an application ownership lookup, not a Minecraft API. Keep the position record alive until its drop event has completed: the block may already be absent when this event fires. If breakable conjured walls are desired, allow the break, mark the owned position destroyed, suppress drops, and reconcile/removal on the server tick. [BlockEvent.java:69](/private/tmp/vestige-native-api/net/neoforged/neoforge/event/level/BlockEvent.java:69), [BlockDropsEvent.java:23](/private/tmp/vestige-native-api/net/neoforged/neoforge/event/level/BlockDropsEvent.java:23), [CommonHooks.java:537](/private/tmp/vestige-native-api/net/neoforged/neoforge/common/CommonHooks.java:537)

**Explosion loot bypasses BlockDropsEvent in this version.** `BlockBehaviour.onExplosionHit` directly invokes `spawnAfterBreak` and `getDrops`, then the explosion creates those items. Canceling ordinary block drops therefore does not make temporary construction non-farmable. Remove owned positions from the mutable `ExplosionEvent.Detonate.getAffectedBlocks()` list, then apply any intended wall damage/destruction through the manifestation's own no-loot path. Detonate is not cancellable; Start is cancellable but cancels the entire explosion. [BlockBehaviour.java:199](/private/tmp/vestige-native-api/net/minecraft/world/level/block/state/BlockBehaviour.java:199), [Explosion.java:350](/private/tmp/vestige-native-api/net/minecraft/world/level/Explosion.java:350), [ExplosionEvent.java:65](/private/tmp/vestige-native-api/net/neoforged/neoforge/event/level/ExplosionEvent.java:65)

Pistons, fire, fluid replacement, tools, and another mod's direct `setBlock` can change geometry independently of player harvesting. `PistonEvent.Pre` is cancellable and exposes a `PistonStructureResolver` through `getStructureHelper()`. `BlockToolModificationEvent`, `FluidPlaceBlockEvent`, and `LivingDestroyBlockEvent` cover specific other paths, not every world mutation. A registered non-dropping, immovable backing block provides stronger guarantees than borrowing an ordinary block; visual BlockDisplay entities can supply material appearance independently. A real water-source wall introduces fluid spread and scheduled-tick ownership problems, so that requires a deliberate custom-medium implementation. [PistonEvent.java:57](/private/tmp/vestige-native-api/net/neoforged/neoforge/event/level/PistonEvent.java:57), [BlockEvent.java:205](/private/tmp/vestige-native-api/net/neoforged/neoforge/event/level/BlockEvent.java:205), [EventHooks.java:720](/private/tmp/vestige-native-api/net/neoforged/neoforge/event/EventHooks.java:720)

## BlockDisplay, TextDisplay, and Interaction

Create vanilla entity types without additional registration:

```java
Display.BlockDisplay block = EntityType.BLOCK_DISPLAY.create(level);
Display.TextDisplay label = EntityType.TEXT_DISPLAY.create(level);
Interaction hitbox = EntityType.INTERACTION.create(level);
```

All factories can return null. Use `level.addFreshEntity(entity)` and check its boolean result. These entities do not supply solid terrain collision: Interaction is pickable but cannot be hit by projectiles and has `noPhysics = true`. It is useful for a viewer's click target, not for a wall that physically stops mobs. [EntityType.java:1099](/private/tmp/vestige-native-api/net/minecraft/world/entity/EntityType.java:1099), [ServerLevel.java:893](/private/tmp/vestige-native-api/net/minecraft/server/level/ServerLevel.java:893), [Interaction.java:43](/private/tmp/vestige-native-api/net/minecraft/world/entity/Interaction.java:43), [Interaction.java:113](/private/tmp/vestige-native-api/net/minecraft/world/entity/Interaction.java:113)

In 1.21.1, display transformation setters, BlockDisplay's block setter, TextDisplay's text setter, and Interaction's dimensions/response setters are **private**. Public `Entity.load(CompoundTag)` reads those properties. Preserve the entity's existing serialization, modify the required tags, and load it before spawning:

```java
CompoundTag data = block.saveWithoutId(new CompoundTag());
data.put("block_state", NbtUtils.writeBlockState(authoredState));
data.putInt("interpolation_duration", 4);
data.putInt("start_interpolation", 0);
data.putInt("teleport_duration", 4);
block.load(data);
block.moveTo(x, y, z, yaw, pitch);
boolean added = level.addFreshEntity(block);
```

For scaled/transformed display geometry, encode `Transformation.EXTENDED_CODEC` through `NbtOps.INSTANCE` into the `transformation` tag. The public Transformation constructor accepts translation, left rotation, scale, and right rotation. `interpolation_duration` controls transform interpolation; `teleport_duration` controls position/rotation interpolation and is clamped to 0–59. [Display.java:209](/private/tmp/vestige-native-api/net/minecraft/world/entity/Display.java:209), [Display.java:581](/private/tmp/vestige-native-api/net/minecraft/world/entity/Display.java:581), [Transformation.java:27](/private/tmp/vestige-native-api/com/mojang/math/Transformation.java:27)

For TextDisplay, put the result of `Component.Serializer.toJson(component, level.registryAccess())` in string tag `text`; other useful tags are `line_width`, `background`, `text_opacity`, `billboard`, `shadow`, and `see_through`. For Interaction, use float tags `width` and `height` and boolean `response`. Its public `getTarget()` reports the last interacting player and `getLastAttacker()` the last attacking player; timestamps are recorded in serialized `interaction`/`attack` data. All visual entities must be owned and discarded by their handle. [Display.java:897](/private/tmp/vestige-native-api/net/minecraft/world/entity/Display.java:897), [Interaction.java:60](/private/tmp/vestige-native-api/net/minecraft/world/entity/Interaction.java:60)

For actual viewer clicks, subscribe to `PlayerInteractEvent.EntityInteractSpecific`/`EntityInteract` and `AttackEntityEvent`, each exposing `getTarget()`. Right-click events expose `setCancellationResult(InteractionResult.SUCCESS)` alongside cancellation. Process a recognized viewer UUID once on the server, with a defined hand policy; consuming the specific interaction prevents a second general interaction. These listeners avoid treating the Interaction entity's remembered last player as a new click every tick. [PlayerInteractEvent.java:60](/private/tmp/vestige-native-api/net/neoforged/neoforge/event/entity/player/PlayerInteractEvent.java:60), [PlayerInteractEvent.java:106](/private/tmp/vestige-native-api/net/neoforged/neoforge/event/entity/player/PlayerInteractEvent.java:106), [AttackEntityEvent.java:27](/private/tmp/vestige-native-api/net/neoforged/neoforge/event/entity/player/AttackEntityEvent.java:27)

## Pet Cache: preserve the actual owned companion

Check a live `Mob` with an `OwnableEntity` whose `getOwnerUUID()` equals the caster's UUID. Comparing UUIDs works across dimensions; `TamableAnimal.isOwnedBy` resolves a live owner and can fail in the cache's dimension. Reject passengers, vehicles, and leashed animals unless their complete transfer behavior is deliberately supported: dimension changes unride entities, transfer passengers recursively, and drop the original leash relationship. [OwnableEntity.java:7](/private/tmp/vestige-native-api/net/minecraft/world/entity/OwnableEntity.java:7), [TamableAnimal.java:194](/private/tmp/vestige-native-api/net/minecraft/world/entity/TamableAnimal.java:194), [Entity.java:2719](/private/tmp/vestige-native-api/net/minecraft/world/entity/Entity.java:2719)

Capture a recovery record before travel: pet UUID/type, owner UUID, full `saveWithoutId` NBT, original dimension/position, cache position, and flags that will be changed. Then:

```java
UUID petId = pet.getUUID();
Entity moved = pet.changeDimension(new DimensionTransition(
    cacheLevel, safeCachePosition, Vec3.ZERO,
    pet.getYRot(), pet.getXRot(), DimensionTransition.DO_NOTHING));
boolean arrived = moved instanceof Mob
    && cacheLevel.getEntity(petId) == moved;
```

For a non-player crossing dimensions, this returns a **new Java entity instance**, copies its serialized state, and removes the old instance. Retain the returned instance or resolve by UUID afterward. `saveWithoutId` includes UUID, NeoForge persistent data/attachments, and each entity's additional serialized state. TamableAnimal serializes ownership; horse subclasses serialize saddle/chest inventory. Do not construct a replacement summon and pretend it is the stored pet. [Entity.java:1737](/private/tmp/vestige-native-api/net/minecraft/world/entity/Entity.java:1737), [Entity.java:2710](/private/tmp/vestige-native-api/net/minecraft/world/entity/Entity.java:2710), [AbstractHorse.java:849](/private/tmp/vestige-native-api/net/minecraft/world/entity/animal/horse/AbstractHorse.java:849), [AbstractChestedHorse.java:102](/private/tmp/vestige-native-api/net/minecraft/world/entity/animal/horse/AbstractChestedHorse.java:102)

Travel can return null when `EntityTravelToDimensionEvent` cancels. A second failure is subtler: destination insertion can be canceled by `EntityJoinLevelEvent` **after the original has been removed**, yet `changeDimension` still returns the clone because `addDuringTeleport` returns void. Keep the recovery record until `cacheLevel.getEntity(petId) == moved` confirms insertion; preserve pending return/recovery when that fails. The public cross-dimension `teleportTo(ServerLevel, ...)` also clones non-players and supplies only a boolean, so it is less convenient for ownership tracking. [Entity.java:2719](/private/tmp/vestige-native-api/net/minecraft/world/entity/Entity.java:2719), [ServerLevel.java:904](/private/tmp/vestige-native-api/net/minecraft/server/level/ServerLevel.java:904), [PersistentEntitySectionManager.java:79](/private/tmp/vestige-native-api/net/minecraft/world/level/entity/PersistentEntitySectionManager.java:79)

While stored, a bounded safe room plus `setNoAi(true)`, stopped navigation, zero motion, and suitable protection can prevent normal companion activity. Save original flags and restore them on return. NoAI is not a complete stasis guarantee: environment damage and independent mod callbacks require consideration. Resolve a safe non-colliding return location, load the required chunks, travel the same UUID-bearing pet back, verify arrival, then clear the recovery record. Owned chunk tickets are available through `addRegionTicket(type, chunk, distance, key)`/`removeRegionTicket(...)`; release them when the handle closes. [Mob.java:1427](/private/tmp/vestige-native-api/net/minecraft/world/entity/Mob.java:1427), [ServerChunkCache.java:445](/private/tmp/vestige-native-api/net/minecraft/server/level/ServerChunkCache.java:445)

## Close, reload, stop, and recovery

Use idempotent `ManifestationHandle.close` for restoration, visual removal, and pet return, including dispel and data reload. **Do this on ServerStoppingEvent, before final saves**, rather than relying on a ServerStoppedEvent handler: Minecraft's stop path saves players/worlds and closes levels before ServerStoppedEvent. The normal stopping event runs before that shutdown path. [MinecraftServer.java:592](/private/tmp/vestige-native-api/net/minecraft/server/MinecraftServer.java:592), [MinecraftServer.java:730](/private/tmp/vestige-native-api/net/minecraft/server/MinecraftServer.java:730), [ServerLifecycleHooks.java:117](/private/tmp/vestige-native-api/net/neoforged/neoforge/server/ServerLifecycleHooks.java:117)

An abnormal crash can skip the normal stopping event. A minimal persistent recovery ledger or pet return marker is therefore necessary if restart recovery is promised; this is cleanup metadata, not serialization of ordinary active spells. On startup/entity load, recover recorded owned construction/pets and reconcile UUID identity before deleting ledger entries. Recover world state before exposing it as ordinary gameplay. This is an implementation requirement inferred from the shutdown and entity insertion paths, not an existing behavior claim.

Implementation verification should exercise canceled placement, harvesting and explosion loot suppression, passage closure around occupants, close/reload restoration, and same-UUID pet round trips with ownership/inventory unchanged. Include canceled destination insertion and orderly-stop persistence. API signatures here were verified against source; these snippets were not compiled or run as GameTests in this research task.
