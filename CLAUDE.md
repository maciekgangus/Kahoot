# CLAUDE.md — Platforma Interaktywnych Quizów Czasu Rzeczywistego

Projekt na przedmiot **Zaawansowane Techniki Internetowe**. Implementacja MUSI być
zgodna z dokumentem architektury (`docs/ProjektD.pdf`). Diagramy UML (use case,
aktywności, klas, komponentów, sekwencji, komunikacji) są źródłem prawdy — kod ma
odzwierciedlać tę architekturę, bo projekt będzie broniony przy tych diagramach.

## Zasady nadrzędne (przeczytaj przed każdą zmianą)

1. **Zgodność z architekturą > elegancja.** Nie zmieniaj nazw encji, pól, enumów ani
   kanałów STOMP względem dokumentu. Jeśli coś w PDF jest niejasne — ZAPYTAJ, nie
   improwizuj.
2. **Bez scope creep.** Implementuj DOKŁADNIE to, co opisuje dana faza planu. Żadnych
   dodatkowych feature'ów, abstrakcji "na przyszłość", frameworków poza stackiem,
   ani gold-platingu. Jeśli widzisz pokusę dodania czegoś — wypisz to jako sugestię
   na końcu, NIE implementuj.
3. **Małe, weryfikowalne kroki.** Po każdej fazie zatrzymaj się i poczekaj na review.
   Nie generuj całej aplikacji w jednym przebiegu.
4. **Najpierw kompiluje się, potem działa.** Po każdej znaczącej zmianie w backendzie
   uruchom `mvn compile` (lub test). Nie zostawiaj kodu, który się nie kompiluje.
5. **Konwencje języka:** kod, nazwy klas, komentarze techniczne po angielsku;
   komunikaty domenowe/UI mogą być po polsku zgodnie z dokumentem.

## Stos technologiczny (NIE zmieniać)

| Warstwa        | Technologia                                              |
|----------------|----------------------------------------------------------|
| Frontend       | React + JavaScript (Vite)                                |
| Backend        | Spring Boot 3.x, Spring Data JPA, Spring AOP             |
| Realtime       | STOMP over SockJS (Spring WebSocket)                     |
| Baza danych    | PostgreSQL 16                                            |
| Infrastruktura | Docker + Docker Compose                                  |
| Testy          | JUnit 5, Mockito, Testcontainers                         |
| Java           | 21 (LTS)                                                 |

## Struktura projektu

```
Kahoot/
├── backend/
│   └── src/main/java/pl/agh/zti/quiz/
│       ├── domain/            # Encje JPA (model domenowy z diagramu klas)
│       ├── repository/        # Spring Data JPA repositories
│       ├── service/           # QuizService, GameService, RankingService
│       ├── web/
│       │   ├── rest/          # REST Controllers (CRUD)
│       │   └── ws/            # WebSocket/STOMP handlers
│       ├── aop/               # AuditAspect, AntiCheatAspect
│       ├── dto/               # DTO request/response + eventy WS
│       └── config/            # WebSocketConfig (STOMP broker), itp.
├── frontend/                  # React SPA (Vite)
├── docs/ProjektD.pdf          # Dokument architektury — źródło prawdy
├── docker-compose.yml
└── CLAUDE.md
```

## Model domenowy (diagram klas — odwzorować 1:1)

Encje JPA. Pola i typy zgodnie z PDF. Relacje w nawiasach.

- **Host** `(Long id, String username, String passwordHash)` — owns→ Quiz
- **Quiz** `(Long id, String title, String description, Integer defaultTimeLimitSec, Host host)`
  - contains→ Question (1:*)
- **Question** `(Long id, String content, Integer orderIndex, Integer timeLimitSec)`
  - has→ Answer (1:*)
- **Answer** `(Long id, String content, Boolean isCorrect)`
- **GameSession** `(UUID id, String lobbyCode, GameState state, LocalDateTime startedAt)`
  - uses→ Quiz, participants→ Player (1:*)
- **Player** `(UUID id, String nickname, Integer totalScore)`
  - submits→ PlayerAnswer (1:*)
- **PlayerAnswer** `(Long id, Long responseTimeMs, Boolean correct, Integer pointsAwarded)`
  - references→ Question, selects→ Answer

**GameState (enum):**
`CREATED → LOBBY → QUESTION_ACTIVE → EVALUATING → RANKING_DISPLAY → FINISHED`

Liczności z diagramu: GameSession ma 2..6 graczy (participants), Quiz→Question 1:*,
Question→Answer 1:* itd. Klucze: `Long` dla treści (Quiz/Question/Answer/Host/PlayerAnswer),
`UUID` dla bytów runtime'owych sesji (GameSession, Player).

## Warstwy logiczne (diagram komponentów)

`React SPA → Warstwa Web (REST + STOMP) → Serwisy → Persystencja (JPA → PostgreSQL)`

Serwisy: **QuizService** (CRUD quizów/pytań), **GameService** (cykl rozgrywki, zmiany
stanu sesji), **RankingService** (liczenie punktów, broadcast rankingu).

AOP przechwytuje wywołania **serwisów** (pointcut na warstwę `service`):

- **AuditAspect** — logowanie wywołań (audyt).
- **AntiCheatAspect** — weryfikacja integralności czasowej odpowiedzi (walidacja
  timestampu / `responseTimeMs` przy `submitAnswer`). Odpowiedź poza limitem → 0 pkt.

## Kanały STOMP (diagram komunikacji — odwzorować dokładnie)

**App destinations (klient → serwer, prefix `/app`):**

- `/app/lobby.join` — gracz dołącza do lobby
- `/app/game.answer` — gracz wysyła odpowiedź
- `/app/game.nextQuestion` — host przechodzi do kolejnego pytania

**Topics (broadcast, prefix `/topic`):**

- `/topic/lobby.{id}` — zdarzenia lobby
- `/topic/game.{id}` — pytania + ranking dla całej sesji

**Queues (prywatne, prefix `/queue`):**

- `/queue/player.{id}` — komunikaty prywatne do gracza

Eventy (DTO): `QuestionEvent`, `RankingEvent` (zgodnie z diagramem sekwencji).

## Przebieg rundy (diagram sekwencji — kontrakt logiki)

1. Host → `SEND /app/game.nextQuestion` → `GameService.nextQuestion(sessionId)`
   → `getNextQuestion()` → `BROADCAST /topic/game.{id}` z `QuestionEvent`.
2. Gracz → `SEND /app/game.answer` → **AntiCheatAspect** przechwytuje `submitAnswer()`,
   waliduje timestamp → `GameService.submitAnswer()` → `save(PlayerAnswer)`.
3. Po timeout / wszystkich odpowiedziach: `RankingService.calculateScore()` →
   `broadcastRanking()` → `getLiveRanking()` → `BROADCAST /topic/game.{id}` z `RankingEvent`.
4. Pętla rund aż do braku kolejnych pytań → wyniki końcowe + zapis do DB → `FINISHED`.

## Polityka testów

- **JUnit 5 + Mockito** dla logiki serwisów (unit, mockowane repozytoria).
- **Testcontainers (PostgreSQL)** dla testów integracyjnych repozytoriów/REST.
- Priorytet testów: `GameService` (przejścia stanów), `RankingService` (punktacja),
  `AntiCheatAspect` (odpowiedź po czasie = 0 pkt).
- Nie pisz testów na getterach/setterach ani na trywialnym CRUD bez logiki.

## Czego NIE robić

- NIE wprowadzać Spring Security / JWT / OAuth, jeśli plan tego nie wymaga (PDF nie
  opisuje pełnej autoryzacji — `passwordHash` istnieje, ale zakres logowania ustalamy
  w planie, nie domyślnie).
- NIE dodawać Kafki, Redisa, message brokerów spoza wbudowanego STOMP broker.
- NIE generować frontendu zanim backend + kontrakty WS nie są gotowe i przetestowane.
- NIE refaktoryzować "przy okazji" kodu spoza bieżącej fazy.

## Komendy

```bash
# backend
cd backend
mvn compile                  # szybka weryfikacja kompilacji
mvn test                     # testy
mvn spring-boot:run          # lokalne uruchomienie (wymaga Postgres na 5432)

# baza lokalnie
docker compose up -d db      # tylko Postgres do dev

# frontend
cd frontend
npm install
npm run dev
```
