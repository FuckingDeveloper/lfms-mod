package com.fuckingdeveloper.lms.check;
import org.objectweb.asm.*;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

final class AttributeOperationFamilyCheck implements Opcodes {
    static final String MOD="net/minecraft/world/entity/ai/attributes/AttributeModifier";
    static final String OP=MOD+"$Operation";
    static class Loader extends ClassLoader {
        Loader(){super(AttributeOperationFamilyCheck.class.getClassLoader());}
        Class<?> define(String name,byte[] bytes){return defineClass(name,bytes,0,bytes.length);}
    }
    static void run() throws Exception {
        var old=new String[]{"ADDITION","MULTIPLY_BASE","MULTIPLY_TOTAL"};
        var current=AttributeModifier.Operation.values();
        for(int index=0;index<old.length;index++) {
            var name="fixture/AttributeArgs"+index;var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);
            writer.visit(V17,ACC_PUBLIC,name,null,"java/lang/Object",null);
            var m=writer.visitMethod(ACC_PUBLIC|ACC_STATIC,"create","()L"+MOD+";",null,null);m.visitCode();
            m.visitTypeInsn(NEW,MOD);m.visitInsn(DUP);
            m.visitTypeInsn(NEW,"java/util/UUID");m.visitInsn(DUP);m.visitInsn(LCONST_0);m.visitInsn(LCONST_1);
            m.visitMethodInsn(INVOKESPECIAL,"java/util/UUID","<init>","(JJ)V",false);
            m.visitLdcInsn("source");m.visitInsn(DCONST_1);m.visitFieldInsn(GETSTATIC,OP,old[index],"L"+OP+";");
            m.visitMethodInsn(INVOKESPECIAL,MOD,"<init>","(Ljava/util/UUID;Ljava/lang/String;DL"+OP+";)V",false);
            m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
            var handle=new Handle(H_GETSTATIC,OP,old[index],"L"+OP+";",false);
            m=writer.visitMethod(ACC_PUBLIC|ACC_STATIC,"handle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();m.visitLdcInsn(handle);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
            var bootstrap=new Handle(H_INVOKESTATIC,"java/lang/invoke/ConstantBootstraps","invoke","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/invoke/MethodHandle;[Ljava/lang/Object;)Ljava/lang/Object;",false);
            m=writer.visitMethod(ACC_PUBLIC|ACC_STATIC,"dynamic","()L"+OP+";",null,null);m.visitCode();
            m.visitLdcInsn(new ConstantDynamic("operation","L"+OP+";",bootstrap,handle));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();writer.visitEnd();
            var transformed=new Forge1192LegacyClassTransformer().transform(name.replace('/','.'),writer.toByteArray());
            var type=new Loader().define(name.replace('/','.'),transformed.bytes());
            var modifier=(AttributeModifier)type.getMethod("create").invoke(null);
            if(modifier.operation()!=current[index])throw new AssertionError("constructor argument operation missed");
            try {
                var getter=(java.lang.invoke.MethodHandle)type.getMethod("handle").invoke(null);
                if(getter.invoke()!=current[index])throw new AssertionError("field handle missed");
            } catch(Throwable failure){throw new AssertionError("operation handle failed",failure);}
            if(type.getMethod("dynamic").invoke(null)!=current[index])throw new AssertionError("dynamic operation missed");
        }
        System.out.println("PASS: three source operations inside rewritten constructors with nested UUID allocation, actual field handles and dynamic constants");
    }
}
