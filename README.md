# 🎓 UNIVERSITY RECOMMENDATION SYSTEM

Console-based university shortlisting tool that reads a curated dataset of ~470 universities, applies filters and scoring rules, and produces either:

- A **full filtered list** of matching universities, or
- A **split of Ambitious / Target / Safe** options based on your profile.

The system is designed to be reproducible:
- Data lives in `data/universities_complete.csv`
- Logic is implemented in Java with a clear model
- Each run writes a transcript into `runs/`

## Features

- Choose one course type: **STEM, MBA, MIS, Health, Arts, Finance**
- Choose up to **3 regions**: USA, Europe, Asia, Middle East, Australia
- Two modes:
  - **Predict**: input CGPA, IELTS, GRE, experience, research → get Ambitious/Target/Safe
  - **View All**: simple filters → list all matching universities
- Each row shows:
  - University name, country, program
  - Minimum IELTS, GRE requirement
  - Estimated total cost
  - **Course-specific rank** within your selected course + regions
  - Short reasons (rank, labs, budget fit)

## Dataset

The dataset was curated offline and stored as:

- `data/universities_complete.csv` – main working file
- `data/universities_dataset_v1.zip` – archived copy of the dataset

Schema (simplified):

- `name, country, city, region`
- `course_type` (STEM, MBA, MIS, HEALTH, ARTS, FINANCE)
- `program, degree`
- `ielts_min, gre_required, on_campus`
- `tuition_usd, total_usd`
- `global_rank, subject_rank`
- `has_research_lab, specialization`

## Usage

```bash
cd ~/Projects/UNIVERSITY-RECOMMENDATION-SYSTEM
make run

