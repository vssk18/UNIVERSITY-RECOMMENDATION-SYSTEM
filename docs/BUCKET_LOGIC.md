# Bucket Logic: Ambitious / Target / Safe

The system assigns each eligible university to one of three buckets:

- **Ambitious** (reach)
- **Target** (balanced fit)
- **Safe** (higher odds, but never guaranteed)

This is done in `logic.RecommendationEngine.classifyBucket`.

## 1. Global Rank

Each university has a `globalRank` field.

- Lower rank = better university.
- If rank is unknown or missing (≤ 0), we treat it as 9999 for ordering, and usually as **SAFE** by default.

## 2. Hard Rule: Top-15

Regardless of the student’s profile:

> All universities with **globalRank ≤ 15** are ALWAYS classified as **Ambitious**.

This covers the genuinely elite programs where competition is extremely high.

## 3. Tier-Based Boundaries

For each **Profile Tier**, we define two boundaries `(b1, b2)`:

- `r ≤ b1` → Ambitious
- `b1 < r ≤ b2` → Target
- `r > b2` → Safe

Approximate boundaries:

- **EXCEPTIONAL**
  - Ambitious: rank ≤ 20
  - Target: rank ≤ 80
  - Safe: rank > 80
- **VERY_STRONG**
  - Ambitious: rank ≤ 50
  - Target: rank ≤ 150
  - Safe: rank > 150
- **STRONG**
  - Ambitious: rank ≤ 80
  - Target: rank ≤ 200
  - Safe: rank > 200
- **GOOD**
  - Ambitious: rank ≤ 150
  - Target: rank ≤ 300
  - Safe: rank > 300
- **DEVELOPING**
  - Ambitious: rank ≤ 250
  - Target: rank ≤ 500
  - Safe: rank > 500
- **BASIC**
  - Ambitious: rank ≤ 400
  - Target: rank ≤ 800
  - Safe: rank > 800

These numbers are approximate and encoded in the `switch` in `classifyBucket`.

---

## 4. Quotas and Final Selection

In **PREDICT** mode, the user specifies `N` universities they plan to apply to.

We compute target **quotas**:

- If N ≤ 5:
  - 20% Ambitious, 60% Target, 20% Safe
- If N ≤ 10:
  - 30% Ambitious, 50% Target, 20% Safe
- If N ≤ 15:
  - 25% Ambitious, 50% Target, 25% Safe
- If N > 15:
  - 20% Ambitious, 40% Target, 40% Safe

The system:

1. Sorts universities inside each bucket by:
   - Global rank (ascending)
   - Name (alphabetical)
2. Takes up to the quota from Ambitious, Target, Safe buckets.
3. If total picked < N, fills remaining slots from leftover universities in order of rank.
4. If total somehow exceeds N (rounding), trims extra from Safe → Target → Ambitious, in that order.

This yields a **fixed-size, realistic shortlist** rather than dumping the entire dataset.

