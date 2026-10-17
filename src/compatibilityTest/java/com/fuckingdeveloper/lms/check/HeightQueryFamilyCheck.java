package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.server.level.ServerLevel;
import static org.objectweb.asm.Opcodes.*;

final class HeightQueryFamilyCheck {
    static final String ROOT="net/minecraft/world/level/LevelHeightAccessor";
    record Query(String name,String srg,String desc) {}
    static final List<Query> QUERIES=List.of(
            new Query("height","m_141928_","()I"),new Query("min","m_141937_","()I"),
            new Query("max","m_151558_","()I"),new Query("count","m_151559_","()I"),
            new Query("minSection","m_151560_","()I"),new Query("maxSection","m_151561_","()I"),
            new Query("outside","m_151562_","(I)Z"),new Query("outsidePos","m_151570_","(Lnet/minecraft/core/BlockPos;)Z"),
            new Query("index","m_151564_","(I)I"),new Query("indexFromSection","m_151566_","(I)I"),
            new Query("sectionFromIndex","m_151568_","(I)I"));
    static final class NativeLoader extends ClassLoader {
        NativeLoader(){super(HeightQueryFamilyCheck.class.getClassLoader());}
        Class<?> define(ClassNode n){byte[] b=bytes(n);return defineClass(n.name.replace('/','.'),b,0,b.length);}
    }
    static void run() throws Exception {
        var calls=node("probe/HeightCalls","java/lang/Object");
        for(var q:QUERIES) {
            var m=new MethodNode(ACC_PUBLIC|ACC_STATIC,q.name,"(L"+ROOT+";"+q.desc.substring(1),null,null);
            m.instructions.add(new VarInsnNode(ALOAD,0));int slot=1;
            for(var arg:Type.getArgumentTypes(q.desc)){m.instructions.add(new VarInsnNode(arg.getOpcode(ILOAD),slot));slot+=arg.getSize();}
            m.instructions.add(new MethodInsnNode(INVOKEINTERFACE,ROOT,q.srg,q.desc,true));m.instructions.add(new InsnNode(Type.getReturnType(q.desc).getOpcode(IRETURN)));calls.methods.add(m);
            var h=new MethodNode(ACC_PUBLIC|ACC_STATIC,q.name+"Handle","()Ljava/lang/invoke/MethodHandle;",null,null);
            h.instructions.add(new LdcInsnNode(new Handle(H_INVOKEINTERFACE,ROOT,q.srg,q.desc,true)));h.instructions.add(new InsnNode(ARETURN));calls.methods.add(h);
        }
        var factory=new MethodNode(ACC_PUBLIC|ACC_STATIC,"create","(II)L"+ROOT+";",null,null);
        factory.instructions.add(new VarInsnNode(ILOAD,0));factory.instructions.add(new VarInsnNode(ILOAD,1));factory.instructions.add(new MethodInsnNode(INVOKESTATIC,ROOT,"m_186487_",factory.desc,true));factory.instructions.add(new InsnNode(ARETURN));calls.methods.add(factory);
        var inherited=new MethodNode(ACC_PUBLIC|ACC_STATIC,"inherited","(Lnet/minecraft/server/level/ServerLevel;)I",null,null);
        inherited.instructions.add(new VarInsnNode(ALOAD,0));inherited.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/server/level/ServerLevel","m_141937_","()I",false));inherited.instructions.add(new InsnNode(IRETURN));calls.methods.add(inherited);
        var custom=node("probe/LegacyHeight","java/lang/Object");custom.interfaces.add(ROOT);
        constant(custom,"m_141937_",7);
        var unrelated=node("probe/UnrelatedHeight","java/lang/Object");constant(unrelated,"m_141937_",19);
        var foreign=new MethodNode(ACC_PUBLIC|ACC_STATIC,"foreign","(L"+unrelated.name+";)I",null,null);
        foreign.instructions.add(new VarInsnNode(ALOAD,0));foreign.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,unrelated.name,"m_141937_","()I",false));foreign.instructions.add(new InsnNode(IRETURN));calls.methods.add(foreign);
        var nativeWorld=node("probe/NativeHeightWorld","net/minecraft/server/level/ServerLevel");constant(nativeWorld,"getMinY",-48);constant(nativeWorld,"getHeight",96);
        var f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);var unsafe=(sun.misc.Unsafe)f.get(null);
        Path jarPath=Files.createTempFile("lms-height-query-", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(jarPath))) {for(var n:List.of(calls,custom,unrelated)){jar.putNextEntry(new JarEntry(n.name+".class"));jar.write(bytes(n));jar.closeEntry();}}
            byte[] original=Files.readAllBytes(jarPath);
            try(var loader=new ManagedLegacyClassLoader(jarPath,HeightQueryFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass("probe.HeightCalls");
                for(int[] range:List.of(new int[]{-64,384},new int[]{0,256},new int[]{-48,96},new int[]{-17,34},new int[]{1,Integer.MAX_VALUE},new int[]{0,0})) {
                    int min=range[0],height=range[1],max=min+height,minSection=min>>4,maxSection=((max-1)>>4)+1;
                    var view=(LevelHeightAccessor)type.getMethod("create",int.class,int.class).invoke(null,min,height);
                    var expected=Map.of("height",height,"min",min,"max",max,"count",maxSection-minSection,"minSection",minSection,"maxSection",maxSection);
                    for(var q:QUERIES) {
                        var handle=(java.lang.invoke.MethodHandle)type.getMethod(q.name+"Handle").invoke(null);
                        if(q.desc.equals("()I")) {
                            check(type.getMethod(q.name,LevelHeightAccessor.class).invoke(null,view).equals(expected.get(q.name)),"height range/endpoint "+q.name);
                            try {check((int)handle.invoke(view)==expected.get(q.name),"height handle "+q.name);}catch(Throwable e){throw new AssertionError(e);}
                        } else for(int y:new int[]{min-1,min,min+15,min+16,max-1,max}) {
                            Object value=q.name.equals("outsidePos")?new BlockPos(0,y,0):y;
                            Object result=type.getMethod(q.name,LevelHeightAccessor.class,q.name.equals("outsidePos")?BlockPos.class:int.class).invoke(null,view,value);
                            Object want=switch(q.name){case "outside","outsidePos"->y<min||y>=max;case "index"->(y>>4)-minSection;case "indexFromSection"->y-minSection;default->y+minSection;};
                            check(result.equals(want),"section/predicate "+q.name+" y="+y);
                            try {check(handle.invokeWithArguments(view,value).equals(want),"parameter handle "+q.name);}catch(Throwable e){throw new AssertionError(e);}
                        }
                    }
                }
                var world=(ServerLevel)unsafe.allocateInstance(new NativeLoader().define(nativeWorld));
                check((int)type.getMethod("inherited",ServerLevel.class).invoke(null,world)==-48,"inherited ServerLevel declaration missed");
                var unknown=unsafe.allocateInstance(loader.linkOwnedClass("probe.LegacyHeight"));
                try {type.getMethod("min",LevelHeightAccessor.class).invoke(null,unknown);throw new AssertionError("source override ignored");}
                catch(java.lang.reflect.InvocationTargetException e){check(e.getCause() instanceof UnsupportedOperationException,"wrong source callback gap");}
                var foreignType=loader.linkOwnedClass("probe.UnrelatedHeight");
                check((int)type.getMethod("foreign",foreignType).invoke(null,unsafe.allocateInstance(foreignType))==19,"coincidental owner rewritten");
            }
            check(Arrays.equals(original,Files.readAllBytes(jarPath)),"height source artifact changed");
        } finally {Files.deleteIfExists(jarPath);}
        System.out.println("PASS: all source native-backed height queries/factory and callable forms, exclusive endpoints, six ranges/overflow/negative sections, inherited ServerLevel owner, foreign owner and custom callback boundaries; real terrain jobs require runtime");
    }
    private static void constant(ClassNode n,String name,int value){var m=new MethodNode(ACC_PUBLIC,name,"()I",null,null);m.instructions.add(new LdcInsnNode(value));m.instructions.add(new InsnNode(IRETURN));n.methods.add(m);}
    private static ClassNode node(String name,String parent){var n=new ClassNode();n.version=V17;n.access=ACC_PUBLIC;n.name=name;n.superName=parent;return n;}
    private static byte[] bytes(ClassNode n){var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);return w.toByteArray();}
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
