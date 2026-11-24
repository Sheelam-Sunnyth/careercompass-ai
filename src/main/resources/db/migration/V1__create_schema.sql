CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    phone VARCHAR(40),
    location VARCHAR(160),
    education VARCHAR(500),
    target_role VARCHAR(160),
    career_preferences TEXT,
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE resumes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    extracted_text LONGTEXT NOT NULL,
    rag_indexed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_resumes_user_created (user_id, created_at)
);

CREATE TABLE resume_analyses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resume_id BIGINT NOT NULL,
    overall_score INT NOT NULL,
    strengths JSON NOT NULL,
    weaknesses JSON NOT NULL,
    recommendations JSON NOT NULL,
    section_feedback JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_analysis_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    INDEX idx_analyses_resume_created (resume_id, created_at)
);

CREATE TABLE resume_skills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resume_id BIGINT NOT NULL,
    skill_name VARCHAR(120) NOT NULL,
    proficiency VARCHAR(30) NOT NULL DEFAULT 'DEMONSTRATED',
    CONSTRAINT fk_skill_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    UNIQUE KEY uq_resume_skill (resume_id, skill_name)
);

CREATE TABLE job_descriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    company VARCHAR(160) NOT NULL,
    job_title VARCHAR(160) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_jobs_user_created (user_id, created_at)
);

CREATE TABLE job_matches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resume_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    match_score INT NOT NULL,
    matched_skills JSON NOT NULL,
    missing_skills JSON NOT NULL,
    partial_skills JSON NOT NULL,
    report TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_match_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    CONSTRAINT fk_match_job FOREIGN KEY (job_id) REFERENCES job_descriptions(id) ON DELETE CASCADE,
    INDEX idx_matches_resume_created (resume_id, created_at)
);

CREATE TABLE interview_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    job_id BIGINT,
    session_type VARCHAR(80) NOT NULL,
    category VARCHAR(80) NOT NULL,
    difficulty VARCHAR(30) NOT NULL,
    score INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interview_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_interview_job FOREIGN KEY (job_id) REFERENCES job_descriptions(id) ON DELETE SET NULL,
    INDEX idx_interviews_user_created (user_id, created_at)
);

CREATE TABLE interview_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    suggested_answer TEXT,
    user_answer TEXT,
    feedback TEXT,
    score INT,
    CONSTRAINT fk_question_session FOREIGN KEY (session_id) REFERENCES interview_sessions(id) ON DELETE CASCADE
);

CREATE TABLE learning_roadmaps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    target_role VARCHAR(160) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_roadmap_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_roadmaps_user_created (user_id, created_at)
);

CREATE TABLE roadmap_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    roadmap_id BIGINT NOT NULL,
    phase_name VARCHAR(160) NOT NULL,
    topic VARCHAR(250) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    due_date DATE,
    CONSTRAINT fk_task_roadmap FOREIGN KEY (roadmap_id) REFERENCES learning_roadmaps(id) ON DELETE CASCADE
);

CREATE TABLE job_applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    company VARCHAR(160) NOT NULL,
    position VARCHAR(160) NOT NULL,
    job_description TEXT,
    application_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    interview_date DATETIME,
    job_url VARCHAR(1000),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_application_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_applications_user_status (user_id, status),
    INDEX idx_applications_user_date (user_id, application_date)
);

CREATE TABLE conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversation_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_conversations_user_created (user_id, created_at)
);

CREATE TABLE chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    source_refs JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

CREATE TABLE document_chunks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resume_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    chunk_text TEXT NOT NULL,
    vector_id VARCHAR(100) NOT NULL UNIQUE,
    CONSTRAINT fk_chunk_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    UNIQUE KEY uq_resume_chunk (resume_id, chunk_index)
);
