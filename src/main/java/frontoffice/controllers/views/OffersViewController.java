package frontoffice.controllers.views;

import frontoffice.models.Offer;
import frontoffice.services.OfferService;
import frontoffice.services.UserOfferService;
import frontoffice.utils.HttpErrorParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class OffersViewController {
    @FXML
    private TextField searchField;
    @FXML
    private TableView<Offer> offersTable;
    @FXML
    private TableColumn<Offer, Integer> colId;
    @FXML
    private TableColumn<Offer, String> colTitle;
    @FXML
    private TableColumn<Offer, Double> colDiscount;
    @FXML
    private Label statusLabel;

    private final OfferService offerService = new OfferService();
    private final UserOfferService userOfferService = new UserOfferService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discountPercentage"));
        onRefresh();
    }

    @FXML
    private void onSearch() {
        statusLabel.setText("Searching...");
        offerService.searchOffers(searchField.getText())
                .thenAccept(list -> Platform.runLater(() -> {
                    offersTable.setItems(FXCollections.observableArrayList(list));
                    statusLabel.setText("");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Search error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onRefresh() {
        statusLabel.setText("Loading...");
        offerService.getAllOffers()
                .thenAccept(list -> Platform.runLater(() -> {
                    offersTable.setItems(FXCollections.observableArrayList(list));
                    statusLabel.setText("");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Load error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onClaimSelected() {
        Offer selected = offersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an offer first.");
            return;
        }

        statusLabel.setText("Claiming...");
        userOfferService.claimOffer(selected.getId())
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        statusLabel.setText("Offer claimed.");
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(resp.body());
                        statusLabel.setText("Claim failed (" + resp.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Claim error: " + ex.getMessage()));
                    return null;
                });
    }
}
