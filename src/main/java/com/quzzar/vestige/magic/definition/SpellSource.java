package com.quzzar.vestige.magic.definition;

import net.minecraft.resources.ResourceLocation;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.net.URI;

/** Provenance retained by a native recreation; it does not delegate execution to the source mod. */
public record SpellSource(ResourceLocation spell, Optional<ResourceLocation> school, String revision, String displayName,
                          Optional<String> castType, OptionalInt cooldownTicks, Optional<Reference> reference) {
    public SpellSource {
        Objects.requireNonNull(spell); Objects.requireNonNull(school); Objects.requireNonNull(revision);
        Objects.requireNonNull(displayName); Objects.requireNonNull(castType); Objects.requireNonNull(cooldownTicks); Objects.requireNonNull(reference);
        if (cooldownTicks.isPresent() && cooldownTicks.getAsInt() < 0) throw new IllegalArgumentException("Negative source cooldown");
        if (reference.isEmpty() && (school.isEmpty() || castType.isEmpty() || cooldownTicks.isEmpty()))
            throw new IllegalArgumentException("Mod provenance requires school, cast type and cooldown; rules provenance requires a reference");
    }
    public SpellSource(ResourceLocation spell, ResourceLocation school, String revision, String displayName,
                       String castType, int cooldownTicks) {
        this(spell, Optional.of(school), revision, displayName, Optional.of(castType), OptionalInt.of(cooldownTicks), Optional.empty());
    }
    /** Source rank/rarity are inert bibliographic facts, independent of native tuning. */
    public record Reference(String system, String edition, String publication, int rank, boolean cantrip, String rarity, URI url) {
        public Reference {
            Objects.requireNonNull(system); Objects.requireNonNull(edition); Objects.requireNonNull(publication);
            Objects.requireNonNull(rarity); Objects.requireNonNull(url);
            if (system.isBlank() || edition.isBlank() || publication.isBlank() || rarity.isBlank() || rank < 1 || rank > 10)
                throw new IllegalArgumentException("Invalid rules reference");
            if (!url.isAbsolute() || url.getHost() == null || !(url.getScheme().equals("https") || url.getScheme().equals("http")))
                throw new IllegalArgumentException("Rules reference requires an absolute HTTP URL");
        }
    }
}
