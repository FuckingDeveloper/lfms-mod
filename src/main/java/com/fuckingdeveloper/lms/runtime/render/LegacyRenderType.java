package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;

/** Source-shaped identity for Minecraft 1.19.2 render types. */
public class LegacyRenderType extends LegacyRenderStateShard {
    public enum Kind {
        SOLID, CUTOUT_MIPPED, CUTOUT, TRANSLUCENT, TRIPWIRE,
        ENTITY_SOLID, ENTITY_CUTOUT, ENTITY_TRANSLUCENT_CULL, ENTITY_TRANSLUCENT,
        EYES, OUTLINE, CRUMBLING, TEXT, TEXT_SEE_THROUGH,
        LEASH, WATER_MASK, LIGHTNING, LINES, CUSTOM
    }

    private static final LegacyRenderType SOLID = new LegacyRenderType(Kind.SOLID, null);
    private static final LegacyRenderType CUTOUT_MIPPED = new LegacyRenderType(Kind.CUTOUT_MIPPED, null);
    private static final LegacyRenderType CUTOUT = new LegacyRenderType(Kind.CUTOUT, null);
    private static final LegacyRenderType TRANSLUCENT = new LegacyRenderType(Kind.TRANSLUCENT, null);
    private static final LegacyRenderType TRIPWIRE = new LegacyRenderType(Kind.TRIPWIRE, null);
    private static final LegacyRenderType LEASH = new LegacyRenderType(Kind.LEASH, null);
    private static final LegacyRenderType WATER_MASK = new LegacyRenderType(Kind.WATER_MASK, null);
    private static final LegacyRenderType LIGHTNING = new LegacyRenderType(Kind.LIGHTNING, null);
    private static final LegacyRenderType LINES = new LegacyRenderType(Kind.LINES, null);
    private static final List<LegacyRenderType> CHUNK_LAYERS = List.of(
            SOLID, CUTOUT_MIPPED, CUTOUT, TRANSLUCENT, TRIPWIRE);

    private final Kind kind;
    private final Identifier texture;
    private final String customName;
    private final int bufferSize;
    private final LegacyCompositeState compositeState;

    private LegacyRenderType(Kind kind, Identifier texture) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.texture = texture;
        this.customName = null;
        this.bufferSize = 0;
        this.compositeState = null;
    }

    /** Source constructor retained for legacy subclasses which fail closed themselves. */
    protected LegacyRenderType(String name, LegacyVertexFormat format,
            LegacyVertexFormatMode mode, int bufferSize, boolean affectsCrumbling,
            boolean sortOnUpload, Runnable setup, Runnable clear) {
        this.kind = Kind.CUSTOM;
        this.texture = null;
        this.customName = Objects.requireNonNull(name, "name");
        this.bufferSize = bufferSize;
        this.compositeState = null;
    }

    public Kind kind() { return kind; }
    public Identifier texture() { return texture; }
    public boolean isTranslucentChunkLayer() {
        return kind == Kind.TRANSLUCENT || kind == Kind.TRIPWIRE;
    }

    public int m_110507_() { return bufferSize; }

    public static LegacyCompositeRenderType m_173215_(String name, LegacyVertexFormat format,
            LegacyVertexFormatMode mode, int bufferSize, boolean affectsCrumbling,
            boolean sortOnUpload, LegacyCompositeState state) {
        return new LegacyCompositeRenderType(name, format, mode, bufferSize,
                sortOnUpload, state);
    }

    protected LegacyRenderType(String name, LegacyVertexFormat format,
            LegacyVertexFormatMode mode, int bufferSize, LegacyCompositeState state) {
        this.kind = Kind.CUSTOM;
        this.texture = null;
        this.customName = Objects.requireNonNull(name, "name");
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(mode, "mode");
        this.bufferSize = bufferSize;
        this.compositeState = Objects.requireNonNull(state, "state");
    }

    /** Exact Minecraft 1.19.2 SRG factory ABI. */
    public static LegacyRenderType m_110451_() { return SOLID; }
    public static LegacyRenderType m_110457_() { return CUTOUT_MIPPED; }
    public static LegacyRenderType m_110463_() { return CUTOUT; }
    public static LegacyRenderType m_110466_() { return TRANSLUCENT; }
    public static LegacyRenderType m_110503_() { return TRIPWIRE; }
    public static List<LegacyRenderType> m_110506_() { return CHUNK_LAYERS; }
    public static LegacyRenderType m_110446_(Identifier texture) { return textured(Kind.ENTITY_SOLID, texture); }
    public static LegacyRenderType m_110452_(Identifier texture) { return textured(Kind.ENTITY_CUTOUT, texture); }
    public static LegacyRenderType m_110470_(Identifier texture) { return textured(Kind.ENTITY_TRANSLUCENT_CULL, texture); }
    public static LegacyRenderType m_110473_(Identifier texture) { return textured(Kind.ENTITY_TRANSLUCENT, texture); }
    public static LegacyRenderType m_110475_() { return LEASH; }
    public static LegacyRenderType m_110478_() { return WATER_MASK; }
    public static LegacyRenderType m_110488_(Identifier texture) { return textured(Kind.EYES, texture); }
    public static LegacyRenderType m_110491_(Identifier texture) { return textured(Kind.OUTLINE, texture); }
    public static LegacyRenderType m_110494_(Identifier texture) { return textured(Kind.CRUMBLING, texture); }
    public static LegacyRenderType m_110497_(Identifier texture) { return textured(Kind.TEXT, texture); }
    public static LegacyRenderType m_110500_(Identifier texture) { return textured(Kind.TEXT_SEE_THROUGH, texture); }
    public static LegacyRenderType m_110502_() { return LIGHTNING; }
    public static LegacyRenderType m_110504_() { return LINES; }

    private static LegacyRenderType textured(Kind kind, Identifier texture) {
        return new LegacyRenderType(kind, Objects.requireNonNull(texture, "texture"));
    }

    /** Resolves only identities with a verified deferred-pipeline counterpart. */
    public RenderType requireTargetRenderType() {
        return switch (kind) {
            case ENTITY_SOLID -> RenderTypes.entitySolid(texture);
            case ENTITY_CUTOUT -> RenderTypes.entityCutout(texture);
            case ENTITY_TRANSLUCENT_CULL -> RenderTypes.entityTranslucentCull(texture);
            case ENTITY_TRANSLUCENT -> RenderTypes.entityTranslucent(texture);
            case EYES -> RenderTypes.eyes(texture);
            case OUTLINE -> RenderTypes.outline(texture);
            case CRUMBLING -> RenderTypes.crumbling(texture);
            case TEXT -> RenderTypes.text(texture);
            case TEXT_SEE_THROUGH -> RenderTypes.textSeeThrough(texture);
            case LEASH -> RenderTypes.leash();
            case WATER_MASK -> RenderTypes.waterMask();
            case LIGHTNING -> RenderTypes.lightning();
            case LINES -> RenderTypes.lines();
            default -> throw new UnsupportedOperationException(
                    "Forge 1.19.2 render type "
                            + (customName == null ? kind : customName)
                            + " has no standalone Minecraft 26.3 RenderType");
        };
    }

    @Override
    public String toString() {
        if (customName != null) return "LegacyRenderType[CUSTOM, " + customName + "]";
        return texture == null ? "LegacyRenderType[" + kind + "]"
                : "LegacyRenderType[" + kind + ", " + texture + "]";
    }
}
