import java.nio.file.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.*;
import com.google.gson.*;
import com.fuckingdeveloper.lms.mapping.MappingFileLoader;
import com.fuckingdeveloper.lms.mapping.ProguardMappingReader;

/**
 * Regenerates source facts from the immutable vanilla 1.19.2 runtime in an
 * isolated loader. Run with compiled LMS mapping utilities and Gson on the
 * classpath; arguments: source version directory, Gradle module artifacts cache.
 * The snapshot is emitted on the original stdout, retained before Minecraft redirects it.
 */
public class GenerateForge1192MaterialSnapshot {
    static ProguardMappingReader.Index mappings;
    static ClassLoader source;
    static final java.io.PrintStream output = System.out;
    static Class<?> type(String named) throws Exception {
        return Class.forName(mappings.obfuscatedClass(named).orElseThrow(), true, source);
    }
    static Method method(String owner, String name, String params, Class<?>... types) throws Exception {
        return type(owner).getMethod(mappings.find(owner, name, params).orElseThrow().obfuscatedName(), types);
    }
    static Field field(String owner, String name) throws Exception {
        String obfuscated = mappings.namedFields().stream().filter(f -> f.namedOwner().equals(owner)
                && f.namedName().equals(name)).findFirst().orElseThrow().obfuscatedName();
        return type(owner).getField(obfuscated);
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Expected source version directory and Gradle artifacts cache");
        Path version = Path.of(args[0]);
        Path artifacts = Path.of(args[1]);
        mappings = new MappingFileLoader().load(version.resolve("client_mappings.txt")).orElseThrow().index();
        var urls = new ArrayList<URL>();
        urls.add(version.resolve("client.jar").toUri().toURL());
        var manifest = JsonParser.parseString(Files.readString(version.resolve("version.json"))).getAsJsonObject();
        for (var element : manifest.getAsJsonArray("libraries")) {
            var lib = element.getAsJsonObject();
            if (!lib.has("downloads") || !lib.getAsJsonObject("downloads").has("artifact")) continue;
            String[] coordinate = lib.get("name").getAsString().split(":");
            Path root = artifacts.resolve(coordinate[0] + "/" + coordinate[1] + "/" + coordinate[2]);
            if (!Files.isDirectory(root)) continue;
            try (var files = Files.walk(root)) {
                var jar = files.filter(f -> f.toString().endsWith("/" + coordinate[1] + "-" + coordinate[2] + ".jar")).findFirst();
                if (jar.isPresent()) urls.add(jar.get().toUri().toURL());
            }
        }
        source = new URLClassLoader(urls.toArray(URL[]::new), ClassLoader.getPlatformClassLoader());
        method("net.minecraft.SharedConstants", "tryDetectVersion", "").invoke(null);
        method("net.minecraft.server.Bootstrap", "bootStrap", "").invoke(null);
        String materialOwner = "net.minecraft.world.level.material.Material";
        var materials = new IdentityHashMap<Object,String>();
        var materialRows = new TreeMap<String,String>();
        for (var f : mappings.namedFields()) {
            if (!f.namedOwner().equals(materialOwner) || !f.namedType().equals(materialOwner)) continue;
            var actual = type(materialOwner).getField(f.obfuscatedName()).get(null);
            materials.put(actual, f.namedName());
            int color = field("net.minecraft.world.level.material.MaterialColor", "id").getInt(
                    method(materialOwner, "getColor", "").invoke(actual));
            StringBuilder row = new StringBuilder(f.namedName()).append('\t').append(color);
            for (String predicate : List.of("isLiquid", "isSolid", "blocksMotion", "isFlammable", "isReplaceable", "isSolidBlocking"))
                row.append('\t').append(method(materialOwner, predicate, "").invoke(actual));
            row.append('\t').append(((Enum<?>)method(materialOwner, "getPushReaction", "").invoke(actual)).name());
            materialRows.put(f.namedName(), row.toString());
        }
        var registry = field("net.minecraft.core.Registry", "BLOCK").get(null);
        var getKey = method("net.minecraft.core.Registry", "getKey", "java.lang.Object", Object.class);
        var defaultState = method("net.minecraft.world.level.block.Block", "defaultBlockState", "");
        var getMaterial = method("net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase", "getMaterial", "");
        var blockRows = new TreeMap<String,String>();
        for (Object block : (Iterable<?>)registry) {
            String name = materials.get(getMaterial.invoke(defaultState.invoke(block)));
            if (name == null) throw new AssertionError("Unnamed material");
            String key = getKey.invoke(registry, block).toString();
            blockRows.put(key, key + "\t" + name);
        }
        // Bootstrap redirects output; retain the original stream for the data.
        output.println("# Minecraft 1.19.2 registry bootstrap; read-only source snapshot.");
        output.println("# Source client SHA-256: " + java.util.HexFormat.of().formatHex(
                java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(version.resolve("client.jar")))));
        output.println("# M: name, palette id, liquid, solid, blocksMotion, flammable, replaceable, solidBlocking, source push reaction");
        output.println("# B: exact source block registry identifier, material name");
        materialRows.values().forEach(row -> output.println("M\t" + row));
        blockRows.values().forEach(row -> output.println("B\t" + row));
    }
}
