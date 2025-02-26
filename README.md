# 🤖 UndAIed(Undead + AI) - 8명의 플레이어 속 인간인 척 하는 AI를 찾아내는 신개념 마피아게임

<br>

# 목차

- [프로젝트 진행 기간](#🎞-프로젝트-진행-기간)
- [개요](#✨-개요)
- [주요 기능](#💻-주요-기능)
- [서비스 화면](#🖼-서비스-화면)
- [주요 기술](#🛠-주요-기술)
- [프로젝트 파일 구조](#🗂-프로젝트-파일-구조)
- [프로젝트 산출물](#📋-프로젝트-산출물)
- [팀원 역할 분배](#👩‍💻-팀원-역할-분배)

<br>
<br>

# 🎞 프로젝트 진행 기간

2025.01.02(월) ~ 2025.02.21(금) (51일간 진행)

SSAFY 12기 2학기 공통프로젝트

<br>
<br>

# ✨ 개요

UndAIed는 "어떻게 하면 LLM에 대한 인간 선호도 평가 데이터를 손쉽게 모을 수 있을까?" 에 대한 답변으로, 게이미피케이션 요소를 적용하여 LLM과 사회적 추론 게임(마피아게임)을 접목한 서비스입니다.<br>
6명의 사람과 2명의 AI가 여러가지 주제에 대한 토론하며 누가 AI인지 찾아내는 과정을 통해 LLM사이의 이질적인 포인트에 대한 데이터를 손쉽게 수집 하기위해 개발했습니다.
<br>
<br>

# 💻 주요 기능

### 홈페이지

- 구글 소셜로그인
- 게임 튜토리얼
- 데이터수집 약관동의 / 게임환경설정 / 인트로

### 게시판

- 공지사항, 버그리포트 작성
- 관리자, 일반유저에 따른 권한관리

### 게임 메인페이지

- 현재 실시간 대기실 목록
  - 입/퇴장에 따른 대기실 인원 변경 관리
  - 게임시작/시작전에 따른 대기실 상태관리
  - 방 생성/삭제에 따른 대기실 변동
- 로그인한 유저 정보(전적, 프로필 이미지, 닉네임)
- 메인페이지에 있는 유저간 실시간 채팅
- 게임 환경설정(전체/창화면, 볼륨조절)

### 게임 플레이

- 유저,AI간 실시간 채팅
  - AI는 유저들이 입력한 데이터를 기반으로 실시간 응답
  - 주제토론, 자유토론
  - 도배방지, 토론 시간에 따른 입력제어
- 투표기능
  - 한사람당 한표씩 AI로 의심되는 사람 투표 가능
  - 투표 결과에 따른 유저 상태 변경
- 유저 연결상태에 따른 처리 로직 구현

### 게임 마이페이지

- 로그인한 유저 정보(전적, 프로필 이미지, 닉네임, 캐릭터)
- 게임 플레이 기록 확인 가능
- 유저 정보 수정(프로필 이미지, 닉네임, 캐릭터)

<br>
<br>

# 🖼 서비스 화면

## 홈페이지

| 기능 | 화면 |
|------|------|
| 유저 로그인 | <video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/login.mp4" type="video/mp4"></video> |
| 튜토리얼 | <video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/tutorial.mp4" type="video/mp4"></video> |
| 게임 시작 | <video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/gameStart.mp4" type="video/mp4"></video> |

<br>
<br>

## 게시판

| 기능 | 화면 |
|------|------|
| 버그리포트 / 공지사항 | <video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/board.mp4" type="video/mp4"></video> |

<br>
<br>

## 게임 메인

| 기능 | 화면 |
|------|------|
| 실시간 유저 채팅 | <video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/lobby-chat.mp4" type="video/mp4"></video> |
| 실시간 대기실 목록 |<video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/lobby-room-created.mp4" type="video/mp4"></video>|
| 방만들기 |<video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/room-create.mp4" type="video/mp4"></video>|
| 게임 설정 |<video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/setting.mp4" type="video/mp4"></video>|

<br>
<br>

## 게임 대기실

| 기능 | 화면 |
|------|------|
| 실시간 유저 입장/퇴장 |<video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/room-enter.mp4" type="video/mp4"></video>|
| 실시간 채팅 | <video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/room-chat.mp4" type="video/mp4"></video> |

<br>
<br>

## 게임 플레이

| 기능 | 화면 |
|------|------|
| 주제토론 | <video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/주제토론.mp4" type="video/mp4"></video> |
| 자유토론 | <video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/자유토론.mp4" type="video/mp4"></video> |
| 투표 | <video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/투표.mp4" type="video/mp4"></video> |

<br>
<br>

## 마이페이지

| 기능 | 화면 |
|------|------|
| 프로필 수정 | <video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/profile-change.mp4" type="video/mp4"></video> |
| 캐릭터 변경 | <video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/character-change.mp4" type="video/mp4"></video> |
| 전적 확인 | <video autoplay loop muted playsinline style="max-width: 100%;"><source src="exec/assets/gamerecord-check.mp4" type="video/mp4"></video> |

##

<br>
<br>

# 🛠 주요 기술

**Backend**
<br>
<img src="https://img.shields.io/badge/IntelliJ-000000?style=for-the-badge&logo=intellijidea&logoColor=white"/>&nbsp;<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/springsecurity-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/fastapi-009688?style=for-the-badge&logo=fastapi&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/redis-FF4438?style=for-the-badge&logo=redis&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/Socket.io-010101?style=for-the-badge&logo=socketdotio&logoColor=white">&nbsp;

- IntelliJ IDEA 2024.3.1.1
- Java : Oracle OpenJDK 17
- SpringBoot 3.4.1
- Spring Security
- Spring Data Jpa
- Gradle 8.12.1
- FastAPI
- MySQL 8.0
- Redis 7.4.2
- Socket.io
- Netty 2.0.9

<br>

**FrontEnd**
<br>

<img src="https://img.shields.io/badge/-React-61DAFB?logo=react&logoColor=white&style=for-the-badge"/>&nbsp;<img src="https://img.shields.io/badge/-TypeScript-007ACC?logo=typescript&logoColor=white&style=for-the-badge"/>&nbsp;<img src="https://img.shields.io/badge/-TailwindCSS-06B6D4?logo=tailwindcss&logoColor=white&style=for-the-badge"/>&nbsp;<img src="https://img.shields.io/badge/-Recoil-3578E5?logo=redux&logoColor=white&style=for-the-badge"/>&nbsp;<img src="https://img.shields.io/badge/-React%20Query-FF4154?logo=react&logoColor=white&style=for-the-badge" alt="React Query" />&nbsp;<img src="https://img.shields.io/badge/-Socket.io--client-010101?logo=socket.io&logoColor=white&style=for-the-badge" alt="Socket.io-client" />&nbsp;<img src="https://img.shields.io/badge/-Axios-5A29E4?logo=axios&logoColor=white&style=for-the-badge" alt="Axios" />&nbsp;<img src="https://img.shields.io/badge/-Vite_6.0.5-646CFF?logo=vite&logoColor=white&style=for-the-badge" alt="Vite 6.0.5" />&nbsp;<!-- Node.js Badge --><img src="https://img.shields.io/badge/-Node.js-339933?logo=node.js&logoColor=white&style=for-the-badge" alt="Node.js" />&nbsp;

- React 18.3.1
- Node.js 22.12.0
- TypeScript 5.6.2
- Recoil 0.7.7
- Socket.io-client 4.8.1
- Axios 1.7.9
- TailwindCss 5.6.2
- vite 6.0.5
- sonner 1.7.4

<br>

**CI/CD**
<br>

<img src="https://img.shields.io/badge/aws ec2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=Jenkins&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=Docker&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/openssl-721412?style=for-the-badge&logo=openssl&logoColor=white">&nbsp;

- AWS EC2
- Ubuntu 20.04 LTS
- Jenkins
- Docker Engine 27.5.1
- Nginx
- SSL

<br>

**협업 툴**
<br>

<img src="https://img.shields.io/badge/gitlab-FC6D26?style=for-the-badge&logo=gitlab&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/jira-0052CC?style=for-the-badge&logo=jirasoftware&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/mattermost-0058CC?style=for-the-badge&logo=mattermost&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white">&nbsp;<img src="https://img.shields.io/badge/figma-EA4335?style=for-the-badge&logo=figma&logoColor=white">&nbsp;

- 형상 관리 : GitLab
- 이슈 관리 : Jira
- 커뮤니케이션 : Mattermost, Notion
- 디자인 : Figma

<br>
<br>

# 🗂 프로젝트 파일 구조

### Backend

```code
📂undaied
├─📂domain
│ ├─📂ai
│ ├─📂board
│ ├─📂comment
│ ├─📂friend
│ ├─📂game
│ └─📂user
├─📂global
│ ├─📂auth
│ │ ├─📂config
│ │ ├─📂entity
│ │ ├─📂service
│ │ └─📂token
│ ├─📂common
│ │ ├─📂exception
│ │ └─📂response
│ └─📂config
└─📂socket
├─📂chat
├─📂common
│ ├─📂config
│ ├─📂constant
│ ├─📂exception
│ ├─📂handler
│ ├─📂response
│ ├─📂service
│ └─📂util
├─📂infect
├─📂init
├─📂json
├─📂lobby
├─📂quit
├─📂result
├─📂room
├─📂stage
└─📂vote