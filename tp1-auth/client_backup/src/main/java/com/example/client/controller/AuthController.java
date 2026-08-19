package com.example.client.controller;

import com.example.client.service.ApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/** Contrôleur JavaFX pour Authentification */
public class AuthController {
    @FXML private TabPane tabPane;

    // Onglet Connexion
    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;
    @FXML private Button loginButton;
    @FXML private Label loginStatus;

    // Onglet Inscription
    @FXML private TextField registerEmail;
    @FXML private PasswordField registerPassword;
    @FXML private Button registerButton;
    @FXML private Label registerStatus;

    // Onglet Mon Profil
    @FXML private VBox profileBox;
    @FXML private Label profileEmail;
    @FXML private Label profileCreatedAt;
    @FXML private Label profileToken;
    @FXML private Button logoutButton;

    private final ApiService apiService = new ApiService();
    private String sessionToken = null;

    @FXML
    public void initialize() {
        // CORRECTION : setManaged(false) évite que la carte garde son espace quand cachée
        profileBox.setVisible(false);
        profileBox.setManaged(false);
    }

    /** Action Connexion. */
    @FXML
    private void handleLogin() {
        String email = loginEmail.getText().trim();
        String password = loginPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            setStatus(loginStatus, "Veuillez remplir tous les champs.", false);
            return;
        }

        loginButton.setDisable(true);
        loginStatus.setText("Connexion en cours...");

        new Thread(() -> {
            ApiService.ApiResult result = apiService.login(email, password);
            Platform.runLater(() -> {
                loginButton.setDisable(false);
                if (result.success()) {
                    sessionToken = result.token();
                    setStatus(loginStatus, "Connexion reussie !", true);
                    loadProfile();
                    tabPane.getSelectionModel().select(2);
                } else {
                    setStatus(loginStatus, "Echec : " + result.message(), false);
                }
            });
        }).start();
    }

    /** Action Inscription. */
    @FXML
    private void handleRegister() {
        String email = registerEmail.getText().trim();
        String password = registerPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            setStatus(registerStatus, "Veuillez remplir tous les champs.", false);
            return;
        }

        registerButton.setDisable(true);
        registerStatus.setText("Inscription en cours...");

        new Thread(() -> {
            ApiService.ApiResult result = apiService.register(email, password);
            Platform.runLater(() -> {
                registerButton.setDisable(false);
                if (result.success()) {
                    setStatus(registerStatus, "Inscription reussie !", true);
                    registerEmail.clear();
                    registerPassword.clear();
                } else {
                    setStatus(registerStatus, "Echec : " + result.message(), false);
                }
            });
        }).start();
    }

    /** Charge le profil avec le token de session. */
    private void loadProfile() {
        if (sessionToken == null) return;

        new Thread(() -> {
            ApiService.ApiResult result = apiService.getMe(sessionToken);
            Platform.runLater(() -> {
                if (result.success()) {
                    String body = result.message();
                    profileEmail.setText("Email : " + extractJson(body, "email"));
                    profileCreatedAt.setText("Cree le : " + extractJson(body, "createdAt"));
                    profileToken.setText("Token : " + sessionToken.substring(0, 8) + "...");
                    // CORRECTION : afficher ET réserver l'espace
                    profileBox.setVisible(true);
                    profileBox.setManaged(true);
                }
            });
        }).start();
    }

    /** Déconnecte l'utilisateur. */
    @FXML
    private void handleLogout() {
        sessionToken = null;
        // CORRECTION : cacher ET libérer l'espace
        profileBox.setVisible(false);
        profileBox.setManaged(false);
        profileEmail.setText("Email : ");
        profileCreatedAt.setText("Cree le : ");
        profileToken.setText("Token : ");
        loginEmail.clear();
        loginPassword.clear();
        loginStatus.setText("");
        tabPane.getSelectionModel().select(0);
    }

    private void setStatus(Label label, String message, boolean success) {
        label.setText(message);
        label.setStyle(success
                ? "-fx-text-fill: #2ecc71; -fx-font-weight: bold;"
                : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }

    private String extractJson(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return "N/A";
        int colon = json.indexOf(":", idx);
        if (colon < 0) return "N/A";
        int quote1 = json.indexOf("\"", colon + 1);
        int quote2 = json.indexOf("\"", quote1 + 1);
        if (quote1 < 0 || quote2 < 0) return "N/A";
        return json.substring(quote1 + 1, quote2);
    }
}