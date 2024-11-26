# Backend

> ## 📝 목차
> 1. [서비스 소개](#-서비스-소개)
> 2. [주요 기능](#주요-기능)
> 3. [R&R](#-rr)
> 4. [프로젝트 일정](#프로젝트-일정)
> 5. [Discord 를 활용한 소통 및 PR 알림 봇](#discord를-활용한-소통-및-pr-알림-봇-)
> 6. [프로젝트 환경](#%EF%B8%8F-프로젝트-환경)
> 7. [기술 선택 이유](#-기술-선택-이유)
> 8. [서비스 아키텍처](#-서비스-아키텍처)
> 9. [API 명세서](#-api-명세서)
> 10. [ERD](#%EF%B8%8F-erd)
> 11. [트러블 슈팅](#-트러블-슈팅)
> 12. [고민한 흔적](#-고민한-흔적)
> 13. [디렉토리 구조](#%EF%B8%8F-디렉토리-구조)

<br/>

## 🍻 서비스 소개
- 만 명이 즐기는 취미 플랫폼, 만취!
- 운동, 개발, 여행 등 다양한 주제의 모임을 만들 수 있고, 같은 관심사를 가진 사람들과 취미를 즐기기 위한 서비스입니다. 참여한 모임에 대한 후기를 공유하며 더 긍정적이고 즐거운 취미 생활을 할 수 있도록 돕는 만취 서비스의 API 서버입니다.
- 취미 활동을 통해 사람들과 소통하고, 새로운 경험을 연결하는 다양한 기능을 제공합니다.

### 주요 기능

1. 소셜 로그인 지원
    - 카카오, 네이버, 구글 계정을 통해 간편하게 로그인할 수 있습니다.

2. 다양한 주제의 모임 생성
    - 운동, 개발, 음악, 여행 등 관심사에 맞는 모임을 자유롭게 생성할 수 있습니다.

3. 모임 탐색 및 필터링
    - 찜한 모임만 조회하거나, 카테고리, 검색어, 마감임박 순 등 다양한 조건으로 모임을 검색하고 필터링할 수 있습니다.

4. 모임 관리
    - 참여하거나 관심 있는 모임에 좋아요를 눌러 관리할 수 있습니다.
    
5. 개인 활동 관리
    - 마이페이지에서 내가 작성한 모임, 참여한 모임, 작성한 후기 등을 한눈에 조회할 수 있습니다.

6. 모임 후기 작성
    - 참여한 모임에 대한 후기를 남겨 다른 사용자들과 경험을 공유할 수 있습니다.
    
<br/>

### 👩🏻‍💻 R&R
| 담당자                                      | 담당 업무                                                        |
|:-------------------------------------------:|------------------------------------------------------------------|
| [강병훈](https://github.com/yosong6729) | 사용자 기능 구현 (로그인, 회원가입, 회원정보 조회, 회원정보 수정)     |
| [오예령](https://github.com/ohyeryung)       | 모임, 후기 도메인 개발 |

<br>

### 프로젝트 일정
<details>
    <summary><b>프로젝트 과정 타임라인 🗓</b></summary><br>
    <img src="https://github.com/user-attachments/assets/42e3dbe5-8afc-4c37-be28-35544828fbff">

</details>

<br>

### Discord를 활용한 소통 및 PR 알림 봇 🤖 

<details>
<summary>소통 및 PR 알림 확인</summary>
<div markdown="1">
   <img src="https://github.com/user-attachments/assets/405e8325-89c7-42be-945f-413d0bcef42f">
    <img src="https://github.com/user-attachments/assets/52c361d5-c22a-4a17-978c-68c6119e8f16">
    <img src="https://github.com/user-attachments/assets/63000938-1e80-4c04-a501-2323dd937b80">

</div>
</details>

<br/>

## 🛠️ 프로젝트 환경

| Stack                                                                                                        | Version           |
|:------------------------------------------------------------------------------------------------------------:|:-----------------:|
| ![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) | Spring Boot 3.3.x |
| ![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)    | Gradle 8.10       |
| ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)    | JDK 17           |
| ![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)       | MySQL 8.0        |
| ![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)    | Redis 6.0        |

<br/>

### ✅ 기술 선택 이유

TODO : 위키로 정리 예정

<br/>

### 🎨 서비스 아키텍처

TODO : 아키텍처 이미지 첨부 예정

<br/>


### 🧾 API 명세서

 > 자세한 명세는 <a href="https://documenter.getpostman.com/view/20456478/2sAXjM4Xgs">🔗여기</a> 를 클릭해주세요! `(Postman API)`

<br>

## ⛓️ ERD
<img width="1221" alt="image" src="https://github.com/user-attachments/assets/8ffc76a4-d372-4d98-bc32-eb68f68fecd0">

<br>


## 💥 트러블 슈팅


<br>

## 🤔 고민한 흔적

<br>

## 🗂️ 디렉토리 구조
<details><summary>직관적인 구조 파악과 관리를 위해 <b>도메인형 구조</b>를 채택하였습니다. <b>(더보기)</b></summary>

