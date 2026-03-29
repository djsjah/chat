# Чат образовательной платформы между студентами и менторами

Проект реализован с использованием следующих технологий:
- Java
- Spring
- JPA
- Gradle
- Liquibase
- PostgreSQL
- Keycloak
- Prometheus
- Grafana
- Grafana Loki
- Grafana Alloy
- Grafana Tempo
- OpenTelemetry
- Centrifugo

## Дисциплина

Инструментальные средства разработки ПО

## Студент

Ноздряков Богдан, M4106

## Документация

OpenAPI-спецификация находится по пути:

`src/main/resources/static/openapi.yml`

## Метрики

Приложение публикует стандартные технические метрики Spring Boot Actuator / Micrometer и кастомные продуктовые метрики через endpoint:

`/actuator/prometheus`

### Кастомные метрики

#### Chat domain metrics

- `chat_messages_created_total` — `Counter`  
  Общее количество созданных сообщений чата.

- `chat_messages_updated_total` — `Counter`  
  Общее количество обновленных сообщений чата.

- `chat_messages_deleted_total` — `Counter`  
  Общее количество удаленных сообщений чата.

- `chat_members_joined_total` — `Counter`  
  Общее количество событий добавления участника в комнату.

- `chat_members_left_total` — `Counter`  
  Общее количество событий удаления участника из комнаты.

- `chat_members_created_total` — `Counter`  
  Общее количество созданных участников чата.

- `chat_members_deleted_total` — `Counter`  
  Общее количество удаленных участников чата.

- `chat_rooms_created_total` — `Counter`  
  Общее количество созданных чат-комнат.

#### CDC / Outbox metrics

- `chat_cdc_created_total` — `Counter`  
  Общее количество созданных записей в таблице `cdc`.

- `chat_cdc_rows_current` — `Gauge`  
  Текущее количество записей в таблице `cdc`.

- `chat_cdc_oldest_row_age_seconds` — `Gauge`  
  Возраст самой старой записи в таблице `cdc` в секундах. Используется для отслеживания возможного накопления необработанных событий.

#### Realtime metrics

- `chat_realtime_connection_token_seconds` — `Timer`  
  Время генерации `connection token` для Realtime-сервера.

- `chat_realtime_subscription_token_seconds` — `Timer`  
  Время генерации `subscription token` для подписки на канал комнаты.

- `chat_realtime_connection_token_generated_total` — `Counter`  
  Общее количество успешно сгенерированных `connection token`.

- `chat_realtime_subscription_token_generated_total` — `Counter`  
  Общее количество успешно сгенерированных `subscription token`.

- `chat_realtime_subscription_token_errors_total` — `Counter`  
  Общее количество ошибок при генерации `subscription token`.

### Примечания

- Счетчики сообщений, участников, комнат и CDC-записей увеличиваются только после успешного коммита транзакции.
- Технические метрики HTTP, JVM, datasource, process и Spring Security публикуются автоматически через Spring Boot Actuator и Micrometer.

## Monitoring и инфраструктура

### Docker Compose

Файлы для поднятия инфраструктуры мониторинга и аутентификации находятся в каталоге `deploy`:

- `deploy/monitoring/docker-compose.yml` — Prometheus, Grafana, Loki, Tempo и Alloy
- `deploy/monitoring/prometheus/config.yml` — конфигурация Prometheus
- `deploy/monitoring/provisioning/datasources/prometheus.yml` — datasource Prometheus для Grafana
- `deploy/monitoring/provisioning/datasources/loki.yml` — datasource Loki для Grafana
- `deploy/monitoring/provisioning/datasources/tempo.yml` — datasource Tempo для Grafana
- `deploy/monitoring/loki/config.yml` — конфигурация Grafana Loki
- `deploy/monitoring/tempo/config.yml` — конфигурация Grafana Tempo
- `deploy/monitoring/alloy/config.alloy` — конфигурация агента сбора логов и экспорта в Loki
- `deploy/keycloak/docker-compose.yml` — Keycloak, MailHog и PostgreSQL для Keycloak
- `deploy/keycloak/import/chat-realm.json` — импорт realm для Keycloak

### Где реализованы кастомные метрики

#### CDC / Outbox

- `src/main/java/com/chat/cdc/service/CdcMetricService` — регистрация CDC-метрик
- `src/main/java/com/chat/cdc/CdcMetricBinder` — периодическое обновление gauge-метрик по таблице `cdc`
- `src/main/java/com/chat/cdc/service/CdcInternalService` — использование CDC-метрик при создании outbox-записей

#### Realtime

- `src/main/java/com/chat/infrastructure/realtime/service/RealtimeMetricService` — регистрация realtime-метрик
- `src/main/java/com/chat/infrastructure/realtime/service/RealtimeService` — использование realtime-метрик при генерации токенов подключения/подписки

#### Chat domain

- `src/main/java/com/chat/app/service/ChatMetricService` — регистрация продуктовых метрик домена чата
- `src/main/java/com/chat/core/message/service/MessageFacade` — использование метрик создания, обновления и удаления сообщений
- `src/main/java/com/chat/core/member/MemberService` — использование метрики создания участника
- `src/main/java/com/chat/core/admin/member/AdminMemberService` — использование метрики удаления участника
- `src/main/java/com/chat/core/admin/room/service/AdminRoomService` — использование метрики создания комнаты
- `src/main/java/com/chat/core/admin/room/service/AdminRoomMemberFacade` — использование метрик добавления и удаления участника из комнаты

### Endpoint метрик

Приложение экспортирует метрики по адресу:

`/actuator/prometheus`

### Логи

Для сбора, хранения и анализа логов приложения используются **Grafana Alloy**, **Grafana Loki** и **Grafana**.

Приложение Spring Boot записывает логи в файл:

`logs/chat-service.log`

Агент **Grafana Alloy** считывает этот файл и отправляет лог-записи в **Grafana Loki**.  
Просмотр и анализ логов выполняется в **Grafana** через datasource `Loki`.

В качестве языка запросов используется **LogQL** — встроенный язык запросов Loki, доступный в datasource Grafana Loki.

### Прикладные логи

- `Message created` — `INFO`  
  Успешное создание сообщения в комнате.

- `Message updated` — `INFO`  
  Успешное обновление сообщения.

- `Message deleted` — `INFO`  
  Успешное удаление сообщения.

- `Member created` — `INFO`  
  Успешное создание участника чата.

- `Member soft-deleted by admin` — `INFO`  
  Пометка участника как удаленного администратором.

- `Room created by admin` — `INFO`  
  Успешное создание комнаты администратором.

- `Member joined room by admin` — `INFO`  
  Успешное добавление участника в комнату администратором.

- `Member removed from room by admin` — `INFO`  
  Успешное удаление участника из комнаты администратором.

- `Realtime connection token generated` — `INFO`  
  Успешная генерация `connection token` для Realtime-сервера.

- `Realtime subscription token generated` — `INFO`  
  Успешная генерация `subscription token` для подписки на канал комнаты.

### Технические логи

- `Realtime outbox saved` — `DEBUG`  
  Успешная запись realtime / outbox события в таблицу `cdc`.

- `CDC backlog normalized` — `INFO`  
  Состояние backlog в таблице `cdc` вернулось к нормальному.

- `CDC backlog cleared` — `INFO`  
  Таблица `cdc` очищена, необработанные записи отсутствуют.

### Предупреждающие логи

- `Realtime subscription token generation failed` — `WARN`  
  Ошибка генерации `subscription token`, если комната недоступна участнику.

- `After-commit action executed immediately because no active transaction was found` — `WARN`  
  Выполнение `afterCommit`-действия вне активной транзакции.

- `CDC backlog detected` — `WARN`  
  Обнаружено накопление необработанных записей в таблице `cdc`.

### Где реализованы логи

#### Chat domain

- `src/main/java/com/chat/core/message/service/MessageFacade` — логи создания, обновления и удаления сообщений, а также технические `DEBUG`-логи записи realtime / outbox события
- `src/main/java/com/chat/core/member/MemberService` — лог создания участника
- `src/main/java/com/chat/core/admin/member/AdminMemberService` — лог soft-delete участника администратором
- `src/main/java/com/chat/core/admin/room/service/AdminRoomService` — лог создания комнаты
- `src/main/java/com/chat/core/admin/room/service/AdminRoomMemberFacade` — логи добавления и удаления участника из комнаты, а также технические `DEBUG`-логи записи realtime / outbox события

#### Realtime

- `src/main/java/com/chat/infrastructure/realtime/service/RealtimeService` — логи генерации токенов подключения и подписки на канал комнаты чата, а также предупреждающие логи ошибок подписки

#### CDC

- `src/main/java/com/chat/cdc/CdcMetricBinder` — логи обнаружения, нормализации и очистки backlog в таблице `cdc`

#### App

- `src/main/java/com/chat/app/AfterCommitExecutor` — предупреждающий лог выполнения `afterCommit`-действия вне активной транзакции

#### Примечания

- `INFO` используется для значимых бизнес-событий.
- `DEBUG` используется для технических деталей realtime / outbox интеграции.
- `WARN` используется для аномальных или подозрительных ситуаций.

## Трейсы

Приложение экспортирует distributed traces по протоколу OTLP.  
Для хранения и поиска трейсов используется **Grafana Tempo**, для просмотра — **Grafana**.

Экспорт трейсов выполняется напрямую из Spring Boot-приложения в Tempo.

### Что трассируется

Приложение публикует стандартные HTTP server traces Spring Boot / Micrometer Tracing, а также кастомные прикладные spans для ключевых бизнес-операций.

### Кастомные spans

#### Chat domain traces

- `chat.message.create`  
  Создание сообщения в комнате.

- `chat.message.patch`  
  Обновление сообщения.

- `chat.message.delete`  
  Удаление сообщения.

- `chat.member.create`  
  Создание участника чата.

#### CDC / Outbox traces

- `chat.cdc.create`  
  Создание записи в таблице `cdc` в рамках outbox / realtime-интеграции.

#### Realtime traces

- `chat.realtime.connection-token.generate`  
  Генерация токена подключения к комнате Realtime-сервера.

- `chat.realtime.subscription-token.generate`  
  Генерация токена подписки на канал комнаты.

#### Admin traces

- `chat.admin.member.delete`  
  Soft-delete участника администратором.

- `chat.admin.room.create`  
  Создание комнаты администратором.

- `chat.admin.room.member.join`  
  Добавление участника в комнату администратором.

- `chat.admin.room.member.remove`  
  Удаление участника из комнаты администратором.

#### Internal traces

- `chat.room.resolve-for-update`  
  Получение комнаты с блокировкой для операций изменения.

- `chat.admin.room.resolve-for-update`  
  Получение комнаты с блокировкой для административных операций.

### Атрибуты spans

Для прикладных spans используются low-cardinality атрибуты, например:

- `chat.operation`
- `chat.event`
- `room.id`
- `message.id`
- `member.id`
- `resolve.by`
- `cdc.method`
- `cdc.partition`

### Где реализованы трейсы

#### Chat domain

- `src/main/java/com/chat/core/message/service/MessageFacade` — прикладные spans создания, обновления и удаления сообщений
- `src/main/java/com/chat/core/member/MemberService` — span создания участника

#### Realtime

- `src/main/java/com/chat/infrastructure/realtime/service/RealtimeService` — spans генерации токена подписки и токена подключения

#### CDC / Outbox

- `src/main/java/com/chat/cdc/service/CdcInternalService` — span создания записи в таблице `cdc`

#### Admin

- `src/main/java/com/chat/core/admin/member/AdminMemberService` — span soft-delete участника
- `src/main/java/com/chat/core/admin/room/service/AdminRoomService` — span создания комнаты
- `src/main/java/com/chat/core/admin/room/service/AdminRoomMemberFacade` — spans добавления и удаления участника из комнаты
- `src/main/java/com/chat/core/admin/room/service/AdminRoomInternalService` — spans получения комнаты с блокировкой для административных операций

#### Internal

- `src/main/java/com/chat/core/room/service/RoomInternalService` — spans получения комнаты с блокировкой для операций изменения

### Экспорт трейсов

Приложение экспортирует трейсы по OTLP в Tempo по адресу:

`http://localhost:4318/v1/traces`

### Просмотр трейсов

Просмотр и анализ трейсов выполняется в **Grafana** через datasource `Tempo`.

В качестве языка запросов используется **TraceQL** — встроенный язык запросов Tempo, доступный в datasource Grafana Tempo.

### Примечания

- Трейсы создаются для HTTP-запросов автоматически через Spring Boot Actuator / Micrometer Tracing.
- Прикладные spans добавлены вручную через `ObservationRegistry` и `Observation`.
- Для корреляции логов и трейсов в шаблон логирования добавлены `traceId` и `spanId`.
- Метрики из трейсов и service graph не настраивались; в рамках проекта реализованы генерация, экспорт, сбор, просмотр и TraceQL-запросы по трейсам.

### Скриншоты для лабораторной работы 2

![Скриншот 1](docs/1.png)
![Скриншот 2](docs/2.png)
![Скриншот 3](docs/3.png)
![Скриншот 4](docs/4.png)
![Скриншот 5](docs/5.png)
![Скриншот 6](docs/6.png)

### Скриншоты для лабораторной работы 3
![Скриншот 7](docs/7.png)
![Скриншот 8](docs/8.png)
![Скриншот 9](docs/9.png)
![Скриншот 10](docs/10.png)
![Скриншот 11](docs/11.png)
![Скриншот 12](docs/12.png)
![Скриншот 13](docs/13.png)
![Скриншот 14](docs/14.png)
![Скриншот 15](docs/15.png)
![Скриншот 16](docs/16.png)
![Скриншот 17](docs/17.png)
![Скриншот 18](docs/18.png)
![Скриншот 19](docs/19.png)
![Скриншот 20](docs/20.png)

### Скриншоты для лабораторной работы 4
![Скриншот 21](docs/21.png)
![Скриншот 22](docs/22.png)
![Скриншот 23](docs/23.png)
![Скриншот 24](docs/24.png)

### Скриншоты для лабораторной работы 5
![Скриншот 25](docs/25.png)
![Скриншот 26](docs/26.png)
![Скриншот 27](docs/27.png)
![Скриншот 28](docs/28.png)
![Скриншот 29](docs/29.png)
![Скриншот 30](docs/30.png)
![Скриншот 31](docs/31.png)
![Скриншот 32](docs/32.png)