# System Overview

The **University Recommendation System** is a Java-based command-line application that helps students plan realistic application lists for international university admissions.

## Goals

- Provide **transparent, realistic guidance** instead of arbitrary "rank-based" sorting.
- Keep logic aligned with **holistic admissions** (profile tiers, not just marks).
- Make it easy to extend:
  - Add new universities
  - Tune scoring and tier boundaries
  - Add new course types or regions

## Core Components

- `model.University`
  - Represents a single university/program row from the dataset.
  - Encapsulates:
    - Core identifiers
    - Region and course type
    - Rankings
    - IELTS/GRE requirements
    - Cost estimates
    - Flags like `hasResearchLab`, `isOnCampus`.

- `data.UniversityRepository`
  - Loads `data/universities_complete.csv`.
  - Parses CSV safely (handles quoted commas).
  - Builds `University` objects and exposes them as a list.

- `logic.EligibilityProfile`
  - Captures user inputs:
    - GPA (10-point scale)
    - IELTS
    - GRE (optional)
    - Work experience
    - Research papers
    - Budget
    - Target course type
    - Regions
  - Computes a **profile score (0–100)**.
  - Maps the score to a **Profile Tier**.

- `logic.RecommendationEngine`
  - Heart of the system.
  - Filters universities by:
    - Course type
    - Regions
    - Min IELTS requirement
    - Budget ceiling (if set)
  - Classifies each eligible university into:
    - Ambitious
    - Target
    - Safe
  - Respects the user’s requested number of universities in **PREDICT** mode.
  - Provides a **VIEW_ALL** mode that shows all matching universities, still bucketed.

- `cli.UniversityRecommendationSystem`
  - Interactive CLI front-end.
  - Guides the user step-by-step:
    - Course type
    - Regions
    - Mode (PREDICT vs VIEW_ALL)
    - Profile details
  - Prints formatted tables.
  - Saves run logs into `runs/` for future reference.

- `tools/cpp_summary`
  - Optional C++ helper.
  - Summarizes dataset by region and course type from the CSV.
  - Useful for quick consistency checks.

## Data Flow

1. CSV loaded → `UniversityRepository`.
2. User answers questions → `EligibilityProfile`.
3. Profile → score → `ProfileTier`.
4. `RecommendationEngine`:
   - Filters by course type + region.
   - Applies IELTS and budget filters.
   - Assigns each university to Ambitious / Target / Safe based on:
     - Global rank.
     - Tier boundaries.
     - Hard rule for top-15.
   - In PREDICT mode:
     - Computes desired quotas for each bucket.
     - Picks universities from each bucket respecting quotas.
5. CLI prints results and logs a run file under `runs/`.

