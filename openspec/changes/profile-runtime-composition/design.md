# Composition boundaries

## Contract

See [profile composition](../../specs/profile-composition/spec.md) for normative
selection, ownership, pass-order and staged-registration requirements.

## Migration scope

Source ABI, callbacks, packet/thread/render timing, namespace mappings and
functional limits remain unchanged. Session constructors used by focused tests
remain available. Native UI observes the common session state without treating
managed sessions as native FML containers.

The initial pass pipeline composes existing implementations; family helpers
still owned by the large transformer can be extracted later without changing
its ordering contract. Only evidence-backed contracts are extracted now. Some
source integrations still use native APIs; no target-neutrality claim follows
from putting an interface around them. Target build selection stays Gradle-pinned.
