package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.MethodRuleCatalog;
import com.fuckingdeveloper.lms.target.InstalledTargetAdapters;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;

/** Source-owned admitted spec operations; exact target ABI and source declarations are both required. */
final class Forge1192ConfigRules {
    private static final String ROOT="net/minecraftforge/common/ForgeConfigSpec",VALUE=ROOT+"$ConfigValue";
    private static final String VALUES=InstalledTargetAdapters.configValuesBridge();
    static List<MethodRuleCatalog.Rule> entries() {
        var result=new ArrayList<MethodRuleCatalog.Rule>();
        var builder=Set.of("<init>","comment","translation","worldRestart","push","pop","build","configure","define","defineInRange","defineInList","defineEnum");
        var spec=Set.of("isLoaded","isEmpty","getSpec","getValues","getLevelComment","getLevelTranslationKey","save","afterReload");
        var value=Set.of("get","set","save","getPath","getDefault","next","clearCache","getAsBoolean","getAsInt","getAsLong","getAsDouble");
        var contexts=Map.of("net/minecraftforge/fml/ModLoadingContext",Set.of("get","getActiveContainer","setActiveContainer","getActiveNamespace","registerConfig"),
                "net/minecraftforge/fml/javafmlmod/FMLJavaModLoadingContext",Set.of("get","getModEventBus"));
        for(String owner:Forge1192SourceHierarchyIndex.owners()) {
            boolean context=contexts.containsKey(owner);
            String alias=Forge1192NativeConfigRewriter.relocation(owner);
            if(alias==null||!context&&!owner.startsWith(ROOT))continue;
            if (!context && !owner.equals(ROOT) && !owner.equals(ROOT+"$Builder")
                    && !Set.of(VALUE,ROOT+"$BooleanValue",ROOT+"$IntValue",ROOT+"$LongValue",ROOT+"$DoubleValue",ROOT+"$EnumValue").contains(owner)) continue;
            var allowed=context?contexts.get(owner):owner.equals(ROOT+"$Builder")?builder:owner.equals(ROOT)?spec:value;
            var node=new ClassNode();new ClassReader(Forge1192SourceHierarchyIndex.methodBytes(owner)).accept(node,ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
            for(var method:node.methods) {
                if((method.access&Opcodes.ACC_PUBLIC)==0||!allowed.contains(method.name)||method.desc.contains(ROOT+"$ValueSpec;"))continue;
                int opcode=method.name.equals("<init>")?Opcodes.INVOKESPECIAL:(method.access&Opcodes.ACC_STATIC)!=0?Opcodes.INVOKESTATIC:Opcodes.INVOKEVIRTUAL;
                int tag=method.name.equals("<init>")?Opcodes.H_NEWINVOKESPECIAL:opcode==Opcodes.INVOKESTATIC?Opcodes.H_INVOKESTATIC:Opcodes.H_INVOKEVIRTUAL;
                Handle target=new Handle(tag,alias,method.name,Forge1192NativeConfigRewriter.descriptor(method.desc),false);
                if(owner.equals(VALUE)&&Set.of("get","set","save").contains(method.name)) {
                    String name=method.name.equals("save")?"saveValue":method.name;
                    target=new Handle(Opcodes.H_INVOKESTATIC,VALUES,name,"(L"+owner+";"+method.desc.substring(1),false);
                } else if(owner.equals(ROOT)&&Set.of("save","afterReload").contains(method.name))
                    target=new Handle(Opcodes.H_INVOKESTATIC,VALUES,method.name,"(L"+owner+";)V",false);
                else if(method.name.startsWith("getAs")&&owner.startsWith(ROOT+"$"))
                    target=new Handle(Opcodes.H_INVOKESTATIC,VALUES,method.name,"(L"+owner+";)"+Type.getReturnType(method.desc).getDescriptor(),false);
                if(!InstalledTargetAdapters.hasPublicConfigMethod(target.getOwner(),target.getName(),Forge1192NativeConfigRewriter.descriptor(target.getDesc())))continue;
                result.add(new MethodRuleCatalog.Rule("source-config-"+owner+"#"+method.name+method.desc,"SOURCE_CONFIG",
                        new MethodRuleCatalog.Member(owner,method.name,method.desc),opcode,target,
                        "Forge43.4.0 source declarations/config source; pinned target ABI; native storage with source cache/identity semantics"));
            }
        }
        return List.copyOf(result);
    }
}
