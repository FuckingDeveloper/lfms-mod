# Complete source container render callbacks

Pinned source: Forge 1.19.2; target: Minecraft26.3/NeoForge26.3.0.58-beta.
Original container render calls its panel background, native controls/slots,
labels and carried item; it does not automatically call renderTooltip. Managed
overrides can run logic before/after super and explicitly gather component
hover text. Merely adapting renderBg/renderLabels never enters that logic.
The original integration screen gathers energy bar text in precisely this path.

A verified managed AbstractContainerScreen render(PoseStack,int,int,float)
override MUST enter once from target extractRenderState, preserving virtual
inheritance and explicit managed super dispatch. Native full-screen backdrop
remains in the target background phase; source panel background MUST execute at
the source base-render point, after source pre-render logic. Native contents,
labels, slots, foreground event and carried item MUST run once at that point.
Source explicit base renderTooltip MUST run genuine native tooltip extraction
once at its call site. Only its newly scheduled native tooltip closure may be
executed immediately, with the prior deferred closure restored on every exit;
unrelated deferred hover/IME/cursor work MUST NOT be flushed.

The verified Screen formatted-sequence renderTooltip maps each existing line
to a native tooltip component without repeating line gathering/wrapping.
Verified Screen.renderComponentTooltip uses actual screen font, native component
gathering and immediate tooltip extraction/events with empty item context, as
in the source default. Source extraction scope MUST be suspended while native
tooltip components execute and restored on success/failure. No callback, player,
world or managed loader may be retained globally. Native-only screens and source
screens without a full-render override retain the previous target lifecycle.
Unproven source-super owners/callable forms remain explicit boundaries; no mod
identity selects any rule. This contract does not claim arbitrary immediate GPU
rendering or source drag animation equivalence.
