package com.fuckingdeveloper.lms.target.neoforge263;

import com.google.gson.JsonObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.*;
import net.minecraft.world.flag.FeatureFlagSet;

/** Add a source-intent sprite source to each actual target atlas, including registered custom atlases. */
public final class Target263LegacyAtlasSources {
    private Target263LegacyAtlasSources(){}
    public static Map<Identifier,byte[]> definitions(Collection<Identifier> atlases) {return definitions(atlases,Set.of());}
    public static Map<Identifier,byte[]> definitions(Collection<Identifier> atlases,Collection<Identifier> modelTextures) {
        var result=new LinkedHashMap<Identifier,byte[]>();
        for(var atlas:atlases) {
            var source=new JsonObject();source.addProperty("type","lms:legacy_texture_stitch");source.addProperty("atlas",atlas.toString());
            var document=new JsonObject();var sources=new com.google.gson.JsonArray();sources.add(source);document.add("sources",sources);
            if(atlas.equals(net.minecraft.data.AtlasIds.BLOCKS))modelTextures.stream().sorted(Comparator.comparing(Identifier::toString)).forEach(texture->{
                var single=new JsonObject();single.addProperty("type","minecraft:single");single.addProperty("resource",texture.toString());sources.add(single);
            });
            result.put(Identifier.fromNamespaceAndPath(atlas.getNamespace(),"atlases/"+atlas.getPath()+".json"),document.toString().getBytes(StandardCharsets.UTF_8));
        }
        return Collections.unmodifiableMap(result);
    }
    /** Standard source model dependencies backed by real immutable artifact PNGs. */
    public static Set<Identifier> modelTextures(Collection<java.nio.file.Path> artifacts) {
        var result=new HashSet<Identifier>();
        for(var artifact:artifacts)try(var archive=new java.util.zip.ZipFile(artifact.toFile())) {
            for(var entry:Collections.list(archive.entries())) {
                String[] parts=entry.getName().split("/",3);
                if(parts.length!=3||!parts[0].equals("assets")||!parts[2].startsWith("models/")||!parts[2].endsWith(".json"))continue;
                try(var reader=new InputStreamReader(archive.getInputStream(entry),StandardCharsets.UTF_8)) {
                    var model=net.minecraft.util.GsonHelper.parse(reader);
                    if(!model.has("textures"))continue;
                    for(var value:model.getAsJsonObject("textures").entrySet()) {
                        if(!value.getValue().isJsonPrimitive()||!value.getValue().getAsJsonPrimitive().isString())
                            throw new IllegalStateException("Unproven source model texture slot: "+entry.getName()+"#"+value.getKey());
                        String text=value.getValue().getAsString();if(text.startsWith("#"))continue;
                        var texture=Identifier.parse(text);
                        if(archive.getEntry("assets/"+texture.getNamespace()+"/textures/"+texture.getPath()+".png")!=null)result.add(texture);
                    }
                }catch(IOException|RuntimeException failure){throw new IllegalStateException("Cannot inspect source model textures: "+artifact+" / "+entry.getName(),failure);}
            }
        }catch(IOException failure){throw new UncheckedIOException("Cannot index source model textures: "+artifact,failure);}
        return Set.copyOf(result);
    }
    public static Pack createPack() {return createPack(List.of());}
    public static Pack createPack(Collection<java.nio.file.Path> artifacts) {
        var textures=modelTextures(artifacts);
        var location=new PackLocationInfo("lms/legacy-atlas-bridge",Component.literal("LMS legacy atlas bridge"),PackSource.BUILT_IN,Optional.empty());
        var metadata=new Pack.Metadata(Component.literal("Source texture-stitch intent for native atlases"),PackCompatibility.COMPATIBLE,FeatureFlagSet.of(),List.of());
        Pack.ResourcesSupplier supplier=new Pack.ResourcesSupplier(){
            public PackMetadataResources openMetadata(PackLocationInfo location){return new Resources(location,Map.of());}
            public java.util.stream.Stream<PackResources> openResources(PackLocationInfo location,Pack.Metadata ignored){
                var atlasIds=new ArrayList<Identifier>();
                Minecraft.getInstance().getAtlasManager().forEach((id,atlas)->atlasIds.add(id));
                return java.util.stream.Stream.of(new Resources(location,definitions(atlasIds,textures)));
            }
        };
        return new Pack(location,supplier,metadata,new PackSelectionConfig(true,Pack.Position.TOP,false));
    }
    private static final class Resources extends AbstractPackMetadataResources implements PackResources {
        private final Map<Identifier,byte[]> resources;
        Resources(PackLocationInfo location,Map<Identifier,byte[]> resources){super(location);this.resources=resources;}
        public net.minecraft.server.packs.resources.IoSupplier<InputStream> getRootResource(String... path){return null;}
        public net.minecraft.server.packs.resources.IoSupplier<InputStream> getResource(PackType type,Identifier id){
            byte[] bytes=type==PackType.CLIENT_RESOURCES?resources.get(id):null;return bytes==null?null:()->new ByteArrayInputStream(bytes);
        }
        public void listResources(PackType type,String namespace,String directory,ResourceOutput output){
            if(type==PackType.CLIENT_RESOURCES)resources.forEach((id,bytes)->{if(id.getNamespace().equals(namespace)&&id.getPath().startsWith(directory))output.accept(id,()->new ByteArrayInputStream(bytes));});
        }
        public Set<String> getNamespaces(PackType type){return type==PackType.CLIENT_RESOURCES?resources.keySet().stream().map(Identifier::getNamespace).collect(java.util.stream.Collectors.toUnmodifiableSet()):Set.of();}
        public void close(){}
    }
}
