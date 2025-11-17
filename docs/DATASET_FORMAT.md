# Dataset Format

Main file: `data/universities_complete.csv`

This CSV contains all universities used by the recommendation engine.

## Core Columns

A simplified view of the important columns:

- `name` — University name
- `country` — Country
- `region` — One of:
  - `USA`
  - `Europe`
  - `Asia`
  - `MiddleEast`
  - `Australia`
- `course_type` — One of:
  - `STEM`
  - `MBA`
  - `MIS`
  - `Health`
  - `Arts`
  - `Finance`
- `global_rank` — Overall world ranking (integer; lower is better)
- `subject_rank` — Course/subject-specific ranking (optional)
- `min_ielts` — Minimum IELTS overall required (approximate)
- `gre_required` — `Required`, `Optional`, or `NotRequired`
- `estimated_total_usd` — Approximate total cost in USD
- `has_research_lab` — `true` / `false`
- `on_campus` — `true` / `false`

There may be additional fields for richer analysis; see `data/COMPLETE_DATASET_README.md` and `data/DATASET_STATISTICS.md` for full details.

## Parsing Rules

`data.UniversityRepository`:

- Reads the header row.
- For each subsequent row:
  - Splits using a custom CSV parser that:
    - Respects quotes
    - Handles commas inside quoted cells
  - Builds a `Map<String, String>` from header → cell.
  - Passes it into `new University(rowMap)`.

`model.University`:

- Responsible for:
  - Parsing numbers safely with defaults.
  - Mapping `region` to `Region` enum.
  - Mapping `course_type` to `CourseType` enum.
  - Converting booleans like `has_research_lab` and `on_campus`.

---

## Zipped Dataset

`data/universities_dataset_v1.zip` contains:

- `universities_complete.csv`
- `START_HERE.md`
- `COMPLETE_DATASET_README.md`
- `DATASET_SUMMARY.txt`
- `DATASET_STATISTICS.md`
- `QUICK_START.md`
- `validate_dataset.py`

This makes it easy to reuse the dataset standalone or plug into other tools/projects.

