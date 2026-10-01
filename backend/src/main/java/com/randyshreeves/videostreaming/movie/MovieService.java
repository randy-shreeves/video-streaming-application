package com.randyshreeves.videostreaming.movie;

import com.randyshreeves.videostreaming.auth.StreamTokenService;
import com.randyshreeves.videostreaming.exception.InvalidMediaFileException;
import com.randyshreeves.videostreaming.exception.MediaFileNotFoundException;
import com.randyshreeves.videostreaming.exception.MediaStorageException;
import com.randyshreeves.videostreaming.exception.MovieNotFoundException;
import com.randyshreeves.videostreaming.movie.dto.MovieRequest;
import com.randyshreeves.videostreaming.movie.dto.MovieResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final StreamTokenService streamTokenService;
    private String mediaRoot;

    public MovieService(
            MovieRepository movieRepository,
            StreamTokenService streamTokenService,
            @Value("${media.root}") String mediaRoot
    ) {
        this.movieRepository = movieRepository;
        this.streamTokenService = streamTokenService;
        this.mediaRoot = mediaRoot;
    }

    public Page<MovieResponse> getAllPublishedMovies(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Movie> moviePage;
        if (search == null || search.isBlank()) {
            moviePage = movieRepository.findByPublishedTrue(pageable);
        } else {
            moviePage = movieRepository.findByPublishedTrueAndTitleContainingIgnoreCase(search, pageable);
        }
        return moviePage.map(this::toMovieResponse);
    }

    public Page<MovieResponse> getAllMovies(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Movie> moviePage;
        if (search == null || search.isBlank()) {
            moviePage = movieRepository.findAll(pageable);
        } else {
            moviePage = movieRepository.findByTitleContainingIgnoreCase(search, pageable);
        }
        return moviePage.map(this::toMovieResponse);
    }

    public MovieResponse getPublishedMovie(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
        if (!movie.isPublished()) {
            throw new MovieNotFoundException(id);
        }
        return toMovieResponse(movie);
    }

    public MovieResponse getMovie(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
        return toMovieResponse(movie);
    }

    public Resource getMovieStream(Long id) throws MalformedURLException {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
        if (!movie.isPublished()) {
            throw new MovieNotFoundException(id);
        }
        Path path = Paths.get(mediaRoot, movie.getStorageLocation());
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) {
            throw new MediaFileNotFoundException("Video file not found.");
        }
        return resource;
    }

    public String getStreamToken(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
        if (!movie.isPublished()) {
            throw new MovieNotFoundException(id);
        }
        return streamTokenService.generateToken(id);
    }

    public Resource getPublishedMoviePoster(Long id) throws MalformedURLException {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
        if (!movie.isPublished()) {
            throw new MovieNotFoundException(id);
        }
        Path path = Paths.get(mediaRoot, movie.getPosterLocation());
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) {
            throw new MediaFileNotFoundException("Movie poster not found.");
        }
        return resource;
    }

    public Resource getMoviePoster(Long id) throws MalformedURLException {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
        Path path = Paths.get(mediaRoot, movie.getPosterLocation());
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) {
            throw new MediaFileNotFoundException("Movie poster not found.");
        }
        return resource;
    }

    public MovieResponse createMovie(MovieRequest movieRequest) {
        Movie movie = toMovie(movieRequest);
        Movie savedMovie = movieRepository.save(movie);
        return toMovieResponse(savedMovie);
    }

    public void uploadVideo(Long id, MultipartFile video) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
        String oldVideoLocation = movie.getStorageLocation();
        if (video == null || video.isEmpty()) {
            throw new InvalidMediaFileException("Video file is required.");
        }
        if(!"video/mp4".equals(video.getContentType())) {
            throw new InvalidMediaFileException("Video must be .mp4 format.");
        }
        String filename = UUID.randomUUID() + ".mp4";
        String newStorageLocation = "movies/" + filename;
        Path destination = Paths.get(mediaRoot, newStorageLocation);
        try (InputStream inputStream = video.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new MediaStorageException("Failed to save video.");
        }
        movie.setStorageLocation(newStorageLocation);
        movieRepository.save(movie);
        if (oldVideoLocation != null) {
            Path oldVideoPath = Paths.get(mediaRoot, oldVideoLocation);
            try {
                Files.deleteIfExists(oldVideoPath);
            } catch (IOException e) {
                throw new MediaStorageException("Failed to delete old video.");
            }
        }
    }

    public void uploadPoster(Long id, MultipartFile poster) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
        String oldPosterLocation = movie.getPosterLocation();
        if (poster == null || poster.isEmpty()) {
            throw new InvalidMediaFileException("Poster file is required.");
        }
        if (!MediaType.IMAGE_JPEG_VALUE.equals(poster.getContentType())) {
            throw new InvalidMediaFileException("Poster must be a JPEG image.");
        }
        String filename = UUID.randomUUID() + ".jpg";
        String newPosterLocation = "movies/posters/" + filename;
        Path destination = Paths.get(mediaRoot, newPosterLocation);
        try (InputStream inputStream = poster.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new MediaStorageException("Failed to save movie poster.");
        }
        movie.setPosterLocation("movies/posters/" + filename);
        movieRepository.save(movie);
        if (oldPosterLocation != null) {
            Path oldPosterPath = Paths.get(mediaRoot, oldPosterLocation);
            try {
                Files.deleteIfExists(oldPosterPath);
            } catch (IOException e) {
                throw new MediaStorageException("Failed to delete old movie poster");
            }
        }
    }

    public MovieResponse publishMovie(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
        if (movie.getStorageLocation() == null || movie.getPosterLocation() == null) {
            throw new IllegalStateException("A movie must have video file and poster file before it can be published.");
        }
        movie.setPublished(true);
        Movie savedMovie = movieRepository.save(movie);
        return toMovieResponse(savedMovie);
    }

    public MovieResponse unpublishMovie(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
        movie.setPublished(false);
        Movie savedMovie = movieRepository.save(movie);
        return toMovieResponse(savedMovie);
    }

    public MovieResponse updateMovie(Long id, MovieRequest movieRequest) {
        Movie existingMovie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
        existingMovie.setTitle(movieRequest.getTitle());
        existingMovie.setDescription(movieRequest.getDescription());
        existingMovie.setReleaseYear(movieRequest.getReleaseYear());
        existingMovie.setRuntimeMinutes(movieRequest.getRuntimeMinutes());
        Movie savedMovie = movieRepository.save(existingMovie);
        return toMovieResponse(savedMovie);
    }

    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
        String videoFileLocation = movie.getStorageLocation();
        String posterFileLocation = movie.getPosterLocation();
        if (videoFileLocation != null) {
            Path videoFilePath = Paths.get(mediaRoot, videoFileLocation);
            try {
                Files.deleteIfExists(videoFilePath);
            } catch (IOException e) {
                throw new MediaStorageException("Failed to delete video file.");
            }
        }
        if (posterFileLocation != null) {
            Path posterFilePath = Paths.get(mediaRoot, posterFileLocation);
            try {
                Files.deleteIfExists(posterFilePath);
            } catch (IOException e) {
                throw new MediaStorageException("Failed to delete poster file.");
            }
        }
        movieRepository.delete(movie);
    }

    private MovieResponse toMovieResponse(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getReleaseYear(),
                movie.getRuntimeMinutes(),
                movie.getStorageLocation() != null,
                movie.isPublished()
        );
    }

    private Movie toMovie(MovieRequest movieRequest) {
        return new Movie(
          movieRequest.getTitle(),
          movieRequest.getDescription(),
          movieRequest.getReleaseYear(),
          movieRequest.getRuntimeMinutes()
        );
    }
}
