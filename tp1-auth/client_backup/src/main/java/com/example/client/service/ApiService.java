package com.example.client.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiService {

    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient client = HttpClient.newHttpClient();

    public record ApiResult(boolean success, String message, String token) {}

    /** Inscrit un nouvel utilisateur. */
    public ApiResult register(String email, String password) {
        String body = buildFormBody(Map.of("email", email, "password", password));
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/register"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean ok = response.statusCode() >= 200 && response.statusCode() < 300;
            String msg = extractJsonField(response.body(), "message");
            return new ApiResult(ok, msg, null);

        } catch (IOException | InterruptedException e) {
            return new ApiResult(false, "Erreur de connexion au serveur : " + e.getMessage(), null);
        }
    }

    /** Connecte un utilisateur et retourne son token de session. */
    public ApiResult login(String email, String password) {
        String body = buildFormBody(Map.of("email", email, "password", password));
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean ok = response.statusCode() >= 200 && response.statusCode() < 300;
            String msg = extractJsonField(response.body(), "message");
            // CORRECTION : utilise la même méthode générique pour extraire le token
            String token = ok ? extractJsonField(response.body(), "token") : null;
            return new ApiResult(ok, msg, token);

        } catch (IOException | InterruptedException e) {
            return new ApiResult(false, "Erreur de connexion au serveur : " + e.getMessage(), null);
        }
    }

    /** Récupère le profil de l'utilisateur connecté via son token. */
    public ApiResult getMe(String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/me"))
                    .header("X-Session-Token", token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean ok = response.statusCode() >= 200 && response.statusCode() < 300;
            return new ApiResult(ok, response.body(), null);

        } catch (IOException | InterruptedException e) {
            return new ApiResult(false, "Erreur de connexion au serveur : " + e.getMessage(), null);
        }
    }

    /** Encode les paramètres en form-urlencoded. */
    private String buildFormBody(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    /**
     * Extrait la valeur d'un champ string dans un JSON simple.
     * CORRECTION : méthode unifiée et fiable pour tous les champs.
     */
    private String extractJsonField(String json, String key) {
        if (json == null) return "N/A";
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx < 0) return "N/A";
        // Cherche le premier guillemet ouvrant après le ":"
        int colon = json.indexOf(":", keyIdx + search.length());
        if (colon < 0) return "N/A";
        int quote1 = json.indexOf("\"", colon + 1);
        if (quote1 < 0) return "N/A";
        int quote2 = json.indexOf("\"", quote1 + 1);
        if (quote2 < 0) return "N/A";
        return json.substring(quote1 + 1, quote2);
    }
}