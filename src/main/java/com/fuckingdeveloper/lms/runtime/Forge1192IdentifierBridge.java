package com.fuckingdeveloper.lms.runtime;

import net.minecraft.resources.Identifier;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/** Forge 1.19.2 ResourceLocation construction compatibility. */
public final class Forge1192IdentifierBridge {
    private static final Method TWO_PART_FACTORY = resolveTwoPartFactory();

    private Forge1192IdentifierBridge() {}

    public static Identifier create(String namespace, String path) {
        try {
            return (Identifier) TWO_PART_FACTORY.invoke(null, namespace, path);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Target Identifier factory invocation failed", ex);
        }
    }

    private static Method resolveTwoPartFactory() {
        var candidates = Arrays.stream(Identifier.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.getReturnType() == Identifier.class)
                .filter(method -> Arrays.equals(method.getParameterTypes(),
                        new Class<?>[]{String.class, String.class}))
                .toList();
        if (candidates.size() != 1) {
            throw new IllegalStateException("Expected exactly one public Identifier(String,String) factory, found "
                    + candidates.stream().map(Method::toString).toList());
        }
        return candidates.getFirst();
    }
}
