package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.classloading.InstrumentedClassTransformer;
import com.fuckingdeveloper.lms.transform.TransformationSpec;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** Common observation/control; native host attachment belongs to the integration. */
public interface RuntimeSession extends AutoCloseable {
    enum State { LINKED, INITIALIZED, BLOCKED, CLOSED }
    String modId();
    Path artifact();
    State state();
    Optional<String> blockReason();
    InstrumentedClassTransformer transformer();
    void initialize() throws Exception;
    void blockInitialization(String reason);
    void bindCoremodHooks(List<TransformationSpec> specs);
    @Override void close() throws Exception;
}
