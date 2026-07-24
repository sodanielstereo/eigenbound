module com.eigenbound {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.eigenbound to javafx.fxml;
    exports com.eigenbound;
}
