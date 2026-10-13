package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public final class LivingFallFamilyCheck {
    public interface FallFactory { LivingFallEvent create(net.minecraft.world.entity.LivingEntity entity, float distance, float multiplier); }
    private static final String EVENT = Type.getInternalName(LivingFallEvent.class);
    static void run() throws Exception {
        var transformer = new Forge1192LegacyClassTransformer();
        var pass = Forge1192LegacyClassTransformer.class.getDeclaredMethod("rewriteLegacyLivingFallFamily", ClassNode.class);
        pass.setAccessible(true);
        var node = node("fixture/FallCalls", "java/lang/Object");
        var read = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "read", "(L" + EVENT + ";)F", null, null);
        read.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        read.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, EVENT, "getDistance", "()F", false));
        read.instructions.add(new InsnNode(Opcodes.FRETURN)); read.maxStack = 1; read.maxLocals = 1; node.methods.add(read);
        var write = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "write", "(L" + EVENT + ";F)V", null, null);
        write.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); write.instructions.add(new VarInsnNode(Opcodes.FLOAD, 1));
        write.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, EVENT, "setDistance", "(F)V", false));
        write.instructions.add(new InsnNode(Opcodes.RETURN)); write.maxStack = 2; write.maxLocals = 2; node.methods.add(write);
        var construct = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "construct", "(FF)L" + EVENT + ";", null, null);
        construct.instructions.add(new TypeInsnNode(Opcodes.NEW, EVENT)); construct.instructions.add(new InsnNode(Opcodes.DUP));
        construct.instructions.add(new InsnNode(Opcodes.ACONST_NULL)); construct.instructions.add(new VarInsnNode(Opcodes.FLOAD, 0));
        construct.instructions.add(new VarInsnNode(Opcodes.FLOAD, 1));
        construct.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, EVENT, "<init>", "(Lnet/minecraft/world/entity/LivingEntity;FF)V", false));
        construct.instructions.add(new InsnNode(Opcodes.ARETURN)); construct.maxStack = 5; construct.maxLocals = 2; node.methods.add(construct);
        // Exercise nested dynamic constants containing both a virtual handle and a constructor handle.
        var constants = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "constants", "()V", null, null);
        var bootstrap = new Handle(Opcodes.H_INVOKESTATIC, "fixture/Bootstrap", "constant",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", false);
        var ldc = new LdcInsnNode(new ConstantDynamic("outer", "Ljava/lang/Object;", bootstrap,
                new ConstantDynamic("inner", "Ljava/lang/Object;", bootstrap,
                        new Handle(Opcodes.H_INVOKEVIRTUAL, EVENT, "getDistance", "()F", false),
                        new Handle(Opcodes.H_NEWINVOKESPECIAL, EVENT, "<init>", "(Lnet/minecraft/world/entity/LivingEntity;FF)V", false))));
        constants.instructions.add(ldc); constants.instructions.add(new InsnNode(Opcodes.POP));
        constants.instructions.add(new InsnNode(Opcodes.RETURN)); constants.maxStack = 1; node.methods.add(constants);
        String bindDescriptor = "(L" + EVENT + ";)Ljava/util/function/Supplier;";
        var bind = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "bind", bindDescriptor, null, null);
        bind.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        var metafactory = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);
        bind.instructions.add(new InvokeDynamicInsnNode("get", bindDescriptor, metafactory,
                Type.getMethodType("()Ljava/lang/Object;"), new Handle(Opcodes.H_INVOKEVIRTUAL, EVENT, "getDistance", "()F", false),
                Type.getMethodType("()Ljava/lang/Float;")));
        bind.instructions.add(new InsnNode(Opcodes.ARETURN)); bind.maxLocals = 1; bind.maxStack = 1; node.methods.add(bind);
        String constructorDescriptor = "(Lnet/minecraft/world/entity/LivingEntity;FF)L" + EVENT + ";";
        var reference = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "factory", "()" + Type.getDescriptor(FallFactory.class), null, null);
        reference.instructions.add(new InvokeDynamicInsnNode("create", reference.desc, metafactory,
                Type.getMethodType(constructorDescriptor),
                new Handle(Opcodes.H_NEWINVOKESPECIAL, EVENT, "<init>", "(Lnet/minecraft/world/entity/LivingEntity;FF)V", false),
                Type.getMethodType(constructorDescriptor)));
        reference.instructions.add(new InsnNode(Opcodes.ARETURN)); reference.maxStack = 1; node.methods.add(reference);
        check((int)pass.invoke(transformer, node) == 6, "fall family coverage");
        for (var method : node.methods) new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
        var inner = (ConstantDynamic)((ConstantDynamic)ldc.cst).getBootstrapMethodArgument(0);
        check(((Handle)inner.getBootstrapMethodArgument(0)).getTag() == Opcodes.H_INVOKESTATIC
                && ((Handle)inner.getBootstrapMethodArgument(1)).getName().equals("newFallEvent"), "nested handle rewrite");
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer); byte[] bytes = writer.toByteArray();
        class Loader extends ClassLoader {
            Loader() { super(LivingFallFamilyCheck.class.getClassLoader()); }
            Class<?> define() { return defineClass(node.name.replace('/', '.'), bytes, 0, bytes.length); }
        }
        var generated = new Loader().define();
        var event = new LivingFallEvent(null, 3.123456789, .75f); event.setCanceled(true);
        float observed = (Float)generated.getMethod("read", LivingFallEvent.class).invoke(null, event);
        check(observed == (float)3.123456789 && event.getDistance() == 3.123456789, "read mutated target precision");
        generated.getMethod("write", LivingFallEvent.class, float.class).invoke(null, event, 2.3f);
        check(event.getDistance() == (double)2.3f && event.getDamageMultiplier() == .75f && event.isCanceled(), "write lost shared event state");
        var created = (LivingFallEvent)generated.getMethod("construct", float.class, float.class).invoke(null, 4.2f, .4f);
        check(created.getDistance() == (double)4.2f && created.getDamageMultiplier() == .4f, "constructor operands/initialization");
        var supplier = (java.util.function.Supplier<?>)generated.getMethod("bind", LivingFallEvent.class).invoke(null, event);
        check(supplier.get().equals(2.3f), "actual fall method reference linkage");
        var constructorReference = (FallFactory)generated.getMethod("factory").invoke(null);
        var factory = constructorReference.create(null, 1.2f, .3f);
        check(factory.getDistance() == (double)1.2f && factory.getDamageMultiplier() == .3f, "constructor-handle factory ABI");
        var unrelated = node("fixture/Unrelated", "java/lang/Object");
        var negative = new MethodNode(Opcodes.ACC_STATIC, "negative", "()V", null, null);
        negative.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
        negative.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "fixture/NotAnEvent", "getDistance", "()F", false));
        negative.instructions.add(new InsnNode(Opcodes.POP)); negative.instructions.add(new InsnNode(Opcodes.RETURN)); unrelated.methods.add(negative);
        check((int)pass.invoke(transformer, unrelated) == 0, "descriptor-only fall rewrite");
        var overridden = node("fixture/FallOverride", EVENT);
        overridden.methods.add(new MethodNode(Opcodes.ACC_PUBLIC, "getDistance", "()F", null, null));
        try { pass.invoke(transformer, overridden); throw new AssertionError("unadapted override accepted"); }
        catch (java.lang.reflect.InvocationTargetException expected) { check(expected.getCause() instanceof IllegalStateException, "override boundary"); }
        System.out.println("PASS: fall getter/setter/construction, shared target state/cancellation/precision, real lambda linkage, nested handles, verifier and negative owners/overrides");
    }
    private static ClassNode node(String name, String superclass) {
        var node = new ClassNode(); node.name = name; node.superName = superclass;
        node.version = Opcodes.V17; node.access = Opcodes.ACC_PUBLIC; return node;
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
