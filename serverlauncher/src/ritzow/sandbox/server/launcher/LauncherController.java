package ritzow.sandbox.server.launcher;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

class LauncherController {
	@FXML TextField ipField, portField, worldFileField;
	@FXML Button startButton, browseButton;
	@FXML Text errorMessageText;

	private final Stage stage;

	public LauncherController() {
		try(var in = Files.newInputStream(Path.of("bin/launcher_main.fxml"));
			var iconIn = Files.newInputStream(Path.of("bin/greenFace.png"))) {
			var loader = new FXMLLoader();
			loader.setController(this);
			Control root = loader.load(in);
			stage = new Stage(StageStyle.DECORATED);
			stage.setForceIntegerRenderScale(true);
			stage.getIcons().add(new Image(iconIn));
			ipField.textProperty().addListener(this::onIpFieldChange);
			startButton.addEventHandler(ActionEvent.ACTION, this::onStartButtonPress);
			browseButton.addEventHandler(ActionEvent.ACTION, this::onBrowseButtonPress);
			Rectangle2D screenSize = Screen.getPrimary().getBounds();
			stage.setWidth(screenSize.getWidth()/2);
			stage.setHeight(screenSize.getHeight()/2);
			stage.setScene(new Scene(root));
			stage.setTitle("Sandbox2D Server");
			stage.show();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings({"unused", "TypeParameterExtendsFinalClass"})
	private void onIpFieldChange(ObservableValue<? extends String> change,
								 String oldText, String newText) {
		if(ipField.getEffect() != null)
			ipField.setEffect(null);
	}

	private void onStartButtonPress(@SuppressWarnings("unused") ActionEvent event) {
		try {
			startButton.setDisable(true);
			InetAddress ip = InetAddress.getByName(ipField.getText().isEmpty() ?
					ipField.getPromptText() : ipField.getText());
			int port = Integer.parseUnsignedInt(portField.getText().isEmpty() ?
					portField.getPromptText() : portField.getText());
			new Thread(() -> {
				try {
					StartLauncher.runServer(ip, port);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}, "Server Update Thread").start();
		} catch(UnknownHostException e) {
			errorMessageText.setText("Invalid IP address");
			ipField.setEffect(new DropShadow(5, Color.RED));
		} catch(NumberFormatException e) {
			errorMessageText.setText("Invalid port number");
		}
	}

	private void onBrowseButtonPress(@SuppressWarnings("unused") ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select World File");
		chooser.setInitialDirectory(Path.of(".").toFile());
		chooser.getExtensionFilters().add(
			new FileChooser.ExtensionFilter("World Files", List.of("*.dat","*.world")));
		File worldFile = chooser.showOpenDialog(stage);
		if(worldFile != null) {
			worldFileField.setText(worldFile.getAbsolutePath());
		}
	}
}