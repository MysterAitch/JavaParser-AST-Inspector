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
    private final NotificationGroup traceNotificationGroup;
    @NotNull
    private final NotificationGroup debugNotificationGroup;
    @NotNull
    private final NotificationGroup infoNotificationGroup;
    @NotNull
    private final NotificationGroup warnNotificationGroup;
    @NotNull
    private final NotificationGroup errorNotificationGroup;


    //    public NotificationLogger(@NotNull Class<T> clazz, @NotNull String displayId) {
    public NotificationLogger(@NotNull Class<?> clazz) {
        this.logger = Logger.getInstance(clazz.getName());
        this.traceNotificationGroup = new NotificationGroup(
                clazz.getName() + " (trace)",
                NotificationDisplayType.NONE,
                true
        );
        this.debugNotificationGroup = new NotificationGroup(
                clazz.getName() + " (debug)",
                NotificationDisplayType.NONE,
                true
        );
        this.infoNotificationGroup = new NotificationGroup(
                clazz.getName() + " (information)",
                NotificationDisplayType.NONE,
                true
        );
        this.warnNotificationGroup = new NotificationGroup(
                clazz.getName() + " (warnings)",
                NotificationDisplayType.BALLOON,
                true
        );
        this.errorNotificationGroup = new NotificationGroup(
                clazz.getName() + " (errors)",
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
        this.traceNotificationGroup
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
        this.debugNotificationGroup
                .createNotification("DEBUG", this.getSubtitle(project), messageContent, NotificationType.INFORMATION)
                .notify(project);
    }
    public void debug(@NotNull String messageContent, Throwable e) {
        this.debug(null, messageContent, e);
    }
    public void debug(@Nullable Project project, @NotNull String messageContent, Throwable e) {
        this.logger.debug(messageContent, e);
        this.debugNotificationGroup
                .createNotification("DEBUG", this.getSubtitle(project), messageContent, NotificationType.INFORMATION)
                .notify(project);
    }

    public void info(@NotNull String messageContent) {
        this.info(null, messageContent);
    }
    public void info(@Nullable Project project, @NotNull String messageContent) {
        this.logger.info(messageContent);
        this.infoNotificationGroup
                .createNotification("INFORMATION", this.getSubtitle(project), messageContent, NotificationType.INFORMATION)
                .notify(project);
    }
    public void info(@NotNull String messageContent, Throwable e) {
        this.info(null, messageContent, e);
    }
    public void info(@Nullable Project project, @NotNull String messageContent, Throwable e) {
        this.logger.info(messageContent, e);
        this.infoNotificationGroup
                .createNotification("INFORMATION", this.getSubtitle(project), messageContent, NotificationType.INFORMATION)
                .notify(project);
    }

    public void warn(@NotNull String messageContent) {
        this.warn(null, messageContent);
    }
    public void warn(@Nullable Project project, @NotNull String messageContent) {
        this.logger.warn(messageContent);
        this.warnNotificationGroup
                .createNotification("WARNING", this.getSubtitle(project), messageContent, NotificationType.WARNING)
                .notify(project);
    }
    public void warn(@NotNull String messageContent, Throwable e) {
        this.warn(null, messageContent, e);
    }
    public void warn(@Nullable Project project, @NotNull String messageContent, Throwable e) {
        this.logger.warn(messageContent, e);
        this.warnNotificationGroup
                .createNotification("WARNING", this.getSubtitle(project), messageContent, NotificationType.WARNING)
                .notify(project);
    }

    public void error(@NotNull String messageContent) {
        this.error(null, messageContent);
    }
    public void error(@Nullable Project project, @NotNull String messageContent) {
        this.logger.error(messageContent);
        this.errorNotificationGroup
                .createNotification("ERROR", this.getSubtitle(project), messageContent, NotificationType.ERROR)
                .notify(project);
    }
    public void error(@NotNull String messageContent, Throwable e) {
        this.error(null, messageContent, e);
    }
    public void error(@Nullable Project project, @NotNull String messageContent, Throwable e) {
        this.logger.error(messageContent, e);
        this.errorNotificationGroup
                .createNotification("ERROR", this.getSubtitle(project), messageContent, NotificationType.ERROR)
                .notify(project);
    }

}
