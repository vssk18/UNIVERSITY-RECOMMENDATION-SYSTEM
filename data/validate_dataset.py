#!/usr/bin/env python3
"""
University Dataset Validator
Checks the dataset for completeness, consistency, and potential issues.
"""

import csv
import sys

def validate_dataset(filename):
    """Validate the university dataset CSV file"""
    
    print("=" * 80)
    print("UNIVERSITY DATASET VALIDATOR")
    print("=" * 80)
    print()
    
    errors = []
    warnings = []
    stats = {
        'total': 0,
        'regions': {},
        'courses': {},
        'countries': set()
    }
    
    required_columns = [
        'name', 'country', 'region', 'city', 'course_type', 
        'program', 'degree', 'global_rank', 'subject_rank',
        'ielts_min', 'gre_required', 'on_campus', 
        'security_focus', 'has_research_lab', 'tuition_usd', 'total_usd'
    ]
    
    try:
        with open(filename, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            
            # Check headers
            headers = reader.fieldnames
            missing_cols = set(required_columns) - set(headers)
            if missing_cols:
                errors.append(f"Missing required columns: {missing_cols}")
                return errors, warnings, stats
            
            # Validate each row
            for i, row in enumerate(reader, start=2):  # start=2 because row 1 is header
                stats['total'] += 1
                
                # Check for empty required fields
                for col in required_columns:
                    if not row.get(col, '').strip():
                        if col not in ['security_focus']:  # security_focus can be empty
                            errors.append(f"Row {i}: Empty {col} for {row.get('name', 'UNKNOWN')}")
                
                # Validate region
                region = row.get('region', '')
                if region not in ['USA', 'Europe', 'Asia', 'Australia', 'MiddleEast']:
                    errors.append(f"Row {i}: Invalid region '{region}' for {row.get('name')}")
                stats['regions'][region] = stats['regions'].get(region, 0) + 1
                
                # Validate course type
                course = row.get('course_type', '')
                if course not in ['STEM', 'MBA', 'MIS', 'ARTS']:
                    warnings.append(f"Row {i}: Unusual course_type '{course}' for {row.get('name')}")
                stats['courses'][course] = stats['courses'].get(course, 0) + 1
                
                # Validate IELTS
                try:
                    ielts = float(row.get('ielts_min', 0))
                    if ielts < 0 or ielts > 9:
                        errors.append(f"Row {i}: Invalid IELTS {ielts} for {row.get('name')}")
                    elif ielts < 6.0:
                        warnings.append(f"Row {i}: Very low IELTS {ielts} for {row.get('name')}")
                except ValueError:
                    errors.append(f"Row {i}: Invalid IELTS value for {row.get('name')}")
                
                # Validate rankings
                try:
                    global_rank = int(row.get('global_rank', 0))
                    if global_rank < 0 or global_rank > 10000:
                        warnings.append(f"Row {i}: Unusual global_rank {global_rank} for {row.get('name')}")
                except ValueError:
                    errors.append(f"Row {i}: Invalid global_rank for {row.get('name')}")
                
                # Validate costs
                try:
                    tuition = int(row.get('tuition_usd', 0))
                    total = int(row.get('total_usd', 0))
                    if tuition > total:
                        warnings.append(f"Row {i}: Tuition > Total cost for {row.get('name')}")
                    if total > 200000:
                        warnings.append(f"Row {i}: Very high total cost ${total} for {row.get('name')}")
                except ValueError:
                    errors.append(f"Row {i}: Invalid cost values for {row.get('name')}")
                
                # Validate GRE
                gre = row.get('gre_required', '').lower()
                if gre not in ['true', 'false', 'optional']:
                    errors.append(f"Row {i}: Invalid gre_required '{gre}' for {row.get('name')}")
                
                # Track countries
                stats['countries'].add(row.get('country', ''))
        
        # Print results
        print("VALIDATION RESULTS")
        print("-" * 80)
        print()
        
        if errors:
            print(f"❌ ERRORS FOUND: {len(errors)}")
            print()
            for error in errors[:20]:  # Show first 20 errors
                print(f"  • {error}")
            if len(errors) > 20:
                print(f"  ... and {len(errors) - 20} more errors")
            print()
        else:
            print("✅ No errors found!")
            print()
        
        if warnings:
            print(f"⚠️  WARNINGS: {len(warnings)}")
            print()
            for warning in warnings[:10]:  # Show first 10 warnings
                print(f"  • {warning}")
            if len(warnings) > 10:
                print(f"  ... and {len(warnings) - 10} more warnings")
            print()
        else:
            print("✅ No warnings!")
            print()
        
        # Print statistics
        print("DATASET STATISTICS")
        print("-" * 80)
        print(f"Total Records: {stats['total']}")
        print(f"Total Countries: {len(stats['countries'])}")
        print()
        print("By Region:")
        for region, count in sorted(stats['regions'].items(), key=lambda x: x[1], reverse=True):
            print(f"  {region:15s}: {count:3d} universities")
        print()
        print("By Course Type:")
        for course, count in sorted(stats['courses'].items(), key=lambda x: x[1], reverse=True):
            print(f"  {course:15s}: {count:3d} programs")
        print()
        
        if not errors:
            print("=" * 80)
            print("✅ DATASET IS VALID!")
            print("=" * 80)
            return 0
        else:
            print("=" * 80)
            print("❌ DATASET HAS ERRORS - Please fix before using")
            print("=" * 80)
            return 1
            
    except FileNotFoundError:
        print(f"❌ ERROR: File '{filename}' not found!")
        return 1
    except Exception as e:
        print(f"❌ ERROR: {str(e)}")
        return 1

if __name__ == "__main__":
    if len(sys.argv) > 1:
        filename = sys.argv[1]
    else:
        filename = "universities_dataset.csv"
    
    exit_code = validate_dataset(filename)
    sys.exit(exit_code)
