package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.banner.LegacyBannerPatterns;
import java.io.*;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.*;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/** Native dynamic registry/data lifecycle; no synthetic registry or RegisterEvent. */
public final class Target263BannerPatternPack {
    private Target263BannerPatternPack() {}
    public static void install(IEventBus bus) {
        bus.addListener(net.neoforged.bus.api.EventPriority.HIGHEST,false,
                net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent.class,event->LegacyBannerPatterns.materialize());
        bus.addListener(Target263BannerPatternPack::addPackFinders);
    }
    public static Map<Identifier,byte[]> resources() {
        Map<Identifier,byte[]> result=new LinkedHashMap<>();
        LegacyBannerPatterns.definitions().forEach((id,source)->{
            var value=new BannerPattern(id,"block.minecraft.banner."+id.toShortLanguageKey());
            var json=BannerPattern.DIRECT_CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,value).getOrThrow();
            result.put(Identifier.fromNamespaceAndPath(id.getNamespace(),"banner_pattern/"+id.getPath()+".json"),json.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        });
        var items=LegacyBannerPatterns.items();
        if(!items.isEmpty()) {
            var json=new com.google.gson.JsonObject();json.addProperty("replace",false);var values=new com.google.gson.JsonArray();
            items.stream().sorted(Comparator.comparing(Identifier::toString)).forEach(id->values.add(id.toString()));json.add("values",values);
            result.put(Identifier.withDefaultNamespace("tags/item/loom_patterns.json"),json.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return Collections.unmodifiableMap(result);
    }
    private static void addPackFinders(AddPackFindersEvent event) {
        if(event.getPackType()!=PackType.SERVER_DATA)return;
        var location=new PackLocationInfo("lms/legacy-banner-patterns",Component.literal("LMS source banner definitions"),PackSource.BUILT_IN,Optional.empty());
        var metadata=new Pack.Metadata(Component.literal("Native data-driven source banner declarations"),PackCompatibility.COMPATIBLE,FeatureFlagSet.of(),List.of());
        var supplier=new Pack.ResourcesSupplier(){
            public PackMetadataResources openMetadata(PackLocationInfo ignored){return new Resources(location,Map.of());}
            public java.util.stream.Stream<PackResources> openResources(PackLocationInfo ignored,Pack.Metadata info){
                var snapshot=resources();org.slf4j.LoggerFactory.getLogger(Target263BannerPatternPack.class).info("LMS source banner data resources={}",snapshot.size());
                return java.util.stream.Stream.of(new Resources(location,snapshot));
            }
        };
        event.addRepositorySource(out->out.accept(new Pack(location,supplier,metadata,new PackSelectionConfig(true,Pack.Position.BOTTOM,false))));
    }
    private static final class Resources extends AbstractPackMetadataResources implements PackResources {
        private final Map<Identifier,byte[]> values;
        Resources(PackLocationInfo location,Map<Identifier,byte[]> values){super(location);this.values=values;}
        public IoSupplier<InputStream> getRootResource(String... path){return null;}
        public IoSupplier<InputStream> getResource(PackType type,Identifier id){var bytes=type==PackType.SERVER_DATA?values.get(id):null;return bytes==null?null:()->new ByteArrayInputStream(bytes);}
        public void listResources(PackType type,String namespace,String path,ResourceOutput output){if(type!=PackType.SERVER_DATA)return;values.forEach((id,bytes)->{if(id.getNamespace().equals(namespace)&&(path.isEmpty()||id.getPath().startsWith(path+"/")))output.accept(id,()->new ByteArrayInputStream(bytes));});}
        public Set<String> getNamespaces(PackType type){return type!=PackType.SERVER_DATA?Set.of():values.keySet().stream().map(Identifier::getNamespace).collect(java.util.stream.Collectors.toUnmodifiableSet());}
        public void close(){}
    }
}
