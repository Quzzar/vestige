package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.presentation.MagicAdjectives;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class SpellStaffItem extends Item {
    public SpellStaffItem(Properties properties) { super(properties.attributes(StaffCombat.attributes())); }
    /** Vanilla combining rebuilds a fresh item and discards stored magic; use exact-stack ritual repair. */
    @Override public boolean isRepairable(ItemStack stack) { return false; }
    @Override public boolean canGrindstoneRepair(ItemStack stack) { return false; }
    @Override public Component getName(ItemStack stack) {
        return StaffData.binding(stack).map(SpellStaffItem::staffName)
                .orElseGet(() -> super.getName(stack));
    }
    /** The same knowledge-aware name labels the item and its selection menu. */
    public static Component staffName(StaffData.Binding binding) {
        var trait=binding.affinity();var label=new StringBuilder();
        for(var word:trait.getPath().replace('_',' ').replace('/',' ').split(" +")) {
            if(word.isEmpty()) continue;
            if(!label.isEmpty()) label.append(' ');
            label.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        var fallback=trait.getNamespace().equals("vestige") ? label.toString() : trait.getNamespace()+": "+label;
        var affinity=Component.translatableWithFallback("trait."+trait.getNamespace()+"."+trait.getPath().replace('/','.'),fallback);
        var base=Component.translatable("item.vestige.staff.named",affinity);
        return binding.active().<Component>map(s -> Component.translatable("item.vestige.staff.active",base,slotName(s))).orElse(base);
    }
    public static Component slotName(ScrollItems.Scroll scroll) {
        if(!SpellKnowledge.visible(scroll.spell())) return Component.translatable("item.vestige.spell_scroll");
        var name=MagicAdjectives.prefix(VestigeMainMod.location("spell_scroll"), scroll.augments().stream()
                .map(a -> MagicAdjectives.Adjustment.spellshaping(a.id(), a.degree())).toList());
        return name.append(ScrollItems.spellName(scroll.spell()));
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        var staff=player.getItemInHand(hand);
        if(player.isShiftKeyDown() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            StaffSelection.open(serverPlayer,hand);return InteractionResultHolder.consume(staff);
        }
        if(player.isShiftKeyDown()) return InteractionResultHolder.success(staff);
        // An unbound active slot turns the magical staff back into a held guard. A failed selected spell
        // remains a failed cast; it never masks a payment or condition failure by starting to block.
        if(StaffData.binding(staff).map(StaffData.Binding::active).orElse(java.util.Optional.empty()).isEmpty()) return StaffCombat.guard(player,hand);
        if(level.isClientSide) return InteractionResultHolder.success(staff);
        return StaffCasting.cast(player,hand) ? InteractionResultHolder.consume(staff) : InteractionResultHolder.fail(staff);
    }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) { return StaffCombat.hurtEnemy(stack,attacker); }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return StaffCombat.useAnimation(); }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return StaffCombat.useDuration(); }
    @Override public boolean isEnchantable(ItemStack stack) { return false; }
}
