package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.*;
import java.util.*;

/** Native container IDs, slots and carried items own selection and scroll transfers. */
public final class StaffSelection {
    public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,VestigeMainMod.MOD_ID);
    public static final DeferredHolder<MenuType<?>,MenuType<StaffMenu>> MENU=MENUS.register("staff",() -> IMenuTypeExtension.create(StaffMenu::new));
    private static final Set<ServerPlayer> OPEN=Collections.newSetFromMap(new IdentityHashMap<>());
    private StaffSelection() { }
    public static OptionalInt open(ServerPlayer player,InteractionHand hand) {
        var staff=player.getItemInHand(hand);var binding=StaffData.binding(staff).orElse(null);
        if(!player.isAlive() || player.isSpectator() || binding==null) return OptionalInt.empty();
        var snapshot=staff.copy();int source=hand==InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
        var result=player.openMenu(new SimpleMenuProvider((id,inventory,p) -> new StaffMenu(id,inventory,hand,snapshot,source),
                SpellStaffItem.staffName(binding)),buffer -> {
            buffer.writeEnum(hand);ItemStack.STREAM_CODEC.encode(buffer,snapshot);buffer.writeVarInt(source);
        });
        if(result.isPresent()) OPEN.add(player);return result;
    }
    static void closed(net.minecraft.world.entity.player.Player player) { OPEN.remove(player); }
    public static void cancelAll() {
        for(var player:List.copyOf(OPEN)) if(player.containerMenu instanceof StaffMenu) player.closeContainer();
        OPEN.clear();
    }
}
