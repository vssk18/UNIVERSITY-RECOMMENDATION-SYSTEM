# Changelog

All notable changes to this project will be documented here.

## v2.0.0

- Introduced **Profile Scoring (0–100)** with:
  - GPA, IELTS, GRE, Research, Experience.
- Added **Profile Tiers**:
  - EXCEPTIONAL, VERY_STRONG, STRONG, GOOD, DEVELOPING, BASIC.
- Implemented **tier-based bucket logic**:
  - Ambitious / Target / Safe with realistic global rank thresholds.
  - Hard rule: Top-15 global = always Ambitious.
- Implemented **quota-based selection** for PREDICT mode:
  - Distributes user’s target count across Ambitious/Target/Safe.
- Added dataset ZIP packaging:
  - `universities_dataset_v1.zip` under `data/`.
- Added `docs/` with:
  - SYSTEM_OVERVIEW, PROFILE_SCORING, BUCKET_LOGIC, DATASET_FORMAT, CONTRIBUTING, CHANGELOG.
- Added GitHub Actions workflow (`java-ci.yml`) to ensure the project compiles on every push/PR.
- Refined README to be portfolio-ready and onboarding-friendly.

