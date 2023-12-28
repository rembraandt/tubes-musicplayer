package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicPlayerController {

    @FXML private Label durationLabel;
    @FXML private Button playButton;
    @FXML private ListView <String> songList;
    @FXML private Slider volumeSlider;
    @FXML private ImageView albumCover;
    @FXML private Label songName;
    @FXML private Label ArtistName;
    @FXML private Slider timeSlider;




    private MediaPlayer mediaPlayer;
    private List<File> playlist = new ArrayList<>();
    private int currentSongIndex = -1;




    @FXML void initialize() {
        timeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null && timeSlider.isValueChanging()) {
                mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(newValue.doubleValue() / 100.0));
            }
        });

        volumeSlider.valueChangingProperty().addListener((observable, oldValue, isChanging) -> {
            if (!isChanging) {
                double sliderValue = volumeSlider.getValue();
                double volume = sliderValue / 100.0; // Normalisasi nilai slider menjadi rentang 0.0 - 1.0

                if (mediaPlayer != null) {
                    if (sliderValue == 0) {
                        mediaPlayer.setVolume(0); // Jika slider di ujung kiri, atur volume menjadi 0
                    } else {
                        mediaPlayer.setVolume(volume); // Atur volume berdasarkan posisi slider
                    }
                }
            }
        });

        songList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2){
                int selectedIndex = songList.getSelectionModel().getSelectedIndex();
                if (selectedIndex != -1) {
                    currentSongIndex = selectedIndex;
                    playSelectedMusic(playlist.get(selectedIndex));
                }
            }
        });

        songList.setOnDragOver(event -> {
            if (event.getGestureSource() != songList && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        songList.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasFiles()) {
                List<File> files = dragboard.getFiles();
                files.forEach(this::addToPlaylist); // Tambahkan file yang di-drop ke dalam playlist
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

    }

    @FXML void play() {
        if (mediaPlayer != null) {
            MediaPlayer.Status status = mediaPlayer.getStatus();
            if (status == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playButton.setText("Play");
            } else {
                mediaPlayer.play();
                playButton.setText("Pause");
            }
        }
    }

    @FXML void next() {
        if (!playlist.isEmpty()) {
            mediaPlayer.stop();
            currentSongIndex = (currentSongIndex + 1) % playlist.size();
            playSelectedMusic(playlist.get(currentSongIndex));
            songList.getSelectionModel().select(currentSongIndex);
        }
    }

    @FXML void previous() {
        if (!playlist.isEmpty()) {
            mediaPlayer.stop();
            currentSongIndex = (currentSongIndex - 1 + playlist.size()) % playlist.size();
            playSelectedMusic(playlist.get(currentSongIndex));
            songList.getSelectionModel().select(currentSongIndex);
        }
    }

    @FXML void chooseMusic() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Music Files");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Audio Files", "*.mp3", "*.m4a")
        );
        Stage stage = (Stage) playButton.getScene().getWindow();
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null) {
            selectedFiles.forEach(this::addToPlaylist);
        }
    }


    private int songCounter = 1;
    private void addToPlaylist(File file) {
        playlist.add(file);
        String songNumber = Integer.toString(songCounter); // Nomor urut lagu
        String songName = file.getName();
        songList.getItems().add(songNumber + ". " + songName);
        songCounter++;
        if (currentSongIndex == -1) {
            currentSongIndex = 0;
            playSelectedMusic(playlist.get(currentSongIndex));
        }
    }
    private String formatDuration(javafx.util.Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }
    private void updateDurationLabel() {
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                javafx.util.Duration currentTime = mediaPlayer.getCurrentTime();
                javafx.util.Duration totalDuration = mediaPlayer.getTotalDuration();
                durationLabel.setText(formatDuration(currentTime) + " / " + formatDuration(totalDuration));
            }
        });

        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null && !timeSlider.isValueChanging()) {
                double currentTimeMs = mediaPlayer.getCurrentTime().toMillis();
                double totalTimeMs = mediaPlayer.getTotalDuration().toMillis();
                double percentage = (currentTimeMs / totalTimeMs) * 100.0;
                timeSlider.setValue(percentage);
            }
        });
    }
    private void playSelectedMusic(File file) {
        Media media = new Media(file.toURI().toString());

        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            if (tag != null) {
                String title = tag.getFirst(FieldKey.TITLE);
                String artist = tag.getFirst(FieldKey.ALBUM_ARTIST);

                songName.setText(title != null ? title : file.getName());
                ArtistName.setText(artist != null ? artist : file.getName());

                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] imageData = artwork.getBinaryData();
                    Image image = new Image(new ByteArrayInputStream(imageData));
                    albumCover.setImage(image);
                } else {
                    Image defaultImage = new Image(new File("D:\\img def").toURI().toString());
                    albumCover.setImage(defaultImage);
                }
            } else {
                songName.setText(file.getName());
                Image defaultImage = new Image(new File("D:\\img def").toURI().toString());
                albumCover.setImage(defaultImage);
            }

        } catch (Exception e) {
            // Tangani exception di sini
            e.printStackTrace();
            // Misalnya, tampilkan pesan kepada pengguna atau gunakan gambar default
            songName.setText(file.getName());
            Image defaultImage = new Image(new File("D:\\img def").toURI().toString());
            albumCover.setImage(defaultImage);
        }


        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnReady(() -> {
            mediaPlayer.play();
            playButton.setText("Pause");
            updateDurationLabel();
        });
        mediaPlayer.setOnEndOfMedia(this::next);


    }




}
// drftgyhujiko,lyhuijhuajis