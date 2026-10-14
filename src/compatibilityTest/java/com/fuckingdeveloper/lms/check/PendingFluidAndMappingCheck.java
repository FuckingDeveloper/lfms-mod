package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.mapping.ProguardMappingReader;
import com.fuckingdeveloper.lms.runtime.Target263FluidModelLookupBridge;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.material.Fluids;
import java.io.StringReader;
import java.util.*;

public final class PendingFluidAndMappingCheck {
    public static void run() throws Exception {
        // Forces application of the exact target mixin, including local-map capture.
        Class.forName("net.minecraft.client.resources.model.ModelManager");
        var model=new FluidModel(ChunkSectionLayer.SOLID,null,null,null,(net.neoforged.neoforge.client.fluid.FluidTintSource)null);
        check(Target263FluidModelLookupBridge.current(Fluids.WATER)==null,"pending lookup leaked before scope");
        try(var scope=Target263FluidModelLookupBridge.enter(Map.of(Fluids.WATER,model))) {
            check(Target263FluidModelLookupBridge.current(Fluids.WATER)==model,"pending lookup did not retain native identity");
            try(var nested=Target263FluidModelLookupBridge.enter(Map.of())) {
                try{Target263FluidModelLookupBridge.current(Fluids.WATER);throw new AssertionError("missing model manufactured");}catch(IllegalStateException expected){}
            }
            check(Target263FluidModelLookupBridge.current(Fluids.WATER)==model,"nested pending scope did not restore");
        }
        check(Target263FluidModelLookupBridge.current(Fluids.WATER)==null,"pending lookup leaked after scope");
        var reader=new ProguardMappingReader();
        var index=reader.read(new StringReader("fixture.Source -> a:\n    int count -> b\n    void first(int) -> c\n    void second(java.lang.String) -> c\n"));
        check(index.findByObfuscated("a","c").size()==2,"overload candidates lost");
        check(index.findFieldByObfuscated("a","b").size()==1,"field candidate lost");
        check(index.findByObfuscated("a","unknown").isEmpty(),"unknown method invented");
        var expected=index.namedMethods().values().stream().filter(m->m.obfuscatedOwner().equals("a")&&m.obfuscatedName().equals("c")).distinct().toList();
        for(int i=0;i<10000;i++)check(index.findByObfuscated("a","c").equals(expected),"indexed method ordering changed");
        var other=reader.read(new StringReader("fixture.Other -> a:\n    void third() -> c\n"));
        check(other.findByObfuscated("a","c").size()==1&&index.findByObfuscated("a","c").size()==2,"index instances share candidates");
        System.out.println("PASS: exact pending fluid-model scope/absence/restoration and instance-owned method/field indexes");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
