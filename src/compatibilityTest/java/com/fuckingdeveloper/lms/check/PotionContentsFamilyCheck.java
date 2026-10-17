package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.*;
import com.fuckingdeveloper.lms.target.neoforge263.Target263PotionContents;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;
import net.minecraft.core.Holder;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

public final class PotionContentsFamilyCheck {
    private static final String P="net/minecraft/world/item/alchemy/Potion",PD="L"+P+";",STACK="Lnet/minecraft/world/item/ItemStack;",HOST="fixture/Potions";
    public static Object constant(MethodHandles.Lookup lookup,String name,Class<?> type,Object value){return value;}
    public static void run() throws Exception {
        String mappings=Files.readString(Path.of("legacy-mappings/1.19.2/client.txt"));
        String section=mappings.substring(mappings.indexOf("net.minecraft.world.item.alchemy.Potions ->"));section=section.substring(0,section.indexOf("\nnet.minecraft.",1));
        var names=new ArrayList<String>();var matcher=Pattern.compile("    net.minecraft.world.item.alchemy.Potion ([A-Z_]+) ->").matcher(section);while(matcher.find())names.add(matcher.group(1));
        check(names.size()>30,"source constant evidence incomplete");
        var path=Files.createTempFile("lms-potion-family-", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry(HOST+".class"));jar.write(fixture(names));jar.closeEntry();}
            byte[] original=Files.readAllBytes(path);
            try(var loader=new ManagedLegacyClassLoader(path,PotionContentsFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass("fixture.Potions");
                for(String name:names) {
                    Potion expected=name.equals("EMPTY")?Forge1192PotionBridge.EMPTY:(Potion)((Holder<?>)Potions.class.getField(name).get(null)).value();
                    check(type.getMethod("field"+name).invoke(null)==expected,"constant raw identity "+name);
                }
                check(type.getMethod("water").invoke(null)==Potions.WATER.value(),"source SRG WATER identity");
                for(String method:List.of("handle","nested")) {
                    var handle=(MethodHandle)type.getMethod(method).invoke(null);
                    try{check(handle.invokeWithArguments()==Potions.WATER.value(),"callable constant identity");}catch(Throwable failure){throw new AssertionError(failure);}
                }
                var stack=stack();
                check(type.getMethod("set",ItemStack.class,Potion.class).invoke(null,stack,Potions.WATER.value())==stack,"source set receiver identity");
                check(type.getMethod("get",ItemStack.class).invoke(null,stack)==Potions.WATER.value(),"source typed contents read");
                check(type.getMethod("name",Potion.class,String.class).invoke(null,Potions.LONG_SWIFTNESS.value(),"item.effect.").equals("item.effect.swiftness"),"source name suffix");
                stack.setCount(0);
                check(Forge1192PotionBridge.getPotion(stack)==Potions.WATER.value(),"source empty-count potion data disappeared");
                stack.setCount(1);
                var effect=new MobEffectInstance(MobEffects.SPEED,200,1);var initial=new PotionContents(Optional.of(Potions.WATER),Optional.of(0x123456),List.of(effect),Optional.of("custom"));
                stack.set(DataComponents.POTION_CONTENTS,initial);
                check(Forge1192PotionBridge.setPotion(stack,Potions.AWKWARD.value())==stack,"typed setter receiver");
                var changed=stack.get(DataComponents.POTION_CONTENTS);
                check(changed.potion().orElseThrow()==Potions.AWKWARD&&changed.customColor().equals(initial.customColor())&&changed.customName().equals(initial.customName())&&changed.customEffects().size()==1,"setPotion dropped custom fields");
                var effects=Forge1192PotionBridge.getCustomEffects(stack);check(effects!=changed.customEffects()&&effects.getFirst()!=effect,"custom effects not copied");effects.clear();check(Forge1192PotionBridge.getCustomEffects(stack).size()==1,"returned list mutated contents");
                check(Forge1192PotionBridge.setCustomEffects(stack,List.of())==stack&&stack.get(DataComponents.POTION_CONTENTS)==changed,"empty collection changed source contents");
                Forge1192PotionBridge.setCustomEffects(stack,List.of(new MobEffectInstance(MobEffects.STRENGTH,300)));
                check(Forge1192PotionBridge.getCustomEffects(stack).size()==1&&Forge1192PotionBridge.getCustomEffects(stack).getFirst().getEffect().is(MobEffects.STRENGTH),"source nonempty effects replacement");
                Forge1192PotionBridge.setPotion(stack,Forge1192PotionBridge.EMPTY);
                check(Forge1192PotionBridge.getPotion(stack)==Forge1192PotionBridge.EMPTY&&stack.get(DataComponents.POTION_CONTENTS).potion().isEmpty(),"source absence encoded as fake holder");
                check(!BuiltInRegistries.POTION.containsValue(Forge1192PotionBridge.EMPTY),"source absence leaked into native registry");
                check(Forge1192PotionBridge.getColor(stack)==0x123456,"custom source color precedence");
                check(Forge1192PotionBridge.getColor(Forge1192PotionBridge.EMPTY)==16253176&&Forge1192PotionBridge.getColor(List.of())==3694022,"source color defaults");
                Holder<MobEffect> red=Holder.direct(new ColorEffect(0xff0000));Holder<MobEffect> blue=Holder.direct(new ColorEffect(0x0000ff));
                check(Forge1192PotionBridge.getColor(List.of(new MobEffectInstance(red,100),new MobEffectInstance(blue,100,1)))==0x5500aa,"source amplifier-weighted RGB arithmetic");
                check(Forge1192PotionBridge.getColor(List.of(new MobEffectInstance(red,100,0,false,false,false)))==0,"invisible effects contributed color");
                check(Forge1192PotionBridge.byName("fixture:missing")==Forge1192PotionBridge.EMPTY&&Forge1192PotionBridge.byName("invalid identifier")==Forge1192PotionBridge.EMPTY,"source default absence");
                var raw=stack();var rawTag=new net.minecraft.nbt.CompoundTag();rawTag.putString("Potion","minecraft:water");raw.set(DataComponents.CUSTOM_DATA,new net.minecraft.world.item.component.CustomData(rawTag));
                try{Forge1192PotionBridge.getPotion(raw);throw new AssertionError("raw source potion tag silently ignored");}catch(UnsupportedOperationException expected){}
                try{Target263PotionContents.holder(Forge1192PotionBridge.EMPTY);throw new AssertionError("source absence acquired a holder");}catch(UnsupportedOperationException expected){}
                try{Forge1192ItemBridge.getTag(stack);throw new AssertionError("raw potion NBT silently omitted components");}catch(UnsupportedOperationException expected){}
            }
            check(Arrays.equals(original,Files.readAllBytes(path)),"source input mutated");
        } finally {Files.deleteIfExists(path);}
        System.out.println("PASS: all pinned potion constants/direct/callable identity, typed contents/custom data/effect copies, source EMPTY absence, name suffix, source color arithmetic and raw NBT boundary");
    }
    private static ItemStack stack(){return new ItemStack(Items.POTION.builtInRegistryHolder(),1,new PatchedDataComponentMap(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,1).build()));}
    private static final class ColorEffect extends MobEffect { ColorEffect(int color){super(MobEffectCategory.BENEFICIAL,color);} }
    private static byte[] fixture(List<String> names){
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,HOST,null,"java/lang/Object",null);
        for(String name:names){var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"field"+name,"()"+PD,null,null);m.visitCode();m.visitFieldInsn(GETSTATIC,P+"s",name,PD);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();}
        var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"water","()"+PD,null,null);m.visitCode();m.visitFieldInsn(GETSTATIC,P+"s","f_43599_",PD);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        var getter=new Handle(H_GETSTATIC,P+"s","f_43599_",PD,false);
        for(String name:List.of("handle","nested")){m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,name,"()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();Object value=getter;if(name.equals("nested"))value=new ConstantDynamic("potionGetter","Ljava/lang/invoke/MethodHandle;",new Handle(H_INVOKESTATIC,"com/fuckingdeveloper/lms/check/PotionContentsFamilyCheck","constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false),getter);m.visitLdcInsn(value);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();}
        emit(w,"set","("+STACK+PD+")"+STACK,P+"Utils","m_43549_","("+STACK+PD+")"+STACK,INVOKESTATIC,2);
        emit(w,"get","("+STACK+")"+PD,P+"Utils","m_43579_","("+STACK+")"+PD,INVOKESTATIC,1);
        emit(w,"name","("+PD+"Ljava/lang/String;)Ljava/lang/String;",P,"m_43492_","(Ljava/lang/String;)Ljava/lang/String;",INVOKEVIRTUAL,2);
        w.visitEnd();return w.toByteArray();
    }
    private static void emit(ClassWriter w,String name,String desc,String owner,String target,String targetDesc,int opcode,int locals){var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,name,desc,null,null);m.visitCode();for(int i=0;i<locals;i++)m.visitVarInsn(ALOAD,i);m.visitMethodInsn(opcode,owner,target,targetDesc,false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
