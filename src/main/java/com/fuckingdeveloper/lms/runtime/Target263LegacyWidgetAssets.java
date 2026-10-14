package com.fuckingdeveloper.lms.runtime;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.*;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipFile;

/** Isolated source-generation GUI sheet; current vanilla resources are never overwritten. */
public final class Target263LegacyWidgetAssets extends AbstractPackMetadataResources implements PackResources {
    public static final Identifier WIDGETS=Identifier.fromNamespaceAndPath("lms","legacy/forge1192/widgets.png");
    private static final String ENTRY="assets/minecraft/textures/gui/widgets.png";
    private final byte[] bytes;
    private Target263LegacyWidgetAssets(PackLocationInfo location,byte[] bytes){super(location);this.bytes=bytes;}
    public static Pack createPack(List<Path> artifacts) {
        byte[] image=null;
        var configured=System.getProperty("lms.forge1192.clientAssets");
        if(configured!=null && !configured.isBlank())image=read(Path.of(configured));
        for(var artifact:artifacts){byte[] replacement=read(artifact);if(replacement!=null)image=replacement;}
        if(image==null)return null;
        final byte[] data=image;
        String hash;try{hash=HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(data));}catch(java.security.NoSuchAlgorithmException impossible){throw new ExceptionInInitializerError(impossible);}
        var location=new PackLocationInfo("lms/legacy-widget-source/forge1192/"+hash,Component.literal("LMS source widget sheet"),PackSource.BUILT_IN,Optional.empty());
        var metadata=new Pack.Metadata(Component.literal("Immutable source-generation widget pixels"),PackCompatibility.COMPATIBLE,FeatureFlagSet.of(),List.of());
        var supplier=new Pack.ResourcesSupplier(){
            public PackMetadataResources openMetadata(PackLocationInfo ignored){return new Target263LegacyWidgetAssets(location,data);}
            public java.util.stream.Stream<PackResources> openResources(PackLocationInfo ignored,Pack.Metadata meta){return java.util.stream.Stream.of(new Target263LegacyWidgetAssets(location,data));}
        };
        return new Pack(location,supplier,metadata,new PackSelectionConfig(true,Pack.Position.TOP,false));
    }
    private static byte[] read(Path archive) {
        try(var zip=new ZipFile(archive.toFile())){var entry=zip.getEntry(ENTRY);if(entry==null)return null;try(var input=zip.getInputStream(entry)){return input.readAllBytes();}}
        catch(IOException failure){throw new IllegalStateException("Cannot read source widget assets from "+archive,failure);}
    }
    public static void requireAvailable() {
        if(net.minecraft.client.Minecraft.getInstance().getResourceManager().getResource(WIDGETS).isEmpty())
            throw new UnsupportedOperationException("Source widget sheet missing; supply source 1.19.2 client assets with lms.forge1192.clientAssets");
    }
    public IoSupplier<InputStream> getRootResource(String... path){return null;}
    public IoSupplier<InputStream> getResource(PackType type,Identifier id){return type==PackType.CLIENT_RESOURCES&&id.equals(WIDGETS)?()->new ByteArrayInputStream(bytes):null;}
    public void listResources(PackType type,String namespace,String directory,ResourceOutput output){if(type==PackType.CLIENT_RESOURCES&&namespace.equals(WIDGETS.getNamespace())&&(directory.isEmpty()||WIDGETS.getPath().startsWith(directory+"/")))output.accept(WIDGETS,()->new ByteArrayInputStream(bytes));}
    public Set<String> getNamespaces(PackType type){return type==PackType.CLIENT_RESOURCES?Set.of(WIDGETS.getNamespace()):Set.of();}
    public void close(){}
}
