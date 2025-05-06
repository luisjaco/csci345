CREATE DATABASE  IF NOT EXISTS `instant_messenger` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `instant_messenger`;
-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: instant_messenger
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `message_id` int NOT NULL AUTO_INCREMENT,
  `sender_id` int NOT NULL,
  `receiver_id` int NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `message_content` text NOT NULL,
  PRIMARY KEY (`message_id`),
  KEY `messages_ibfk_1` (`sender_id`),
  KEY `messages_ibfk_2` (`receiver_id`),
  CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=116 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES (45,6,9,'2025-05-05 03:00:38','whats good gangalang'),(46,9,6,'2025-05-05 03:00:55','yo'),(47,9,6,'2025-05-05 03:00:57','gurt'),(48,6,9,'2025-05-05 03:01:01','gurt: yo'),(49,9,6,'2025-05-05 03:01:19',':)'),(50,9,6,'2025-05-05 03:01:21',';('),(51,9,6,'2025-05-05 03:01:28','8=========D'),(52,6,9,'2025-05-05 03:01:33','LMAO'),(53,6,9,'2025-05-05 03:01:38','alright bye'),(54,6,14,'2025-05-05 03:09:56','hi'),(55,6,14,'2025-05-05 03:10:02','yo'),(56,6,14,'2025-05-05 03:10:04','bye'),(57,14,6,'2025-05-05 03:10:31','yo'),(58,6,14,'2025-05-05 03:10:39','whats up'),(59,6,14,'2025-05-05 03:11:06','bye'),(60,14,15,'2025-05-05 03:14:39','hi'),(61,14,15,'2025-05-05 03:14:41','hi'),(62,14,15,'2025-05-05 03:14:50','0001'),(63,15,14,'2025-05-05 03:14:55','hi'),(64,15,14,'2025-05-05 03:14:57','bye'),(65,6,8,'2025-05-05 03:42:45','yoooo'),(66,6,8,'2025-05-05 03:42:51','hi'),(67,8,6,'2025-05-05 03:43:08','btw your username is my username'),(68,6,8,'2025-05-05 03:43:17','lmao i also just noticed that'),(69,8,6,'2025-05-05 03:43:40','weird....Daniel.'),(70,6,8,'2025-05-05 03:43:52','alright im gonna end the connection'),(71,6,9,'2025-05-05 03:47:02','hi'),(72,9,6,'2025-05-05 03:47:06','yo'),(73,6,9,'2025-05-05 03:47:25','alright fixed the bug'),(74,6,9,'2025-05-05 04:08:29','hi'),(75,9,6,'2025-05-05 04:08:33','hey'),(76,14,15,'2025-05-05 04:08:49','sup'),(77,15,14,'2025-05-05 04:08:51','yo'),(78,6,9,'2025-05-05 16:34:18','hey!'),(79,9,6,'2025-05-05 16:34:20','hey!'),(80,14,15,'2025-05-05 16:40:52','hi'),(81,14,15,'2025-05-05 16:40:56','hi'),(82,6,14,'2025-05-05 16:41:21','hi'),(83,6,14,'2025-05-05 16:41:22','hi'),(84,6,14,'2025-05-05 16:41:23','hi'),(85,14,6,'2025-05-05 16:41:25','hi'),(86,14,6,'2025-05-05 16:41:49','hi'),(87,14,6,'2025-05-05 16:41:50','ok'),(88,14,6,'2025-05-05 16:52:48','hi'),(89,6,14,'2025-05-05 16:52:51','yo'),(90,6,14,'2025-05-05 16:52:57','ok sending file now'),(91,6,14,'2025-05-05 16:53:07','                    [!] Chat with [%s] has started.'),(92,6,14,'2025-05-05 16:53:07','                    [!] Type %%quit to exit the chat.'),(93,6,14,'2025-05-05 16:53:28','wrong thing'),(94,6,9,'2025-05-05 17:00:51','hi'),(95,6,9,'2025-05-05 17:00:54','sending file now'),(96,14,15,'2025-05-05 17:03:00','hi'),(97,15,14,'2025-05-05 17:03:06','hi'),(98,9,6,'2025-05-05 17:13:28','hi'),(99,6,9,'2025-05-05 17:13:34','one last test'),(100,6,9,'2025-05-05 17:13:38','ok bye'),(101,6,14,'2025-05-05 17:43:45','hi'),(102,8,17,'2025-05-05 17:46:05','Hello'),(103,17,8,'2025-05-05 17:46:17','hello'),(104,8,17,'2025-05-05 17:46:24','Yes'),(105,17,8,'2025-05-05 17:46:31','bruh'),(106,6,17,'2025-05-05 17:56:23','hi'),(107,17,6,'2025-05-05 17:56:26','hello'),(108,6,17,'2025-05-05 17:59:49','hi'),(109,17,6,'2025-05-05 17:59:49','hello'),(110,6,17,'2025-05-05 18:03:33','hi'),(111,17,6,'2025-05-05 18:03:33','hello'),(112,6,17,'2025-05-05 18:03:38','sending file'),(113,6,17,'2025-05-05 19:04:19','hi'),(114,17,6,'2025-05-05 19:04:21','hello'),(115,6,17,'2025-05-05 19:04:27','sending a file');
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `requests`
--

DROP TABLE IF EXISTS `requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `requests` (
  `request_id` int NOT NULL AUTO_INCREMENT,
  `sender_id` int NOT NULL,
  `receiver_id` int NOT NULL,
  PRIMARY KEY (`request_id`),
  KEY `sender_id` (`sender_id`),
  KEY `requests_ibfk_2` (`receiver_id`),
  CONSTRAINT `requests_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `requests_ibfk_2` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `requests`
--

LOCK TABLES `requests` WRITE;
/*!40000 ALTER TABLE `requests` DISABLE KEYS */;
/*!40000 ALTER TABLE `requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '0',
  `chatting` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'testUser1','testPass123',0,1),(5,'testUser2','pass2',0,0),(6,'ljaco','tiramisu',1,0),(7,'newguy47','bomboclaaat',0,0),(8,'Daniel','abc',0,0),(9,'testing32','ok',0,0),(14,'1','1',0,0),(15,'2','2',0,0),(16,'0','BACK',0,0),(17,'Jake','password123',1,0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-06 13:22:51
