package com.randyshreeves.videostreaming.watchlist;

import com.randyshreeves.videostreaming.exception.MovieAlreadyInWatchlistException;
import com.randyshreeves.videostreaming.exception.MovieNotFoundException;
import com.randyshreeves.videostreaming.movie.Movie;
import com.randyshreeves.videostreaming.movie.MovieRepository;
import com.randyshreeves.videostreaming.movie.dto.MovieResponse;
import com.randyshreeves.videostreaming.user.User;
import com.randyshreeves.videostreaming.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public WatchlistService(
            WatchlistRepository watchlistRepository,
            UserRepository userRepository,
            MovieRepository movieRepository
    ) {
        this.watchlistRepository = watchlistRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    public Page<MovieResponse> getWatchlist(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Watchlist> watchlistPage = watchlistRepository.findByUserIdAndMoviePublishedTrueOrderByIdAsc(userId, pageable);
        return watchlistPage.map(watchlist -> toMovieResponse(watchlist.getMovie()));
    }

    public void addToWatchlist(Long userId, Long movieId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("Username not found."));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException(movieId));
        if (!movie.isPublished()) {
            throw new MovieNotFoundException(movieId);
        }
        if (watchlistRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw new MovieAlreadyInWatchlistException("Movie is already in watchlist.");
        }

        Watchlist watchlist = new Watchlist(user, movie);
        watchlistRepository.save(watchlist);
    }

    public void removeFromWatchlist(Long userId, Long movieId) {
        Watchlist watchlist = watchlistRepository
                .findByUserIdAndMovieId(userId, movieId)
                .orElseThrow(() -> new IllegalStateException("Movie is not in the watchlist."));
        watchlistRepository.delete(watchlist);
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
}
