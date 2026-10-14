package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.UnbakedModelParser;

import java.io.Reader;
import java.util.Objects;

/** Target adapter for the Forge 1.19.2 JSON BlockModel property subset. */
public final class Forge1192UnbakedModelBridge {
    private Forge1192UnbakedModelBridge() {}

    public static Object parse(Reader reader) {
        return new ParsedModel(UnbakedModelParser.parse(reader));
    }

    public static Identifier parent(Object parsed) {
        return requireParsed(parsed).model().parent();
    }

    public static ItemTransforms transforms(Object parsed) {
        ItemTransforms transforms = requireParsed(parsed).model().transforms();
        return transforms == null ? ItemTransforms.NO_TRANSFORMS : transforms;
    }

    private static ParsedModel requireParsed(Object parsed) {
        if (parsed instanceof ParsedModel model) return model;
        throw new IllegalArgumentException("Not an LMS Forge 1.19.2 parsed block model: "
                + Objects.toString(parsed));
    }

    private record ParsedModel(UnbakedModel model) {
        private ParsedModel {
            Objects.requireNonNull(model, "parsed model");
        }
    }
}
