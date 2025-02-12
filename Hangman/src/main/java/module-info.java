module ru.kpfu.hangman {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.kpfu.hangman to javafx.fxml;
    exports ru.kpfu.hangman;
}