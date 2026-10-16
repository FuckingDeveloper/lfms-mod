package com.fuckingdeveloper.lms.profile.forge1192;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/** Indexed entity implementation identity derived from matching native registry constructor factories. */
public final class Forge1192EntityRelocations {
    public record Entry(String id,String source,String target){}
    private static final List<Entry> ENTRIES;
    private static final Map<String,String> FORWARD,REVERSE;
    private static final Pattern TYPES=Pattern.compile("L([^;<]+)(?=[;<])");
    static {
        var entries=new ArrayList<Entry>();var forward=new HashMap<String,String>();var reverse=new HashMap<String,String>();
        try(var stream=Objects.requireNonNull(Forge1192EntityRelocations.class.getResourceAsStream("/lms/source/forge1192/entity-implementations-target263.tsv"));var reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))) {
            for(String line;(line=reader.readLine())!=null;) {
                if(line.isBlank()||line.startsWith("#"))continue;
                var fields=line.split("\t");if(fields.length!=3)throw new IllegalStateException("Malformed source entity implementation evidence");
                var entry=new Entry(fields[0],fields[1],fields[2]);entries.add(entry);
                String target=forward.putIfAbsent(entry.source(),entry.target());String source=reverse.putIfAbsent(entry.target(),entry.source());
                if(target!=null&&!target.equals(entry.target())||source!=null&&!source.equals(entry.source()))throw new IllegalStateException("Ambiguous entity implementation evidence: "+entry);
            }
        }catch(IOException failure){throw new ExceptionInInitializerError(failure);}
        ENTRIES=List.copyOf(entries);FORWARD=Map.copyOf(forward);REVERSE=Map.copyOf(reverse);
    }
    private Forge1192EntityRelocations(){}
    public static List<Entry> entries(){return ENTRIES;}
    public static String targetOwner(String source){return FORWARD.get(source);}
    public static String sourceOwner(String target){return REVERSE.get(target);}
    public static String projectDescriptor(String descriptor){return project(descriptor,FORWARD);}
    public static String reverseDescriptor(String descriptor){return project(descriptor,REVERSE);}
    private static String project(String descriptor,Map<String,String> names) {
        var matcher=TYPES.matcher(descriptor);var result=new StringBuffer();
        while(matcher.find())matcher.appendReplacement(result,java.util.regex.Matcher.quoteReplacement("L"+names.getOrDefault(matcher.group(1),matcher.group(1))));
        matcher.appendTail(result);return result.toString();
    }
}
