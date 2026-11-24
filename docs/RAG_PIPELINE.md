# RAG pipeline
The server splits extracted resume text into overlapping chunks, embeds them with Gemini, stores them in ChromaDB with user metadata, retrieves only the current user’s matching chunks, and marks excerpts as untrusted reference data in the grounded Gemini prompt.
