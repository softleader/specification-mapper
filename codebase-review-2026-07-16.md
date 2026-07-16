# Codebase Review — specification-mapper (2026-07-16)

Branch `jakarta` · parent `3.2.3-SNAPSHOT` · Java 17 · Spring Boot 3.5.15. Scope: modules `mapper/` and `starter/`. Out of scope (by request): `site/`, all `target/`. Profile: **thorough** — 9 scan agents (5 vertical units + 4 horizontal sweeps) + a completeness-critic backfill round, cross-axis dedup, adversarial verification (blocker/high 3-vote, medium 1-vote). All six dimensions in scope.

## Executive summary

specification-mapper is a small, well-tested (`mvn test` green, ~1:1 test ratio), maturely-built annotation-driven POJO→JPA-`Specification` binder — no SQL/JPQL string concatenation anywhere, so the classic injection surface is absent. The library engine is sound; the risk is concentrated in **two silent-behavior traps at the framework seams**, a **cluster of join-machinery edge cases**, and **order-dependent boolean composition**. The single biggest risk is the starter silently swapping the repository base class of *every* JPA repository, which can turn a consumer's soft-delete into a hard delete (COR-01). Close behind, `@JoinFetch` on a paged query triggers Hibernate in-memory pagination and can OOM on large tables (PERF-01). The join resolvers silently return wrong results on several realistic misconfigurations (COR-05, COR-06, COR-07, COR-11), a field-level `@Or`/`@And` on the *first-declared* field is silently ignored so the same annotation yields opposite SQL depending on declaration order (COR-15, COR-16), and the LIKE specs don't escape `%`/`_` wildcards (SEC-01). Nothing is a blocker — both high-severity findings need a specific-but-realistic consumer combination — but for a library published to Maven Central these silent-failure modes are the priority. Start with COR-01 and PERF-01. **43 findings CONFIRMED** under adversarial verification, 2 REFUTED and dropped.

## Scorecard

| Category | Grade | Why |
|---|---|---|
| correctness | 🟡 | 1 CONFIRMED high (COR-01, silent base-class clobber) + 15 CONFIRMED medium (join semantics, order-dependent `@Or`/`@And`, exception masking, fluent-query edge) |
| security | 🟢 | 1 CONFIRMED medium (SEC-01 LIKE wildcard injection); no string-built queries, values are always bound parameters |
| performance | 🟡 | 1 CONFIRMED high (PERF-01 fetch-join in-memory pagination) + 2 medium (PERF-02 reused-spec heap growth, PERF-03 eager AST) |
| testing | 🟡 | 8 CONFIRMED medium — join edge/error paths, concurrency machinery, native profile, leaf-spec negative paths, and mapper-path type-mismatch all uncovered |
| dependencies | 🟡 | 4 CONFIRMED medium — unpinned plugins float to Boot 4.x (DEP-03), no Maven update lane (DEP-01), EOL Boot line (DEP-04), `@latest` action with write token (DEP-02) |
| maintainability | 🟢 | 2 CONFIRMED medium (dead `Ordered` contract MAINT-01, accidental auto-config ordering MAINT-02) + 7 low |

Grades are deterministic (worst-first: 🔴 any CONFIRMED blocker or 3+ high; 🟡 any CONFIRMED high or 3+ medium; 🟢 otherwise).

## Ground truth

| Gate | Command | Outcome |
|---|---|---|
| Build + unit/integration tests | `make test` (JDK 17.0.6-tem, `mvn process-classes test`) | ✅ pass — 214 tests, 0 failures, 0 skipped; BUILD SUCCESS in 37.7 s |
| Native tests | `make test-native` (`-PnativeTest`) | ⏭️ not run (needs GraalVM; also never run in CI — see TEST-02) |
| Format / license | spotless + mycila (bound to `validate`) | ⏭️ not exercised as a separate gate |

The green suite is genuine but blind to the join edge cases, concurrency machinery, and native path (see Testing themes).

## Top risks

1. **Starter silently overrides every repository's base class (COR-01, correctness, high, CONFIRMED).** `repositoryBaseClassCustomizer` calls `setRepositoryBaseClass(DefaultQueryBySpecExecutor)` unconditionally on every `JpaRepositoryFactoryBean`, and Spring Data applies a user's `@EnableJpaRepositories(repositoryBaseClass=…)` *before* running customizers — so the starter always wins, DEBUG-log only. *Scenario:* a consumer with a soft-delete base class adds the starter; `delete()` silently reverts to `SimpleJpaRepository`'s hard delete → physical data loss. *Fix:* only register the customizer when `spec.mapper.repository-base-class` is explicitly set, or detect a pre-configured non-default base class and skip with a WARN. *Effort: M.*

2. **`@JoinFetch` + pagination forces in-memory paging (PERF-01, performance, high, CONFIRMED).** `JoinFetch` delegates to a plain join only for `Number` result types (count/exists); the paged *content* query keeps the fetch, so `findBySpec(pojo, pageable)` combines `firstResult/maxResults` with a collection fetch — Hibernate `HHH90003004`, whole joined set loaded into heap before slicing. *Scenario:* paged fetch over a 1M-row table → latency/heap grow linearly, potential OOM, on the primary public API. *Fix:* fall back to a plain join (or id-page-then-fetch) on the content query when pagination is in play; at minimum warn + document. *Effort: M.*

3. **Join resolvers silently return wrong results on realistic misconfigurations (COR-05/06/07/10/11, correctness, medium, CONFIRMED).** `query.distinct()` is last-writer-wins across multiple joins (COR-05); a reused alias with a different path/joinType is silently dropped (COR-06); a 3-segment dotted path silently drops the tail yet registers a 3-level-looking alias (COR-07); a `@Spec` referencing a not-yet-registered alias silently falls back to `root.get` (COR-10); and a non-INNER `@JoinFetch` resolves as INNER in the content query but honors the joinType in the count query, so **`totalElements` can disagree with page content** (COR-11). Each is a silent wrong-result path with no error. *Fix theme:* validate and fail loudly (path depth, alias compatibility, alias availability); accumulate `distinct`; resolve fetch aliases consistently. *Effort: M (cluster).*

4. **LIKE-family wildcard injection (SEC-01, security, medium, CONFIRMED).** `Like`/`NotLike`/`StartingWith`/`EndingWith` build patterns as `"%" + value + "%"` with no escaping and no `escape` char on `cb.like`. *Scenario:* a `%` submitted into a `StartingWith`-mapped field becomes `%%` (match-all), defeating a prefix scoping filter (tenant/department code) → data exposure; pathological patterns force full-scan `LIKE` → cheap DoS. Values are bound parameters (not SQL injection), but wildcard semantics leak. *Fix:* escape `\ % _` and use the `cb.like(expr, pattern, '\\')` overload. *Effort: S.*

5. **Fluent `findBySpec` 500s on an empty search form (COR-13, correctness, medium, CONFIRMED).** Empty criteria map to a `null` Specification; `SimpleJpaRepository.findBy` asserts non-null and throws `IllegalArgumentException`, while every sibling (`findBySpec`/`findBySpec(…, pageable)`) tolerates it and returns all rows. *Scenario:* an empty form submitted through the fluent projection/paging API → opaque 500. *Fix:* substitute an unrestricted spec when mapping yields null. *Effort: S.*

## Themes

- **Silent framework-seam behavior (theme: repository-base-class-override, bean-post-processor-early-init, autoconfig-ordering).** The starter's auto-config takes powerful, container-wide actions with no opt-in and DEBUG-only visibility: it clobbers the base class (COR-01), forces early instantiation of user `RepositoryFactoryCustomizer` beans by injecting a `List` into a non-static `BeanPostProcessor` `@Bean` — dropping proxies/`@Transactional` on their deps (COR-12) — and relies on accidental alphabetical ordering because it lacks `@AutoConfiguration`/`@AutoConfigureAfter` (MAINT-02). *Fix strategy:* make side effects opt-in, use `static` BPP `@Bean` + `ObjectProvider`, add proper auto-config ordering annotations. *Effort to clear: M.*

- **Join machinery: validate-or-fail-loud (theme: dotted-path-two-level-limit, alias-collision-unchecked, distinct-flag-last-writer-wins, alias-availability-not-validated, fetch-alias-implicit-join, result-type-delegation-heuristic).** Six confirmed correctness findings and zero edge-case tests (TEST-04) in the highest-churn area. The common root cause: the resolvers assume well-formed, well-ordered input and silently mis-handle anything else instead of rejecting it. `delete(Specification)` with a join/fetch POJO is also broken by the `Number`-result heuristic (COR-08). *Fix strategy:* one pass adding validation + descriptive exceptions, `distinct` accumulation, consistent fetch-alias resolution, plus the missing tests. *Effort to clear: M.*

- **Order-dependent boolean composition (theme: reduce-seed-wrapper-ignored).** `Conjunction`/`Disjunction.combine` inspects only the reduce *element*, never the seed, and `CompoundSpecification.toPredicate` folds via a seedless `reduce`. So a field-level `@Or` on the **first-declared** field becomes the accumulator and its wrapper is never examined: `[@Or nickname, name]` yields `nickname AND name` (the `@Or` silently ignored) while the reverse order yields `name OR nickname` (COR-15); the mirror holds for `@And` under a class-level `@Or` (COR-16). The same annotation produces opposite SQL depending on declaration position, no test pins first-position behavior (TEST-09), and the `specs` field is a `Collection` whose order the API doesn't guarantee (MAINT-08); `@And`+`@Or` on one field silently resolves to `And` (MAINT-09). *Fix:* make the fold seed-aware (or fail/log on a leading wrapper); pin first-position tests; narrow the type to `List`. *Effort: M.*

- **Reflective construction hides the real exception (theme: reflective-construction-masking).** `SimpleSpecification.newSpec` builds specs via `Constructor.newInstance` under `@SneakyThrows`, so the documented `TypeMismatchException`/`IllegalArgumentException` surface to callers as `InvocationTargetException` (COR-09) — and the only tests assert the exception via *direct* construction, never through `SpecMapper.toSpec` (TEST-07), so the masking is uncovered. *Fix:* unwrap the cause in `newSpec`; add mapper-path tests. *Effort: S.*

- **Leaf-spec value validation is inconsistent (theme: missing-constructor-validation, one-shot-iterable-consumption, negative-path-test-gap).** Typed leaves diverge on fail-fast: `ComparableSpecification`/`BooleanSpecification` reject bad types at construction, but `Between` never checks its elements are `Comparable` (raw `ClassCastException` at query time, MAINT-06) and the LIKE family accepts any `Object` and `toString`s it (MAINT-07); `Between`/`In` also consume the value `Iterable` more than once, breaking one-shot iterables (COR-14); and `BetweenTest` has no negative-path assertions unlike its siblings (TEST-08). *Fix:* add fail-fast element checks mirroring the comparable/boolean leaves, materialize the iterable once, add negative tests. *Effort: S.*

- **Hot-path allocation thrown away (theme: ast-eager-logging, root-keyed-context-growth, distinct-default).** The debug AST tree is fully built and stringified (recursive `toString` of the whole spec tree) on every `toSpec`, then discarded when DEBUG is off (PERF-03); a reused Specification accumulates `Root`-keyed join entries unboundedly (PERF-02); `distinct=true` default forces `SELECT DISTINCT` even on to-one joins (PERF-04). *Fix:* gate AST build behind `isDebugEnabled`; scope per-execution join state to the query lifecycle. *Effort: M.*

- **CI/CD & dependency drift (theme: unpinned-plugin-versions, no-maven-update-lane, spring-line-eol, unpinned-actions, ci-gating, stale-branch-config).** `spring-boot-maven-plugin` is unversioned and currently resolves to **Boot 4.1.0** against a 3.5.15 BOM (DEP-03); no automated Maven update lane exists (DEP-01, gpg plugin ~5 years stale); the auto-bump lane is capped at `~3.x` on an EOL Boot line (DEP-04); `madhead/semver-utils@latest` runs in a workflow wielding a write token that auto-merges its own PRs (DEP-02); bump PRs auto-merge with only commitlint as an in-repo check (TEST-01); and the Jenkins failure alert is gated on `BRANCH_NAME == 'main'`, dead on the `jakarta` line (COR-02). *Fix strategy:* pin plugins to `${spring-boot.version}` in `pluginManagement`, add a Maven dependabot lane, SHA-pin third-party actions, fix the branch guard, decide the Boot 4 story. *Effort: M.*

- **Native-image support can regress unseen (theme: aot-hints-coverage, native-profile-not-in-ci).** Runtime hints cover only classes in the mapper's own Jandex index; consumer query POJOs and custom spec classes get no hints and the contract is undocumented (COR-04) — and the `nativeTest` profile is never run by any pipeline (TEST-02), so a native regression ships green. *Fix:* document the `reflect-config.idx` contract and/or add a starter AOT processor; run `make test-native` in CI. *Effort: M.*

## Quick wins

CONFIRMED, effort S, severity ≥ medium — actionable the same day:

| id | title | file | fix |
|---|---|---|---|
| SEC-01 | LIKE specs don't escape `%`/`_` wildcards | `Like.java:53` | escape `\ % _`, use `cb.like(…, '\\')` |
| COR-13 | fluent `findBySpec` 500s on empty criteria | `QueryBySpecExecutorAdapter.java:116` | substitute unrestricted spec when mapping yields null |
| COR-09 | `TypeMismatchException` masked as `InvocationTargetException` | `SimpleSpecification.java:67` | unwrap the cause in `newSpec` |
| COR-05 | `distinct` is last-writer-wins across joins | `Join.java:84` | accumulate `distinct`, never reset to false |
| COR-06 | conflicting same-alias joins silently dropped | `Join.java:94` | compare stored path/joinType, throw on mismatch |
| COR-07 | 3-segment dotted join path silently drops the tail | `Join.java:112` | validate `byDot.length == 2`, throw named error |
| COR-12 | non-static BPP `@Bean` forces early bean init | `SpecMapperAutoConfiguration.java:116` | `static` `@Bean` + `ObjectProvider` |
| COR-03 | jar ships `META-INF/build-info.properties`, hijacks consumer `/actuator/info` | `mapper/pom.xml:87` | remove the `build-info` goal (nothing reads it) |
| COR-02 | Jenkins failure alert gated on dead `main` branch | `Jenkinsfile:157` | guard on `jakarta` / primary branch |
| MAINT-02 | auto-config ordering rests on alphabetical FQCN sort | `SpecMapperAutoConfiguration.java:111` | `@AutoConfiguration(after = JpaRepositoriesAutoConfiguration.class)` |
| PERF-02 | reused Specification leaks `Root`-keyed join entries | `SpecJoinContext.java:41` | scope join state per-execution |
| DEP-03 | `spring-boot-maven-plugin` unversioned → Boot 4.1.0 | `mapper/pom.xml:83` | pin to `${spring-boot.version}` in `pluginManagement` |
| DEP-01 | no Maven update lane (dependabot = actions only) | `.github/dependabot.yml:5` | add `package-ecosystem: maven` |
| DEP-02 | `madhead/semver-utils@latest` + write token auto-merge | `bump-spring.yml:42` | SHA-pin third-party actions |
| TEST-01 | bump PRs auto-merge with only commitlint in-repo | `bump-spring.yml:91` | require the Jenkins check / add `mvn verify` GH Action |
| TEST-03 | inherited criteria-POJO fields silently ignored, untested | `ReflectionDatabind.java:80` | add a subclass test pinning the contract |
| TEST-05 | nested-AND test typo (`@NestedSpec` on a `String`) | `NestedSpecificationResolverTest.java:392` | change to `@Spec`, assert depth-3 tree |
| TEST-06 | `fireOnlyOnce` concurrency test runs single-threaded | `ReflectionDatabindTest.java:52` | raise thread count + start-barrier |
| TEST-07 | type-mismatch only tested via direct construction | `InTest.java:51` | add `SpecMapper.toSpec` mismatch tests |

## Coverage statement

**Units (vertical deep-read axis).** All five review units deep-read, no scan agent dropped or retried:
- A — core engine (`SpecMapper`, resolver chain, `ReflectionDatabind`, `FieldDescriptor`, `SkippingStrategy`, `SpecContext`) — deep-read.
- B — join machinery (`JoinSpecificationResolver`, `JoinFetchSpecificationResolver`, `SpecJoinContext`, `domain/Join`, `domain/JoinFetch`, join annotations) — deep-read (hotspot).
- C — domain spec leaves + annotation surface — deep-read.
- D — AST/observability + AOT + build config — deep-read.
- E — starter (autoconfig, `QueryBySpecExecutor*`, `JpaRepositoryFactoryBeanPostProcessor`) — deep-read (hotspot).

**Horizontals (repo-wide path axis).** security, performance, testing, dependencies — all completed, full repo (S-size repo, no risk sampling needed).

**Verification.** Main scan: 87 raw findings → 66 distinct after cross-axis dedup; all blocker/high (3-vote) and all medium (1-vote) verified — the run extended medium verification because the output disposition is GitHub issues (CONFIRMED-only). Completeness-critic **backfill** round (2 targeted scans over leaf-spec negative paths and compound composition): 9 findings, all verified. Combined result: **43 CONFIRMED, 0 PLAUSIBLE, 2 REFUTED**. Low-severity findings surfaced by the medium/backfill passes carried a verifier through anyway; pure-low findings from the main scan were not verified per rule. Two findings were REFUTED and dropped: the "dotted *fetch* path drops segments" claim (the fetch ref retains all segments; only eager-loading is affected, not correctness) and the "parity guard deleted → undetected API drift" claim (the `countSpec` overload was *deliberately* removed in PR #127 for cross-Boot-3.x compatibility — reintroducing it would break compilation on 3.4). Both refutations were independently code/git-verified.

**Backfill outcome.** The completeness critic (thorough-profile Phase 7) flagged two under-covered in-scope areas; both were scanned and both were real: the leaf-spec constructors were existence-tested but not correctness-tested (COR-14, MAINT-06/07, TEST-08), and the compound-composition fold had a genuine order-dependent-`@Or` bug (COR-15/16, TEST-09, MAINT-08/09). This is why the confirmed count rose from 34 to 43.

**Not covered.** No dimension was descoped — all six were systematically reviewed. `site/` and all `target/` were excluded by request and not reviewed. Native-image *runtime* behavior was assessed by reading (COR-04, TEST-02) but not executed. Two dimension notes from the completeness critic: the **security** sweep considered the reflection/`makeAccessible` surface (operates only on caller-supplied POJOs — genuinely low risk) and the `@SneakyThrows` masking (filed as correctness COR-09) and cleared them — LIKE injection (SEC-01) is the one grounded security defect, not a sign the surface went unexamined. **Testing dialect risk:** both modules test exclusively against H2; dialect-sensitive constructs (`trim`/`length` in `HasText`/`HasLength`, `LIKE` case behavior, IN-list limits per MAINT-03) are unexercised against a real RDBMS — a known, accepted gap for a library test suite.

## Next steps

Output disposition (agreed at checkpoint): **report + GitHub issues**. This file is the deliverable; the 43 CONFIRMED findings are packaged into **14 work packages** under a parent tracking Epic, filed against `softleader/specification-mapper` (no prior review Epic existed). Issues are *not* labelled `ready-for-agent` — this is a review handoff, not an authorization to auto-build; a person triages and prioritizes them.

Recommended order — quick wins and high-severity first, independent packages parallelizable:

| # | package | type | findings | effort |
|---|---|---|---|---|
| WP1 | Starter silently overrides every repository base class | Bug | COR-01 | M |
| WP2 | `@JoinFetch` on paged queries forces in-memory pagination | Bug | PERF-01 | M |
| WP13 | Compound composition: first-position `@Or`/`@And` silently ignored | Bug | COR-15/16, TEST-09, MAINT-08/09 | M |
| WP4 | LIKE wildcard injection + unbounded IN | Risk | SEC-01, MAINT-03 | S |
| WP5 | Reflective construction masks `TypeMismatchException` | Bug | COR-09, TEST-07, MAINT-04 | S |
| WP6 | Starter auto-configuration robustness | Bug | COR-13, COR-12, MAINT-02, MAINT-05 | S |
| WP14 | Leaf-spec constructor validation & one-shot `Iterable` | Bug | COR-14, MAINT-06/07, TEST-08 | S |
| WP12 | CI/CD supply-chain & gating | Task | DEP-02, TEST-01, COR-02 | S |
| WP3 | Join machinery silent-wrong-result cluster + tests | Bug | COR-05/06/07/08/10/11, PERF-02, TEST-04 | M |
| WP10 | Mapper test-coverage gaps | Task | TEST-03/05/06 | S |
| WP11 | Build config & dependency hygiene | Task | DEP-01/03/04, COR-03 | M |
| WP7 | Mapper hot-path allocation | Task | PERF-03, PERF-04 | M |
| WP8 | Native-image / AOT completeness | Task | COR-04, TEST-02 | M |
| WP9 | Dead `Ordered`/`getOrder` resolver contract | Task | MAINT-01 | M |

Parent Epic and per-package links are filled in below once created. Same-file packages (WP1/WP6/MAINT-02 all touch `SpecMapperAutoConfiguration`; WP13/WP14 both touch `domain/`) should run sequentially to avoid conflicts.

**Parent Epic:** [#172](https://github.com/softleader/specification-mapper/issues/172) · report committed to branch `codebase-review-2026-07-16`.

| WP | issue | type | findings |
|---|---|---|---|
| WP1 | #173 | Bug | COR-01 |
| WP2 | #174 | Bug | PERF-01 |
| WP13 | #175 | Bug | COR-15, COR-16, TEST-09, MAINT-08, MAINT-09 |
| WP4 | #176 | Risk | SEC-01, MAINT-03 |
| WP5 | #177 | Bug | COR-09, TEST-07, MAINT-04 |
| WP6 | #178 | Bug | COR-13, COR-12, MAINT-02, MAINT-05 |
| WP14 | #179 | Bug | COR-14, MAINT-06, MAINT-07, TEST-08 |
| WP12 | #180 | Task | DEP-02, TEST-01, COR-02 |
| WP3 | #181 | Bug | COR-07, COR-05, COR-06, COR-08, COR-11, COR-10, PERF-02, TEST-04 |
| WP10 | #182 | Task | TEST-06, TEST-05, TEST-03 |
| WP11 | #183 | Task | DEP-03, DEP-01, DEP-04, COR-03 |
| WP7 | #184 | Task | PERF-03, PERF-04 |
| WP8 | #185 | Task | COR-04, TEST-02 |
| WP9 | #186 | Task | MAINT-01 |

## Appendix — all findings

Rank order (severity, then category). REFUTED findings excluded (they exist only as the refuted count above).

| id | cat | sev | verdict | conf | file:line | title | theme | effort |
|---|---|---|---|---|---|---|---|---|
| COR-01 | correctness | high | CONFIRMED | high | `starter/src/main/java/tw/com/softleader/data/jpa/spec/starter/autoconfigure/SpecMapperAutoConfiguration.java:131` | repositoryBaseClassCustomizer silently overrides the repository base class of every JPA repository, clobbering @EnableJpaRepositories(repositoryBaseClass=...) | repository-base-class-override | M |
| PERF-01 | performance | high | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/JoinFetch.java:91` | Fetch join executes on paged content queries, forcing Hibernate in-memory pagination | fetch-join-pagination | M |
| COR-02 | correctness | medium | CONFIRMED | high | `Jenkinsfile:157` | Jenkins failure notification is gated on BRANCH_NAME == 'main', which is never true on the active jakarta branch | stale-branch-config | S |
| COR-03 | correctness | medium | CONFIRMED | high | `mapper/pom.xml:87` | Library jar ships META-INF/build-info.properties, hijacking consumers' BuildProperties//actuator/info | library-jar-pollution | S |
| COR-04 | correctness | medium | CONFIRMED | medium | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/aot/SpecMapperRuntimeHints.java:50` | Native-image reflection hints cover only jars shipping their own Jandex index; user query POJOs are silently unfiltered, and the contract is undocumented | aot-hints-coverage | M |
| COR-14 | correctness | medium | CONFIRMED | medium | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/Between.java:57` | Between and In consume the value Iterable multiple times, breaking one-shot Iterables | one-shot-iterable-consumption | S |
| COR-15 | correctness | medium | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/Conjunction.java:38` | Field-level @Or on the first-declared field is silently ignored because combine() inspects only the reduce element, never the seed | reduce-seed-wrapper-ignored | M |
| COR-16 | correctness | medium | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/Disjunction.java:38` | Mirror defect: field-level @And on the first-declared field of an @Or class is silently ignored | reduce-seed-wrapper-ignored | M |
| COR-05 | correctness | medium | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/Join.java:84` | query.distinct() is overwritten by the last-executed join spec — distinct flag is last-writer-wins | distinct-flag-last-writer-wins | S |
| COR-06 | correctness | medium | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/Join.java:94` | Conflicting join definitions sharing an alias are silently dropped — first registration wins without any compatibility check | alias-collision-unchecked | S |
| COR-07 | correctness | medium | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/Join.java:112` | Dotted join path silently drops every segment after the second | dotted-path-two-level-limit | S |
| COR-08 | correctness | medium | CONFIRMED | medium | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/JoinFetch.java:91` | Number-result-type delegation heuristic does not cover delete(Specification), contradicting its own comment | result-type-delegation-heuristic | M |
| COR-09 | correctness | medium | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/SimpleSpecification.java:67` | TypeMismatchException from spec constructors is masked as InvocationTargetException when going through SpecMapper | reflective-construction-masking | S |
| COR-10 | correctness | medium | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/SimpleSpecification.java:86` | A @Spec path referencing an unregistered join alias silently falls back to root.get(alias) — declaration order and null-valued join fields break alias resolution | alias-availability-not-validated | M |
| COR-11 | correctness | medium | CONFIRMED | medium | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/SimpleSpecification.java:100` | Count and content queries resolve a @JoinFetch alias with different join semantics — page totals can disagree with page content for non-INNER fetches | fetch-alias-implicit-join | M |
| COR-12 | correctness | medium | CONFIRMED | medium | `starter/src/main/java/tw/com/softleader/data/jpa/spec/starter/autoconfigure/SpecMapperAutoConfiguration.java:116` | BeanPostProcessor declared as non-static @Bean forces early instantiation of all RepositoryFactoryCustomizer beans and their dependencies | bean-post-processor-early-init | S |
| COR-13 | correctness | medium | CONFIRMED | high | `starter/src/main/java/tw/com/softleader/data/jpa/spec/starter/repository/support/QueryBySpecExecutorAdapter.java:116` | Fluent findBySpec throws IllegalArgumentException for null or empty criteria, unlike every sibling method | null-spec-fluent-query | S |
| DEP-01 | dependencies | medium | CONFIRMED | high | `.github/dependabot.yml:5` | No automated update lane for any Maven dependency or plugin except spring-boot.version; visible staleness confirms the gap | no-maven-update-lane | S |
| DEP-02 | dependencies | medium | CONFIRMED | high | `.github/workflows/bump-spring.yml:42` | Third-party actions pinned to mutable tags — including `madhead/semver-utils@latest` — in a workflow that wields a repo-write App token and auto-merges its own PRs | unpinned-actions | S |
| DEP-03 | dependencies | medium | CONFIRMED | high | `mapper/pom.xml:83` | spring-boot-maven-plugin and native-maven-plugin have no pinned version and currently resolve to Boot 4.1.0 against the 3.5.15 BOM | unpinned-plugin-versions | S |
| DEP-04 | dependencies | medium | CONFIRMED | medium | `pom.xml:63` | Spring Boot 3.5.x baseline is at/past OSS end-of-support and the auto-bump lane is capped at ~3.x, so the lane will go silently stale | spring-line-eol | M |
| MAINT-01 | maintainability | medium | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/SpecificationResolver.java:73` | Ordered/getOrder contract is documented but never honored anywhere — resolver order is purely insertion order | dead-order-contract | M |
| MAINT-02 | maintainability | medium | CONFIRMED | medium | `starter/src/main/java/tw/com/softleader/data/jpa/spec/starter/autoconfigure/SpecMapperAutoConfiguration.java:111` | Auto-configuration lacks @AutoConfiguration/@AutoConfigureAfter; @ConditionalOnBean(JpaRepositoryFactoryBean) correctness rests on alphabetical ordering | autoconfig-ordering | S |
| PERF-02 | performance | medium | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/SpecJoinContext.java:41` | joined/fetched maps keyed by Root grow on every execution of a reused Specification and are never cleaned | root-keyed-context-growth | S |
| PERF-03 | performance | medium | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/SpecMapper.java:89` | AST debug tree is fully built and stringified on every toSpec call even when DEBUG is off | ast-eager-logging | M |
| SEC-01 | security | medium | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/Like.java:53` | LIKE-family specs concatenate user values into patterns without escaping % and _ wildcards | like-wildcard-injection | S |
| TEST-01 | testing | medium | CONFIRMED | low | `.github/workflows/bump-spring.yml:91` | Dependency-bump PRs are auto-merged while the only in-repo PR-triggered check is commitlint — build/test gating depends entirely on out-of-repo Jenkins branch protection | ci-gating | S |
| TEST-02 | testing | medium | CONFIRMED | high | `Jenkinsfile:107` | nativeTest profile is never exercised by any CI pipeline — GraalVM/AOT support can regress silently | native-profile-not-in-ci | M |
| TEST-03 | testing | medium | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/ReflectionDatabind.java:80` | Inherited fields of criteria POJOs are silently ignored (doWithLocalFields) and no test pins this contract | local-fields-only | S |
| TEST-04 | testing | medium | CONFIRMED | high | `mapper/src/test/java/tw/com/softleader/data/jpa/spec/JoinSpecificationResolverTest.java:48` | Join machinery edge and error paths have no test coverage | join-edge-case-coverage | M |
| TEST-05 | testing | medium | CONFIRMED | high | `mapper/src/test/java/tw/com/softleader/data/jpa/spec/NestedSpecificationResolverTest.java:392` | NestedInNestedAnd.name is annotated @NestedSpec on a String (likely a @Spec typo), so the three-level AND scenario is never actually tested | nested-test-typo | S |
| TEST-06 | testing | medium | CONFIRMED | high | `mapper/src/test/java/tw/com/softleader/data/jpa/spec/ReflectionDatabindTest.java:52` | fireOnlyOnce concurrency test runs with a single thread, leaving the AtomicBoolean+CountDownLatch machinery untested | bespoke-lazy-load | S |
| TEST-08 | testing | medium | CONFIRMED | high | `mapper/src/test/java/tw/com/softleader/data/jpa/spec/domain/BetweenTest.java:39` | BetweenTest covers only the happy path; none of Between's constructor validation branches are tested | negative-path-test-gap | S |
| TEST-07 | testing | medium | CONFIRMED | high | `mapper/src/test/java/tw/com/softleader/data/jpa/spec/domain/InTest.java:51` | Type-mismatch behavior is only tested via direct construction, never through SpecMapper.toSpec, hiding the exception-masking bug | reflective-construction-masking | S |
| MAINT-09 | maintainability | low | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/SimpleSpecificationResolver.java:88` | A field annotated with both @And and @Or silently resolves to And with no validation | conflicting-combinator-annotations | S |
| MAINT-06 | maintainability | low | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/Between.java:67` | Between never validates elements are Comparable, deferring failure to a raw ClassCastException at query time | missing-constructor-validation | S |
| MAINT-08 | maintainability | low | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/CompoundSpecification.java:39` | Order-dependent fold semantics over an unordered Collection type | order-sensitive-collection-api | S |
| MAINT-03 | maintainability | low | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/In.java:64` | In/NotIn expand unbounded user-supplied collections into a single IN (...) list | unbounded-in-list | S |
| MAINT-07 | maintainability | low | CONFIRMED | medium | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/Like.java:52` | Like family accepts any Object and silently coerces it via toString, unlike its type-checked siblings | missing-constructor-validation | S |
| MAINT-04 | maintainability | low | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/domain/SimpleSpecification.java:91` | Dotted-path specs constructed directly (outside SpecMapper) throw NoSuchElementException because CTX_JOIN is a hidden invariant | context-hidden-invariant | S |
| MAINT-05 | maintainability | low | CONFIRMED | high | `starter/src/main/java/tw/com/softleader/data/jpa/spec/starter/repository/QueryBySpecExecutor.java:44` | QueryBySpecExecutor lacks counterparts for JpaSpecificationExecutor's findAll(spec, countSpec, pageable) and delete(spec) | executor-api-parity | S |
| PERF-04 | performance | low | CONFIRMED | high | `mapper/src/main/java/tw/com/softleader/data/jpa/spec/annotation/Join.java:72` | distinct=true default forces SELECT DISTINCT on every joined query | distinct-default | S |
| TEST-09 | testing | low | CONFIRMED | high | `mapper/src/test/java/tw/com/softleader/data/jpa/spec/SimpleSpecificationResolverTest.java:438` | No test pins a first-position @Or/@And; Conjunction/Disjunction combine has no direct unit tests | reduce-seed-wrapper-ignored | S |
