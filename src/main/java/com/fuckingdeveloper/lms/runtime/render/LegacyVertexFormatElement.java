package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.vertex.VertexFormatElement;

/** Source-shaped metadata view of a Forge 1.19.2 vertex element. */
public final class LegacyVertexFormatElement {
    public enum Usage { POSITION, NORMAL, COLOR, UV, PADDING, GENERIC }

    public enum Type {
        FLOAT(4), UBYTE(1), BYTE(1), USHORT(2), SHORT(2), UINT(4), INT(4);

        private final int size;

        Type(int size) { this.size = size; }

        public int getSize() { return size; }
    }

    private final Type type;
    private final Usage usage;
    private final int index;
    private final int count;
    private final int byteSize;

    public LegacyVertexFormatElement(int index, Type type, Usage usage, int count) {
        this(type, usage, index, count, type.size * count);
    }

    private LegacyVertexFormatElement(Type type, Usage usage, int index, int count, int byteSize) {
        this.type = type;
        this.usage = usage;
        this.index = index;
        this.count = count;
        this.byteSize = byteSize;
    }

    static LegacyVertexFormatElement wrap(VertexFormatElement element) {
        String name = element.name();
        Usage usage = switch (name) {
            case "Position" -> Usage.POSITION;
            case "Color" -> Usage.COLOR;
            case "Normal" -> Usage.NORMAL;
            default -> name.startsWith("UV") ? Usage.UV : Usage.GENERIC;
        };
        int index = name.startsWith("UV") && name.length() > 2
                ? Character.digit(name.charAt(2), 10) : 0;
        GpuFormat format = element.format();
        Type type = legacyType(format);
        return new LegacyVertexFormatElement(
                type, usage, index, format.componentCount(), format.blockSize());
    }

    private static Type legacyType(GpuFormat format) {
        String name = format.name();
        if (name.endsWith("_FLOAT")) return Type.FLOAT;
        if (name.contains("16_")) return name.endsWith("_SINT") ? Type.SHORT : Type.USHORT;
        if (name.contains("32_")) return name.endsWith("_SINT") ? Type.INT : Type.UINT;
        return name.endsWith("_SINT") || name.endsWith("_SNORM") ? Type.BYTE : Type.UBYTE;
    }

    public Type getType() { return type; }
    public Usage getUsage() { return usage; }
    public Usage m_86048_() { return getUsage(); }
    public int getCount() { return count; }
    public int getIndex() { return index; }
    public int getByteSize() { return byteSize; }
    public int m_86050_() { return getByteSize(); }
}
