package com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

/** Source declaration storage. Native server-list rendering no longer consumes DisplayTest. */
public interface LegacyExtensionPoint<T extends Record> extends net.neoforged.fml.IExtensionPoint {
    record DisplayTest(Supplier<String> suppliedVersion, BiPredicate<String,Boolean> remoteVersionTest)
            implements LegacyExtensionPoint<DisplayTest> {
        public static final String IGNORESERVERONLY = "OHNOES😱😱😱😱😱😱😱😱😱😱😱😱😱😱😱😱😱";
        public static final Supplier<DisplayTest> IGNORE_SERVER_VERSION = () -> new DisplayTest(IGNORESERVERONLY, (remote,server) -> true);
        public static final Supplier<DisplayTest> IGNORE_ALL_VERSION = () -> new DisplayTest("", (remote,server) -> true);
        public DisplayTest(String version,BiPredicate<String,Boolean> test){this(() -> version,test);}
    }
}
