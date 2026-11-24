# CareerCompass AI – AI-Powered Career Development Platform

<p align="center">
  An intelligent career management platform that helps users analyze resumes, discover skill gaps, match job opportunities, prepare for interviews, generate personalized learning roadmaps, and manage job applications using AI.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?logo=openjdk" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?logo=springboot" />
  <img src="https://img.shields.io/badge/MySQL-8.4-blue?logo=mysql" />
  <img src="https://img.shields.io/badge/JavaScript-Vanilla-yellow?logo=javascript" />
  <img src="https://img.shields.io/badge/Docker-Compose-blue?logo=docker" />
</p>

---

## Overview

CareerCompass AI is a full-stack AI-powered career development platform designed to simplify the job preparation process.

It combines resume analysis, intelligent job matching, AI-powered interview preparation, personalized learning recommendations, and job application tracking into a single platform.

The application integrates Google's Gemini API for AI capabilities and ChromaDB for vector-based semantic search through a Retrieval-Augmented Generation (RAG) pipeline.

## Key Features

### 1. AI-Powered Resume Analyzer
- Upload resumes in PDF format.
- Extract resume content using Apache PDFBox.
- Generate AI-powered resume analysis using Google Gemini.
- Identify strengths, weaknesses, and improvement opportunities.
- Extract and organize technical skills.
- Generate resume analysis reports.

### 2. Intelligent Job Matching
- Create and manage job descriptions.
- Compare resume skills against job requirements.
- Generate job compatibility scores.
- Identify matched and missing skills.
- View detailed job matching results.

### 3. AI Interview Preparation
- Generate interview questions using AI.
- Prepare questions based on categories and difficulty levels.
- Generate suggested answers.
- Evaluate user-submitted answers.
- Receive AI-generated feedback and performance scores.

### 4. Personalized Learning Roadmaps
- Generate personalized learning roadmaps based on career goals.
- Organize learning objectives into structured phases.
- Generate actionable learning tasks.
- Track task completion and learning progress.

### 5. Job Application Tracker
- Create, view, update, and delete job applications.
- Track application statuses.
- Manage interview dates, job links, and notes.
- Monitor application progress through the dashboard.

### 6. RAG-Based AI Career Assistant
- Extract and split resume content into overlapping text chunks.
- Generate embeddings using the Gemini Embedding API.
- Store vector embeddings in ChromaDB.
- Perform semantic search to retrieve relevant resume information.
- Generate context-aware responses using Google Gemini.
- Retrieve user-specific resume information for grounded AI responses.

### 7. Secure Authentication and User Management
- User registration and login.
- JWT-based authentication.
- Password hashing.
- Protected REST API endpoints.
- User-specific data access and authorization.
- Profile and password management.

### 8. Interactive Dashboard
- View career management statistics.
- Monitor job application progress.
- Access resume analysis and interview preparation.
- Navigate between career management modules.

---

## Technology Stack

| Category | Technologies |
|---|---|
| Programming Language | Java 17, JavaScript |
| Backend Framework | Spring Boot 3.5.6 |
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Database | MySQL 8.4 |
| ORM | Spring Data JPA, Hibernate |
| Database Migration | Flyway |
| AI Integration | Google Gemini API |
| Vector Database | ChromaDB |
| AI Architecture | Retrieval-Augmented Generation (RAG) |
| Embeddings | Gemini Embedding API |
| Security | Spring Security, JWT |
| PDF Processing | Apache PDFBox |
| Build Tool | Maven |
| Containerization | Docker, Docker Compose |
| Testing | JUnit, Spring Boot Test, Playwright-based runtime validation |

---

## System Architecture

```text
                  USER
                    |
                    v
           FRONTEND APPLICATION
          HTML + CSS + JavaScript
                    |
                    v
             REST API REQUESTS
                    |
                    v
          SPRING BOOT BACKEND
                    |
           SPRING SECURITY + JWT
                    |
        +-----------+------------+
        |           |            |
        v           v            v
      MySQL       Gemini       ChromaDB
        |           |            |
        |      AI Analysis    Vector Search
        |      Interview     Embeddings
        |      Roadmaps      Resume Chunks
        |      AI Chat            |
        |           |             |
        +-----------+-------------+
                    |
                    v
             CAREER ASSISTANT
```

## RAG Pipeline

CareerCompass AI uses Retrieval-Augmented Generation (RAG) to provide context-aware responses based on uploaded resume information.

**Workflow:**

1. Resume Upload – The user uploads a PDF resume.
2. Text Extraction – Apache PDFBox extracts the resume content.
3. Text Chunking – Extracted content is divided into overlapping chunks.
4. Embedding Generation – Gemini Embedding API converts chunks into vector representations.
5. Vector Storage – ChromaDB stores embeddings with user-specific metadata.
6. Semantic Retrieval – Relevant resume chunks are retrieved based on the user's question.
7. AI Response – Retrieved information is provided as reference context to Gemini to generate a grounded response.

This approach enables the AI assistant to answer career-related questions using relevant information from the user's resume.

## Project Structure

```text
career-assistant/
│
├── .github/
│
├── docs/
│   ├── API.md
│   ├── ARCHITECTURE.md
│   ├── DATABASE.md
│   ├── RAG_PIPELINE.md
│   ├── SETUP.md
│   └── runtime-validation-report.md
│
├── src/
│   ├── main/
│   │   ├── java/com/careerassistant/
│   │   │   ├── ai/
│   │   │   ├── auth/
│   │   │   ├── chatbot/
│   │   │   ├── common/
│   │   │   ├── config/
│   │   │   ├── dashboard/
│   │   │   ├── interview/
│   │   │   ├── job/
│   │   │   ├── matching/
│   │   │   ├── rag/
│   │   │   ├── resume/
│   │   │   ├── roadmap/
│   │   │   ├── security/
│   │   │   ├── tracker/
│   │   │   └── user/
│   │   │
│   │   └── resources/
│   │       ├── db/migration/
│   │       └── static/
│   │           ├── index.html
│   │           ├── styles.css
│   │           └── app.js
│   │
│   └── test/
│
├── tests/
│   └── runtime-validation/
│
├── Dockerfile
├── compose.yaml
├── pom.xml
├── .env.example
└── README.md
```

## Installation and Setup

### Prerequisites

Make sure the following tools are installed:

- Java 17
- Docker Desktop
- Docker Compose
- Git
- Google Gemini API Key

Node.js 22 is additionally required for the provided runtime validation scripts.

### Step 1: Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/careercompass-ai.git

cd careercompass-ai
```

### Step 2: Configure Environment Variables

Create a `.env` file by copying the example configuration.

**Windows PowerShell:**

```powershell
Copy-Item .env.example .env
```

**Linux / macOS:**

```bash
cp .env.example .env
```

Configure the following variables in your `.env` file:

```env
MYSQL_DATABASE=career_assistant_db
MYSQL_USER=career_user
MYSQL_PASSWORD=your_mysql_password
MYSQL_ROOT_PASSWORD=your_root_password

JWT_SECRET=your_long_random_jwt_secret

GEMINI_API_KEY=your_gemini_api_key
GEMINI_MODEL=gemini-3.5-flash-lite
GEMINI_EMBEDDING_MODEL=gemini-embedding-2

CHROMA_URL=http://chromadb:8000
UPLOAD_DIR=./uploads
```

**Important:**
- Replace all placeholder passwords with strong passwords.
- Generate a secure JWT secret of at least 32 characters.
- Obtain your Gemini API key from [Google AI Studio](https://aistudio.google.com/apikey).
- Keep your `.env` file private.
- The Gemini model shown above matches the model verified in the project's runtime validation report.

### Step 3: Start the Application Using Docker

Run the following command from the project root:

```bash
docker compose --env-file .env up --build
```

Docker Compose automatically builds the Spring Boot application and starts the required services:

- Spring Boot application
- MySQL database
- ChromaDB vector database

Flyway automatically executes database migrations during application startup.

### Step 4: Access the Application

Once the containers are running, access the application using:

| Service | URL |
|---|---|
| CareerCompass AI | http://localhost:8080 |
| ChromaDB | http://localhost:8000 |
| MySQL | localhost:3307 |

### Step 5: Verify Running Containers

```bash
docker compose ps
```

### Step 6: Stop the Application

```bash
docker compose down
```

To remove containers and all persistent Docker volumes:

```bash
docker compose down -v
```

**Warning:** The second command permanently removes the MySQL, ChromaDB, and uploaded-file volumes.

---

## API Modules

The application exposes RESTful API endpoints for the following modules:

| Module | Base Endpoint |
|---|---|
| Authentication | `/api/auth` |
| User Management | `/api/users` |
| Resume Management | `/api/resumes` |
| Job Descriptions | `/api/jobs` |
| Job Matching | `/api/matches` |
| Interview Preparation | `/api/interviews` |
| Learning Roadmaps | `/api/roadmaps` |
| Job Applications | `/api/applications` |
| AI Career Chat | `/api/chat` |
| Dashboard | `/api/dashboard` |

See the [API Documentation](docs/API.md) for more information.

## Database Management

MySQL stores the application's relational data, including:

- User accounts and profiles
- Resume information and analysis
- Skills and job descriptions
- Job matching results
- Interview questions and evaluations
- Learning roadmaps and tasks
- Job applications
- Chat conversations and messages
- Document chunk metadata

Flyway manages database schema migrations automatically.

ChromaDB stores the vector representations used by the RAG pipeline.

## Testing and Validation

### Run Maven Tests

```bash
mvn test
```

### Run Runtime Validation Tests

Start the application using Docker Compose, then execute:

```bash
node --experimental-default-type=module --test --test-concurrency=1 tests/runtime-validation/ai-features.test.js tests/runtime-validation/auth-profile-tracker.test.js tests/runtime-validation/resume-job-match.test.js
```

### Run Live Gemini Integration Tests

```bash
node --experimental-default-type=module tests/runtime-validation/gemini-live.js
```

**Note:** Live Gemini integration tests make actual API requests and may consume API quota.

### Validation Results

The runtime validation report records:

- Application startup and health-check verification.
- Authentication and profile management tests.
- Resume upload, extraction, and analysis.
- Job creation and matching.
- Interview question generation and answer evaluation.
- Learning roadmap generation and task completion.
- Job application tracking.
- ChromaDB embedding storage and semantic retrieval.
- Grounded AI career chat.
- Browser-based UI validation.

The recorded validation run passed the listed feature-level checks. This is not a substitute for exhaustive security, performance, or production testing.

See [Runtime Validation Report](docs/runtime-validation-report.md) for detailed results and known limitations.

---

## Security Considerations

- JWT-based authentication protects secured API endpoints.
- Passwords are stored using password hashing.
- User ownership is checked before accessing user-specific records.
- Sensitive configuration is managed through environment variables.
- Never commit `.env` or expose API keys and database credentials.
- Do not upload private resume documents or personal information to a public repository.

## Future Enhancements

- Integration with external job listing platforms.
- Advanced career analytics and visualizations.
- More detailed interview performance tracking.
- Additional resume formats and document processing.
- Cloud deployment and CI/CD automation.

## Author

**Sunnyth Reddy**

- GitHub: https://github.com/Sheelam-Sunnyth

---

<p align="center">
  Built with Java, Spring Boot, and AI to make career development smarter.
</p>