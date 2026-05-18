## Commit hygiene

CI runs commitlint (`@commitlint/config-conventional`) and blocks merge on violations. Run this right after every `git commit` to catch issues before push (no setup required, cross-platform):

```bash
npx --yes -p @commitlint/cli -p @commitlint/config-conventional \
  commitlint --last --extends @commitlint/config-conventional
```

On failure, fix with `git commit --amend -m "<corrected message>"` and re-run — only before the commit is pushed (amending pushed commits is destructive).
