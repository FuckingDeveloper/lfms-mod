package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.MethodRuleCatalog;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192CompatibilitySurface;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.Forge1192RegistrationPlanner;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class MethodCatalogCheck implements Opcodes {
    private static final String ROOT = Forge1192MethodRules.WORK;
    public static final String EVENT = "net/minecraftforge/fml/event/lifecycle/FMLCommonSetupEvent";
    private static final String DESC = "(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;";
    public static void run() throws Exception {
        var raw = new HashMap<String,byte[]>(); var reads = new java.util.concurrent.atomic.AtomicInteger();
        var declarations = new Forge1192MethodDeclarations(owner -> { reads.incrementAndGet(); return raw.get(owner); });
        int descendants = 0;
        for (String owner : Forge1192SourceHierarchyIndex.owners()) {
            if (!declarations.subtype(owner, ROOT)) continue;
            for (var rule : Forge1192MethodRules.catalog().entries())
                check(Forge1192MethodRules.catalog().resolve(INVOKEVIRTUAL, owner, "enqueueWork", rule.source().descriptor(), declarations::owner).isPresent(), "pinned event subclass lost inherited rule: " + owner);
            descendants++;
        }
        check(descendants >= 8, "source family evidence incomplete");
        raw.put("fixture/Child", header(false));
        check(ROOT.equals(declarations.owner("fixture/Child", "enqueueWork", DESC)), "managed ancestor not resolved");
        int before = reads.get(); declarations.owner("fixture/Child", "enqueueWork", DESC);
        check(reads.get() == before, "declaration cache missed");
        raw.put("fixture/Child", header(true));
        check(ROOT.equals(declarations.owner("fixture/Child", "enqueueWork", DESC)), "snapshot changed without rebind");
        declarations.clear();
        check("fixture/Child".equals(declarations.owner("fixture/Child", "enqueueWork", DESC)), "override retained cached base declaration");
        reject(() -> Forge1192MethodRules.bridge(INVOKEVIRTUAL, "fixture/Child", "enqueueWork", DESC, declarations));
        for (int opcode : new int[]{INVOKESTATIC, INVOKESPECIAL, INVOKEINTERFACE})
            reject(() -> Forge1192MethodRules.bridge(opcode, EVENT, "enqueueWork", DESC, declarations));
        reject(() -> Forge1192MethodRules.bridge(INVOKEVIRTUAL, EVENT, "enqueueWork", "()V", declarations));
        check(Forge1192MethodRules.bridge(INVOKEVIRTUAL, "fixture/Unrelated", "enqueueWork", DESC, declarations) == null, "descriptor-only selection");
        var invalid = new ClassNode();new ClassReader(com.fuckingdeveloper.lms.check.LifecycleWorkFixture.bytes()).accept(invalid,0);
        for(var method : invalid.methods)for(var insn : method.instructions)
            if(insn instanceof MethodInsnNode call && call.name.equals("enqueueWork"))call.setOpcode(INVOKESTATIC);
        var invalidWriter=new ClassWriter(0);invalid.accept(invalidWriter);
        reject(() -> new Forge1192LegacyClassTransformer().transform("fixture.WorkCalls",invalidWriter.toByteArray()));
        check(declarations.owner("fixture/Missing","enqueueWork",DESC)==null,"missing source inferred");
        raw.put("fixture/Missing",header("fixture/Missing",false));
        check(declarations.owner("fixture/Missing","enqueueWork",DESC)==null,"negative snapshot changed without rebind");
        declarations.clear();
        check(ROOT.equals(declarations.owner("fixture/Missing","enqueueWork",DESC)),"negative lookup not invalidated");
        var rule = Forge1192MethodRules.catalog().entries().getFirst();
        try { new MethodRuleCatalog(List.of(rule,rule)); throw new AssertionError("duplicate rule accepted"); } catch (IllegalArgumentException expected) {}
        var surface = new Forge1192CompatibilitySurface();
        var good = new Forge1192RegistrationPlanner.Boundary("fixture", EVENT.replace('/','.'), "enqueueWork", DESC, Forge1192RegistrationPlanner.ExecutionPhase.RUNTIME, INVOKEVIRTUAL);
        check(surface.assess(List.of(good)).executable(), "admission does not share implemented rule");
        var bad = new Forge1192RegistrationPlanner.Boundary("fixture", EVENT.replace('/','.'), "enqueueWork", DESC,
                Forge1192RegistrationPlanner.ExecutionPhase.RUNTIME, INVOKESTATIC);
        check(!surface.assess(List.of(good,bad)).executable(), "mixed invocation shape admitted");
        var original = com.fuckingdeveloper.lms.check.LifecycleWorkFixture.bytes();
        var artifact=java.nio.file.Files.createTempFile("lms-catalog-fixture", ".jar");
        try(var jar=new java.util.jar.JarOutputStream(java.nio.file.Files.newOutputStream(artifact))) {
            jar.putNextEntry(new java.util.jar.JarEntry("fixture/WorkCalls.class"));jar.write(original);jar.closeEntry();
        }
        try {
            var plan=new Forge1192RegistrationPlanner().plan(artifact,"fixture.WorkCalls");
            check(plan.complete()&&plan.boundaries().stream().anyMatch(b->b.name().equals("enqueueWork")&&b.opcode()==INVOKEVIRTUAL),"planner lost nested callable invocation provenance");
            check(surface.assess(plan.boundaries(),artifact).executable(),"planner and runtime rule selection diverge");
        } finally {java.nio.file.Files.delete(artifact);}
 var transformed = new Forge1192LegacyClassTransformer().transform("fixture.WorkCalls", original);
        var node = new ClassNode(); new ClassReader(transformed.bytes()).accept(node,0);
        for (var method : node.methods) for (var insn : method.instructions)
            if (insn instanceof MethodInsnNode call && call.name.equals("enqueueWork"))
                check(call.getOpcode() == INVOKESTATIC && call.owner.equals(rule.bridge().getOwner()), "call did not use catalog bridge");
        check(Arrays.equals(original,com.fuckingdeveloper.lms.check.LifecycleWorkFixture.bytes()), "input fixture mutated");
        System.out.println("PASS: catalog/shared admission rules, " + descendants + " inherited source event declarations, owner/descriptor/opcode/override negatives and scoped cache invalidation");
    }
    private static byte[] header(boolean override) { return header("fixture/Child",override); }
    private static byte[] header(String name, boolean override) {
        var node=new ClassNode();node.version=V17;node.name=name;node.superName=EVENT;
        if(override)node.methods.add(new MethodNode(ACC_PUBLIC,"enqueueWork",DESC,null,null));
        var writer=new ClassWriter(0);node.accept(writer);return writer.toByteArray();
    }
    private static void reject(Runnable action) { try { action.run(); } catch(IllegalStateException expected) { return; } throw new AssertionError("unproven invocation accepted"); }
    private static void check(boolean condition,String message) { if(!condition)throw new AssertionError(message); }
}
