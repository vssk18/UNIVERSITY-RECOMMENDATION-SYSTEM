#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <map>

int main(int argc, char** argv) {
    std::string path = "data/universities_complete.csv";
    if (argc > 1) path = argv[1];

    std::ifstream in(path);
    if (!in) {
        std::cerr << "Could not open CSV: " << path << "\n";
        return 1;
    }

    std::string header;
    if (!std::getline(in, header)) {
        std::cerr << "Empty CSV\n";
        return 1;
    }

    std::vector<std::string> cols;
    {
        std::stringstream ss(header);
        std::string cell;
        while (std::getline(ss, cell, ',')) cols.push_back(cell);
    }

    auto findCol = [&](const std::string& name) {
        for (size_t i = 0; i < cols.size(); ++i) {
            if (cols[i] == name) return (int)i;
        }
        return -1;
    };

    int idxRegion = findCol("region");
    int idxCourse = findCol("course_type");
    if (idxRegion < 0 || idxCourse < 0) {
        std::cerr << "Missing region or course_type columns\n";
        return 1;
    }

    std::map<std::string, int> regionCounts;
    std::map<std::string, int> courseCounts;

    std::string line;
    int total = 0;
    while (std::getline(in, line)) {
        if (line.empty()) continue;
        std::stringstream ss(line);
        std::string cell;
        std::vector<std::string> row;
        while (std::getline(ss, cell, ',')) row.push_back(cell);
        if ((int)row.size() <= std::max(idxRegion, idxCourse)) continue;
        std::string r = row[idxRegion];
        std::string c = row[idxCourse];
        regionCounts[r]++;
        courseCounts[c]++;
        total++;
    }

    std::cout << "Total universities: " << total << "\n\n";

    std::cout << "By region:\n";
    for (auto& [r, cnt] : regionCounts) {
        std::cout << "  " << r << " : " << cnt << "\n";
    }

    std::cout << "\nBy course_type:\n";
    for (auto& [c, cnt] : courseCounts) {
        std::cout << "  " << c << " : " << cnt << "\n";
    }
    return 0;
}
