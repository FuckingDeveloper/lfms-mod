package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.render.*;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.Identifier;

final class AtlasMaterialFamilyCheck {
    static final class Sprite extends TextureAtlasSprite {
        Sprite(Identifier atlas,SpriteContents contents){super(atlas,contents,64,64,16,32,0);}
    }
    static void run() throws Exception {
        var atlas=Identifier.parse("fixture:textures/atlas/custom.png");var texture=Identifier.parse("fixture:material");
        try(var contents=new SpriteContents(texture,new FrameSize(16,16),new NativeImage(16,16,true))) {
            var sprite=new Sprite(atlas,contents);var lookups=new AtomicInteger();var factories=new AtomicInteger();
            var buffers=LegacyMultiBufferSource.recording(id->{check(id.atlasLocation().equals(atlas)&&id.texture().equals(texture),"material lost explicit source atlas/sprite identity");lookups.incrementAndGet();return sprite;});
            var material=new LegacyMaterial(atlas,texture);
            java.util.function.Function<Identifier,LegacyRenderType> factory=id->{check(id.equals(atlas),"material factory received sprite instead of atlas");factories.incrementAndGet();return LegacyRenderType.m_110452_(id);};
            material.buffer(buffers,factory).setUv(0.25F,0.75F);material.buffer(buffers,factory,false).setUv(0.5F,0.5F);
            check(lookups.get()==2&&factories.get()==1,"source material type cache/lookup timing changed");
            var geometry=buffers.finishRecording();var field=geometry.getClass().getDeclaredField("batches");field.setAccessible(true);
            var batches=(List<?>)field.get(geometry);check(batches.size()==1,"source material split native render type identity");
            var operations=batches.getFirst().getClass().getDeclaredMethod("operations");operations.setAccessible(true);
            var uv=new ArrayList<List<Float>>();
            var consumer=(VertexConsumer)java.lang.reflect.Proxy.newProxyInstance(AtlasMaterialFamilyCheck.class.getClassLoader(),new Class<?>[]{VertexConsumer.class},(proxy,method,args)-> {
                if(method.getName().equals("setUv")){uv.add(List.of((Float)args[0],(Float)args[1]));return proxy;}
                throw new AssertionError(method);
            });
            for(var operation:(List<java.util.function.Consumer<VertexConsumer>>)operations.invoke(batches.getFirst()))operation.accept(consumer);
            check(uv.equals(List.of(List.of(0.3125F,0.6875F),List.of(0.375F,0.625F))),"native sprite UV projection or immutable recording changed");
            try{material.buffer(LegacyMultiBufferSource.recording(),factory);throw new AssertionError("unscoped atlas lookup admitted");}catch(UnsupportedOperationException expected){}
            try{material.buffer(buffers,factory,true);throw new AssertionError("unproven foil rendering admitted");}catch(UnsupportedOperationException expected){}
        }
        System.out.println("PASS: native sprite/custom-atlas identity, native UV expansion, source render-type caching, immutable recorded UVs and unscoped/glint failure boundaries");
    }
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
}
