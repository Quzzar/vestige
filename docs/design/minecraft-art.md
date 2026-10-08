# Minecraft art and pixel scale

**Owner baseline, October 8, 2026:** Vestige artwork must fit Minecraft's style and native pixel ratios. Read this before authoring item sprites, block textures/UVs, worn artwork or GUI assets. [Spell art direction](../spell-art-direction.md) covers spell silhouettes and motion separately.

## Workflow and completion

1. **Inspect pinned vanilla references.** Choose comparable items or surfaces from the local Minecraft 1.21.1 resources. Establish their logical grid, occupied footprint, shading and model/UV scale from actual pixels and native rendering. Use that pixel language with an independently authored shape.
2. **Design at the destination scale.** Ordinary inventory sprites use an actual 16×16 grid. Make the silhouette, material and important accents readable there. Larger generated studies are source concepts: export them before judging their detail. For targeted revisions, an integer nearest-neighbor enlargement of the exact production sprite is a useful edit reference.
3. **Inspect the exported PNG.** View native size and an integer nearest-neighbor enlargement of those same pixels, on light and dark backgrounds. Verify that strand/shadow pairs, openings, diagonal steps, accents and component joins survived. A 16×16 canvas alone does not establish a good drawing.
4. **Inspect Minecraft.** Load those exact assets through their real models beside vanilla items in the ordinary inventory at a normal GUI scale. Keep neighbors, lighting and scale consistent between alternatives. Inspect held, dropped, worn or block views when those appearances or transforms change. Label concept sheets and comparison boards accurately; native evidence comes from the actual client.
5. **Freeze the accepted result.** Retain production PNGs, source/prompt, model/export settings, native captures and resource hashes. Check that loaded and packaged textures match the reviewed files. Record owner acceptance separately from verification, publication and installation. Changing accepted pixels creates a new art revision.

Completion means the exported asset fits the reference's pixel scale and reads correctly in Minecraft. Mechanical checks establish file integrity; native review establishes appearance, and the owner selects the accepted design.

## Pixel ratios and footprint

- Ordinary item sprites use 16×16 production pixels and ordinary generated/handheld model scale. GUI scale enlarges those logical pixels uniformly. Match the visual weight of comparable vanilla items instead of filling every cell.
- Vestige's inventory-sprite default is a clear one-pixel transparent margin. This is our convention; some vanilla references reach the canvas edge. Preserve proportions and use padding for placement rather than stretching or squashing the drawing.
- For block faces, match reference texel density against actual model dimensions and UVs. A small face should not automatically receive a stretched full-block texture. Inspect repeated finishes and seams in the world.
- For worn textures and multi-part atlases, follow the comparable vanilla UV layout and physical pixel density. An atlas can exceed 16×16 while its parts retain native pixel ratios; dimensions alone are insufficient.
- Preserve existing owner-approved asset-specific decisions. A larger texture or special transform needs an explicit documented purpose and native review. Describe its resolution accurately: fitting a detailed PNG into a small menu slot does not make it a literal 16×16 sprite.

## Shading and detail

Use a small, coherent material palette with broad readable shade regions. Match the reference's contrast and lighting. Shadows support the form with restrained material-tinted values; preserve crisp pixel boundaries and ordinary item alpha. Partial transparency belongs to an intentionally translucent material/effect with a supporting native reference.

For fine string, keep a pale strand and its modest darker companion edge readable together around bends, crossings and loose ends. Maintain that relationship through the shape. The rejected pass reduced long sections to a single alternating light/dark path: it became too thin and patchy. The accepted result restores a fuller strand with a softer, more continuous edge. Judge the exported pixels rather than imposing one stroke-width formula on every corner.

Palette budgets are asset-specific. The approved threads use at most four opaque shades; retained wand bodies use at most eight including their small gold bindings. These are examples, not a universal Minecraft color count. Preserve distinctive accents and layered component joins when reducing colors. Broad shade regions should carry the material instead of many tiny variations, checkerboard texture or isolated bright specks.

## Conversion pitfalls

A large generated source can differ substantially from its inventory export. Extra logical steps merge adjacent pale/shadow lanes or lose narrow strands during reduction. Nearest-neighbor sampling alone does not repair an incompatible source grid. Simplify or reauthor the drawing so its structure survives at the destination scale, then inspect the export again.

Our nondithered exporter selects a dominant shade per logical cell. Coverage, crop and fit affect contour and shading; they can erase a companion edge even when the concept looks good. Treat sampling changes as art changes. Preserve aspect ratio and important accents instead of fitting unrelated shapes into identical occupied rectangles. Use the final exported PNG for enlarged diagnosis and native captures for the final comparison.

Occupied-pixel counts, palette counts, connectivity and padding are diagnostics. Optimizing them did not establish good artwork: the rejected sparse pass passed structural checks. Preserve enough adjacent pale/dark pixels to describe the material at normal inventory size.

## Approved example

The owner approved [magical threads v6](../art/wands-native/threads-v6/README.md) on October 8. The [approval record](../art/wands-native/threads-v6/approval.json) pins the five exact accepted PNGs. Its [native String comparison](../art/wands-native/threads-v6/inventory/threads-beside-string-and-lead.png), [edit prompt](../art/wands-native/source-art/prompts-threads-v6.json) and [verification](../art/wands-native/threads-v6/verification.json) preserve the form and export/client evidence. V5 remains the rejected thin/patchy example.

For wand/thread changes, run `tools/author_wand_models.py --check` with the configured Pillow runtime. It checks exact exports/models, 16×16 RGBA, hard alpha, palette bounds, padding and connectivity, including all 63 body/tip compositions. Pair it with client review. Retain the approved five thread PNGs and fifteen selected wand layers unless a new revision is requested. Use the corresponding asset author/checker for other item, block or worn artwork.
