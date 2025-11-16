# Quick Start Guide - University Dataset

## What You Have

You now have a complete university dataset with **247 universities** covering:
- 🇺🇸 USA: 88 universities
- 🇪🇺 Europe: 62 universities  
- 🌏 Asia: 49 universities
- 🇦🇺 Australia: 23 universities
- 🌍 Middle East: 25 universities

## Files Included

1. **universities_dataset.csv** - The main dataset (247 rows)
2. **DATASET_README.md** - Complete documentation
3. **DATASET_STATISTICS.md** - Detailed statistics and insights
4. **SAMPLE_UNIVERSITIES.md** - Sample data from each region
5. **validate_dataset.py** - Python script to validate the data
6. **This file** - Quick start guide

## How to Use the Dataset

### Option 1: Open in Excel/Sheets
1. Open `universities_dataset.csv` in Microsoft Excel or Google Sheets
2. Use filters to explore by region, course type, cost, etc.
3. Sort by rankings or costs

### Option 2: Python/Pandas
```python
import pandas as pd

# Load the dataset
df = pd.read_csv('universities_dataset.csv')

# View basic info
print(f"Total universities: {len(df)}")
print(df.head())

# Filter examples
usa_stem = df[(df['region'] == 'USA') & (df['course_type'] == 'STEM')]
affordable = df[df['total_usd'] < 50000]
top_100 = df[df['global_rank'] <= 100]

# Sort by ranking
best_ranked = df.sort_values('global_rank').head(20)
```

### Option 3: Java (for your project)
```java
// Read CSV and parse
List<University> universities = loadCSV("universities_dataset.csv");

// Filter by criteria
List<University> filtered = universities.stream()
    .filter(u -> u.region.equals("USA"))
    .filter(u -> u.courseType.equals("STEM"))
    .filter(u -> u.totalUsd <= 70000)
    .collect(Collectors.toList());
```

## Common Queries

### Find Affordable STEM Programs
```
Filter: course_type = "STEM" AND total_usd < 40000
Result: ~65 universities (mostly Europe, Asia, Middle East)
```

### Find Top 50 Universities for CS
```
Filter: course_type = "STEM" AND global_rank <= 50
Result: 25 universities across USA, Europe, Asia
```

### Find MBA Programs in Asia
```
Filter: region = "Asia" AND course_type = "MBA"
Result: 9 universities
```

### Find Programs Without GRE Required
```
Filter: gre_required = "optional" OR gre_required = "false"
Result: 245 universities (99% of dataset)
```

## Data Verification

Run the validator to check data quality:
```bash
python3 validate_dataset.py universities_dataset.csv
```

## Column Reference (Quick)

| Column | Values | Example |
|--------|--------|---------|
| region | USA, Europe, Asia, Australia, MiddleEast | "USA" |
| course_type | STEM, MBA, MIS, ARTS | "STEM" |
| degree | MS, MBA, MA, MEng, MASc | "MS" |
| gre_required | true, false, optional | "optional" |
| ielts_min | 6.5 or 7.0 typically | 6.5 |
| global_rank | 1 to 800+ | 25 |
| total_usd | Cost range: $8,000 - $145,000 | 65000 |

## Next Steps for Your Java Project

1. **Create Data Model**
   ```java
   class University {
       String name, country, region, city;
       String courseType, program, degree;
       int globalRank, subjectRank;
       double ieltsMin;
       boolean greRequired, onCampus, hasResearchLab;
       String securityFocus;
       int tuitionUsd, totalUsd;
   }
   ```

2. **Load CSV**
   - Read the CSV file
   - Parse each row into University objects
   - Store in ArrayList<University>

3. **Implement Filters**
   - Filter by region
   - Filter by course type
   - Filter by budget
   - Filter by IELTS score

4. **Implement Scoring**
   - Rank by global_rank
   - Score by security_focus tags
   - Consider cost vs ranking
   - Create Ambitious/Target/Safe buckets

5. **Create CLI**
   - Ask user for preferences
   - Show filtered results
   - Save recommendations to file

## Tips for Your Application

### Ambitious/Target/Safe Logic
```
High Profile (GPA 8.5+, IELTS 7.0+, Research):
  - Ambitious: Rank 1-50
  - Target: Rank 50-150  
  - Safe: Rank 150+

Mid Profile (GPA 7.5-8.5, IELTS 6.5-7.0):
  - Ambitious: Rank 50-100
  - Target: Rank 100-250
  - Safe: Rank 250+

Developing Profile (GPA 7.0-7.5, IELTS 6.5):
  - Ambitious: Rank 100-200
  - Target: Rank 200-400
  - Safe: Rank 400+
```

### Budget-Conscious Recommendations
- Europe (Germany): Many €0 tuition programs
- Asia (India): IITs at $8,000 total
- Middle East: Some zero-tuition options
- USA: Public universities $50,000-70,000

### Research-Focused
All 247 programs have research labs, but focus on:
- security_focus containing "systems" or "security"
- Top 100 ranked universities
- Universities with multiple specialization tags

## Support

For questions or issues:
- Check DATASET_README.md for detailed documentation
- Check DATASET_STATISTICS.md for insights
- Run validate_dataset.py to verify data integrity

## License

Educational use only. Always verify information with official university sources.

---

**Ready to build your University Recommendation System!** 🎓

The dataset is validated, documented, and ready for your Java application.
