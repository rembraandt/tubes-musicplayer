module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;
    requires jaudiotagger;


    opens com.example.demo to javafx.fxml;
    exports com.example.demo;
}