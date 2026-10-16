package com.fuckingdeveloper.lms.runtime;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.*;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipFile;

/** Immutable source GUI sheets at isolated IDs; modern vanilla resources are never overwritten. */
public final class Target263LegacyWidgetAssets extends AbstractPackMetadataResources implements PackResources {
    public static final Identifier WIDGETS=Identifier.fromNamespaceAndPath("lms","legacy/forge1192/widgets.png");
    private static final String PREFIX="assets/minecraft/textures/gui/";
    private static volatile Map<Identifier,Identifier> projection=Map.of();
    private final Map<Identifier,byte[]> resources;
    private Target263LegacyWidgetAssets(PackLocationInfo location,Map<Identifier,byte[]> resources){super(location);this.resources=resources;}
    /** Called only at the source GUI binding boundary; no native resource namespace is changed. */
    public static Identifier sourceTexture(Identifier source){return projection.getOrDefault(source,source);}
    private static Identifier isolated(String relative){return (relative.equals("widgets.png")||relative.equals("widgets.png.mcmeta"))?Identifier.fromNamespaceAndPath("lms","legacy/forge1192/"+relative):Identifier.fromNamespaceAndPath("lms","legacy/forge1192/gui/"+relative);}
    public static Pack createPack(List<Path> artifacts) {
        var entries=new TreeMap<String,byte[]>();
        var configured=System.getProperty("lms.forge1192.clientAssets");
        if(configured!=null && !configured.isBlank())read(Path.of(configured),entries);
        for(var artifact:artifacts)read(artifact,entries);
        var bindings=new HashMap<Identifier,Identifier>();var indexed=new HashMap<Identifier,byte[]>();
        for(var entry:entries.entrySet()){
            String relative=entry.getKey();indexed.put(isolated(relative),entry.getValue());
            if(relative.endsWith(".png"))bindings.put(Identifier.fromNamespaceAndPath("minecraft","textures/gui/"+relative),isolated(relative));
        }
        projection=Map.copyOf(bindings);
        if(indexed.isEmpty())return null;
        final Map<Identifier,byte[]> data=Map.copyOf(indexed);
        String hash;
        try{
            var digest=java.security.MessageDigest.getInstance("SHA-256");
            for(var entry:entries.entrySet()){
                byte[] name=entry.getKey().getBytes(StandardCharsets.UTF_8),bytes=entry.getValue();
                digest.update(java.nio.ByteBuffer.allocate(8).putInt(name.length).putInt(bytes.length).array());digest.update(name);digest.update(bytes);
            }
            hash=HexFormat.of().formatHex(digest.digest());
        }catch(java.security.NoSuchAlgorithmException impossible){throw new ExceptionInInitializerError(impossible);}
        var location=new PackLocationInfo("lms/legacy-widget-source/forge1192/"+hash,Component.literal("LMS source GUI sheets"),PackSource.BUILT_IN,Optional.empty());
        var metadata=new Pack.Metadata(Component.literal("Immutable source-generation GUI pixels"),PackCompatibility.COMPATIBLE,FeatureFlagSet.of(),List.of());
        var supplier=new Pack.ResourcesSupplier(){
            public PackMetadataResources openMetadata(PackLocationInfo ignored){return new Target263LegacyWidgetAssets(location,data);}
            public java.util.stream.Stream<PackResources> openResources(PackLocationInfo ignored,Pack.Metadata meta){return java.util.stream.Stream.of(new Target263LegacyWidgetAssets(location,data));}
        };
        return new Pack(location,supplier,metadata,new PackSelectionConfig(true,Pack.Position.TOP,false));
    }
    private static void read(Path archive,Map<String,byte[]> output) {
        try(var zip=new ZipFile(archive.toFile())){
            var entries=zip.entries();while(entries.hasMoreElements()){
                var entry=entries.nextElement();String path=entry.getName();
                if(entry.isDirectory()||!path.startsWith(PREFIX)||!(path.endsWith(".png")||path.endsWith(".png.mcmeta")))continue;
                try(var input=zip.getInputStream(entry)){output.put(path.substring(PREFIX.length()),input.readAllBytes());}
            }
        }catch(IOException failure){throw new IllegalStateException("Cannot read source GUI assets from "+archive,failure);}
    }
    public static void requireAvailable() {
        if(net.minecraft.client.Minecraft.getInstance().getResourceManager().getResource(WIDGETS).isEmpty())
            throw new UnsupportedOperationException("Source widget sheet missing; supply source 1.19.2 client assets with lms.forge1192.clientAssets");
    }
    public IoSupplier<InputStream> getRootResource(String... path){return null;}
    public IoSupplier<InputStream> getResource(PackType type,Identifier id){byte[] bytes=type==PackType.CLIENT_RESOURCES?resources.get(id):null;return bytes==null?null:()->new ByteArrayInputStream(bytes);}
    public void listResources(PackType type,String namespace,String directory,ResourceOutput output){if(type!=PackType.CLIENT_RESOURCES)return;resources.forEach((id,bytes)->{if(id.getNamespace().equals(namespace)&&(directory.isEmpty()||id.getPath().startsWith(directory+"/")))output.accept(id,()->new ByteArrayInputStream(bytes));});}
    public Set<String> getNamespaces(PackType type){return type==PackType.CLIENT_RESOURCES?Set.of("lms"):Set.of();}
    public void close(){}
}
