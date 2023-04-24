module ru.avklimenko.life {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires com.almasb.fxgl.all;

    opens ru.avklimenko.life to javafx.fxml;
    exports ru.avklimenko.life;
}