package com.fuckingdeveloper.lms.profile.forge1192;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Pinned source-generation declaration/registry identity evidence, independent of a target JVM ABI. */
public final class Forge1192BuiltinBlockEntityCatalog {
    private static final Map<String,String> IDENTIFIERS=load();
    private Forge1192BuiltinBlockEntityCatalog() {}
    public static Map<String,String> identifiers(){return IDENTIFIERS;}
    private static Map<String,String> load() {
        String path="/lms/profile/forge1192/block-entity-constants.tsv";
        var stream=Forge1192BuiltinBlockEntityCatalog.class.getResourceAsStream(path);
        if(stream==null)throw new IllegalStateException("Missing pinned source catalog: "+path);
        try(var reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))) {
            var result=new LinkedHashMap<String,String>();String line;
            while((line=reader.readLine())!=null) {
                if(line.isBlank()||line.startsWith("#"))continue;
                String[] cells=line.split("\t",-1);
                if(cells.length!=2 || !cells[0].matches("[A-Z_]+") || !cells[1].matches("[a-z0-9_.-]+:[a-z0-9/._-]+"))
                    throw new IllegalStateException("Invalid pinned block-entity identity row: "+line);
                if(result.putIfAbsent(cells[0],cells[1])!=null)throw new IllegalStateException("Duplicate source constant: "+cells[0]);
            }
            if(result.size()!=36)throw new IllegalStateException("Incomplete pinned source block-entity catalog: "+result.size());
            return Collections.unmodifiableMap(result);
        }catch(IOException failure){throw new UncheckedIOException("Cannot read pinned source block-entity catalog",failure);}
    }
}
