package com.quzzar.vestige.apparatus.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import java.util.*;

/** Three fixed-height rows, real scroll slots and the ordinary player inventory. */
public final class StaffScreen extends AbstractContainerScreen<StaffMenu> {
    private final List<SpellButton> rows=new ArrayList<>();
    private boolean draggingScrollbar;
    public StaffScreen(StaffMenu menu,Inventory inventory,Component title) {
        super(menu,inventory,title);imageWidth=176;imageHeight=menu.inventoryTop()+86;inventoryLabelY=menu.inventoryTop()-11;
    }
    @EventBusSubscriber(modid=VestigeMainMod.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        private Registration() { }
        @SubscribeEvent public static void register(RegisterMenuScreensEvent event) {event.register(StaffSelection.MENU.get(),StaffScreen::new);}
    }
    @Override protected void init() {
        super.init();rows.clear();menu.scrollTo(menu.firstVisible());
        for(int i=0;i<menu.visibleRows();i++) {
            int row=i;rows.add(addRenderableWidget(new SpellButton(Button.builder(Component.empty(),button ->
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId,menu.firstVisible()+row))
                    .bounds(leftPos+30,topPos+24+i*22,130,20))));
        }
        refreshRows();
    }
    private void refreshRows() {
        for(int i=0;i<rows.size();i++) {
            int slot=menu.firstVisible()+i;var stack=menu.getSlot(slot).getItem();
            var full=ScrollItems.scroll(stack).map(SpellStaffItem::slotName).orElseGet(() -> Component.translatable("gui.vestige.staff.empty"));
            var button=rows.get(i);button.selected=slot==menu.selected() && !stack.isEmpty();
            button.setMessage(fit(full,button.selected ? 106 : 118));button.active=!stack.isEmpty();button.setTooltip(Tooltip.create(full));
        }
    }
    /** The active spell stays visibly inset, independent of hover or keyboard focus. */
    static final class SpellButton extends Button {
        private boolean selected;
        private SpellButton(Builder builder) { super(builder); }
        boolean selected() { return selected; }
        @Override protected void renderWidget(GuiGraphics graphics,int mouseX,int mouseY,float partialTick) {
            if(!selected) {super.renderWidget(graphics,mouseX,mouseY,partialTick);return;}
            int x=getX(),y=getY(),w=getWidth(),h=getHeight();
            graphics.fill(x,y,x+w,y+h,0xffeeeeee);
            graphics.fill(x+1,y+1,x+w-1,y+h-1,0xff353535);
            graphics.fill(x+2,y+2,x+w-2,y+h-2,isHoveredOrFocused() ? 0xff686868 : 0xff555555);
            graphics.fill(x+2,y+h-3,x+w-2,y+h-2,0xff8b8b8b);
            graphics.fill(x+w-3,y+2,x+w-2,y+h-2,0xff8b8b8b);
            // A full check rather than a bent white mark: this stays legible at Minecraft's small GUI scale.
            int shadow=0xff1f2a1e, mark=0xffbceaa9;
            graphics.fill(x+5,y+10,x+8,y+12,shadow);
            graphics.fill(x+7,y+12,x+10,y+14,shadow);
            graphics.fill(x+9,y+10,x+12,y+12,shadow);
            graphics.fill(x+11,y+8,x+14,y+10,shadow);
            graphics.fill(x+6,y+10,x+8,y+11,mark);
            graphics.fill(x+7,y+11,x+9,y+13,mark);
            graphics.fill(x+9,y+10,x+11,y+12,mark);
            graphics.fill(x+11,y+8,x+13,y+10,mark);
            var font=Minecraft.getInstance().font;
            graphics.drawString(font,getMessage(),x+18+(w-24-font.width(getMessage()))/2,y+(h-font.lineHeight)/2,0xffffff);
        }
    }
    private Component fit(Component text,int width) {
        if(font.width(text)<=width) return text;
        var clipped=Component.empty();
        font.substrByWidth(text,width-font.width("…")).visit((style,part) -> {
            clipped.append(Component.literal(part).withStyle(style));return Optional.empty();
        },Style.EMPTY);
        return clipped.append("…");
    }
    @Override public Component getTitle() { return menu.heading(); }
    @Override protected void containerTick() {super.containerTick();refreshRows();}
    @Override protected void renderBg(GuiGraphics graphics,float partialTick,int mouseX,int mouseY) {
        graphics.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff373737);
        graphics.fill(leftPos+1,topPos+1,leftPos+imageWidth-1,topPos+imageHeight-1,0xfff1f1f1);
        graphics.fill(leftPos+3,topPos+3,leftPos+imageWidth-3,topPos+imageHeight-3,0xffc6c6c6);
        for(var slot:menu.slots) if(slot.isActive()) {
            int x=leftPos+slot.x,y=topPos+slot.y;
            graphics.fill(x-1,y-1,x+17,y+17,0xff373737);graphics.fill(x,y,x+17,y+17,0xffffffff);graphics.fill(x,y,x+16,y+16,0xff8b8b8b);
        }
        if(menu.capacity()>menu.visibleRows()) {
            int x=leftPos+165,y=topPos+25,height=menu.visibleRows()*22;
            graphics.fill(x,y,x+4,y+height,0xff8b8b8b);
            int thumb=height*menu.visibleRows()/menu.capacity(),offset=(height-thumb)*menu.firstVisible()/(menu.capacity()-menu.visibleRows());
            graphics.fill(x,y+offset,x+4,y+offset+thumb,0xffeeeeee);
        }
    }
    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY) {
        var text=fit(menu.heading(),160);
        graphics.drawString(font,text,(imageWidth-font.width(text))/2,8,0x404040,false);
        graphics.drawString(font,playerInventoryTitle,8,inventoryLabelY,0x404040,false);
    }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick) {
        super.render(graphics,mouseX,mouseY,partialTick);renderTooltip(graphics,mouseX,mouseY);
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical) {
        if(isHovering(6,24,166,menu.visibleRows()*22,x,y) && vertical!=0) {
            menu.scrollTo(menu.firstVisible()+(vertical>0 ? -1 : 1));refreshRows();return true;
        }
        return super.mouseScrolled(x,y,horizontal,vertical);
    }
    private void scrollAt(double y) {
        double fraction=(y-topPos-25)/(menu.visibleRows()*22);
        menu.scrollTo((int)Math.round(fraction*(menu.capacity()-menu.visibleRows())));refreshRows();
    }
    @Override public boolean mouseClicked(double x,double y,int button) {
        if(button==0 && menu.capacity()>menu.visibleRows() && isHovering(164,24,7,menu.visibleRows()*22,x,y)) {
            draggingScrollbar=true;scrollAt(y);return true;
        }
        return super.mouseClicked(x,y,button);
    }
    @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy) {
        if(draggingScrollbar) {scrollAt(y);return true;}return super.mouseDragged(x,y,button,dx,dy);
    }
    @Override public boolean mouseReleased(double x,double y,int button) {
        draggingScrollbar=false;return super.mouseReleased(x,y,button);
    }
}
