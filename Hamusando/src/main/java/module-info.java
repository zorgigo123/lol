module com.example.hamusando {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.hamusando to javafx.fxml;
    exports com.example.hamusando;
}