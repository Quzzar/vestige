# Dissentient Diamond: blue fissure depth

**Owner follow-up:** the artwork looks good; its horizontal inventory placement needs centering. [V11](../dissentient-diamond-v11/README.md) retains every texture byte and corrects only the GUI alignment.

The owner liked v9 and requested blue recesses instead of black cracks, slightly darker than the outline and darker toward their centers. V10 changes only the thirteen near-black fissure pixels to dark petrol blue **#155365** and deeper navy **#103548**. Every other v9 pixel, including the liked cyan faces and teal lower outline, and the complete alpha mask remain identical. Final review of this revision is pending.

The built-in imagegen [edit](source.png) uses the [exact enlarged v9 native sprite](edit-target-64x.png). [Prompt](prompt.txt). The mechanical [export](export.py) samples only those thirteen generated crack cells and aligns them to the two requested blue shades. It retains all unaffected v9 texels to prevent generation drift. [Export settings, generated samples, changed coordinates and hashes](export.json).

The [16×16 PNG](dissentient-diamond-16.png) is the native source. The [32×32 PNG](dissentient-diamond-32.png) is its exact nearest-neighbor enlargement. [Light/dark pixel preview](production-preview.png), [vanilla reference board](inventory-reference-board.png), [production metadata](production-export.json).

The ordinary generated-item model, ingredient registry/recipe status and held Fluxed Flint revision are unchanged.

## Native inventory review

Three unedited 1920×1440 Minecraft frames were inspected: [GUI scale 3](native/inventory-scale-3.png), [GUI scale 2](native/inventory-scale-2.png), and [held/offered/dropped](native/held-offered-and-dropped.png). The [close crop](native/inventory-comparison-crop.png) retains raw screenshot pixels; the candidate is second in the top main row beside vanilla Diamond.

All six [native assertions](native/verification.json) pass. Production, exact native export, preview pack, frozen resources and loaded texture/model hashes agree. The final isolated client, including compilation, completed successfully in **1m 17s**. [Review receipt and hashes](native-review.json), [compressed final log](native/client.log.gz). The unchanged [art fixture](NativeFlintCapture.java) uses a CustomModelData Diamond carrier, without an ingredient registry or recipe.

The initial startup stalled in the unrelated Standing Stone voxel-union builder; its [thread evidence](initial-startup/startup-threads.txt) and [log](initial-startup/client.log.gz) are retained. The stopped preview was rerun with an isolated [collision bypass](StandingStoneCollision.preview.java). No Standing Stone appears in this scene. The original temporary collision source was restored after capture; root gameplay Java was not modified. These images verify the exact diamond asset, not Standing Stone collision behavior. No full build/unit/world suite, installed pack update, Fluxed Flint revision or publication is claimed. Final owner review remains pending.
