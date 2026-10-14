package com.fuckingdeveloper.lms.runtime.capability;

/** Receiver-owned state supplied by the target base-constructor integration. */
public interface LegacyBlockCapabilityCarrier {
    LegacyBlockCapabilityDispatcher lms$sourceCapabilities();
}
