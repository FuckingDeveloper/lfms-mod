package com.fuckingdeveloper.lms.check;

import com.google.gson.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipFile;

/** Metadata-only proof of retained vanilla block-model graphs; no game-class loading. */
public final class GenerateRetainedModelTemplates {
    private static final String PREFIX="assets/minecraft/models/";
    public static void main(String[] args) throws Exception {
        if(args.length!=3)throw new IllegalArgumentException("Expected pinned source assets JAR, configured target JAR, output table");
        String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(Path.of(args[0]))));
        if(!hash.equals("b8068935fc7cd142a43decb6ed9aa9efd2e64517a54258fa27f208d674a3a871"))throw new IllegalArgumentException("Unpinned source model resources");
        var source=read(Path.of(args[0]));var target=read(Path.of(args[1]));var memo=new HashMap<String,Boolean>();var result=new TreeSet<String>();
        for(String name:source.keySet())if(retained(name,source,target,memo,new HashSet<>()))result.add("minecraft:"+name);
        String output="# Pinned Forge1.19.2 vanilla model resources SHA256 "+hash+"\n# Native26.3.0.58-beta retained block-model graphs; only target-only on_shelf display entries are ignored during comparison.\n"+String.join("\n",result)+"\n";
        Files.createDirectories(Path.of(args[2]).getParent());Files.writeString(Path.of(args[2]),output);
        System.out.println("Retained source/native block-model templates="+result.size());
    }
    private static boolean retained(String name,Map<String,JsonObject> source,Map<String,JsonObject> target,Map<String,Boolean> memo,Set<String> path) {
        if(memo.containsKey(name))return memo.get(name);
        if(!path.add(name))return false;
        var a=source.get(name);var b=target.get(name);boolean accepted=false;
        if(a!=null&&b!=null&&!a.has("loader")&&!a.has("overrides")) {
            b=b.deepCopy();
            if(b.has("display")&&(!a.has("display")||!a.getAsJsonObject("display").has("on_shelf"))) {
                var display=b.getAsJsonObject("display");display.remove("on_shelf");
                if(display.isEmpty()&&!a.has("display"))b.remove("display");
            }
            if(a.equals(b)) {
                accepted=!a.has("parent");
                if(!accepted) {String parent=a.get("parent").getAsString();if(parent.startsWith("minecraft:"))parent=parent.substring(10);accepted=retained(parent,source,target,memo,path);}
            }
        }
        path.remove(name);memo.put(name,accepted);return accepted;
    }
    private static Map<String,JsonObject> read(Path artifact) throws Exception {
        var result=new HashMap<String,JsonObject>();try(var zip=new ZipFile(artifact.toFile())) {
            for(var entry:Collections.list(zip.entries()))if(entry.getName().startsWith(PREFIX+"block/")&&entry.getName().endsWith(".json")) {
                try(var reader=new java.io.InputStreamReader(zip.getInputStream(entry),java.nio.charset.StandardCharsets.UTF_8)) {
                    result.put(entry.getName().substring(PREFIX.length(),entry.getName().length()-5),JsonParser.parseReader(reader).getAsJsonObject());
                }
            }
        }return result;
    }
}
