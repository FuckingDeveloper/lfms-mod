package com.fuckingdeveloper.lms.transform;

import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps legacy JVM argument slots to a migrated current method descriptor.
 *
 * Conservative: only argument slots are remapped. Non-argument locals are
 * intentionally left unresolved because compiler slot reuse requires bytecode
 * data-flow/local-variable evidence.
 */
public final class LocalSlotRemapper {
    public record Slot(int index, Type type) {}
    public record Result(boolean resolved, Integer currentSlot, String reason) {}

    private LocalSlotRemapper() {}

    public static Result remapArgumentSlotUnknownAccess(String legacyDescriptor, String currentDescriptor,
                                                        int legacySlot) {
        Result instance = remapArgumentSlot(legacyDescriptor, currentDescriptor, false, legacySlot);
        Result statik = remapArgumentSlot(legacyDescriptor, currentDescriptor, true, legacySlot);
        if (instance.resolved() && !statik.resolved()) return instance;
        if (!instance.resolved() && statik.resolved()) return statik;
        if (instance.resolved() && statik.resolved()
                && java.util.Objects.equals(instance.currentSlot(), statik.currentSlot())) {
            return new Result(true, instance.currentSlot(), "Static/instance access is immaterial for this slot mapping");
        }
        return new Result(false, null,
                "Target access is unknown and static/instance slot mappings are ambiguous");
    }

    public static Result remapArgumentSlot(String legacyDescriptor, String currentDescriptor,
                                           boolean isStatic, int legacySlot) {
        List<Slot> legacy = argumentSlots(legacyDescriptor, isStatic);
        List<Slot> current = argumentSlots(currentDescriptor, isStatic);
        Slot source = legacy.stream().filter(slot -> occupies(slot, legacySlot)).findFirst().orElse(null);
        if (source == null) {
            return new Result(false, null,
                    "Legacy slot " + legacySlot + " is not an argument slot; data-flow evidence is required");
        }

        int sourceOrdinal = legacy.indexOf(source);
        int[] mapping = align(legacy, current);
        if (mapping == null || mapping[sourceOrdinal] < 0) {
            return new Result(false, null,
                    "Legacy argument slot " + legacySlot + " cannot be uniquely aligned to current descriptor");
        }

        Slot target = current.get(mapping[sourceOrdinal]);
        int intraSlot = legacySlot - source.index();
        if (intraSlot >= target.type().getSize()) {
            return new Result(false, null, "Argument slot width changed");
        }
        return new Result(true, target.index() + intraSlot,
                "Mapped legacy argument " + source.type().getDescriptor()
                        + " from slot " + legacySlot + " to " + (target.index() + intraSlot));
    }

    private static List<Slot> argumentSlots(String descriptor, boolean isStatic) {
        List<Slot> result = new ArrayList<>();
        int slot = isStatic ? 0 : 1;
        for (Type type : Type.getArgumentTypes(descriptor)) {
            result.add(new Slot(slot, type));
            slot += type.getSize();
        }
        return List.copyOf(result);
    }

    private static boolean occupies(Slot slot, int index) {
        return index >= slot.index() && index < slot.index() + slot.type().getSize();
    }

    /**
     * Unique order-preserving type alignment. This deliberately accepts inserted
     * current arguments (e.g. ServerLevel before legacy DamageSource,float), but
     * rejects ambiguous repeated-type alignments.
     */
    private static int[] align(List<Slot> legacy, List<Slot> current) {
        List<int[]> matches = new ArrayList<>();
        search(legacy, current, 0, 0, new int[legacy.size()], matches);
        return matches.size() == 1 ? matches.getFirst() : null;
    }

    private static void search(List<Slot> legacy, List<Slot> current, int li, int ci,
                               int[] mapping, List<int[]> matches) {
        if (matches.size() > 1) return;
        if (li == legacy.size()) {
            matches.add(mapping.clone());
            return;
        }
        for (int i = ci; i < current.size(); i++) {
            if (!legacy.get(li).type().equals(current.get(i).type())) continue;
            mapping[li] = i;
            search(legacy, current, li + 1, i + 1, mapping, matches);
        }
    }
}
