package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceItemCooldowns;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import net.minecraft.world.item.*;
import static org.objectweb.asm.Opcodes.*;

final class ItemCooldownFamilyCheck {
    static void run() throws Exception {
        String owner = "net/minecraft/world/item/ItemCooldowns", item = "Lnet/minecraft/world/item/Item;";
        var node = new ClassNode(); node.version = V17; node.access = ACC_PUBLIC;
        node.name = "probe/CooldownCalls"; node.superName = "java/lang/Object";
        var names = Map.of("add", "m_41524_", "remove", "m_41527_", "active", "m_41519_", "percent", "m_41521_");
        var descriptors = Map.of("add", "("+item+"I)V", "remove", "("+item+")V", "active", "("+item+")Z", "percent", "("+item+"F)F");
        for (var operation : names.keySet()) {
            String desc = descriptors.get(operation), full = "(L"+owner+";"+desc.substring(1);
            var method = new MethodNode(ACC_PUBLIC|ACC_STATIC, operation, full, null, null);
            method.instructions.add(new VarInsnNode(ALOAD, 0)); method.instructions.add(new VarInsnNode(ALOAD, 1));
            if (operation.equals("add")) method.instructions.add(new VarInsnNode(ILOAD, 2));
            if (operation.equals("percent")) method.instructions.add(new VarInsnNode(FLOAD, 2));
            method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, owner, names.get(operation), desc, false));
            method.instructions.add(new InsnNode(operation.equals("active")?IRETURN:operation.equals("percent")?FRETURN:RETURN)); node.methods.add(method);
        }
        var handle = new MethodNode(ACC_PUBLIC|ACC_STATIC,"handle","()Ljava/lang/invoke/MethodHandle;",null,null);
        handle.instructions.add(new LdcInsnNode(new Handle(H_INVOKEVIRTUAL,owner,names.get("add"),descriptors.get("add"),false)));
        handle.instructions.add(new InsnNode(ARETURN));node.methods.add(handle);
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer);
        Path archive = Files.createTempFile("lms-cooldown-", ".jar");
        try {
            try(var jar = new JarOutputStream(Files.newOutputStream(archive))) {
                jar.putNextEntry(new JarEntry(node.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();
            }
            byte[] original = Files.readAllBytes(archive);
            try(var loader = new ManagedLegacyClassLoader(archive, ItemCooldownFamilyCheck.class.getClassLoader(), new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass("probe.CooldownCalls");
                NativeDefaultComponentFixture.run(() -> {
                    try {
                        var manager=new ItemCooldowns();
                        var add=type.getMethod("add",ItemCooldowns.class,Item.class,int.class);
                        var active=type.getMethod("active",ItemCooldowns.class,Item.class);
                        var percent=type.getMethod("percent",ItemCooldowns.class,Item.class,float.class);
                        add.invoke(null,manager,Items.STICK,4);
                        check((boolean)active.invoke(null,manager,Items.STICK),"source add/query lost");
                        check(!(boolean)active.invoke(null,manager,Items.APPLE),"source item keys merged");
                        manager.tick();
                        check(Math.abs((float)percent.invoke(null,manager,Items.STICK,0.5f)-0.625f)<0.0001f,"fractional cooldown timing changed");
                        var callable=(java.lang.invoke.MethodHandle)type.getMethod("handle").invoke(null);
                        try { callable.invoke(manager,Items.STICK,2); } catch(Throwable failure) { throw new AssertionError(failure); }
                        manager.tick(); check((boolean)active.invoke(null,manager,Items.STICK),"replacement expired early");
                        manager.tick(); check(!(boolean)active.invoke(null,manager,Items.STICK),"native expiry lost");
                        add.invoke(null,manager,Items.STICK,10);
                        type.getMethod("remove",ItemCooldowns.class,Item.class).invoke(null,manager,Items.STICK);
                        check(!(boolean)active.invoke(null,manager,Items.STICK),"remove lost");
                        add.invoke(null,manager,Items.STICK,0); manager.tick();
                        check(!(boolean)active.invoke(null,manager,Items.STICK),"zero expiry changed");
                        try { Target263SourceItemCooldowns.addCooldown(new ItemCooldowns(){},Items.STICK,4); throw new AssertionError("custom manager admitted"); }
                        catch(UnsupportedOperationException expected) {}
                    } catch(ReflectiveOperationException failure) { throw new AssertionError(failure); }
                });
            }
            check(Arrays.equals(original,Files.readAllBytes(archive)),"source cooldown artifact changed");
        } finally { Files.deleteIfExists(archive); }
        System.out.println("PASS: source cooldown add/remove/query/percent, callable handles, native fractional timing/replacement/expiry, distinct keys, custom-manager rejection and immutable input");
    }
    private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
}
