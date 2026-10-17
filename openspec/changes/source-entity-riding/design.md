# Native riding with source offsets

Source one/two-argument mounting operations on verified Entity ancestry MUST use
native startRiding(vehicle,force,true), retaining native checks, mount events,
triggers and return values. Custom source startRiding overrides remain explicit
unsupported cases until source-super and skip-trigger dispatch is proven.

Source scalar offset overrides over Entity or managed source ancestry MUST retain
virtual and source-super dispatch. A vehicle offset replaces the native passenger
attachment Y; a rider offset replaces native vehicle attachment Y with its negative
because native positioning subtracts that vector. Native X/Z attachments remain.
Native-only entities MUST keep native attachment behavior. Direct scalar queries
on native-only specialized entities MUST fail rather than infer an offset without
passenger context. Exact Entity superclass scalar calls retain source defaults.

Source public positionRider callbacks MUST execute through native protected
positionRider(Entity,MoveFunction). Their exact Entity superclass operation MUST
use native attachments and the supplied movement function. A scoped, restored
movement context MAY carry that function through the source scalar-shaped callback.
Native specialized superclass attachment conflicts MUST fail explicitly. Retained
add/remove passenger, turn, eligibility and dismount callbacks use verified native
member dispatch. This policy does not reproduce the 1.19.2 riding simulation.

Source profiles own member selection and ASM aliases; the installed target adapter
owns attachment, movement and event/trigger execution. No fixture identity selects
production behavior. Immutable independent binaries and native client interaction
are separate acceptance gates.
