package com.quzzar.vestige.magic.world;

import com.mojang.math.Transformation;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

/** Finite vanilla block-model animation, owned by its formation rather than saved as world content. */
public final class SpellBlockDisplay extends Display.BlockDisplay {
    public SpellBlockDisplay(EntityType<? extends Display.BlockDisplay> type,Level level) { super(type,level); }
    void appearance(BlockState material,float offset) {
        CompoundTag data=new CompoundTag();
        data.put("block_state",NbtUtils.writeBlockState(material));
        data.put("transformation",Transformation.CODEC.encodeStart(NbtOps.INSTANCE,
                new Transformation(new Vector3f(0,offset,0),null,new Vector3f(1,1,1),null)).getOrThrow());
        data.putInt("interpolation_duration",1); data.putInt("start_interpolation",0);
        data.putFloat("width",1); data.putFloat("height",2); data.putFloat("view_range",1);
        // Vanilla's setters are private; its protected NBT reader updates the same synchronized fields.
        super.readAdditionalSaveData(data);
    }
    @Override protected void readAdditionalSaveData(CompoundTag data) { super.readAdditionalSaveData(data); discard(); }
    @Override public boolean shouldBeSaved() { return false; }
}
