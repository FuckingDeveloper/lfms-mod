package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.Target263LegacyWidgetAssets;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

final class SourceGuiAssetsCheck {
    static void run() throws Exception {
        String key="lms.forge1192.clientAssets",previous=System.getProperty(key);
        Path archive=previous==null?Path.of(System.getProperty("user.home"),".gradle/caches/forge_gradle/minecraft_repo/versions/1.19.2/client-extra.jar"):Path.of(previous);
        check(Files.isRegularFile(archive),"pinned source GUI archive unavailable");
        try {
            System.setProperty(key,archive.toString());
            var pack=Target263LegacyWidgetAssets.createPack(List.of());
            var source=Identifier.withDefaultNamespace("textures/gui/checkbox.png");var projected=Target263LegacyWidgetAssets.sourceTexture(source);
            check(projected.getNamespace().equals("lms")&&!projected.equals(source),"source checkbox sheet not isolated");
            check(Target263LegacyWidgetAssets.sourceTexture(Identifier.withDefaultNamespace("textures/gui/widgets.png")).equals(Target263LegacyWidgetAssets.WIDGETS),"existing widget identity changed");
            for(var untouched:List.of(Identifier.fromNamespaceAndPath("other","textures/gui/checkbox.png"),Identifier.withDefaultNamespace("textures/block/stone.png"),Identifier.withDefaultNamespace("textures/gui/not_a_source_sheet.png")))check(Target263LegacyWidgetAssets.sourceTexture(untouched).equals(untouched),"unverified source resource rewritten");
            try(var stream=pack.open();var zip=new ZipFile(archive.toFile())){
                var resources=stream.findFirst().orElseThrow();int count=0;
                var entries=zip.entries();while(entries.hasMoreElements()){
                    var entry=entries.nextElement();String name=entry.getName();if(!name.startsWith("assets/minecraft/textures/gui/")||!name.endsWith(".png"))continue;
                    var id=Target263LegacyWidgetAssets.sourceTexture(Identifier.withDefaultNamespace(name.substring("assets/minecraft/".length())));
                    try(var original=zip.getInputStream(entry);var exposed=resources.getResource(PackType.CLIENT_RESOURCES,id).get()){check(Arrays.equals(original.readAllBytes(),exposed.readAllBytes()),"source GUI pixels changed: "+name);}count++;
                }
                check(count>1,"only one hardcoded sheet exposed");
                check(resources.getResource(PackType.CLIENT_RESOURCES,source)==null&&resources.getResource(PackType.SERVER_DATA,projected)==null,"native namespace/data resource replaced");
                var listed=new HashSet<Identifier>();resources.listResources(PackType.CLIENT_RESOURCES,"lms","legacy/forge1192",(id,input)->listed.add(id));
                check(listed.contains(projected)&&listed.contains(Target263LegacyWidgetAssets.WIDGETS)&&resources.getNamespaces(PackType.CLIENT_RESOURCES).equals(Set.of("lms")),"resource listing/namespace diverges");
                byte[] original;try(var input=resources.getResource(PackType.CLIENT_RESOURCES,projected).get()){original=input.readAllBytes();}
                try(var image=com.mojang.blaze3d.platform.NativeImage.read(original)){check(image.getWidth()==64&&image.getHeight()==64,"source checkbox UV dimensions differ");}
                Path override=Files.createTempFile("lms-gui-override", ".jar");byte[] meta="{\"texture\":{\"blur\":false,\"clamp\":true}}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                try(var out=new ZipOutputStream(Files.newOutputStream(override))){
                    out.putNextEntry(new ZipEntry("assets/minecraft/textures/gui/checkbox.png"));out.write(original);out.closeEntry();
                    out.putNextEntry(new ZipEntry("assets/minecraft/textures/gui/checkbox.png.mcmeta"));out.write(meta);out.closeEntry();
                }
                var changed=Target263LegacyWidgetAssets.createPack(List.of(override));check(!changed.getId().equals(pack.getId()),"metadata not in cache identity");
                try(var overlays=changed.open()){
                    var overlay=overlays.findFirst().orElseThrow();var metadataId=projected.withPath(projected.getPath()+".mcmeta");
                    try(var input=overlay.getResource(PackType.CLIENT_RESOURCES,metadataId).get()){check(Arrays.equals(meta,input.readAllBytes()),"source texture metadata lost");}
                }
                check(resources.getResource(PackType.CLIENT_RESOURCES,projected.withPath(projected.getPath()+".mcmeta"))==null,"old pack snapshot mutated on rediscovery");
                resources.close();Files.delete(override);
            }
        }finally{
            if(previous==null)System.clearProperty(key);else System.setProperty(key,previous);
            Target263LegacyWidgetAssets.createPack(List.of());
        }
        System.out.println("PASS: actual source GUI catalog/pixel identity/checkbox dimensions, isolated bind IDs, native/unknown boundaries, metadata/cache identity and immutable pack snapshots");
    }
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
