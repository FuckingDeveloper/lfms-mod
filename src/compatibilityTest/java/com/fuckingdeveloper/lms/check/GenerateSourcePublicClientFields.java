package com.fuckingdeveloper.lms.check;

import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipFile;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Pinned source-public immutable Minecraft service fields with unchanged native identities. */
public final class GenerateSourcePublicClientFields {
    public static void main(String[] args) throws Exception {
        if(args.length!=3)throw new IllegalArgumentException("Expected pinned source, native JAR, access transformer");
        if(!HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(Path.of(args[0])))).equals("579b78e19ff551ae3b340a6f908ca0923633a9395b35e6ae446772a778793bb4"))throw new IllegalArgumentException("Unpinned source fields");
        String owner="net/minecraft/client/Minecraft";
        var source=read(args[0],owner);var target=read(args[1],owner);var lines=new TreeSet<String>();
        int flags=Opcodes.ACC_PUBLIC|Opcodes.ACC_FINAL;
        for(var field:source.fields)if((field.access&(flags|Opcodes.ACC_STATIC))==flags)
            for(var nativeField:target.fields)if(nativeField.name.equals(field.name)&&nativeField.desc.equals(field.desc)
                    &&(nativeField.access&(Opcodes.ACC_STATIC|Opcodes.ACC_FINAL|Opcodes.ACC_PUBLIC))==Opcodes.ACC_FINAL)
                lines.add("public "+owner.replace('/','.')+" "+field.name);
        if(!lines.contains("public net.minecraft.client.Minecraft textureManager"))throw new IllegalStateException("Expected retained source texture service");
        String begin="# BEGIN generated source-public immutable client fields",end="# END generated source-public immutable client fields";
        var path=Path.of(args[2]);String text=Files.readString(path);int index=text.indexOf(begin);
        if(index>=0){int last=text.indexOf(end,index);if(last<0)throw new IllegalStateException("Unterminated client field evidence");text=text.substring(0,index)+text.substring(last+end.length()).stripLeading();}
        Files.writeString(path,text.stripTrailing()+"\n"+begin+"\n# Pinned Forge43.4.0 source public final -> exact native final identity/descriptor.\n# Visibility only; final and construction lifetime are unchanged.\n"+String.join("\n",lines)+"\n"+end+"\n");
        System.out.println("Generated retained immutable client fields="+lines.size());
    }
    private static ClassNode read(String file,String owner)throws Exception{try(var zip=new ZipFile(file);var input=zip.getInputStream(zip.getEntry(owner+".class"))){var node=new ClassNode();new ClassReader(input.readAllBytes()).accept(node,ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);return node;}}
}
