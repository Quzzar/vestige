package com.quzzar.vestige.magic.world;

import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.*;
import java.util.*;
import java.util.function.Predicate;

/** A bounded block formation. A removed cell permanently loses its claim, including same-material replacements. */
final class TemporaryBlockFormation {
    private final ServerLevel level;
    private final BlockState material;
    private final boolean lift;
    private final List<Cell> cells=new ArrayList<>();
    private final int riseTicks,collapseTicks,height;
    private int age,collapseAge;
    private boolean collapsing,finished;
    private static final class Cell {
        final BlockPos pos;
        final int row,column;
        final BlockState state;
        boolean placed,abandoned,retiring;
        SpellBlockDisplay display;
        Cell(BlockPos pos,int row,int column,BlockState state) { this.pos=pos;this.row=row;this.column=column;this.state=state; }
    }
    TemporaryBlockFormation(ServerLevel level,BlockState material,BlockPos base,Direction across,int width,int height,int riseTicks,int collapseTicks) {
        this(level,material,base,across,width,height,riseTicks,collapseTicks,true);
    }
    TemporaryBlockFormation(ServerLevel level,BlockState material,BlockPos base,Direction across,int width,int height,int riseTicks,int collapseTicks,boolean lift) {
        this.level=level;this.material=material;this.lift=lift;this.height=height;this.riseTicks=riseTicks;this.collapseTicks=collapseTicks;
        for(int row=0;row<height;row++) for(int column=0;column<width;column++) cells.add(new Cell(base.relative(across,column-width/2).above(row),row,column-width/2,material));
    }
    static TemporaryBlockFormation tree(ServerLevel level,BlockPos base,int riseTicks,int collapseTicks) {
        var result=new TemporaryBlockFormation(level,SpellBlocks.TEMPORARY_LOG.get().defaultBlockState(),base,Direction.EAST,1,5,riseTicks,collapseTicks,false);
        result.cells.clear();
        for(int y=0;y<4;y++) result.cells.add(new Cell(base.above(y),y,0,SpellBlocks.TEMPORARY_LOG.get().defaultBlockState()));
        for(int y=3;y<=4;y++) for(int x=-1;x<=1;x++) for(int z=-1;z<=1;z++) {
            if(y==3 && x==0 && z==0) continue;
            result.cells.add(new Cell(base.offset(x,y,z),y,0,SpellBlocks.TEMPORARY_LEAVES.get().defaultBlockState()));
        }
        return result;
    }
    long survivingTrunks() { return cells.stream().filter(c->c.state.is(SpellBlocks.TEMPORARY_LOG.get()) && !c.abandoned).count(); }
    boolean water() { return material.is(SpellBlocks.TEMPORARY_WATER.get()); }
    boolean canPlace(Predicate<BlockPos> permitted) {
        for(Cell cell:cells) if (!permitted.test(cell.pos) || !level.getBlockState(cell.pos).isAir() || level.getBlockEntity(cell.pos)!=null) return false;
        for(Cell cell:cells) if(cell.row==0) {
            BlockPos support=cell.pos.below();
            if (!level.hasChunkAt(support) || level.getBlockState(support).getCollisionShape(level,support).isEmpty()) return false;
        }
        // Validate the complete lift before starting; a low ceiling must never trap a creature.
        for(Entity entity:occupants(bounds())) {
            if(!lift) { if(!water() && cells.stream().anyMatch(c->new AABB(c.pos).intersects(entity.getBoundingBox()))) return false;continue; }
            double top=cells.getFirst().pos.getY()+height+.02;
            AABB destination=entity.getBoundingBox().move(0,Math.max(0,top-entity.getY()),0);
            if (!level.getWorldBorder().isWithinBounds(destination) || !level.noCollision(entity,destination)) return false;
        }
        return true;
    }
    void tick() {
        if (finished) return;
        if (collapsing) { retract();return; }
        age++;
        double step=(double)riseTicks/height;
        for(Cell cell:cells) {
            if(cell.abandoned) continue;
            if(cell.placed) { if(!level.getBlockState(cell.pos).equals(cell.state)) abandon(cell);continue; }
            double progress=Math.max(0,Math.min(1,(age-cell.row*step-Math.abs(cell.column)*2)/step));
            if(progress<=0) continue;
            if (!level.hasChunkAt(cell.pos) || !level.getBlockState(cell.pos).isAir()) { abandon(cell);continue; }
            double top=cell.pos.getY()+progress;
            AABB rising=new AABB(cell.pos.getX(),cell.pos.getY()-1,cell.pos.getZ(),cell.pos.getX()+1,top,cell.pos.getZ()+1);
            for(Entity entity:lift?occupants(rising):List.<Entity>of()) {
                double lift=Math.max(0,top+.02-entity.getY());
                if(lift<=0) continue;
                AABB destination=entity.getBoundingBox().move(0,lift,0);
                if (!level.noCollision(entity,destination) || !level.getWorldBorder().isWithinBounds(destination)) { abandon(cell);break; }
                entity.teleportTo(entity.getX(),entity.getY()+lift,entity.getZ());entity.fallDistance=0;entity.hurtMarked=true;
            }
            if(cell.abandoned) continue;
            if(!water()) {
                if(cell.display==null) cell.display=display(cell,-1);
                cell.display.appearance(cell.state,(float)(progress-1));
            }
            if(age%3==0) particles(cell.pos.getX()+.5,top,cell.pos.getZ()+.5,12);
            if(progress>=1) {
                // Record ownership before onPlace/scheduled ticks can query it.
                cell.placed=true;
                if(!level.setBlock(cell.pos,cell.state,3)) { abandon(cell);continue; }
                discard(cell);particles(cell.pos.getX()+.5,top,cell.pos.getZ()+.5,20);
            }
        }
    }
    void collapse() {
        if (finished || collapsing) return;
        collapsing=true;
        for(Cell cell:cells) if(!cell.placed) abandon(cell);
        level.playSound(null,cells.getFirst().pos,(water()?SoundEvents.BUCKET_EMPTY:lift?SoundEvents.GLASS_BREAK:SoundEvents.WOOD_BREAK),SoundSource.BLOCKS,.7f,.65f);
    }
    private void retract() {
        collapseAge++;
        int stagger=Math.max(1,collapseTicks/(height+2));
        int movement=Math.max(4,collapseTicks-stagger*(height-1));
        for(Cell cell:cells) {
            if(cell.abandoned) continue;
            int start=(height-1-cell.row)*stagger;
            if(collapseAge<=start) continue;
            if(!cell.retiring) {
                if(!owns(cell.pos)) { abandon(cell);continue; }
                cell.retiring=true;cell.placed=false;
                if(!water()) cell.display=display(cell,0);
                level.setBlock(cell.pos,Blocks.AIR.defaultBlockState(),3);
                particles(cell.pos.getX()+.5,cell.pos.getY()+.5,cell.pos.getZ()+.5,28);
            }
            double progress=Math.min(1,(double)(collapseAge-start)/movement);
            if(cell.display!=null) cell.display.appearance(cell.state,(float)(-progress*(cell.row+1)));
            if(collapseAge%3==0) particles(cell.pos.getX()+.5,cell.pos.getY()+.5-progress*(cell.row+1),cell.pos.getZ()+.5,8);
            if(progress>=1) abandon(cell);
        }
        if(collapseAge>=collapseTicks+1) finish(null);
    }
    boolean owns(BlockPos pos) { return cells.stream().anyMatch(c->c.placed && !c.abandoned && c.pos.equals(pos) && level.hasChunkAt(pos) && level.getBlockState(pos).equals(c.state)); }
    void changed(BlockPos pos) { cells.stream().filter(c->c.placed && c.pos.equals(pos)).forEach(this::abandon); }
    boolean touches(ChunkAccess chunk) { return cells.stream().anyMatch(c->new net.minecraft.world.level.ChunkPos(c.pos).equals(chunk.getPos())); }
    boolean finished() { return finished; }
    ServerLevel level() { return level; }
    void finish(ChunkAccess unloading) {
        if(finished) return;finished=true;
        for(Cell cell:cells) {
            boolean remove=cell.placed && !cell.abandoned;
            cell.placed=false;cell.abandoned=true;discard(cell);
            if(!remove) continue;
            if(unloading!=null && new net.minecraft.world.level.ChunkPos(cell.pos).equals(unloading.getPos())) {
                if(unloading.getBlockState(cell.pos).equals(cell.state)) { unloading.setBlockState(cell.pos,Blocks.AIR.defaultBlockState(),false);unloading.setUnsaved(true); }
            } else if(level.hasChunkAt(cell.pos) && level.getBlockState(cell.pos).equals(cell.state)) level.setBlock(cell.pos,Blocks.AIR.defaultBlockState(),3);
        }
    }
    private AABB bounds() {
        AABB box=new AABB(cells.getFirst().pos);
        for(Cell cell:cells) box=box.minmax(new AABB(cell.pos));
        return box;
    }
    private List<Entity> occupants(AABB box) {
        return level.getEntities((Entity)null,box,e->!e.isSpectator() && e.isAlive() && !e.noPhysics && !(e instanceof SpellAnchor) && !(e instanceof Display));
    }
    private SpellBlockDisplay display(Cell cell,float offset) {
        BlockPos pos=cell.pos;
        var display=(SpellBlockDisplay)SpellEntities.BLOCK_DISPLAY.get().create(level);
        Objects.requireNonNull(display).setPos(pos.getX(),pos.getY(),pos.getZ());display.appearance(cell.state,offset);
        level.addFreshEntity(display);return display;
    }
    private void particles(double x,double y,double z,int count) {
        level.sendParticles(water()?ParticleTypes.SPLASH:lift?ParticleTypes.SNOWFLAKE:ParticleTypes.HAPPY_VILLAGER,x,y,z,count,.55,.28,.55,.04);
        if(!water()) level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,material),x,y,z,count/2,.45,.2,.45,.08);
    }
    private void abandon(Cell cell) { cell.abandoned=true;cell.placed=false;discard(cell); }
    private void discard(Cell cell) { if(cell.display!=null) { cell.display.discard();cell.display=null; } }
}
