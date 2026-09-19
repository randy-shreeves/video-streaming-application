# Video Streaming

A full-stack video streaming application built with Spring Boot and React. Authenticated users can browse a movie catalog, view movie details, and stream video content directly from the browser. Administrators can manage the movie catalog through a protected admin interface.

## Features

- Browse movie catalog and view movie details
- Stream MP4 video through the browser
- JWT-based authentication
- Role-based authorization for administrative operations
- Protected access to media resources
- Admin interface for creating, editing, uploading, and deleting movies
- RESTful backend API
- PostgreSQL database with Flyway migrations
- Unit and integration testing
- Docker Compose development environment


## Screenshots

### Movie Catalog
![Movie Catalog](screenshots/movie-catalog.png)

### Video Player
![Video Player](screenshots/video-player.png)

### Administrator Controls
![Video Player](screenshots/admin-page.png)

## Tech Stack

**Frontend**
- React
- TypeScript
- Vite
- Nginx

**Backend**
- Java
- Spring Boot
- Spring Data JPA / Hibernate
- Spring Security
- PostgreSQL
- Flyway
- JUnit

**Infrastructure**
- Docker

## Architecture

The application consists of three containerized services:
- Frontend:5173 maps to Nginx + React
- Backend:8080 maps to Spring Boot
- PostgreSQL:5432

The backend follows a controller, service, repository architecture. The frontend communicates with the backend through a REST API. Video and poster files are stored outside the containers and mounted into the backend container as persistent media storage. PostgreSQL data is persisted in a named volume.

## Running Locally

### Pre-requisites

Project root requires a .env file with the following environmental variables:

MEDIA_ROOT=/media_files

DB_NAME=video_streaming_db

DB_URL=jdbc:postgresql://postgres:5432/video_streaming_db 

DB_USERNAME=postgres 

DB_PASSWORD=<your-password> 

JWT_SECRET=<your-base64-secret> 

JWT_EXPIRATION=86400000 

STREAM_TOKEN_SECRET=<your-base64-secret> 

STREAM_TOKEN_EXPIRATION=21600000 

VITE_API_BASE_URL=```http://localhost:8080```

Host machine requires a C:\media_files\movies\posters directory path to properly store and access video and poster files.

### Starting the Application

```bash
docker compose up --build
```

The application is then available at:
- Frontend: ```http://localhost:5173```
- Backend API: ```http://localhost:8080```

## Test Media
Videos used for development and testing were sourced from [Pexels](https://www.pexels.com/). The video files are not included in this repository.