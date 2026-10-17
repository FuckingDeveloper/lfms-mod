# Texture-bearing source render factories

Runtime92 passes source screen setup, then source NukeModel construction fails on
RenderType.entityCutoutNoCull. Adapt the proven cutout/outline factory family,
retaining semantic identity until target submission. No fixture selectors or JAR
changes are required. Renderer construction remains distinct from actual drawing.
