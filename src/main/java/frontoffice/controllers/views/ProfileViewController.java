package frontoffice.controllers.views;

import frontoffice.models.UserProfile;
import frontoffice.services.UserProfileService;
import frontoffice.utils.HttpErrorParser;
import frontoffice.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class ProfileViewController {
    @FXML
    private Label nameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private ImageView imageView;
    private String currentImageUrl;
    @FXML
    private Label statusLabel;

    private final UserProfileService userProfileService = new UserProfileService();

    @FXML
    public void initialize() {
        onRefresh();
    }

    @FXML
    private void onRefresh() {
        statusLabel.setText("Loading...");
        userProfileService.getMe()
                .thenAccept(profile -> Platform.runLater(() -> {
                    if (profile != null) {
                        setFields(profile);
                        statusLabel.setText("");
                    } else {
                        var sessionUser = SessionManager.getInstance().getCurrentUser();
                        if (sessionUser != null) {
                            UserProfile u = new UserProfile();
                            u.setId(sessionUser.id());
                            u.setName(sessionUser.name());
                            u.setEmail(sessionUser.email());
                            setFields(u);
                            statusLabel.setText("");
                        } else {
                            statusLabel.setText("Failed to load profile.");
                        }
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Load error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onSave() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            statusLabel.setText("Please login.");
            return;
        }

        Map<String, Object> update = new HashMap<>();
        update.put("name", trim(nameLabel.getText()));
        update.put("email", trim(emailLabel.getText()));
        update.put("tel", trim(phoneLabel.getText()));
        update.put("imageUrl", trim(currentImageUrl));

        statusLabel.setText("Saving...");
        userProfileService.updateProfile(user.id(), update)
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        statusLabel.setText("Saved.");
                        onRefresh();
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
    private void onEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/views/profileEditDialog.fxml"));
            Parent root = loader.load();
            frontoffice.controllers.views.ProfileEditController ctrl = loader.getController();
            // prepare current profile
            UserProfile current = new UserProfile();
            current.setName(nameLabel.getText());
            current.setEmail(emailLabel.getText());
            current.setTel(phoneLabel.getText());
            current.setImageUrl(currentImageUrl);
            ctrl.setProfile(current);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Edit Profile");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            if (ctrl.isSaved()) {
                onRefresh();
            }
        } catch (Exception e) {
            statusLabel.setText("Could not open edit dialog: " + e.getMessage());
        }
    }

    private void setFields(UserProfile profile) {
        if (profile == null) {
            nameLabel.setText("");
            emailLabel.setText("");
            phoneLabel.setText("");
            imageView.setImage(null);
            currentImageUrl = null;
            return;
        }

        nameLabel.setText(profile.getName() == null ? "" : profile.getName());
        emailLabel.setText(profile.getEmail() == null ? "" : profile.getEmail());
        phoneLabel.setText(profile.getTel() == null ? "" : profile.getTel());
        currentImageUrl = profile.getImageUrl();
        if (currentImageUrl == null || currentImageUrl.isBlank()) {
            imageView.setImage(null);
            statusLabel.setText("");
        } else {
            try {
                statusLabel.setText("Loading image...");
                Image img = new Image(currentImageUrl, true);
                img.progressProperty().addListener((obs, oldV, newV) -> {
                    if (newV != null && newV.doubleValue() >= 1.0) {
                        Platform.runLater(() -> statusLabel.setText(""));
                    }
                });
                imageView.imageProperty().addListener((obs, oldImg, newImg) -> {
                    if (newImg != null && newImg.isError()) {
                        Platform.runLater(() -> statusLabel.setText("Image load failed."));
                    }
                });
                imageView.setImage(img);
            } catch (Exception e) {
                imageView.setImage(null);
                statusLabel.setText("Image load error: " + e.getMessage());
            }
        }
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
