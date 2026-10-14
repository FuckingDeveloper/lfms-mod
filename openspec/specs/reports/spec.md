# Compatibility Report Specification

## Requirements

LMS MUST produce a machine-readable report for managed legacy artifacts.

The report MUST be versioned as a data format and SHOULD contain:
- LMS and target runtime identity;
- source artifact identity and cryptographic hash;
- discovered metadata/evidence;
- dependency resolution result;
- selected profile and reason/evidence;
- applied transformation rules;
- applied mod-specific rules;
- capability states;
- unsupported/unresolved symbols or APIs;
- lifecycle progress/failure stage;
- cache identity/status;
- achieved compatibility level;
- fatal errors.

Reports MUST distinguish observed/verified results from inference.

Human-readable logging MAY accompany the report but MUST NOT be the only structured diagnostic output.

Sensitive local paths SHOULD be avoidable/redactable in reports intended for sharing.
