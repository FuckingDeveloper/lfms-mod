# Fluid stack display queries

Forge43.4.0 FluidStack.getDisplayName and getTranslationKey MUST retain their
source stack receiver and invoke the active fluid type's description and
stack-sensitive description-ID callbacks. NeoForge26.3 getHoverName and
getDescriptionId have these verified bodies; the target adapter MAY consume them.
The exact two member identities, descriptors and virtual invocation shape MUST
select direct/callable/nested rewrites; unrelated owners MUST remain untouched.
The returned component and string MUST remain the callback results, without
translation snapshots, fallback names or dummy values. Empty/zero-amount receiver
fluid selection MUST follow the verified source/native empty behavior.

Source FluidStack subclass execution is not established: target FluidStack is
final. Source subclasses MUST fail explicitly. Raw fluid NBT, codecs, inventory
transfer, custom rendering and source getRawFluid are independent boundaries.
Evidence: exact Forge43.4.0 FluidStack bodies, NeoForge26.3 FluidStack and
FluidInstance bodies; source FluidType callback descriptors survive namespace
projection. No mod/fixture identity selects this adaptation.
