# Native camera, sound, body, and item appearance APIs

Inspected **2026-10-01** against Minecraft **1.21.1 / NeoForge 21.1.72**. This records usable source seams for native spell manifestations; it does not claim these mechanics are implemented or verified in a running client. Wands, discovery, and progression remain deferred.

The primary Minecraft source is the locally generated [patched development source JAR](/Users/quzzar/Projects/vestige/build/moddev/artifacts/neoforge-21.1.72-minecraft-sources.jar), SHA-256 `e9eb34437fbdd7bc6d7fe25917d442b99737ea088db3fae31f29ae072af4e5b7`. NeoForge declarations come from its [pinned source JAR](/Users/quzzar/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/21.1.72/a518151fcf88e5bda178895a6fba9493999686b2/neoforge-21.1.72-sources.jar), SHA-256 `d15a21180846b42e8456e716d25d6bc75b99707d3b51acbe71851708e943c3ee`. Method citations point to temporary extracted archive members. Their package paths and line numbers identify the same source if `/tmp` is cleared. No upstream Java source is added to the repository.

## Remote perception without moving the body

Use the **client** camera setter:

```java
Minecraft mc = Minecraft.getInstance();
Entity previous = mc.getCameraEntity();
CameraType previousType = mc.options.getCameraType();
mc.options.setCameraType(CameraType.FIRST_PERSON);
mc.setCameraEntity(sensor);
```

`Minecraft.getCameraEntity(): Entity` is nullable; `setCameraEntity(Entity)` assigns the client camera and selects its entity post-effect. It does not teleport the server player. **`ServerPlayer.setCamera(@Nullable Entity)` is unsuitable for preserving the body:** that method explicitly teleports the player to the camera entity's coordinates, updates chunk tracking, and sends the camera packet. [Minecraft.java:2612][camera], [Options.java:1574][camera-options], [ServerPlayer.java:1717][server-camera]

Switching the camera does not provide a complete control system. `LocalPlayer.sendPosition()` and `serverAiStep()` gate normal body movement on `isControlledCamera()`, which compares the camera entity to the local player. However, `MouseHandler.turnPlayer(double)` still calls `minecraft.player.turn(...)`; mouse look is not redirected to the sensor. `CalculatePlayerTurnEvent` affects sensitivity/cinematic smoothing, not the chosen entity. A controllable sensor therefore needs an explicit client turn redirect plus cast-owned, rate-limited input payloads that the server validates against owner, active cast, sensor, dimension, speed, and allowed radius. [LocalPlayer.java:266][local-player], [LocalPlayer.java:684][local-player], [MouseHandler.java:298][mouse], [ClientHooks.java:367][client-hooks]

Capture desired sensor movement from `MovementInputUpdateEvent.getInput(): Input`, then clear the body's public movement fields (`forwardImpulse`, `leftImpulse`, `jumping`, directional booleans, and `shiftKeyDown`) while the lease is active. Cancel `InputEvent.InteractionKeyMappingTriggered` and call `setSwingHand(false)` for attack/use/pick inputs. These events use the **client game bus**. Cancellation is client behavior, so the server must also reject body attacks, mining, and use actions forbidden during the view. Camera selection itself does not stop gravity or knockback: a requirement that the body remain at its original position needs a separate, explicit body anchoring mechanic that continues ordinary damage/status processing. [MovementInputUpdateEvent.java:36][movement], [Input.java:8][input], [InputEvent.java:296][interaction]

The server's normal chunk tracking follows `player.chunkPosition()`; entity tracking also requires its chunk to be tracked by that player. A remote camera does not automatically acquire distant terrain. Initially bound perception to already tracked chunks, or implement a separate observer subscription with bounded tickets and cleanup. Do not advertise unlimited scouting based on the camera setter alone. [ChunkMap.java:1061][chunks], [ChunkMap.java:1321][chunks]

### Camera and cosmetic lease cleanup

One client lease should own the cast ID, world identity, sensor ID/UUID, deadline, saved camera/type, and every associated cosmetic. Make closure idempotent. The following are usable hooks; all except registration use the **game bus**:

| Trigger | Exact hook and action |
| --- | --- |
| Missing/dead sensor, expired lease, changed dimension | `ClientTickEvent.Post`; validate `mc.level`, `mc.player`, sensor and deadline each tick. Allow bounded waiting for a newly tracked sensor. [ClientTickEvent.java:33][client-tick] |
| Sensor removed | `EntityLeaveLevelEvent.getEntity()` / `getLevel()`; close only the matching lease. [EntityLeaveLevelEvent.java:24][entity-leave] |
| Respawn | `ClientPlayerNetworkEvent.Clone.getOldPlayer()` / `getNewPlayer()`; avoid restoring a removed old player. [ClientPlayerNetworkEvent.java:135][network] |
| Disconnect | `ClientPlayerNetworkEvent.LoggingOut`; its player/connection/game-mode getters can return null. Clear state without reinstalling stale entities. [ClientPlayerNetworkEvent.java:86][network] |
| Client level unload | `LevelEvent.Unload.getLevel()`; verify the event belongs to the active client level. [LevelEvent.java:76][level-event] |
| Resource reload | **Mod bus** `RegisterClientReloadListenersEvent.registerReloadListener(PreparableReloadListener)`; run cleanup/cache changes on the client main thread during application. [RegisterClientReloadListenersEvent.java:41][client-reload] |
| Cast end, dispel, server data reload | Server sends an explicit lease-end cue; client deadline and sensor validation still cover a missed cue. This is a proposed protocol, not a built-in event. |

Restore only when the lease still owns the current camera. Prefer the current valid `mc.player` after respawn; reuse the saved entity only when it remains live in the same client level. A newer cast or another mod may have taken camera ownership. Minecraft itself nulls its camera during disconnect, so cleanup must not reverse that. These ownership rules are implementation requirements inferred from the mutable camera and network lifecycle. [Minecraft.java:2233][camera], [Minecraft.java:2616][camera], [ClientPlayerNetworkEvent.java:86][network]

## Silence and muffling

Two different hooks are required for complete ordinary sound coverage:

| Hook | API and path |
| --- | --- |
| World emission | `PlayLevelSoundEvent.AtPosition.getPosition(): Vec3` or `.AtEntity.getEntity(): Entity`; base event has `getLevel()`, `getSound(): Holder<SoundEvent>`, `getSource(): SoundSource`, `setNewVolume(float)`, and `setNewPitch(float)`. It **is cancellable**: `setCanceled(true)` suppresses emission. Both server `playSeededSound` overloads post it before broadcasting. [PlayLevelSoundEvent.java:39][level-sound], [ServerLevel.java:986][server-sound] |
| Client playback | `net.neoforged.neoforge.client.event.sound.PlaySoundEvent` exposes `getOriginalSound()` / `getSound(): SoundInstance` and `setSound(@Nullable SoundInstance)`. It **is not cancellable**; `setSound(null)` suppresses playback. `SoundEngine.play(SoundInstance)` invokes the hook before resolving/playing sound. [PlaySoundEvent.java:53][play-sound], [SoundEngine.java:429][sound-engine] |

`ClientLevel.playSeededSound` also posts the world event, but `playLocalSound` goes directly through sound instances to the sound manager. A world-only filter therefore misses local ambience and other direct playback. Both event types use the game bus; only the playback event is client-only. [ClientLevel.java:521][client-sound], [ClientLevel.java:560][client-sound], [PlaySoundEvent.java:22][play-sound]

Distinguish **a silent source region** from **a deafened listener**. Filter positional `SoundInstance` coordinates for the former; apply receiver state for the latter. Check `isRelative()` and `getSource()` rather than treating UI/music coordinates as world positions. The sound listener follows the active camera through `SoundManager.updateSource(Camera)`, so remote-view spells need an explicit decision about hearing at the eye or at the body. [SoundInstance.java:13][sound-instance], [SoundManager.java:222][sound-manager]

Canceling new playback does not terminate an already playing loop. `SoundManager.stop(SoundInstance)` and `isActive(SoundInstance)` allow a bounded registry of matching active instances; remove inactive entries and stop only owned/matching sounds when a silence zone appears or a listener enters it. The alternative `stop(ResourceLocation, SoundSource)` targets whole ID/category matches and is too broad for a local region. Muffling via a replacement sound instance must preserve all delegated playback/streaming properties and tickability for moving/looping sounds; a simple volume-only wrapper can change their behavior. [SoundManager.java:269][sound-manager], [SoundManager.java:298][sound-manager], [SoundInstance.java:13][sound-instance]

## Physical scale, reach, wall climbing, and water walking

`Attributes.SCALE` is synced, defaults to `1`, and ranges from `0.0625` to `16`. `LivingEntity.getScale()` reads it, the living tick refreshes dimensions when it changes, `getDimensions(Pose)` scales physical dimensions except the special sleeping pose, and the living renderer scales the model and shadow. This is the usable **physical** enlargement/shrinking seam; a renderer-only matrix scale does not change collision or eye height. `Entity.refreshDimensions()` posts `EntityEvent.Size`, updates bounds, and may adjust position to fit an expanded body. Placement and restoration near walls need behavioral verification. [Attributes.java:124][attributes], [LivingEntity.java:552][living], [LivingEntity.java:2573][living], [LivingEntity.java:3422][living], [Entity.java:2963][entity], [LivingEntityRenderer.java:97][living-renderer]

Use cast-owned transient attribute modifiers, checking `getAttribute(...)` for null:

```java
AttributeInstance scale = living.getAttribute(Attributes.SCALE);
scale.addTransientModifier(new AttributeModifier(
    castModifierId, factor - 1.0,
    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
// Idempotent close removes this cast's modifier only:
scale.removeModifier(castModifierId);
```

In 1.21.1 the modifier ID is a **`ResourceLocation`, not a UUID**. Unique cast IDs avoid removing another cast's state; do not reset the base attribute to implement cleanup. `EntityEvent.Size.setNewSize(EntityDimensions)` is available for other physical shapes, but layering it on the scale attribute can double-scale dimensions. [AttributeModifier.java:22][modifier], [AttributeInstance.java:94][attribute-instance], [EntityEvent.java:126][size-event]

Scale does not implicitly increase player reach. The separate synced attributes are `Attributes.BLOCK_INTERACTION_RANGE` (default `4.5`, range `0–64`) and `ENTITY_INTERACTION_RANGE` (default `3`, range `0–64`); `STEP_HEIGHT` defaults to `0.6`, range `0–10`. Exact player accessors are `blockInteractionRange()` and `entityInteractionRange()`. Server interaction validation uses the corresponding `canInteractWithBlock(...)` / `canInteractWithEntity(...)` methods. Own spell target/range limits remain a separate authored constraint. [Attributes.java:45][attributes], [Attributes.java:136][attributes], [Player.java:2276][player], [ServerGamePacketListenerImpl.java:1132][server-input], [ServerGamePacketListenerImpl.java:1593][server-input]

**Wall climbing:** `LivingEntity.onClimbable(): boolean` calls `CommonHooks.isLivingOnLadder(BlockState, Level, BlockPos, LivingEntity): Optional<BlockPos>`, which checks `IBlockStateExtension.isLadder(LevelReader, BlockPos, LivingEntity)` and records `lastClimbablePos`. This is a block-specific ladder seam. An ordinary player's spell that climbs arbitrary existing walls needs a method hook for active state and a valid nearby vertical surface, or explicit authoritative movement. Merely spawning climbing particles does not provide climbing. [LivingEntity.java:1609][living], [CommonHooks.java:381][common-hooks], [IBlockStateExtension.java:102][block-extension]

A narrowly scoped `onClimbable` hook reuses the vanilla climb motion path, but must account for the recorded climb position and fall behavior. A motion implementation using `EntityTickEvent.Pre/Post`, horizontal collision and controlled vertical velocity is a different mechanic and needs matching prediction on both sides. Do **not** cancel `EntityTickEvent.Pre` to freeze movement: cancellation skips the entity's entire tick and its post event. [LivingEntity.java:2388][living], [LivingEntity.java:2411][living], [EntityTickEvent.java:22][entity-tick]

**Water walking:** `LivingEntity.canStandOnFluid(FluidState): boolean` defaults to false and influences both fluid travel and `EntityCollisionContext`. A scoped, synchronized hook returning true for active water walking on `FluidTags.WATER` is usable on ordinary living entities; an override is available for an owned entity subclass. There is no need to replace water blocks. [LivingEntity.java:2209][living], [LivingEntity.java:2227][living], [EntityCollisionContext.java:35][collision-context]

The vanilla collision path has a material limitation: `LiquidBlock.getCollisionShape(...)` supplies `STABLE_SHAPE` only for a source block (`LEVEL == 0`), feet above that shape, and no same-fluid layer above. That shape is **half a block high**. It does not produce accurate surface-height collision across flowing water. If the adaptation promises flowing/full-surface walking, add a per-entity fluid collision hook using `EntityCollisionContext.getEntity()`, actual fluid height, an above-surface test, and the active lease. Waterlogged blocks and custom fluids need their own tested coverage. Crouch-to-sink should be explicitly authored. [LiquidBlock.java:56][liquid], [LiquidBlock.java:81][liquid], [EntityCollisionContext.java:52][collision-context]

## ItemFacade: appearance without changing the actual item

`DataComponents.CUSTOM_MODEL_DATA` is `DataComponentType<CustomModelData>`, with **persistent and network-synchronized** storage; the value is `new CustomModelData(int)`. The generic `minecraft:custom_model_data` item predicate reads that component. This version has **no `DataComponents.ITEM_MODEL`**. Custom model data selects only overrides actually authored for the base item's model; it cannot by itself make any item render as any other item. [DataComponents.java:98][components], [CustomModelData.java:8][custom-model], [ItemProperties.java:102][item-properties]

For a temporary illusion, keep a private client appearance lease keyed to a stable target (for example, the dropped item's entity UUID), resolve an `ItemStack.copy()` or a new facade stack **only for rendering**, and leave the real stack, inventory, tool behavior and serialization alone. Setting the component on the real server stack changes persistent item state. `IClientItemExtensions.of(ItemStack)` resolves by item type, and its `getCustomRenderer()` is a type-level renderer extension rather than a per-cast facade resolver. [ItemStack.java:570][item-stack], [DataComponents.java:98][components], [IClientItemExtensions.java:42][item-extensions], [IClientItemExtensions.java:167][item-extensions]

The renderer must receive the same facade stack for **both model selection and drawing**. Public calls are:

```java
BakedModel getModel(ItemStack stack, @Nullable Level level,
                   @Nullable LivingEntity entity, int seed);
void render(ItemStack stack, ItemDisplayContext context, boolean leftHand,
            PoseStack poses, MultiBufferSource buffers,
            int light, int overlay, BakedModel model);
void renderStatic(@Nullable LivingEntity entity, ItemStack stack,
                  ItemDisplayContext context, boolean leftHand,
                  PoseStack poses, MultiBufferSource buffers,
                  @Nullable Level level, int light, int overlay, int seed);
```

Replacing only `getModel` leaves item-specific model branches, tint/glint, render passes and custom renderer selection operating on the original stack. Replacing only `renderStatic` misses important callers. [ItemRenderer.java:102][item-renderer], [ItemRenderer.java:213][item-renderer], [ItemRenderer.java:241][item-renderer]

| Context | Complete source path and usable anchor |
| --- | --- |
| First/third person held item | `ItemInHandRenderer.renderItem` → entity-aware `ItemRenderer.renderStatic` → `getModel` + `render`. `ItemInHandLayer.renderArmWithItem` uses the same path. Resolve by living entity/hand. [ItemInHandRenderer.java:136][hand-renderer], [ItemInHandLayer.java:54][hand-layer] |
| Dropped item | `ItemEntityRenderer.render` → `getModel` → `renderMultipleFromCount` → `ItemRenderer.render`; **bypasses `renderStatic`**. Resolve by `ItemEntity` UUID and pass the facade copy through model and drawing. [ItemEntityRenderer.java:44][drop-renderer], [ItemEntityRenderer.java:123][drop-renderer] |
| Item frame | `ItemFrameRenderer.render` → non-entity `ItemRenderer.renderStatic`; resolve by frame UUID at the caller. Maps use a separate map path. [ItemFrameRenderer.java:105][frame-renderer] |
| GUI | `GuiGraphics.renderItem` → `getModel` + `render`; **bypasses `renderStatic`**. Menu/slot identity requires a caller-level anchor; identical stacks and seeds alone are not reliable item identity. [GuiGraphics.java:1278][gui] |

`RenderHandEvent` cancellation covers the first-person hand only. A dropped-item-only initial facade is a valid scope if documented; it should not claim inventory, held-item or frame illusions. Clear facade leases on expiration, changed target stack, entity removal, disconnect and reload; do not cache stale baked models across resource reload. Those lease rules are proposed ownership requirements, inferred from the rendering and reload paths. [RenderHandEvent.java:30][render-hand], [ItemRenderer.java:260][item-renderer]

## Verification still required

Source inspection confirms API signatures and hook paths only. Implementation tests should cover body position and sensor control; camera restoration after despawn/disconnect/reload/respawn; distant chunk limits; active-loop silence; physical bounds/eye/reach changes and restoration; climbing and source/flowing-water collision; facade consistency across every advertised render context with original item data unchanged. No client run, compile, unit test or GameTest was performed for this research document.

[camera]: /tmp/vestige-perception-api-sources/net/minecraft/client/Minecraft.java
[camera-options]: /tmp/vestige-perception-api-sources/net/minecraft/client/Options.java
[server-camera]: /tmp/vestige-perception-api-sources/net/minecraft/server/level/ServerPlayer.java
[local-player]: /tmp/vestige-perception-api-sources/net/minecraft/client/player/LocalPlayer.java
[mouse]: /tmp/vestige-perception-api-sources/net/minecraft/client/MouseHandler.java
[client-hooks]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/client/ClientHooks.java
[movement]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/client/event/MovementInputUpdateEvent.java
[input]: /tmp/vestige-perception-api-sources/net/minecraft/client/player/Input.java
[interaction]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/client/event/InputEvent.java
[chunks]: /tmp/vestige-perception-api-sources/net/minecraft/server/level/ChunkMap.java
[client-tick]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/client/event/ClientTickEvent.java
[entity-leave]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/event/entity/EntityLeaveLevelEvent.java
[network]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/client/event/ClientPlayerNetworkEvent.java
[level-event]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/event/level/LevelEvent.java
[client-reload]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/client/event/RegisterClientReloadListenersEvent.java
[level-sound]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/event/PlayLevelSoundEvent.java
[server-sound]: /tmp/vestige-perception-api-sources/net/minecraft/server/level/ServerLevel.java
[play-sound]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/client/event/sound/PlaySoundEvent.java
[sound-engine]: /tmp/vestige-perception-api-sources/net/minecraft/client/sounds/SoundEngine.java
[client-sound]: /tmp/vestige-perception-api-sources/net/minecraft/client/multiplayer/ClientLevel.java
[sound-instance]: /tmp/vestige-perception-api-sources/net/minecraft/client/resources/sounds/SoundInstance.java
[sound-manager]: /tmp/vestige-perception-api-sources/net/minecraft/client/sounds/SoundManager.java
[attributes]: /tmp/vestige-perception-api-sources/net/minecraft/world/entity/ai/attributes/Attributes.java
[living]: /tmp/vestige-perception-api-sources/net/minecraft/world/entity/LivingEntity.java
[entity]: /tmp/vestige-perception-api-sources/net/minecraft/world/entity/Entity.java
[living-renderer]: /tmp/vestige-perception-api-sources/net/minecraft/client/renderer/entity/LivingEntityRenderer.java
[modifier]: /tmp/vestige-perception-api-sources/net/minecraft/world/entity/ai/attributes/AttributeModifier.java
[attribute-instance]: /tmp/vestige-perception-api-sources/net/minecraft/world/entity/ai/attributes/AttributeInstance.java
[size-event]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/event/entity/EntityEvent.java
[player]: /tmp/vestige-perception-api-sources/net/minecraft/world/entity/player/Player.java
[server-input]: /tmp/vestige-perception-api-sources/net/minecraft/server/network/ServerGamePacketListenerImpl.java
[common-hooks]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/common/CommonHooks.java
[block-extension]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/common/extensions/IBlockStateExtension.java
[entity-tick]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/event/tick/EntityTickEvent.java
[collision-context]: /tmp/vestige-perception-api-sources/net/minecraft/world/phys/shapes/EntityCollisionContext.java
[liquid]: /tmp/vestige-perception-api-sources/net/minecraft/world/level/block/LiquidBlock.java
[components]: /tmp/vestige-perception-api-sources/net/minecraft/core/component/DataComponents.java
[custom-model]: /tmp/vestige-perception-api-sources/net/minecraft/world/item/component/CustomModelData.java
[item-properties]: /tmp/vestige-perception-api-sources/net/minecraft/client/renderer/item/ItemProperties.java
[item-stack]: /tmp/vestige-perception-api-sources/net/minecraft/world/item/ItemStack.java
[item-extensions]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/client/extensions/common/IClientItemExtensions.java
[item-renderer]: /tmp/vestige-perception-api-sources/net/minecraft/client/renderer/entity/ItemRenderer.java
[hand-renderer]: /tmp/vestige-perception-api-sources/net/minecraft/client/renderer/ItemInHandRenderer.java
[hand-layer]: /tmp/vestige-perception-api-sources/net/minecraft/client/renderer/entity/layers/ItemInHandLayer.java
[drop-renderer]: /tmp/vestige-perception-api-sources/net/minecraft/client/renderer/entity/ItemEntityRenderer.java
[frame-renderer]: /tmp/vestige-perception-api-sources/net/minecraft/client/renderer/entity/ItemFrameRenderer.java
[gui]: /tmp/vestige-perception-api-sources/net/minecraft/client/gui/GuiGraphics.java
[render-hand]: /tmp/vestige-perception-api-sources/net/neoforged/neoforge/client/event/RenderHandEvent.java
