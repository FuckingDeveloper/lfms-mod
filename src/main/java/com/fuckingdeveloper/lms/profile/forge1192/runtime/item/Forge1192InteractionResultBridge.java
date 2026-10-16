package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import net.minecraft.world.InteractionResult;
import java.util.Objects;

/** Source enum information/branching over native results; not a replacement Java Enum. */
public final class Forge1192InteractionResultBridge {
    public static final InteractionResult CONSUME_PARTIAL = InteractionResult.CONSUME.withoutItem();
    private static final InteractionResult[] VALUES = {InteractionResult.SUCCESS, InteractionResult.CONSUME, CONSUME_PARTIAL, InteractionResult.PASS, InteractionResult.FAIL};
    private static final String[] NAMES = {"SUCCESS","CONSUME","CONSUME_PARTIAL","PASS","FAIL"};
    private Forge1192InteractionResultBridge() {}
    public static InteractionResult[] values() { return VALUES.clone(); }
    public static int ordinal(InteractionResult result) {
        Objects.requireNonNull(result,"result");
        if (result instanceof InteractionResult.Success success) {
            if (success.swingSource()!=InteractionResult.SwingSource.NONE) return 0;
            return success.wasItemInteraction()?1:2;
        }
        if (result instanceof InteractionResult.Pass) return 3;
        if (result instanceof InteractionResult.Fail) return 4;
        throw new UnsupportedOperationException("Native interaction result has no source enum category: " + result);
    }
    public static String name(InteractionResult result) { return NAMES[ordinal(result)]; }
    public static InteractionResult valueOf(String name) {
        Objects.requireNonNull(name,"Name is null");
        for (int i=0;i<NAMES.length;i++) if (NAMES[i].equals(name)) return VALUES[i];
        throw new IllegalArgumentException("No enum constant net.minecraft.world.InteractionResult." + name);
    }
    public static boolean consumesAction(InteractionResult result) { return ordinal(result)<=2; }
    public static boolean shouldSwing(InteractionResult result) { return ordinal(result)==0; }
    public static boolean shouldAwardStats(InteractionResult result) { return ordinal(result)<=1; }
    public static InteractionResult sidedSuccess(boolean client) { return client?InteractionResult.SUCCESS:InteractionResult.CONSUME; }
}
