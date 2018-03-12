CREATE DATABASE IF NOT EXISTS moura
    DEFAULT CHARACTER SET=utf8mb4
    DEFAULT COLLATE=utf8mb4_unicode_ci;

USE moura;

CREATE TABLE `users` (
    `user_id` int(11) NOT NULL AUTO_INCREMENT,
    `username` varchar(60) NOT NULL,
    `email` varchar(256) NOT NULL,
    `password` varchar(128) NOT NULL,
    `alias` varchar(60) NOT NULL,
    `auth_token` varchar(60),
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `organizations` (
    `organization_id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(120) NOT NULL,
    `token` varchar(40) NOT NULL,
    PRIMARY KEY (`organization_id`),
    UNIQUE KEY `token` (`token`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `organization_relationships` (
    `relationship_id` int(11) NOT NULL AUTO_INCREMENT,
    `user_id` int(11) NOT NULL,
    `organization_id` int(11) NOT NULL,
    PRIMARY KEY (`relationship_id`),
    UNIQUE KEY `uq_user_id_organization_id` (`user_id`, `organization_id`),
    FOREIGN KEY `fk_org_rel_user_id` (`user_id`)
		REFERENCES `users`(`user_id`)
		ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    FOREIGN KEY `fk_org_rel_organization_id` (`organization_id`)
		REFERENCES `organizations`(`organization_id`)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `discussion_contexts` (
    `discussion_context_id` int(11) NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (`discussion_context_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `projects` (
    `project_id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(180) NOT NULL,
    `organization_id` int(11) NOT NULL,
    `discussion_context_id` int(11) NOT NULL,
    `parent_project_id` int(11) NULL,
    PRIMARY KEY (`project_id`),
    FOREIGN KEY `fk_projects_organization_id` (`organization_id`)
		REFERENCES `organizations` (`organization_id`)
		ON DELETE RESTRICT
        ON UPDATE RESTRICT,
	FOREIGN KEY `fk_projects_context_id` (`discussion_context_id`)
		REFERENCES `discussion_contexts` (`discussion_context_id`)
		ON DELETE RESTRICT
        ON UPDATE RESTRICT,
	FOREIGN KEY `fk_projects_parent_project_id` (`parent_project_id`)
		REFERENCES `projects` (`project_id`)
		ON DELETE RESTRICT
        ON UPDATE RESTRICT        
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `discussion_messages` (
    `discussion_message_id` int(11) NOT NULL AUTO_INCREMENT,
    `discussion_context_id` int(11) NOT NULL,
    `user_id` int(11) NOT NULL,
    `content` TEXT NOT NULL,
    `created_time` DATETIME NOT NULL,
    PRIMARY KEY (`discussion_message_id`),
    CONSTRAINT `fk_discussionMessage_discussion_context_id`
        FOREIGN KEY (`discussion_context_id`)
        REFERENCES `discussion_contexts` (`discussion_context_id`)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT `fk_discussion_message_user_id`
        FOREIGN KEY (`user_id`)
        REFERENCES `users` (`user_id`)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `chat_channels` (
    `chat_channel_id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(180) NOT NULL,
    `organization_id` int(11) NOT NULL,
    `discussion_context_id` int(11) NOT NULL,
    PRIMARY KEY (`chat_channel_id`),
    CONSTRAINT `fk_chat_channel_organization_id`
        FOREIGN KEY (`organization_id`)
        REFERENCES `organizations` (`organization_id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT `fk_chat_channel_discussion_context_id`
        FOREIGN KEY (`discussion_context_id`)
        REFERENCES `discussion_contexts` (`discussion_context_id`)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
