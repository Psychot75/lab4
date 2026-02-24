module etardif.etsmtl.lab4 {
    requires javafx.controls;
    requires javafx.fxml;


    opens etardif.etsmtl.lab4 to javafx.fxml;
    exports etardif.etsmtl.lab4;
}