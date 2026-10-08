package com.fuckingdevelop.lms.discovery;

import java.nio.file.Path;

public record LegacyModDescriptor(Path file, String modId, String loader, String minecraftVersion,
                                  String evidence) {
}
