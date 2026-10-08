# Recorded spell definition archive

These 214 exact source files were retrieved from Git revision `4ae5c801` and checked against the original `definitionSha256` values in `tools/effects-viewer/src/native-clips.json`. Filenames are their SHA-256 hashes. They are historical recording evidence, never loaded as gameplay definitions.

The October 7 removal of ordinary spell cooldowns changed those definitions, including the cost lists of three spells’ modes. The cast gallery uses operator casts that bypass resources and cooldowns while retaining preparation and effects. `encode_native_capture.py --check --complete` permits reuse only when the current definition equals the hash-verified recorded definition with explicit cooldown cost entries removed. It rejects changes to effects, presentation, preparation, mana, modes or any other content. New capture encoding still requires the exact current source hash.

Original video bytes, capture metadata and their hashes remain unchanged. This archive proves a narrow source-only compatibility revision; it does not claim new footage or a new survival-balance test.
