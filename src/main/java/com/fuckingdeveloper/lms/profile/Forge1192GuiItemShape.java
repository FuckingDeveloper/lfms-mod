package com.fuckingdeveloper.lms.profile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.objectweb.asm.tree.*;

/** Full source instruction evidence, never a selector based on a fixture's identity. */
public final class Forge1192GuiItemShape {
    private Forge1192GuiItemShape() {}
    public static String fingerprint(MethodNode method) {
        if(!method.tryCatchBlocks.isEmpty())return "unsupported";
        var positions=new IdentityHashMap<AbstractInsnNode,Integer>();int index=0;
        for(var insn:method.instructions){positions.put(insn,index);if(insn.getOpcode()>=0)index++;}
        var text=new StringBuilder();
        for(var insn:method.instructions){int op=insn.getOpcode();if(op<0)continue;text.append(op);
            if(insn instanceof VarInsnNode n)text.append(':').append(n.var);
            else if(insn instanceof MethodInsnNode n)text.append(':').append(n.owner).append(':').append(n.name).append(':').append(n.desc).append(':').append(n.itf);
            else if(insn instanceof FieldInsnNode n)text.append(':').append(n.owner).append(':').append(n.name).append(':').append(n.desc);
            else if(insn instanceof LdcInsnNode n)text.append(':').append(n.cst.getClass().getName()).append(':').append(n.cst);
            else if(insn instanceof JumpInsnNode n)text.append(':').append(positions.get(n.label));
            else if(insn instanceof IntInsnNode n)text.append(':').append(n.operand);
            else if(!(insn instanceof InsnNode))return "unsupported";
            text.append('\n');
        }
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.toString().getBytes(StandardCharsets.UTF_8)));}
        catch(java.security.NoSuchAlgorithmException impossible){throw new ExceptionInInitializerError(impossible);}
    }
}
