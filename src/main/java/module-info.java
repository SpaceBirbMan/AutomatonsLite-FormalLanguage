module stud.atmt.atmtlite {
    requires javafx.controls;
    requires javafx.fxml;


    opens stud.atmt.atmtlite to javafx.fxml;
    exports stud.atmt.atmtlite;
}