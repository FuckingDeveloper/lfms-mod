package com.fuckingdeveloper.lms.target.neoforge263;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Target ABI metadata only; inspection does not initialize registries or load source classes. */
public final class Target263BuiltinBlockEntityConstants {
    public static final String OWNER="net/minecraft/world/level/block/entity/BlockEntityTypes";
    private Target263BuiltinBlockEntityConstants() {}
    public static Map<String,Handle> inspect(byte[] bytes) {
        if(bytes==null)throw new IllegalStateException("Native block-entity constant catalog ABI is unavailable");
        var node=new ClassNode();new ClassReader(bytes).accept(node,ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        if(!node.name.equals(OWNER))throw new IllegalArgumentException("Wrong native block-entity catalog: "+node.name);
        String descriptor="Lnet/minecraft/world/level/block/entity/BlockEntityType;";
        var fields=new HashMap<String,Handle>();
        for(var field:node.fields)if(field.desc.equals(descriptor) && (field.access&(ACC_PUBLIC|ACC_STATIC|ACC_FINAL))==(ACC_PUBLIC|ACC_STATIC|ACC_FINAL)) {
            if(fields.putIfAbsent(field.name,new Handle(H_GETSTATIC,OWNER,field.name,descriptor,false))!=null)
                throw new IllegalStateException("Ambiguous native constant: "+field.name);
        }
        return Map.copyOf(fields);
    }
}
