# 🎓 UNIVERSITY RECOMMENDATION SYSTEM

A **Java-based, data-driven CLI tool** that helps students plan realistic university applications by suggesting **Ambitious / Target / Safe** schools based on their profile, preferences, and a curated dataset of **470 universities** across the world.

This is not a random “sorting by ranking” script. It encodes **realistic admission logic**:

- Profile scoring on a **0–100 scale**
- 6 profile tiers (Exceptional → Basic)
- Dynamic **Ambitious / Target / Safe** splits
- Hard checks for **IELTS** and optional **budget**
- Region and course-type filters
- Transparent warnings and notes (e.g., “Top-15 are always Ambitious”)

---

## 🧠 Core Idea

Given:

- Your **course type** (STEM / MBA / MIS / Health / Arts / Finance)
- Your **regions** (USA / Europe / Asia / Middle East / Australia)
- Your profile:
  - CGPA on 10-point scale
  - IELTS overall
  - GRE (optional)
  - Work experience (years)
  - Number of research papers
  - Budget ceiling (USD, optional)
- Desired number of universities (e.g., 12, 15)

The system:

1. Scores your profile on a **0–100 scale**.
2. Classifies you into a **Profile Tier**.
3. Filters universities by:
   - Course type
   - Region
   - Minimum IELTS
   - Budget (if given)
4. Classifies each matching university into:
   - **Ambitious** (reach)
   - **Target** (balanced fit)
   - **Safe** (higher odds, still not guaranteed)
5. Respects your requested count and gives a **split** like:
   - Ambitious: 3
   - Target: 8
   - Safe: 4

You also get detailed reasons for each university (rank, research lab, on-campus, bucket).

---

## 📂 Project Structure

```text
UNIVERSITY-RECOMMENDATION-SYSTEM/
├─ data/
│  ├─ universities_complete.csv        # Main dataset (470 rows)
│  ├─ universities_dataset_v1.zip      # Zipped dataset + docs
│  ├─ START_HERE.md
│  ├─ COMPLETE_DATASET_README.md
│  ├─ DATASET_SUMMARY.txt
│  ├─ DATASET_STATISTICS.md
│  ├─ QUICK_START.md
│  └─ validate_dataset.py
├─ src/
│  ├─ model/
│  │  ├─ CourseType.java               # Enum: STEM / MBA / MIS / Health / Arts / Finance
│  │  ├─ Region.java                   # Enum: USA / EUROPE / ASIA / MIDDLE_EAST / AUSTRALIA
│  │  └─ University.java               # Core university model
│  ├─ data/
│  │  └─ UniversityRepository.java     # CSV loader + parser
│  ├─ logic/
│  │  ├─ EligibilityProfile.java       # Holds user profile + scoring logic
│  │  └─ RecommendationEngine.java     # Main recommendation and bucketing logic
│  └─ cli/
│     └─ UniversityRecommendationSystem.java  # Interactive CLI entry point
├─ tools/
│  └─ cpp_summary/
│     ├─ summary.cpp                   # C++ summary tool for dataset stats
│     └─ Makefile
├─ runs/
│  └─ ...                              # Saved logs per run (timestamped)
├─ docs/
│  ├─ SYSTEM_OVERVIEW.md
│  ├─ PROFILE_SCORING.md
│  ├─ BUCKET_LOGIC.md
│  ├─ DATASET_FORMAT.md
│  ├─ CONTRIBUTING.md
│  └─ CHANGELOG.md
├─ .github/
│  └─ workflows/
│     └─ java-ci.yml                   # GitHub Actions CI: compile check
├─ Makefile                            # Local build / run helper
├─ LICENSE                             # MIT License
└─ README.md                           # You are here