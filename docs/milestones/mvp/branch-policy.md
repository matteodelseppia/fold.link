# Branch and Merge Policy

This document describes the GitLab workflow used to deliver the MVP safely
through small, reviewed merge requests. It corresponds to ticket
`MVP-005: Establish the branch and merge policy`.

## Protected branch: `main`

`main` is a protected branch with the following GitLab settings
(Settings > Repository > Protected branches):

- **Allowed to push**: `No one` (push access level `0`). Nobody, including
  Maintainers, can push commits to `main` directly — all changes must land
  through a merge request.
- **Allowed to merge**: `Maintainers` (access level `40`).
- **Allow force push**: disabled.
- **Pipeline requirement**: at the project level, "Pipelines must succeed" is
  enabled (`only_allow_merge_if_pipeline_succeeds = true`), so a merge
  request cannot be merged into `main` unless its most recent CI pipeline
  passed.

Together, these settings guarantee that every change to `main` has gone
through an MR, passed CI, and been merged by someone with Maintainer
permissions.

## Merge requests

- **Merge method**: squash merging is enforced project-wide
  (`squash_option = always`), so every MR is squashed into a single commit
  on `main` regardless of how many commits exist on the source branch. This
  keeps `main`'s history linear and easy to bisect.
- **Source branch cleanup**: `remove_source_branch_after_merge = true` is
  set at the project level, and MRs are opened with `remove_source_branch =
  true`. Merged feature branches are deleted automatically.
- **Target branch**: MRs target `main`.
- **Branch naming**: feature branches are named
  `<issue-number>-<short-slug>` (e.g. `39-mvp-005-establish-the-branch-and-merge-policy`),
  matching the GitLab issue they close.
- **Closing issues**: MR descriptions reference the ticket/issue with
  `Closes #<issue-number>` so the issue is closed automatically when the MR
  is merged.

## Emergency rollback procedure

Even in an incident, there are **no direct pushes to `main`** — the
protected branch rule blocks this technically (push access level `0`), not
just by convention. To roll back a bad change:

1. Identify the offending commit(s) on `main`.
2. Create a hotfix branch from `main`, e.g. `hotfix/revert-<short-sha>`.
3. Run `git revert <sha>` (or revert the merge commit with `-m 1` if it was
   a merge commit) on the hotfix branch. Avoid `git reset`/force-push —
   history on `main` must only move forward.
4. Push the hotfix branch and open a merge request targeting `main`, same
   as any other change.
5. Let CI run. The revert MR must pass the pipeline and be squash-merged by
   a Maintainer, exactly like a normal MR — there is no bypass path, even
   for emergencies. This keeps rollbacks auditable and prevents
   unreviewed, unpiped changes from reaching production under pressure.
6. Once merged, redeploy from `main` following the normal deployment
   process.

## Who may promote to production

Promotion to production happens by merging into `main`, which is gated by
the `merge_access_level = 40` setting on the protected branch. This means
only users with the **Maintainer** role on the project (currently the repo
owner) can approve and merge an MR into `main`, and therefore only
Maintainers can promote code to production. Developers/Reporters can open
MRs and push to feature branches, but cannot merge into `main` or push to
it directly.

## Verifying the protection

The policy can be verified two ways:

1. **GitLab's permission view**: go to Settings > Repository > Protected
   branches for the `main` row. It should show:
   - "Allowed to push and merge": `No one`
   - "Allowed to merge": `Maintainers`
   - "Allowed to force push": disabled
   This view is the source of truth for who can do what to `main`.

2. **A real push attempt**: as a non-Maintainer (or by attempting a direct
   push with a Developer-scoped token), running
   `git push origin HEAD:main` against a local commit is rejected by
   GitLab with an error such as:
   ```
   remote: GitLab: You are not allowed to push code to protected branches on this project.
   ! [remote rejected] HEAD -> main (pre-receive hook declined)
   ```
   Even a Maintainer cannot bypass this — the push access level is set to
   `No one`, so the only way to change `main` is via a merge request that
   also satisfies the "pipeline must succeed" requirement.

## Live project settings applied

The following were configured via the GitLab API on the `fold.link`
project (id `84607112`) as part of this ticket:

| Setting | Before | After |
|---|---|---|
| `only_allow_merge_if_pipeline_succeeds` | `false` | `true` |
| `remove_source_branch_after_merge` | `true` | `true` (unchanged) |
| `squash_option` | `default_off` | `always` |
| `main` protected branch — push access | Maintainers (`40`) | No one (`0`) |
| `main` protected branch — merge access | Maintainers (`40`) | Maintainers (`40`, unchanged) |
| `main` protected branch — allow force push | `false` | `false` (unchanged) |

Note: GitLab's project-level `merge_method` field only accepts `merge`,
`rebase_merge`, or `ff` — there is no `squash_merge` value. Squash-on-every-merge
is instead enforced via the separate `squash_option` field, set to `always`,
which is the correct mechanism for "squash merging is the default/enforced
behavior" on this GitLab tier.
