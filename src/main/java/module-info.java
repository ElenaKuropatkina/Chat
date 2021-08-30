module com.elena.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;


    opens com.elena.chat to javafx.graphics;
    exports com.elena.chat;
    opens com.elena.chat.client to javafx.fxml;
    exports com.elena.chat.client;
}