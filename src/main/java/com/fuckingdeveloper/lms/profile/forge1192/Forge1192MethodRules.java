package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.MethodRuleCatalog;
import java.util.List;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;

/** Evidence-backed migrated families shared by admission and source transformation. */
public final class Forge1192MethodRules {
    public static final String WORK = "net/minecraftforge/fml/event/lifecycle/ParallelDispatchEvent";
    private static final MethodRuleCatalog CATALOG = new MethodRuleCatalog(entries());
    private Forge1192MethodRules() {}
    private static List<MethodRuleCatalog.Rule> entries(){var result=new java.util.ArrayList<MethodRuleCatalog.Rule>(Forge1192ConfigRules.entries());result.addAll(Forge1192MenuRewriter.entries());result.add(explicitListener());result.add(work("Runnable","java/lang/Runnable"));result.add(work("Supplier","java/util/function/Supplier"));return List.copyOf(result);}
    private static MethodRuleCatalog.Rule explicitListener() {
        String bus="net/minecraftforge/eventbus/api/IEventBus";
        String desc="(Lnet/minecraftforge/eventbus/api/EventPriority;ZLjava/lang/Class;Ljava/util/function/Consumer;)V";
        return new MethodRuleCatalog.Rule("source-explicit-listener","EXPLICIT_LISTENER",
                new MethodRuleCatalog.Member(bus,"addListener",desc),Opcodes.INVOKEINTERFACE,
                com.fuckingdeveloper.lms.target.InstalledTargetAdapters.explicitListenerBridge(bus,desc),
                "Forge43.4.0 IEventBus explicit class/priority/cancellation; native bus with recorded source scope and unregister");
    }
    private static MethodRuleCatalog.Rule work(String id, String argument) {
        String desc = "(L" + argument + ";)Ljava/util/concurrent/CompletableFuture;";
        return new MethodRuleCatalog.Rule("lifecycle-work-" + id, "LIFECYCLE_WORK",
                new MethodRuleCatalog.Member(WORK, "enqueueWork", desc), Opcodes.INVOKEVIRTUAL,
                com.fuckingdeveloper.lms.target.InstalledTargetAdapters.lifecycleWorkBridge(desc),
                "Forge43.4.0 ParallelDispatchEvent/DeferredWorkQueue; FML12.0.9 native queue; scoped source owner");
    }
    public static Handle bridge(int opcode, String owner, String name, String descriptor, Forge1192MethodDeclarations declarations) {
        var rule = CATALOG.resolve(opcode, owner, name, descriptor, declarations::owner);
        if (rule.isPresent() && java.util.Set.of("LIFECYCLE_WORK","EXPLICIT_LISTENER").contains(rule.get().family())) return rule.get().bridge();
        if (name.equals("enqueueWork") && declarations.subtype(owner, WORK))
            throw new IllegalStateException("Unadapted lifecycle work invocation/override: " + owner + "#" + name + descriptor + " opcode=" + opcode);
        return null;
    }
    public static int rewrite(org.objectweb.asm.tree.ClassNode node, Forge1192MethodDeclarations declarations,
            com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter invocations) {
        for (var method : node.methods) for (var insn : method.instructions) {
            if (insn instanceof org.objectweb.asm.tree.MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESTATIC)
                bridge(call.getOpcode(), call.owner, call.name, call.desc, declarations);
            else if (insn instanceof org.objectweb.asm.tree.LdcInsnNode ldc) validateStatic(ldc.cst, declarations);
            else if (insn instanceof org.objectweb.asm.tree.InvokeDynamicInsnNode indy)
                for(Object value : indy.bsmArgs) validateStatic(value, declarations);
        }
        return invocations.rewrite(node,(opcode,owner,name,descriptor)->bridge(opcode,owner,name,descriptor,declarations));
    }
    private static void validateStatic(Object value, Forge1192MethodDeclarations declarations) {
        if (value instanceof Handle handle && handle.getTag() == Opcodes.H_INVOKESTATIC)
            bridge(Opcodes.INVOKESTATIC,handle.getOwner(),handle.getName(),handle.getDesc(),declarations);
        else if (value instanceof org.objectweb.asm.ConstantDynamic dynamic)
            for(int i=0;i<dynamic.getBootstrapMethodArgumentCount();i++)validateStatic(dynamic.getBootstrapMethodArgument(i),declarations);
    }
    public static java.util.Optional<String> family(String owner, String name, String descriptor, Forge1192MethodDeclarations declarations) {
        for(int opcode:new int[]{Opcodes.INVOKESTATIC,Opcodes.INVOKEVIRTUAL,Opcodes.INVOKESPECIAL,Opcodes.INVOKEINTERFACE}) {
            var rule=CATALOG.resolve(opcode,owner,name,descriptor,declarations::owner);
            if(rule.isPresent())return java.util.Optional.of(rule.get().family());
        }
        return java.util.Optional.empty();
    }
    public static MethodRuleCatalog catalog() { return CATALOG; }
}
