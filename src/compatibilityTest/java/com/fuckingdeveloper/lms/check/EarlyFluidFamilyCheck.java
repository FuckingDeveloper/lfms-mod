package com.fuckingdeveloper.lms.check;
import com.fuckingdeveloper.lms.runtime.*;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import net.minecraft.world.level.material.Fluids;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public final class EarlyFluidFamilyCheck {
    public static void run() { NativeDefaultComponentFixture.run(EarlyFluidFamilyCheck::checks); }
    private static void checks() {
        var fluid=Forge1192EarlyFluidStackBridge.create(Fluids.WATER,777);
        if(fluid.getFluid()!=Fluids.WATER || fluid.getAmount()!=777)throw new AssertionError("early fluid identity/amount lost");
        if(!fluid.getComponents().isEmpty())throw new AssertionError("native water defaults were fabricated");
        var node=new ClassNode();node.name="fixture/EarlyFluid";node.superName="java/lang/Object";node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC;
        var method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"nested","()Lnet/minecraftforge/fluids/FluidStack;",null,null);
        String owner="net/minecraftforge/fluids/FluidStack";
        method.instructions.add(new TypeInsnNode(Opcodes.NEW,owner));method.instructions.add(new InsnNode(Opcodes.DUP));
        method.instructions.add(new TypeInsnNode(Opcodes.NEW,owner));method.instructions.add(new InsnNode(Opcodes.DUP));
        method.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC,"net/minecraft/world/level/material/Fluids","WATER","Lnet/minecraft/world/level/material/Fluid;"));method.instructions.add(new InsnNode(Opcodes.ICONST_1));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,owner,"<init>","(Lnet/minecraft/world/level/material/Fluid;I)V",false));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,owner,"getFluid","()Lnet/minecraft/world/level/material/Fluid;",false));method.instructions.add(new InsnNode(Opcodes.ICONST_2));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,owner,"<init>","(Lnet/minecraft/world/level/material/Fluid;I)V",false));method.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(method);
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);var mapped=new ClassNode();new ClassReader(new Forge1192LegacyClassTransformer().transform(node.name.replace('/','.'),writer.toByteArray()).bytes()).accept(mapped,0);
        int factories=0;for(var instruction:mapped.methods.getFirst().instructions){if(instruction instanceof TypeInsnNode type && type.getOpcode()==Opcodes.NEW)throw new AssertionError("nested source NEW escaped early factory");if(instruction instanceof MethodInsnNode call && call.owner.endsWith("Forge1192EarlyFluidStackBridge"))factories++;}
        if(factories!=2)throw new AssertionError("nested NEW pairing skipped/detached argument subtree");
        System.out.println("PASS: actual pending native water prototype/amount and nested source fluid constructor pairing; global holder binding is not mutated");
    }
}
