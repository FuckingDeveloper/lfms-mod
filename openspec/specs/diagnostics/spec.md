# Diagnostics Specification

## Requirements

LMS diagnostics MUST be able to report:
- LMS version and target runtime (including target loader and exact build);
- discovered legacy artifacts;
- selected profile or reason no profile was selected;
- applied transformation rules;
- unsupported APIs or integrations detected;
- compatibility level and evidence used to assign it;
- fatal compatibility failures.

Diagnostics MUST distinguish verified behavior from inferred or untested behavior.
