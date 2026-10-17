package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceTextureAtlasHolder;
import com.google.gson.JsonParser;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.*;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

/** Headless resource/dispatch/access gates; actual stitching/upload belongs to the fresh client. */
public final class SourceOwnedAtlasCheck {
    public static Stream<Identifier> resources(){return Stream.of(Identifier.parse("atlas_fixture:button"));}
    static void run() throws Exception {
        var f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);var unsafe=(sun.misc.Unsafe)f.get(null);
        var path=Files.createTempFile("lms-owned-atlas", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry("probe/OwnedAtlas.class"));jar.write(fixture());jar.closeEntry();}
            byte[] original=Files.readAllBytes(path);
            try(var loader=new ManagedLegacyClassLoader(path,SourceOwnedAtlasCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var source=loader.linkOwnedClass("probe.OwnedAtlas");var holder=(Target263SourceTextureAtlasHolder)unsafe.allocateInstance(source);
                var atlas=(TextureAtlas)unsafe.allocateInstance(TextureAtlas.class);var sprite=(TextureAtlasSprite)unsafe.allocateInstance(TextureAtlasSprite.class);
                var textures=TextureAtlas.class.getDeclaredField("texturesByName");textures.setAccessible(true);textures.set(atlas,Map.of(Identifier.parse("atlas_fixture:gui/button"),sprite));
                set(holder,"atlas",atlas);set(holder,"prefix","gui");
                check(source.getMethod("getSprite",Identifier.class).invoke(holder,Identifier.parse("atlas_fixture:button"))==sprite,"source super lookup/prefix/native sprite identity");
                var method=source.getDeclaredMethod("getResourcesToLoad");method.setAccessible(true);
                try(var stream=(Stream<?>)method.invoke(holder)){check(stream.toList().equals(List.of(Identifier.parse("atlas_fixture:button"))),"source enumeration callback lost");}
                var minecraft=(Minecraft)unsafe.allocateInstance(Minecraft.class);var manager=(TextureManager)unsafe.allocateInstance(TextureManager.class);
                var service=Minecraft.class.getDeclaredField("textureManager");check(java.lang.reflect.Modifier.isPublic(service.getModifiers())&&java.lang.reflect.Modifier.isFinal(service.getModifiers()),"retained native client field flags");service.setAccessible(true);service.set(minecraft,manager);
                check(source.getMethod("textureService",Minecraft.class).invoke(null,minecraft)==manager,"source field read changed native identity");
                check(Arrays.equals(original,Files.readAllBytes(path)),"source artifact changed");
            }
        }finally{Files.deleteIfExists(path);}
        Identifier atlas=Identifier.parse("atlas_fixture:owned"),image=Identifier.parse("atlas_fixture:gui/button");
        var overlay=Target263SourceTextureAtlasHolder.definition(ResourceManager.Empty.INSTANCE,atlas,List.of(image));
        var definitions=overlay.getResourceStack(Identifier.parse("atlas_fixture:atlases/owned.json"));
        check(definitions.size()==1&&definitions.getFirst().sourcePackId().equals("lms/source-owned-atlas"),"reload-local definition provenance");
        try(var input=definitions.getFirst().openAsReader()){
            var entries=JsonParser.parseReader(input).getAsJsonObject().getAsJsonArray("sources");
            check(entries.size()==1&&entries.get(0).getAsJsonObject().get("resource").getAsString().equals(image.toString()),"source single-file identity");
        }
        check(overlay.getResource(image).isEmpty()&&overlay.getResourceStack(image).isEmpty(),"image lookup fabricated by overlay");
        System.out.println("PASS: immutable source owned-atlas superclass/callback/super lookup, native sprite identity/prefix, retained final client service field and isolated native single-file definition; actual reload/upload requires client");
    }
    private static void set(Object holder,String field,Object value)throws Exception{var f=Target263SourceTextureAtlasHolder.class.getDeclaredField(field);f.setAccessible(true);f.set(holder,value);}
    private static byte[] fixture(){
        String name="probe/OwnedAtlas",base="net/minecraft/client/resources/TextureAtlasHolder",id="Lnet/minecraft/resources/ResourceLocation;",sprite="Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;";
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,name,null,base,null);
        var m=w.visitMethod(ACC_PROTECTED,"m_7535_","()Ljava/util/stream/Stream;",null,null);m.visitCode();m.visitMethodInsn(INVOKESTATIC,"com/fuckingdeveloper/lms/check/SourceOwnedAtlasCheck","resources","()Ljava/util/stream/Stream;",false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC,"m_118901_","("+id+")"+sprite,null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKESPECIAL,base,"m_118901_","("+id+")"+sprite,false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"textureService","(Lnet/minecraft/client/Minecraft;)Lnet/minecraft/client/renderer/texture/TextureManager;",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitFieldInsn(GETFIELD,"net/minecraft/client/Minecraft","f_90987_","Lnet/minecraft/client/renderer/texture/TextureManager;");m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        w.visitEnd();return w.toByteArray();
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
