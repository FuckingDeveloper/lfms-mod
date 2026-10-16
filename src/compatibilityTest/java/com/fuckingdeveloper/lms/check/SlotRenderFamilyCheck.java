package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyBakedModel;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyItemOverrides;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyRenderType;

import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.neoforged.neoforge.model.data.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

/** Independent source fixtures: field ownership and all proven dirty operations. */
public final class SlotRenderFamilyCheck {
    private static final String SLOT = "net/minecraft/world/inventory/Slot";
    private static final String RENDERER = "net/minecraft/client/renderer/LevelRenderer";
    public static void run() throws Exception {
        fields(); slotAccessors(); renderTransforms(); screenTransitions(); positionedModelData();
        System.out.println("PASS: source field hiding/declaration order/descriptors/rebinding/current node, actual Slot subclass linkage, annotated mutable coordinate accessors, four render invalidation operations/direct/indy/condy/inherited/negative owners, positioned model-data callback");
    }

    private static void slotAccessors() throws Exception {
        var transformer = new Forge1192LegacyClassTransformer();
        var accessor = node("fixture/MutableSlotAccessor", "java/lang/Object"); accessor.access |= Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT;
        var mixin = new AnnotationNode("Lorg/spongepowered/asm/mixin/Mixin;"); mixin.values = List.of("value", List.of(Type.getObjectType(SLOT)));
        accessor.invisibleAnnotations = List.of(mixin);
        for (String coordinate : List.of("x", "y")) {
            var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, "write" + coordinate, "(I)V", null, null);
            var annotation = new AnnotationNode("Lorg/spongepowered/asm/mixin/gen/Accessor;"); annotation.values = List.of("value", coordinate);
            method.visibleAnnotations = new ArrayList<>(List.of(annotation, new AnnotationNode("Lorg/spongepowered/asm/mixin/Mutable;"))); accessor.methods.add(method);
        }
        transformer.bindManagedClassBytes(name -> name.equals(accessor.name) ? bytes(accessor) : null);
        for (String coordinate : List.of("x", "y")) {
            var caller = node("fixture/CoordinateCaller", "java/lang/Object");
            var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "write", "(L" + SLOT + ";I)V", null, null);
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); method.instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, accessor.name));
            method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, accessor.name, "write" + coordinate, "(I)V", true));
            method.instructions.add(new InsnNode(Opcodes.RETURN)); caller.methods.add(method);
            check(pass(transformer, "rewriteLegacyMixinPropertyAccessors", caller) == 2, "verified mutable slot accessor missed"); verify(caller);
            var call = (MethodInsnNode)method.instructions.get(3);
            check(call.owner.endsWith("Forge1192SlotBridge") && call.name.equals(coordinate.equals("x") ? "setX" : "setY"), "wrong coordinate setter selected");
        }
        accessor.methods.getFirst().visibleAnnotations.removeLast();
        transformer.bindManagedClassBytes(name -> name.equals(accessor.name) ? bytes(accessor) : null);
        var caller = node("fixture/UnverifiedCoordinateCaller", "java/lang/Object");
        var method = new MethodNode(Opcodes.ACC_PUBLIC, "write", "()V", null, null);
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, accessor.name, "writex", "(I)V", true)); caller.methods.add(method);
        check(pass(transformer, "rewriteLegacyMixinPropertyAccessors", caller) == 0, "coordinate mutation authorized without source @Mutable");
    }

    private static void fields() throws Exception {
        var transformer = new Forge1192LegacyClassTransformer();
        var parent = node("fixture/OwnedSlot", SLOT);
        parent.fields.add(new FieldNode(Opcodes.ACC_PROTECTED, "f_40217_", "I", null, null));
        var ctor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); ctor.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
        for (int value : new int[]{9, 11, 12}) ctor.instructions.add(new IntInsnNode(Opcodes.BIPUSH, value));
        ctor.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, SLOT, "<init>", "(Lnet/minecraft/world/Container;III)V", false));
        ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); ctor.instructions.add(new IntInsnNode(Opcodes.BIPUSH, 37));
        ctor.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, parent.name, "f_40217_", "I"));
        ctor.instructions.add(new InsnNode(Opcodes.RETURN)); parent.methods.add(ctor);
        parent.methods.add(read("own", parent.name, "f_40217_", "I", false));
        var child = node("fixture/ChildSlot", parent.name);
        var childCtor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        childCtor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        childCtor.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, parent.name, "<init>", "()V", false));
        childCtor.instructions.add(new InsnNode(Opcodes.RETURN)); child.methods.add(childCtor);
        child.methods.add(read("inherited", child.name, "f_40217_", "I", false));
        child.methods.add(read("nativeIndex", child.name, "f_40219_", "I", false));
        Map<String, byte[]> original = new HashMap<>(); original.put(parent.name, bytes(parent)); original.put(child.name, bytes(child));
        transformer.bindManagedClassBytes(original::get);
        Map<String, byte[]> transformed = new HashMap<>();
        for (var entry : original.entrySet()) transformed.put(entry.getKey().replace('/', '.'), transformer.transform(entry.getKey().replace('/', '.'), entry.getValue()).bytes());
        ClassLoader loader = new ClassLoader(SlotRenderFamilyCheck.class.getClassLoader()) {
            @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
                byte[] code = transformed.get(name); if (code == null) throw new ClassNotFoundException(name);
                return defineClass(name, code, 0, code.length);
            }
        };
        var type = loader.loadClass(child.name.replace('/', '.')); var object = type.getConstructor().newInstance();
        check(type.getMethod("inherited").invoke(object).equals(37) && type.getMethod("own").invoke(object).equals(37), "source field redirected to private native Slot.slot");
        check(((net.minecraft.world.inventory.Slot)object).getContainerSlot() == 9, "source and native fields conflated");
        ((net.minecraft.world.inventory.Slot)object).index = 6;
        check(type.getMethod("nativeIndex").invoke(object).equals(6), "genuine inherited native field no longer migrated");
        check(Modifier.isPrivate(net.minecraft.world.inventory.Slot.class.getDeclaredField("slot").getModifiers()), "native private field was widened instead of preserving source ownership");

        var iface = node("fixture/SlotFieldInterface", "java/lang/Object"); iface.access |= Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT;
        iface.fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "f_40217_", "I", null, 41));
        var interfaceChild = node("fixture/InterfaceSlot", SLOT); interfaceChild.interfaces.add(iface.name);
        interfaceChild.methods.add(read("value", interfaceChild.name, "f_40217_", "I", true));
        original.put(iface.name, bytes(iface)); original.put(interfaceChild.name, bytes(interfaceChild));
        transformer.bindManagedClassBytes(original::get);
        check(pass(transformer, "rewriteVerifiedSrgFieldAccesses", interfaceChild) == 0, "superclass field won before a source interface declaration");
        var different = node("fixture/DifferentDescriptor", SLOT);
        different.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "f_40219_", "J", null, null));
        different.methods.add(read("value", different.name, "f_40219_", "I", false));
        check(pass(transformer, "rewriteVerifiedSrgFieldAccesses", different) == 1, "different descriptor hid native field");
        var mutated = node(parent.name, SLOT); mutated.methods.add(read("value", mutated.name, "f_40217_", "I", false));
        check(pass(transformer, "rewriteVerifiedSrgFieldAccesses", mutated) == 1, "current transformed declarations were replaced by cached source declarations");
        var changedParent = node(parent.name, "java/lang/Object"); changedParent.methods.add(read("value", changedParent.name, "f_40219_", "I", false));
        check(pass(transformer, "rewriteVerifiedSrgFieldAccesses", changedParent) == 0, "current transformed ancestry was replaced by cached source ancestry");

        var caller = node("fixture/ExternalReader", "java/lang/Object");
        caller.methods.add(readStaticReceiver("value", child.name, "f_40217_"));
        check(pass(transformer, "rewriteVerifiedSrgFieldAccesses", caller) == 0, "source inherited field missed");
        transformer.bindManagedClassBytes(name -> name.equals(child.name) ? bytes(node(child.name, SLOT)) : null);
        check(pass(transformer, "rewriteVerifiedSrgFieldAccesses", caller) == 1, "field declaration index survived rebinding");
    }

    private static void renderTransforms() throws Exception {
        var transformer = new Forge1192LegacyClassTransformer();
        var child = node("fixture/RendererSubclass", RENDERER);
        transformer.bindManagedClassBytes(name -> name.equals(child.name) ? bytes(child) : null);
        String state = "Lnet/minecraft/world/level/block/state/BlockState;";
        String[] names = {"m_109494_", "m_109721_", "m_109770_", "m_109490_", "m_109544_"};
        String[] descriptors = {"(IIIIII)V", "(Lnet/minecraft/core/BlockPos;" + state + state + ")V", "(III)V", "(III)V",
                "(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;" + state + state + "I)V"};
        for (int i = 0; i < names.length; i++) {
            for (String owner : List.of(RENDERER, child.name)) {
                var fixture = call(owner, names[i], descriptors[i], Opcodes.INVOKEVIRTUAL);
                check(pass(transformer, "rewriteLegacyRenderInvalidationFamily", fixture) == 1, "render dirty family missed " + names[i]); verify(fixture);
                check(fixture.methods.getFirst().instructions.iterator().next() instanceof VarInsnNode, "receiver missing");
            }
            check(pass(transformer, "rewriteLegacyRenderInvalidationFamily", call("fixture/Unrelated", names[i], descriptors[i], Opcodes.INVOKEVIRTUAL)) == 0, "descriptor-only dirty rewrite");
            check(pass(transformer, "rewriteLegacyRenderInvalidationFamily", call(RENDERER, names[i], descriptors[i], Opcodes.INVOKESTATIC)) == 0, "wrong invocation kind rewritten");
            var constants = node("fixture/RenderConstants" + i, "java/lang/Object");
            var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "constants", "()V", null, null);
            Handle source = new Handle(Opcodes.H_INVOKEVIRTUAL, RENDERER, names[i], descriptors[i], false);
            Handle bootstrap = new Handle(Opcodes.H_INVOKESTATIC, "fixture/Bootstrap", "constant", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", false);
            method.instructions.add(new LdcInsnNode(new ConstantDynamic("outer", "Ljava/lang/Object;", bootstrap, new ConstantDynamic("inner", "Ljava/lang/Object;", bootstrap, source))));
            method.instructions.add(new InsnNode(Opcodes.POP));
            method.instructions.add(new InvokeDynamicInsnNode("apply", "()Ljava/lang/Object;", bootstrap, source));
            method.instructions.add(new InsnNode(Opcodes.POP)); method.instructions.add(new InsnNode(Opcodes.RETURN)); constants.methods.add(method);
            check(pass(transformer, "rewriteLegacyRenderInvalidationFamily", constants) == 2, "dirty indy/condy references missed"); verify(constants);
        }
        var override = node("fixture/DirtyOverride", RENDERER); override.methods.add(new MethodNode(Opcodes.ACC_PUBLIC, "m_109494_", "(IIIIII)V", null, null));
        try { pass(transformer, "rewriteLegacyRenderInvalidationFamily", override); throw new AssertionError("source renderer override silently bypassed"); }
        catch (InvocationTargetException expected) { check(expected.getCause() instanceof IllegalStateException, "wrong override failure"); }
    }

    private static void positionedModelData() throws Exception {
        ModelProperty<Integer> property = new ModelProperty<>();
        ModelData original = ModelData.builder().with(property, 1).build();
        ModelData refined = ModelData.builder().with(property, 2).build();
        BlockPos position = new BlockPos(17, 42, -1); BlockState state = Blocks.STONE.defaultBlockState();
        int[] calls = new int[2];
        var level = (BlockAndTintGetter)Proxy.newProxyInstance(SlotRenderFamilyCheck.class.getClassLoader(), new Class<?>[]{BlockAndTintGetter.class}, (proxy, method, args) -> {
            if (method.getName().equals("getModelData")) { check(args[0].equals(position), "snapshot position changed"); return original; }
            throw new AssertionError("unexpected live world query: " + method);
        });
        var model = new EmptyModel() {
            @Override public ModelData getModelData(BlockAndTintGetter view, BlockPos pos, BlockState received, ModelData data) {
                check(view == level && pos == position && received == state && data == original, "model-data refinement operands"); calls[0]++; return refined;
            }
            @Override public List<BakedQuad> getQuads(BlockState received, Direction side, RandomSource random, ModelData data, LegacyRenderType layer) {
                check(data == refined && calls[0] == 1, "refinement result/order not preserved"); calls[1]++; return List.of();
            }
        };
        var part = collect(model, level, position, state); part.getQuads(Direction.NORTH);
        check(calls[0] == 1 && calls[1] == 1, "source refinement callback skipped/duplicated");
        check(new EmptyModel().getModelData(level, position, state, original) == original, "default refinement lost snapshot identity");
        try { collect(new EmptyModel() { @Override public ModelData getModelData(BlockAndTintGetter v, BlockPos p, BlockState s, ModelData d) { return null; } }, level, position, state); throw new AssertionError("null model data silently accepted"); }
        catch (InvocationTargetException expected) { check(expected.getCause() instanceof NullPointerException, "wrong null refinement failure"); }
    }

    private static void screenTransitions() throws Exception {
        var transformer = new Forge1192LegacyClassTransformer();
        String client = "net/minecraft/client/Minecraft", descriptor = "(Lnet/minecraft/client/gui/screens/Screen;)V";
        var fixture = call(client, "m_91152_", descriptor, Opcodes.INVOKEVIRTUAL);
        check(pass(transformer, "rewriteLegacyScreenTransitions", fixture) == 1, "source screen transition missed"); verify(fixture);
        check(pass(transformer, "rewriteLegacyScreenTransitions", call("fixture/UnrelatedClient", "m_91152_", descriptor, Opcodes.INVOKEVIRTUAL)) == 0, "unrelated screen receiver rewritten");
        check(pass(transformer, "rewriteLegacyScreenTransitions", call(client, "m_91152_", descriptor, Opcodes.INVOKESTATIC)) == 0, "static screen operation rewritten");
        // Nested callable references use the same checked traversal as render operations.
        var constants = node("fixture/ScreenConstants", "java/lang/Object");
        var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "constant", "()V", null, null);
        var bootstrap = new Handle(Opcodes.H_INVOKESTATIC, "fixture/Bootstrap", "constant", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", false);
        var source = new Handle(Opcodes.H_INVOKEVIRTUAL, client, "m_91152_", descriptor, false);
        method.instructions.add(new LdcInsnNode(new ConstantDynamic("screen", "Ljava/lang/Object;", bootstrap, source)));
        method.instructions.add(new InsnNode(Opcodes.POP));
        method.instructions.add(new InvokeDynamicInsnNode("accept", "()Ljava/lang/Object;", bootstrap, source));
        method.instructions.add(new InsnNode(Opcodes.POP)); method.instructions.add(new InsnNode(Opcodes.RETURN)); constants.methods.add(method);
        check(pass(transformer, "rewriteLegacyScreenTransitions", constants) == 2, "screen callable references missed"); verify(constants);
    }
    private static net.minecraft.client.renderer.block.dispatch.BlockStateModelPart collect(LegacyBakedModel source, BlockAndTintGetter level, BlockPos pos, BlockState state) throws Exception {
        Class<?> model = Class.forName("com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192ModelBakingBridge$LegacyBlockModel");
        var constructor = model.getDeclaredConstructors()[0]; constructor.setAccessible(true);
        Object adapter = constructor.newInstance(source, state, null);
        var method = model.getMethod("collectParts", BlockAndTintGetter.class, BlockPos.class, BlockState.class, RandomSource.class, List.class); method.setAccessible(true);
        var output = new ArrayList<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart>();
        method.invoke(adapter, level, pos, state, RandomSource.create(0), output); return output.getFirst();
    }
    private static class EmptyModel implements LegacyBakedModel {
        public List<BakedQuad> m_213637_(BlockState s, Direction d, RandomSource r) { return List.of(); }
        public boolean m_7541_() { return true; } public boolean m_7539_() { return true; }
        public boolean m_7547_() { return true; } public boolean m_7521_() { return false; }
        public TextureAtlasSprite m_6160_() { return null; }
        public LegacyItemOverrides m_7343_() { return null; }
    }
    private static MethodNode read(String name, String owner, String field, String desc, boolean isStatic) {
        var method = new MethodNode(Opcodes.ACC_PUBLIC | (isStatic ? Opcodes.ACC_STATIC : 0), name, "()" + desc, null, null);
        if (!isStatic) method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new FieldInsnNode(isStatic ? Opcodes.GETSTATIC : Opcodes.GETFIELD, owner, field, desc));
        method.instructions.add(new InsnNode(Type.getType(desc).getOpcode(Opcodes.IRETURN))); return method;
    }
    private static MethodNode readStaticReceiver(String name, String owner, String field) {
        var method = read(name, owner, field, "I", false); method.access |= Opcodes.ACC_STATIC; method.desc = "(L" + owner + ";)I"; return method;
    }
    private static ClassNode call(String owner, String name, String desc, int opcode) {
        var fixture = node("fixture/RenderCaller", "java/lang/Object");
        boolean instance = opcode != Opcodes.INVOKESTATIC;
        var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "call", instance ? "(L" + owner + ";" + desc.substring(1) : desc, null, null);
        int local = 0; if (instance) method.instructions.add(new VarInsnNode(Opcodes.ALOAD, local++));
        for (Type arg : Type.getArgumentTypes(desc)) { method.instructions.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), local)); local += arg.getSize(); }
        method.instructions.add(new MethodInsnNode(opcode, owner, name, desc, false)); method.instructions.add(new InsnNode(Opcodes.RETURN)); fixture.methods.add(method); return fixture;
    }
    private static ClassNode node(String name, String parent) { var node = new ClassNode(); node.version = Opcodes.V17; node.access = Opcodes.ACC_PUBLIC; node.name = name; node.superName = parent; return node; }
    private static byte[] bytes(ClassNode node) { var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer); return writer.toByteArray(); }
    private static int pass(Object transformer, String name, ClassNode node) throws Exception { var method = Forge1192LegacyClassTransformer.class.getDeclaredMethod(name, ClassNode.class); method.setAccessible(true); return (int)method.invoke(transformer, node); }
    private static void verify(ClassNode node) throws Exception { for (var method : node.methods) { method.maxLocals = Type.getArgumentsAndReturnSizes(method.desc) >> 2; method.maxStack = 20; new Analyzer<>(new BasicVerifier()).analyze(node.name, method); } }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
