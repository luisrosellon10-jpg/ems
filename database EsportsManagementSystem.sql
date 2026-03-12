-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 12, 2026 at 05:09 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `ems`
--

-- --------------------------------------------------------

--
-- Table structure for table `announcements`
--

CREATE TABLE `announcements` (
  `id` int(11) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `body` text DEFAULT NULL,
  `posted_at` datetime DEFAULT NULL,
  `author_id` int(11) DEFAULT NULL,
  `author_name` varchar(128) DEFAULT NULL,
  `target` varchar(16) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `audit_logs`
--

CREATE TABLE `audit_logs` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `actor_user_id` bigint(20) UNSIGNED DEFAULT NULL,
  `action` varchar(100) NOT NULL,
  `entity_type` varchar(50) DEFAULT NULL,
  `entity_id` bigint(20) UNSIGNED DEFAULT NULL,
  `metadata_text` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `matches`
--

CREATE TABLE `matches` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `tournament_id` bigint(20) UNSIGNED NOT NULL,
  `team_a_id` bigint(20) UNSIGNED DEFAULT NULL,
  `team_b_id` bigint(20) UNSIGNED DEFAULT NULL,
  `team_a` varchar(120) NOT NULL,
  `team_b` varchar(120) NOT NULL,
  `scheduled_at` datetime DEFAULT NULL,
  `score_a` int(11) DEFAULT NULL,
  `score_b` int(11) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'SCHEDULED',
  `stage` varchar(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `matches`
--

INSERT INTO `matches` (`id`, `tournament_id`, `team_a_id`, `team_b_id`, `team_a`, `team_b`, `scheduled_at`, `score_a`, `score_b`, `status`, `stage`, `created_at`, `updated_at`) VALUES
(1, 3, 5, 6, '22A1', '22A2', '2026-03-11 13:16:00', 3, 1, 'COMPLETED', 'GROUP', '2026-03-11 13:16:37', '2026-03-11 14:00:00');

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `type` varchar(50) NOT NULL,
  `title` varchar(200) NOT NULL,
  `message` text NOT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `otp_codes`
--

CREATE TABLE `otp_codes` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `otp_hash` varchar(100) NOT NULL,
  `expires_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `attempts` int(11) NOT NULL DEFAULT 0,
  `used` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `otp_codes`
--

INSERT INTO `otp_codes` (`id`, `user_id`, `otp_hash`, `expires_at`, `attempts`, `used`, `created_at`) VALUES
(1, 3, '$2a$12$uMGJjkxgO7OMUFWPbE.OCep1hBdlHQCd/g5eM0VnSN3IU3/FsIJHO', '2026-03-05 14:47:55', 0, 1, '2026-03-05 14:23:12'),
(2, 3, '$2a$12$7DJPx3gKpzH3mGkDaFmH6ukfBASI4UnbXDTd5uVA8msO9xxfENyqy', '2026-03-05 14:56:21', 0, 1, '2026-03-05 14:47:55'),
(3, 3, '$2a$12$MpI71zs48nAVazkUvWGTpuHAnS.hr97etZO6wLw8gOyBrPl7LJqjq', '2026-03-05 15:11:46', 0, 1, '2026-03-05 14:56:21'),
(4, 3, '$2a$12$.Xb5nKK0sGrxt8GxkrxLA.7wbxaHwbyMSp4rJOfSFUWWgL89e8oT2', '2026-03-05 15:12:10', 0, 1, '2026-03-05 15:11:46'),
(5, 3, '$2a$12$Y7ZsO16w61hKCXzSoHaGQO8DVHfji3R.o6L0MFR3J2RKLGCzMxxIe', '2026-03-05 15:15:24', 0, 1, '2026-03-05 15:12:10'),
(6, 3, '$2a$12$.uSQ0Pc2Bp.MqrrkNCzaE.RjAgdYQ4Q3Baym/Axan.orQCyYfSyFm', '2026-03-05 15:15:47', 0, 1, '2026-03-05 15:15:24'),
(7, 3, '$2a$12$WBYe4Cj65TVLXBQ2RKXm1uqyuIUXlYd3apTSw4WJqqDjdUY9PIL4C', '2026-03-05 15:16:14', 0, 1, '2026-03-05 15:15:47'),
(8, 3, '$2a$12$P3Mv5Ykf52R5Yvnn4YV4Ve6.9v0HUiu0HfLvVci2xQDi2cPXN5b3S', '2026-03-05 15:27:30', 0, 1, '2026-03-05 15:27:05');

-- --------------------------------------------------------

--
-- Table structure for table `players`
--

CREATE TABLE `players` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `team_id` bigint(20) UNSIGNED NOT NULL,
  `gamer_tag` varchar(80) NOT NULL,
  `full_name` varchar(150) DEFAULT NULL,
  `gender` varchar(20) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `contact_email` varchar(190) DEFAULT NULL,
  `phone` varchar(40) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `players`
--

INSERT INTO `players` (`id`, `team_id`, `gamer_tag`, `full_name`, `gender`, `date_of_birth`, `contact_email`, `phone`, `created_at`, `updated_at`) VALUES
(4, 5, 'jhinocide', 'jhinobili', NULL, NULL, 'jhino.noarin', NULL, '2026-03-11 13:15:37', '2026-03-12 05:14:16'),
(6, 5, 'mochi', 'Luis', NULL, NULL, 'luis.rosellon10@gmail.com', NULL, '2026-03-11 13:18:14', '2026-03-12 05:14:35'),
(7, 5, 'jinn', 'jhinobili', NULL, NULL, 'jhino', NULL, '2026-03-11 13:52:26', NULL),
(8, 5, 'lowiss', 'luis', NULL, NULL, 'lowiss', NULL, '2026-03-11 13:52:30', '2026-03-12 05:14:27'),
(9, 5, 'marj', 'marjolaine', NULL, NULL, 'marj', NULL, '2026-03-11 13:52:35', NULL),
(10, 6, 'allenn', 'allen sigle', NULL, NULL, 'allen', NULL, '2026-03-11 13:59:20', NULL),
(11, 6, 'cols', 'colin paul', NULL, NULL, 'colin', NULL, '2026-03-11 13:59:24', NULL),
(12, 6, 'law', 'lawrence', NULL, NULL, 'lawrence', NULL, '2026-03-11 13:59:28', NULL),
(13, 6, 'pao', 'paolo george', NULL, NULL, 'paolo', NULL, '2026-03-11 13:59:33', NULL),
(15, 6, 'dawdasd', 'luis', NULL, NULL, 'luis', NULL, '2026-03-12 05:39:30', NULL),
(18, 7, 'asdawdw', 'fafss', NULL, NULL, 'lowis', NULL, '2026-03-12 14:36:52', NULL),
(19, 7, 'mochi', 'luis emanuel rosellon', NULL, NULL, 'luisrosellon', NULL, '2026-03-12 14:37:02', NULL),
(20, 7, 'luis', 'luis', NULL, NULL, 'luiss', NULL, '2026-03-12 14:37:05', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `teams`
--

CREATE TABLE `teams` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `tournament_id` bigint(20) UNSIGNED NOT NULL,
  `name` varchar(120) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `teams`
--

INSERT INTO `teams` (`id`, `tournament_id`, `name`, `created_at`, `updated_at`) VALUES
(5, 3, '22A1', '2026-03-11 13:15:23', NULL),
(6, 3, '22A2', '2026-03-11 13:15:29', NULL),
(7, 4, 'team caden', '2026-03-12 14:36:25', NULL),
(8, 4, 'team komore', '2026-03-12 14:36:31', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `tournaments`
--

CREATE TABLE `tournaments` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `name` varchar(150) NOT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `tournaments`
--

INSERT INTO `tournaments` (`id`, `name`, `start_date`, `end_date`, `status`, `created_at`, `updated_at`) VALUES
(3, 'MATIBAY', NULL, NULL, 'ACTIVE', '2026-03-11 13:14:56', '2026-03-11 13:16:09'),
(4, 'adawdasdad', '2026-03-15', '2026-03-16', 'DRAFT', '2026-03-12 14:36:06', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(150) DEFAULT NULL,
  `phone` varchar(30) DEFAULT NULL,
  `gamer_tag` varchar(60) DEFAULT NULL,
  `gender` varchar(20) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `password_hash` varchar(100) NOT NULL,
  `role` enum('ADMIN','MANAGER','PLAYER') NOT NULL,
  `status` enum('PENDING','APPROVED','REJECTED') NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `last_login` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `email`, `full_name`, `phone`, `gamer_tag`, `gender`, `date_of_birth`, `address`, `password_hash`, `role`, `status`, `created_at`, `updated_at`, `last_login`) VALUES
(1, 'admin', NULL, NULL, NULL, NULL, NULL, NULL, '$2a$12$Wla4F63hV.UOsdM0lb7gAO2OOUz7LhOSAn7yg9F8S77QACCYQtJCi', 'ADMIN', 'APPROVED', '2026-03-05 06:55:30', '2026-03-12 14:42:48', '2026-03-12 14:42:48'),
(2, 'admin@ems.com', 'System Admin', NULL, NULL, NULL, NULL, NULL, '$2a$12$3wkyPiMZsKLxrQujG0BISuzV67XoB9b1R9myapCC6t5zCRSCTskDi', 'ADMIN', 'APPROVED', '2026-03-05 07:22:29', '2026-03-05 07:22:29', NULL),
(3, 'luis.rosellon10@gmail.com', 'Luis Rosellon', NULL, 'mochi', 'Male', '2004-03-15', 'Blk 9 Excess Lot Brgy. Sampaloc IV Zone 11 Dasmarinas, Cavite', '$2a$12$Ib.JP84wmrV5sjE9ArXQP.bpQRAmD1bNIQjQWBUNmj8vKtnJvHmRW', 'PLAYER', 'APPROVED', '2026-03-05 07:42:44', '2026-03-07 13:28:22', '2026-03-07 13:28:22'),
(4, 'marj.malto', 'marjolaine faye malto', NULL, 'faye', 'Female', '1995-03-14', 'GMA Cavite', '$2a$12$pefMyE/Fn4H9cM319oc1PeEKBLaui2ppnKgW7yWJZJ0ma5Rt0KliK', 'MANAGER', 'APPROVED', '2026-03-05 14:16:02', '2026-03-12 14:39:46', '2026-03-12 14:39:46'),
(5, 'luisrosellon', 'luis emanuel rosellon', NULL, 'mochi', 'Male', '2004-07-10', 'Dasmarinas, Cavite', '$2a$12$4OC7xRPHXkxY/K5KajDkLeCUZI5pehq43ZLHLSRWMnTT8fPYdRnc2', 'PLAYER', 'APPROVED', '2026-03-05 14:18:51', '2026-03-05 14:19:04', NULL),
(6, 'jhino.noarin', 'jhinobili noarin', NULL, 'jhinocide', 'Male', '2000-03-15', 'Las Pinas, Philippines', '$2a$12$UbVW/9bOQOpBoF48QB5LNORTtOATcDn2.NZu/Vf/H5.qK25ic3WL6', 'PLAYER', 'APPROVED', '2026-03-05 14:31:06', '2026-03-12 14:41:05', '2026-03-12 14:41:05'),
(7, 'jhino', 'jhinobili', NULL, 'jinn', 'Male', '2004-03-16', 'dyan lang', '$2a$12$651ODVSO13DabKG5DfNCP.oJZ/pbcpBn9uq.8yVeoQ3acBRjAnO6a', 'PLAYER', 'APPROVED', '2026-03-11 13:49:42', '2026-03-11 13:51:13', NULL),
(8, 'lowiss', 'luis rosellon', NULL, 'lowiss', 'Male', '2000-03-15', 'adawddasd', '$2a$12$XdE3ScnfIR8/vzi7w8729OAlBcldLLBdem/2hVUGtb57eWDU0Yi2.', 'PLAYER', 'APPROVED', '2026-03-11 13:50:21', '2026-03-11 13:51:25', NULL),
(9, 'marj', 'marjolaine', NULL, 'marj', 'Female', '2005-03-17', 'addawdsa', '$2a$12$JBC22X1iB3OBNTlIj2Wj4.ATIIDyLFlZfCUbvizA0pxU5Efe93t12', 'PLAYER', 'APPROVED', '2026-03-11 13:50:57', '2026-03-11 14:26:07', '2026-03-11 14:26:07'),
(10, 'allen', 'allen sigle', NULL, 'allenn', 'Male', '2026-03-18', 'dadsdd', '$2a$12$AW.JoN2Z47ETCTOUk.ENG.662tJ1QyA3ad19IwCWi.oeUOEmp6aQS', 'PLAYER', 'APPROVED', '2026-03-11 13:56:10', '2026-03-11 13:58:16', NULL),
(11, 'lawrence', 'lawrence', NULL, 'law', 'Male', '2026-03-10', 'dawda', '$2a$12$h5Ea1yDJQFe51z6UQBoADun1rtVeHeXbJyXxWHrDxKbF.Fk1FowNq', 'PLAYER', 'APPROVED', '2026-03-11 13:56:35', '2026-03-11 13:58:22', NULL),
(12, 'colin', 'colin paul', NULL, 'cols', 'Male', '2026-03-02', 'dasdasddawd', '$2a$12$9LlJdh4vq/AYIPhNCD4t.O7FJE0LYXMbEqyDYDXARFHihI4/gqVNC', 'PLAYER', 'APPROVED', '2026-03-11 13:57:06', '2026-03-11 13:58:29', NULL),
(13, 'paolo', 'paolo george', NULL, 'pao', 'Male', '2026-03-22', 'dasdwada', '$2a$12$a2z6P8.dYbj0d9xnurCpaO1oSBqQFf4XnBwbOjpvgxxCenXaVZ70e', 'PLAYER', 'APPROVED', '2026-03-11 13:57:31', '2026-03-11 13:58:34', NULL),
(14, 'luis', 'luis', NULL, 'dawdasd', 'Male', '2026-03-08', 'sadawdwd', '$2a$12$nzI8Px0vT8Elhy2bgkuU6.PfoGzgnfDbF9VlYXyQC4.hlxFtr6DV2', 'PLAYER', 'APPROVED', '2026-03-12 05:22:36', '2026-03-12 05:23:24', '2026-03-12 05:23:24'),
(15, 'luiss', 'luis', NULL, 'luis', 'Male', '2026-03-23', 'adwdawd', '$2a$12$TxhlIqG62XPYkRKH8cTlmuD6AYox98WpIdJ520alxlrCsluO6thHC', 'PLAYER', 'APPROVED', '2026-03-12 05:37:57', '2026-03-12 05:38:39', '2026-03-12 05:38:39'),
(16, 'lowis', 'fafss', NULL, 'asdawdw', 'Male', '2026-03-01', 'wefsefsefes', '$2a$12$37ZO.CgBkd2jX2iDt4NsfuCM1jnRUg.bXHPgvwh.MRufUMN/6/kza', 'PLAYER', 'APPROVED', '2026-03-12 05:45:17', '2026-03-12 05:46:00', '2026-03-12 05:46:00');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `announcements`
--
ALTER TABLE `announcements`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `audit_logs`
--
ALTER TABLE `audit_logs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_audit_actor` (`actor_user_id`),
  ADD KEY `idx_audit_action` (`action`);

--
-- Indexes for table `matches`
--
ALTER TABLE `matches`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_matches_tournament` (`tournament_id`),
  ADD KEY `idx_matches_scheduled_at` (`scheduled_at`),
  ADD KEY `idx_matches_team_a_id` (`team_a_id`),
  ADD KEY `idx_matches_team_b_id` (`team_b_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_notif_user` (`user_id`);

--
-- Indexes for table `otp_codes`
--
ALTER TABLE `otp_codes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_otp_user` (`user_id`),
  ADD KEY `idx_otp_expires` (`expires_at`);

--
-- Indexes for table `players`
--
ALTER TABLE `players`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_player_tag_per_team` (`team_id`,`gamer_tag`),
  ADD KEY `idx_players_team` (`team_id`);

--
-- Indexes for table `teams`
--
ALTER TABLE `teams`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_team_name_per_tournament` (`tournament_id`,`name`),
  ADD KEY `idx_teams_tournament` (`tournament_id`);

--
-- Indexes for table `tournaments`
--
ALTER TABLE `tournaments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_tournaments_status` (`status`),
  ADD KEY `idx_tournaments_start_date` (`start_date`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_users_email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `announcements`
--
ALTER TABLE `announcements`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `audit_logs`
--
ALTER TABLE `audit_logs`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `matches`
--
ALTER TABLE `matches`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `otp_codes`
--
ALTER TABLE `otp_codes`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `players`
--
ALTER TABLE `players`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `teams`
--
ALTER TABLE `teams`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `tournaments`
--
ALTER TABLE `tournaments`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `audit_logs`
--
ALTER TABLE `audit_logs`
  ADD CONSTRAINT `fk_audit_actor` FOREIGN KEY (`actor_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `matches`
--
ALTER TABLE `matches`
  ADD CONSTRAINT `fk_matches_team_a` FOREIGN KEY (`team_a_id`) REFERENCES `teams` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_matches_team_b` FOREIGN KEY (`team_b_id`) REFERENCES `teams` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_matches_tournament` FOREIGN KEY (`tournament_id`) REFERENCES `tournaments` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `otp_codes`
--
ALTER TABLE `otp_codes`
  ADD CONSTRAINT `fk_otp_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `players`
--
ALTER TABLE `players`
  ADD CONSTRAINT `fk_players_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `teams`
--
ALTER TABLE `teams`
  ADD CONSTRAINT `fk_teams_tournament` FOREIGN KEY (`tournament_id`) REFERENCES `tournaments` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
