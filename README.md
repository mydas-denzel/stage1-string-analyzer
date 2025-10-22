# Stage1 String Analyzer

## 📘 Overview
**Stage1 String Analyzer** is a Java Spring Boot application designed to analyze and process text strings.  
It exposes RESTful APIs for submitting strings, performing character and linguistic analysis, and storing the results in a **MySQL database**.  
This project was developed as part of the **HNG Internship (Stage 1)** challenge.

---

## 🧩 Features
- RESTful API endpoint for analyzing strings  
- Character frequency and pattern analysis  
- Natural language parsing (via `NaturalLanguageParserService`)  
- MySQL database integration via Spring Data JPA  
- JSON-based API responses  
- Maven wrapper for easy setup and build  
- Unit tests included for validation  

---

## 🗂 Project Structure
```
stage1 string analyzer/
├── src/
│ ├── main/java/com/hng/stage1_string_analyzer/
│ │ ├── Stage1StringAnalyzerApplication.java # Main Spring Boot entry point
│ │ ├── controller/StringController.java # REST endpoints for string analysis
│ │ ├── model/AnalyzedString.java # Entity class for storing analysis results
│ │ ├── model/CharacterFrequencyMapConverter.java # Converts maps to JSON for persistence
│ │ ├── repository/AnalyzedStringRepository.java # JPA repository for database access
│ │ ├── service/StringAnalysisService.java # Core string analysis logic
│ │ └── service/NaturalLanguageParserService.java # NLP service for linguistic parsing
│ ├── main/resources/application.properties # Database and app configuration
│ └── test/java/... # Unit tests
├── data/ # (Optional) database-related artifacts
├── pom.xml # Maven build configuration
└── mvnw / mvnw.cmd # Maven wrapper scripts
```
---

## ⚙️ Requirements
- **Java 17** or higher  
- **Maven 3.8+**  
- **MySQL 8.x** (or compatible server)

---

## 🚀 Setup and Installation

### 1. Clone or Extract the Project
Clone from Git:
```
git clone gihub.com/mydas-denzel/stage1-string-analyzer.git
cd stage1_string_analyzer
```
2. Configure Database
The project connects to a MySQL database by default.
Edit the file at:

`src/main/resources/application.properties`
Update the credentials to match your own MySQL setup:

properties
```
spring.datasource.url=jdbc:mysql://localhost:3306/string_analyzer
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```
⚠️ Important:
Do not commit real credentials (like production usernames or passwords) to public repositories.
Use environment variables or a config service instead.

3. Build the Application
bash
Copy code
./mvnw clean install
4. Run the Application
bash
Copy code
./mvnw spring-boot:run
Once started, the application will be available at:

`http://localhost:8080`
📡 API Usage
POST /api/analyze
Analyzes a given string and returns its statistics.

Request Example:

```json

{
  "input": "Hello, World!"
}
```
Response Example:

```json
{
  "input": "Hello, World!",
  "length": 13,
  "characterFrequency": {
    "H": 1,
    "e": 1,
    "l": 3,
    "o": 2,
    ",": 1,
    " ": 1,
    "W": 1,
    "r": 1,
    "d": 1,
    "!": 1
  }
}
```

### Core Components
Component	Description
- StringAnalysisService	Core logic for counting, mapping, and analyzing text data.
- NaturalLanguageParserService	(Optional) Provides language-aware text parsing.
- AnalyzedStringRepository	Handles CRUD operations on the MySQL database.
- StringController	Defines API routes and request handling.

🧪 Running Tests
You can run all unit and integration tests using:

bash
Copy code
./mvnw test
