
package com.ieti.proyectoieti.Controllers;

import com.ieti.proyectoieti.Models.PlaylistInfo;
import com.ieti.proyectoieti.Models.PlaylistSearchRequest;
import com.ieti.proyectoieti.Services.SpotifyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spotify")
@CrossOrigin(origins = "*")
public class SpotifyController {

    private final SpotifyService spotifyService;

    public SpotifyController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    /**
     * Search playlists by a single genre OR a single artist
     * POST /api/spotify/playlists/search
     */
    @PostMapping("/playlists/search")
    public ResponseEntity<List<PlaylistInfo>> searchPlaylists(@RequestBody PlaylistSearchRequest request) {
        try {
            List<PlaylistInfo> playlists = spotifyService.searchPlaylists(
                    request.getGenre(),
                    request.getArtist(),
                    request.getLimit()
            );
            return ResponseEntity.ok(playlists);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a specific playlist by ID
     * GET /api/spotify/playlists/{playlistId}
     */
    @GetMapping("/playlists/{playlistId}")
    public ResponseEntity<PlaylistInfo> getPlaylist(@PathVariable String playlistId) {
        try {
            PlaylistInfo playlist = spotifyService.getPlaylistById(playlistId);
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Search playlists by genre using query parameter
     * GET /api/spotify/playlists/by-genre?genre=rock&limit=10
     */
    @GetMapping("/playlists/by-genre")
    public ResponseEntity<List<PlaylistInfo>> searchByGenre(
            @RequestParam String genre,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<PlaylistInfo> playlists = spotifyService.searchPlaylists(
                    genre,
                    null,
                    limit
            );
            return ResponseEntity.ok(playlists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search playlists by artist using query parameter
     * GET /api/spotify/playlists/by-artist?artist=KingCrimson&limit=10
     */
    @GetMapping("/playlists/by-artist")
    public ResponseEntity<List<PlaylistInfo>> searchByArtist(
            @RequestParam String artist,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<PlaylistInfo> playlists = spotifyService.searchPlaylists(
                    null,
                    artist,
                    limit
            );
            return ResponseEntity.ok(playlists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}