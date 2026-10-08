# Save and Persistent Data Specification

Transparent migration of worlds created by historical Minecraft versions is outside the required scope of 01.000.00.

LMS MUST NOT claim old-world compatibility merely because a legacy mod can run in a newly created target-version world.

Any future save migration MUST be specified separately and MUST address identifiers, block/entity data, capabilities/components and irreversible conversion behavior.

LMS compatibility code MUST avoid silently discarding persistent legacy data when an unsupported conversion is detected.
