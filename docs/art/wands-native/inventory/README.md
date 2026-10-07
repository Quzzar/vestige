# First inventory comparison · October 7

**Historical first artwork, superseded by [the padded revision](../padded-v2/README.md).** These are the native frames that prompted the owner's request to simplify shading and add one pixel of transparent padding.

Both PNGs are unedited Minecraft 1.21.1 / NeoForge 21.1.72 framebuffer captures using the vanilla survival InventoryScreen renderer. The disposable review world holds thirty-six actual item stacks. Automatic GUI scaling yields a 320×240 GUI in the 1920×1440 framebuffer. Review-only cursor positioning over the avatar and clearing recipe notifications leave the comparison unobstructed.

`wands-and-threads.png` shows seven untipped bodies on the first row, eight tipped examples on the second, and five threads beside String, Lead, Amethyst and Diamond on the third. The hotbar contains vanilla items. `beside-vanilla-materials.png` interleaves tipped wands with their vanilla bases.

All twenty resource-loaded component/thread PNG hashes match the original October 7 Kithkyn Testing artifact (`ab9ef827eb60c7c0e10a92681d01e771f73a008a4298ddeabc1ed437bcc5518f`). The manifest pins assets, frames, exact slot contents and the opt-in capture harness. No artwork, normal gameplay, installed jar or production source changes accompanied this first review. The harness source is retained here for repeatable inspection; it is not part of normal mod compilation.

To repeat, temporarily place the retained harness in the matching apparatus/client source package, exclude `wand_inventory` from the two generic NativeEffectsCapture guards, and run the existing `runEffectsCapture` task with `capture_kind=wand_inventory`, `recipe_viewer=none` and an isolated capture directory. Restore those temporary source changes after inspection.
