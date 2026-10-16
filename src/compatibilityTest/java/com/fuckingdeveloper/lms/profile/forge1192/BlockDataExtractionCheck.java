package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataTargetLayout;
import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataTargetLayout.Callback;
import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataTargetLayout.Operation;
import com.fuckingdeveloper.lms.target.InstalledTargetAdapters;
import java.util.HashMap;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Source callback-header ownership across managed-evidence rebinding, without a native world. */
public final class BlockDataExtractionCheck implements Opcodes {
    private static final String ROOT="net/minecraft/world/level/block/entity/BlockEntity";
    private static final String CONTAINER="net/minecraft/world/level/block/entity/BaseContainerBlockEntity";
    public static void run() {
        layoutChecks();
        var bytes=new HashMap<String,byte[]>();
        var evidence=new Forge1192BlockDataRewriter.Evidence() {
            public byte[] managedBytes(String owner){return bytes.get(owner);}
            public boolean subtype(ClassNode node,String ancestor){return ancestor.equals(ROOT)&&node.superName.equals("fixture/Middle");}
            public boolean subtype(String owner,String ancestor){return owner.equals(ancestor);}
            public String sourceMethod(ClassNode node,String owner,String name,String descriptor){throw new AssertionError("unrelated loot identity queried");}
        };
        var component=new Forge1192BlockDataRewriter(evidence,new SemanticInvocationRewriter(evidence::subtype),InstalledTargetAdapters.blockDataCallbacks());
        bytes.put("fixture/Middle",middle(true));
        var first=child();component.rewrite(first);check(!nativeWrapper(first),"duplicated inherited source callback");
        bytes.put("fixture/Middle",middle(false));
        var cached=child();component.rewrite(cached);check(!nativeWrapper(cached),"header snapshot changed without explicit rebind");
        component.clearEvidence();
        var rebound=child();component.rewrite(rebound);check(nativeWrapper(rebound),"old callback ancestry retained after rebind");
        check(rebound.methods.stream().anyMatch(m->m.name.equals("lms$sourceLoad")),"source callback alias lost");
        var collision=child();collision.methods.add(new MethodNode(ACC_PUBLIC,"lms$sourceLoad","(Lnet/minecraft/nbt/CompoundTag;)V",null,null));
        try {component.rewrite(collision);throw new AssertionError("source alias collision accepted");}catch(IllegalStateException expected){}
        System.out.println("PASS: extracted block-data pass preserves inherited dispatch, scoped raw-header snapshots/rebind invalidation and explicit alias collision failure");
    }
    private static void layoutChecks() {
        var installed = InstalledTargetAdapters.blockDataCallbacks();
        var callbacks = new java.util.EnumMap<Operation,
                Callback>(Operation.class);
        for (var operation : Operation.values()) {
            callbacks.put(operation, new Callback(
                    "native" + operation.name(), "Lfixture/" + operation.name() + ";",
                    operation == Operation.UPDATE_TAG ? "Lfixture/Tag;" : "V"));
        }
        var alternate = new BlockDataTargetLayout("synthetic-test-only", "fixture/NativeBase", callbacks);
        callbacks.clear();
        check(alternate.callbacks().size() == 4, "layout retains mutable caller map");
        reject(() -> alternate.callbacks().clear());
        reject(() -> new BlockDataTargetLayout("missing", ROOT, callbacks));
        reject(() -> new Callback("bad", "I", "V"));
        reject(() -> new Callback("bad", "Lfixture/Tag;;", "V"));
        callbacks.putAll(installed.callbacks());
        var load = Operation.LOAD;
        var save = Operation.SAVE;
        var update = Operation.UPDATE_TAG;
        callbacks.put(save, callbacks.get(load));
        reject(() -> new BlockDataTargetLayout("duplicate", ROOT, callbacks));
        callbacks.putAll(installed.callbacks());
        callbacks.put(load, new Callback("load", "Lfixture/Input;", "Lfixture/Tag;"));
        reject(() -> new BlockDataTargetLayout("result", ROOT, callbacks));
        callbacks.putAll(installed.callbacks());
        callbacks.put(update, new Callback("update", "Lfixture/Input;", "V"));
        reject(() -> new BlockDataTargetLayout("result", ROOT, callbacks));
        var evidence = new Forge1192BlockDataRewriter.Evidence() {
            public byte[] managedBytes(String owner) { return null; }
            public boolean subtype(ClassNode node, String ancestor) { return ROOT.equals(ancestor); }
            public boolean subtype(String owner, String ancestor) { return owner.equals(ancestor); }
            public String sourceMethod(ClassNode node, String owner, String name, String descriptor) { throw new AssertionError("unrelated identity"); }
        };
        var node = child(); node.superName = ROOT;
        node.methods.getFirst().instructions.insertBefore(node.methods.getFirst().instructions.getFirst(), new VarInsnNode(ALOAD, 0));
        node.methods.getFirst().instructions.insertBefore(node.methods.getFirst().instructions.getLast(), new VarInsnNode(ALOAD, 1));
        node.methods.getFirst().instructions.insertBefore(node.methods.getFirst().instructions.getLast(), new MethodInsnNode(INVOKESPECIAL, ROOT, "load", "(Lnet/minecraft/nbt/CompoundTag;)V", false));
        new Forge1192BlockDataRewriter(evidence, new SemanticInvocationRewriter(evidence::subtype), alternate).rewrite(node);
        for (var operation : Operation.values()) {
            var callback = alternate.callback(operation);
            var wrapper = node.methods.stream().filter(m -> m.name.equals(callback.name()) && m.desc.equals(callback.descriptor())).findFirst().orElseThrow();
            check(java.util.Arrays.stream(wrapper.instructions.toArray()).anyMatch(insn -> insn instanceof MethodInsnNode call
                    && call.desc.equals("(Lfixture/NativeBase;" + callback.descriptor().substring(1))), "wrapper ignored explicit receiver/descriptor");
        }
        check(node.methods.stream().flatMap(m -> java.util.Arrays.stream(m.instructions.toArray())).anyMatch(insn -> insn instanceof MethodInsnNode call
                && call.name.equals("sourceInput") && call.desc.endsWith("Lfixture/LOAD;")), "source-super view ignored native input");
        var profile = new Forge1192Profile(alternate);
        check(profile.blockDataTarget().equals(alternate), "profile lost target layout");
        var transformer = profile.createTransformer("CLIENT");
        var factoryFixture = child(); factoryFixture.superName = ROOT;
        var writer = new ClassWriter(0); factoryFixture.accept(writer);
        var transformed = new ClassNode();
        new ClassReader(transformer.transform("fixture.Child", writer.toByteArray()).bytes()).accept(transformed, 0);
        check(transformed.methods.stream().anyMatch(m -> m.name.equals(alternate.callback(load).name())
                && m.desc.equals(alternate.callback(load).descriptor())), "profile factory lost explicit target layout");
        System.out.println("PASS: block-data layout rejects malformed ABI and immutable synthetic layout drives all wrappers/source-super views");
    }
    private static void reject(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException | UnsupportedOperationException expected) { return; }
        throw new AssertionError("invalid/mutable layout accepted");
    }
    private static boolean nativeWrapper(ClassNode node){return node.methods.stream().anyMatch(m->m.name.equals("loadAdditional")&&m.desc.equals("(Lnet/minecraft/world/level/storage/ValueInput;)V"));}
    private static ClassNode child(){var node=new ClassNode();node.version=V17;node.access=ACC_PUBLIC;node.name="fixture/Child";node.superName="fixture/Middle";var load=new MethodNode(ACC_PUBLIC,"load","(Lnet/minecraft/nbt/CompoundTag;)V",null,null);load.instructions.add(new InsnNode(RETURN));node.methods.add(load);return node;}
    private static byte[] middle(boolean callback){var node=new ClassNode();node.version=V17;node.access=ACC_PUBLIC;node.name="fixture/Middle";node.superName=CONTAINER;if(callback){var load=new MethodNode(ACC_PUBLIC,"load","(Lnet/minecraft/nbt/CompoundTag;)V",null,null);load.instructions.add(new InsnNode(RETURN));node.methods.add(load);}var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();}
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
