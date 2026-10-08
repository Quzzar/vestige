package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Scroll slots are projections of one locked held staff; closing never drops their contents again. */
public final class StaffMenu extends AbstractContainerMenu {
    private final Inventory inventory;
    private final InteractionHand hand;
    private final int sourceIndex,capacity;
    private final SimpleContainer scrolls;
    private final DataSlot selected=DataSlot.standalone();
    private ItemStack expected;
    private boolean loading=true;
    private int firstVisible;

    public StaffMenu(int id,Inventory inventory,RegistryFriendlyByteBuf buffer) {
        this(id,inventory,buffer.readEnum(InteractionHand.class),ItemStack.STREAM_CODEC.decode(buffer),buffer.readVarInt());
    }
    public StaffMenu(int id,Inventory inventory,InteractionHand hand,ItemStack staff,int sourceIndex) {
        super(StaffSelection.MENU.get(),id);this.inventory=inventory;this.hand=hand;this.sourceIndex=sourceIndex;expected=staff.copy();
        var binding=StaffData.binding(staff).orElseThrow();capacity=binding.capacity();selected.set(binding.selected());addDataSlot(selected);
        scrolls=new SimpleContainer(capacity) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public void setChanged() { super.setChanged();if(!loading) persist(); }
        };
        for(int i=0;i<capacity;i++) {scrolls.setItem(i,StaffData.source(staff,i));addSlot(scrollSlot(i));}
        for(int y=0;y<3;y++) for(int x=0;x<9;x++) addSlot(playerSlot(x+y*9+9,8+x*18,inventoryTop()+y*18));
        for(int x=0;x<9;x++) addSlot(playerSlot(x,8+x*18,inventoryTop()+58));
        loading=false;
    }
    public int capacity() { return capacity; }
    public int visibleRows() { return Math.min(3,capacity); }
    public int inventoryTop() { return 46+visibleRows()*22; }
    public int firstVisible() { return firstVisible; }
    public int selected() { return selected.get(); }
    public Component heading() {
        var binding=StaffData.binding(expected).orElseThrow();var sources=new ArrayList<Optional<ScrollItems.Scroll>>();
        for(int i=0;i<capacity;i++) sources.add(ScrollItems.scroll(scrolls.getItem(i)));
        return SpellStaffItem.staffName(new StaffData.Binding(binding.affinity(),capacity,selected(),sources));
    }
    /** Client positions change, but every slot retains its server container index and underlying storage. */
    public void scrollTo(int first) {
        firstVisible=Math.clamp(first,0,capacity-visibleRows());
        for(int i=0;i<capacity;i++) {var slot=scrollSlot(i);slot.index=i;slots.set(i,slot);}
    }
    private Slot scrollSlot(int i) {
        return new Slot(scrolls,i,8,26+(i-firstVisible)*22) {
            @Override public boolean mayPlace(ItemStack stack) { return accepts(stack); }
            @Override public boolean isActive() { return !inventory.player.level().isClientSide || (i>=firstVisible && i<firstVisible+visibleRows()); }
        };
    }
    private Slot playerSlot(int i,int x,int y) {
        return new Slot(inventory,i,x,y) {
            @Override public boolean mayPickup(Player player) { return i!=sourceIndex; }
            @Override public boolean mayPlace(ItemStack stack) { return i!=sourceIndex; }
        };
    }
    private boolean accepts(ItemStack stack) {
        var source=ScrollItems.scroll(stack).orElse(null);if(source==null) return false;
        if(inventory.player.level().isClientSide) return SpellKnowledge.visible(source.spell());
        if(!SpellKnowledge.identified(inventory.player,source.spell())) return false;
        var spell=NativeMagic.spells().spells().get(source.spell());var binding=StaffData.binding(expected).orElseThrow();
        if(spell==null || !StaffData.accepts(binding.affinity(),spell)) return false;
        try {Spellshaping.compile(spell,source.augments(),source.modifiers(),source.shaping());return true;}
        catch(IllegalArgumentException invalid) {return false;}
    }
    @Override public boolean stillValid(Player player) {
        if(player.level().isClientSide) return true;
        return player==inventory.player && player.isAlive() && !player.isSpectator()
                && (hand!=InteractionHand.MAIN_HAND || inventory.selected==sourceIndex)
                && ItemStack.matches(expected,player.getItemInHand(hand));
    }
    private void persist() {
        var player=inventory.player;if(player.level().isClientSide || !stillValid(player)) return;
        var sources=new ArrayList<ItemStack>();for(int i=0;i<capacity;i++) sources.add(scrolls.getItem(i));
        expected=StaffData.withScrolls(expected,sources);player.setItemInHand(hand,expected.copy());inventory.setChanged();
    }
    @Override public void clicked(int slot,int button,ClickType type,Player player) {
        if(!stillValid(player)) {if(player instanceof ServerPlayer server) server.closeContainer();return;}
        // Hotbar/offhand swaps can bypass the source slot's own mayPickup check.
        if(type==ClickType.SWAP && button==sourceIndex) return;
        super.clicked(slot,button,type,player);
    }
    @Override public boolean clickMenuButton(Player player,int slot) {
        if(!stillValid(player) || slot<0 || slot>=capacity || scrolls.getItem(slot).isEmpty()) return false;
        selected.set(slot);
        if(!player.level().isClientSide) {expected=StaffData.select(expected,slot);player.setItemInHand(hand,expected.copy());inventory.setChanged();}
        broadcastChanges();return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(!stillValid(player) || index<0 || index>=slots.size()) return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        var source=slot.getItem();var original=source.copy();
        if(index<capacity) {
            if(!moveItemStackTo(source,capacity,slots.size(),true)) return ItemStack.EMPTY;
        } else {
            if(!accepts(source)) return ItemStack.EMPTY;
            int target=-1;
            if(scrolls.getItem(selected()).isEmpty()) target=selected();
            else for(int i=0;i<capacity;i++) if(scrolls.getItem(i).isEmpty()) {target=i;break;}
            if(target<0) return ItemStack.EMPTY;
            scrolls.setItem(target,source.split(1));
        }
        if(source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,source);return original;
    }
    @Override public void removed(Player player) {
        super.removed(player);StaffSelection.closed(player);
    }
}
