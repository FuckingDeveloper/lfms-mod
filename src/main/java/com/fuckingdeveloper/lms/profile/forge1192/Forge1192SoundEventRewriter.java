package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.function.BiFunction;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Verified source SoundEvent value API; source class identity selects every operation. */
final class Forge1192SoundEventRewriter implements Opcodes {
    private static final String OWNER = "net/minecraft/sounds/SoundEvent";
    private static final String LOCATION = "Lnet/minecraft/resources/ResourceLocation;";
    private static final String BRIDGE = "com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192SoundEventValues";
    private Forge1192SoundEventRewriter() {}

    static int rewrite(ClassNode node, BiFunction<AbstractInsnNode,String,MethodInsnNode> constructor) {
        if (OWNER.equals(node.superName))
            throw new UnsupportedOperationException("Source SoundEvent subclass cannot extend the target final record: " + node.name);
        int count = 0;
        for (var method : node.methods) for (var instruction : method.instructions.toArray()) {
            if (instruction.getPrevious() == null && instruction.getNext() == null
                    && method.instructions.getFirst() != instruction) continue; // Removed paired constructor/allocation.
            if (instruction instanceof TypeInsnNode allocation && allocation.getOpcode() == NEW && allocation.desc.equals(OWNER)) {
                AbstractInsnNode duplicate = allocation.getNext();
                while (duplicate != null && duplicate.getOpcode() < 0) duplicate = duplicate.getNext();
                if (duplicate == null || duplicate.getOpcode() != DUP)
                    throw new IllegalStateException("Source SoundEvent allocation is not paired NEW/DUP: " + node.name);
                var init = constructor.apply(duplicate, OWNER);
                String factory = init == null ? null : factory(init.desc);
                if (factory == null) throw new UnsupportedOperationException("Unadapted source SoundEvent constructor: " + node.name);
                method.instructions.remove(allocation); method.instructions.remove(duplicate);
                method.instructions.set(init, new MethodInsnNode(INVOKESTATIC,OWNER,factory,returnsValue(init.desc),false));
                count++;
            } else if (instruction instanceof MethodInsnNode call && call.owner.equals(OWNER)) {
                String replacement = member(call.name,call.desc);
                if (replacement != null && call.getOpcode() == INVOKEVIRTUAL) {call.name=replacement;count++;}
                else if (call.name.equals("<init>"))
                    throw new UnsupportedOperationException("Unpaired source SoundEvent constructor: " + node.name);
            } else if (instruction instanceof FieldInsnNode field && codec(field.owner,field.name,field.desc) && field.getOpcode()==GETSTATIC) {
                field.owner=BRIDGE;field.name="CODEC";count++;
            } else if (instruction instanceof LdcInsnNode ldc) {
                Object replacement=constant(ldc.cst); if(replacement!=ldc.cst){ldc.cst=replacement;count++;}
            } else if (instruction instanceof InvokeDynamicInsnNode dynamic) {
                Object bootstrap=constant(dynamic.bsm);
                if(bootstrap!=dynamic.bsm){dynamic.bsm=(Handle)bootstrap;count++;}
                for(int i=0;i<dynamic.bsmArgs.length;i++) {
                    Object replacement=constant(dynamic.bsmArgs[i]);
                    if(replacement!=dynamic.bsmArgs[i]){dynamic.bsmArgs[i]=replacement;count++;}
                }
            }
        }
        return count;
    }
    private static String member(String name,String desc) {
        if ((name.equals("getLocation")||name.equals("m_11660_"))&&desc.equals("()"+LOCATION))return "location";
        if ((name.equals("getRange")||name.equals("m_215668_"))&&desc.equals("(F)F"))return "getRange";
        return null;
    }
    private static boolean codec(String owner,String name,String desc) {
        return owner.equals(OWNER)&&(name.equals("CODEC")||name.equals("f_11655_"))&&desc.equals("Lcom/mojang/serialization/Codec;");
    }
    private static String factory(String desc) {
        if(desc.equals("("+LOCATION+")V"))return "createVariableRangeEvent";
        if(desc.equals("("+LOCATION+"F)V"))return "createFixedRangeEvent";
        return null;
    }
    private static String returnsValue(String constructor){return constructor.substring(0,constructor.length()-1)+"L"+OWNER+";";}
    private static Object constant(Object value) {
        if(value instanceof Handle h) {
            if(h.getOwner().equals(OWNER)) {
                String member=member(h.getName(),h.getDesc());
                if(member!=null&&h.getTag()==H_INVOKEVIRTUAL)return new Handle(h.getTag(),OWNER,member,h.getDesc(),false);
                String factory=factory(h.getDesc());
                if(h.getTag()==H_NEWINVOKESPECIAL&&h.getName().equals("<init>")) {
                    if(factory==null)throw new UnsupportedOperationException("Unadapted source SoundEvent constructor handle: "+h);
                    return new Handle(H_INVOKESTATIC,OWNER,factory,returnsValue(h.getDesc()),false);
                }
            }
            if(h.getTag()==H_GETSTATIC&&codec(h.getOwner(),h.getName(),h.getDesc()))
                return new Handle(H_GETSTATIC,BRIDGE,"CODEC",h.getDesc(),false);
        } else if(value instanceof ConstantDynamic dynamic) {
            Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()];
            Handle bootstrap=(Handle)constant(dynamic.getBootstrapMethod());boolean changed=bootstrap!=dynamic.getBootstrapMethod();
            for(int i=0;i<args.length;i++){Object original=dynamic.getBootstrapMethodArgument(i);args[i]=constant(original);changed|=args[i]!=original;}
            if(changed)return new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),bootstrap,args);
        }
        return value;
    }
}
