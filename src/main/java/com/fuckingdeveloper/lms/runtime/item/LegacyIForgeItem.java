package com.fuckingdeveloper.lms.runtime.item;

/**
 * Structural compatibility contract for Forge 1.19.2 IForgeItem.
 *
 * <p>The legacy type was an interface and can appear in user-defined interface
 * inheritance. Mapping it directly to modern Item corrupts JVM type shape.
 * Operations are adapted independently; this marker preserves only the legacy
 * interface identity until each executable boundary has semantic evidence.</p>
 */
public interface LegacyIForgeItem {
}
