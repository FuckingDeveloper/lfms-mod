package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.runtime.render.Forge1192VertexConsumerBridge;
import com.mojang.blaze3d.vertex.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.world.phys.shapes.*;
import org.joml.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class PoseMatrixDrawCheck {
    static void run() throws Exception {
        Path fixture=Path.of("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar");
        if(Files.isRegularFile(fixture)) try(var loader=new ManagedLegacyClassLoader(fixture,PoseMatrixDrawCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            String name="ic2.core.platform.rendering.RenderShapes";
            var type=loader.loadClass(name);
            var node=new ClassNode();new ClassReader(loader.definedClassBytes(name)).accept(node,0);
            check(node.methods.stream().noneMatch(m->m.desc.contains("com/mojang/math/Matrix")),"private matrix/lambda descriptors escaped");
            check(node.methods.stream().flatMap(m->Arrays.stream(m.instructions.toArray())).noneMatch(i->i instanceof MethodInsnNode c && (c.name.equals("m_85861_")||c.name.equals("m_85864_"))),"pose getter siblings escaped");
            var pose=new PoseStack();pose.translate(10,20,30);pose.scale(2,3,4);pose.last().normal().identity().scale(2,3,4);
            var before=new Matrix4f(pose.last().pose());
            var positions=new ArrayList<Vector3f>();var normals=new ArrayList<Vector3f>();int[] colors={0};
            var vertex=(VertexConsumer)Proxy.newProxyInstance(PoseMatrixDrawCheck.class.getClassLoader(),new Class[]{VertexConsumer.class},(p,m,a)->{
                if(m.isDefault())return InvocationHandler.invokeDefault(p,m,a);
                switch(m.getName()) {
                    case "addVertex" -> positions.add(new Vector3f((float)a[0],(float)a[1],(float)a[2]));
                    case "setNormal" -> normals.add(new Vector3f((float)a[0],(float)a[1],(float)a[2]));
                    case "setColor" -> colors[0]++;
                    default -> throw new AssertionError("unexpected source line operation: "+m);
                }
                return p;
            });
            type.getMethod("drawShape",PoseStack.class,VertexConsumer.class,VoxelShape.class,double.class,double.class,double.class,
                    float.class,float.class,float.class,float.class).invoke(null,pose,vertex,Shapes.block(),0D,0D,0D,0.2F,0.4F,0.6F,0.8F);
            check(positions.size()==24 && normals.size()==24 && colors[0]==24,"source cube edge/color/normal count changed");
            var corners=new ArrayList<Vector3f>();
            for(int x=0;x<=1;x++)for(int y=0;y<=1;y++)for(int z=0;z<=1;z++)corners.add(new Vector3f(x,y,z).mulPosition(before));
            for(var position:positions)check(corners.stream().anyMatch(p->p.equals(position,0.00001F)),"position was transformed twice or lost source matrix");
            check(normals.stream().anyMatch(n->java.lang.Math.abs(n.length()-1)>0.01F),"source normal matrix product was renormalized");
            check(pose.last().pose().equals(before),"draw mutated live pose matrix");
            // A foreign matrix consumer prevents the entire closed-flow migration.
            var escaped=new ClassNode();new ClassReader(loader.originalClassBytes(name)).accept(escaped,0);
            var escape=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"escapeMatrix","(Lcom/mojang/math/Matrix4f;)V",null,null);
            escape.instructions.add(new InsnNode(Opcodes.RETURN));escape.maxLocals=1;escaped.methods.add(escape);
            var out=new ClassWriter(0);escaped.accept(out);
            var denied=new ClassNode();new ClassReader(new Forge1192LegacyClassTransformer().transform(name,out.toByteArray()).bytes()).accept(denied,0);
            check(denied.methods.stream().flatMap(m->Arrays.stream(m.instructions.toArray())).anyMatch(i->i instanceof MethodInsnNode c && c.name.equals("m_85861_")),"unproven escaping matrix flow silently admitted");
        }
        check(Forge1192VertexConsumerBridge.sourceLineWidth(960)==2.5F && Forge1192VertexConsumerBridge.sourceLineWidth(3840)==5F,"source default line state not preserved");
        System.out.println("PASS: actual source shape lambda, both pose matrix getters, native positions and unnormalized normals, color/edge counts, live pose preservation, source line-width scaling and escaping-flow rejection");
    }
    private static void check(boolean value,String reason) {if(!value)throw new AssertionError(reason);}
}
