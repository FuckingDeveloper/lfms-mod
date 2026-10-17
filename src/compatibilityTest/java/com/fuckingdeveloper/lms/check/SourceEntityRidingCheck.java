package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceRiding;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

/** Immutable source-shaped binary exercises real native attachment and callback dispatch. */
public final class SourceEntityRidingCheck {
    public static class MountProbe extends PrimedTnt {
        public Entity actualVehicle; public boolean actualForce, actualEvents;
        public MountProbe() { super(null, null); }
        @Override public boolean startRiding(Entity vehicle, boolean force, boolean events) {
            actualVehicle=vehicle; actualForce=force; actualEvents=events; return !force;
        }
    }
    static void run() throws Exception {
        var field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);var unsafe=(sun.misc.Unsafe)field.get(null);
        var path=Files.createTempFile("lms-source-riding", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))) {
                jar.putNextEntry(new JarEntry("probe/Riding.class"));jar.write(fixture());jar.closeEntry();
            }
            byte[] original=Files.readAllBytes(path);
            try(var loader=new ManagedLegacyClassLoader(path,SourceEntityRidingCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var source=loader.linkOwnedClass("probe.Riding");var vehicle=(Entity)unsafe.allocateInstance(source);
                var rider=(MountProbe)unsafe.allocateInstance(MountProbe.class);
                check(source.getMethod("mount",Entity.class,Entity.class,boolean.class).invoke(null,rider,vehicle,true).equals(false),"source mount result");
                check(rider.actualVehicle==vehicle&&rider.actualForce&&rider.actualEvents,"force/event operands");
                var handle=(java.lang.invoke.MethodHandle)source.getMethod("mountHandle").invoke(null);
                try { check((boolean)handle.invokeExact((Entity)rider,vehicle),"source one-argument handle result"); }
                catch(Throwable failure){throw new AssertionError(failure);}
                check(!rider.actualForce&&rider.actualEvents,"one-argument default force/events");
                rider.setId(1); vehicle.setId(2);
                var dimensions=EntityDimensions.fixed(1F,2F);
                set(vehicle,"dimensions",dimensions);set(vehicle,"passengers",com.google.common.collect.ImmutableList.of(rider));
                set(rider,"dimensions",dimensions);set(vehicle,"position",new Vec3(10,20,30));
                Vec3 attachment=(Vec3)source.getMethod("getPassengerAttachmentPoint",Entity.class,EntityDimensions.class,float.class).invoke(vehicle,rider,dimensions,1F);
                check(attachment.y==-0.2D,"source absolute passenger offset lost");
                check(Target263SourceRiding.passengerOffset(vehicle)==-0.2D,"virtual scalar source offset");
                Vec3[] moved={null};
                source.getMethod("positionRider",Entity.class,Entity.MoveFunction.class).invoke(vehicle,rider,(Entity.MoveFunction)(e,x,y,z)->moved[0]=new Vec3(x,y,z));
                Vec3 expected=vehicle.getPassengerRidingPosition(rider).subtract(rider.getVehicleAttachmentPoint(vehicle));
                check(expected.equals(moved[0]),"source position-super did not retain native movement callback/attachments");
                check(source.getField("calls").getInt(vehicle)==1,"source position callback bypassed");
                check(Arrays.equals(original,Files.readAllBytes(path)),"source binary mutated");
            }
        }finally{Files.deleteIfExists(path);}
        check(Target263SourceRiding.riderAttachment(new Vec3(2,3,4),0.25).equals(new Vec3(2,-0.25,4)),"rider offset sign/XZ");
        System.out.println("PASS: immutable source mount calls/handles, force/events/result, native passenger attachment Y and native movement callback through source position-super; actual mount lifecycle requires client");
    }
    private static void set(Entity entity,String name,Object value)throws Exception{var f=Entity.class.getDeclaredField(name);f.setAccessible(true);f.set(entity,value);}
    private static byte[] fixture(){
        String root="net/minecraft/world/entity/Entity",name="probe/Riding",one="(L"+root+";)Z",two="(L"+root+";Z)Z";
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,name,null,root,null);
        w.visitField(ACC_PUBLIC,"calls","I",null,null).visitEnd();
        var m=w.visitMethod(ACC_PUBLIC,"m_6048_","()D",null,null);m.visitCode();m.visitLdcInsn(-0.2D);m.visitInsn(DRETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC,"m_7332_","(L"+root+";)V",null,null);m.visitCode();
        m.visitVarInsn(ALOAD,0);m.visitInsn(DUP);m.visitFieldInsn(GETFIELD,name,"calls","I");m.visitInsn(ICONST_1);m.visitInsn(IADD);m.visitFieldInsn(PUTFIELD,name,"calls","I");
        m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKESPECIAL,root,"m_7332_","(L"+root+";)V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"mount","(L"+root+";L"+root+";Z)Z",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitVarInsn(ILOAD,2);m.visitMethodInsn(INVOKEVIRTUAL,root,"m_7998_",two,false);m.visitInsn(IRETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"mountHandle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();m.visitLdcInsn(new Handle(H_INVOKEVIRTUAL,root,"m_20329_",one,false));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        w.visitEnd();return w.toByteArray();
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
