# Source configuration UI controls, notifications and narration

## Checkbox

The source Checkbox constructors MUST preserve x/y/width/height, message,
initial selection and showLabel. A source-shaped native AbstractButton subclass
MAY represent the removed constructor contract without mutating native Checkbox
access or inventing a font dependency in source construction. Native activation
MUST invoke the original no-argument source callback virtually once. The source
base toggles its own selection; managed selected() overrides and explicit super
activation MUST retain dispatch. Source custom rendering remains in the existing
GUI extraction adapter. Default presentation MAY use native checkbox sprites,
retaining source geometry and label visibility. Source base render/super callbacks
MUST execute the base drawing rather than recursively call a source override.

## System notifications

Source SystemToast identity, constructor arguments, nullable message and reset
intent MUST remain meaningful. Source enum names, order, category identity and
5000/10000 ms display durations MUST survive even for categories the target
removed. Categories MUST NOT be aliased to an unrelated native category.

A source-shaped Toast implementation MAY capture title/message/category and
materialize a genuine native SystemToast at its first layout/update/extraction
consumption. It MUST NOT return dummy dimensions. Native layout/wrapping and
presentation are the target policy. The source token remains the source enum;
queue lookup, clear, add, addOrUpdate and reset MUST use the actual native
ToastManager and preserve identity and native toast-add cancellation. Native
extraction/update consume the delegate, not an emulated scheduler. The original
source immediate toast render callback and custom render overrides remain an
explicit fail-fast boundary. Native manager construction/rendering is not a
source constructor/immediate-render compatibility claim.

The Minecraft toast accessor MUST return that client's actual GUI-owned manager.
Unrelated descriptors or owners MUST NOT select a migration. No managed mod
identity may influence control, category, message or notification behavior.

## Narration

The source two-component joinForNarration operation MUST create a fresh mutable
component with the original component references separated by the actual native
narration separator. It MUST preserve styles/siblings and input immutability.
Native varargs joining MAY implement it because inspected source/target code
performs that exact append sequence.

All supported operations MUST cover exact verified source owners/members and
normal calls, handles, invokedynamic and nested constants. Class and descriptor
projection MUST remain consistent. Unproven operations and native/source callback
collisions MUST fail explicitly. Verification MUST include original immutable
consumer construction/state callbacks plus native queue identity/cancellation and
component mutation tests. A real client is required for visual and complete
configuration workflow acceptance.
