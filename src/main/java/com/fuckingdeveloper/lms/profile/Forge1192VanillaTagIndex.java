package com.fuckingdeveloper.lms.profile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Source tag identities, not source tag contents or executable classes. */
final class Forge1192VanillaTagIndex {
    private static final Map<String,String> TAGS = load();
    static String location(String owner,String member) { return TAGS.get(owner + "#" + member); }
    private static Map<String,String> load() {
        try(var input=Forge1192VanillaTagIndex.class.getResourceAsStream("/lms/profile/forge1192/vanilla-tags.tsv")) {
            if(input==null) throw new IllegalStateException("Pinned source vanilla tag identities missing");
            var result=new HashMap<String,String>();
            for(String line:new String(input.readAllBytes(),StandardCharsets.UTF_8).split("\n")) {
                if(line.isBlank()||line.startsWith("#"))continue;
                String[] row=line.split("\t");
                if(row.length!=3||result.putIfAbsent(row[0]+"#"+row[1],row[2])!=null) throw new IllegalStateException("Malformed source tag metadata");
            }
            return Map.copyOf(result);
        } catch(IOException failure) { throw new UncheckedIOException(failure); }
    }
}
