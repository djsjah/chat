# Чат образовательной платформы между студентами и менторами

Проект реализован с использованием следующих технологий:
- Java
- Spring
- JPA
- Liquibase
- PostgreSQL
- Keycloak
- Prometheus
- Grafana
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

- Бизнесовые счетчики сообщений, участников, комнат и CDC-записей увеличиваются только после успешного коммита транзакции.
- Технические метрики HTTP, JVM, datasource, process и Spring Security публикуются автоматически через Spring Boot Actuator и Micrometer.
- Метрики могут использоваться в Prometheus и визуализироваться в Grafana.

## Monitoring и инфраструктура

### Docker Compose

Файлы для поднятия инфраструктуры мониторинга и аутентификации находятся в каталоге `deploy`:

- `deploy/monitoring/docker-compose.yml` — Prometheus и Grafana
- `deploy/monitoring/prometheus/prometheus.yml` — конфигурация Prometheus
- `deploy/monitoring/provisioning/datasources/prometheus.yml` — автоматическое подключение Grafana к Prometheus
- `deploy/keycloak/docker-compose.yml` — Keycloak, MailHog и PostgreSQL для Keycloak
- `deploy/keycloak/import/chat-realm.json` — импорт realm для Keycloak

### Где реализованы кастомные метрики

#### CDC / Outbox

- `src/main/java/com/chat/cdc/service/CdcMetricService` — регистрация CDC-метрик
- `src/main/java/com/chat/cdcCdcMetricBinder` — периодическое обновление gauge-метрик по таблице `cdc`
- `src/main/java/com/chat/service/CdcInternalService` — использование CDC-метрик при создании outbox-записей

#### Realtime

- `src/main/java/com/chat/infrastructure/realtime/service/RealtimeMetricService` — регистрация realtime-метрик
- `src/main/java/com/chat/infrastructure/realtime/service/RealtimeService` — использование realtime-метрик при генерации токенов подключения/подписки

#### Chat domain

- `src/main/java/com/chat/app/metric/ChatMetricService` — регистрация продуктовых метрик домена чата
- `src/main/java/com/chat/core/message/service/MessageFacade` — использование метрик создания, обновления и удаления сообщений
- `src/main/java/com/chat/core/member/MemberService` — использование метрики создания участника
- `src/main/java/com/chat/core/admin/member/AdminMemberService` — использование метрики удаления участника
- `src/main/java/com/chat/core/admin/room/service/AdminRoomService` — использование метрики создания комнаты
- `src/main/java/com/chat/core/admin/room/service/AdminRoomMemberFacade` — использование метрик добавления и удаления участника из комнаты

### Endpoint метрик

Приложение экспортирует метрики по адресу:

`/actuator/prometheus`

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