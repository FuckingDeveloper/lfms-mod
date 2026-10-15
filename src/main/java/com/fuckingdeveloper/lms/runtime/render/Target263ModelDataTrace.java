package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.model.data.ModelData;
import org.slf4j.LoggerFactory;

/** Opt-in, position-scoped runtime evidence; never queries a live entity from a worker. */
public final class Target263ModelDataTrace {
    private static final String POSITION = System.getenv("LMS_TRACE_MODEL_POS");
    private Target263ModelDataTrace() {}

    public static void log(BlockPos pos, String stage, Object data) {
        if (POSITION == null || !POSITION.equals(pos.getX() + "," + pos.getY() + "," + pos.getZ())) return;
        Object value = data;
        if (data instanceof ModelData model) {
            value = model.getProperties().stream().map(property -> {
                Object entry = model.get(property);
                return property.getClass().getName() + "=" +
                        (entry instanceof Number || entry instanceof Boolean || entry instanceof String
                                ? entry : entry == null ? "null" : entry.getClass().getName());
            }).toList();
        }
        LoggerFactory.getLogger(Target263ModelDataTrace.class).info(
                "LMS model-data trace pos={} stage={} data={}", pos, stage, value);
    }
}
