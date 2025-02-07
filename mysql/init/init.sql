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

INSERT INTO
    users (
        user_id,
        email,
        nickname,
        sex,
        provider,
        provider_id,
        profile_image,
        avatar,
        age,
        is_deleted,
        created_at,
        updated_at,
        last_login,
        total_win,
        total_lose,
        role_type
    )
VALUES (
        '1',
        'yetcome0123@gmail.com',
        'yetcome0123',
        '1',
        'GOOGLE',
        '112615265389652052420',
        '1',
        '1',
        '0',
        '0',
        '2025-01-31 05:04:35',
        '2025-01-31 05:04:35',
        NULL,
        '0',
        '0',
        'USER'
    ),
    (
        '2',
        'suhyun224@gmail.com',
        'suhyun224',
        '1',
        'GOOGLE',
        '118165552243654929436',
        '1',
        '1',
        '20',
        '0',
        '2025-01-29 08:13:24',
        '2025-01-29 08:13:24',
        NULL,
        '0',
        '0',
        'USER'
    ),
    (
        '3',
        'cchh6462@gmail.com',
        'cchh6462',
        '0',
        'GOOGLE',
        '118165552243654929436',
        '1',
        '1',
        '22',
        '0',
        '2025-01-31 08:13:24',
        '2025-01-31 08:13:24',
        NULL,
        '0',
        '0',
        'USER'
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

-- Games 더미데이터
INSERT INTO
    games (
        game_id,
        room_title,
        started_at,
        ended_at,
        play_time,
        human_win
    )
VALUES (
        1,
        '초보만 오세요',
        '2025-01-30 10:00:00',
        '2025-01-30 10:30:00',
        '00:30:00',
        true
    ),
    (
        2,
        '고수방',
        '2025-01-30 11:15:00',
        '2025-01-30 11:45:00',
        '00:30:00',
        false
    ),
    (
        3,
        '즐겁게 한판',
        '2025-01-30 13:20:00',
        '2025-01-30 13:50:00',
        '00:30:00',
        true
    ),
    (
        4,
        '실력자만',
        '2025-01-30 15:45:00',
        '2025-01-30 16:15:00',
        '00:30:00',
        false
    ),
    (
        5,
        '친선전',
        '2025-01-30 18:30:00',
        '2025-01-30 19:00:00',
        '00:30:00',
        true
    );

INSERT INTO
    ais (ai_id, ai_name, created_at)
VALUES (
        1,
        'AI Player 1',
        '2025-01-27 09:00:00'
    ),
    (
        2,
        'AI Player 2',
        '2025-01-27 09:00:00'
    );

-- GameParticipants 더미데이터
INSERT INTO
    game_participants (game_id, user_id, ai_id)
VALUES (1, 1, null),
    (2, 1, null),
    (3, null, 1);

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

-- Subjects 테이블 데이터
INSERT INTO
    subjects (item)
VALUES ('인공지능의 미래'),
    ('우주 탐사'),
    ('기후 변화'),
    ('가상현실 기술'),
    ('자율주행 자동차');

INSERT INTO
    game_records (
        game_id,
        round_number,
        subject_id,
        subject_talk,
        free_talk,
        events
    )
VALUES (
        1,
        1,
        1,
        '{12345} [JohnDoe] (AI가 인간의 일자리를 완전히 대체할까요?) 2025-01-30T10:05:23 | {67890} [JaneDoe] (저는 AI가 도구일 뿐이라고 생각해요) 2025-01-30T10:06:15 | {11111} [Mike] (새로운 직업이 생길 거에요) 2025-01-30T10:07:30',
        '{12345} [JohnDoe] (AI 발전 속도가 너무 빨라서 걱정이네요) 2025-01-30T10:15:23 | {67890} [JaneDoe] (우리가 잘 활용하면 될 것 같아요) 2025-01-30T10:16:45',
        '{vote} [JohnDoe] (JaneDoe) 2025-01-30T10:20:00 | {vote} [Mike] (JaneDoe) 2025-01-30T10:20:15'
    ),
    (
        1,
        2,
        2,
        '{12345} [JohnDoe] (화성 탐사에 대해 어떻게 생각하세요?) 2025-01-30T10:22:00 | {67890} [JaneDoe] (인류의 위대한 도전이라고 봅니다) 2025-01-30T10:23:15',
        '{11111} [Mike] (예산이 너무 많이 들어가는 것 같아요) 2025-01-30T10:25:30 | {12345} [JohnDoe] (하지만 기술 발전에 도움될 거에요) 2025-01-30T10:26:45',
        '{infection} [] (Mike) 2025-01-30T10:27:00'
    ),
    (
        2,
        1,
        3,
        '{22222} [Sarah] (기후 변화가 심각해지고 있어요) 2025-01-30T11:20:00 | {33333} [Tom] (개인이 할 수 있는 게 뭐가 있을까요?) 2025-01-30T11:21:30',
        '{22222} [Sarah] (재활용을 더 열심히 해야겠어요) 2025-01-30T11:25:00 | {33333} [Tom] (전기차로 바꾸는 것도 좋을 것 같아요) 2025-01-30T11:26:15',
        '{vote} [Sarah] (Tom) 2025-01-30T11:28:00 | {infection} [] (Sarah) 2025-01-30T11:29:00'
    ),
    (
        2,
        2,
        4,
        '{22222} [Sarah] (VR로 무엇을 하고 싶으신가요?) 2025-01-30T11:35:00 | {33333} [Tom] (여행이요!) 2025-01-30T11:36:30',
        '{22222} [Sarah] (저는 교육에 활용하고 싶어요) 2025-01-30T11:40:00 | {33333} [Tom] (좋은 생각이네요) 2025-01-30T11:41:15',
        '{vote} [Tom] (Sarah) 2025-01-30T11:43:00'
    ),
    (
        3,
        1,
        5,
        '{44444} [Alex] (자율주행차가 언제 상용화될까요?) 2025-01-30T13:25:00 | {55555} [Emma] (5년 안에 가능할 것 같아요) 2025-01-30T13:26:30',
        '{44444} [Alex] (안전 문제가 걱정되네요) 2025-01-30T13:30:00 | {55555} [Emma] (기술이 더 발전하면 괜찮을 거예요) 2025-01-30T13:31:15',
        '{infection} [] (Alex) 2025-01-30T13:35:00 | {vote} [Emma] (Alex) 2025-01-30T13:36:00'
    );

CREATE TABLE `friends` (
    `friendship_id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    `status` ENUM(
        'PENDING',
        'ACCEPTED',
        'BLOCKED',
        'DELETED'
    ) NOT NULL DEFAULT 'PENDING', -- 친구 상태(보류, 수락, 차단, 삭제)
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NULL,
    `user_id` INT UNSIGNED NOT NULL REFERENCES users (user_id), -- user.user_id 참조
    `friend_id` INT UNSIGNED NOT NULL REFERENCES users (user_id) -- user.user_id 참조
);

INSERT INTO
    friends (
        friendship_id,
        status,
        created_at,
        updated_at,
        user_id,
        friend_id
    )
VALUES (
        1,
        'ACCEPTED',
        '2025-01-29 08:31:08',
        '2025-01-29 08:31:08',
        2,
        1
    ),
    (
        2,
        'PENDING',
        '2025-01-29 21:20:03',
        '2025-01-31 13:04:11',
        2,
        3
    ),
    (
        3,
        'BLOCKED',
        '2025-01-29 21:21:10',
        '2025-01-31 05:21:25',
        3,
        1
    );