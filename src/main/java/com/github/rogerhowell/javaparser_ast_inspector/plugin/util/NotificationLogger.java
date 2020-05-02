package com.github.rogerhowell.javaparser_ast_inspector.plugin.util;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class NotificationLogger {

    @NotNull
    private final Logger logger;

    @NotNull
    private final NotificationGroup notificationGroup;


//    public NotificationLogger(@NotNull Class<T> clazz, @NotNull String displayId) {
    public NotificationLogger(@NotNull Class<?> clazz) {
        this.logger = Logger.getInstance(clazz.getName());
        this.notificationGroup = new NotificationGroup(
                clazz.getName(),
                NotificationDisplayType.STICKY_BALLOON,
                true
        );
    }
    
    private Optional<String> projectName(@Nullable Project project) {
        if(project == null) {
            return Optional.empty();
        }
        return Optional.of(project.getName());
    }

    @NotNull
    private String getSubtitle(@Nullable final Project project) {
        return "Project: " + this.projectName(project).orElse("<unspecified>");
    }



    public void trace(@NotNull String messageContent) {
        this.trace(null, messageContent);
    }
    public void trace(@Nullable Project project, @NotNull String messageContent) {
        this.logger.trace(messageContent);
        this.notificationGroup
                .createNotification("TRACE", this.getSubtitle(project), messageContent, NotificationType.INFORMATION)
                .notify(project);
    }
//    public void trace(@NotNull String messageContent, Throwable e) {
//        this.trace(null, messageContent, e);
//    }
//    public void trace(@Nullable Project project, @NotNull String messageContent, Throwable e) {
//        final String projectName = (project == null) ? "<unspecified>" : project.getName();
//        this.logger.trace(messageContent, e);
//        notificationGroup.createNotification("Title", "Project: " + projectName(project).orElse("<unspecified>"), messageContent).notify(project);
//    }

    public void debug(@NotNull String messageContent) {
        this.debug(null, messageContent);
    }
    public void debug(@Nullable Project project, @NotNull String messageContent) {
        this.logger.debug(messageContent);
        this.notificationGroup
                .createNotification("DEBUG", this.getSubtitle(project), messageContent, NotificationType.INFORMATION)
                .notify(project);
    }
    public void debug(@NotNull String messageContent, Throwable e) {
        this.debug(null, messageContent, e);
    }
    public void debug(@Nullable Project project, @NotNull String messageContent, Throwable e) {
        this.logger.debug(messageContent, e);
        this.notificationGroup
                .createNotification("DEBUG", this.getSubtitle(project), messageContent, NotificationType.INFORMATION)
                .notify(project);
    }

    public void info(@NotNull String messageContent) {
        this.info(null, messageContent);
    }
    public void info(@Nullable Project project, @NotNull String messageContent) {
        this.logger.info(messageContent);
        this.notificationGroup
                .createNotification("INFORMATION", this.getSubtitle(project), messageContent, NotificationType.INFORMATION)
                .notify(project);
    }
    public void info(@NotNull String messageContent, Throwable e) {
        this.info(null, messageContent, e);
    }
    public void info(@Nullable Project project, @NotNull String messageContent, Throwable e) {
        this.logger.info(messageContent, e);
        this.notificationGroup
                .createNotification("INFORMATION", this.getSubtitle(project), messageContent, NotificationType.INFORMATION)
                .notify(project);
    }

    public void warn(@NotNull String messageContent) {
        this.warn(null, messageContent);
    }
    public void warn(@Nullable Project project, @NotNull String messageContent) {
        this.logger.warn(messageContent);
        this.notificationGroup
                .createNotification("WARNING", this.getSubtitle(project), messageContent, NotificationType.WARNING)
                .notify(project);
    }
    public void warn(@NotNull String messageContent, Throwable e) {
        this.warn(null, messageContent, e);
    }
    public void warn(@Nullable Project project, @NotNull String messageContent, Throwable e) {
        this.logger.warn(messageContent, e);
        this.notificationGroup
                .createNotification("WARNING", this.getSubtitle(project), messageContent, NotificationType.WARNING)
                .notify(project);
    }

    public void error(@NotNull String messageContent) {
        this.error(null, messageContent);
    }
    public void error(@Nullable Project project, @NotNull String messageContent) {
        this.logger.error(messageContent);
        this.notificationGroup
                .createNotification("ERROR", this.getSubtitle(project), messageContent, NotificationType.ERROR)
                .notify(project);
    }
    public void error(@NotNull String messageContent, Throwable e) {
        this.error(null, messageContent, e);
    }
    public void error(@Nullable Project project, @NotNull String messageContent, Throwable e) {
        this.logger.error(messageContent, e);
        this.notificationGroup
                .createNotification("ERROR", this.getSubtitle(project), messageContent, NotificationType.ERROR)
                .notify(project);
    }

}
