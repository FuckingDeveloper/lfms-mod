package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;

/** Proven source read operations; registration and arbitrary registry subclasses are separate contracts. */
final class Forge1192RegistryQueryRewriter {
    interface Canonical { String name(String owner,String name,String descriptor); }
    private static final Set<String> ROOTS=Set.of("net/minecraft/core/Registry","net/minecraft/core/MappedRegistry","net/minecraft/core/DefaultedRegistry");
    private static final String ID_MAP="net/minecraft/core/IdMap";
    private static final boolean REGISTRY_ID_MAP=registryIdMap();
    private static final String ID="Lnet/minecraft/resources/ResourceLocation;",KEY="Lnet/minecraft/resources/ResourceKey;",OBJECT="Ljava/lang/Object;",OPTIONAL="Ljava/util/Optional;";
    private static final Map<String,String> QUERIES=Map.ofEntries(
            Map.entry("get("+ID+")"+OBJECT,"get"),Map.entry("get("+KEY+")"+OBJECT,"get"),
            Map.entry("getOptional("+ID+")"+OPTIONAL,"getOptional"),Map.entry("getOptional("+KEY+")"+OPTIONAL,"getOptional"),
            Map.entry("getKey("+OBJECT+")"+ID,"getKey"),Map.entry("getResourceKey("+OBJECT+")"+OPTIONAL,"getResourceKey"),
            Map.entry("getId("+OBJECT+")I","getId"),Map.entry("byId(I)"+OBJECT,"byId"),
            Map.entry("getDefaultKey()"+ID,"getDefaultKey"),
            Map.entry("getRandom(Lnet/minecraft/util/RandomSource;)"+OPTIONAL,"getRandom"),
            Map.entry("containsKey("+ID+")Z","containsKey"),Map.entry("containsKey("+KEY+")Z","containsKey"));
    private static boolean registryIdMap() {
        byte[] bytes=Forge1192SourceHierarchyIndex.methodBytes("net/minecraft/core/Registry");
        if(bytes==null)throw new IllegalStateException("Missing source Registry declaration evidence");
        return List.of(new ClassReader(bytes).getInterfaces()).contains(ID_MAP);
    }
    static int rewrite(ClassNode node,Forge1192MethodDeclarations declarations,Canonical canonical,SemanticInvocationRewriter calls) {
        return calls.rewrite(node,(opcode,owner,name,descriptor)->{
            if(opcode!=Opcodes.INVOKEVIRTUAL||!ROOTS.contains(owner))return null;
            String mapped=name.startsWith("m_")?canonical.name(owner,name,descriptor):name;
            String target=mapped==null?null:QUERIES.get(mapped+descriptor);
            if(target==null)return null;
            String declaration=declarations.owner(owner,mapped,descriptor);
            // Source Registry inherits byId from IdMap; the common declaration walker
            // deliberately follows class parents only. Prove this exact interface edge.
            if(declaration==null&&REGISTRY_ID_MAP&&mapped.equals("byId")&&descriptor.equals("(I)"+OBJECT))
                declaration=declarations.owner(ID_MAP,mapped,descriptor);
            if(declaration==null||!ROOTS.contains(declaration)&&!declaration.equals(ID_MAP))return null;
            String receiver=target.equals("getDefaultKey")?"net/minecraft/core/DefaultedRegistry":"net/minecraft/core/Registry";
            return new Handle(Opcodes.H_INVOKESTATIC,com.fuckingdeveloper.lms.target.InstalledTargetAdapters.registryQueriesBridge(),
                    target,"(L"+receiver+";"+descriptor.substring(1),false);
        });
    }
}
