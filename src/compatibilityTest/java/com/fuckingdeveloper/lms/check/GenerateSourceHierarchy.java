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
        if(args.length!=4)throw new IllegalArgumentException("Expected Forge JAR, output TSV, FML core JAR and Java FML JAR");
        Path destination=Path.of(args[1]);
        var expected=List.of("579b78e19ff551ae3b340a6f908ca0923633a9395b35e6ae446772a778793bb4",
                "7759350688f802c2e7ed21b31ea21f7c45cfb960d1f495036872bee3a842d702",
                "dace027d2ae25dc0a27b84924e561fc0ecb7914d7e9c2699b10f2caea189def5");
        var inputs=List.of(Path.of(args[0]),Path.of(args[2]),Path.of(args[3]));
        var text=new StringBuilder("# Forge 1.19.2 / 43.4.0 official named patched artifact + FML core + Java FML\n");
        var declarations=new LinkedHashMap<String,ClassNode>();
        for(int i=0;i<inputs.size();i++) {
            var jarPath=inputs.get(i);
            String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(jarPath)));
            if(!digest.equals(expected.get(i)))throw new IllegalArgumentException("Source artifact differs from pinned Forge 1.19.2/43.4.0 input: "+digest);
            text.append("# ").append(jarPath.getFileName()).append(" sha256=").append(digest).append('\n');
            try(var jar=new JarFile(jarPath.toFile())) {
                for(String name:jar.stream().map(entry->entry.getName()).filter(name->(name.startsWith("net/minecraftforge/")||name.startsWith("com/mojang/blaze3d/vertex/"))&&name.endsWith(".class")).sorted().toList()) {
                    var node=new ClassNode();
                    try(var input=jar.getInputStream(jar.getJarEntry(name))){new ClassReader(input.readAllBytes()).accept(node,ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);}
                    if(declarations.putIfAbsent(node.name,node)!=null)throw new IllegalArgumentException("Duplicate source declaration: "+node.name);
                }
            }
        }
        for(var node:declarations.values()) {
            text.append("C\t").append(node.name).append('\t').append(node.access).append('\t').append(node.superName==null?"-":node.superName).append('\t').append(node.interfaces.isEmpty()?"-":String.join(",",node.interfaces)).append('\n');
            for(var field:node.fields)text.append("F\t").append(field.access).append('\t').append(field.name).append('\t').append(field.desc).append('\n');
            for(var method:node.methods)text.append("M\t").append(method.access).append('\t').append(method.name).append('\t').append(method.desc).append('\n');
        }
        Files.createDirectories(destination.getParent()); Files.writeString(destination, text);
        System.out.println("Indexed source Forge declarations: classes=" + declarations.size() + " output=" + destination);
    }
}
