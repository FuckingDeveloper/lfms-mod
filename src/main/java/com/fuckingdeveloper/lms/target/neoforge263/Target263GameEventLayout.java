package com.fuckingdeveloper.lms.target.neoforge263;

import java.io.*;
import java.util.*;
import org.objectweb.asm.*;

/** Native field metadata only: never initializes a registry while transforming a class. */
public final class Target263GameEventLayout {
    private Target263GameEventLayout() {}
    private static final Map<String,String> FIELDS=read();
    public static Map<String,String> fields(){return FIELDS;}
    private static Map<String,String> read() {
        String root="net/minecraft/world/level/gameevent/GameEvent";var fields=new HashMap<String,String>();
        try(var in=Target263GameEventLayout.class.getClassLoader().getResourceAsStream(root+".class")) {
            if(in==null)throw new IllegalStateException("Missing native GameEvent metadata");
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9){
                @Override public FieldVisitor visitField(int access,String name,String desc,String signature,Object value) {
                    if((access&(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC|Opcodes.ACC_FINAL))==(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC|Opcodes.ACC_FINAL)
                            && desc.equals("Lnet/minecraft/core/Holder$Reference;")&&("Lnet/minecraft/core/Holder$Reference<L"+root+";>;").equals(signature))fields.put(name,desc);
                    return null;
                }
            },ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
            if(fields.isEmpty())throw new IllegalStateException("Native typed GameEvent constants not found");
            return Map.copyOf(fields);
        }catch(IOException failure){throw new UncheckedIOException(failure);}
    }
}
