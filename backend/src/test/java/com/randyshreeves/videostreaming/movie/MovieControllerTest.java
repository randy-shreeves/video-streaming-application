package com.randyshreeves.videostreaming.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.randyshreeves.videostreaming.auth.JwtAuthenticationFilter;
import com.randyshreeves.videostreaming.auth.JwtService;
import com.randyshreeves.videostreaming.auth.StreamTokenService;
import com.randyshreeves.videostreaming.exception.MovieNotFoundException;
import com.randyshreeves.videostreaming.movie.dto.MovieRequest;
import com.randyshreeves.videostreaming.movie.dto.MovieResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private StreamTokenService streamTokenService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturnAllPublishedMoviesSuccessfully() throws Exception {
        MovieResponse movieResponse1 = createTestMovieResponse();
        MovieResponse movieResponse2 = new MovieResponse(
                2L,
                "Another Test Movie",
                "TestMovieDescription",
                2009,
                90,
                true,
                true
        );
        List<MovieResponse> movieResponseList = new ArrayList<>();
        movieResponseList.add(movieResponse1);
        movieResponseList.add(movieResponse2);
        Page<MovieResponse> movieResponsePage = new PageImpl<>(movieResponseList);
        when(movieService.getAllPublishedMovies(null, 0, 12)).thenReturn(movieResponsePage);
        mockMvc.perform(get("/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test Movie Title"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].title").value("Another Test Movie"));
        verify(movieService).getAllPublishedMovies(null, 0, 12);
    }

    @Test
    void shouldReturnAllMoviesSuccessfully() throws Exception {
        MovieResponse movieResponse1 = createTestMovieResponse();
        MovieResponse movieResponse2 = new MovieResponse(
                2L,
                "Another Test Movie",
                "TestMovieDescription",
                2009,
                90,
                true,
                true
        );
        List<MovieResponse> movieResponseList = new ArrayList<>();
        movieResponseList.add(movieResponse1);
        movieResponseList.add(movieResponse2);
        Page<MovieResponse> movieResponsePage = new PageImpl<>(movieResponseList);
        when(movieService.getAllMovies(null, 0, 12)).thenReturn(movieResponsePage);
        mockMvc.perform(get("/movies/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test Movie Title"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].title").value("Another Test Movie"));
        verify(movieService).getAllMovies(null, 0, 12);
    }

    @Test
    void shouldReturnOnePublishedMovieSuccessfully() throws Exception {
        MovieResponse movieResponse = createTestMovieResponse();
        Long movieId = movieResponse.getId();
        when(movieService.getPublishedMovie(movieId)).thenReturn(movieResponse);
        mockMvc.perform(get("/movies/{id}/details", movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Movie Title"));
        verify(movieService).getPublishedMovie(movieId);
    }

    @Test
    void shouldReturnOneMovieSuccessfully() throws Exception {
        MovieResponse movieResponse = createTestMovieResponse();
        Long movieId = movieResponse.getId();
        when(movieService.getMovie(movieId)).thenReturn(movieResponse);
        mockMvc.perform(get("/movies/admin/{id}/details", movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Movie Title"));
        verify(movieService).getMovie(movieId);
    }

    @Test
    void shouldCreateMovieSuccessfully() throws Exception {
        MovieRequest movieRequest = createTestMovieRequest();
        MovieResponse movieResponse = createTestMovieResponse();
        when(movieService.createMovie(any(MovieRequest.class))).thenReturn(movieResponse);
        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Movie Title"));
        verify(movieService).createMovie(any(MovieRequest.class));
    }

    @Test
    void shouldUpdateMovieSuccessfully() throws Exception {
        MovieRequest movieRequest = createTestMovieRequest();
        MovieResponse movieResponse = createTestMovieResponse();
        Long movieId = movieResponse.getId();
        when(movieService.updateMovie(eq(movieId), any(MovieRequest.class))).thenReturn(movieResponse);
        mockMvc.perform(put("/movies/{id}", movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Movie Title"));
        verify(movieService).updateMovie(eq(movieId), any(MovieRequest.class));
    }

    @Test
    void shouldDeleteMovieSuccessfully() throws Exception {
        Long movieId = 1L;
        mockMvc.perform(delete("/movies/{id}", movieId))
                .andExpect(status().isNoContent());
        verify(movieService).deleteMovie(movieId);
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        MovieRequest movieRequest = createTestMovieRequest();
        movieRequest.setTitle("");
        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Title cannot be blank."));
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsTooLong() throws Exception {
        MovieRequest movieRequest = createTestMovieRequest();
        movieRequest.setTitle("A".repeat(256));
        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Title cannot be greater than 255 characters."));
    }

    @Test
    void shouldReturnBadRequestWhenDescriptionIsTooLong() throws Exception {
        MovieRequest movieRequest = createTestMovieRequest();
        movieRequest.setDescription("A".repeat(1001));
        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Description cannot be greater than 1000 characters."));
    }

    @Test
    void shouldReturnBadRequestWhenReleaseYearIsMissing() throws Exception {
        String json = """
                    {
                      "title": "Test Movie",
                      "description": "Description",
                      "runtimeMinutes": 90
                    }
                    """;
        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Release year is required."));
    }

    @Test
    void shouldReturnBadRequestWhenReleaseYearIsLessThan1888() throws Exception {
        MovieRequest movieRequest = createTestMovieRequest();
        movieRequest.setReleaseYear(1887);
        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Release year must be after 1887."));
    }

    @Test
    void shouldReturnBadRequestWhenRuntimeMinutesIsMissing() throws Exception {
        String json = """
                    {
                      "title": "Test Movie",
                      "description": "Description",
                      "releaseYear": 2009,
                      "storageLocation": "movies/test.mp4"
                    }
                    """;
        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Runtime is required."));
    }

    @Test
    void shouldReturnBadRequestWhenRuntimeMinutesIsNotPositive() throws Exception {
        MovieRequest movieRequest = createTestMovieRequest();
        movieRequest.setRuntimeMinutes(-1);
        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Runtime must be greater than 0."));
    }

    @Test
    void shouldReturnNotFoundExceptionWhenMovieDoesNotExist() throws Exception {
        when(movieService.getPublishedMovie(999L)).thenThrow(new MovieNotFoundException(999L));
        mockMvc.perform(get("/movies/999/details"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie not found."));
    }

    @Test
    void shouldReturnNotFoundExceptionWhenDeletingNonexistentMovie() throws Exception {
        doThrow(new MovieNotFoundException(999L)).when(movieService).deleteMovie(999L);
        mockMvc.perform(delete("/movies/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie not found."));
    }

    @Test
    void shouldReturnNotFoundExceptionWhenUpdatingNonexistentMovie() throws Exception {
        MovieRequest movieRequest = createTestMovieRequest();
        when(movieService.updateMovie(eq(999L), any(MovieRequest.class))).thenThrow(new MovieNotFoundException(999L));
        mockMvc.perform(put("/movies/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie not found."));
    }

    @Test
    void shouldReturnMovieStreamSuccessfully() throws Exception {
        Long movieId = 1L;
        Resource resource = new ByteArrayResource("test".getBytes());
        when(movieService.getMovieStream(movieId)).thenReturn(resource);
        mockMvc.perform(get("/movies/{id}/stream", movieId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("video/mp4"));
        verify(movieService).getMovieStream(movieId);
    }

    @Test
    void shouldReturnPublishedMoviePosterSuccessfully() throws Exception {
        Long movieId = 1L;
        Resource resource = new ByteArrayResource("test".getBytes());
        when(movieService.getPublishedMoviePoster(movieId)).thenReturn(resource);
        mockMvc.perform(get("/movies/{id}/poster", movieId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
        verify(movieService).getPublishedMoviePoster(movieId);
    }

    @Test
    void shouldReturnMoviePosterSuccessfully() throws Exception {
        Long movieId = 1L;
        Resource resource = new ByteArrayResource("test".getBytes());
        when(movieService.getMoviePoster(movieId)).thenReturn(resource);
        mockMvc.perform(get("/movies/admin/{id}/poster", movieId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
        verify(movieService).getMoviePoster(movieId);
    }

    private MovieRequest createTestMovieRequest() {
        return new MovieRequest(
            "Test Movie Title",
            "Test Movie Description",
            2009,
            90
        );
    }

    private MovieResponse createTestMovieResponse() {
        return new MovieResponse(
            1L,
            "Test Movie Title",
            "TestMovieDescription",
            2009,
            90,
            true,
            true
        );
    }
}
