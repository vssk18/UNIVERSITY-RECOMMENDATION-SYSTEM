# Profile Scoring

The system converts user inputs into a **0–100 profile score**, then maps that score to a **Profile Tier**.

## Score Components

Total = 100 points:

- **GPA (10-point scale): 40 points**
- **IELTS: 25 points**
- **GRE: 20 points**
- **Research papers: 10 points**
- **Work experience: 5 points**

The exact formula is implemented in `logic/EligibilityProfile.java`.

### 1. GPA (0–40)

- We map GPA from 0.0–10.0 to 0–40 linearly.
- Example:
  - GPA 8.0 → 32/40
  - GPA 9.0 → 36/40
  - GPA 7.0 → 28/40

### 2. IELTS (0–25)

- IELTS 6.0 is considered a **minimum** for serious international applications.
- IELTS 7.5–8.0 is treated as very strong.
- We map 5.0–9.0 to 0–25 with a non-linear bias favoring higher scores.

### 3. GRE (0–20, optional)

- If user has no GRE, they score 0 here, but are not blocked.
- If provided:
  - Quant and Verbal are normalized from 130–170 range.
  - More weight is usually given to Quant for STEM.
- Very high scores (e.g. 330+) yield close to 20/20.

### 4. Research Papers (0–10)

- 0 papers → 0 points
- 1 paper → modest boost
- 2+ papers → higher points, capped at 10.
- This reflects the advantage of **research exposure**, not just marks.

### 5. Work Experience (0–5)

- Especially helpful for:
  - MBA / MIS
  - Industry-oriented programs
- Years are capped, with decreasing marginal returns (2–3+ years already strong).

---

## Profile Tiers

Once we compute the 0–100 score:

- **EXCEPTIONAL**: ≥ 90
- **VERY_STRONG**: ≥ 80
- **STRONG**: ≥ 70
- **GOOD**: ≥ 60
- **DEVELOPING**: ≥ 45
- **BASIC**: < 45

These tiers are used by `RecommendationEngine` to decide which global rank ranges count as Ambitious / Target / Safe for that user.

