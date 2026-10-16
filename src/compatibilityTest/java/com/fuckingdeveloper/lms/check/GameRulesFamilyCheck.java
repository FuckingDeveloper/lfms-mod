package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.world.Forge1192GameRuleViews;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.world.Forge1192GameRuleViews.*;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.world.Forge1192GameRuleViews.Type;
import com.mojang.serialization.Dynamic;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.gamerules.GameRules;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class GameRulesFamilyCheck {
    static void run() throws Exception {
        var rules=Forge1192GameRuleViews.newRules();
        var drops=new Key<BooleanValue>("minecraft:block_drops",Category.DROPS);
        var ticks=new Key<IntegerValue>("minecraft:random_tick_speed",Category.UPDATES);
        var value=Forge1192GameRuleViews.getRule(rules,drops);
        check(value==Forge1192GameRuleViews.getRule(rules,drops),"source value identity changed while view alive");
        value.set(false,null);check(!rules.get(GameRules.BLOCK_DROPS),"source write disconnected from native owner");
        rules.set(GameRules.BLOCK_DROPS,true,null);check(value.get(),"source read used stale snapshot");
        var copy=Forge1192GameRuleViews.copy(rules);Forge1192GameRuleViews.getRule(copy,drops).set(false,null);check(value.get(),"source copy aliases native state");
        check(copy.availableRules().count()==rules.availableRules().count(),"copy changed available rule set");
        var tag=Forge1192GameRuleViews.createTag(copy);
        var restored=Forge1192GameRuleViews.fromDynamic(new Dynamic<>(NbtOps.INSTANCE,tag));
        check(!restored.get(GameRules.BLOCK_DROPS),"string snapshot lost actual native value");
        var speed=Forge1192GameRuleViews.getRule(rules,ticks);speed.set(7,null);check(rules.get(GameRules.RANDOM_TICK_SPEED)==7,"native integer write lost");
        try{speed.set(-1,null);throw new AssertionError("invalid target rule value accepted");}catch(IllegalStateException expected){}
        check(speed.get()==7,"rejected write mutated authoritative state");
        tag.putString("fixture:missing","true");try{Forge1192GameRuleViews.fromDynamic(new Dynamic<>(NbtOps.INSTANCE,tag));throw new AssertionError("unknown snapshot identity silently lost");}catch(UnsupportedOperationException expected){}
        var events=new ArrayList<String>();var count=new int[2];
        Forge1192GameRuleViews.visitGameRuleTypes(new Visitor(){
            public <T extends Value<T>> void visit(Key<T> key,Type<T> type){events.add(key.getId());check(!key.getDescriptionId().isBlank()&&key.getCategory()!=null,"native rule description/category missing");}
            public void visitBoolean(Key<BooleanValue> key,Type<BooleanValue> type){check(events.getLast().equals(key.getId()),"typed visit preceded generic visit");count[0]++;}
            public void visitInteger(Key<IntegerValue> key,Type<IntegerValue> type){check(events.getLast().equals(key.getId()),"typed visit preceded generic visit");count[1]++;}
        });
        check(count[0]>0&&count[1]>0&&events.size()==rules.availableRules().count()&&events.size()==count[0]+count[1],"native source visitor inventory lost/duplicated definitions");
        sourceVisitor(events.size());
        check(net.minecraft.server.MinecraftServer.class.getMethod("getGameRules").getReturnType()==GameRules.class&&net.minecraft.server.level.ServerLevel.class.getMethod("getGameRules").getReturnType()==GameRules.class,"native rule accessor ABI changed");
        var external=Path.of("legacy-mods/CarbonConfig-1.19.2-2.0.2.jar");
        if(Files.exists(external))try(var jar=new JarFile(external.toFile())) {
            var transformer=new Forge1192LegacyClassTransformer();transformer.bindManagedClassBytes(name->{var entry=jar.getJarEntry(name+".class");if(entry==null)return null;try(var in=jar.getInputStream(entry)){return in.readAllBytes();}});
            String name="carbonconfiglib/gui/impl/minecraft/MinecraftConfig";byte[] bytes;try(var in=jar.getInputStream(jar.getJarEntry(name+".class"))){bytes=in.readAllBytes();}
            var output=new ClassNode();new ClassReader(transformer.transform(name.replace('/','.'),bytes).bytes()).accept(output,0);
            var getters=new ArrayList<MethodInsnNode>();
            for(var method:output.methods)for(var instruction:method.instructions)if(instruction instanceof MethodInsnNode call&&call.owner.equals("net/minecraft/server/MinecraftServer")&&call.desc.equals("()Lnet/minecraft/world/level/gamerules/GameRules;"))getters.add(call);
            check(!getters.isEmpty()&&getters.stream().allMatch(call->call.name.equals("getGameRules")),"immutable consumer retained unadapted rule getter");
        }

        System.out.println("PASS: native rule/view identity, boolean/integer live mutation and constraints, independent copy, string snapshots, generic/typed source callbacks and immutable transformed visitor");
    }
    private static void sourceVisitor(int count) throws Exception {
        String root="net/minecraft/world/level/GameRules",owner="fixture/RuleVisitor";
        var node=new ClassNode();node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC;node.name=owner;node.superName="java/lang/Object";node.interfaces.add(root+"$GameRuleTypeVisitor");node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,"count","I",null,null));
        var init=new MethodNode(Opcodes.ACC_PUBLIC,"<init>","()V",null,null);node.methods.add(init);init.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));init.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,"java/lang/Object","<init>","()V",false));init.instructions.add(new InsnNode(Opcodes.RETURN));
        for(String name:List.of("m_6891_","m_6894_")) {
            var m=new MethodNode(Opcodes.ACC_PUBLIC,name,"(L"+root+"$Key;L"+root+"$Type;)V",null,null);node.methods.add(m);
            m.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));m.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,root+"$Key","m_46331_","()Ljava/lang/String;",false));m.instructions.add(new InsnNode(Opcodes.POP));
            m.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));m.instructions.add(new InsnNode(Opcodes.DUP));m.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,owner,"count","I"));m.instructions.add(new InsnNode(Opcodes.ICONST_1));m.instructions.add(new InsnNode(Opcodes.IADD));m.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD,owner,"count","I"));m.instructions.add(new InsnNode(Opcodes.RETURN));
        }
        var make=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"defaults","()L"+root+";",null,null);node.methods.add(make);make.instructions.add(new TypeInsnNode(Opcodes.NEW,root));make.instructions.add(new InsnNode(Opcodes.DUP));make.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,root,"<init>","()V",false));make.instructions.add(new InsnNode(Opcodes.ARETURN));
        var path=Files.createTempFile("lms-rule-visitor", ".jar");var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry(owner+".class"));jar.write(writer.toByteArray());jar.closeEntry();}
        var original=Files.readAllBytes(path);
        try(var loader=new ManagedLegacyClassLoader(path,GameRulesFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass(owner.replace('/','.'));var source=type.getConstructor().newInstance();Forge1192GameRuleViews.visitGameRuleTypes((Visitor)source);
            check(type.getField("count").getInt(source)==count,"transformed source typed callback lost dispatch");
            check(((GameRules)type.getMethod("defaults").invoke(null)).availableRules().count()==count,"source constructor native defaults changed");
        }
        check(Arrays.equals(original,Files.readAllBytes(path)),"source visitor artifact mutated");
    }
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
}
