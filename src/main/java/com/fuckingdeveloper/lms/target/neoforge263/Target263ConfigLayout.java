package com.fuckingdeveloper.lms.target.neoforge263;

import java.io.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;

/** Exact current spec-family identities and indexed target ABI, not a forge-to-neoforge prefix guess. */
public final class Target263ConfigLayout {
    public static final String NATIVE="net/neoforged/neoforge/common/ModConfigSpec";
    private static final Map<String,Set<String>> METHODS=new java.util.concurrent.ConcurrentHashMap<>();
    private Target263ConfigLayout(){}
    public static boolean hasMethod(String owner,String name,String descriptor) {
        return METHODS.computeIfAbsent(owner,key->{
            try(var input=Target263ConfigLayout.class.getClassLoader().getResourceAsStream(key+".class")) {
                if(input==null)throw new IllegalStateException("Pinned target config ABI missing: "+key);
                var node=new ClassNode();new ClassReader(input.readAllBytes()).accept(node,ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
                var methods=new HashSet<String>();node.methods.forEach(m->{if((m.access&Opcodes.ACC_PUBLIC)!=0)methods.add(m.name+m.desc);});return Set.copyOf(methods);
            }catch(IOException failure){throw new UncheckedIOException(failure);}
        }).contains(name+descriptor);
    }
}
