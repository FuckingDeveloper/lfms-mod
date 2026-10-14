# Loader-owned milk opt-in intent

Source ForgeMod.enableMilkFluid during construction MUST forward the opt-in to
the actual native NeoForgeMod.enableMilkFluid before its fluid registration phase.
Source MILK get resolves the actual native deferred holder after registration.
No fluid instance, global registry entry or successful loaded capability may be
fabricated. A late unmaterialized opt-in after the source owner's tracked fluid
phase MUST fail explicitly; repeating an already materialized opt-in is harmless.
Existing unrelated built-in handles retain their contracts. This is a Forge/NeoForge
loader-generation operation, not a reference-mod identity rule.
