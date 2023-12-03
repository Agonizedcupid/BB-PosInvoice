module com.hady.ditf.possystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.desktop;
    requires org.apache.pdfbox;

    opens com.hady.ditf.possystem to javafx.fxml;
    opens com.hady.ditf.possystem.model to javafx.base;

    exports com.hady.ditf.possystem;
}