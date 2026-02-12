# 🎓 Smart Student ID Finder

An intelligent automation tool that finds student IDs using **binary search** and Arabic alphabetical ordering. **75x faster** than checking all IDs sequentially.

![Java](https://img.shields.io/badge/Java-24-orange)
![Selenium](https://img.shields.io/badge/Selenium-4.16.1-green)
![License](https://img.shields.io/badge/license-MIT-blue)

## ✨ Features

-  **Smart Binary Search** - Finds students in 10-15 attempts (vs 900 linear)
-  **Arabic Support** - Full Arabic alphabet with normalization
-  **Name Matching** - Matches first + last name only
-  **Screenshots** - Optional PNG captures
-  **Reports** - Automatic text files with search statistics
-  **Speed** - 50-60x faster than linear search

## 🚀 Quick Start

### Prerequisites
- Java JDK 11+
- Maven
- Google Chrome

### Installation

1. **Clone the repo**
```bash
   git clone https://github.com/bishoyhanyhalim/Automation_Finder.git
   cd Automation_Finder
```

2. **Add dependencies to `pom.xml`**
```xml
   <dependencies>
       <dependency>
           <groupId>org.seleniumhq.selenium</groupId>
           <artifactId>selenium-java</artifactId>
           <version>4.16.1</version>
       </dependency>
       <dependency>
           <groupId>commons-io</groupId>
           <artifactId>commons-io</artifactId>
           <version>2.15.1</version>
       </dependency>
   </dependencies>
```

3. **Configure website URL**
```java
   // Line ~200 in StudentIDFinder.java
   String websiteURL = "YOUR_WEBSITE_URL";
```

4. **Run**
```bash
   mvn clean install
   java -jar target/student-finder.jar
```

## 💻 Usage
```
Enter student first name (الاسم الأول): محمد
Enter student last name (الاسم الثاني): أحمد

Using binary search algorithm...

Attempt 1: ID 45450 | Found: زينب فاطمة | Range: 45001-45449
Attempt 2: ID 45225 | Found: سارة حسن | Range: 45001-45224
Attempt 3: ID 45112 | Found: خالد علي | Range: 45113-45224
Attempt 4: ID 45168 | Found: محمد أحمد علي | ✓ MATCH!

╔════════════════════════════════════════════════╗
║           ✓ STUDENT FOUND!                     ║
╚════════════════════════════════════════════════╝

Student Name: محمد أحمد علي حسن
Student ID: 45168
Attempts: 4
Time: 6 seconds
Efficiency: 225x faster than linear search
```

## 🔧 How It Works

### Binary Search Algorithm
```
1. Start at middle ID (45450)
2. Compare found name with target alphabetically
3. If found name < target → search upper half
4. If found name > target → search lower half
5. Repeat until match found
```


### Name Matching
- **Input:** "محمد أحمد"
- **Website:** "محمد أحمد علي حسن" → ✓ **MATCH** (first two names)
- **Website:** "محمد حسن" → ✗ NO MATCH

## 📊 Performance

| Method | Attempts | Time | Improvement |
|--------|----------|------|-------------|
| Linear Search | ~450 | 7-8 min | - |
| Binary Search | 10-15 | 15-30 sec | **30-45x faster** |

##  Output Structure
```
project/
├── screenshots/
│   └── Student_45168_محمد_أحمد_2026-02-12_14-30-15.png
├── results/
│   └── Student_Info_45168_2026-02-12_14-30-15.txt
└── src/
```

##  Configuration

Update XPaths if your website structure differs:
```java
// Student ID textbox
By.xpath("//*[@id=\"contentdata\"]/tbody/tr[5]/td/form/div/input[1]")

// Submit button
By.xpath("//*[@id=\"submit1\"]")

// Student name (result page)
By.xpath("//*[@id=\"rsval\"]/table/tbody/tr[2]/th")

// Student ID (result page)
By.xpath("//*[@id=\"rsval\"]/table/tbody/tr[3]/td[2]")
```

##  Troubleshooting

| Issue | Solution |
|-------|----------|
| Element not found | Update XPath selectors |
| Student not found | Check name spelling/order |
| Slow search | Reduce `Thread.sleep()` value |
| Screenshot failed | Check `commons-io` dependency |


##  Author

**Bishoy Hany Halim**
- GitHub: [@bishoyhanyhalim](https://github.com/bishoyhanyhalim)
- Email: bishoyhanyhalim@gmail.com

---
