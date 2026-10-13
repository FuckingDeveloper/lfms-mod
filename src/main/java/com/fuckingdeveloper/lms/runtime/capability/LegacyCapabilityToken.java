package com.fuckingdeveloper.lms.runtime.capability;

/**
 * Structural identity for Forge 1.19.2 CapabilityToken<T>.
 *
 * <p>The legacy token is commonly instantiated as an anonymous subclass so its
 * generic signature can identify T. Current NeoForge no longer exposes this
 * class. LMS preserves only the verifier/linker-visible superclass shape here;
 * capability lookup and resolution remain separate executable migration
 * boundaries and must be adapted explicitly.</p>
 */
public abstract class LegacyCapabilityToken<T> {
    protected LegacyCapabilityToken() {
    }

    private static final ClassValue<String> TYPES = new ClassValue<>() {
        @Override protected String computeValue(Class<?> type) {
            byte[] bytes;
            try {
                String path = type.getName().replace('.', '/') + ".class";
                if (type.getClassLoader() instanceof com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader managed) {
                    bytes = managed.originalClassBytes(type.getName());
                    if (bytes == null) throw new java.io.IOException("Missing source token bytes: " + path);
                } else {
                    try (var input = type.getResourceAsStream("/" + path)) {
                        if (input == null) throw new java.io.IOException("Missing token bytes: " + path);
                        bytes = input.readAllBytes();
                    }
                }
                var node = new org.objectweb.asm.tree.ClassNode();
                new org.objectweb.asm.ClassReader(bytes).accept(node,
                        org.objectweb.asm.ClassReader.SKIP_CODE | org.objectweb.asm.ClassReader.SKIP_DEBUG | org.objectweb.asm.ClassReader.SKIP_FRAMES);
                if (node.signature == null) throw new IllegalStateException("Capability token lacks a generic type: " + type.getName());
                String[] result = {null};
                new org.objectweb.asm.signature.SignatureReader(node.signature).accept(new org.objectweb.asm.signature.SignatureVisitor(org.objectweb.asm.Opcodes.ASM9) {
                    @Override public org.objectweb.asm.signature.SignatureVisitor visitSuperclass() {
                        return new org.objectweb.asm.signature.SignatureVisitor(org.objectweb.asm.Opcodes.ASM9) {
                            @Override public org.objectweb.asm.signature.SignatureVisitor visitTypeArgument(char wildcard) {
                                if (wildcard != INSTANCEOF) throw new IllegalStateException("Wildcard capability identity is unsupported");
                                return new org.objectweb.asm.signature.SignatureVisitor(org.objectweb.asm.Opcodes.ASM9) {
                                    @Override public void visitClassType(String name) {
                                        if (result[0] != null) throw new IllegalStateException("Ambiguous capability token identity");
                                        result[0] = name;
                                    }
                                };
                            }
                        };
                    }
                });
                if (result[0] == null) throw new IllegalStateException("Unsupported capability token type: " + node.signature);
                return result[0];
            } catch (java.io.IOException failure) {
                throw new IllegalStateException("Cannot recover source capability identity", failure);
            }
        }
    };

    final String typeName() { return TYPES.get(getClass()); }
}
