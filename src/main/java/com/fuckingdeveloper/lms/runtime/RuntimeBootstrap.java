package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.discovery.LegacyJarScanner;
import com.fuckingdeveloper.lms.profile.SourceProfiles;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import org.slf4j.LoggerFactory;

/** Select all source identities before admitting any managed session. */
public final class RuntimeBootstrap {
    private RuntimeBootstrap() {}

    public static void start(Path directory, SourceProfiles profiles,
                             List<? extends SourceRuntimeBinding> bindings) throws IOException {
        var log = LoggerFactory.getLogger("com.fuckingdeveloper.lms.LmsMod");
        var indexed = new LinkedHashMap<String, SourceRuntimeBinding>();
        for (var binding : bindings) {
            if (indexed.putIfAbsent(binding.profile().id(), binding) != null)
                throw new IllegalArgumentException("Duplicate runtime binding: " + binding.profile().id());
            var installed = profiles.installed().stream().filter(p -> p == binding.profile()).findFirst();
            if (installed.isEmpty()) throw new IllegalArgumentException("Runtime binding has another source profile instance");
        }
        Files.createDirectories(directory);
        var discovered = new LegacyJarScanner().scan(directory);
        var selected = new LinkedHashMap<SourceRuntimeBinding, java.util.ArrayList<com.fuckingdeveloper.lms.discovery.LegacyModDescriptor>>();
        log.info("LMS discovery: {} candidate(s) in {}", discovered.size(), directory.toAbsolutePath());
        for (var mod : discovered) {
            var profile = profiles.resolve(mod);
            log.info("LMS candidate={} id={} loader={} minecraft={} profile={} evidence={}", mod.file().getFileName(),
                    mod.modId(), mod.loader(), mod.minecraftVersion(), profile.map(p -> p.id()).orElse("UNRESOLVED"), mod.evidence());
            if (profile.isEmpty()) continue;
            var binding = indexed.get(profile.get().id());
            if (binding == null) throw new IllegalStateException("Missing runtime binding for " + profile.get().id());
            var missing = new java.util.HashSet<>(profile.get().requiredContracts());
            missing.removeAll(binding.providedContracts());
            if (!missing.isEmpty()) throw new IllegalStateException("Missing target contracts for " + profile.get().id() + ": " + missing);
            log.info("LMS profile-binding id={} source={} target={} contracts={}",
                    mod.modId(),profile.get().id(),binding.targetId(),binding.providedContracts());
            selected.computeIfAbsent(binding, ignored -> new java.util.ArrayList<>()).add(mod);
        }
        if (selected.size() > 1)
            throw new UnsupportedOperationException("Mixed source-generation runtime admission needs dependency-domain ordering");
        for (var entry : selected.entrySet()) entry.getKey().start(List.copyOf(entry.getValue()), discovered);
    }
}
