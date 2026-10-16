package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;


import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.jar.*;

public final class GuiItemOperationCheck {
    public static void run() throws Exception {
        Class.forName("com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192GuiItemExtractionBridge");
        var input=Path.of("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar");
        if(!Files.exists(input)){System.out.println("SKIP: external immutable GUI operation evidence absent");return;}
        MethodNode body;
        try(var jar=new JarFile(input.toFile())){var source=new ClassNode();new ClassReader(jar.getInputStream(jar.getJarEntry("ic2/core/platform/rendering/RenderUtils.class"))).accept(source,0);body=source.methods.stream().filter(m->m.name.equals("renderGuiItem")).findFirst().orElseThrow();}
        String descriptor=body.desc;
        var utility=new ClassNode();utility.version=Opcodes.V17;utility.access=Opcodes.ACC_PUBLIC;utility.name="fixture/UnrelatedGuiUtility";utility.superName="java/lang/Object";body.name="drawEvidence";utility.methods.add(body);
        var caller=new ClassNode();caller.version=Opcodes.V17;caller.access=Opcodes.ACC_PUBLIC;caller.name="fixture/GuiOperationCaller";caller.superName="java/lang/Object";
        var method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"call","(Lnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;)V",null,null);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"net/minecraft/client/Minecraft","m_91291_","()Lnet/minecraft/client/renderer/entity/ItemRenderer;",false));method.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));method.instructions.add(new VarInsnNode(Opcodes.ALOAD,2));method.instructions.add(new InsnNode(Opcodes.FCONST_0));method.instructions.add(new InsnNode(Opcodes.FCONST_0));method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,utility.name,body.name,descriptor,false));method.instructions.add(new InsnNode(Opcodes.RETURN));caller.methods.add(method);
        var transformer=new Forge1192LegacyClassTransformer();transformer.bindManagedClassBytes(name->name.equals(utility.name)?bytes(utility):null);
        check(projected(transformer,caller),"complete source GUI body/direct getter flow not projected");
        for(var insn:body.instructions)if(insn instanceof LdcInsnNode ldc && Integer.valueOf(15728880).equals(ldc.cst)){ldc.cst=0;break;}
        transformer.bindManagedClassBytes(name->name.equals(utility.name)?bytes(utility):null);
        check(!projected(transformer,caller),"unproven lighting change accepted or stale shape cache retained");
        System.out.println("PASS: unrelated helper identity, full source GUI-operation body/direct flow, native extraction linkage and changed-light/rebinding rejection");
    }
    private static byte[] bytes(ClassNode node){var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();}
    private static boolean projected(Forge1192LegacyClassTransformer transformer,ClassNode node){var result=new ClassNode();new ClassReader(transformer.transform(node.name.replace('/','.'),bytes(node)).bytes()).accept(result,0);return java.util.stream.StreamSupport.stream(result.methods.getFirst().instructions.spliterator(),false).anyMatch(i->i instanceof MethodInsnNode c&&c.owner.equals("com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192GuiItemExtractionBridge")&&c.name.equals("item"));}
    private static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);}
}
