package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.Forge1192AnalogOutputRewriter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;

final class AnalogOutputFamilyCheck {
    public static class Parent extends Block {
        public int calls;
        public BlockState lastState;
        public Level lastLevel;
        public BlockPos lastPos;
        public Parent(BlockBehaviour.Properties properties) { super(properties); }
        public int m_6782_(BlockState state, Level level, BlockPos pos) {
            calls++; lastState=state; lastLevel=level; lastPos=pos; return pos.getX();
        }
    }
    public static class Child extends Parent {
        public Child(BlockBehaviour.Properties properties) { super(properties); }
        @Override public int m_6782_(BlockState state, Level level, BlockPos pos) {
            return super.m_6782_(state, level, pos) + 2;
        }
    }
    static void run() throws Exception {
        try(var jar=new java.util.jar.JarFile("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar")) {
            var transformer=new com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer();
            transformer.bindManagedClassBytes(name->{
                var entry=jar.getJarEntry(name+".class");if(entry==null)return null;
                try(var in=jar.getInputStream(entry)){return in.readAllBytes();}
                catch(java.io.IOException failure){throw new java.io.UncheckedIOException(failure);}
            });
            String name="ic2/core/block/base/IC2ContainerBlock";byte[] original;
            try(var in=jar.getInputStream(jar.getJarEntry(name+".class"))){original=in.readAllBytes();}
            var transformed=new ClassNode();new ClassReader(transformer.transform(name.replace('/','.'),original).bytes()).accept(transformed,0);
            var nativeEntry=transformed.methods.stream().filter(m->m.name.equals("getAnalogOutputSignal")
                    && m.desc.equals(Forge1192AnalogOutputRewriter.TARGET)).findFirst().orElseThrow();
            check(java.util.stream.StreamSupport.stream(nativeEntry.instructions.spliterator(),false)
                    .anyMatch(i->i instanceof MethodInsnNode c && c.owner.equals(name)
                            && c.desc.equals(Forge1192AnalogOutputRewriter.SOURCE)),"full pipeline lost original directionless callback");
        }
        var parent = node(Parent.class); var child = node(Child.class);
        var unrelated = node(Parent.class);
        check(Forge1192AnalogOutputRewriter.rewrite(unrelated, false)==0, "unproven owner adapted");
        check(Forge1192AnalogOutputRewriter.rewrite(parent, true)==1, "source callback missing native entry");
        try { Forge1192AnalogOutputRewriter.rewrite(parent, true); throw new AssertionError("entry collision accepted"); }
        catch (IllegalStateException expected) {}
        var bad = node(Parent.class);
        bad.methods.stream().filter(m->m.name.equals("m_6782_")).findFirst().orElseThrow().access |= Opcodes.ACC_STATIC;
        try { Forge1192AnalogOutputRewriter.rewrite(bad,true);throw new AssertionError("static callback accepted"); }
        catch (IllegalStateException expected) {}
        var loader = new ClassLoader(AnalogOutputFamilyCheck.class.getClassLoader()) {
            Class<?> define(ClassNode node) {var writer=new ClassWriter(0);node.accept(writer);byte[] bytes=writer.toByteArray();return defineClass(node.name.replace('/','.'),bytes,0,bytes.length);}
        };
        var parentType=loader.define(parent);var childType=loader.define(child);
        var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);
        var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
        var propertiesField=BlockBehaviour.class.getDeclaredField("properties");propertiesField.setAccessible(true);
        for (var type : java.util.List.of(parentType,childType)) {
            // Isolated callback receiver: do not construct/register a block after registry freeze.
            var block=(Block)unsafe.allocateInstance(type);
            propertiesField.set(block,BlockBehaviour.Properties.of());
            var pos=new BlockPos(7,8,9);
            var state=new BlockState(block,new net.minecraft.world.level.block.state.properties.Property<?>[0],new Comparable<?>[0]);
            int count=0;
            for(var direction:Direction.values()) {
                check(state.getAnalogOutputSignal(null,pos,direction)==(type==parentType?7:9),"native callback lost source virtual/super dispatch");
                check(parentType.getField("calls").getInt(block)==++count,"source callback not called once");
                check(parentType.getField("lastState").get(block)==state && parentType.getField("lastLevel").get(block)==null
                        && parentType.getField("lastPos").get(block)==pos,"native callback changed source argument identity");
            }
        }
        System.out.println("PASS: actual native block-state analog entry, all directions, inherited source virtual/super dispatch, once-only calls, argument identity and negative owner/collision/static boundaries");
    }
    private static ClassNode node(Class<?> type) throws Exception {
        String root=Type.getInternalName(Parent.class),leaf=Type.getInternalName(Child.class);
        var out=new ClassNode();
        try(var in=type.getResourceAsStream("/"+Type.getInternalName(type)+".class")) {
            new ClassReader(in).accept(new ClassRemapper(out,new Remapper(Opcodes.ASM9) {
                @Override public String map(String name) {return name.equals(root)?"fixture/AnalogParent":name.equals(leaf)?"fixture/AnalogChild":name;}
            }),0);
        }
        out.nestHostClass=null;out.nestMembers=null;out.innerClasses.clear();return out;
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
