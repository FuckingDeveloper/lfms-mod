package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import java.util.function.BiPredicate;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Exact source-generation worldgen ABI; target algorithms live in target/neoforge263/worldgen. */
final class Forge1192WorldgenRewriter {
    private static final String SOURCE="net/minecraft/world/level/levelgen/";
    private static final String RUNTIME="com/fuckingdeveloper/lms/profile/forge1192/runtime/worldgen/";
    private static final String ADAPTER="com/fuckingdeveloper/lms/target/neoforge263/worldgen/Target263WorldgenAdapter";
    private static final String DEFINITIONS=RUNTIME+"LegacyWorldgenDefinitions";
    private static final String TRUNK=SOURCE+"feature/trunkplacers/TrunkPlacer",FOLIAGE=SOURCE+"feature/foliageplacers/FoliagePlacer";
    private static final Map<String,String> TYPES=Map.ofEntries(
        Map.entry(SOURCE+"feature/Feature",RUNTIME+"LegacyFeature"),
        Map.entry(SOURCE+"feature/ConfiguredFeature",RUNTIME+"LegacyConfiguredFeature"),
        Map.entry(SOURCE+"feature/configurations/FeatureConfiguration",RUNTIME+"LegacyFeatureConfiguration"),
        Map.entry(SOURCE+"feature/configurations/TreeConfiguration",RUNTIME+"LegacyTreeConfiguration"),
        Map.entry(SOURCE+"feature/configurations/TreeConfiguration$TreeConfigurationBuilder",RUNTIME+"LegacyTreeConfiguration$TreeConfigurationBuilder"),
        Map.entry(SOURCE+"feature/configurations/OreConfiguration",RUNTIME+"LegacyOreConfiguration"),
        Map.entry(SOURCE+"feature/configurations/OreConfiguration$TargetBlockState",RUNTIME+"LegacyOreConfiguration$TargetBlockState"),
        Map.entry(SOURCE+"placement/PlacedFeature",RUNTIME+"LegacyPlacedFeature")
    );
    static String relocation(String name){return TYPES.get(name);}
    private static String callback(String owner,String name,String desc,BiPredicate<String,String> subtype){
        if(subtype.test(owner,TRUNK)){
            if((name.equals("m_7362_")||name.equals("type"))&&desc.equals("()L"+SOURCE+"feature/trunkplacers/TrunkPlacerType;"))return "lms$trunkType";
            if((name.equals("m_213934_")||name.equals("placeTrunk"))&&desc.equals("(Lnet/minecraft/world/level/LevelSimulatedReader;Ljava/util/function/BiConsumer;Lnet/minecraft/util/RandomSource;ILnet/minecraft/core/BlockPos;L"+SOURCE+"feature/configurations/TreeConfiguration;)Ljava/util/List;"))return "lms$placeTrunk";
            if(name.equals("m_70305_")&&desc.equals("(Lcom/mojang/serialization/codecs/RecordCodecBuilder$Instance;)Lcom/mojang/datafixers/Products$P3;"))return "lms$trunkParts";
            if(name.equals("m_226169_")&&desc.equals("(Lnet/minecraft/world/level/LevelSimulatedReader;Ljava/util/function/BiConsumer;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;L"+SOURCE+"feature/configurations/TreeConfiguration;)V"))return "lms$belowTrunk";
        }
        if(subtype.test(owner,FOLIAGE)){
            if((name.equals("m_5897_")||name.equals("type"))&&desc.equals("()L"+SOURCE+"feature/foliageplacers/FoliagePlacerType;"))return "lms$foliageType";
            if((name.equals("m_214116_")||name.equals("foliageHeight"))&&desc.equals("(Lnet/minecraft/util/RandomSource;IL"+SOURCE+"feature/configurations/TreeConfiguration;)I"))return "lms$foliageHeight";
            if((name.equals("m_214203_")||name.equals("shouldSkipLocation"))&&desc.equals("(Lnet/minecraft/util/RandomSource;IIIIZ)Z"))return "lms$skipLocation";
            if((name.equals("m_213633_")||name.equals("createFoliage"))&&desc.equals("(Lnet/minecraft/world/level/LevelSimulatedReader;Ljava/util/function/BiConsumer;Lnet/minecraft/util/RandomSource;L"+SOURCE+"feature/configurations/TreeConfiguration;IL"+FOLIAGE+"$FoliageAttachment;III)V"))return "lms$createFoliage";
            if(name.equals("m_68573_")&&desc.equals("(Lcom/mojang/serialization/codecs/RecordCodecBuilder$Instance;)Lcom/mojang/datafixers/Products$P2;"))return "lms$foliageParts";
        }
        return null;
    }
    private static Object callable(Object value,BiPredicate<String,String> subtype){
        if(value instanceof Handle h){
            if(h.getTag()==H_GETSTATIC&&h.getOwner().equals("net/minecraft/data/worldgen/placement/VegetationPlacements")&&(h.getName().equals("f_195420_")||h.getName().equals("TREE_THRESHOLD"))&&h.getDesc().equals("L"+SOURCE+"placement/PlacementModifier;"))return new Handle(H_INVOKESTATIC,ADAPTER,"treeThreshold","()"+h.getDesc(),false);
            if(h.getTag()==H_INVOKESTATIC&&h.getOwner().equals(SOURCE+"feature/stateproviders/BlockStateProvider")&&(h.getName().equals("m_191384_")||h.getName().equals("m_191382_")||h.getName().equals("simple"))&&(h.getDesc().equals("(Lnet/minecraft/world/level/block/state/BlockState;)L"+SOURCE+"feature/stateproviders/SimpleStateProvider;")||h.getDesc().equals("(Lnet/minecraft/world/level/block/Block;)L"+SOURCE+"feature/stateproviders/SimpleStateProvider;")))return new Handle(H_INVOKESTATIC,h.getOwner(),"of",h.getDesc(),true);
            var name=callback(h.getOwner(),h.getName(),h.getDesc(),subtype);return name==null?h:new Handle(h.getTag(),h.getOwner().equals(TRUNK)?RUNTIME+"LegacyTrunkPlacer":h.getOwner().equals(FOLIAGE)?RUNTIME+"LegacyFoliagePlacer":h.getOwner(),name,h.getDesc(),h.isInterface());}
        if(value instanceof ConstantDynamic c){Object[] args=new Object[c.getBootstrapMethodArgumentCount()];for(int i=0;i<args.length;i++)args[i]=callable(c.getBootstrapMethodArgument(i),subtype);return new ConstantDynamic(c.getName(),c.getDescriptor(),(Handle)callable(c.getBootstrapMethod(),subtype),args);}
        return value;
    }
    static int rewrite(ClassNode node,BiPredicate<String,String> subtype,BiPredicate<String,String> unitCodec){
        int edits=0;
        final String sourceModifier="net/minecraftforge/common/world/BiomeModifier",nativeModifier="net/neoforged/neoforge/common/world/BiomeModifier";
        if(subtype.test(node.name,sourceModifier)){
            var added=new ArrayList<MethodNode>();
            for(var method:node.methods){
                if(method.name.equals("modify")&&method.desc.equals("(Lnet/minecraft/core/Holder;L"+sourceModifier+"$Phase;Lnet/minecraftforge/common/world/ModifiableBiomeInfo$BiomeInfo$Builder;)V")){
                    var sourceMethod=new MethodNode(ACC_PUBLIC|ACC_SYNTHETIC,"lms$modifyBiome",method.desc,null,null);
                    for(int i=0;i<4;i++)sourceMethod.instructions.add(new VarInsnNode(ALOAD,i));
                    sourceMethod.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,node.name,method.name,method.desc,false));sourceMethod.instructions.add(new InsnNode(RETURN));added.add(sourceMethod);
                    if(!node.interfaces.contains(RUNTIME+"LegacyBiomeModifierCallbacks"))node.interfaces.add(RUNTIME+"LegacyBiomeModifierCallbacks");
                    var nativeMethod=new MethodNode(ACC_PUBLIC|ACC_SYNTHETIC,"modify","(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/core/Holder;L"+nativeModifier+"$Phase;Lnet/neoforged/neoforge/common/world/ModifiableBiomeInfo$BiomeInfo$Builder;)V",null,null);
                    for(int i=0;i<5;i++)nativeMethod.instructions.add(new VarInsnNode(ALOAD,i));
                    nativeMethod.instructions.add(new MethodInsnNode(INVOKESTATIC,"com/fuckingdeveloper/lms/target/neoforge263/worldgen/Target263BiomeModifierBridge","modify","(L"+RUNTIME+"LegacyBiomeModifierCallbacks;Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/core/Holder;L"+nativeModifier+"$Phase;Lnet/neoforged/neoforge/common/world/ModifiableBiomeInfo$BiomeInfo$Builder;)V",false));nativeMethod.instructions.add(new InsnNode(RETURN));added.add(nativeMethod);edits++;
                }
                if(method.name.equals("codec")&&method.desc.equals("()Lcom/mojang/serialization/Codec;")){
                    var body=new ArrayList<AbstractInsnNode>();for(var insn:method.instructions)if(insn.getOpcode()>=0)body.add(insn);
                    if(body.size()==2&&body.getFirst() instanceof FieldInsnNode field&&field.getOpcode()==GETSTATIC&&unitCodec.test(field.owner,field.name)){
                        // The existing proven-unit producer pass migrates this same field.
                        method.desc="()Lcom/mojang/serialization/MapCodec;";if(method.signature!=null)method.signature=method.signature.replace("Lcom/mojang/serialization/Codec<","Lcom/mojang/serialization/MapCodec<");edits++;
                    }else throw new UnsupportedOperationException("Unproven source biome modifier codec operation: "+node.name);
                }
            }
            node.methods.addAll(added);
        }
        if(node.superName.equals(TRUNK)){node.superName=RUNTIME+"LegacyTrunkPlacer";edits++;}
        else if(node.superName.equals(FOLIAGE)){node.superName=RUNTIME+"LegacyFoliagePlacer";edits++;}
        for(var method:node.methods){
            String alias=callback(node.name,method.name,method.desc,subtype);if(alias!=null){method.name=alias;edits++;}
            boolean declarations=false;
            for(var insn:method.instructions)if(insn instanceof FieldInsnNode f&&f.owner.equals("net/minecraft/data/BuiltinRegistries")&&f.desc.equals("Lnet/minecraft/core/Registry;")&&(f.name.equals("f_123861_")||f.name.equals("f_194653_")))declarations=true;
            for(var insn=method.instructions.getFirst();insn!=null;insn=insn.getNext()){
                if(insn instanceof FieldInsnNode f){
                    if(f.getOpcode()==GETSTATIC&&f.owner.equals("net/minecraft/data/BuiltinRegistries")&&f.desc.equals("Lnet/minecraft/core/Registry;")&&(f.name.equals("f_123861_")||f.name.equals("f_194653_"))){
                        f.owner=DEFINITIONS;f.name=f.name.equals("f_123861_")?"CONFIGURED":"PLACED";f.desc="L"+DEFINITIONS+"$Domain;";edits++;
                    } else if(f.getOpcode()==GETSTATIC&&f.owner.equals("net/minecraft/data/worldgen/placement/VegetationPlacements")&&(f.name.equals("f_195420_")||f.name.equals("TREE_THRESHOLD"))&&f.desc.equals("L"+SOURCE+"placement/PlacementModifier;")){
                        var replacement=new MethodInsnNode(INVOKESTATIC,ADAPTER,"treeThreshold","()"+f.desc,false);method.instructions.set(f,replacement);insn=replacement;edits++;
                    } else if(f.getOpcode()==GETSTATIC&&f.owner.equals("net/minecraft/data/worldgen/features/OreFeatures")&&f.desc.equals("L"+SOURCE+"structure/templatesystem/RuleTest;")){
                        String name=switch(f.name){case "f_195071_"->"naturalStone";case "f_195072_"->"stone";case "f_195073_"->"deepslate";case "f_195074_"->"netherrack";case "f_195075_"->"nether";default->null;};
                        if(name!=null){var replacement=new MethodInsnNode(INVOKESTATIC,ADAPTER,name,"()"+f.desc,false);method.instructions.set(f,replacement);insn=replacement;edits++;}
                    }
                } else if(insn instanceof MethodInsnNode call){
                    String mapped=callback(call.owner,call.name,call.desc,subtype);if(mapped!=null){call.name=mapped;
                        if(call.owner.equals(TRUNK))call.owner=RUNTIME+"LegacyTrunkPlacer";
                        else if(call.owner.equals(FOLIAGE))call.owner=RUNTIME+"LegacyFoliagePlacer";
                        edits++;}
                    if((call.name.equals("m_204201_")||call.name.equals("addFeature"))&&call.desc.equals("(L"+SOURCE+"GenerationStep$Decoration;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/level/biome/BiomeGenerationSettings$Builder;")&&(call.owner.equals("net/minecraftforge/common/world/BiomeGenerationSettingsBuilder")||call.owner.equals("net/minecraft/world/level/biome/BiomeGenerationSettings$Builder"))){
                        call.setOpcode(INVOKESTATIC);call.owner="com/fuckingdeveloper/lms/target/neoforge263/worldgen/Target263BiomeModifierBridge";call.name="addFeature";call.desc="(Lnet/minecraft/world/level/biome/BiomeGenerationSettings$PlainBuilder;L"+SOURCE+"GenerationStep$Decoration;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/level/biome/BiomeGenerationSettings$PlainBuilder;";call.itf=false;edits++;
                    }
                    if(call.getOpcode()==INVOKESPECIAL&&call.name.equals("<init>")){
                        if(call.owner.equals(TRUNK)&&call.desc.equals("(III)V")){call.owner=RUNTIME+"LegacyTrunkPlacer";edits++;}
                        else if(call.owner.equals(FOLIAGE)&&call.desc.equals("(Lnet/minecraft/util/valueproviders/IntProvider;Lnet/minecraft/util/valueproviders/IntProvider;)V")){call.owner=RUNTIME+"LegacyFoliagePlacer";edits++;}
                    }
                    if(declarations&&call.getOpcode()==INVOKESTATIC&&call.owner.equals("net/minecraft/core/Registry")&&(call.name.equals("m_122965_")||call.name.equals("register"))&&call.desc.equals("(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)Ljava/lang/Object;")){
                        call.owner=DEFINITIONS;call.name="register";call.desc="(Ljava/lang/Object;Lnet/minecraft/resources/Identifier;Ljava/lang/Object;)Ljava/lang/Object;";call.itf=false;edits++;
                    }
                    if(declarations&&call.getOpcode()==INVOKESTATIC&&call.owner.equals("net/minecraft/data/BuiltinRegistries")&&call.name.equals("m_206388_")&&call.desc.equals("(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)Lnet/minecraft/core/Holder;")){
                        call.owner=DEFINITIONS;call.name="registerHolder";call.desc="(Ljava/lang/Object;Lnet/minecraft/resources/Identifier;Ljava/lang/Object;)Lnet/minecraft/core/Holder;";call.itf=false;edits++;
                    }
                    if(call.getOpcode()==INVOKESTATIC&&call.owner.equals("net/minecraft/data/worldgen/placement/PlacementUtils")&&call.name.equals("m_206513_")&&call.desc.equals("(Ljava/lang/String;Lnet/minecraft/core/Holder;[L"+SOURCE+"placement/PlacementModifier;)Lnet/minecraft/core/Holder;")){
                        call.owner=DEFINITIONS;call.name="placement";call.itf=false;edits++;
                    }
                    if(call.getOpcode()==INVOKESPECIAL&&call.name.equals("<init>")&&(call.owner.equals(SOURCE+"feature/foliageplacers/FoliagePlacerType")||call.owner.equals(SOURCE+"feature/trunkplacers/TrunkPlacerType"))&&call.desc.equals("(Lcom/mojang/serialization/Codec;)V")){
                        method.instructions.insertBefore(call,new MethodInsnNode(INVOKESTATIC,ADAPTER,"recordCodec","(Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/MapCodec;",false));call.desc="(Lcom/mojang/serialization/MapCodec;)V";edits++;
                    }
                    if(call.getOpcode()==INVOKESTATIC&&call.owner.equals(SOURCE+"feature/stateproviders/BlockStateProvider")&&(call.name.equals("m_191384_")||call.name.equals("m_191382_")||call.name.equals("simple"))&&(call.desc.equals("(Lnet/minecraft/world/level/block/state/BlockState;)L"+SOURCE+"feature/stateproviders/SimpleStateProvider;")||call.desc.equals("(Lnet/minecraft/world/level/block/Block;)L"+SOURCE+"feature/stateproviders/SimpleStateProvider;"))){
                        call.name="of";call.itf=true;edits++;
                    }
                    if(call.getOpcode()==INVOKESTATIC&&call.owner.equals(SOURCE+"blockpredicates/BlockPredicate")&&call.name.equals("m_190399_")&&call.desc.equals("(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Vec3i;)L"+SOURCE+"blockpredicates/BlockPredicate;")){
                        call.owner=ADAPTER;call.name="survival";call.itf=false;edits++;
                    }
                    if(call.owner.equals(SOURCE+"feature/stateproviders/BlockStateProvider")&&call.name.equals("m_213972_")&&call.desc.equals("(Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")){
                        call.setOpcode(INVOKESTATIC);call.owner=ADAPTER;call.name="state";call.desc="(L"+SOURCE+"feature/stateproviders/BlockStateProvider;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;";call.itf=false;edits++;
                    }
                } else if(insn instanceof InvokeDynamicInsnNode indy){indy.bsm=(Handle)callable(indy.bsm,subtype);for(int i=0;i<indy.bsmArgs.length;i++)indy.bsmArgs[i]=callable(indy.bsmArgs[i],subtype);}
                else if(insn instanceof LdcInsnNode ldc)ldc.cst=callable(ldc.cst,subtype);
            }
        }
        return edits;
    }
}
