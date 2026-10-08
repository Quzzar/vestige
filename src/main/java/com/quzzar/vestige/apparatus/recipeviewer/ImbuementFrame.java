package com.quzzar.vestige.apparatus.recipeviewer;

import java.util.ArrayList;
import java.util.List;

/** The approved Plinth rim, measured in the original 1254-pixel illustration. */
public final class ImbuementFrame {
    public static final int SOURCE_SIZE=1254, SIZE=34;
    private static final int[][] OUTER={{271,294},{342,294},{366,299},{469,299},{630,299},{787,299},{893,299},{914,294},{980,294},{991,305},{991,365},{990,387},{990,876},{990,900},{990,944},{980,958},{914,962},{893,958},{368,958},{346,965},{271,965},{260,950},{260,902},{267,878},{267,384},{259,358},{260,307}};
    private static final int[][] INNER={{357,340},{894,340},{900,355},{918,373},{943,379},{943,879},{916,891},{899,910},{895,921},{355,921},{347,908},{328,888},{314,879},{314,379},{330,370},{347,355}};
    private ImbuementFrame() { }
    public static boolean contains(double x,double y){return inside(OUTER,x,y) && !inside(INNER,x,y);}
    private static boolean inside(int[][] polygon,double x,double y) {
        boolean hit=false;
        for(int i=0,j=polygon.length-1;i<polygon.length;j=i++) {
            var a=polygon[i];var b=polygon[j];
            if((a[1]>y)!=(b[1]>y) && x<(b[0]-a[0])*(y-a[1])/(double)(b[1]-a[1])+a[0])hit=!hit;
        }
        return hit;
    }
    public record HitBox(int x,int y,int width) { }
    /** Native catalyst slots cover rim pixels only, reserving the offering's central hover area. */
    public static List<HitBox> hitBoxes(int offeringSize) {
        var boxes=new ArrayList<HitBox>();
        for(int y=0;y<SIZE;y++) {
            int start=-1;
            for(int x=0;x<=SIZE;x++) {
                boolean hit=x<SIZE && contains((x+.5)*SOURCE_SIZE/SIZE,(y+.5)*SOURCE_SIZE/SIZE)
                        && !(Math.abs(x+.5-SIZE/2.)<offeringSize/2. && Math.abs(y+.5-SIZE/2.)<offeringSize/2.);
                if(hit && start<0)start=x;
                if(!hit && start>=0){boxes.add(new HitBox(start,y,x-start));start=-1;}
            }
        }
        return List.copyOf(boxes);
    }
}
