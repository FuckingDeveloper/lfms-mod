# Source projectile base family

Source Forge1.19.2 ThrowableItemProjectile moved and changed constructors,
item-stack defaults and synchronized-data initialization. Native flight/collision
ordering and deflection also changed. A class rename alone cannot preserve intent.

The proposed bounded adapter retains source constructors, lazy default-item
behavior, source item-stack/custom-data semantics and overrides, while using the
active target projectile simulation. Original legacy artifacts stay immutable;
selectors use exact source base/member identity and source hierarchy.

Native simulation is a semantic choice requiring user acceptance before its
implementation. Menu creation proceeds as an independent compatibility family.
