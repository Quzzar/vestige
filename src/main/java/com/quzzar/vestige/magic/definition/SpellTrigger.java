package com.quzzar.vestige.magic.definition;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

/**
 * An event pattern that can begin a cast.
 *
 * @param id the identifier of this trigger within its spell
 * @param event the event type matched by this trigger
 * @param conditions the conditions that further constrain activation
 */
public record SpellTrigger(ResourceLocation id, ResourceLocation event, List<SpellCondition> conditions) {
    public SpellTrigger {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(conditions, "conditions");
        conditions = List.copyOf(conditions);
    }
}
