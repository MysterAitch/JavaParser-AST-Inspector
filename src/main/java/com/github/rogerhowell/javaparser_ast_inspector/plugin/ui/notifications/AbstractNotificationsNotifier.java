package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.notifications;

import com.intellij.notification.Notification;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractNotificationsNotifier implements NotificationsNotifier {

    @NotNull
    @Override
    public Notification notify(@NotNull final String content) {
        return this.notify(null, content);
    }


    @NotNull
    @Override
    public Notification notify(@Nullable final Project project, @NotNull final String content) {
        return null;
    }

}
