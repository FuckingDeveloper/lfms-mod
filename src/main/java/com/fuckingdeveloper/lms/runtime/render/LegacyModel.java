package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/** Source-shaped base for direct Minecraft 1.19.2 {@code Model} subclasses. */
public abstract class LegacyModel extends Model<Object> {
    protected final Function<Identifier, LegacyRenderType> f_103106_;

    protected LegacyModel(Function<Identifier, LegacyRenderType> renderType) {
        super(new ModelPart(List.of(), Map.of()),
                identifier -> renderType.apply(identifier).requireTargetRenderType());
        this.f_103106_ = Objects.requireNonNull(renderType, "renderType");
    }

    public final LegacyRenderType m_103119_(Identifier texture) {
        return f_103106_.apply(texture);
    }

    public abstract void m_7695_(PoseStack poseStack, VertexConsumer consumer,
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha);
}
