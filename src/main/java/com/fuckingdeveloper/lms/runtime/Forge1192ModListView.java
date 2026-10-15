package com.fuckingdeveloper.lms.runtime;

import java.util.*;
import java.util.function.Consumer;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.*;

/** Source loader queries over native membership and admitted managed sessions. */
public final class Forge1192ModListView {
    private static final Forge1192ModListView INSTANCE=new Forge1192ModListView();
    private static final Map<String,Forge1192ModContainerView> MANAGED=new LinkedHashMap<>();
    private static final Map<ModContainer,Forge1192ModContainerView> NATIVE=new IdentityHashMap<>();
    private Forge1192ModListView(){}
    public static Forge1192ModListView get(){return INSTANCE;}
    public static synchronized Map<java.nio.file.Path,com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader> sharedDependencies(List<java.nio.file.Path> artifacts) {
        var result=new LinkedHashMap<java.nio.file.Path,com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader>();
        for(var path:artifacts)for(var view:MANAGED.values())if(view.session().state()==Forge1192RuntimeSession.State.INITIALIZED
                &&view.session().managedLoader().providesArtifact(path)) {
            result.put(path.toAbsolutePath().normalize(),view.session().managedLoader());break;
        }
        return Map.copyOf(result);
    }
    public static synchronized void register(Forge1192RuntimeSession session) {
        if(MANAGED.containsKey(session.modId()))throw new IllegalStateException("Duplicate managed source container: "+session.modId());
        MANAGED.put(session.modId(),Forge1192ModContainerView.managed(session));
    }
    public static synchronized void unregister(Forge1192RuntimeSession session) {
        var view=MANAGED.get(session.modId());
        if(view!=null&&view.session()==session){MANAGED.remove(session.modId());view.clear();if(MANAGED.isEmpty())NATIVE.clear();}
    }
    public static synchronized Forge1192ModContainerView sourceOwner(String id) {
        var view=MANAGED.get(id);
        if(view==null)throw new IllegalStateException("No managed source container: "+id);
        return view;
    }
    public static synchronized Forge1192ModContainerView wrap(ModContainer container) {
        return NATIVE.computeIfAbsent(container,Forge1192ModContainerView::nativeView);
    }
    private static boolean visible(Forge1192ModContainerView view) {
        return view.session().state()==Forge1192RuntimeSession.State.INITIALIZED
                || view.session().state()==Forge1192RuntimeSession.State.LINKED
                &&Forge1192LifecycleBridge.currentContext().map(c->c.modId().equals(view.getModId())).orElse(false);
    }
    public Optional<Forge1192ModContainerView> getModContainerById(String id) {
        synchronized(Forge1192ModListView.class){var managed=MANAGED.get(id);if(managed!=null)return visible(managed)?Optional.of(managed):Optional.empty();}
        return ModList.get().getModContainerById(id).map(Forge1192ModListView::wrap);
    }
    public boolean isLoaded(String id){return getModContainerById(id).isPresent();}
    public List<Forge1192ModContainerView> containers() {
        var result=new ArrayList<Forge1192ModContainerView>();
        ModList.get().forEachModInOrder(c->result.add(wrap(c)));
        List<Forge1192ModContainerView> managed;
        synchronized(Forge1192ModListView.class){managed=MANAGED.values().stream().filter(Forge1192ModListView::visible).toList();}
        return merge(result,managed);
    }
    public static List<Forge1192ModContainerView> merge(List<Forge1192ModContainerView> nativeViews,List<Forge1192ModContainerView> managed) {
        var result=new ArrayList<>(nativeViews);
        Set<String> ids=new HashSet<>();result.forEach(v->ids.add(v.getModId()));
        for(var view:ordered(managed)){
            if(!ids.add(view.getModId()))throw new IllegalStateException("Source/native mod identity collision: "+view.getModId());
            result.add(view);
        }
        return List.copyOf(result);
    }
    /** Stable topological order of present mandatory source dependencies. */
    public static List<Forge1192ModContainerView> ordered(List<Forge1192ModContainerView> input) {
        var remaining=new LinkedHashMap<String,Forge1192ModContainerView>();
        for(var view:input)if(remaining.putIfAbsent(view.getModId(),view)!=null)throw new IllegalStateException("Duplicate source identity");
        var result=new ArrayList<Forge1192ModContainerView>();
        while(!remaining.isEmpty()) {
            boolean progressed=false;
            for(var iterator=remaining.entrySet().iterator();iterator.hasNext();) {
                var view=iterator.next().getValue();
                if(view.nativeOwner().isEmpty()&&view.getModInfo().getDependencies().stream().anyMatch(d->d.getType()==IModInfo.DependencyType.REQUIRED&&remaining.containsKey(d.getModId())))continue;
                result.add(view);iterator.remove();progressed=true;
            }
            if(!progressed)throw new IllegalStateException("Cyclic source dependencies: "+remaining.keySet());
        }
        return List.copyOf(result);
    }
    public void forEachModInOrder(Consumer<Forge1192ModContainerView> consumer){containers().forEach(consumer);}
    public List<IModInfo> getMods(){return containers().stream().map(Forge1192ModContainerView::getModInfo).toList();}
    public IModFileInfo getModFileById(String id){return getModContainerById(id).map(c->c.getModInfo().getOwningFile()).orElse(null);}
    public List<ModFileScanData> getAllScanData(){return ModList.get().getAllScanData();}
}
