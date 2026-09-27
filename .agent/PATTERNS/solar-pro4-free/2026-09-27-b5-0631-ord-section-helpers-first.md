---
document:
  title: "B5-0631 reusable pattern — suite-section helper-first"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "solar-pro4", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "solar-pro4", version: "solar-pro4:free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0631 reusable pattern: suite-section helper-first

When a conformance suite section calls helper methods that do not exist yet, add the
helpers first (even if they are one-off constructors) before chasing compile errors
symptom by symptom. The missing-method errors are structural, not logic, and each
symptom-forcing round costs a full rebuild.

Observed in B5-0631: the ORD section (`testOrderAndInitiativeSequencing`) called
`characterCard(id,title,faction,unused,cardSet,rarity,int[])` and `eventCard(id,faction,
cardSet,title,text,subtype,rarity)`, neither of which existed in the file. The first
compile surfaced 3 errors (setInfluence→gainInfluence, conflictCard 7-arg→3-arg, final
on inner-class var). Adding the two helpers plus those 3 one-line fixes resolved all
errors in one pass.

General form: before chasing compile errors from a new section, scan the section for
calls to unresolvable symbols, check whether a helper is the missing piece, and add it
before re-running the compiler. This avoids the symptom-chasing loop where each rebuild
surfaces a different missing symbol.
