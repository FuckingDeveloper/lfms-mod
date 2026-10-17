package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.target.neoforge263.Target263LegacyStaticItemResources;
import com.fuckingdeveloper.lms.target.neoforge263.render.Target263LegacyItemTint;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.resources.Identifier;

final class StaticItemResourcesCheck {
    static void run() throws Exception {
        blockInheritance();
        nativeModelParsing();
        modelAtlasDependencies();
        Path archive = Files.createTempFile("lms-static-item-probe-", ".jar");
        try {
            var entries = Map.of(
                    "assets/probe/models/item/generated.json", "{\"parent\":\"item/generated\",\"textures\":{\"layer0\":\"probe:item/icon\"}}",
                    "assets/probe/models/item/tool.json", "{\"parent\":\"minecraft:item/handheld\"}",
                    "assets/probe/models/item/dynamic.json", "{\"parent\":\"item/generated\",\"overrides\":[]}",
                    "assets/probe/models/item/entity.json", "{\"parent\":\"builtin/entity\"}",
                    "assets/probe/models/item/custom.json", "{\"parent\":\"item/generated\",\"loader\":\"probe:custom\"}",
                    "assets/probe/models/item/native.json", "{\"parent\":\"item/generated\"}",
                    "assets/probe/items/native.json", "{\"explicit\":true}");
            try (var zip = new ZipOutputStream(Files.newOutputStream(archive))) {
                for (var entry : entries.entrySet()) {
                    zip.putNextEntry(new ZipEntry(entry.getKey()));
                    zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
                }
            }
            byte[] original = Files.readAllBytes(archive);
            var definitions = Target263LegacyStaticItemResources.definitions(archive);
            check(definitions.keySet().equals(Set.of(id("items/generated.json"), id("items/tool.json"), id("items/entity.json"), id("models/lms_legacy/item/entity.json"))),
                    "unsupported or explicit native models replaced");
            var document = JsonParser.parseString(new String(definitions.get(id("items/generated.json")), StandardCharsets.UTF_8)).getAsJsonObject();
            var model = document.getAsJsonObject("model");
            check(model.get("type").getAsString().equals("minecraft:model"), "native model dispatch identity");
            var tints = model.remove("tints").getAsJsonArray();
            var nativeModel = CuboidItemModelWrapper.Unbaked.MAP_CODEC.codec().parse(JsonOps.INSTANCE, model).getOrThrow();
            check(nativeModel.model().equals(id("item/generated")), "original model identity lost");
            check(tints.size() == 5, "source generated layer count changed");
            NativeDefaultComponentFixture.run(() -> {
            for (int i = 0; i < tints.size(); i++) {
                var tint = Target263LegacyItemTint.CODEC.codec().parse(JsonOps.INSTANCE, tints.get(i)).getOrThrow();
                check(tint.index() == i, "source layer tint index changed");
                check(tint.calculate(com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192EarlyItemStackBridge.create(net.minecraft.world.item.Items.STICK), null, null) == -1,
                        "absent source tint did not preserve opaque white");
            }
            var stack = com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192EarlyItemStackBridge.create(net.minecraft.world.item.Items.APPLE);
            var seen = new ArrayList<Integer>();
            new com.fuckingdeveloper.lms.profile.forge1192.runtime.event.LegacyRegisterColorHandlersEvent.Item().register(
                    (item, index) -> { seen.add(index); return 0x123456; }, net.minecraft.world.item.Items.APPLE);
            check(new Target263LegacyItemTint(3).calculate(stack, null, null) == 0xFF123456
                    && seen.equals(List.of(3)), "native tint did not invoke source callback exactly once");
            });
            var pack = Target263LegacyStaticItemResources.createPack(archive);
            try (var manager = new net.minecraft.server.packs.resources.MultiPackResourceManager(
                    net.minecraft.server.packs.PackType.CLIENT_RESOURCES, pack.open().toList())) {
                check(manager.listResources("items", i -> true).size() == 3, "native resource discovery lost definitions");
                check(manager.getResource(id("items/generated.json")).isPresent(), "native direct resource lookup lost definition");
                var custom = JsonParser.parseString(new String(definitions.get(id("items/entity.json")), StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("model");
                check(custom.get("type").getAsString().equals("lms:legacy_gui_item"), "source custom selection lost");
                var decoded = com.fuckingdeveloper.lms.target.neoforge263.render.Target263LegacyGuiItemModel.Unbaked.CODEC.codec().parse(JsonOps.INSTANCE, custom).getOrThrow();
                check(decoded.base().equals(id("lms_legacy/item/entity")), "custom base identity lost");
                var base = JsonParser.parseString(new String(definitions.get(id("models/lms_legacy/item/entity.json")), StandardCharsets.UTF_8)).getAsJsonObject();
                check(!base.has("parent"), "builtin/entity marker left as target geometry parent");
            }
            check(Arrays.equals(original, Files.readAllBytes(archive)), "source artifact modified");
        } finally { Files.deleteIfExists(archive); }
        Path iron = Path.of("legacy-mods/ironchest-1.19.2-14.2.7.jar");
        if (Files.isRegularFile(iron)) check(Target263LegacyStaticItemResources.definitions(iron).size() == 35,
                "immutable source generated-model inventory changed");
        System.out.println("PASS: native generated-model definition codec/resource discovery, five source tint callbacks, explicit-model precedence and immutable artifact inventory");
    }
    private static void modelAtlasDependencies() throws Exception {
        Path archive=Files.createTempFile("lms-source-model-textures-", ".jar");
        try {
            var entries=Map.of("models/block/source.json","{\"textures\":{\"a\":\"probe:blocks/custom\",\"b\":\"#a\",\"c\":\"probe:entity/model_skin\",\"external\":\"other:item/unproven\"}}",
                    "textures/blocks/custom.png","probe","textures/entity/model_skin.png","probe","textures/unused.png","probe");
            try(var zip=new ZipOutputStream(Files.newOutputStream(archive))) {for(var entry:entries.entrySet()){zip.putNextEntry(new ZipEntry("assets/probe/"+entry.getKey()));zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));zip.closeEntry();}}
            byte[] original=Files.readAllBytes(archive);
            var sprites=com.fuckingdeveloper.lms.target.neoforge263.Target263LegacyAtlasSources.modelTextures(List.of(archive));
            check(sprites.equals(Set.of(id("blocks/custom"),id("entity/model_skin"))),"model texture identities, variable slots, external or unused PNG selection changed");
            var definitions=com.fuckingdeveloper.lms.target.neoforge263.Target263LegacyAtlasSources.definitions(List.of(net.minecraft.data.AtlasIds.BLOCKS,Identifier.parse("probe:custom_atlas")),sprites);
            var source=com.google.gson.JsonParser.parseString(new String(definitions.get(Identifier.parse("minecraft:atlases/blocks.json")),StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("sources");
            check(source.size()==3,"missing/duplicated native single sources or source callback intent");
            for(int i=1;i<source.size();i++) {
                var nativeSource=net.minecraft.client.renderer.texture.atlas.sources.SingleFile.MAP_CODEC.codec().parse(JsonOps.INSTANCE,source.get(i)).getOrThrow();
                check(sprites.contains(nativeSource.resourceId())&&nativeSource.spriteId().isEmpty(),"native sprite source renamed source texture identity");
            }
            var custom=JsonParser.parseString(new String(definitions.get(id("atlases/custom_atlas.json")),StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("sources");
            check(custom.size()==1,"block-model textures leaked into custom atlas");
            check(Arrays.equals(original,Files.readAllBytes(archive)),"model texture artifact changed");
        }finally{Files.deleteIfExists(archive);}
    }
    private static void nativeModelParsing() throws Exception {
        // Actual native model loading reads one strict root, without an extra
        // END_DOCUMENT check. Resource inventory must not be stricter than baking.
        String source="{\"parent\":\"minecraft:block/cube\"}}}";
        var nativeModel=net.minecraft.client.resources.model.cuboid.CuboidModel.fromStream(new java.io.StringReader(source));
        check(nativeModel!=null,"native single-root model contract changed");
        Path archive=Files.createTempFile("lms-native-model-parser-", ".jar");
        try {
            try(var zip=new ZipOutputStream(Files.newOutputStream(archive))) {
                for(var entry:Map.of("models/item/test.json","{\"parent\":\"probe:block/base\"}}", "models/block/base.json",source).entrySet()) {
                    zip.putNextEntry(new ZipEntry("assets/probe/"+entry.getKey()));zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));zip.closeEntry();
                }
            }
            byte[] original=Files.readAllBytes(archive);
            check(Target263LegacyStaticItemResources.definitions(archive).containsKey(id("items/test.json")),"native-readable source model rejected by inventory");
            check(Arrays.equals(original,Files.readAllBytes(archive)),"source resource changed");
            try{net.minecraft.util.GsonHelper.parse(new java.io.StringReader("{\"parent\":"));throw new AssertionError("malformed root accepted");}catch(JsonParseException expected){}
        }finally{Files.deleteIfExists(archive);}
    }
    private static void blockInheritance() throws Exception {
        Path archive = Files.createTempFile("lms-static-block-models-", ".jar");
        try {
            var entries = new LinkedHashMap<String, String>();
            entries.put("models/item/cube.json", "{\"parent\":\"probe:block/base\"}");
            entries.put("models/block/base.json", "{\"parent\":\"minecraft:block/cube_bottom_top\",\"textures\":{\"side\":\"probe:block/side\"}}");
            entries.put("models/item/geometry.json", "{\"parent\":\"probe:block/geometry\"}");
            entries.put("models/block/geometry.json", "{\"textures\":{\"a\":\"probe:block/a\"},\"elements\":[{\"from\":[0,0,0],\"to\":[16,8,16],\"faces\":{\"up\":{\"texture\":\"#a\"}}}]}");
            entries.put("models/item/unknown_leaf.json", "{\"parent\":\"probe:block/unknown_leaf\"}");
            entries.put("models/block/unknown_leaf.json", "{\"textures\":{}}");
            entries.put("models/item/retained_template.json", "{\"parent\":\"minecraft:block/cube_all\"}");
            entries.put("models/item/changed_template.json", "{\"parent\":\"minecraft:block/wall_inventory\"}");
            entries.put("models/item/direct.json", "{\"parent\":\"minecraft:block/cube\"}");
            entries.put("models/item/missing.json", "{\"parent\":\"probe:block/absent\"}");
            entries.put("models/item/cycle.json", "{\"parent\":\"probe:block/cycle\"}");
            entries.put("models/block/cycle.json", "{\"parent\":\"probe:item/cycle\"}");
            entries.put("models/item/custom_parent.json", "{\"parent\":\"probe:block/custom\"}");
            entries.put("models/block/custom.json", "{\"parent\":\"minecraft:block/cube\",\"loader\":\"probe:custom\"}");
            entries.put("models/item/override_parent.json", "{\"parent\":\"probe:block/dynamic\"}");
            entries.put("models/block/dynamic.json", "{\"parent\":\"minecraft:block/cube\",\"overrides\":[]}");
            entries.put("models/item/external.json", "{\"parent\":\"other:block/base\"}");
            entries.put("models/item/inherited_entity.json", "{\"parent\":\"probe:block/entity\"}");
            entries.put("models/block/entity.json", "{\"parent\":\"minecraft:builtin/entity\"}");
            entries.put("models/item/native.json", "{\"parent\":\"probe:block/base\"}");
            entries.put("items/native.json", "{\"explicit\":true}");
            try (var zip = new ZipOutputStream(Files.newOutputStream(archive))) {
                for (var entry : entries.entrySet()) {
                    zip.putNextEntry(new ZipEntry("assets/probe/" + entry.getKey()));
                    zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
                }
            }
            byte[] original = Files.readAllBytes(archive);
            var definitions = Target263LegacyStaticItemResources.definitions(archive);
            check(definitions.keySet().equals(Set.of(id("items/cube.json"), id("items/direct.json"), id("items/geometry.json"), id("items/retained_template.json"))),
                    "closed block inheritance admission/negative boundaries");
            var model = JsonParser.parseString(new String(definitions.get(id("items/cube.json")), StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonObject("model");
            model.remove("tints");
            var decoded = CuboidItemModelWrapper.Unbaked.MAP_CODEC.codec().parse(JsonOps.INSTANCE, model).getOrThrow();
            check(decoded.model().equals(id("item/cube")), "inherited source model identity lost");
            check(Arrays.equals(original, Files.readAllBytes(archive)), "block-model artifact modified");
        } finally { Files.deleteIfExists(archive); }
    }
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("probe", path); }
    private static void check(boolean value, String reason) { if (!value) throw new AssertionError(reason); }
}
