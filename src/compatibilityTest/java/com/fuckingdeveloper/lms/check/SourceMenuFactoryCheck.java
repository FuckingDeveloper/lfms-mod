package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.menu.*;
import io.netty.buffer.Unpooled;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.*;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;

public final class SourceMenuFactoryCheck {
    public static final class Probe extends AbstractContainerMenu {
        public final Inventory inventory; public final FriendlyByteBuf data;
        public Probe(int id,Inventory inventory,FriendlyByteBuf data){super(null,id);this.inventory=inventory;this.data=data;}
        public boolean stillValid(Player player){return true;}
        public net.minecraft.world.item.ItemStack quickMoveStack(Player player,int slot){return net.minecraft.world.item.ItemStack.EMPTY;}
    }
    public static final class Fixture {
        public static Forge1192ContainerFactory<Probe> factory(){return Probe::new;}
        public static MenuType<Probe> build(Forge1192ContainerFactory<Probe> factory){return Forge1192MenuType.create(factory);}
        public static Probe source(MenuType<Probe> menu,int id,Inventory inventory,FriendlyByteBuf data){return ((Forge1192MenuType<Probe>)(Object)menu).create(id,inventory,data);}
        public static BiFunction<Integer,Inventory,Probe> bound(Forge1192ContainerFactory<Probe> factory){return factory::create;}
    }
    static void run() throws Exception {
        String original=Fixture.class.getName().replace('.','/'),name="fixture/SourceMenus";
        byte[] bytes;try(var in=SourceMenuFactoryCheck.class.getResourceAsStream("SourceMenuFactoryCheck$Fixture.class")){bytes=in.readAllBytes();}
        var writer=new ClassWriter(0);new ClassReader(bytes).accept(new ClassRemapper(writer,new Remapper(){
            public String map(String owner){if(owner.equals(original))return name;return switch(owner){
                case "com/fuckingdeveloper/lms/profile/forge1192/runtime/menu/Forge1192ContainerFactory"->"net/minecraftforge/network/IContainerFactory";
                case "com/fuckingdeveloper/lms/profile/forge1192/runtime/menu/Forge1192MenuType"->"net/minecraftforge/common/extensions/IForgeMenuType";default->owner;};}
        }),0);
        var node=new ClassNode();new ClassReader(writer.toByteArray()).accept(node,0);
        String source="net/minecraftforge/common/extensions/IForgeMenuType",desc="(Lnet/minecraftforge/network/IContainerFactory;)Lnet/minecraft/world/inventory/MenuType;";
        var handle=new Handle(Opcodes.H_INVOKESTATIC,source,"create",desc,true);
        var bootstrap=new Handle(Opcodes.H_INVOKESTATIC,"com/fuckingdeveloper/lms/check/SourceMenuFactoryCheck","constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/invoke/MethodHandle;)Ljava/lang/invoke/MethodHandle;",false);
        for(boolean nested:List.of(false,true)){
            var m=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,nested?"nested":"handle","()Ljava/lang/invoke/MethodHandle;",null,null);
            m.instructions.add(new LdcInsnNode(nested?new ConstantDynamic("menu","Ljava/lang/invoke/MethodHandle;",bootstrap,handle):handle));m.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(m);
        }
        var output=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(output);var jarPath=Files.createTempFile("lms-source-menu", ".jar");
        try(var jar=new java.util.jar.JarOutputStream(Files.newOutputStream(jarPath))){jar.putNextEntry(new java.util.jar.JarEntry(name+".class"));jar.write(output.toByteArray());jar.closeEntry();}
        var inventory=new Inventory(null,new EntityEquipment());
        var raw=new FriendlyByteBuf(Unpooled.buffer());raw.writeInt(12345);int before=raw.readerIndex();
        var registry=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));registry.writeInt(6789);
        try(var loader=new ManagedLegacyClassLoader(jarPath,SourceMenuFactoryCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass(name.replace('/','.'));
            @SuppressWarnings("unchecked") var factory=(Forge1192ContainerFactory<Probe>)type.getMethod("factory").invoke(null);
            @SuppressWarnings("unchecked") var menu=(MenuType<Probe>)type.getMethod("build",Forge1192ContainerFactory.class).invoke(null,factory);
            check(menu.requiredFeatures().equals(FeatureFlags.VANILLA_SET),"source constructor feature flags");
            Probe result=menu.create(4,inventory,registry);check(result.containerId==4&&result.inventory==inventory&&result.data==registry,"native registry buffer was replaced");
            result=(Probe)type.getMethod("source",MenuType.class,int.class,Inventory.class,FriendlyByteBuf.class).invoke(null,menu,5,inventory,raw);
            check(result.containerId==5&&result.inventory==inventory&&result.data==raw&&raw.readerIndex()==before&&raw.refCnt()==1,"source raw buffer identity/lifetime");
            check(menu.create(6,inventory).data==null,"two-argument source factory lost null contract");
            @SuppressWarnings("unchecked") var bound=(BiFunction<Integer,Inventory,Probe>)type.getMethod("bound",Forge1192ContainerFactory.class).invoke(null,factory);
            check(bound.apply(7,inventory).data==null,"bound default method reference");
            for(String method:List.of("handle","nested"))try{
                var callable=(MethodHandle)type.getMethod(method).invoke(null);var other=(MenuType<?>)callable.invoke(factory);
                check(other instanceof Forge1192MenuType<?>,"static/nested handle did not return native source-view menu");
            }catch(Throwable failure){throw new AssertionError(failure);}
            var nativeOnly=net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create((int id,Inventory inv,RegistryFriendlyByteBuf data)->new Probe(id,inv,data));
            var sourceView=(Forge1192MenuType<?>)(Object)nativeOnly;
            check(((Probe)sourceView.create(8,inventory,registry)).data==registry,"native factory source-view identity");
            try{sourceView.create(8,inventory,raw);throw new AssertionError("invented native registry context");}catch(UnsupportedOperationException expected){}
            var vanilla=new MenuType<Probe>((id,inv)->new Probe(id,inv,null),FeatureFlags.DEFAULT_FLAGS);
            check(((Probe)((Forge1192MenuType<?>)(Object)vanilla).create(9,inventory,raw)).data==null,"vanilla fallback changed source behavior");
            var failure=new IllegalArgumentException("source-menu-callback");var failing=Forge1192MenuType.create((Forge1192ContainerFactory<Probe>)(id,inv,data)->{throw failure;});
            try{failing.create(10,inventory,registry);throw new AssertionError("callback failure swallowed");}catch(IllegalArgumentException expected){check(expected==failure,"callback failure replaced");}
        }finally{raw.release();registry.release();}
        System.out.println("PASS: actual native menu/source interface, source lambda and default method reference, raw/registry/null buffer identity, nested static handles, feature flags, vanilla/native fallback and callback failure; opening packets/screens require runtime");
    }
    public static MethodHandle constant(MethodHandles.Lookup lookup,String name,Class<?> type,MethodHandle handle){return handle;}
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
