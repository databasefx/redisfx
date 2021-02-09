package cn.navigational.redisfx;

import cn.navigational.redisfx.editor.EditorPlatform;
import cn.navigational.redisfx.util.MessageBox;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AppPlatform {
    private static String applicationDataFolder;
    private static String userLibraryFolder;
    private static String messageBoxFolder;
    private static MessageBox<MessageBoxMessage> messageBox;

    public static synchronized String getApplicationDataFolder() {

        if (applicationDataFolder == null) {
            final String appName = "RedisFx";

            if (EditorPlatform.IS_WINDOWS()) {
                applicationDataFolder
                        = System.getenv("APPDATA") + "\\" + appName;
            } else if (EditorPlatform.IS_MAC()) {
                applicationDataFolder
                        = System.getProperty("user.home")
                        + "/Library/Application Support/"
                        + appName;
            } else if (EditorPlatform.IS_LINUX()) {
                applicationDataFolder
                        = System.getProperty("user.home") + "/.redsiFx";
            }
        }

        assert applicationDataFolder != null;

        return applicationDataFolder;
    }


    public static synchronized String getUserLibraryFolder() {

        if (userLibraryFolder == null) {
            userLibraryFolder = getApplicationDataFolder() + "/Library";
        }

        return userLibraryFolder;
    }

    public static boolean requestStart(
            AppNotificationHandler notificationHandler, Application.Parameters parameters)
            throws IOException {
        if (EditorPlatform.isAssertionEnabled()) {
            // Development mode : we do not delegate to the existing instance
            notificationHandler.handleLaunch(parameters.getUnnamed());
            return true;
        } else {
            return requestStartGeneric(notificationHandler, parameters);
        }
    }

    public interface AppNotificationHandler {
        /**
         * 启动成功时调用该函数
         *
         * @param files 用户传递进来文件列表
         */
        void handleLaunch(List<String> files);

        void handleOpenFilesAction(List<String> files);

        void handleMessageBoxFailure(Exception x);
    }

    private static synchronized boolean requestStartGeneric(
            AppNotificationHandler notificationHandler, Application.Parameters parameters)
            throws IOException {
        assert notificationHandler != null;
        assert parameters != null;
        assert messageBox == null;

        try {
            Files.createDirectories(Paths.get(getMessageBoxFolder()));
        } catch (FileAlreadyExistsException x) {
            // Fine
        }

        boolean result;
        messageBox = new MessageBox<>(getMessageBoxFolder(), MessageBoxMessage.class, 1000);
        ArrayList<String> parametersUnnamed = new ArrayList<>(parameters.getUnnamed());
        if (EditorPlatform.IS_MAC()) {
            parametersUnnamed.removeIf(p -> p.startsWith("-psn"));
        }
        if (messageBox.grab(new MessageBoxDelegate(notificationHandler))) {
            notificationHandler.handleLaunch(parametersUnnamed);
            result = true;
        } else {
            result = false;
            final MessageBoxMessage unamedParameters
                    = new MessageBoxMessage(parametersUnnamed);
            try {
                messageBox.sendMessage(unamedParameters);
            } catch (InterruptedException x) {
                throw new IOException(x);
            }
        }

        return result;
    }

    private static String getMessageBoxFolder() {
        if (messageBoxFolder == null) {
            messageBoxFolder = getApplicationDataFolder() + "/MB";
        }

        return messageBoxFolder;
    }

    private static class MessageBoxMessage extends ArrayList<String> {
        static final long serialVersionUID = 10;

        public MessageBoxMessage(List<String> strings) {
            super(strings);
        }

        ;
    }

    ;

    private static class MessageBoxDelegate implements MessageBox.Delegate<MessageBoxMessage> {

        private final AppNotificationHandler eventHandler;

        public MessageBoxDelegate(AppNotificationHandler eventHandler) {
            assert eventHandler != null;
            this.eventHandler = eventHandler;
        }

        /*
         * MessageBox.Delegate
         */

        @Override
        public void messageBoxDidGetMessage(MessageBoxMessage message) {
            assert !Platform.isFxApplicationThread();
            Platform.runLater(() -> eventHandler.handleOpenFilesAction(message));
        }

        @Override
        public void messageBoxDidCatchException(Exception x) {
            assert !Platform.isFxApplicationThread();
            Platform.runLater(() -> eventHandler.handleMessageBoxFailure(x));
        }

    }
}
