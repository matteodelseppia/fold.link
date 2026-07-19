# fold.link - MVP - Definition of Done

## Purpose

Every ticket in `docs/milestones/mvp/tickets/` is small by design. This document
defines the single checklist a ticket must satisfy before it is considered done,
so completion is judged the same way regardless of who does the work or which
ticket it is.

## Definition of done checklist

A ticket is done only when **all** of the following are true:

1. **Code review** - the change was submitted as a GitLab merge request, targets
   `main`, and was reviewed (self-review is acceptable pending MVP-005's
   approval policy, but the MR must exist and its diff must match the ticket's
   scope - no unrelated files).
2. **Automated test for changed behavior** - if the change alters observable
   behavior (application code, configuration that affects runtime behavior,
   scripts, CI jobs), an automated test (unit, system, load, or pipeline check,
   whichever fits the ticket) exercises that behavior and is committed
   alongside it. Tickets that only add non-behavioral assets (for example a
   `.gitignore` entry or a documentation file) satisfy this by the ticket's own
   "Testing" acceptance criterion instead, since there is no runtime behavior
   to assert on.
3. **Commands run are recorded** - every command used to verify the acceptance
   criteria (build, test, lint, `git status --ignored`, etc.) is listed in the
   MR description or an evidence note, with enough detail to rerun it.
4. **External-console evidence, no secret values** - for changes that touch
   Infisical, GitLab (settings, not code), DNS, or Railway, the MR description
   or a linked note records what was configured and observed in that
   console/API (e.g. project/environment names, identifiers, screenshots,
   `curl` calls against non-secret endpoints). Actual secret values, tokens,
   passwords, or connection strings are never pasted into the MR, a commit, or
   this repository. Redact them (e.g. `***`) if a screenshot would otherwise
   include one.
5. **Documentation updated** - if the change affects how the system is run,
   configured, or deployed, the relevant doc under `docs/` is updated in the
   same MR (not left as a follow-up).
6. **Every acceptance criterion is checked off** - each acceptance criterion in
   the ticket file has a corresponding piece of evidence (a test, a command
   output, or a console note) confirming it holds.

## The ticket stays open if any check fails, is skipped, or is undocumented

A ticket must **not** be marked done while any of the following is true:

- An acceptance criterion was not verified.
- A check (test, lint, pipeline job) was run and failed.
- A check was skipped (including "skipped for now" or commented out) without
  the ticket itself being reopened or blocked on that skip.
- A completion step happened but was not written down anywhere (an
  undocumented completion step). If it isn't recorded, it isn't done.

Partial progress belongs in a comment or a follow-up ticket, not in a "done"
ticket with caveats. Do not combine unfinished acceptance criteria across
tickets to call either one complete - each ticket closes independently on its
own criteria.

## Worked examples

### Example 1: pure-repository ticket - MVP-007 (add repository ignore rules)

Checklist applied:

1. **Code review**: MR opened against `main`, diff limited to `.gitignore` (and
   this ticket's own status update).
2. **Automated test for changed behavior**: `.gitignore` has no runtime
   behavior, so the ticket's own "Testing" criterion stands in - create
   representative disposable files (`build/`, `*.env.local`, `.idea/`,
   `node_modules/`, k6 output, an Infisical export) and run
   `git status --ignored` to confirm they're classified as ignored while
   tracked files (source, Gradle wrapper, `docs/`) are not.
3. **Commands run recorded**: the `git status --ignored` invocation and its
   output (or a summary of it) are pasted into the MR description.
4. **External-console evidence**: not applicable - no Infisical, GitLab
   settings, DNS, or Railway change is involved.
5. **Documentation updated**: not applicable - the ignore rules are
   self-documenting via the acceptance criteria; no other doc references them.
6. **Acceptance criteria checked off**: build outputs and secrets ignored,
   source/wrapper/docs tracked, no planning document accidentally ignored -
   each verified by the `git status --ignored` run above.

No step here is left undocumented: the whole check is a local git command
whose output goes straight into the MR.

### Example 2: hosted-service ticket - MVP-039 (create Railway project environments)

Checklist applied:

1. **Code review**: MR opened against `main` containing only this ticket's
   documentation update (e.g. recording the project/environment names and
   IDs), since the Railway resources themselves are created in the Railway
   console/API, not in this repository.
2. **Automated test for changed behavior**: not applicable in the "code test"
   sense - the ticket's own "Testing" criterion (list resources in each
   environment and confirm no service or variable is unintentionally shared)
   is the verification, and its output is the evidence.
3. **Commands run recorded**: the `railway` CLI (or API) calls used to list
   resources per environment are written into the MR description, e.g.
   `railway status`, `railway environment list`.
4. **External-console evidence, no secret values**: the MR notes the Railway
   project name, the staging and production environment names, and their
   non-secret identifiers (project ID, environment IDs), plus confirmation
   that team access is least-privilege and production is restricted. No
   Railway API tokens or variable values are included.
5. **Documentation updated**: the non-secret project/environment identifiers
   are recorded in a repo doc (e.g. a deployment or infrastructure note) so
   later deployment-automation tickets can reference them without recreating
   the resources.
6. **Acceptance criteria checked off**: isolation, least-privilege access, and
   identifier availability are each backed by the console listing evidence
   from step 3.

Every completion step is documented even though the resource creation itself
happened outside the repository: the console/CLI evidence and identifiers are
what get committed, never the secret credentials used to obtain them.

## Relationship to other MVP tickets

- MVP-005 defines the branch/merge mechanics (protected `main`, squash merge,
  who may promote production) that this checklist's "code review" step relies
  on.
- MVP-003's ticket index tracks status; a ticket's status only becomes "done"
  in that index once this checklist is satisfied in full.
