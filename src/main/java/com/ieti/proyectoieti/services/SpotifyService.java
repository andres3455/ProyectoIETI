package com.ieti.proyectoieti.services;

import com.ieti.proyectoieti.config.SpotifyApiConfig;
import com.ieti.proyectoieti.models.PlaylistInfo;
import com.neovisionaries.i18n.CountryCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.artists.GetArtistsTopTracksRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchArtistsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpotifyService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyService.class);
    private static final int MIN_SAMPLE_SIZE = 20;
    private static final double MIN_ARTIST_PRESENCE = 0.15;

    private final SpotifyApi spotifyApi;
    private final SpotifyApiConfig spotifyApiConfig;

    public SpotifyService(SpotifyApi spotifyApi, SpotifyApiConfig spotifyApiConfig) {
        this.spotifyApi = spotifyApi;
        this.spotifyApiConfig = spotifyApiConfig;
    }

    /**
     * Search for playlists based on a single genre OR a single artist
     */
    public List<PlaylistInfo> searchPlaylists(String genre, String artist, Integer limit) {
        try {
            spotifyApiConfig.authenticateClientCredentials(spotifyApi);

            List<PlaylistInfo> playlists = new ArrayList<>();
            int effectiveLimit = limit != null ? limit : 10;

            if ((genre != null && !genre.isEmpty()) && (artist != null && !artist.isEmpty())) {
                throw new IllegalArgumentException("Solo se puede buscar por un género O un artista, no ambos");
            }

            if (genre == null && artist == null) {
                throw new IllegalArgumentException("Debe proporcionar un género o un artista para la búsqueda");
            }

            if (genre != null && !genre.isEmpty()) {
                playlists = searchPlaylistsByGenre(genre, effectiveLimit);
            }

            if (artist != null && !artist.isEmpty()) {
                playlists = searchPlaylistsByArtistWithContentVerification(artist, effectiveLimit);
            }

            return playlists.stream()
                    .distinct()
                    .sorted(Comparator.comparing(PlaylistInfo::getFollowers,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(effectiveLimit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error searching playlists: {}", e.getMessage(), e);
            throw new RuntimeException("Error searching playlists: " + e.getMessage(), e);
        }
    }

    /**
     * Search playlists by genre
     */
    private List<PlaylistInfo> searchPlaylistsByGenre(String genre, Integer limit) {
        List<PlaylistInfo> playlists = new ArrayList<>();

        try {
            SearchPlaylistsRequest searchRequest = spotifyApi
                    .searchPlaylists(genre)
                    .limit(Math.min(limit * 2, 50))
                    .build();

            Paging<PlaylistSimplified> playlistPaging = searchRequest.execute();

            if (playlistPaging != null && playlistPaging.getItems() != null) {
                for (PlaylistSimplified playlist : playlistPaging.getItems()) {
                    if (playlist != null) {
                        playlists.add(mapToPlaylistInfo(playlist));
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error searching playlists for genre '{}': {}", genre, e.getMessage(), e);
        }

        return playlists;
    }

    /**
     * Search playlists by artist name with content verification
     */
    private List<PlaylistInfo> searchPlaylistsByArtistWithContentVerification(String artistName, Integer limit) {
        List<PlaylistInfo> validPlaylists = new ArrayList<>();

        try {
            String artistId = getArtistId(artistName);
            if (artistId == null) {
                return validPlaylists;
            }

            List<String> trackIds = getArtistTopTrackIds(artistId);
            Set<String> playlistIdsFound = new HashSet<>();

            for (String trackId : trackIds) {
                if (playlistIdsFound.size() >= limit * 3) break;

                try {
                    SearchPlaylistsRequest trackPlaylistSearch = spotifyApi
                            .searchPlaylists("track:" + trackId)
                            .limit(10)
                            .build();

                    Paging<PlaylistSimplified> results = trackPlaylistSearch.execute();
                    if (results != null && results.getItems() != null) {
                        for (PlaylistSimplified playlist : results.getItems()) {
                            if (playlist != null) {
                                playlistIdsFound.add(playlist.getId());
                            }
                        }
                    }
                } catch (Exception e) {
                    // Continuar con la siguiente canción
                }
            }

            SearchPlaylistsRequest directSearch = spotifyApi
                    .searchPlaylists(artistName)
                    .limit(30)
                    .build();

            Paging<PlaylistSimplified> directResults = directSearch.execute();
            if (directResults != null && directResults.getItems() != null) {
                for (PlaylistSimplified playlist : directResults.getItems()) {
                    if (playlist != null) {
                        playlistIdsFound.add(playlist.getId());
                    }
                }
            }

            for (String playlistId : playlistIdsFound) {
                if (validPlaylists.size() >= limit) break;

                try {
                    var playlist = spotifyApi.getPlaylist(playlistId).build().execute();
                    if (playlist != null && verifyPlaylistContainsArtist(playlist.getId(), artistId, artistName)) {
                        validPlaylists.add(mapToPlaylistInfo(playlist));
                    }
                } catch (Exception e) {
                    // Continuar con la siguiente playlist
                }
            }

        } catch (Exception e) {
            logger.error("Error searching playlists for artist '{}': {}", artistName, e.getMessage(), e);
        }

        return validPlaylists;
    }

    /**
     * Get artist ID from artist name
     */
    private String getArtistId(String artistName) {
        try {
            SearchArtistsRequest searchArtistsRequest = spotifyApi
                    .searchArtists(artistName)
                    .limit(1)
                    .build();

            Paging<Artist> artistPaging = searchArtistsRequest.execute();

            if (artistPaging != null && artistPaging.getItems() != null && artistPaging.getItems().length > 0) {
                Artist artist = artistPaging.getItems()[0];
                return artist.getId();
            }
        } catch (Exception e) {
            logger.error("Error searching for artist '{}': {}", artistName, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Get top track IDs from an artist
     */
    private List<String> getArtistTopTrackIds(String artistId) {
        List<String> trackIds = new ArrayList<>();
        try {
            GetArtistsTopTracksRequest topTracksRequest = spotifyApi
                    .getArtistsTopTracks(artistId, CountryCode.US)
                    .build();

            Track[] topTracks = topTracksRequest.execute();
            if (topTracks != null) {
                for (Track track : topTracks) {
                    if (track != null && trackIds.size() < 5) {
                        trackIds.add(track.getId());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error getting top tracks for artist '{}': {}", artistId, e.getMessage(), e);
        }
        return trackIds;
    }

    /**
     * Verify that a playlist contains songs from the specified artist
     */
    private boolean verifyPlaylistContainsArtist(String playlistId, String artistId, String artistName) {
        try {
            GetPlaylistsItemsRequest getPlaylistItemsRequest = spotifyApi
                    .getPlaylistsItems(playlistId)
                    .limit(50)
                    .build();

            Paging<PlaylistTrack> playlistTracks = getPlaylistItemsRequest.execute();

            if (playlistTracks == null || playlistTracks.getItems() == null || playlistTracks.getItems().length == 0) {
                return false;
            }

            int totalTracksChecked = Math.min(playlistTracks.getItems().length, MIN_SAMPLE_SIZE);
            int artistTracksFound = 0;

            for (int i = 0; i < totalTracksChecked; i++) {
                PlaylistTrack playlistTrack = playlistTracks.getItems()[i];
                if (playlistTrack != null && playlistTrack.getTrack() != null) {
                    IPlaylistItem item = playlistTrack.getTrack();

                    if (item instanceof Track) {
                        Track track = (Track) item;
                        ArtistSimplified[] artists = track.getArtists();

                        if (artists != null) {
                            for (ArtistSimplified artist : artists) {
                                if (artist.getId().equals(artistId) ||
                                        artist.getName().equalsIgnoreCase(artistName)) {
                                    artistTracksFound++;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            double artistPresence = (double) artistTracksFound / totalTracksChecked;
            return artistPresence >= MIN_ARTIST_PRESENCE;

        } catch (Exception e) {
            logger.error("Error verifying playlist content for playlist '{}': {}", playlistId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get a specific playlist by ID with full details
     */
    public PlaylistInfo getPlaylistById(String playlistId) {
        try {
            spotifyApiConfig.authenticateClientCredentials(spotifyApi);

            var playlist = spotifyApi.getPlaylist(playlistId).build().execute();

            if (playlist == null) {
                throw new RuntimeException("Playlist not found with ID: " + playlistId);
            }

            PlaylistInfo playlistInfo = new PlaylistInfo();
            playlistInfo.setId(playlist.getId());
            playlistInfo.setName(playlist.getName());

            if (playlist.getExternalUrls() != null) {
                playlistInfo.setSpotifyUrl(playlist.getExternalUrls().get("spotify"));
            }

            if (playlist.getFollowers() != null) {
                playlistInfo.setFollowers(playlist.getFollowers().getTotal());
            }

            if (playlist.getImages() != null && playlist.getImages().length > 0) {
                playlistInfo.setImageUrl(playlist.getImages()[0].getUrl());
            }

            return playlistInfo;
        } catch (Exception e) {
            logger.error("Error fetching playlist {}: {}", playlistId, e.getMessage(), e);
            throw new RuntimeException("Error fetching playlist: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to map Spotify playlist to our DTO
     */
    private PlaylistInfo mapToPlaylistInfo(PlaylistSimplified playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("Playlist cannot be null");
        }

        PlaylistInfo info = new PlaylistInfo();
        info.setId(playlist.getId());
        info.setName(playlist.getName());

        if (playlist.getExternalUrls() != null) {
            info.setSpotifyUrl(playlist.getExternalUrls().get("spotify"));
        }

        if (playlist.getImages() != null && playlist.getImages().length > 0) {
            info.setImageUrl(playlist.getImages()[0].getUrl());
        }

        info.setFollowers(playlist.getTracks() != null ? playlist.getTracks().getTotal() : 0);

        return info;
    }

    /**
     * Helper method to map full Playlist object to our DTO
     */
    private PlaylistInfo mapToPlaylistInfo(Playlist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("Playlist cannot be null");
        }

        PlaylistInfo info = new PlaylistInfo();
        info.setId(playlist.getId());
        info.setName(playlist.getName());

        if (playlist.getExternalUrls() != null) {
            info.setSpotifyUrl(playlist.getExternalUrls().get("spotify"));
        }

        if (playlist.getImages() != null && playlist.getImages().length > 0) {
            info.setImageUrl(playlist.getImages()[0].getUrl());
        }

        if (playlist.getFollowers() != null) {
            info.setFollowers(playlist.getFollowers().getTotal());
        } else {
            info.setFollowers(playlist.getTracks() != null ? playlist.getTracks().getTotal() : 0);
        }

        return info;
    }
}