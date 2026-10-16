package com.fuckingdeveloper.lms.runtime.blockdata;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import org.objectweb.asm.Type;

/** Bounded callback ABI; semantic equivalence and native bridge implementation need separate proof. */
public record BlockDataTargetLayout(String targetId, String baseOwner, Map<Operation, Callback> callbacks) {
    public enum Operation { LOAD, SAVE, UPDATE_TAG, HANDLE_UPDATE_TAG }

    public record Callback(String name, String argumentDescriptor, String returnDescriptor) {
        public Callback {
            if (name == null || !name.matches("[A-Za-z_$][A-Za-z0-9_$]*"))
                throw new IllegalArgumentException("Invalid callback name: " + name);
            requireReference(argumentDescriptor);
            if (!"V".equals(returnDescriptor)) requireReference(returnDescriptor);
        }
        public String descriptor() { return "(" + argumentDescriptor + ")" + returnDescriptor; }
    }

    public BlockDataTargetLayout {
        if (targetId == null || targetId.isBlank()) throw new IllegalArgumentException("Empty target identity");
        if (baseOwner == null || !baseOwner.matches("[A-Za-z_$][A-Za-z0-9_$]*(/[A-Za-z_$][A-Za-z0-9_$]*)*"))
            throw new IllegalArgumentException("Invalid target base owner: " + baseOwner);
        var copy = new EnumMap<Operation,Callback>(Operation.class);
        copy.putAll(Objects.requireNonNull(callbacks));
        var methods = new java.util.HashSet<String>();
        for (var operation : Operation.values()) {
            var callback = copy.get(operation);
            if (callback == null) throw new IllegalArgumentException("Missing callback: " + operation);
            if (!methods.add(callback.name() + callback.descriptor()))
                throw new IllegalArgumentException("Duplicate native callback: " + callback.name());
            boolean returnsTag = operation == Operation.UPDATE_TAG;
            if (returnsTag == callback.returnDescriptor().equals("V"))
                throw new IllegalArgumentException("Unsupported callback result shape: " + operation);
        }
        callbacks = Map.copyOf(copy);
    }

    public Callback callback(Operation operation) { return callbacks.get(Objects.requireNonNull(operation)); }

    private static void requireReference(String descriptor) {
        if (descriptor == null || !descriptor.matches("L[A-Za-z_$][A-Za-z0-9_$]*(/[A-Za-z_$][A-Za-z0-9_$]*)*;"))
            throw new IllegalArgumentException("Expected one reference descriptor: " + descriptor);
        if (Type.getType(descriptor).getSort() != Type.OBJECT)
            throw new IllegalArgumentException("Unsupported callback reference: " + descriptor);
    }
}
