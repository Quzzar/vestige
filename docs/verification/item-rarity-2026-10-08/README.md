# Approved item-name rarity assignments

The owner approved the preceding rarity review on October 8: “Okay update them.” The production change adds native rarity properties in `ScrollItems`, the robe constructor and the Wayfarer constructor. It adds no text-color override, mechanic, trait, cost, wear, recipe, artwork or naming alias. [Accepted policy](../../design/item-imbuement-and-readiness.md#accepted-item-name-rarities).

- **Uncommon / yellow:** Dissentient Diamond (already set), Attunement Shard, Homebound Eye, Whispering Shell, magical Staffs, Wardweave Robes, Cinderweave Robes and Wayfarer Boots.
- **Rare / aqua:** Crane Bag and Fluxed Flint.
- **Common / white:** the five magical threads, Scroll Fragments, seven mundane Staffs and every Spellstone/Plinth/Standing Stone finish.
- Scroll/Wand names retain their identified spell's color and unknown white names. The magical Staff's item tier stays yellow across capacity/selection. Item rarity does not set spell rarity or Staff wear. Native enchantment promotion and existing shimmer remain independent.

## Executed verification

**202 unit tests** across 45 suites pass with zero failures/errors; the root Java 21 build completed in **1m 44s**. The adjective catalog (79 sources / 145 aliases), Staff trait audit and Staff texture author checks pass. No new formal unit tests were added for these cosmetic property assignments.

The isolated native client completed in **1m 38s**. All **84 presentation assertions** pass: registered colors, all sixteen Wayfarer variants, all five Homebound Eye routes, three Staff capacities, Common component/finish defaults, retained thread glint, italic adjective inheritance, known Scroll/Wand colors across all four spell rarities, unknown white names and vanilla enchanted-robe promotion. [GUI scale 3](native/rarity-scale-3.png), [GUI scale 2](native/rarity-scale-2.png) and [raw-pixel crop](native/rarity-comparison-crop.png). The two comparison columns render each real native tooltip's first line; the ordinary cursor tooltip remains visible over the inventory. Both unedited native screenshots were inspected.

The six relevant compiled classes match production, the native capture and the packaged JAR byte-for-byte. All 2328 production asset files are unchanged from the frozen snapshot and match native/packaged resources. This includes approved item artwork and the adjective catalog. [Receipt, hashes and logs](verification.json).

## Scope and limitations

The temporary capture replaces only the isolated copy's repair capture. It reuses the documented art-only Standing Stone collision bypass; no Standing Stone is placed in the scene. The original isolated collision source was restored exactly afterward. Root collision source was untouched, and the packaged root collision class differs from the preview bypass. No new world-test suite, optional Kithkyn client co-load, testing-pack installation or publication is claimed. Fluxed Flint's proposed ingredient/art changes and the Dissentient Diamond crafting recipe remain separate work.
