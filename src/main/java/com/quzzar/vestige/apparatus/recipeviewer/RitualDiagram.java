package com.quzzar.vestige.apparatus.recipeviewer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Approved parchment illustrations with native item overlays and concealed spell offerings. */
public final class RitualDiagram {
    public static final int WIDTH=184, HEIGHT=150, OUTPUT_X=84, OUTPUT_Y=67;
    private static final int CENTER_X=92, CENTER_Y=75;
    private static final int INK=0xff503722, FAINT=0xffad946e;
    private static final ResourceLocation PAPER=texture("parchment"), PLINTH=texture("plinth"), SPELLSTONE=texture("spellstone");
    // Native GUI pixels: both circles and slots use these same symmetric coordinates.
    private static final int[][] FOUR={{92,28},{139,75},{92,122},{45,75}};
    private static final int[][] EIGHT={{92,41},{135,32},{126,75},{135,118},{92,109},{49,118},{58,75},{49,32}};
    private RitualDiagram() { }
    private static ResourceLocation texture(String name){return VestigeMainMod.location("textures/gui/ritual/"+name+".png");}
    private static int[] point(RitualDisplays.Entry recipe,int seat){return recipe.capacity()==4?FOUR[seat/2]:EIGHT[seat];}
    public static int x(RitualDisplays.Entry recipe,int seat){return point(recipe,seat)[0]-8;}
    public static int y(RitualDisplays.Entry recipe,int seat){return point(recipe,seat)[1]-8;}
    public static void draw(GuiGraphics g,RitualDisplays.Entry recipe) {
        RenderSystem.enableBlend();
        illustration(g,PAPER,CENTER_X,CENTER_Y,150);
        if(recipe.capacity()==4)circle(g,47,0);else {circle(g,34,0);circle(g,Math.hypot(43,43),Math.PI/4);}
        for(int seat:recipe.seats()) {
            var p=point(recipe,seat);double distance=Math.hypot(p[0]-CENTER_X,p[1]-CENTER_Y);
            double ux=(p[0]-CENTER_X)/distance,uy=(p[1]-CENTER_Y)/distance;
            double tx=CENTER_X+ux*14.8,ty=CENTER_Y+uy*14.8;
            stroke(g,p[0]-ux*14.5,p[1]-uy*14.5,tx,ty,INK);
            stroke(g,tx,ty,tx+ux*2.7-uy*1.6,ty+uy*2.7+ux*1.6,INK);
            stroke(g,tx,ty,tx+ux*2.7+uy*1.6,ty+uy*2.7-ux*1.6,INK);
        }
        for(int seat:recipe.seats()){var p=point(recipe,seat);illustration(g,PLINTH,p[0],p[1],34);}
        illustration(g,SPELLSTONE,CENTER_X,CENTER_Y,33);
        if(recipe.concealed()) {
            var font=Minecraft.getInstance().font;
            for(int seat:recipe.seats()){var p=point(recipe,seat);g.drawString(font,"?",p[0]-font.width("?")/2,p[1]-4,INK,false);}
        }
        g.flush();RenderSystem.disableBlend();
    }
    private static void illustration(GuiGraphics g,ResourceLocation texture,int x,int y,int size) {
        g.pose().pushPose();g.pose().translate(x-size/2f,y-size/2f,0);
        g.blit(texture,0,0,size,size,0f,0f,1254,1254,1254,1254);g.pose().popPose();
    }
    private static void circle(GuiGraphics g,double radius,double offset) {
        // Faint short dashes; each circle passes through its layer's Plinth centers.
        double step=2.2/radius,gap=1.3/radius;
        for(double a=0;a<Math.PI*2;a+=step+gap)
            stroke(g,CENTER_X+radius*Math.cos(a),CENTER_Y+radius*Math.sin(a),
                    CENTER_X+radius*Math.cos(a+step),CENTER_Y+radius*Math.sin(a+step),FAINT);
        for(int i=0;i<4;i++) {
            double a=offset+Math.PI/4+i*Math.PI/2,ux=Math.cos(a),uy=Math.sin(a);
            double x=CENTER_X+radius*ux,y=CENTER_Y+radius*uy;
            if(i%2==0) {
                int direction=i==0?1:-1;
                stroke(g,x+ux+uy*.7*direction,y+uy-ux*.7*direction,x-uy*1.1*direction,y+ux*1.1*direction,FAINT);
                stroke(g,x-uy*1.1*direction,y+ux*1.1*direction,x-ux*.7+uy*.25*direction,y-uy*.7-ux*.25*direction,FAINT);
            }else stroke(g,x-ux*.9,y-uy*.9,x+ux*.9,y+uy*.9,FAINT);
        }
    }
    private static void stroke(GuiGraphics g,double x1,double y1,double x2,double y2,int color) {
        int steps=(int)Math.ceil(Math.max(Math.abs(x2-x1),Math.abs(y2-y1)));
        for(int i=0;i<=steps;i++) {
            int x=(int)Math.round(x1+(x2-x1)*i/Math.max(1,steps)),y=(int)Math.round(y1+(y2-y1)*i/Math.max(1,steps));
            g.fill(x,y,x+1,y+1,color);
        }
    }
}
