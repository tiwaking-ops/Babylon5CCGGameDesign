---
document:
  title: "Project guidelines"
  status: "Governance"
provenance:
  author_llm: {name: "unknown", version: "unknown"}
  assessor_llm:
    - {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "unknown"
  last_modified_date: "2026-09-21"
---

**Project documentation system — provenance required (formal)**
All LLM-produced documentation (proposals, reports, investigations, stored artifacts, any `.md` file an LLM creates) MUST carry at its top:
`author_llm: <name> (<version>)`
The author line records the original creator and is never overwritten or removed. Any file for which no record of authorship can be found SHALL be labelled `author_llm: unknown` — never infer authorship from style, date, filename, or content.
Any LLM that later assesses, reviews, edits, migrates, or otherwise changes the document MUST append (never replace) an assessor entry:
`assessor_llm: <name> (<version>) — <date> — <assess|edit|migrate: brief note>`
Multiple assessors append multiple `assessor_llm:` lines in order, preserving full history. An LLM MUST never list itself as both author and assessor of the same document in the same revision; if author edits own doc, add a dated assessor line for the change. Human edits do not require an LLM field but must not remove LLM provenance.

**Build rule — Java 6 only, no external libraries by default**
* `b5ccg/` builds with `javac -source 6 -target 6`, no external jars. `b5ccg/src-java8-archive/` is the frozen Java 8 original — never edit it.
* All further development in `b5ccg/src/` MUST be Java 6 compatible (no lambdas, method refs, streams, `computeIfAbsent`, `@FunctionalInterface`, try-with-resources, diamond-dependent APIs beyond 6).
* Adding any external library requires explicit human approval first. Propose the library, license, and why stdlib cannot suffice; do not vendor it until approved.

**Add your own guidelines here**
<!--

System Guidelines

Use this file to provide the AI with rules and guidelines you want it to follow.
This template outlines a few examples of things you can add. You can add your own sections and format it to suit your needs

TIP: More context isn't always better. It can confuse the LLM. Try and add the most important rules you need

# General guidelines

Any general rules you want the AI to follow.
For example:

* Only use absolute positioning when necessary. Opt for responsive and well structured layouts that use flexbox and grid by default
* Refactor code as you go to keep code clean
* Keep file sizes small and put helper functions and components in their own files.

--------------

# Design system guidelines
Rules for how the AI should make generations look like your company's design system

Additionally, if you select a design system to use in the prompt box, you can reference
your design system's components, tokens, variables and components.
For example:

* Use a base font-size of 14px
* Date formats should always be in the format “Jun 10”
* The bottom toolbar should only ever have a maximum of 4 items
* Never use the floating action button with the bottom toolbar
* Chips should always come in sets of 3 or more
* Don't use a dropdown if there are 2 or fewer options

You can also create sub sections and add more specific details
For example:


## Button
The Button component is a fundamental interactive element in our design system, designed to trigger actions or navigate
users through the application. It provides visual feedback and clear affordances to enhance user experience.

### Usage
Buttons should be used for important actions that users need to take, such as form submissions, confirming choices,
or initiating processes. They communicate interactivity and should have clear, action-oriented labels.

### Variants
* Primary Button
  * Purpose : Used for the main action in a section or page
  * Visual Style : Bold, filled with the primary brand color
  * Usage : One primary button per section to guide users toward the most important action
* Secondary Button
  * Purpose : Used for alternative or supporting actions
  * Visual Style : Outlined with the primary color, transparent background
  * Usage : Can appear alongside a primary button for less important actions
* Tertiary Button
  * Purpose : Used for the least important actions
  * Visual Style : Text-only with no border, using primary color
  * Usage : For actions that should be available but not emphasized
-->
