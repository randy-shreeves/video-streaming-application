# Video Streaming Application

A full-stack video streaming application that permits authenticated users to browse and stream video content in their browser.

## Features

Users may register a new account and, once logged in, browse a movie catalog, view specific movie details, and stream a movie/video in their browser. Administrators have access to a media management page where they can upload, edit, or delete movies/videos, as well as publish or unpublish them to/from the movie catalog for regular users to view. The application utilizes byte range requests to ensure fast load times and video seeking.

## Screenshots

### Movie Catalog
![Movie Catalog](screenshots/movie-catalog.png)

### Video Player
![Video Player](screenshots/video-player.png)

### Administrator Controls
![Video Player](screenshots/admin-page.png)

## Built With

- React
- TypeScript
- Java
- Spring Boot
- PostgreSQL
- Docker

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