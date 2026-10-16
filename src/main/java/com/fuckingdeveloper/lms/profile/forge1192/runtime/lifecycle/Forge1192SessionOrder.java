package com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle;

import com.fuckingdeveloper.lms.discovery.LegacyModDescriptor;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192Profile;
import java.util.*;
import net.neoforged.neoforgespi.language.IModInfo;

/** Stable source admission order; dependency ownership cannot depend on JAR filenames. */
public final class Forge1192SessionOrder {
    private Forge1192SessionOrder(){}
    public static List<LegacyModDescriptor> order(List<LegacyModDescriptor> input,Forge1192Profile profile) {
        var indexed=new LinkedHashMap<String,LegacyModDescriptor>();
        var predecessors=new LinkedHashMap<String,Set<String>>();
        for(var mod:input)if(profile.supports(mod)) {
            if(indexed.putIfAbsent(mod.modId(),mod)!=null)throw new IllegalStateException("Duplicate source mod identity: "+mod.modId());
            predecessors.put(mod.modId(),new LinkedHashSet<>());
        }
        for(var mod:indexed.values())for(var dependency:Forge1192MetadataView.read(mod.file(),mod.modId()).getDependencies()) {
            if(!indexed.containsKey(dependency.getModId())||!dependency.getSide().isCorrectSide())continue;
            if(dependency.getType()==IModInfo.DependencyType.REQUIRED||dependency.getOrdering()==IModInfo.Ordering.AFTER)
                predecessors.get(mod.modId()).add(dependency.getModId());
            if(dependency.getOrdering()==IModInfo.Ordering.BEFORE)
                predecessors.get(dependency.getModId()).add(mod.modId());
        }
        var result=new ArrayList<LegacyModDescriptor>();var emitted=new HashSet<String>();
        while(emitted.size()<indexed.size()) {
            boolean progressed=false;
            for(var mod:indexed.values())if(!emitted.contains(mod.modId())&&emitted.containsAll(predecessors.get(mod.modId()))) {
                result.add(mod);emitted.add(mod.modId());progressed=true;
            }
            if(!progressed)throw new IllegalStateException("Conflicting/cyclic source initialization order: "+predecessors);
        }
        for(var mod:input)if(!profile.supports(mod))result.add(mod);
        return List.copyOf(result);
    }
}
