package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.notifications;

import com.intellij.notification.Notification;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NotificationsNotifier {

    @NotNull
    Notification notify(@NotNull String content);

    @NotNull
    Notification notify(@Nullable Project project, @NotNull String content);

}
