package com.fuckingdeveloper.lms.runtime;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Manifest;
import java.util.zip.ZipFile;
import net.neoforged.neoforgespi.language.*;
import net.neoforged.neoforgespi.locating.*;
import org.apache.maven.artifact.versioning.*;

/** Actual immutable source TOML metadata; deliberately not native loader ModInfo. */
final class Forge1192MetadataView implements IModInfo {
    private final UnmodifiableConfig document,mod;
    private final String id,version;
    private final IModFileInfo file;
    private Forge1192MetadataView(UnmodifiableConfig document,UnmodifiableConfig mod,String version) {
        this.document=document;this.mod=mod;this.id=mod.get("modId");this.version=version;
        this.file=new SourceFile();
    }
    static IModInfo read(Path path,String id) {
        try(var zip=new ZipFile(path.toFile())) {
            var entry=zip.getEntry("META-INF/mods.toml");
            if(entry==null)throw new IllegalStateException("Missing source metadata: "+path);
            UnmodifiableConfig document;
            try(var reader=new InputStreamReader(zip.getInputStream(entry),StandardCharsets.UTF_8)){document=new TomlParser().parse(reader);}
            List<UnmodifiableConfig> mods=document.get("mods");
            var mod=mods.stream().filter(m->id.equals(m.get("modId"))).findFirst().orElseThrow();
            String version=text(mod,"version","1");
            if(version.contains("${file.jarVersion}")) {
                var manifest=zip.getEntry("META-INF/MANIFEST.MF");
                if(manifest==null)throw new IllegalStateException("No source manifest for version substitution");
                String jarVersion;try(var stream=zip.getInputStream(manifest)){jarVersion=new Manifest(stream).getMainAttributes().getValue("Implementation-Version");}
                if(jarVersion==null)throw new IllegalStateException("No source Implementation-Version");
                version=version.replace("${file.jarVersion}",jarVersion);
            }
            if(version.contains("${"))throw new UnsupportedOperationException("Unadapted source version substitution: "+version);
            return new Forge1192MetadataView(document,mod,version);
        }catch(IOException failure){throw new IllegalStateException("Cannot read source metadata "+path,failure);}
    }
    public String getModId(){return id;}
    public String getNamespace(){return text(mod,"namespace",id);}
    public String getDisplayName(){return text(mod,"displayName",id);}
    public String getDescription(){return text(mod,"description","MISSING DESCRIPTION").replace("\r\n","\n");}
    public ArtifactVersion getVersion(){return new DefaultArtifactVersion(version);}
    public IModFileInfo getOwningFile(){return file;}
    public IModLanguageLoader getLoader(){throw new UnsupportedOperationException("Managed source language loader has no native loader object");}
    public Map<String,Object> getModProperties(){return table(document,List.of("modproperties",id));}
    public Optional<URL> getUpdateURL(){return url(mod,"updateJSONURL");}
    public Optional<URL> getModURL(){return url(mod,"modUrl");}
    public Optional<String> getLogoFile(){return Optional.ofNullable(mod.<String>get("logoFile")).or(()->Optional.ofNullable(document.get("logoFile")));}
    public boolean getLogoBlur(){return mod.<Boolean>getOptional("logoBlur").orElseGet(()->document.<Boolean>getOptional("logoBlur").orElse(true));}
    public IConfigurable getConfig(){return configurable(mod);}
    public List<ForgeFeature.Bound> getForgeFeatures(){
        if(table(document,List.of("features",id)).isEmpty())return List.of();
        throw new UnsupportedOperationException("Source feature bounds require an explicit feature-generation adapter");
    }
    public List<? extends ModVersion> getDependencies(){
        List<UnmodifiableConfig> entries=document.getOptional(List.of("dependencies",id)).map(v->(List<UnmodifiableConfig>)v).orElse(List.of());
        return entries.stream().map(Dependency::new).toList();
    }
    private final class Dependency implements ModVersion {
        private final UnmodifiableConfig data;
        Dependency(UnmodifiableConfig data){this.data=data;}
        public String getModId(){return data.get("modId");}
        public VersionRange getVersionRange(){try{return VersionRange.createFromVersionSpec(text(data,"versionRange"," "));}catch(InvalidVersionSpecificationException failure){throw new IllegalStateException(failure);}}
        public DependencyType getType(){return data.<Boolean>getOptional("mandatory").orElse(true)?DependencyType.REQUIRED:DependencyType.OPTIONAL;}
        public Optional<String> getReason(){return Optional.empty();} // 1.19.2 has no reason declaration.
        public Ordering getOrdering(){return Ordering.valueOf(text(data,"ordering","NONE"));}
        public DependencySide getSide(){return DependencySide.valueOf(text(data,"side","BOTH"));}
        public void setOwner(IModInfo owner){if(owner!=Forge1192MetadataView.this)throw new UnsupportedOperationException("Immutable source dependency owner");}
        public IModInfo getOwner(){return Forge1192MetadataView.this;}
        public Optional<URL> getReferralURL(){return url(data,"referralUrl");}
    }
    private final class SourceFile implements IModFileInfo {
        public List<IModInfo> getMods(){
            List<UnmodifiableConfig> mods=document.get("mods");
            if(mods.size()!=1)throw new UnsupportedOperationException("Multi-entry source file metadata requires all admitted identities");
            return List.of(Forge1192MetadataView.this);
        }
        public List<LanguageSpec> requiredLanguageLoaders(){try{return List.of(new LanguageSpec(document.get("modLoader"),VersionRange.createFromVersionSpec(text(document,"loaderVersion"," "))));}catch(InvalidVersionSpecificationException failure){throw new IllegalStateException(failure);}}
        public boolean showAsResourcePack(){return document.<Boolean>getOptional("showAsResourcePack").orElse(false);}
        public boolean showAsDataPack(){throw new UnsupportedOperationException("Source file metadata has no independent data-pack declaration");}
        public Map<String,Object> getFileProperties(){return table(document,List.of("properties"));}
        public String getLicense(){return text(document,"license","");}
        public String versionString(){return version;}
        public List<String> usesServices(){return document.<List<String>>getOptional("services").orElse(List.of());}
        public IModFile getFile(){throw new UnsupportedOperationException("Managed source archive is not a native loader file");}
        public IConfigurable getConfig(){return configurable(document);}
    }
    private static IConfigurable configurable(UnmodifiableConfig data){return new IConfigurable(){
        public <T> Optional<T> getConfigElement(String... key){return data.getOptional(List.of(key));}
        public List<? extends IConfigurable> getConfigList(String... key){return data.<List<UnmodifiableConfig>>getOptional(List.of(key)).orElse(List.of()).stream().map(Forge1192MetadataView::configurable).toList();}
    };}
    private static String text(UnmodifiableConfig config,String key,String fallback){return config.<String>getOptional(key).orElse(fallback);}
    private static Map<String,Object> table(UnmodifiableConfig config,List<String> key){
        Object value=config.get(key);if(value==null)return Map.of();
        if(!(value instanceof UnmodifiableConfig table))throw new IllegalStateException("Expected source metadata table: "+key);
        var result=new LinkedHashMap<String,Object>();table.entrySet().forEach(e->result.put(e.getKey(),e.getValue()));return Collections.unmodifiableMap(result);
    }
    private static Optional<URL> url(UnmodifiableConfig data,String key){return data.<String>getOptional(key).map(value->{try{return java.net.URI.create(value).toURL();}catch(java.net.MalformedURLException failure){throw new IllegalStateException(failure);}});}
}
