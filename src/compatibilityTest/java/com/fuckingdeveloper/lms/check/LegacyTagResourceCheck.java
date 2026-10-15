package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.Target263LegacyTagResources;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.network.chat.Component;

final class LegacyTagResourceCheck {
    static void run() throws Exception {
        var data = new LinkedHashMap<Identifier,String>();
        for (String folder : List.of("blocks","items","fluids","entity_types","game_events")) {
            data.put(id("tags/"+folder+"/nested/probe.json"),"source:"+folder);
        }
        data.put(id("tags/block/native.json"),"native");
        data.put(id("tags/blocks/native.json"),"must-not-win");
        data.put(id("tags/blocks_extra/probe.json"),"unrelated");
        int[] closes={0}, lists={0};
        PackResources source = new PackResources() {
            public PackLocationInfo location(){return new PackLocationInfo("probe",Component.literal("probe"),PackSource.BUILT_IN,Optional.empty());}
            public IoSupplier<InputStream> getRootResource(String... p){return null;}
            public <T> T getMetadataSection(MetadataSectionType<T> t){return null;}
            public Set<String> getNamespaces(PackType t){return Set.of("probe");}
            public void close(){closes[0]++;}
            public IoSupplier<InputStream> getResource(PackType t,Identifier i){
                if(!data.containsKey(i))throw new AssertionError("unlisted procedural lookup "+i);
                return () -> new ByteArrayInputStream(data.get(i).getBytes(StandardCharsets.UTF_8));
            }
            public void listResources(PackType t,String n,String dir,ResourceOutput out){
                lists[0]++;
                data.forEach((i,v)->{if(i.getNamespace().equals(n)&&i.getPath().startsWith(dir))out.accept(i,getResource(t,i));});
            }
        };
        var pack=new Target263LegacyTagResources(source);
        for(String[] pair:new String[][]{{"blocks","block"},{"items","item"},{"fluids","fluid"},{"entity_types","entity_type"},{"game_events","game_event"}}){
            var found=new LinkedHashMap<Identifier,IoSupplier<InputStream>>();
            pack.listResources(PackType.SERVER_DATA,"probe","tags/"+pair[1],found::put);
            var key=id("tags/"+pair[1]+"/nested/probe.json");
            check(read(found.get(key)).equals("source:"+pair[0]),"listed source bytes");
            check(read(pack.getResource(PackType.SERVER_DATA,key)).equals("source:"+pair[0]),"direct source bytes");
            check(!found.containsKey(id("tags/blocks_extra/probe.json")),"directory boundary");
        }
        check(read(pack.getResource(PackType.SERVER_DATA,id("tags/block/native.json"))).equals("native"),"native precedence");
        check(pack.getResource(PackType.SERVER_DATA,id("tags/block/absent.json"))==null,"missing resource");
        var ancestor=new LinkedHashMap<Identifier,IoSupplier<InputStream>>();
        pack.listResources(PackType.SERVER_DATA,"probe","tags",ancestor::put);
        check(ancestor.containsKey(id("tags/block/nested/probe.json")),"ancestor listing");
        var client=new LinkedHashMap<Identifier,IoSupplier<InputStream>>();
        pack.listResources(PackType.CLIENT_RESOURCES,"probe","tags/item",client::put);
        check(!client.containsKey(id("tags/item/nested/probe.json")),"client resources projected");
        int before=lists[0]; pack.listResources(PackType.SERVER_DATA,"probe","recipes",(i,s)->{});
        check(lists[0]==before+1,"non-tag source execution duplicated");
        data.put(id("tags/blocks/nested/probe.json"),"reload");
        check(read(pack.getResource(PackType.SERVER_DATA,id("tags/block/nested/probe.json"))).equals("reload"),"source reload data cached");
        pack.close();check(closes[0]==1,"source ownership");
        proceduralNamespace();
    }
    private static void proceduralNamespace() throws Exception {
        var key=Identifier.fromNamespaceAndPath("forge","tags/items/dusts/probe.json");
        var queries=new ArrayList<String>();
        var source=new com.fuckingdeveloper.lms.runtime.Forge1192LegacyPackResourcesAccess() {
            public InputStream lms$legacyGetRootResource(String p) { return null; }
            public InputStream lms$legacyGetResource(PackType t,Identifier i) throws IOException {
                if(!i.equals(key)) throw new FileNotFoundException(i.toString());
                return new ByteArrayInputStream("{\"values\":[\"minecraft:coal\"]}".getBytes(StandardCharsets.UTF_8));
            }
            public Collection<Identifier> lms$legacyListResources(PackType t,String n,String d,java.util.function.Predicate<Identifier> p) {
                queries.add(n+":"+d);
                return n.equals("forge")&&key.getPath().startsWith(d)&&p.test(key)?List.of(key):List.of();
            }
            public Set<String> lms$legacyNamespaces(PackType t) { return Set.of("neoforge","probe"); }
            public String lms$legacyPackName() { return "procedural-probe"; }
            public void close() {}
        };
        var projected=com.fuckingdeveloper.lms.runtime.Forge1192PackRepositoryBridge.create(
                "probe",Component.literal("probe"),true,()->source,
                com.fuckingdeveloper.lms.runtime.Forge1192PackRepositoryBridge.metadata(Component.literal("probe"),9),
                net.minecraft.server.packs.repository.Pack.Position.TOP,PackSource.BUILT_IN);
        var resources=projected.open().toList();
        check(!resources.getFirst().getNamespaces(PackType.CLIENT_RESOURCES).contains("forge"),"client namespace changed");
        try(var manager=new net.minecraft.server.packs.resources.MultiPackResourceManager(PackType.SERVER_DATA,resources)) {
            var found=manager.listResources("tags/item",i->true);
            var nativeKey=Identifier.fromNamespaceAndPath("forge","tags/item/dusts/probe.json");
            check(found.containsKey(nativeKey),"native manager never discovered intrinsic source data");
            try(var input=found.get(nativeKey).open()) {
                check(new String(input.readAllBytes(),StandardCharsets.UTF_8).contains("minecraft:coal"),"source resource bytes lost");
            }
            check(queries.contains("forge:tags/items"),"source namespace was not queried");
            check(manager.getResource(Identifier.fromNamespaceAndPath("forge","tags/item/absent.json")).isEmpty(),"source data fabricated");
        }
    }
    private static Identifier id(String p){return Identifier.fromNamespaceAndPath("probe",p);}
    private static String read(IoSupplier<InputStream> s)throws Exception{check(s!=null,"missing supplier");try(var in=s.get()){return new String(in.readAllBytes(),StandardCharsets.UTF_8);}}
    private static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
}
