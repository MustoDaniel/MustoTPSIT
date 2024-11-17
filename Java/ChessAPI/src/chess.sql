-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Creato il: Ott 25, 2024 alle 11:52
-- Versione del server: 10.4.28-MariaDB
-- Versione PHP: 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `chess`
--
CREATE DATABASE IF NOT EXISTS `chess` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `chess`;

-- --------------------------------------------------------

--
-- Struttura della tabella `player`
--

CREATE TABLE `player` (
  `player_id` int(16) NOT NULL,
  `username` varchar(32) NOT NULL,
  `last_online` int(32) DEFAULT NULL,
  `rapidbest` int(6) DEFAULT NULL,
  `rapidlast` int(6) DEFAULT NULL,
  `bulletbest` int(6) DEFAULT NULL,
  `bulletlast` int(6) DEFAULT NULL,
  `blitzbest` int(6) DEFAULT NULL,
  `blitzlast` int(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `player`
--

INSERT INTO `player` (`player_id`, `username`, `last_online`, `rapidbest`, `rapidlast`, `bulletbest`, `bulletlast`, `blitzbest`, `blitzlast`) VALUES
(3889224, 'magnuscarlsen', 1729825184, 2906, 2977, 3212, 3390, 3293, 3377),
(15448422, 'hikaru', 1729721408, 2769, 2927, 3305, 3570, 3270, 3405),
(166212687, 'danielmusto', 1641410158, 540, 608, -1, -1, -1, -1),
(183493837, 'fedemaniglio', 1729776822, 1351, 1409, 1304, 1452, 1253, 1453);

--
-- Indici per le tabelle scaricate
--

--
-- Indici per le tabelle `player`
--
ALTER TABLE `player`
  ADD PRIMARY KEY (`player_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
