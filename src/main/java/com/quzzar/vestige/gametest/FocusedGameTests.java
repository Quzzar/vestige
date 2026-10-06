package com.quzzar.vestige.gametest;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;

/** Optional development-only selector; ordinary runs retain the full native suite. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,bus=EventBusSubscriber.Bus.MOD)
public final class FocusedGameTests {
    private FocusedGameTests() { }
    @SubscribeEvent public static void select(FMLLoadCompleteEvent event) {
        String filter=System.getProperty("vestige.gametest.filter","");
        if(filter.isBlank() || !Boolean.getBoolean("neoforge.gameTestServer"))return;
        var pattern=java.util.regex.Pattern.compile(filter);
        event.enqueueWork(()->{
            GameTestHooks.registerGametests();
            var tests=GameTestRegistry.getAllTestFunctions();
            tests.removeIf(test->!pattern.matcher(test.batchName()+"/"+test.testName()).find());
            if(tests.isEmpty())throw new IllegalArgumentException("No native tests matched: "+filter);
        });
    }
}
