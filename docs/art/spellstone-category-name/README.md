# Spellstone category name

October 6, 2026. The owner requested the workstation name **Spellstone** in place of **Spellstone Rituals**. Both JEI and EMI translation keys now use that title. The internal category identity remains `spellstone_ritual`.

[The actual JEI screen](jei/6-crafted_fireball.png) was visually inspected and shows **Spellstone** above the unchanged ritual drawing. The other two native captures retain the independent identification/crafting-memory checks. The Java 21 build succeeds with all 104 unit tests, zero failures. This resource-only rename adds no world mechanic, and the prior 187 passing world tests are not presented as a fresh run. EMI's packaged translation is verified; its screen was not recaptured for this label change.

[Verification](verification.json) pins the staged `build/spellstone-category-name/vestige-0.1.0.jar`, ZIP integrity and both packaged category labels. [Native build/client log](native-client-build.log) records the successful run. No Prism installation or scroll-name rarity coloring is included in this pass.
