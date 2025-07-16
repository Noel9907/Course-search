import json
import os

def fix_sample_courses_json():
    """
    Simple script to fix date format in sample-courses.json
    Changes 'YYYY-MM-DDTHH:mm' to 'YYYY-MM-DDTHH:mm:ss'
    """

    # Try different possible locations for the file
    possible_paths = [
        "sample-courses.json",
        "src/main/resources/sample-courses.json",
        "resources/sample-courses.json"
    ]

    json_file_path = None
    for path in possible_paths:
        if os.path.exists(path):
            json_file_path = path
            break

    if not json_file_path:
        print("❌ sample-courses.json not found in expected locations:")
        for path in possible_paths:
            print(f"   - {path}")
        print("\nPlease make sure you're running this script from the correct directory.")
        return

    try:
        # Read the JSON file
        with open(json_file_path, 'r', encoding='utf-8') as file:
            data = json.load(file)

        # Fix nextSessionDate in each course
        fixed_count = 0
        for course in data:
            if 'nextSessionDate' in course:
                date_value = course['nextSessionDate']
                # Check if it's in the problematic format (missing seconds)
                if isinstance(date_value, str) and date_value.endswith(':00') == False and len(date_value) == 16:
                    course['nextSessionDate'] = date_value + ':00'
                    fixed_count += 1
                    print(f"✅ Fixed: {date_value} → {course['nextSessionDate']}")

        if fixed_count > 0:
            # Create backup
            backup_path = json_file_path.replace('.json', '_backup.json')
            with open(backup_path, 'w', encoding='utf-8') as backup_file:
                json.dump(data, backup_file, indent=2, ensure_ascii=False)
            print(f"📁 Backup created: {backup_path}")

            # Write fixed data back
            with open(json_file_path, 'w', encoding='utf-8') as file:
                json.dump(data, file, indent=2, ensure_ascii=False)

            print(f"✅ Successfully fixed {fixed_count} dates in {json_file_path}")
            print("🚀 Your Spring Boot application should now start without errors!")
        else:
            print("✅ No dates needed fixing. All dates are already in correct format.")

    except Exception as e:
        print(f"❌ Error: {e}")

if __name__ == "__main__":
    print("🔧 Fixing sample-courses.json date format...")
    print("=" * 50)
    fix_sample_courses_json()