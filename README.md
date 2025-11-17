# 🎓 UNIVERSITY RECOMMENDATION SYSTEM

A **Java + CSV** based, explainable university recommender that:

- Reads a **curated dataset** of ~470 universities (`data/universities_complete.csv`)
- Lets the user choose a **course type** (STEM, MBA, MIS, Health, Arts, Finance)
- Filters by **regions** (USA, Europe, Asia, Middle East, Australia)
- Asks for **CGPA, IELTS, GRE, experience, research, budget**
- Either:
  - **Predicts Ambitious / Target / Safe** split, **or**
  - **Lists all matching universities** without prediction
- Saves a **run log** for reproducibility in `runs/`
- Includes a small **C++ stats tool** to summarise the dataset

This is a **console application** designed as a clean, reproducible utility that you can show in interviews and link from your SOP / resume.

> ⚠️ **Important:** This tool uses a curated, static dataset for educational and planning purposes.  
> It **does not** guarantee admission and **does not** fetch live rankings or cut-offs.  
> Always verify official requirements on each university’s website.

---

## 🗂️ Repository layout

```text
UNIVERSITY-RECOMMENDATION-SYSTEM/
├─ src/
│  ├─ model/
│  │  ├─ CourseType.java          # Enum: STEM, MBA, MIS, Health, Arts, Finance
│  │  ├─ Region.java              # Enum: USA, Europe, Asia, MiddleEast, Australia
│  │  └─ University.java          # University record + parsed fields
│  ├─ data/
│  │  └─ UniversityRepository.java# CSV loader with header→field mapping
│  ├─ logic/
│  │  ├─ EligibilityProfile.java  # Normalised profile (CGPA, IELTS, GRE, XP, papers, budget)
│  │  └─ RecommendationEngine.java# Filter + scoring + Ambitious/Target/Safe split
│  └─ cli/
│     └─ UniversityRecommendationSystem.java  # CLI flow + pretty-printed tables + run logs
│
├─ data/
│  ├─ universities_complete.csv   # Main dataset (470 rows, 1 row = 1 program)
│  ├─ START_HERE.md               # High-level dataset overview (from data package)
│  ├─ QUICK_START.md              # How to plug dataset into tools
│  ├─ COMPLETE_DATASET_README.md  # Detailed field-by-field description
│  ├─ DATASET_STATISTICS.md       # Counts per region / course type
│  ├─ DATASET_SUMMARY.txt         # Human-readable summary
│  └─ validate_dataset.py         # Python validator (optional helper)
│
├─ tools/
│  └─ cpp_summary/
│     ├─ summary.cpp              # C++ tool summarising region / course counts
│     └─ Makefile                 # `make run` → build + run on universities_complete.csv
│
├─ runs/
│  └─ .gitkeep                    # Actual run logs are git-ignored
│
├─ Makefile                       # `make run` and `make quick-demo`
├─ README.md
├─ LICENSE
└─ .gitignore
