package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import org.objectweb.asm.*;
import java.lang.invoke.MethodHandle;

final class CombatRuleFamilyCheck implements Opcodes {
    static void run() throws Throwable {
        String owner="net/minecraft/world/damagesource/CombatRules", name="fixture/SourceCombatRules";
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,name,null,"java/lang/Object",null);
        for(String member:new String[]{"m_19272_","m_19269_"}) {
            int count=member.equals("m_19272_")?3:2;
            String descriptor=count==3?"(FFF)F":"(FF)F";
            var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,member,descriptor,null,null);m.visitCode();
            for(int i=0;i<count;i++)m.visitVarInsn(FLOAD,i);
            m.visitMethodInsn(INVOKESTATIC,owner,member,descriptor,false);m.visitInsn(FRETURN);m.visitMaxs(0,0);m.visitEnd();
            m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,member+"Handle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();
            m.visitLdcInsn(new Handle(H_INVOKESTATIC,owner,member,descriptor,false));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        }
        w.visitEnd();
        var bytes=new Forge1192LegacyClassTransformer().transform(name.replace('/','.'),w.toByteArray()).bytes();
        var type=new AttributeOperationFamilyCheck.Loader().define(name.replace('/','.'),bytes);
        var physical=type.getMethod("m_19272_",float.class,float.class,float.class);
        close((float)physical.invoke(null,4f,8f,0f),3.04f);
        close((float)physical.invoke(null,100f,20f,0f),84f); // lower armor floor under high damage
        close((float)physical.invoke(null,4f,100f,0f),0.8f); // upper effective armor cap
        close((float)((MethodHandle)type.getMethod("m_19272_Handle").invoke(null)).invokeExact(4f,10f,0f),2.72f);
        var magic=type.getMethod("m_19269_",float.class,float.class);
        close((float)magic.invoke(null,10f,-1f),10f);
        close((float)magic.invoke(null,10f,25f),2f);
        close((float)((MethodHandle)type.getMethod("m_19269_Handle").invoke(null)).invokeExact(10f,5f),8f);
        System.out.println("PASS: loaded source CombatRules physical/magic calls and handles, lower/upper protection bounds; no fabricated modern damage context");
    }
    private static void close(float value,float expected) {if(Math.abs(value-expected)>0.00001f)throw new AssertionError(value+" != "+expected);}
}
