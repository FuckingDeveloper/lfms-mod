package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.Forge1192ItemBridge;
import net.minecraft.core.component.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;

final class EmptyStackDecodeCheck {
    static void run() {
        // Explicit isolated smoke holder defaults, not a real game component reload.
        if(!Items.AIR.builtInRegistryHolder().areComponentsBound())
            Items.AIR.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
        NativeDefaultComponentFixture.run(EmptyStackDecodeCheck::checks);
    }
    private static void checks() {
        for(int count:new int[]{0,-1,-128,256}) {
            var tag=new CompoundTag();tag.putString("id","minecraft:stone");tag.putInt("Count",count);
            var payload=new CompoundTag();payload.putInt("charge",17);tag.put("tag",payload);
            var before=tag.copy();var stack=Forge1192ItemBridge.of(tag);
            check(stack.isEmpty()&&stack.getCount()==0,"source signed-byte empty count rejected/changed: "+count);
            check(stack!=ItemStack.EMPTY&&Forge1192ItemBridge.getTag(stack)!=null
                    &&Forge1192ItemBridge.getTag(stack).getIntOr("charge",-1)==17,"source empty tag hidden at count="+count);
            var live=Forge1192ItemBridge.getTag(stack);live.putInt("charge",18);
            check(Forge1192ItemBridge.getTag(stack)==live,"empty source tag identity lost");
            stack.setCount(1);
            check(stack.get(DataComponents.CUSTOM_DATA).tag.getIntOr("charge",-1)==18,"empty source tag detached from native patch");
            stack.setCount(0);check(Forge1192ItemBridge.getTag(stack)==live,"native empty visibility invalidated owned source data");
            check(tag.equals(before),"source input mutated");
        }
        var missing=new CompoundTag();missing.putString("id","minecraft:stone");
        var missingPayload=new CompoundTag();missingPayload.putInt("charge",19);missing.put("tag",missingPayload);
        var missingStack=Forge1192ItemBridge.of(missing);
        check(missingStack.isEmpty(),"missing source count became target default1");
        check(Forge1192ItemBridge.getTag(missingStack).getIntOr("charge",-1)==19,"missing-count payload skipped target data fix");
        check(Forge1192ItemBridge.of(new CompoundTag())==ItemStack.EMPTY,"genuine empty-map encoding changed");
        var air=new CompoundTag();air.putString("id","minecraft:air");air.putByte("Count",(byte)1);
        check(Forge1192ItemBridge.of(air).isEmpty(),"source air identity rejected");
        var positive=new CompoundTag();positive.putString("id","minecraft:stone");positive.putByte("Count",(byte)3);
        var stack=Forge1192ItemBridge.of(positive);check(stack.is(Items.STONE)&&stack.getCount()==3,"ordinary native codec changed");
        positive.putInt("Count",257);check(Forge1192ItemBridge.of(positive).getCount()==1,"positive source numeric narrowing changed");
        var unknown=new CompoundTag();unknown.putString("id","empty_stack_check:unknown");unknown.putByte("Count",(byte)0);
        try{Forge1192ItemBridge.of(unknown);throw new AssertionError("unknown empty item silently discarded");}
        catch(IllegalStateException|IllegalArgumentException expected){check(expected.getMessage()!=null,"unknown item lacks diagnosis");}
        System.out.println("PASS: zero/negative/signed-byte/missing source stack counts and air, retained decoded custom data, immutable input, ordinary positive stack and unknown-item fail-fast");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
