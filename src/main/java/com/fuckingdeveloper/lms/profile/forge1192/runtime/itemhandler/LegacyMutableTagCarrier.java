package com.fuckingdeveloper.lms.profile.forge1192.runtime.itemhandler;

public interface LegacyMutableTagCarrier {
    /** Source tag reads do not disappear when the native stack's count becomes zero. */
    net.minecraft.core.component.DataComponentMap lms$sourceComponents();
    LegacyMutableTagState lms$mutableTags();
    LegacyMutableTagState lms$existingMutableTags();
}
