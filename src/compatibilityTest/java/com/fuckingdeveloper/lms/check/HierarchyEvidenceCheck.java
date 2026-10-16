package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.util.concurrent.atomic.AtomicInteger;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;

final class HierarchyEvidenceCheck {
    static void run() throws Exception {
        var transformer = new Forge1192LegacyClassTransformer(); var reads = new AtomicInteger();
        var source = node("net/minecraft/world/level/block/Block"); byte[] original = bytes(source);
        transformer.bindManagedClassBytes(name -> { if (!name.equals(source.name)) return null; reads.incrementAndGet(); return original; });
        var query = Forge1192LegacyClassTransformer.class.getDeclaredMethod("isManagedSubtypeOf", String.class, String.class); query.setAccessible(true);
        var nodeQuery = Forge1192LegacyClassTransformer.class.getDeclaredMethod("isManagedSubtypeOf", ClassNode.class, String.class); nodeQuery.setAccessible(true);
        check((boolean)query.invoke(transformer, source.name, source.superName), "positive source ancestry");
        check((boolean)query.invoke(transformer, source.name, "java/lang/Object") && reads.get() == 1, "raw graph bytes reread across subtype queries");
        source.superName = "java/lang/Object";
        check(!(boolean)nodeQuery.invoke(transformer, source, "net/minecraft/world/level/block/Block"), "mutated ClassNode used stale name-only ancestry");
        transformer.bindManagedClassBytes(name -> name.equals(source.name) ? bytes(source) : null);
        check(!(boolean)query.invoke(transformer, source.name, "net/minecraft/world/level/block/Block"), "positive evidence survived rebinding");
        transformer.bindManagedClassBytes(name -> name.equals(source.name) ? original : null);
        check((boolean)query.invoke(transformer, source.name, "net/minecraft/world/level/block/Block"), "negative evidence survived rebinding");
        System.out.println("PASS: indexed hierarchy evidence, raw-byte reuse, positive/negative rebinding and mutated-node ancestry");
    }
    private static ClassNode node(String parent) { var node = new ClassNode(); node.version = Opcodes.V17; node.access = Opcodes.ACC_PUBLIC; node.name = "fixture/IndexedHierarchy"; node.superName = parent; return node; }
    private static byte[] bytes(ClassNode node) { var writer = new ClassWriter(0); node.accept(writer); return writer.toByteArray(); }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
