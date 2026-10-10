# Proposal: Multi-Generation Source and Target Architecture

## Why

The initial LMS implementation necessarily couples many Forge 1.19.2 compatibility rules to the Minecraft 26.3 target while the first integration target is being made executable.

LMS is intended to support additional legacy Forge generations and future Minecraft/NeoForge targets. Without an explicit architectural boundary, adding Forge 1.16.5/1.12.2 or moving from target 26.3 to a future 27.3 risks multiplying direct version-to-version translators and reference-mod special cases.

## Change

Formalize three compatibility layers:

1. legacy **Source Profiles/Profile Families**;
2. target-independent **Semantic Compatibility Operations**;
3. modern **Target Runtime Adapters**.

Add normative rules for:

- canonical planned legacy generations;
- future target-runtime upgrades;
- target-specific release/testing;
- compatibility levels and functional claims;
- regression fixtures;
- agent behavior and Git safety;
- prohibition of hidden reference-mod special cases.

## Non-goals

This change does NOT:

- implement Forge 1.16.5, 1.12.2, 1.20.1 or 1.7.10 support;
- implement a Minecraft 27.3 target;
- require an immediate large refactor of the active Forge 1.19.2 work;
- require a serialized/interpreted LMS IR immediately;
- claim compatibility for any newly documented generation.

## Success

The specification clearly defines where source-version, semantic and target-version behavior belongs, and autonomous development can continue without hard-coding the project to IC2, Forge 1.19.2 or Minecraft 26.3.
