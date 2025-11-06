package com.ieti.proyectoieti.services;

import com.ieti.proyectoieti.config.SpotifyApiConfig;
import com.ieti.proyectoieti.models.PlaylistInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SpotifyApi spotifyApi;

    @Mock
    private SpotifyApiConfig spotifyApiConfig;

    @InjectMocks
    private SpotifyService spotifyService;

    @Test
    void searchPlaylists_whenBothGenreAndArtist_throwsRuntimeException_and_authenticateCalled() {
        doNothing().when(spotifyApiConfig).authenticateClientCredentials(spotifyApi);

        assertThrows(RuntimeException.class,
                () -> spotifyService.searchPlaylists("rock", "artist", 5));

        verify(spotifyApiConfig, times(1)).authenticateClientCredentials(spotifyApi);
    }

    @Test
    void searchPlaylists_whenNeitherGenreNorArtist_throwsRuntimeException() {
        doNothing().when(spotifyApiConfig).authenticateClientCredentials(spotifyApi);

        assertThrows(RuntimeException.class,
                () -> spotifyService.searchPlaylists(null, null, 5));

        verify(spotifyApiConfig, times(1)).authenticateClientCredentials(spotifyApi);
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchPlaylists_byGenre_returnsList() throws Exception {
        doNothing().when(spotifyApiConfig).authenticateClientCredentials(spotifyApi);

        // mock Paging<PlaylistSimplified> and two PlaylistSimplified items
        Paging<PlaylistSimplified> pagingMock = mock(Paging.class);
        PlaylistSimplified p1 = mock(PlaylistSimplified.class);
        PlaylistSimplified p2 = mock(PlaylistSimplified.class);

        when(p1.getId()).thenReturn("1");
        when(p1.getName()).thenReturn("Playlist One");

        when(p2.getId()).thenReturn("2");
        when(p2.getName()).thenReturn("Playlist Two");

        when(pagingMock.getItems()).thenReturn(new PlaylistSimplified[]{p1, p2});

        // stub the chained SpotifyApi call
        when(spotifyApi.searchPlaylists(eq("rock")).limit(anyInt()).build().execute()).thenReturn(pagingMock);

        List<PlaylistInfo> result = spotifyService.searchPlaylists("rock", null, 2);

        assertNotNull(result);
        assertEquals(2, result.size());
        List<String> ids = result.stream().map(PlaylistInfo::getId).toList();
        assertTrue(ids.containsAll(Arrays.asList("1", "2")));
    }

    @Test
    void getPlaylistById_returnsMappedPlaylistInfo() throws Exception {
        doNothing().when(spotifyApiConfig).authenticateClientCredentials(spotifyApi);

        Playlist playlistMock = mock(Playlist.class);
        Followers followersMock = mock(Followers.class);
        Image imgMock = mock(Image.class);

        when(followersMock.getTotal()).thenReturn(10);
        when(imgMock.getUrl()).thenReturn("http://img.example/1.jpg");

        when(playlistMock.getId()).thenReturn("abc");
        when(playlistMock.getName()).thenReturn("MiPlaylist");
        // externalUrls may be implementation-specific; avoid relying on it in assertion
        when(playlistMock.getFollowers()).thenReturn(followersMock);
        when(playlistMock.getImages()).thenReturn(new Image[]{imgMock});

        when(spotifyApi.getPlaylist("abc").build().execute()).thenReturn(playlistMock);

        PlaylistInfo info = spotifyService.getPlaylistById("abc");

        assertNotNull(info);
        assertEquals("abc", info.getId());
        assertEquals("MiPlaylist", info.getName());
        assertEquals(Integer.valueOf(10), info.getFollowers());
        assertEquals("http://img.example/1.jpg", info.getImageUrl());
    }
}
