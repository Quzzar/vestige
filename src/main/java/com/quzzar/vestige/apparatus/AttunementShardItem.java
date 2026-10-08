package com.quzzar.vestige.apparatus;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import java.util.*;

/** An inspectable, versioned key with a retained blueprint, rather than a random channel. */
public final class AttunementShardItem extends Item {
    public AttunementShardItem(Properties properties) { super(properties); }
    @Override public boolean isFoil(ItemStack stack) { return true; }
    /** Inventory decoration is presentation only; malformed/unattuned keys have no badge. */
    public static Optional<AttunementMark> mark(ItemStack stack) {
        if(!stack.is(ScrollItems.ATTUNEMENT_SHARD.get()))return Optional.empty();
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if(tag.getInt("vestige_attunement_version")!=Attunement.VERSION)return Optional.empty();
        try { return Optional.of(AttunementMark.fromKey(tag.getString("key"))); }
        catch(IllegalArgumentException invalid) { return Optional.empty(); }
    }
    public static List<ResourceLocation> ingredients() {
        return List.of("amethyst_shard","amethyst_shard","echo_shard","iron_ingot","diamond","lapis_lazuli").stream().map(ResourceLocation::withDefaultNamespace).toList();
    }
    public static ItemStack create(RitualInputs inputs) {
        if(inputs.geometry().slots()!=8)throw new IllegalArgumentException("Attunement requires both four-node layers");
        var signature=new Attunement.Signature(inputs.geometry().slots(),inputs.geometry().innerShape(),
                inputs.geometry().slots()==8 ? Optional.of(inputs.geometry().outerShape()) : Optional.empty(),inputs.nodes().stream().map(n ->
                new Attunement.Node(n.seat()%2,n.offset(),BuiltInRegistries.ITEM.getKey(n.offering().getItem()),n.offering().isEmpty() ? 0 : 1,
                        n.material().isEmpty() ? Optional.empty() : Optional.of(BuiltInRegistries.ITEM.getKey(n.material().getItem())))).toList());
        var stack=new ItemStack(ScrollItems.ATTUNEMENT_SHARD.get());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(encodeSignature(signature)));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE,true); return stack;
    }
    /** Canonical bounded fields only; incidental shard metadata never becomes a device component. */
    public static CompoundTag encodeSignature(Attunement.Signature signature) {
        var tag = new CompoundTag();
        tag.putInt("vestige_attunement_version",Attunement.VERSION); tag.putInt("slots",signature.slots()); tag.putString("inner_shape",signature.innerShape().name());
        signature.outerShape().ifPresent(s -> tag.putString("outer_shape",s.name())); tag.putString("key",signature.key());
        ListTag nodes=new ListTag();
        for (var n:signature.nodes()) { CompoundTag entry=new CompoundTag(); entry.putInt("layer",n.layer()); entry.putIntArray("offset",new int[]{n.offset().getX(),n.offset().getY(),n.offset().getZ()});
            entry.putString("ingredient",n.ingredient().toString()); entry.putInt("count",n.count()); n.material().ifPresent(m -> entry.putString("material",m.toString()));nodes.add(entry); }
        tag.put("nodes",nodes);
        return tag;
    }
    public static Optional<Attunement.Signature> signature(ItemStack stack) {
        if (!stack.is(ScrollItems.ATTUNEMENT_SHARD.get())) return Optional.empty();
        return readSignature(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag());
    }
    /** Devices retain and verify the original shard blueprint without changing its item identity. */
    public static Optional<Attunement.Signature> readSignature(CompoundTag tag) {
        if (tag.getInt("slots")!=8 || tag.getInt("vestige_attunement_version")!=Attunement.VERSION || tag.getString("key").length()!=64) return Optional.empty();
        try {
            var list=tag.getList("nodes",Tag.TAG_COMPOUND); if (list.size()!=tag.getInt("slots") || list.size()>8) return Optional.empty();
            List<Attunement.Node> nodes=new ArrayList<>();
            for (int i=0;i<list.size();i++) { var n=list.getCompound(i); var xyz=n.getIntArray("offset"); if (xyz.length!=3) return Optional.empty();
                var ingredient=ResourceLocation.tryParse(n.getString("ingredient"));var material=n.contains("material") ? ResourceLocation.tryParse(n.getString("material")) : null;
                if(ingredient==null || n.contains("material") && material==null)return Optional.empty();
                nodes.add(new Attunement.Node(n.getInt("layer"),new net.minecraft.core.BlockPos(xyz[0],xyz[1],xyz[2]),ingredient,n.getInt("count"),Optional.ofNullable(material))); }
            var result=new Attunement.Signature(tag.getInt("slots"),LeylineShaping.Shape.valueOf(tag.getString("inner_shape")),
                    tag.contains("outer_shape") ? Optional.of(LeylineShaping.Shape.valueOf(tag.getString("outer_shape"))) : Optional.empty(),nodes);
            return result.key().equals(tag.getString("key")) ? Optional.of(result) : Optional.empty();
        } catch (IllegalArgumentException invalid) { return Optional.empty(); }
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> text,TooltipFlag flag) {
        signature(stack).ifPresent(s -> text.add(AttunementMark.fromKey(s.key()).component()));
    }
}
