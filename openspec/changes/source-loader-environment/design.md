# Read-only active launch environment

Pinned Forge43.4.0 FMLLoader exposes static launch-mode, physical-side and game-path
queries. FML12.0.9 stores these in the active loader instance. Exact source static
owner/member/descriptor rules MUST query the actual configured target loader,
without inferring mode from IDE arguments, substituting a directory or fabricating
side values. Direct and callable-constant forms MUST retain their source shape;
source Dist is projected through the existing proven namespace contract. Missing
active target loader MUST fail explicitly. No native membership, loading-mod-list,
source naming-service, old module-layer or source version-info compatibility is
implied. No legacy classes may be initialized to obtain launch information.
