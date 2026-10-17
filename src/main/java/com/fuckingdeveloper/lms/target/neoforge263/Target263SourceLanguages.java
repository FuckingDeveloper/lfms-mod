package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.language.LegacyLanguageInfo;
import java.util.*;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;

/** Native catalog/lifecycle boundary; source values are owned by each native manager. */
public final class Target263SourceLanguages {
    private Target263SourceLanguages() {}
    public interface Carrier { State lms$sourceLanguages(); }
    public static final class State {
        private record Entry(LanguageInfo nativeInfo, LegacyLanguageInfo sourceInfo) {}
        private final Map<String, Entry> catalog = new HashMap<>();
        private LegacyLanguageInfo selected;
        public synchronized void selectionChanged() { selected = null; }
        public synchronized void reloading() { selected = null; catalog.clear(); }
        private LegacyLanguageInfo wrap(String code, LanguageInfo info) {
            if (info == null) { catalog.remove(code); return null; }
            var entry = catalog.get(code);
            if (entry == null || entry.nativeInfo() != info) {
                entry = new Entry(info, new LegacyLanguageInfo(code, info.region(), info.name(), info.bidirectional()));
                catalog.put(code, entry);
            }
            return entry.sourceInfo();
        }
    }
    private static State state(LanguageManager manager) {
        Objects.requireNonNull(manager, "manager");
        if (!(manager instanceof Carrier carrier)) throw new IllegalStateException("Native language lifecycle adapter is not installed");
        return carrier.lms$sourceLanguages();
    }
    public static LegacyLanguageInfo getSelected(LanguageManager manager) {
        var state = state(manager);
        synchronized (state) {
            if (state.selected != null) return state.selected;
            var code = manager.getSelected();
            var info = state.wrap(code, manager.getLanguage(code));
            if (info != null) return info;
            var fallback = state.wrap("en_us", manager.getLanguage("en_us"));
            return fallback == null ? LegacyLanguageInfo.DEFAULT : fallback;
        }
    }
    public static LegacyLanguageInfo getLanguage(LanguageManager manager, String code) {
        var state = state(manager);
        synchronized (state) { return state.wrap(code, manager.getLanguage(code)); }
    }
    public static SortedSet<LegacyLanguageInfo> getLanguages(LanguageManager manager) {
        var state = state(manager);
        synchronized (state) {
            var nativeCatalog = manager.getLanguages();
            state.catalog.keySet().retainAll(nativeCatalog.keySet());
            var result = new TreeSet<LegacyLanguageInfo>();
            nativeCatalog.forEach((code, info) -> result.add(state.wrap(code, info)));
            return result;
        }
    }
    public static void setSelected(LanguageManager manager, LegacyLanguageInfo info) {
        Objects.requireNonNull(info, "language");
        var state = state(manager);
        manager.setSelected(info.getCode());
        synchronized (state) { state.selected = info; }
    }
}
