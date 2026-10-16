# Repeated damage singleton comparisons

Runtime58 fails when a spider attacks a wearer of source hazmat armor:
`DamageSource.f_19310_` remains unresolved. The original consumer contains four
adjacent singleton comparisons. The existing, specified source-singleton to native
DamageType-key predicate rule correctly adapts the first, then traverses the old
jump node detached by ASM InsnList.set, ending the scan before the siblings.

Resume traversal at the attached replacement successor. Preserve the exact
source owner/field/descriptor mapping and unique target key evidence; do not
change arbitrary object comparisons, custom damage identities or native tags.
This repairs implementation of the existing compatibility-architecture contract;
no new damage policy or fixture-specific production behavior is introduced.

Regression checks cover repeated equal/not-equal branches with debug nodes, all
four original keys, distinct native source instances backed by the same keys,
an unrelated type and the immutable original armor method. Runtime acceptance
must recheck equipped armor damage and the interrupted charged-copy test.

Verification: wrapper compatibilityTest, nativeCompatibilitySmoke and
compileLaunchJava passed together in batch-check-12. Runtime59 loads the source
world and the original equipped hazmat callbacks process multiple mob hits and
an explosion without the previous linkage failure. These are callback/runtime
observations, not claims about every armor absorption policy.

The crash saved the player in survival. During Quick Play startup, before MCP
restored creative, natural hostile mobs damaged armor and an explosion changed
nearby terrain. All four known pristine armor values are restored in both native
and source damage data. Test inventory is restored exactly against the runtime58
structured baseline, including newer rotten flesh/lead. The original backed-up
poppy was absent from the barrel after the crash and was recreated as one plain
poppy. The one observed original grass block under the baseline position is
restored; the rest of the crater has no prior exact snapshot and is NOT claimed
restored. No death occurred in this probe. Future restarts must not leave the
player in survival during startup; timed mining probes restore creative in a
finally block and do not wait for manual follow-up.
