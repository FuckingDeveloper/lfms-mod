import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.security.MessageDigest;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

/** Build evidence from immutable source/target entity registry constructors, without loading game classes.
 * Arguments: mapped source JAR, patched target JAR, output TSV. Requires ASM/tree/analysis on the classpath.
 */
public class GenerateEntityRelocations {
    record RegistryEvidence(Map<String,String> factories,String classHash){}
    static final String BUILDER="net/minecraft/world/entity/EntityType$Builder";
    static ClassNode read(JarFile jar,String owner) throws Exception {
        var entry=jar.getJarEntry(owner+".class");if(entry==null)throw new IllegalStateException("Missing evidence class "+owner);
        try(var input=jar.getInputStream(entry)){var node=new ClassNode();new ClassReader(input).accept(node,0);return node;}
    }
    static RegistryEvidence inspect(Path jarPath,String owner) throws Exception {
        try(var jar=new JarFile(jarPath.toFile(),false)) {
            var node=read(jar,owner);var builder=read(jar,BUILDER);
            Map<String,String> keys=jar.getJarEntry("net/minecraft/world/entity/EntityTypeIds.class")==null?Map.of():keys(read(jar,"net/minecraft/world/entity/EntityTypeIds"));
            var method=node.methods.stream().filter(m->m.name.equals("<clinit>")).findFirst().orElseThrow();
            var frames=new Analyzer<>(new SourceInterpreter()).analyze(owner,method);
            var result=new TreeMap<String,String>();
            for(var instruction:method.instructions)if(instruction instanceof MethodInsnNode call
                    &&call.getOpcode()==Opcodes.INVOKESTATIC&&call.owner.equals(owner)&&call.name.equals("register")
                    &&(call.desc.equals("(Ljava/lang/String;L"+BUILDER+";)Lnet/minecraft/world/entity/EntityType;")
                    ||call.desc.equals("(Lnet/minecraft/resources/ResourceKey;L"+BUILDER+";)Lnet/minecraft/world/entity/EntityType;"))) {
                var frame=frames[method.instructions.indexOf(call)];
                var id=frame.getStack(frame.getStackSize()-2);var value=frame.getStack(frame.getStackSize()-1);
                if(id.insns.size()!=1)continue;
                var keySource=id.insns.iterator().next();
                String key=keySource instanceof LdcInsnNode literal&&literal.cst instanceof String text?text
                        :keySource instanceof FieldInsnNode field?keys.get(field.owner+"#"+field.name):null;
                if(key==null)continue;
                String factory=factory(value,method,frames,builder,new HashSet<>());
                if(factory!=null&&result.putIfAbsent(key,factory)!=null)throw new IllegalStateException("Ambiguous entity registry ID "+key);
            }
            byte[] bytes;try(var input=jar.getInputStream(jar.getJarEntry(owner+".class"))){bytes=input.readAllBytes();}
            return new RegistryEvidence(result,HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
        }
    }
    static Map<String,String> keys(ClassNode node) throws Exception {
        var method=node.methods.stream().filter(m->m.name.equals("<clinit>")).findFirst().orElseThrow();
        var frames=new Analyzer<>(new SourceInterpreter()).analyze(node.name,method);var keys=new HashMap<String,String>();
        var creator=node.methods.stream().filter(m->m.name.equals("create")&&m.desc.equals("(Ljava/lang/String;)Lnet/minecraft/resources/ResourceKey;")).findFirst().orElseThrow();
        // Verify that these are vanilla entity registry keys, not unrelated same-shaped constants.
        boolean registry=false,identifier=false,keyFactory=false;
        for(var instruction:creator.instructions) {
            if(instruction instanceof FieldInsnNode f&&f.owner.equals("net/minecraft/core/registries/Registries")&&f.name.equals("ENTITY_TYPE"))registry=true;
            if(instruction instanceof MethodInsnNode c&&c.owner.equals("net/minecraft/resources/Identifier")&&c.name.equals("withDefaultNamespace"))identifier=true;
            if(instruction instanceof MethodInsnNode c&&c.owner.equals("net/minecraft/resources/ResourceKey")&&c.name.equals("create"))keyFactory=true;
        }
        if(!registry||!identifier||!keyFactory)throw new IllegalStateException("Unverified entity-key declaration operation");
        for(var instruction:method.instructions)if(instruction instanceof FieldInsnNode field&&field.getOpcode()==Opcodes.PUTSTATIC&&field.owner.equals(node.name)) {
            var frame=frames[method.instructions.indexOf(field)];var value=frame.getStack(frame.getStackSize()-1);
            if(value.insns.size()!=1||!(value.insns.iterator().next() instanceof MethodInsnNode call)||!call.owner.equals(node.name)||!call.name.equals("create"))continue;
            var input=frames[method.instructions.indexOf(call)];var id=input.getStack(input.getStackSize()-1);
            if(id.insns.size()==1&&id.insns.iterator().next() instanceof LdcInsnNode literal&&literal.cst instanceof String text)keys.put(node.name+"#"+field.name,text);
        }
        return keys;
    }
    static String factory(SourceValue value,MethodNode method,Frame<SourceValue>[] frames,ClassNode builder,Set<AbstractInsnNode> seen) {
        if(value.insns.size()!=1)return null;
        var instruction=value.insns.iterator().next();if(!seen.add(instruction)||!(instruction instanceof MethodInsnNode call)||!call.owner.equals(BUILDER))return null;
        var frame=frames[method.instructions.indexOf(call)];
        if(call.getOpcode()==Opcodes.INVOKESTATIC&&call.name.equals("of")&&call.desc.equals("(Lnet/minecraft/world/entity/EntityType$EntityFactory;Lnet/minecraft/world/entity/MobCategory;)L"+BUILDER+";")) {
            var factory=frame.getStack(frame.getStackSize()-2);
            if(factory.insns.size()!=1||!(factory.insns.iterator().next() instanceof InvokeDynamicInsnNode lambda)
                    ||!lambda.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory")
                    ||!lambda.desc.equals("()Lnet/minecraft/world/entity/EntityType$EntityFactory;"))return null;
            for(var arg:lambda.bsmArgs)if(arg instanceof Handle handle&&handle.getTag()==Opcodes.H_NEWINVOKESPECIAL
                    &&handle.getDesc().equals("(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V"))return handle.getOwner();
            return null;
        }
        if(call.getOpcode()!=Opcodes.INVOKEVIRTUAL||!Type.getReturnType(call.desc).getDescriptor().equals("L"+BUILDER+";"))return null;
        var member=builder.methods.stream().filter(m->m.name.equals(call.name)&&m.desc.equals(call.desc)).findFirst().orElse(null);
        if(member==null)return null;
        boolean returned=false;
        for(var op:member.instructions)if(op.getOpcode()==Opcodes.ARETURN) {
            returned=true;var previous=op.getPrevious();while(previous!=null&&previous.getOpcode()<0)previous=previous.getPrevious();
            if(!(previous instanceof VarInsnNode load)||load.getOpcode()!=Opcodes.ALOAD||load.var!=0)return null;
        }
        if(!returned)return null;
        return factory(frame.getStack(frame.getStackSize()-Type.getArgumentTypes(call.desc).length-1),method,frames,builder,seen);
    }
    public static void main(String[] args) throws Exception {
        var source=inspect(Path.of(args[0]),"net/minecraft/world/entity/EntityType");
        var target=inspect(Path.of(args[1]),"net/minecraft/world/entity/EntityTypes");
        var lines=new ArrayList<String>();
        lines.add("# Forge 1.19.2-43.4.0 -> Minecraft 26.3 / NeoForge 26.3.0.58-beta");
        lines.add("# source EntityType class sha256 "+source.classHash());lines.add("# target EntityTypes class sha256 "+target.classHash());
        lines.add("# registry ID -> direct source constructor -> direct target constructor; fluent builder returns verified this");
        var aliases=new HashMap<String,String>();
        for(var entry:source.factories().entrySet()) {
            String destination=target.factories().get(entry.getKey());if(destination==null||destination.equals(entry.getValue()))continue;
            String previous=aliases.putIfAbsent(entry.getValue(),destination);
            if(previous!=null&&!previous.equals(destination))throw new IllegalStateException("Ambiguous source entity implementation "+entry.getValue());
            lines.add("minecraft:"+entry.getKey()+"\t"+entry.getValue()+"\t"+destination);
        }
        Files.write(Path.of(args[2]),lines);
        System.out.println("Source direct factories="+source.factories().size()+", target="+target.factories().size()+", relocations="+(lines.size()-4));
    }
}
