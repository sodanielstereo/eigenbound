module com.eigenbound {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.eigenbound;

    opens com.eigenbound.presentation.canvas
            to javafx.fxml;

    opens com.eigenbound.presentation.laboratory
            to javafx.fxml;

    opens com.eigenbound.presentation.expedition
            to javafx.fxml;
}