package com.fuckingdeveloper.lms.target.neoforge263;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;

/** Source-owned atlas, using only public native preparation/upload and the real reload lifecycle. */
public abstract class Target263SourceTextureAtlasHolder extends SimplePreparableReloadListener<SpriteLoader.Preparations> implements AutoCloseable {
    private final TextureAtlas atlas;
    private final String prefix;
    protected Target263SourceTextureAtlasHolder(TextureManager manager, Identifier location, String prefix) {
        this.prefix=Objects.requireNonNull(prefix);atlas=new TextureAtlas(location);
        manager.register(location,atlas);
    }
    protected abstract Stream<Identifier> getResourcesToLoad();
    protected TextureAtlasSprite getSprite(Identifier id) { return atlas.getSprite(resolve(id)); }
    private Identifier resolve(Identifier id){return Identifier.fromNamespaceAndPath(id.getNamespace(),prefix+"/"+id.getPath());}
    @Override protected SpriteLoader.Preparations prepare(ResourceManager manager, ProfilerFiller profiler) {
        List<Identifier> resources;
        try(var source=getResourcesToLoad()){resources=source.map(this::resolve).distinct().toList();}
        Identifier definition=Identifier.fromNamespaceAndPath("lms","source_owned_atlas");
        var overlay=definition(manager,definition,resources);
        return SpriteLoader.create(atlas).loadAndStitch(overlay,definition,0,Runnable::run,Set.of()).join();
    }
    @Override protected void apply(SpriteLoader.Preparations preparations, ResourceManager manager, ProfilerFiller profiler) {
        preparations.readyForUpload().join();atlas.upload(preparations);
    }
    @Override public void close(){atlas.clearTextureData();}
    /** Reload-local definition only. Image and metadata resolution retain the actual resource stack. */
    public static ResourceManager definition(ResourceManager manager,Identifier atlasId,List<Identifier> sprites) {
        var entries=new JsonArray();
        for(var sprite:sprites){var entry=new JsonObject();entry.addProperty("type","minecraft:single");entry.addProperty("resource",sprite.toString());entries.add(entry);}
        var document=new JsonObject();document.add("sources",entries);
        byte[] bytes=document.toString().getBytes(StandardCharsets.UTF_8);
        Identifier resourceId=atlasId.withPath("atlases/"+atlasId.getPath()+".json");
        var pack=new DefinitionPack(resourceId,bytes);
        var resource=new Resource(pack,()->new ByteArrayInputStream(bytes));
        return new ResourceManager(){
            public Optional<Resource> getResource(Identifier id){return id.equals(resourceId)?Optional.of(resource):manager.getResource(id);}
            public List<Resource> getResourceStack(Identifier id){return id.equals(resourceId)?List.of(resource):manager.getResourceStack(id);}
            public Set<String> getNamespaces(){var namespaces=new HashSet<>(manager.getNamespaces());namespaces.add(resourceId.getNamespace());return Set.copyOf(namespaces);}
            public Map<Identifier,Resource> listResources(String directory,Selector selector){var result=new HashMap<>(manager.listResources(directory,selector));if(resourceId.getPath().startsWith(directory+"/")&&selector.isIncluded(resourceId))result.put(resourceId,resource);return Map.copyOf(result);}
            public Map<Identifier,List<Resource>> listResourceStacks(String directory,Selector selector){var result=new HashMap<>(manager.listResourceStacks(directory,selector));if(resourceId.getPath().startsWith(directory+"/")&&selector.isIncluded(resourceId))result.put(resourceId,List.of(resource));return Map.copyOf(result);}
            public Stream<PackResources> listPacks(){return Stream.concat(manager.listPacks(),Stream.of(pack));}
        };
    }
    private static final class DefinitionPack extends AbstractPackMetadataResources implements PackResources {
        private final Identifier id; private final byte[] bytes;
        DefinitionPack(Identifier id,byte[] bytes){super(new PackLocationInfo("lms/source-owned-atlas",Component.literal("LMS source-owned atlas definition"),PackSource.BUILT_IN,Optional.empty()));this.id=id;this.bytes=bytes;}
        public IoSupplier<InputStream> getRootResource(String... path){return null;}
        public IoSupplier<InputStream> getResource(PackType type,Identifier id){return type==PackType.CLIENT_RESOURCES&&this.id.equals(id)?()->new ByteArrayInputStream(bytes):null;}
        public void listResources(PackType type,String namespace,String directory,ResourceOutput output){if(type==PackType.CLIENT_RESOURCES&&id.getNamespace().equals(namespace)&&id.getPath().startsWith(directory+"/"))output.accept(id,()->new ByteArrayInputStream(bytes));}
        public Set<String> getNamespaces(PackType type){return type==PackType.CLIENT_RESOURCES?Set.of(id.getNamespace()):Set.of();}
        public void close(){}
    }
}
