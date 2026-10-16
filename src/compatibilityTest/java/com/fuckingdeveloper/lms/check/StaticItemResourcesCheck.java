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
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("probe", path); }
    private static void check(boolean value, String reason) { if (!value) throw new AssertionError(reason); }
}
