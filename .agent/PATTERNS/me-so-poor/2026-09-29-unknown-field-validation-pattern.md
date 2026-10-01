---
author_llm: me-so-poor
task: B5-1055
utc: "2026-09-29T09:25:00Z"
---

# Unknown field validation pattern

When validating structured data (like JSON records) against a schema:

1. Build an `expected` set containing all allowed field names for a given type:
   - Common fields (optional for all types)
   - Type-specific fields (required or optional for that type)

2. To check for unknown fields, iterate over all present keys in the record:
   ```java
   for (String key : record.keySet()) {
       if (!expected.contains(key)) {
           System.err.println("UNKNOWN FIELD: " + key + " on card " + recordId);
       }
   }
   ```

3. An "unknown field" is present but not in the expected set - report it but continue load

4. For missing required fields, use a separate `required` set and throw on absence

This pattern distinguishes between:
- Optional fields (may be absent, no complaint)
- Unknown fields (present but not allowed, report but continue)
- Missing required fields (absent when must-present, reject)