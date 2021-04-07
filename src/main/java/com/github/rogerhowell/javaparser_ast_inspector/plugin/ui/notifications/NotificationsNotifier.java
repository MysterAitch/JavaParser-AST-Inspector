package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.notifications;

import com.intellij.notification.Notification;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NotificationsNotifier {

    @NotNull
    default Notification notify(@NotNull final String content) {
        return this.notify(null, content);
    }

    @NotNull
    Notification notify(@Nullable Project project, @NotNull String content);

}
