package com.ieti.proyectoieti.controllers;

import com.ieti.proyectoieti.config.SecurityConfig;
import com.ieti.proyectoieti.models.PlaylistInfo;
import com.ieti.proyectoieti.models.PlaylistSearchRequest;
import com.ieti.proyectoieti.services.SpotifyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpotifyController.class)
@Import(SecurityConfig.class)
class SpotifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SpotifyService spotifyService;

    @Test
    void postSearchByGenre_returnsOkAndList() throws Exception {
        PlaylistSearchRequest req = new PlaylistSearchRequest("rock", null, 2);
        List<PlaylistInfo> playlists = Arrays.asList(
                new PlaylistInfo("1", "P1", "url1", 100, "img1"),
                new PlaylistInfo("2", "P2", "url2", 50, "img2")
        );

        when(spotifyService.searchPlaylists(eq("rock"), isNull(), eq(2))).thenReturn(playlists);

        mockMvc.perform(post("/api/spotify/playlists/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void postSearch_whenBothGenreAndArtist_returnsBadRequest() throws Exception {
        PlaylistSearchRequest req = new PlaylistSearchRequest("rock", "artist", 5);

        when(spotifyService.searchPlaylists(anyString(), anyString(), anyInt()))
                .thenThrow(new IllegalArgumentException("Solo se puede buscar por un género O un artista, no ambos"));

        mockMvc.perform(post("/api/spotify/playlists/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPlaylistById_returnsOkWhenFound() throws Exception {
        PlaylistInfo p = new PlaylistInfo("abc", "MiLista", "url", 10, "img");
        when(spotifyService.getPlaylistById("abc")).thenReturn(p);

        mockMvc.perform(get("/api/spotify/playlists/abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("abc"))
                .andExpect(jsonPath("$.name").value("MiLista"));
    }

    @Test
    void getPlaylistById_returnsNotFoundWhenServiceThrows() throws Exception {
        when(spotifyService.getPlaylistById("nope")).thenThrow(new RuntimeException("not found"));

        mockMvc.perform(get("/api/spotify/playlists/nope"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByGenre_queryEndpoint_works() throws Exception {
        List<PlaylistInfo> playlists = Collections.singletonList(new PlaylistInfo("g1", "G1", "url", 1, "img"));
        when(spotifyService.searchPlaylists(eq("rock"), isNull(), eq(5))).thenReturn(playlists);

        mockMvc.perform(get("/api/spotify/playlists/by-genre")
                        .param("genre", "rock")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("g1"));
    }

    @Test
    void getByArtist_queryEndpoint_works() throws Exception {
        List<PlaylistInfo> playlists = Collections.singletonList(new PlaylistInfo("a1", "A1", "url", 1, "img"));
        when(spotifyService.searchPlaylists(isNull(), eq("artistX"), eq(3))).thenReturn(playlists);

        mockMvc.perform(get("/api/spotify/playlists/by-artist")
                        .param("artist", "artistX")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("a1"));
    }
}