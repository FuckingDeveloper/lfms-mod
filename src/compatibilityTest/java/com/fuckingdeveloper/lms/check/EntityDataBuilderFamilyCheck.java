package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Native builder and storage, source-generated callbacks; no world simulation is claimed. */
final class EntityDataBuilderFamilyCheck {
    private static final String BASE="net/minecraft/world/entity/item/PrimedTnt", DATA="net/minecraft/network/syncher/SynchedEntityData";
    private static final String ACCESS="Lnet/minecraft/network/syncher/EntityDataAccessor;";
    static void run() throws Exception {
        var parent=node("probe/SourceTrackedParent",BASE);var child=node("probe/SourceTrackedChild",parent.name);
        callback(parent,BASE,31);callback(child,parent.name,47);
        parent.fields.add(new FieldNode(ACC_PUBLIC,"blasts","I",null,null));
        var blast=new MethodNode(ACC_PROTECTED,"m_32103_","()V",null,null);
        blast.instructions.add(new VarInsnNode(ALOAD,0));blast.instructions.add(new InsnNode(DUP));blast.instructions.add(new FieldInsnNode(GETFIELD,parent.name,"blasts","I"));blast.instructions.add(new InsnNode(ICONST_1));blast.instructions.add(new InsnNode(IADD));blast.instructions.add(new FieldInsnNode(PUTFIELD,parent.name,"blasts","I"));blast.instructions.add(new InsnNode(RETURN));parent.methods.add(blast);
        positionMethods(parent);
        var escaped=node("probe/EscapedTracked",BASE);callback(escaped,BASE,4);
        var escape=escaped.methods.get(2); // callback after static initialization
        for(var insn:escape.instructions.toArray())if(insn instanceof MethodInsnNode call&&call.owner.equals(DATA)) {
            call.name="m_135370_";call.desc="("+ACCESS+")Ljava/lang/Object;";
            // Shape is verifier-valid but unsupported: consume the value, read the data.
            escape.instructions.insertBefore(call,new InsnNode(POP));escape.instructions.insert(call,new InsnNode(POP));
        }
        var named=node("probe/NamedTracked",BASE);callback(named,BASE,59);named.methods.get(2).name="defineSynchedData";
        for(var insn:named.methods.get(2).instructions) {
            if(insn instanceof FieldInsnNode field&&field.name.equals("f_19804_"))field.name="entityData";
            if(insn instanceof MethodInsnNode call&&call.owner.equals(DATA))call.name="define";
        }
        var duplicate=node("probe/DuplicateTracked",BASE);callback(duplicate,BASE,6);
        var definition=duplicate.methods.get(2);var tail=new InsnList();boolean copying=false;
        for(var insn:definition.instructions.toArray()) {
            if(insn instanceof FieldInsnNode field&&field.name.equals("f_19804_")) {copying=true;tail.add(new VarInsnNode(ALOAD,0));}
            if(copying)tail.add(insn.clone(new HashMap<>()));
            if(copying&&insn instanceof MethodInsnNode call&&call.owner.equals(DATA))break;
        }
        definition.instructions.insertBefore(definition.instructions.getLast(),tail);
        var f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);var unsafe=(sun.misc.Unsafe)f.get(null);
        var path=Files.createTempFile("lms-entity-data-", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){for(var n:List.of(parent,child,escaped,named,duplicate)){jar.putNextEntry(new JarEntry(n.name+".class"));jar.write(bytes(n));jar.closeEntry();}}
            byte[] original=Files.readAllBytes(path);
            try(var loader=new ManagedLegacyClassLoader(path,EntityDataBuilderFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var levelNode=node("probe/DataConstructorLevel","net/minecraft/client/multiplayer/ClientLevel");
                var nextId=new MethodNode(ACC_PUBLIC,"getNextEntityId","()I",null,null);nextId.instructions.add(new InsnNode(ICONST_1));nextId.instructions.add(new InsnNode(IRETURN));levelNode.methods.add(nextId);
                byte[] levelBytes=bytes(levelNode);
                var levelType=new ClassLoader(EntityDataBuilderFamilyCheck.class.getClassLoader()){Class<?> define(){return defineClass("probe.DataConstructorLevel",levelBytes,0,levelBytes.length);}}.define();
                var level=(net.minecraft.world.level.Level)unsafe.allocateInstance(levelType);
                var type=loader.linkOwnedClass("probe.SourceTrackedChild");
                var entity=(PrimedTnt)type.getConstructor(net.minecraft.world.entity.EntityType.class,net.minecraft.world.level.Level.class)
                        .newInstance(net.minecraft.world.entity.EntityTypes.TNT,level);
                var namedType=loader.linkOwnedClass("probe.NamedTracked");
                var namedEntity=(PrimedTnt)namedType.getConstructor(net.minecraft.world.entity.EntityType.class,net.minecraft.world.level.Level.class).newInstance(net.minecraft.world.entity.EntityTypes.TNT,level);
                @SuppressWarnings("unchecked") var namedKey=(EntityDataAccessor<Integer>)namedType.getField("KEY").get(null);
                check(namedEntity.getEntityData().get(namedKey)==59,"named source callback/field/definition not admitted");
                var duplicateType=loader.linkOwnedClass("probe.DuplicateTracked");
                try{duplicateType.getConstructor(net.minecraft.world.entity.EntityType.class,net.minecraft.world.level.Level.class).newInstance(net.minecraft.world.entity.EntityTypes.TNT,level);throw new AssertionError("duplicate source definition was ignored");}
                catch(java.lang.reflect.InvocationTargetException expected){check(expected.getCause() instanceof IllegalArgumentException,"native duplicate failure changed");}
                checkPositions(entity,type);
                var data=entity.getEntityData();
                check(entity.getFuse()==80,"native parent definition missing");
                for(var t:List.of(type.getSuperclass(),type)) {
                    @SuppressWarnings("unchecked") var key=(EntityDataAccessor<Integer>)t.getField("KEY").get(null);
                    check(data.get(key).equals(t==type?47:31),"source definition/local value changed");
                    check(t.getDeclaredField("calls").getInt(entity)==1,"source parent/child callback count changed");
                    data.set(key,88);check(data.get(key)==88,"native post-construction set/get broken");
                }
                check(data.isDirty(),"native dirty tracking missing");
                var nativeBlast=PrimedTnt.class.getDeclaredMethod("explode");nativeBlast.setAccessible(true);nativeBlast.invoke(entity);
                check(type.getField("blasts").getInt(entity)==1,"patched native TNT virtual explosion callback was lost");
                try{loader.linkOwnedClass("probe.EscapedTracked");throw new AssertionError("live data read accepted during builder initialization");}
                catch(ClassNotFoundException expected){check(expected.getCause() instanceof UnsupportedOperationException && expected.getCause().getMessage().contains("entity-data builder boundary"),"wrong unsupported boundary");}
            }
            check(Arrays.equals(original,Files.readAllBytes(path)),"source artifact modified");
        } finally{Files.deleteIfExists(path);}
        System.out.println("PASS: source parent/child entity data callbacks over real native builder/storage, inherited native fuse, once-only source values/local slots, dirty get/set, unsupported live data read and immutable input; world sync/persistence unverified");
    }
    private static void positionMethods(ClassNode node) {
        node.fields.add(new FieldNode(ACC_PUBLIC,"snaps","I",null,null));
        var override=new MethodNode(ACC_PUBLIC,"m_7678_","(DDDFF)V",null,null);
        override.instructions.add(new VarInsnNode(ALOAD,0));override.instructions.add(new InsnNode(DUP));override.instructions.add(new FieldInsnNode(GETFIELD,node.name,"snaps","I"));override.instructions.add(new InsnNode(ICONST_1));override.instructions.add(new InsnNode(IADD));override.instructions.add(new FieldInsnNode(PUTFIELD,node.name,"snaps","I"));
        override.instructions.add(new VarInsnNode(ALOAD,0));int slot=1;for(var arg:Type.getArgumentTypes(override.desc)){override.instructions.add(new VarInsnNode(arg.getOpcode(ILOAD),slot));slot+=arg.getSize();}
        override.instructions.add(new MethodInsnNode(INVOKESPECIAL,BASE,"m_7678_",override.desc,false));override.instructions.add(new InsnNode(RETURN));node.methods.add(override);
        String[] names={"m_7678_","m_6027_","m_20219_","m_20035_"},descs={"(DDDFF)V","(DDD)V","(Lnet/minecraft/world/phys/Vec3;)V","(Lnet/minecraft/core/BlockPos;FF)V"};
        for(int i=0;i<names.length;i++) {
            var m=new MethodNode(ACC_PUBLIC|ACC_STATIC,"position"+i,"(Lnet/minecraft/world/entity/Entity;"+descs[i].substring(1),null,null);
            slot=0;for(var arg:Type.getArgumentTypes(m.desc)){m.instructions.add(new VarInsnNode(arg.getOpcode(ILOAD),slot));slot+=arg.getSize();}
            m.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/world/entity/Entity",names[i],descs[i],false));m.instructions.add(new InsnNode(RETURN));node.methods.add(m);
        }
        var handle=new MethodNode(ACC_PUBLIC|ACC_STATIC,"positionHandle","()Ljava/lang/invoke/MethodHandle;",null,null);handle.instructions.add(new LdcInsnNode(new Handle(H_INVOKEVIRTUAL,"net/minecraft/world/entity/Entity","m_7678_","(DDDFF)V",false)));handle.instructions.add(new InsnNode(ARETURN));node.methods.add(handle);
    }
    private static void checkPositions(PrimedTnt entity,Class<?> type) throws Exception {
        entity.setDeltaMovement(0.3,0.4,0.5);var motion=entity.getDeltaMovement();
        type.getMethod("position0",Entity.class,double.class,double.class,double.class,float.class,float.class).invoke(null,entity,3.0,70.0,-4.0,35F,12F);
        type.getMethod("position1",Entity.class,double.class,double.class,double.class).invoke(null,entity,4.0,71.0,-3.0);
        type.getMethod("position2",Entity.class,net.minecraft.world.phys.Vec3.class).invoke(null,entity,new net.minecraft.world.phys.Vec3(5,72,-2));
        type.getMethod("position3",Entity.class,net.minecraft.core.BlockPos.class,float.class,float.class).invoke(null,entity,new net.minecraft.core.BlockPos(6,73,-1),40F,15F);
        check(entity.position().equals(new net.minecraft.world.phys.Vec3(6.5,73,-0.5))&&entity.oldPosition().equals(entity.position()),"source block-center/old-position snap changed");
        check(entity.getYRot()==40F&&entity.getXRot()==15F&&entity.yRotO==40F&&entity.xRotO==15F&&entity.getDeltaMovement().equals(motion),"source rotation/motion snap changed");
        var handle=(java.lang.invoke.MethodHandle)type.getMethod("positionHandle").invoke(null);
        try{handle.invoke(entity,7.0,74.0,0.0,50F,20F);}catch(Throwable failure){throw new AssertionError(failure);}
        check(type.getField("snaps").getInt(entity)==5&&entity.getX()==7&&entity.xo==7&&entity.yo==74&&entity.zo==0,"source snap override/super/handle count or old fields changed");
    }
    private static void callback(ClassNode n,String parent,int value) {
        n.fields.add(new FieldNode(ACC_PUBLIC|ACC_STATIC,"KEY",ACCESS,null,null));n.fields.add(new FieldNode(ACC_PUBLIC,"calls","I",null,null));
        var ctor=new MethodNode(ACC_PUBLIC,"<init>","(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",null,null);
        ctor.instructions.add(new VarInsnNode(ALOAD,0));ctor.instructions.add(new VarInsnNode(ALOAD,1));ctor.instructions.add(new VarInsnNode(ALOAD,2));ctor.instructions.add(new MethodInsnNode(INVOKESPECIAL,parent,"<init>",ctor.desc,false));ctor.instructions.add(new InsnNode(RETURN));n.methods.add(ctor);
        var init=new MethodNode(ACC_STATIC,"<clinit>","()V",null,null);init.instructions.add(new LdcInsnNode(Type.getObjectType(n.name)));
        init.instructions.add(new FieldInsnNode(GETSTATIC,"net/minecraft/network/syncher/EntityDataSerializers","f_135028_","Lnet/minecraft/network/syncher/EntityDataSerializer;"));
        init.instructions.add(new MethodInsnNode(INVOKESTATIC,DATA,"m_135353_","(Ljava/lang/Class;Lnet/minecraft/network/syncher/EntityDataSerializer;)"+ACCESS,false));init.instructions.add(new FieldInsnNode(PUTSTATIC,n.name,"KEY",ACCESS));init.instructions.add(new InsnNode(RETURN));n.methods.add(init);
        var m=new MethodNode(ACC_PROTECTED,"m_8097_","()V",null,null);
        m.instructions.add(new LdcInsnNode(value));m.instructions.add(new VarInsnNode(ISTORE,1));
        m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new MethodInsnNode(INVOKESPECIAL,parent,"m_8097_","()V",false));
        m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new InsnNode(DUP));m.instructions.add(new FieldInsnNode(GETFIELD,n.name,"calls","I"));m.instructions.add(new InsnNode(ICONST_1));m.instructions.add(new InsnNode(IADD));m.instructions.add(new FieldInsnNode(PUTFIELD,n.name,"calls","I"));
        m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new FieldInsnNode(GETFIELD,n.name,"f_19804_","L"+DATA+";"));m.instructions.add(new FieldInsnNode(GETSTATIC,n.name,"KEY",ACCESS));
        m.instructions.add(new VarInsnNode(ILOAD,1));m.instructions.add(new MethodInsnNode(INVOKESTATIC,"java/lang/Integer","valueOf","(I)Ljava/lang/Integer;",false));
        m.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,DATA,"m_135372_","("+ACCESS+"Ljava/lang/Object;)V",false));m.instructions.add(new InsnNode(RETURN));n.methods.add(m);
    }
    private static ClassNode node(String name,String parent){var n=new ClassNode();n.version=V17;n.access=ACC_PUBLIC;n.name=name;n.superName=parent;return n;}
    private static byte[] bytes(ClassNode n){var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);return w.toByteArray();}
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
