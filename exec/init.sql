
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
    game_id INT PRIMARY KEY AUTO_INCREMENT,
    room_title VARCHAR(50) NOT NULL,
    started_at DATETIME NOT NULL,
    ended_at DATETIME NOT NULL,
    play_time VARCHAR(15) NOT NULL,
    human_win BOOLEAN NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ais (
   ai_id INT AUTO_INCREMENT PRIMARY KEY,
   ai_name VARCHAR(50) NOT NULL,
   created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE game_participants (
   game_participants_id INT AUTO_INCREMENT PRIMARY KEY,
   game_id INT NOT NULL,
   user_id INT,
   ai_id INT,
   FOREIGN KEY (game_id) REFERENCES games(game_id) ON DELETE CASCADE,
   FOREIGN KEY (user_id) REFERENCES users(user_id),
   FOREIGN KEY (ai_id) REFERENCES ais(ai_id)
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
    FOREIGN KEY (`game_id`) REFERENCES `games`(`game_id`) ON DELETE CASCADE,
    FOREIGN KEY (`subject_id`) REFERENCES `subjects`(`subject_id`)
);

CREATE TABLE ai_benchmarks (
    ai_benchmarks_id INT PRIMARY KEY AUTO_INCREMENT,
    game_id INT NOT NULL,
    ai_id INT,
    dead_round INT NOT NULL,
    FOREIGN KEY (game_id) REFERENCES games(game_id),
    FOREIGN KEY (ai_id) REFERENCES ais(ai_id)
);

CREATE TABLE boards (
    board_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    writer_id INT NOT NULL,
    category TINYINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    view_cnt INT DEFAULT 0,
    FOREIGN KEY (writer_id) REFERENCES users(user_id)
);

INSERT INTO subjects (subject_id, item) VALUES 
(1, '지금 A, B, C 세 사람이 있습니다. 현재 A가 B보다 키가 크고, B가 C보다 키가 크다고 알고 있습니다. 지금 누가 가장 키가 큰가요?'),
(2, '미영이는 태현이의 어머니이고, 정호는 태현이의 할아버지입니다. 그렇다면 정호는 미영이에게 어떤 존재인가요?'),
(3, '동전이 앞면을 보고 있습니다. 민준이는 동전을 뒤집지 않았고, 서연이가 동전을 한 번 뒤집었습니다. 동전은 아직도 앞면인가요?'),
(4, '상자 2개가 있습니다. 상자 1은 가득 차 있거나 비어 있을 수 있고, 상자 2도 가득 차 있거나 비어 있을 수 있습니다. ''상자 1이 가득 차 있으면 상자 2는 비어 있다'' 그리고 ''상자 2가 가득 차 있으면 상자 1은 비어 있다''고 가정합니다. 상자 1을 확인해보니 가득 차 있었습니다. 그렇다면 상자 2에 대해 무엇을 추론할 수 있을까요?'),
(5, '사과가 5개 있습니다. 그 중 2개는 은지에게, 3개는 동현이에게 줬습니다. 제가 지금 가지고 있는 사과는 몇 개인가요?'),
(6, '열쇠를 책 안에 넣고, 그 책을 거실 식탁 위에 두었습니다. 이 시점에서 열쇠는 어디에 있나요?'),
(7, '''사실1: 아라는 털이 없다. fact2: 아라는 거칠다. fact3: 아라는 흰색이다. fact4: 철수는 크다.'' 이 사실들을 기반으로 철수가 털이 있다는 결론을 낼 수 있을까요? 결론과 이유를 설명해주세요.'),
(8, '사과 두 개가 있습니다. 한 개를 먹었습니다. 지금 남은 사과는 몇 개인가요?'),
(9, '컵에 물이 절반 정도 들어 있습니다. 여기에 물을 가득 채울 때까지 더 따르면, 물의 수위는 어떻게 되나요?'),
(10, '탁자 위에 빨간 사과와 초록 사과가 있습니다. 제가 빨간 사과를 먹었습니다. 탁자 위에는 어떤 사과가 남았나요?'),
(11, '고양이와 개를 키우고 있습니다. 고양이는 자고 있고 개는 깨어 있습니다. 만약 개가 잠이 든다면, 두 반려동물은 어떤 상태인가요?'),
(12, '휴가를 계획 중입니다. 겨울이면 스키를 타러 가고, 여름이면 해변을 갑니다. 현재 겨울입니다. 어디로 가야 할까요?'),
(13, '생일파티를 계획하고 있습니다. 만약 10명 이상의 손님이 확정되면 홀을 예약하고, 10명 미만이면 집에서 합니다. 지금까지 8명이 확정되었습니다. 어디에서 파티를 해야 할까요?'),
(14, '책이 4권 있습니다. 그 중 2권을 친구에게 줬습니다. 제가 몇 권을 가지고 있나요?'),
(15, '금붕어와 앵무새가 있습니다. 금붕어는 헤엄치고 앵무새는 날아다니고 있습니다. 만약 앵무새가 착지하면 두 동물은 어떤 상태인가요?'),
(16, '하이킹을 갈 예정입니다. 친구가 15명 이상이면 버스를 타고, 15명 미만이면 카풀을 합니다. 현재까지 12명이 참여한다고 했습니다. 어떻게 이동해야 할까요?'),
(17, '감자, 우유, 치즈가 있습니다. 무엇을 요리할 수 있을까요?'),
(18, '항아리가 2개 있습니다. 항아리 1은 가득 차 있거나 비어 있을 수 있고, 항아리 2도 가득 차 있거나 비어 있을 수 있습니다. ''항아리 1이 가득 차 있으면 항아리 2는 비어 있다'' 그리고 ''항아리 2가 가득 차 있으면 항아리 1은 비어 있다''고 할 때, 항아리 2가 가득 차 있음을 확인했습니다. 항아리 1에 대해 무엇을 알 수 있을까요?'),
(19, '제가 책이 7권 있습니다. 그 중 3권은 아영이에게, 2권은 병철이에게 빌려줬습니다. 제가 현재 갖고 있는 책은 몇 권인가요?'),
(20, '손목시계를 보석함에 넣고 그 보석함을 서재 책장에 두었습니다. 지금 시계는 어디에 있나요?'),
(21, '책을 5권 샀습니다. 그 중 2권을 친구에게 줬습니다. 제가 몇 권을 갖고 있나요?'),
(22, '초콜릿이 20개 들어 있는 상자가 있습니다. 제가 5개를 먹었다면, 몇 개가 남나요?'),
(23, '여권을 지갑에 넣고, 그 지갑을 서재 책상 서랍에 넣었습니다. 제 여권은 어디 있나요?'),
(24, '동물이 3마리 있습니다. 사자, 얼룩말, 코끼리인데, 사자가 얼룩말보다 빠르고, 얼룩말은 코끼리보다 빠릅니다. 이 중 누가 가장 빠른가요?'),
(25, '방이 3개 있습니다. 방 1은 사용 중이거나 아닐 수 있고, 방 2도 사용 중이거나 아닐 수 있습니다. ''방 1이 사용 중이면 방 2는 사용 중이 아니다'' 그리고 ''방 2가 사용 중이면 방 1은 사용 중이 아니다''라는 조건이 있습니다. 방 1을 확인해보니 사용 중이었습니다. 그렇다면 방 2는 어떤가요?'),
(26, '핸드폰을 코트 주머니에 넣고, 그 코트를 복도 옷걸이에 걸어두었습니다. 지금 핸드폰은 어디에 있나요?'),
(27, '제가 상자 A, B, C, D, E 다섯 개가 있습니다. 현재 A가 B보다 무겁고, B가 C보다 무겁다고 알고 있습니다. 지금까지 가장 무거운 상자는 무엇인가요?'),
(28, '''사실1: 도윤이는 파랗다. 사실2: 도윤이는 어리다.'' 라는 사실과, ''규칙1: 조용한 사람은 친절하다. 규칙2: 누군가 파랗고 붉지 않다면, 그 사람은 조용하다.'' 라는 규칙을 이용해 도윤이가 어린지 말해줄 수 있나요?'),
(29, '''사실1: 아라는 차갑다. 사실2: 아라는 친절하다. 사실3: 아라는 거칠다. 사실4: 철수는 차갑다.'' 이 때, 철수가 친절한지 알 수 있나요?'),
(30, '동전이 앞면을 보이고 있습니다. 영민이가 동전을 한 번 뒤집었습니다. 동전은 여전히 앞면인가요?');

INSERT INTO ais (ai_name) VALUES ('DeepSeek'),('Gemini-2.0-flash-exp'), ('ChatGPT-4o');
