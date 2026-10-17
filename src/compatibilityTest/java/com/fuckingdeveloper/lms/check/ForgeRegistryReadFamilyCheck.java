package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.*;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.level.block.Blocks;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

public final class ForgeRegistryReadFamilyCheck {
    private static final String HOST="fixture/ForgeRegistryReader", R="net/minecraftforge/registries/ForgeRegistry", M="net/minecraftforge/registries/RegistryManager", I="net/minecraftforge/registries/IForgeRegistry";
    private static final String ID="Lnet/minecraft/resources/ResourceLocation;", KEY="Lnet/minecraft/resources/ResourceKey;", OBJ="Ljava/lang/Object;", RD="L"+R+";";
    private static final String[][] READS={
        {"getRegistryName","()"+ID},{"getRegistryKey","()"+KEY},{"getDefaultKey","()"+ID},
        {"getValue","("+ID+")"+OBJ},{"getValue","(I)"+OBJ},{"getKey","("+OBJ+")"+ID},
        {"getKey","(I)"+KEY},{"getResourceKey","("+OBJ+")Ljava/util/Optional;"},
        {"getID","("+OBJ+")I"},{"getID","("+ID+")I"},{"containsKey","("+ID+")Z"},
        {"containsValue","("+OBJ+")Z"},{"isEmpty","()Z"},{"iterator","()Ljava/util/Iterator;"},
        {"spliterator","()Ljava/util/Spliterator;"},{"getKeys","()Ljava/util/Set;"},
        {"getValues","()Ljava/util/Collection;"},{"getEntries","()Ljava/util/Set;"},
        {"getRaw","("+ID+")"+OBJ},{"tags","()Lnet/minecraftforge/registries/tags/ITagManager;"}};
    public static Object constant(MethodHandles.Lookup lookup,String name,Class<?> type,Object value){return value;}
    @SuppressWarnings({"unchecked","rawtypes"})
    public static void run() throws Exception {
        var file=Files.createTempFile("forge-registry-reads-", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(file))){jar.putNextEntry(new JarEntry(HOST+".class"));jar.write(fixture());jar.closeEntry();}
            byte[] original=Files.readAllBytes(file);
            try(var loader=new ManagedLegacyClassLoader(file,ForgeRegistryReadFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass(HOST.replace('/','.'));
                var id=Identifier.parse("minecraft:block");var stone=Identifier.parse("minecraft:stone");var absent=Identifier.parse("fixture:absent");
                Object view=type.getMethod("open",Identifier.class).invoke(null,id);
                check(view instanceof LegacyForgeRegistry&&view==type.getField("stored").get(null),"stored concrete/interface identity");
                check(type.getField("stored").getGenericType().getTypeName().contains(LegacyForgeRegistryView.class.getName()),"generic type projection");
                check(type.getMethod("openKey",ResourceKey.class).invoke(null,Registries.BLOCK)==view,"manager overload/view identity");
                try {type.getMethod("openKey",ResourceKey.class).invoke(null,new Object[]{null});throw new AssertionError("null key returned fabricated absence");}
                catch(java.lang.reflect.InvocationTargetException expected){check(expected.getCause() instanceof NullPointerException,"source null-key behavior changed");}
                try {LegacyRegistryManagerView.ACTIVE.getRegistry(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_TYPES);throw new AssertionError("projected unsupported source registry silently absent");}
                catch(UnsupportedOperationException expected){}
                check(type.getMethod("managerName").invoke(null).equals("ACTIVE"),"manager name");
                check(type.getMethod("reverseName",LegacyForgeRegistry.class).invoke(null,view).equals(id),"manager reverse identity");
                check(type.getMethod("open",Identifier.class).invoke(null,absent)==null,"absent registry became a facade");
                check(type.getMethod("open",Identifier.class).invoke(null,Identifier.parse("minecraft:creative_mode_tab"))==null,"target-only registry leaked into source manager");
                type.getMethod("open",Identifier.class).invoke(null,id);
                check(type.getMethod("cast",Object.class).invoke(null,BuiltInRegistries.BLOCK)==view,"native static producer cast split source identity");
                check(type.getMethod("cast",Object.class).invoke(null,new Object[]{null})==null,"null cast changed");
                check(type.getMethod("isRegistry",Object.class).invoke(null,BuiltInRegistries.BLOCK).equals(true)&&type.getMethod("isRegistry",Object.class).invoke(null,new Object()).equals(false),"source instanceof representations");
                try{type.getMethod("cast",Object.class).invoke(null,new Object());throw new AssertionError("invalid cast admitted");}catch(java.lang.reflect.InvocationTargetException expectedCast){check(expectedCast.getCause() instanceof ClassCastException,"source cast failure changed");}
                check(type.getMethod("read3",Identifier.class).invoke(null,stone)==Blocks.STONE,"key value identity");
                check(type.getMethod("read3",Identifier.class).invoke(null,absent)==Blocks.AIR,"source default fallback");
                int stoneId=BuiltInRegistries.BLOCK.getId(Blocks.STONE),airId=BuiltInRegistries.BLOCK.getId(Blocks.AIR);
                check(type.getMethod("read4",int.class).invoke(null,stoneId)==Blocks.STONE&&type.getMethod("read4",int.class).invoke(null,-1)==Blocks.AIR,"numeric lookup/default");
                check(type.getMethod("read5",Object.class).invoke(null,new Object()).equals(Identifier.parse("minecraft:air")),"missing reverse default");
                check(type.getMethod("read6",int.class).invoke(null,stoneId).equals(ResourceKey.create(Registries.BLOCK,stone)),"typed numeric key");
                check(type.getMethod("read7",Object.class).invoke(null,new Object()).equals(Optional.empty()),"optional reverse absence");
                check(type.getMethod("read8",Object.class).invoke(null,new Object()).equals(airId),"missing value ID default");
                check(type.getMethod("read9",Identifier.class).invoke(null,stone).equals(stoneId)&&type.getMethod("read9",Identifier.class).invoke(null,absent).equals(airId),"direct name IDs");
                check(type.getMethod("read10",Identifier.class).invoke(null,stone).equals(true)&&type.getMethod("read10",Identifier.class).invoke(null,absent).equals(false),"membership absence");
                check(type.getMethod("read11",Object.class).invoke(null,Blocks.STONE).equals(true)&&type.getMethod("read11",Object.class).invoke(null,new Object()).equals(false),"value membership");
                check(type.getMethod("read12").invoke(null).equals(false),"empty registry");
                check(type.getMethod("read0").invoke(null).equals(id)&&type.getMethod("read1").invoke(null).equals(Registries.BLOCK)&&type.getMethod("read2").invoke(null).equals(Identifier.parse("minecraft:air")),"registry keys/default identity");
                var iter=(Iterator<?>)type.getMethod("read13").invoke(null);int last=-1,count=0;while(iter.hasNext()){var value=iter.next();int next=BuiltInRegistries.BLOCK.getId((net.minecraft.world.level.block.Block)value);check(next>last,"iteration numeric order");last=next;count++;}check(iter.next()==null&&count==BuiltInRegistries.BLOCK.size(),"source exhaustion/size");
                check(((Spliterator<?>)type.getMethod("read14").invoke(null)).getExactSizeIfKnown()==-1,"Iterable spliterator contract changed");
                for(int n:new int[]{15,16,17}) {
                    Collection collection=(Collection)type.getMethod("read"+n).invoke(null);check(collection.size()==count,"collection size");
                    try{collection.clear();throw new AssertionError("mutable source collection");}catch(UnsupportedOperationException expected){}
                    if(n==17){var entry=(Map.Entry)collection.iterator().next();try{entry.setValue(new Object());throw new AssertionError("mutable entry");}catch(UnsupportedOperationException expected){}}
                }
                for(String name:List.of("handle","nestedHandle")) {
                    var h=(MethodHandle)type.getMethod(name).invoke(null);
                    try{check(h.invokeWithArguments(view,stone)==Blocks.STONE,"concrete handle/constant projection");}catch(Throwable failure){throw new AssertionError(failure);}
                }
                var fn=(java.util.function.Function<Object,Identifier>)type.getMethod("lambda").invoke(null);check(fn.apply(Blocks.STONE).equals(stone),"bound source method reference");
                var keys=(java.util.function.Supplier<Set<Identifier>>)type.getMethod("boundKeys").invoke(null);
                check(keys.get().contains(stone),"bound interface handle capture type lost");
                check(type.getMethod("interfaceKey",LegacyForgeRegistry.class,Object.class).invoke(null,view,Blocks.STONE).equals(Optional.of(ResourceKey.create(Registries.BLOCK,stone))),"interface consumer interoperability");
                // Source plain registry has no default, even for unknown numeric values.
                type.getMethod("open",Identifier.class).invoke(null,Identifier.parse("minecraft:sound_event"));
                check(type.getMethod("read3",Identifier.class).invoke(null,absent)==null&&type.getMethod("read4",int.class).invoke(null,-1)==null,"ordinary registry invented default");
                check(type.getMethod("read8",Object.class).invoke(null,new Object()).equals(-1)&&type.getMethod("read5",Object.class).invoke(null,new Object())==null,"ordinary reverse absence");
                try{LegacyRegistryManagerView.ACTIVE.getRegistry(Identifier.parse("minecraft:enchantment")).getValues().size();throw new AssertionError("inactive data-driven global fabricated");}catch(IllegalStateException expected){}
                type.getMethod("open",Identifier.class).invoke(null,id);
                check(type.getMethod("read18",Identifier.class).invoke(null,stone)==Blocks.STONE&&type.getMethod("read18",Identifier.class).invoke(null,absent)==null,"raw name lookup invented fallback");
                check(type.getMethod("read19").invoke(null)==Forge1192RegistryTagBridge.tags(BuiltInRegistries.BLOCK),"concrete tag manager identity");
                var blockView=LegacyRegistryManagerView.ACTIVE.getRegistry(Registries.BLOCK);
                check(Forge1192RegistrationContext.getValue(blockView,stone)==Blocks.STONE,"existing interface bridge rejected view");
                check(Forge1192RegistryTagBridge.tags(blockView)==Forge1192RegistryTagBridge.tags(BuiltInRegistries.BLOCK),"tag-manager identity split");
                var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                try{com.fuckingdeveloper.lms.profile.forge1192.runtime.network.Forge1192NetworkBridge.writeRegistryIdUnsafe(buffer,blockView,Blocks.STONE);check(buffer.getByte(0)==stoneId||stoneId>127,"raw ID encoding");check(com.fuckingdeveloper.lms.profile.forge1192.runtime.network.Forge1192NetworkBridge.readRegistryIdUnsafe(buffer,blockView)==Blocks.STONE,"raw ID view roundtrip");}finally{buffer.release();}
                check(Arrays.equals(original,Files.readAllBytes(file)),"legacy JAR mutated");
            }
            for(int shape=0;shape<9;shape++)reject(shape);
        }finally{Files.deleteIfExists(file);}
        System.out.println("PASS: source ACTIVE/concrete/interface/stored/generic identity, key/name/ID/default/absence reads, numeric iteration and source exhaustion, immutable collection entries, handles/nested constants/lambda, existing read/tag/wire interoperability, mutation/snapshot/subclass negatives and immutable JAR");
    }
    private static byte[] fixture() {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);w.visit(V17,ACC_PUBLIC,HOST,null,"java/lang/Object",null);
        w.visitField(ACC_PUBLIC|ACC_STATIC,"stored",RD,"L"+R+"<Ljava/lang/Object;>;",null).visitEnd();
        var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"open","("+ID+")"+RD,null,null);m.visitCode();m.visitFieldInsn(GETSTATIC,M,"ACTIVE","L"+M+";");m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKEVIRTUAL,M,"getRegistry","("+ID+")"+RD,false);m.visitInsn(DUP);m.visitFieldInsn(PUTSTATIC,HOST,"stored",RD);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"openKey","("+KEY+")"+RD,null,null);m.visitCode();m.visitFieldInsn(GETSTATIC,M,"ACTIVE","L"+M+";");m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKEVIRTUAL,M,"getRegistry","("+KEY+")"+RD,false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"managerName","()Ljava/lang/String;",null,null);m.visitCode();m.visitFieldInsn(GETSTATIC,M,"ACTIVE","L"+M+";");m.visitMethodInsn(INVOKEVIRTUAL,M,"getName","()Ljava/lang/String;",false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"reverseName","(L"+I+";)"+ID,null,null);m.visitCode();m.visitFieldInsn(GETSTATIC,M,"ACTIVE","L"+M+";");m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKEVIRTUAL,M,"getName","(L"+I+";)"+ID,false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        for(int i=0;i<READS.length;i++) {
            var op=READS[i];m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"read"+i,op[1],null,null);m.visitCode();m.visitFieldInsn(GETSTATIC,HOST,"stored",RD);int slot=0;for(Type arg:Type.getArgumentTypes(op[1])){m.visitVarInsn(arg.getOpcode(ILOAD),slot);slot+=arg.getSize();}m.visitMethodInsn(INVOKEVIRTUAL,R,op[0],op[1],false);m.visitInsn(Type.getReturnType(op[1]).getOpcode(IRETURN));m.visitMaxs(0,0);m.visitEnd();
        }
        for (boolean cast:List.of(true,false)) {
            m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,cast?"cast":"isRegistry","(Ljava/lang/Object;)"+(cast?RD:"Z"),null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitTypeInsn(cast?CHECKCAST:INSTANCEOF,R);m.visitInsn(cast?ARETURN:IRETURN);m.visitMaxs(0,0);m.visitEnd();
        }
        var h=new Handle(H_INVOKEVIRTUAL,R,"getValue","("+ID+")"+OBJ,false);
        var bsm=new Handle(H_INVOKESTATIC,"com/fuckingdeveloper/lms/check/ForgeRegistryReadFamilyCheck","constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false);
        for(boolean nested:List.of(false,true)){m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,nested?"nestedHandle":"handle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();m.visitLdcInsn(nested?new ConstantDynamic("read","Ljava/lang/invoke/MethodHandle;",bsm,h):h);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();}
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"lambda","()Ljava/util/function/Function;",null,null);m.visitCode();m.visitFieldInsn(GETSTATIC,HOST,"stored",RD);m.visitInvokeDynamicInsn("apply","("+RD+")Ljava/util/function/Function;",new Handle(H_INVOKESTATIC,"java/lang/invoke/LambdaMetafactory","metafactory","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",false),Type.getMethodType("("+OBJ+")"+OBJ),new Handle(H_INVOKEVIRTUAL,R,"getKey","("+OBJ+")"+ID,false),Type.getMethodType("("+OBJ+")"+ID));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"boundKeys","()Ljava/util/function/Supplier;",null,null);m.visitCode();m.visitFieldInsn(GETSTATIC,HOST,"stored",RD);
        m.visitInvokeDynamicInsn("get","(L"+I+";)Ljava/util/function/Supplier;",new Handle(H_INVOKESTATIC,"java/lang/invoke/LambdaMetafactory","metafactory","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",false),Type.getMethodType("()Ljava/lang/Object;"),new Handle(H_INVOKEINTERFACE,I,"getKeys","()Ljava/util/Set;",true),Type.getMethodType("()Ljava/util/Set;"));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"interfaceKey","(L"+I+";"+OBJ+")Ljava/util/Optional;",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKEINTERFACE,I,"getResourceKey","("+OBJ+")Ljava/util/Optional;",true);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();return w.toByteArray();
    }
    private static void reject(int shape) throws Exception {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);String host="fixture/UnsupportedRegistry";w.visit(V17,ACC_PUBLIC,host,null,shape==6?R:"java/lang/Object",null);
        var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"bad","()V",null,null);m.visitCode();
        if(shape<=1){m.visitFieldInsn(GETSTATIC,M,shape==0?"VANILLA":"FROZEN","L"+M+";");m.visitInsn(POP);}
        else if(shape==2){m.visitInsn(ACONST_NULL);m.visitMethodInsn(INVOKEVIRTUAL,R,"unfreeze","()V",false);}
        else if(shape==3){m.visitTypeInsn(NEW,M);m.visitInsn(POP);}
        else if(shape==4||shape==5){var h=new Handle(H_INVOKEVIRTUAL,R,"clear","()V",false);m.visitLdcInsn(shape==4?h:new ConstantDynamic("mutation","Ljava/lang/invoke/MethodHandle;",new Handle(H_INVOKESTATIC,"fixture/Bootstrap","get","()Ljava/lang/Object;",false),h));m.visitInsn(POP);}
        else if(shape==7){m.visitInsn(ACONST_NULL);m.visitFieldInsn(PUTSTATIC,M,"ACTIVE","L"+M+";");}
        else if(shape==8){m.visitInsn(ACONST_NULL);m.visitMethodInsn(INVOKEVIRTUAL,R,"unknown","()V",false);}
        m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();
        try{new Forge1192LegacyClassTransformer().transform(host.replace('/','.'),w.toByteArray());throw new AssertionError("unsupported registry shape admitted "+shape);}catch(UnsupportedOperationException expected){check(expected.getMessage().contains("Source Forge registry read boundary"),"wrong fail-fast boundary");}
    }
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
}
