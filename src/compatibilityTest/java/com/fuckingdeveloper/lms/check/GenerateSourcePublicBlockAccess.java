package com.fuckingdeveloper.lms.check;

import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipFile;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Restore proven source public callback access across every native override, without loading game classes. */
public final class GenerateSourcePublicBlockAccess {
    private static final String ROOT="net/minecraft/world/level/block/state/BlockBehaviour";
    private static final String BEGIN="# BEGIN generated source-public block callbacks",END="# END generated source-public block callbacks";
    public static void main(String[] args) throws Exception {
        if(args.length!=3)throw new IllegalArgumentException("Expected pinned source named JAR, configured native JAR and access-transformer file");
        var source=Path.of(args[0]);
        String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        if(!hash.equals("579b78e19ff551ae3b340a6f908ca0923633a9395b35e6ae446772a778793bb4"))throw new IllegalArgumentException("Unpinned source callback evidence");
        var sourceTypes=read(source);var targetTypes=read(Path.of(args[1]));
        // Select by verified source owner/member identity and an identical native
        // descriptor, never by descriptor alone. Include Block declarations moved
        // into BlockBehaviour (notably isRandomlyTicking).
        var methods=new TreeSet<String>();
        for(String owner:List.of(ROOT,"net/minecraft/world/level/block/Block"))
            for(var method:sourceTypes.get(owner).methods) {
                if((method.access&Opcodes.ACC_PUBLIC)==0||(method.access&Opcodes.ACC_STATIC)!=0||method.name.startsWith("<"))continue;
                for(var target:targetTypes.get(ROOT).methods)
                    if(target.name.equals(method.name)&&target.desc.equals(method.desc)
                            &&(target.access&(Opcodes.ACC_PUBLIC|Opcodes.ACC_PROTECTED))!=0
                            &&(target.access&Opcodes.ACC_STATIC)==0)
                        methods.add(method.name+method.desc);
            }
        // The retained same-identity callbacks are access restoration only.
        // Changed neighbor/remove/inside signatures remain outside this family.
        if(!methods.contains("isRandomlyTicking(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
            throw new IllegalStateException("Pinned source/native block callback evidence changed");
        var lines=new TreeSet<String>();
        for(var node:targetTypes.values())if(subtype(node.name,targetTypes))for(var method:node.methods)
            if(methods.contains(method.name+method.desc)&&(method.access&(Opcodes.ACC_PRIVATE|Opcodes.ACC_STATIC))==0)
                lines.add("public "+node.name.replace('/','.')+" "+method.name+method.desc);
        var output=Path.of(args[2]);String text=Files.readString(output);int start=text.indexOf(BEGIN);
        if(start>=0){int end=text.indexOf(END,start);if(end<0)throw new IllegalStateException("Unterminated generated block access section");text=text.substring(0,start)+text.substring(end+END.length()).stripLeading();}
        Files.writeString(output,text.stripTrailing()+"\n"+BEGIN+"\n# Forge43.4.0 public -> native26.3 protected, exact identities/descriptors.\n# Every native override is widened too so target source recompilation stays valid.\n"+String.join("\n",lines)+"\n"+END+"\n");
        System.out.println("Generated proven source block access: members="+lines.size());
    }
    private static boolean subtype(String name,Map<String,ClassNode> types){var seen=new HashSet<String>();while(name!=null&&seen.add(name)){if(name.equals(ROOT))return true;var node=types.get(name);if(node==null)return false;name=node.superName;}return false;}
    private static Map<String,ClassNode> read(Path path) throws Exception {
        var result=new HashMap<String,ClassNode>();try(var zip=new ZipFile(path.toFile())){for(var entry:Collections.list(zip.entries()))if(entry.getName().endsWith(".class")&&entry.getName().startsWith("net/minecraft/world/level/block/")){
            var node=new ClassNode();try(var in=zip.getInputStream(entry)){new ClassReader(in.readAllBytes()).accept(node,ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);}result.put(node.name,node);
        }}return result;
    }
}
