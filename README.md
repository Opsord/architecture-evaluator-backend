# Backend - Architecture Evaluator for Spring Boot Applications

## Description
This is the backend service for the Architecture Evaluator, a web application designed to assess the architecture of Java applications built with Spring Boot. It provides automated analysis of maintainability-related metrics and exposes REST APIs for data processing and visualization.

## Features
- **Automated Architectural Analysis:**
  - Detects design principle violations and evaluates layered architectures.
  - Calculates key maintainability metrics such as cyclomatic complexity, coupling, and cohesion.
- **REST API for Data Access:**
  - Provides endpoints to retrieve analysis results and manage evaluations.
- **Integration with Git Repositories:**
  - Fetches and analyzes source code from version control systems.
- **Modular and Extensible:**
  - Supports adding new evaluation criteria and metrics.

## Technologies Used
- **Java 17+**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **SonarQube (for code quality analysis)**
- **Docker (for containerization, optional)**
- **Maven (for dependency management and build process)**

## Installation and Usage
### Prerequisites
- **Java 17+**
- **Maven**
- **Docker** (optional, for deployment)

### Installation
1. Clone the repository:
   ```sh
   git clone https://github.com/user/repo.git
   cd repo/backend
   ```
2. Build and run the backend:
   ```sh
   mvn spring-boot:run
   ```
3. The backend will be available at `http://localhost:8080`

## API Endpoints
- `GET /api/metrics` - Retrieve calculated architectural metrics.
- `POST /api/analyze` - Trigger a new analysis of a given project.
- `GET /api/reports` - Fetch historical reports of evaluations.


## License
This project is licensed under the MIT License. See the `LICENSE` file for more details.
