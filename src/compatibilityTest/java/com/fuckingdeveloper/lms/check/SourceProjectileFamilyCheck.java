package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.*;
import com.fuckingdeveloper.lms.target.neoforge263.entity.Target263Forge1192ThrowableItemProjectile;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Real entity construction over a bounded level double, without claiming a running world or native flight. */
public final class SourceProjectileFamilyCheck implements Opcodes {
    private static final String NAME="fixture/SourceItemProjectile",BASE="net/minecraft/world/entity/projectile/ThrowableItemProjectile";
    static void run() throws Exception {
        var jarPath=Files.createTempFile("lms-source-projectile", ".jar");
        try(var jar=new java.util.jar.JarOutputStream(Files.newOutputStream(jarPath))){jar.putNextEntry(new java.util.jar.JarEntry(NAME+".class"));jar.write(fixture());jar.closeEntry();}
        var levelNode=new ClassNode();levelNode.version=V17;levelNode.access=ACC_PUBLIC;levelNode.name="fixture/ConstructorLevel";levelNode.superName="net/minecraft/client/multiplayer/ClientLevel";
        var id=new MethodNode(ACC_PUBLIC,"getNextEntityId","()I",null,null);id.instructions.add(new IntInsnNode(BIPUSH,17));id.instructions.add(new InsnNode(IRETURN));levelNode.methods.add(id);
        var levelWriter=new ClassWriter(ClassWriter.COMPUTE_MAXS);levelNode.accept(levelWriter);byte[] levelBytes=levelWriter.toByteArray();
        var levelClass=new ClassLoader(SourceProjectileFamilyCheck.class.getClassLoader()){Class<?> define(){return defineClass("fixture.ConstructorLevel",levelBytes,0,levelBytes.length);}}.define();
        var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
        var level=(Level)unsafe.allocateInstance(levelClass); // Constructor-only double: supplies identity, never ticks, chunks or world behavior.
        try(var loader=new ManagedLegacyClassLoader(jarPath,SourceProjectileFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass(NAME.replace('/','.'));
            check(type.getTypeParameters()[0].getBounds()[0]==ThrowableItemProjectile.class,"generic bound was incorrectly narrowed to construction adapter");
            NativeDefaultComponentFixture.run(()->{try{checks(type,level);}catch(Exception failure){throw new AssertionError(failure);}});
        }
        var tnt=Path.of("legacy-mods/tntexpansion-forge-1.19.2-3.0.0.jar");
        if(Files.exists(tnt))try(var loader=new ManagedLegacyClassLoader(tnt,SourceProjectileFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var actual=loader.linkOwnedClass("net.tonicarter.tntexpansion.entity.DynamiteEntity");
            check(actual.getSuperclass()==Target263Forge1192ThrowableItemProjectile.class,"immutable source base construction adapter");
            actual.getDeclaredMethod("getDefaultItem");actual.getDeclaredMethod("lms$sourceGravity");actual.getDeclaredMethod("onHit",HitResult.class);
        }
        System.out.println("PASS: real source entity constructors/position, delayed default callback, native ancestry, raw/default/count/custom tag/copy/native synced storage, float gravity, native hit dispatch, codec operands/empty-item/owner storage; native item roundtrip and flight need a loaded world");
    }
    private static void checks(Class<?> type,Level level) throws Exception {
        var entity=(ThrowableItemProjectile)type.getConstructor(EntityType.class,Level.class).newInstance(EntityTypes.SNOWBALL,level);
        check(type.getField("defaultCalls").getInt(entity)==0,"native constructor eagerly invoked source default-item callback");
        check(entity instanceof ThrowableProjectile&&entity.getType()==EntityTypes.SNOWBALL,"native type/ancestry changed");
        check(entity.getGravity()==(double)0.07F,"source float gravity override lost");
        check(type.getMethod("baseGravity").invoke(entity).equals(0.03F),"source super gravity semantics lost");
        check(entity.getItem().is(Items.SNOWBALL)&&((ItemStack)type.getMethod("raw").invoke(entity)).isEmpty(),"source lazy fallback/raw identity");
        var input=Forge1192EarlyItemStackBridge.create(Items.SNOWBALL,8);Forge1192ItemBridge.getOrCreateTag(input).putInt("fixture",37);entity.setItem(input);
        var raw=(ItemStack)type.getMethod("raw").invoke(entity);
        check(raw!=input&&raw.getCount()==1&&input.getCount()==8&&Forge1192ItemBridge.getTag(raw).getIntOr("fixture",0)==37,"source setItem copy/count/custom tag");
        entity.setItem(Forge1192EarlyItemStackBridge.create(Items.SNOWBALL,4));
        check(type.getMethod("raw").invoke(entity)==raw,"untagged default unexpectedly replaced stored source item");
        var positioned=(ThrowableItemProjectile)type.getConstructor(EntityType.class,double.class,double.class,double.class,Level.class).newInstance(EntityTypes.SNOWBALL,3.25,72.0,-4.5,level);
        check(positioned.getX()==3.25&&positioned.getY()==72&&positioned.getZ()==-4.5&&positioned.level()==level,"coordinate constructor changed operands");
        // A bounded owner double avoids manufacturing the loader's not-yet-loaded attribute registry.
        var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
        var owner=(ArmorStand)unsafe.allocateInstance(ArmorStand.class);
        var position=Entity.class.getDeclaredField("position");position.setAccessible(true);position.set(owner,new Vec3(8,70,9));
        var eye=Entity.class.getDeclaredField("eyeHeight");eye.setAccessible(true);eye.setFloat(owner,1.7F);owner.setUUID(UUID.fromString("c3a2e784-9f33-4f94-a038-66926b4e9c00"));
        var owned=(ThrowableItemProjectile)type.getConstructor(EntityType.class,LivingEntity.class,Level.class).newInstance(EntityTypes.SNOWBALL,owner,level);
        check(owned.getOwner()==owner&&owned.getY()==owner.getEyeY()-0.1F&&owned.getX()==8&&owned.getZ()==9,"source owner constructor changed owner/eye offset");
        var hit=Projectile.class.getDeclaredMethod("hitTargetOrDeflectSelf",HitResult.class);hit.setAccessible(true);
        HitResult miss=new HitResult(Vec3.ZERO){public Type getType(){return Type.MISS;}};hit.invoke(entity,miss);
        check(type.getField("hitCalls").getInt(entity)==1,"native collision dispatch lost source callback");
        var lookup=RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var save=Target263Forge1192ThrowableItemProjectile.class.getDeclaredMethod("addAdditionalSaveData",ValueOutput.class);save.setAccessible(true);
        var stored=new HashMap<String,Object>();
        var recording=(ValueOutput)java.lang.reflect.Proxy.newProxyInstance(SourceProjectileFamilyCheck.class.getClassLoader(),new Class[]{ValueOutput.class},(proxy,method,args)->{
            if(method.getName().equals("store")&&args.length==3){
                check(args[1]==ItemStack.CODEC,"item storage selected a different codec");stored.put((String)args[0],args[2]);return null;
            }
            if(method.getName().equals("putBoolean")){stored.put((String)args[0],args[1]);return null;}
            throw new AssertionError("Unexpected parent storage operation: "+method);
        });
        save.invoke(entity,recording);
        check(stored.get("Item")==raw,"source save lost exact nonempty raw item/codec operands");
        // Native item codecs require the completed data-load/component lifecycle; no global holder binding is fabricated in a smoke test.
        var empty=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,lookup);int calls=type.getField("defaultCalls").getInt(positioned);save.invoke(positioned,empty);
        check(!empty.buildResult().contains("Item")&&type.getField("defaultCalls").getInt(positioned)==calls,"save eagerly materialized source default item");
        var load=Target263Forge1192ThrowableItemProjectile.class.getDeclaredMethod("readAdditionalSaveData",ValueInput.class);load.setAccessible(true);
        load.invoke(entity,TagValueInput.create(ProblemReporter.DISCARDING,lookup,new CompoundTag()));
        check(((ItemStack)type.getMethod("raw").invoke(entity)).isEmpty()&&entity.getItem().is(Items.SNOWBALL),"missing item load did not retain source empty/default semantics");
        var ownerOutput=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,lookup);save.invoke(owned,ownerOutput);
        check(ownerOutput.buildResult().contains("Owner"),"native projectile parent owner storage omitted");
    }
    private static byte[] fixture() {
        var n=new ClassNode();n.version=V17;n.access=ACC_PUBLIC;n.name=NAME;n.superName=BASE;n.signature="<T:L"+BASE+";>L"+BASE+";";
        for(String field:List.of("defaultCalls","hitCalls"))n.fields.add(new FieldNode(ACC_PUBLIC,field,"I",null,null));
        n.fields.add(new FieldNode(ACC_PUBLIC,"readyItem","Lnet/minecraft/world/item/Item;",null,null));
        for(String desc:List.of("(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V","(Lnet/minecraft/world/entity/EntityType;DDDLnet/minecraft/world/level/Level;)V","(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V")) {
            var m=new MethodNode(ACC_PUBLIC,"<init>",desc,null,null);m.instructions.add(new VarInsnNode(ALOAD,0));int local=1;
            for(org.objectweb.asm.Type arg:org.objectweb.asm.Type.getArgumentTypes(desc)){m.instructions.add(new VarInsnNode(arg.getOpcode(ILOAD),local));local+=arg.getSize();}
            m.instructions.add(new MethodInsnNode(INVOKESPECIAL,BASE,"<init>",desc,false));m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new FieldInsnNode(GETSTATIC,"net/minecraft/world/item/Items","SNOWBALL","Lnet/minecraft/world/item/Item;"));m.instructions.add(new FieldInsnNode(PUTFIELD,NAME,"readyItem","Lnet/minecraft/world/item/Item;"));m.instructions.add(new InsnNode(RETURN));n.methods.add(m);
        }
        var item=new MethodNode(ACC_PROTECTED,"m_7881_","()Lnet/minecraft/world/item/Item;",null,null);increment(item,"defaultCalls");item.instructions.add(new VarInsnNode(ALOAD,0));item.instructions.add(new FieldInsnNode(GETFIELD,NAME,"readyItem","Lnet/minecraft/world/item/Item;"));item.instructions.add(new MethodInsnNode(INVOKESTATIC,"java/util/Objects","requireNonNull","(Ljava/lang/Object;)Ljava/lang/Object;",false));item.instructions.add(new TypeInsnNode(CHECKCAST,"net/minecraft/world/item/Item"));item.instructions.add(new InsnNode(ARETURN));n.methods.add(item);
        var gravity=new MethodNode(ACC_PROTECTED,"m_7139_","()F",null,null);gravity.instructions.add(new LdcInsnNode(0.07F));gravity.instructions.add(new InsnNode(FRETURN));n.methods.add(gravity);
        var base=new MethodNode(ACC_PUBLIC,"baseGravity","()F",null,null);base.instructions.add(new VarInsnNode(ALOAD,0));base.instructions.add(new MethodInsnNode(INVOKESPECIAL,BASE,"m_7139_","()F",false));base.instructions.add(new InsnNode(FRETURN));n.methods.add(base);
        var raw=new MethodNode(ACC_PUBLIC,"raw","()Lnet/minecraft/world/item/ItemStack;",null,null);raw.instructions.add(new VarInsnNode(ALOAD,0));raw.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,BASE,"getItemRaw","()Lnet/minecraft/world/item/ItemStack;",false));raw.instructions.add(new InsnNode(ARETURN));n.methods.add(raw);
        var hit=new MethodNode(ACC_PROTECTED,"m_6532_","(Lnet/minecraft/world/phys/HitResult;)V",null,null);increment(hit,"hitCalls");hit.instructions.add(new InsnNode(RETURN));n.methods.add(hit);
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(writer);return writer.toByteArray();
    }
    private static void increment(MethodNode method,String field){method.instructions.add(new VarInsnNode(ALOAD,0));method.instructions.add(new InsnNode(DUP));method.instructions.add(new FieldInsnNode(GETFIELD,NAME,field,"I"));method.instructions.add(new InsnNode(ICONST_1));method.instructions.add(new InsnNode(IADD));method.instructions.add(new FieldInsnNode(PUTFIELD,NAME,field,"I"));}
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
