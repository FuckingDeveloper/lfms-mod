package com.fuckingdeveloper.lms.check;

import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.jar.JarFile;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Regenerate source loader metadata from the immutable pinned Forge development artifact. */
public final class GenerateSourceHierarchy {
    public static void main(String[] args) throws Exception {
        Path jarPath = Path.of(args[0]), destination = Path.of(args[1]);
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(jarPath)));
        if (!digest.equals("579b78e19ff551ae3b340a6f908ca0923633a9395b35e6ae446772a778793bb4"))
            throw new IllegalArgumentException("Source artifact differs from the inspected pinned Forge 1.19.2/43.4.0 input: " + digest);
        var text = new StringBuilder("# Forge 1.19.2 / 43.4.0 official named patched artifact\n# sha256=" + digest + "\n");
        int count = 0;
        try (var jar = new JarFile(jarPath.toFile())) {
            for (String name : jar.stream().map(entry -> entry.getName()).filter(name -> (name.startsWith("net/minecraftforge/") || name.startsWith("com/mojang/blaze3d/vertex/")) && name.endsWith(".class")).sorted().toList()) {
                var node = new ClassNode();
                try (var input = jar.getInputStream(jar.getJarEntry(name))) { new ClassReader(input.readAllBytes()).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES); }
                text.append("C\t").append(node.name).append('\t').append(node.access).append('\t').append(node.superName == null ? "-" : node.superName).append('\t').append(node.interfaces.isEmpty() ? "-" : String.join(",", node.interfaces)).append('\n');
                for (var field : node.fields) text.append("F\t").append(field.access).append('\t').append(field.name).append('\t').append(field.desc).append('\n');
                count++;
            }
        }
        Files.createDirectories(destination.getParent()); Files.writeString(destination, text);
        System.out.println("Indexed source Forge declarations: classes=" + count + " output=" + destination);
    }
}
