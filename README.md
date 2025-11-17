<div align="center">
<a href="#readme"><img src="https://img.shields.io/badge/-README-24292f?style=flat&logo=book&logoColor=white" alt="README" /></a>
&nbsp;&nbsp;<a href="docs/CONTRIBUTING.md"><img src="https://img.shields.io/badge/-Contributing-24292f?style=flat&logo=group&logoColor=white" alt="Contributing" /></a>
&nbsp;&nbsp;<a href="LICENSE"><img src="https://img.shields.io/badge/-MIT%20License-24292f?style=flat&logo=scale&logoColor=white" alt="MIT License" /></a>
&nbsp;&nbsp;<a href="SECURITY.md"><img src="https://img.shields.io/badge/-Security-24292f?style=flat&logo=shield&logoColor=white" alt="Security" /></a>
&nbsp;&nbsp;<a href="CODE_OF_CONDUCT.md"><img src="https://img.shields.io/badge/-Code%20of%20Conduct-24292f?style=flat&logo=heart&logoColor=white" alt="Code of Conduct" /></a>

<h1>🎓 UNIVERSITY RECOMMENDATION SYSTEM</h1>
</div>

![Java](https://img.shields.io/badge/Language-Java-blue)
![Python](https://img.shields.io/badge/Language-Python-yellow)
![C++](https://img.shields.io/badge/Language-C++-blue)
![License: MIT](https://img.shields.io/badge/License-MIT-brightgreen)
![Last Commit](https://img.shields.io/github/last-commit/vssk18/UNIVERSITY-RECOMMENDATION-SYSTEM)

---

## 🏷️ Overview

A robust, reproducible command line tool for university admissions decision making.
Predicts “Ambitious / Target / Safe” universities using a transparent profile scoring system and a well-documented global dataset.

---

## ✨ Features

- **Profile scoring:** GPA, IELTS, GRE, experience, research, budget → 6-level tier
- **Ambitious / Target / Safe buckets:** Based on applicant tier and global ranking logic
- **Flexible filters:** Region, course, cost, program type selection
- **Predict & View-all modes:** Bucketed shortlist or full matching output
- **CLI logs:** Every run saved in `/runs` for reproducibility
- **Dataset scripts:** Python validator and C++ summary tool
- **Full documentation:** All logic and formulas available in `/docs/`

---

## 💻 Tech Stack

| Language   | Purpose                    |
|------------|----------------------------|
| Java       | CLI, scoring, bucket logic |
| Python     | Validation & dataset tools |
| C++        | Dataset summaries          |
| Makefile   | Build/run helpers          |

---

## ⚡ Quick Start

**Requirements:**
- Java 11+  
- Python 3.7+ (dataset/script support)

```bash
git clone https://github.com/vssk18/UNIVERSITY-RECOMMENDATION-SYSTEM.git
cd UNIVERSITY-RECOMMENDATION-SYSTEM
make run
```

Manual build/run:
```bash
mkdir -p out
javac -d out src/model/*.java src/data/*.java src/logic/*.java src/cli/*.java
java -cp out cli.UniversityRecommendationSystem
```

Validate dataset:
```bash
python3 data/validate_dataset.py data/universities_complete.csv
```
---

## 🗂️ Project Structure

```text
UNIVERSITY-RECOMMENDATION-SYSTEM/
├── src/
│   ├── cli/
│   │   └── UniversityRecommendationSystem.java   # CLI entry point
│   ├── data/
│   │   └── UniversityRepository.java             # CSV loader and parser
│   ├── logic/
│   │   ├── EligibilityProfile.java               # User profile model
│   │   └── RecommendationEngine.java             # Scoring and buckets
│   └── model/
│       ├── CourseType.java                       # STEM, MBA, MIS, etc
│       ├── Region.java                           # USA, Europe, Asia, ...
│       └── University.java                       # Core university model
├── data/
│   ├── universities_complete.csv                 # Main dataset
│   ├── COMPLETE_DATASET_README.md                # Dataset background
│   ├── DATASET_SUMMARY.txt                       # Basic stats
│   ├── DATASET_STATISTICS.md                     # Deeper stats
│   ├── QUICK_START.md                            # Simple usage
│   └── validate_dataset.py                       # Consistency checks
├── docs/
│   ├── SYSTEM_OVERVIEW.md                        # High level flow
│   ├── PROFILE_SCORING.md                        # Detailed scoring formula
│   ├── BUCKET_LOGIC.md                           # Logic for bucket split
│   ├── DATASET_FORMAT.md                         # Column by column explanation
│   ├── CONTRIBUTING.md                           # Dev workflow
│   └── CHANGELOG.md                              # Version history
├── tools/
│   └── cpp_summary/                              # C++ dataset summary tool
├── runs/                                         # Saved logs per CLI run
├── .github/                                      # CI workflows, if enabled
├── LICENSE
├── Makefile
└── README.md
```
---

## 💡 How It Works

- **Input:** Interactive CLI (course/region/GPA/IELTS/GRE/exp/research/budget/#apps)
- **Profile scoring:** Weighted logic for 6-tier categorization ([docs/PROFILE_SCORING.md])
- **Bucket assignment:** Ambitious/Target/Safe determined by rank and tier ([docs/BUCKET_LOGIC.md])
- **Filtering:** All user constraints applied
- **Results:** Table with bucket, rank, notes; full log in `/runs`

---

## 📊 Dataset

- `universities_complete.csv` (470+ universities, multi-region)
- Documentation in `COMPLETE_DATASET_README.md`, `DATASET_FORMAT.md`
- Python script: `validate_dataset.py`
- Columns: name, country, region, course, ranking, IELTS, cost, research, campus, etc.

---

## 🤝 Contributing

- PR workflow ([docs/CONTRIBUTING.md])
- Add/expand dataset, logic, filters, features—open to all levels!

---

## 📄 License & Policies

MIT License ([LICENSE])  
[CODE_OF_CONDUCT.md] | [SECURITY.md]

---

## 👤 Credits & Contact

**Varanasi Sai Srinivasa Karthik**  
B.Tech CSE (Cybersecurity), GITAM University, Hyderabad  
📧 svaranas3@gitam.in | 📧 varanasikarthik44@gmail.com  
[GitHub: vssk18](https://github.com/vssk18)

---

_Built for real applicants, fully reproducible and ready for portfolio, research, or open-source contribution.