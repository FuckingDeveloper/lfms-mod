package com.fuckingdeveloper.lms.discovery;

import java.nio.file.Path;

public record LegacyModDescriptor(Path file, String modId, String loader, String minecraftVersion,
                                  String evidence, String languageLoader, String languageLoaderVersion) {
    public LegacyModDescriptor(Path file, String modId, String loader, String minecraftVersion, String evidence) {
        this(file, modId, loader, minecraftVersion, evidence, "", "");
    }
}
