# Source riding operations over native passenger attachments

## Evidence and first blocker

Runtime118 normal floor-chair interaction passed the repaired static spawning
hook, then failed on Player.m_7998_(Entity,boolean):boolean, the source inherited
Entity.startRiding overload. Source Forge43.4.0 always posts its mount event and
uses the explicit force operand. Native26.3 uses startRiding(Entity,boolean,
boolean sendEventAndTriggers), retains native event/trigger processing and adds
eligibility rules (including vehicle serialization).

Source Entity also exposes scalar getPassengersRidingOffset/getMyRidingOffset.
The target positions passengers using getPassengerAttachmentPoint(Entity,
EntityDimensions,float), getVehicleAttachmentPoint(Entity) and native attachment
vectors. The source fixtures override a scalar offset, so simply linking the
start call would not prove positional compatibility.

## Proposed native policy (approved by user)

Use native26.3 eligibility, mounting/dismounting events, triggers and attachment
positioning. Source calls without a skip-events parameter MUST request events
and triggers. Preserve explicit force, return/failure, source callbacks and
verified source scalar offset intent through target attachment adapters. Source
one/two-argument calls, inherited receivers and callable forms must be considered
together. Unknown custom dispatch or parameter-dependent native/source conflicts
remain fail-fast; no forced vehicle acceptance, fake source mount event, source
JAR patch or fixture selector is permitted.

Alternative: reproduce the entire1.19.2 mount eligibility, event ordering and
positioning algorithm, including behavior absent from the new engine. This is a
broader source simulation layer and is not currently implemented.

## Verification

Immutable independent source direct/handle/override/super fixtures, actual native
virtual dispatch and event operands, scope/failure cleanup, then real source
chair creation, native packet/factory tracking, mount/dismount and cleanup.
A source constructor or successful packet allocation alone is insufficient.
