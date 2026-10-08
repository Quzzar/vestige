package com.quzzar.vestige.apparatus;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.IntStream;

/** A reproducible visual abbreviation. Device identity always uses the complete key. */
public record AttunementMark(List<Rune> runes) {
    private static final ResourceLocation FONT=ResourceLocation.withDefaultNamespace("alt");
    private static final int[] COLORS={0xff9c85,0xffcf78,0xeeed91,0xa9e58c,0x81ddc3,0x83d9ef,0xa6b9ff,0xd4a3ff,0xf1a2d8,0xe8c7a3,0xb4d6ce,0xe4e4ec};
    public AttunementMark { runes=List.copyOf(runes); }
    public record Rune(char letter,int color) {
        public MutableComponent component() {
            return Component.literal(String.valueOf(letter)).setStyle(Style.EMPTY.withFont(FONT).withColor(color));
        }
    }
    public static AttunementMark fromKey(String key) {
        if(key==null || !key.matches("[0-9a-f]{64}"))throw new IllegalArgumentException("Invalid attunement display key");
        byte[] bytes=HexFormat.of().parseHex(key);
        return new AttunementMark(IntStream.range(0,4).mapToObj(i->new Rune(
                (char)('a'+Byte.toUnsignedInt(bytes[i*8])%26),COLORS[Byte.toUnsignedInt(bytes[i*8+3])%COLORS.length])).toList());
    }
    public MutableComponent component() {
        var result=Component.empty();
        for(int i=0;i<runes.size();i++) {
            if(i>0)result.append(" ");
            result.append(runes.get(i).component());
        }
        return result;
    }
    /** The same compact abbreviation used on a Shell's inventory corner. */
    public MutableComponent symbol() { return runes.getFirst().component(); }
}
