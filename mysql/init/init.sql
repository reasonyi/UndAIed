use undaied;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    sex BOOLEAN,
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    profile_image INT NOT NULL,
    avatar INT NOT NULL,
    age INT,
    is_deleted BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    last_login DATETIME,
    total_win INT,
    total_lose INT,
    role_type VARCHAR(20) NOT NULL
);

CREATE TABLE games (
    game_id INT PRIMARY KEY,
    room_title VARCHAR(50) NOT NULL,
    started_at DATETIME NOT NULL,
    ended_at DATETIME NOT NULL,
    play_time VARCHAR(15) NOT NULL,
    human_win BOOLEAN NOT NULL
);

CREATE TABLE ais (
    ai_id INT PRIMARY KEY,
    ai_name VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL
);

CREATE TABLE game_participants (
    game_participants_id INT AUTO_INCREMENT PRIMARY KEY,
    game_id INT NOT NULL,
    user_id INT,
    ai_id INT,
    FOREIGN KEY (game_id) REFERENCES games (game_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (ai_id) REFERENCES ais (ai_id)
);

CREATE TABLE `subjects` (
    `subject_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `item` VARCHAR(255) NOT NULL
);

CREATE TABLE `game_records` (
    `game_record_id` INT PRIMARY KEY AUTO_INCREMENT,
    `game_id` INT NOT NULL,
    `round_number` INT NOT NULL,
    `subject_id` BIGINT NOT NULL,
    `subject_talk` TEXT NOT NULL,
    `free_talk` TEXT NOT NULL,
    `events` TEXT NOT NULL,
    FOREIGN KEY (`game_id`) REFERENCES `games` (`game_id`) ON DELETE CASCADE,
    FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`subject_id`)
);