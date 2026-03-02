package frontoffice.controllers.views;

import frontoffice.models.UserProfile;
import frontoffice.services.UserProfileService;
import frontoffice.utils.HttpErrorParser;
import frontoffice.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;

public class ProfileEditController {
    @FXML
    public TextField nameField;
    @FXML
    public TextField emailField;
    @FXML
    public TextField phoneField;
    @FXML
    public TextField imageUrlField;
    @FXML
    public Label statusLabel;

    private final UserProfileService userProfileService = new UserProfileService();
    private boolean saved = false;

    public void setProfile(UserProfile profile) {
        if (profile == null) return;
        nameField.setText(profile.getName() == null ? "" : profile.getName());
        emailField.setText(profile.getEmail() == null ? "" : profile.getEmail());
        phoneField.setText(profile.getTel() == null ? "" : profile.getTel());
        imageUrlField.setText(profile.getImageUrl() == null ? "" : profile.getImageUrl());
    }

    @FXML
    public void onSave() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            statusLabel.setText("Please login.");
            return;
        }

        Map<String, Object> update = new HashMap<>();
        update.put("name", nameField.getText().trim());
        update.put("email", emailField.getText().trim());
        update.put("tel", phoneField.getText().trim());
        update.put("imageUrl", imageUrlField.getText().trim());

        statusLabel.setText("Saving...");
        userProfileService.updateProfile(user.id(), update)
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        statusLabel.setText("Saved.");
                        saved = true;
                        closeWindow();
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(resp.body());
                        statusLabel.setText("Save failed (" + resp.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Save error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    public void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage s = (Stage) nameField.getScene().getWindow();
        s.close();
    }

    public boolean isSaved() {
        return saved;
    }
}
