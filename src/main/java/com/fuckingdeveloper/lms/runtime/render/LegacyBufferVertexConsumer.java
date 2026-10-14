package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Source-shaped bridge for the removed 1.19.2 BufferVertexConsumer contract.
 * Legacy implementations retain their exact low-level SRG write operations;
 * the current VertexConsumer surface is reconstructed from those operations.
 * Newly introduced vertex channels fail closed until a source semantic exists.
 */
public interface LegacyBufferVertexConsumer extends VertexConsumer {
    void m_5751_();

    void m_5672_(int offset, byte value);

    void m_5586_(int offset, short value);

    void m_5832_(int offset, float value);

    @Override
    default VertexConsumer addVertex(float x, float y, float z) {
        m_5832_(0, x);
        m_5832_(4, y);
        m_5832_(8, z);
        m_5751_();
        return this;
    }

    @Override
    default VertexConsumer setColor(int red, int green, int blue, int alpha) {
        m_5672_(0, (byte) red);
        m_5672_(1, (byte) green);
        m_5672_(2, (byte) blue);
        m_5672_(3, (byte) alpha);
        m_5751_();
        return this;
    }

    @Override
    default VertexConsumer setColor(int color) {
        return setColor(color >> 16 & 255, color >> 8 & 255, color & 255, color >>> 24);
    }

    @Override
    default VertexConsumer setUv(float u, float v) {
        m_5832_(0, u);
        m_5832_(4, v);
        m_5751_();
        return this;
    }

    @Override
    default VertexConsumer setUv1(int u, int v) {
        return writeShortUv(u, v);
    }

    @Override
    default VertexConsumer setUv2(int u, int v) {
        return writeShortUv(u, v);
    }

    private VertexConsumer writeShortUv(int u, int v) {
        m_5586_(0, (short) u);
        m_5586_(2, (short) v);
        m_5751_();
        return this;
    }

    @Override
    default VertexConsumer setNormal(float x, float y, float z) {
        m_5672_(0, normalByte(x));
        m_5672_(1, normalByte(y));
        m_5672_(2, normalByte(z));
        m_5751_();
        return this;
    }

    @Override
    default VertexConsumer setUv3(float u, float v) {
        throw unsupported("UV3");
    }

    @Override
    default VertexConsumer setLineWidth(float width) {
        throw unsupported("line width");
    }

    private static byte normalByte(float value) {
        return (byte) ((int) (Math.max(-1.0F, Math.min(1.0F, value)) * 127.0F) & 255);
    }

    private static UnsupportedOperationException unsupported(String channel) {
        return new UnsupportedOperationException(
                "LMS legacy BufferVertexConsumer cannot represent target " + channel + " data");
    }
}
