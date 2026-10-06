package com.quzzar.vestige;

import net.minecraft.resources.ResourceLocation;

public final class VestigeMainMod {
    public static final String MOD_ID = "vestige";
    public static final String MOD_NAME = "Vestige";
    private VestigeMainMod() { }
    public static ResourceLocation location(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
    public static ResourceLocation location(String namespace, String path) { return ResourceLocation.fromNamespaceAndPath(namespace, path); }
}
