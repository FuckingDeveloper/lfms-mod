package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192SoundEventValues;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import java.lang.invoke.MethodHandle;
import java.util.jar.JarFile;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class SoundEventValueCheck implements Opcodes {
    private static final String SOUND="net/minecraft/sounds/SoundEvent", ID="Lnet/minecraft/resources/ResourceLocation;";
    static void run() throws Throwable {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,"fixture/SoundValues",null,"java/lang/Object",null);
        for(boolean fixed:new boolean[]{false,true}) {
            String constructor="("+ID+(fixed?"F":"")+")V";
            String factory="("+ID+(fixed?"F":"")+")L"+SOUND+";";
            var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,fixed?"fixed":"variable",factory,null,null);m.visitCode();
            m.visitTypeInsn(NEW,SOUND);m.visitInsn(DUP);m.visitVarInsn(ALOAD,0);if(fixed)m.visitVarInsn(FLOAD,1);
            m.visitMethodInsn(INVOKESPECIAL,SOUND,"<init>",constructor,false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
            m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,fixed?"fixedHandle":"variableHandle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();
            m.visitLdcInsn(new Handle(H_NEWINVOKESPECIAL,SOUND,"<init>",constructor,false));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        }
        for(String name:new String[]{"m_11660_","getLocation","m_215668_","getRange"}) {
            boolean range=name.equals("m_215668_")||name.equals("getRange");
            String desc=range?"(F)F":"()"+ID;
            var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,name,range?"(L"+SOUND+";F)F":"(L"+SOUND+";)"+ID,null,null);m.visitCode();
            m.visitVarInsn(ALOAD,0);if(range)m.visitVarInsn(FLOAD,1);m.visitMethodInsn(INVOKEVIRTUAL,SOUND,name,desc,false);
            m.visitInsn(range?FRETURN:ARETURN);m.visitMaxs(0,0);m.visitEnd();
            m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,name+"Handle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();
            m.visitLdcInsn(new Handle(H_INVOKEVIRTUAL,SOUND,name,desc,false));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        }
        var nested=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"nested","()L"+SOUND+";",null,null);nested.visitCode();
        nested.visitTypeInsn(NEW,SOUND);nested.visitInsn(DUP);
        nested.visitTypeInsn(NEW,"net/minecraft/resources/ResourceLocation");nested.visitInsn(DUP);
        nested.visitLdcInsn("sound_check:nested");nested.visitMethodInsn(INVOKESPECIAL,"net/minecraft/resources/ResourceLocation","<init>","(Ljava/lang/String;)V",false);
        nested.visitMethodInsn(INVOKESPECIAL,SOUND,"<init>","("+ID+")V",false);nested.visitInsn(ARETURN);nested.visitMaxs(0,0);nested.visitEnd();
        var function=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"function","()Ljava/util/function/Function;",null,null);function.visitCode();
        function.visitInvokeDynamicInsn("apply","()Ljava/util/function/Function;",
                new Handle(H_INVOKESTATIC,"java/lang/invoke/LambdaMetafactory","metafactory",
                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",false),
                Type.getMethodType("(Ljava/lang/Object;)Ljava/lang/Object;"),new Handle(H_INVOKEVIRTUAL,SOUND,"m_11660_","()"+ID,false),Type.getMethodType("(L"+SOUND+";)"+ID));
        function.visitInsn(ARETURN);function.visitMaxs(0,0);function.visitEnd();
        var codec=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"codec","()Lcom/mojang/serialization/Codec;",null,null);codec.visitCode();
        codec.visitFieldInsn(GETSTATIC,SOUND,"f_11655_","Lcom/mojang/serialization/Codec;");codec.visitInsn(ARETURN);codec.visitMaxs(0,0);codec.visitEnd();w.visitEnd();
        var transformer=new Forge1192LegacyClassTransformer();
        var type=new AttributeOperationFamilyCheck.Loader().define("fixture.SoundValues",transformer.transform("fixture.SoundValues",w.toByteArray()).bytes());
        Identifier id=Identifier.parse("sound_check:value");
        var variable=(SoundEvent)type.getMethod("variable",Identifier.class).invoke(null,id);
        var fixed=(SoundEvent)type.getMethod("fixed",Identifier.class,float.class).invoke(null,id,7f);
        check(((SoundEvent)type.getMethod("nested").invoke(null)).location().equals(Identifier.parse("sound_check:nested")),"nested constructor pairing");
        @SuppressWarnings("unchecked") var getterFunction=(java.util.function.Function<SoundEvent,Identifier>)type.getMethod("function").invoke(null);
        check(getterFunction.apply(variable).equals(id),"indy unbound getter reference");
        negatives();
        check(variable.location().equals(id)&&variable.getRange(2)==32&&fixed.getRange(2)==7,"constructor/range semantics");
        check(((SoundEvent)((MethodHandle)type.getMethod("variableHandle").invoke(null)).invokeExact(id)).location().equals(id),"variable constructor handle");
        check(((SoundEvent)((MethodHandle)type.getMethod("fixedHandle").invoke(null)).invokeExact(id,9f)).getRange(3)==9,"fixed constructor handle");
        for(String name:new String[]{"m_11660_","getLocation"}) {
            check(type.getMethod(name,SoundEvent.class).invoke(null,variable).equals(id),"location call");
            check(((Identifier)((MethodHandle)type.getMethod(name+"Handle").invoke(null)).invokeExact(variable)).equals(id),"location handle");
        }
        for(String name:new String[]{"m_215668_","getRange"}) {
            check((float)type.getMethod(name,SoundEvent.class,float.class).invoke(null,fixed,3f)==7,"range call");
            check((float)((MethodHandle)type.getMethod(name+"Handle").invoke(null)).invokeExact(variable,3f)==48,"range handle");
        }
        check(type.getMethod("codec").invoke(null)==Forge1192SoundEventValues.CODEC,"source codec field identity");
        var encoded=Forge1192SoundEventValues.CODEC.encodeStart(JsonOps.INSTANCE,fixed).getOrThrow();
        check(encoded.equals(new JsonPrimitive("sound_check:value")),"source flat sound codec became native holder/record");
        check(Forge1192SoundEventValues.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow().getRange(2)==32,"source codec variable-range decode");
        try(var jar=new JarFile("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar")) {
            transformer.bindManagedClassBytes(owner->{var entry=jar.getJarEntry(owner+".class");return entry==null?null:jar.getInputStream(entry).readAllBytes();});
            String owner="ic2/core/block/machines/tiles/nv/StoneBasicMachineTileEntity";
            var source=jar.getInputStream(jar.getJarEntry(owner+".class")).readAllBytes();
            var node=new ClassNode();new ClassReader(transformer.transform(owner.replace('/','.'),source).bytes()).accept(node,0);
            int locations=0;
            for(var method:node.methods)for(var insn:method.instructions)if(insn instanceof MethodInsnNode call&&call.owner.equals(SOUND)) {
                check(!call.name.equals("m_11660_")&&!call.name.equals("getLocation"),"original fuel callback still links removed getter");
                if(call.name.equals("location"))locations++;
            }
            check(locations>0,"source callback was skipped");
        }
        System.out.println("PASS: source sound constructors/getters/range/callable handles and flat value codec execute on native record; original fuel callback preserved");
    }
    private static void negatives() {
        var original=new ClassNode();original.version=V17;original.access=ACC_PUBLIC;original.name="fixture/SoundNegatives";original.superName="java/lang/Object";
        var method=new MethodNode(ACC_PUBLIC|ACC_STATIC,"unrelated","(Ljava/lang/Object;)"+ID,null,null);
        method.instructions.add(new VarInsnNode(ALOAD,0));method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,"java/lang/Object","m_11660_","()"+ID,false));method.instructions.add(new InsnNode(ARETURN));original.methods.add(method);
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);original.accept(writer);
        var transformed=new ClassNode();new ClassReader(new Forge1192LegacyClassTransformer().transform("fixture.SoundNegatives",writer.toByteArray()).bytes()).accept(transformed,0);
        check(transformed.methods.getFirst().instructions.iterator().hasNext(),"negative method removed");
        var call=java.util.stream.StreamSupport.stream(java.util.Spliterators.spliteratorUnknownSize(transformed.methods.getFirst().instructions.iterator(),0),false)
                .filter(i->i instanceof MethodInsnNode).map(i->(MethodInsnNode)i).findFirst().orElseThrow();
        check(call.owner.equals("java/lang/Object")&&call.name.equals("m_11660_"),"descriptor coincidence rewrote unrelated owner");
        var subclass=new ClassWriter(0);subclass.visit(V17,ACC_PUBLIC,"fixture/SoundSubclass",null,SOUND,null);subclass.visitEnd();
        try{new Forge1192LegacyClassTransformer().transform("fixture.SoundSubclass",subclass.toByteArray());throw new AssertionError("target final record subclass accepted");}
        catch(UnsupportedOperationException expected){check(expected.getMessage().contains("final record"),"subclass lacks explicit diagnosis");}
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
