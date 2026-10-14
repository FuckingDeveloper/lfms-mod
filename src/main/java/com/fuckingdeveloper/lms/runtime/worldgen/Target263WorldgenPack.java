package com.fuckingdeveloper.lms.runtime.worldgen;
import java.io.*;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.*;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/** Native pack lifecycle consumes an immutable snapshot of staged worldgen definitions. */
public final class Target263WorldgenPack {
    private Target263WorldgenPack() {}
    public static void addPackFinders(AddPackFindersEvent event){
        if(event.getPackType()!=PackType.SERVER_DATA)return;
        var location=new PackLocationInfo("lms/legacy-worldgen",Component.literal("LMS legacy world generation"),PackSource.BUILT_IN,Optional.empty());
        var metadata=new Pack.Metadata(Component.literal("Legacy generation intent using native placement"),PackCompatibility.COMPATIBLE,FeatureFlagSet.of(),List.of());
        var supplier=new Pack.ResourcesSupplier(){
            public PackMetadataResources openMetadata(PackLocationInfo ignored){return new Resources(location,Map.of());}
            public java.util.stream.Stream<PackResources> openResources(PackLocationInfo ignored,Pack.Metadata info){
                var resources=LegacyWorldgenDefinitions.resources();
                org.slf4j.LoggerFactory.getLogger(Target263WorldgenPack.class).info("LMS native worldgen pack definitions={}",resources.size());
                return java.util.stream.Stream.of(new Resources(location,resources));
            }
        };
        event.addRepositorySource(out->out.accept(new Pack(location,supplier,metadata,new PackSelectionConfig(true,Pack.Position.BOTTOM,false))));
    }
    /** Per-artifact loader-owned directory migration; native entries in that same artifact win. */
    public static Pack aliases(java.nio.file.Path artifact){
        Map<Identifier,byte[]> aliases=new LinkedHashMap<>();
        try(var zip=new java.util.zip.ZipFile(artifact.toFile())){
            for(var entry:java.util.Collections.list(zip.entries())){
                var parts=entry.getName().split("/",3);
                if(parts.length!=3||!parts[0].equals("data")||!parts[2].startsWith("forge/biome_modifier/")||!parts[2].endsWith(".json"))continue;
                String target="neoforge/biome_modifier/"+parts[2].substring("forge/biome_modifier/".length());
                if(zip.getEntry("data/"+parts[1]+"/"+target)!=null)continue;
                try(var input=zip.getInputStream(entry)){aliases.put(Identifier.fromNamespaceAndPath(parts[1],target),input.readAllBytes());}
            }
        }catch(IOException failure){throw new java.io.UncheckedIOException("Cannot project source biome modifier resources: "+artifact,failure);}
        if(aliases.isEmpty())return null;
        var location=new PackLocationInfo("lms/legacy-biome-modifiers/"+Integer.toUnsignedString(artifact.toAbsolutePath().normalize().toString().hashCode(),16),Component.literal("LMS source biome modifier resources"),PackSource.BUILT_IN,Optional.empty());
        var metadata=new Pack.Metadata(Component.literal("Source Forge biome definitions in native loader directory"),PackCompatibility.COMPATIBLE,FeatureFlagSet.of(),List.of());
        var snapshot=Map.copyOf(aliases);
        var supplier=new Pack.ResourcesSupplier(){public PackMetadataResources openMetadata(PackLocationInfo ignored){return new Resources(location,Map.of());}public java.util.stream.Stream<PackResources> openResources(PackLocationInfo ignored,Pack.Metadata metadata){return java.util.stream.Stream.of(new Resources(location,snapshot));}};
        return new Pack(location,supplier,metadata,new PackSelectionConfig(true,Pack.Position.TOP,false));
    }
    private static final class Resources extends AbstractPackMetadataResources implements PackResources {
        private final Map<Identifier,byte[]> resources;
        Resources(PackLocationInfo location,Map<Identifier,byte[]> resources){super(location);this.resources=resources;}
        public IoSupplier<InputStream> getRootResource(String... path){return null;}
        public IoSupplier<InputStream> getResource(PackType type,Identifier id){var bytes=type==PackType.SERVER_DATA?resources.get(id):null;return bytes==null?null:()->new ByteArrayInputStream(bytes);}
        public void listResources(PackType type,String namespace,String path,ResourceOutput output){if(type!=PackType.SERVER_DATA)return;resources.forEach((id,bytes)->{if(id.getNamespace().equals(namespace)&&(path.isEmpty()||id.getPath().startsWith(path+"/")))output.accept(id,()->new ByteArrayInputStream(bytes));});}
        public Set<String> getNamespaces(PackType type){return type!=PackType.SERVER_DATA?Set.of():resources.keySet().stream().map(Identifier::getNamespace).collect(java.util.stream.Collectors.toUnmodifiableSet());}
        public void close(){}
    }
}
