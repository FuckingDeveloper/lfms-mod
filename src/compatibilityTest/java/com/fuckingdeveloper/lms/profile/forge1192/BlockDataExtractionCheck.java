package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import java.util.HashMap;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Source callback-header ownership across managed-evidence rebinding, without a native world. */
public final class BlockDataExtractionCheck implements Opcodes {
    private static final String ROOT="net/minecraft/world/level/block/entity/BlockEntity";
    private static final String CONTAINER="net/minecraft/world/level/block/entity/BaseContainerBlockEntity";
    public static void run() {
        var bytes=new HashMap<String,byte[]>();
        var evidence=new Forge1192BlockDataRewriter.Evidence() {
            public byte[] managedBytes(String owner){return bytes.get(owner);}
            public boolean subtype(ClassNode node,String ancestor){return ancestor.equals(ROOT)&&node.superName.equals("fixture/Middle");}
            public boolean subtype(String owner,String ancestor){return owner.equals(ancestor);}
            public String sourceMethod(ClassNode node,String owner,String name,String descriptor){throw new AssertionError("unrelated loot identity queried");}
        };
        var component=new Forge1192BlockDataRewriter(evidence,new SemanticInvocationRewriter(evidence::subtype));
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
    private static boolean nativeWrapper(ClassNode node){return node.methods.stream().anyMatch(m->m.name.equals("loadAdditional")&&m.desc.equals("(Lnet/minecraft/world/level/storage/ValueInput;)V"));}
    private static ClassNode child(){var node=new ClassNode();node.version=V17;node.access=ACC_PUBLIC;node.name="fixture/Child";node.superName="fixture/Middle";var load=new MethodNode(ACC_PUBLIC,"load","(Lnet/minecraft/nbt/CompoundTag;)V",null,null);load.instructions.add(new InsnNode(RETURN));node.methods.add(load);return node;}
    private static byte[] middle(boolean callback){var node=new ClassNode();node.version=V17;node.access=ACC_PUBLIC;node.name="fixture/Middle";node.superName=CONTAINER;if(callback){var load=new MethodNode(ACC_PUBLIC,"load","(Lnet/minecraft/nbt/CompoundTag;)V",null,null);load.instructions.add(new InsnNode(RETURN));node.methods.add(load);}var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();}
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
