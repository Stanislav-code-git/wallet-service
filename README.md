# Wallet Service - REST-приложение для управления балансом кошелька.  
Поддерживает операции **пополнения (DEPOSIT)** и **списания (WITHDRAW)**, а также получение текущего баланса.  

## Стек
- Java 17  
- Spring Boot 3.2.x  
- PostgreSQL 15/16  
- Liquibase (миграции схемы)  
- Docker + docker-compose  
- Testcontainers (интеграционные тесты)  

## Запуск

### 1. Локально (без Docker)
# сборка
mvn clean package

# запуск
java -jar target/wallet-service-0.0.1-SNAPSHOT.jar

По умолчанию приложение слушает порт **8080**.

### 2. В Docker
docker compose up --build

Поднимутся два контейнера:  
- `wallet_db` — PostgreSQL 16  
- `wallet_app` — Spring Boot приложение  

## ⚙️ Переменные окружения

Все параметры можно настраивать без пересборки контейнеров через `.env` или прямо в `docker-compose.yml`:

| Переменная             | Назначение                           | Значение по умолчанию       |
|-------------------------|--------------------------------------|-----------------------------|
| `SERVER_PORT`          | Порт HTTP сервиса                    | `8080`                      |
| `DB_URL`               | JDBC URL базы                        | `jdbc:postgresql://db:5432/wallet` |
| `DB_USER`              | Пользователь БД                      | `wallet`                    |
| `DB_PASSWORD`          | Пароль БД                            | `wallet`                    |
| `DB_POOL_SIZE`         | Максимум соединений пула Hikari       | `50`                        |
| `DB_MIN_IDLE`          | Минимум idle-соединений              | `10`                        |
| `DB_CONN_TIMEOUT_MS`   | Таймаут получения соединения (мс)    | `500`                       |
| `LOG_LEVEL`            | Уровень логов (`INFO`, `DEBUG`, …)   | `INFO`                      |

---

## REST API

### POST `/api/v1/wallet`
Создание операции над кошельком (DEPOSIT или WITHDRAW).  

**Request (JSON):**
```json
{
  "walletId": "11111111-1111-1111-1111-111111111111",
  "operationType": "DEPOSIT",
  "amount": 1000
}
```

**Response (200):**
```json
{
  "walletId": "11111111-1111-1111-1111-111111111111",
  "balance": 1000
}
```

---

### GET `/api/v1/wallets/{id}`
Получить текущий баланс кошелька.  

**Response (200):**
```json
{
  "walletId": "11111111-1111-1111-1111-111111111111",
  "balance": 1000
}
```

---

### Ошибки
Формат ошибок соответствует [RFC 7807 Problem Details](https://datatracker.ietf.org/doc/html/rfc7807):  
```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Insufficient funds"
}
```

Коды ошибок:  
- `400` — невалидный JSON или ошибка валидации  
- `404` — кошелёк не найден  
- `409` — недостаточно средств  

---

## Тестирование

Запуск unit и интеграционных тестов:  
```bash
mvn verify
```

В проекте есть:  
- Unit-тесты сервиса  
- Интеграционные тесты с Testcontainers (Postgres)  
- Конкурентный тест (`ConcurrencyTest`) — моделирует 1000 одновременных запросов
