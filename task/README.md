# Task Service

## Описание
**Task Service** предоставляет функциональность для управления задачами и подзадачами пользователей, включая создание, обновление, удаление и получение задач и подзадач. Основные возможности:

- Управление задачами: создание, обновление, удаление и получение задач.
- Управление подзадачами: создание, обновление, удаление и получение подзадач, связанных с задачами.
- Асинхронное удаление задач с использованием **RabbitMQ**.
- Кэширование данных задач и подзадач с использованием **Redis**.
- Фильтрация задач с помощью **Spring Data JPA**.

Для хранения данных о задачах используется **PostgreSQL**. Для кеширования данных используется Redis с кастомным сериализатором для **protobuf**, что позволило уменьшить вес кешируемых объектов.

Для обмена сообщениями между сервисами используется **RabbitMQ**. Система использует **Spring Security** для управления доступом, обеспечивая различные уровни доступа для пользователей и администраторов. В системе предусмотрены две роли:
- **User** — обычный пользователь, который может управлять только своим задачами.
- **Admin** — администратор, который может управлять задачами всех пользователей.


## API эндпоинты


### **Task Controller**

#### **Получить страницу с задачами.**

**GET** `/api/tasks`

**Тело запроса:**

Нет тела запроса.

**Ответ:**

HTTP статус: `200 OK`

**Тело ответа:**
```json
{
    "content": [
        {
            "id": 1,
            "title": "New Task 13",
            "description": "Description of the task",
            "category": "Work",
            "subtasks": [
                {
                    "id": 1,
                    "title": "Subtask 10",
                    "createdBy": 1,
                    "createdAt": "2025-01-20T18:30:38.13883",
                    "redactedAt": "2025-01-20T18:30:38.138852",
                    "completed": true
                },
                {
                    "id": 2,
                    "title": "Subtask 12",
                    "createdBy": 1,
                    "createdAt": "2025-01-20T18:30:38.143475",
                    "redactedAt": "2025-01-20T18:30:38.1435",
                    "completed": true
                }
            ],
            "dateEnd": "2025-01-05T03:29:25",
            "importance": 5,
            "createdBy": 1,
            "createdAt": "2025-01-20T18:30:38.075386",
            "redactedAt": "2025-01-20T18:30:38.154159",
            "endedAt": "2025-01-20T18:30:38.150279"
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "unsorted": false,
            "sorted": true,
            "empty": false
        },
        "offset": 0,
        "unpaged": false,
        "paged": true
    },
    "totalPages": 1,
    "totalElements": 1,
    "last": true,
    "numberOfElements": 1,
    "size": 10,
    "number": 0,
    "sort": {
        "unsorted": false,
        "sorted": true,
        "empty": false
    },
    "first": true,
    "empty": false
}
```

---

#### **Получить задачу по ID.**

**GET** `/api/tasks/{id}`

**Тело запроса:**

Нет тела запроса.

**Ответ:**

HTTP статус: `200 OK`

**Тело ответа:**
```json
{
    "id": 1,
    "title": "New Task 13",
    "description": "Description of the task",
    "category": "Work",
    "subtasks": [
        {
            "id": 2,
            "title": "Subtask 12",
            "createdBy": 1,
            "createdAt": "2025-01-20T18:30:38.143475",
            "redactedAt": "2025-01-20T18:30:38.1435",
            "completed": true
        },
        {
            "id": 3,
            "title": "Subtask 43",
            "createdBy": 1,
            "createdAt": "2025-01-20T18:32:49.126116",
            "redactedAt": "2025-01-20T18:32:49.126143",
            "completed": true
        }
    ],
    "dateEnd": "2025-01-05T03:29:25",
    "importance": 5,
    "createdBy": 1,
    "createdAt": "2025-01-20T18:30:38.075386",
    "redactedAt": "2025-01-20T18:32:49.135638",
    "endedAt": "2025-01-20T18:32:49.132504657"
}
```

---

#### **Создать новую задачу.**

**POST** `/api/tasks`

**Тело запроса:**
```json
{
    "task": {
        "title": "New Task 13",
        "description": "Description of the task",
        "category": "Work",
        "importance": 5,
        "dateEnd": "2025-01-05T03:29:25"
    },
    "subtasks": [
        {
            "title": "Subtask 10",
            "completed": true
        },
        {
            "title": "Subtask 12",
            "completed": true
        }
    ]
}

```
**Ответ:**

HTTP статус: `201 CREATED`

**Тело ответа:**
```json
{
    "id": 1,
    "title": "New Task 13",
    "description": "Description of the task",
    "category": "Work",
    "subtasks": [
        {
            "id": 1,
            "title": "Subtask 10",
            "createdBy": 1,
            "createdAt": "2025-01-20T18:30:38.13883",
            "redactedAt": "2025-01-20T18:30:38.138852",
            "completed": true
        },
        {
            "id": 2,
            "title": "Subtask 12",
            "createdBy": 1,
            "createdAt": "2025-01-20T18:30:38.143475",
            "redactedAt": "2025-01-20T18:30:38.1435",
            "completed": true
        }
    ],
    "dateEnd": "2025-01-05T03:29:25",
    "importance": 5,
    "createdBy": 1,
    "createdAt": "2025-01-20T18:30:38.075386",
    "redactedAt": "2025-01-20T18:30:38.154159",
    "endedAt": "2025-01-20T18:30:38.150279002"
}
```

---

#### **Обновить задачу по ID.**

**PUT** `/api/tasks/{id}`

**Тело запроса:**
```json
{
    "task": {
        "title": "New Task 13",
        "description": "Description of the task",
        "category": "Work",
        "importance": 5,
        "dateEnd": "2025-01-05T03:29:25"
    },
    "subtasks": [
        {
            "title": "Subtask 10",
            "completed": true
        },
        {
            "title": "Subtask 12",
            "completed": true
        }
    ]
}

```

**Ответ:**

HTTP статус: `200 OK`

**Тело ответа:**
```json
{
    "id": 1,
    "title": "New Task 13",
    "description": "Description of the task",
    "category": "Work",
    "subtasks": [
        {
            "id": 1,
            "title": "Subtask 10",
            "createdBy": 1,
            "createdAt": "2025-01-20T18:30:38.13883",
            "redactedAt": "2025-01-20T18:30:38.138852",
            "completed": true
        },
        {
            "id": 2,
            "title": "Subtask 12",
            "createdBy": 1,
            "createdAt": "2025-01-20T18:30:38.143475",
            "redactedAt": "2025-01-20T18:30:38.1435",
            "completed": true
        }
    ],
    "dateEnd": "2025-01-05T03:29:25",
    "importance": 5,
    "createdBy": 1,
    "createdAt": "2025-01-20T18:30:38.075386",
    "redactedAt": "2025-01-20T18:30:38.154159",
    "endedAt": "2025-01-20T18:30:38.150279002"
}
```

---

#### **Удалить задачу по ID.**

**DELETE** `/api/tasks/{id}`

**Тело запроса:**

Нет тела запроса.

**Ответ:**

HTTP статус: `204 NO CONTENT`

Нет тела ответа

---

### **Subtask Controller**

#### **Получить подзадачу по ID.**

**GET** `/api/subtasks/{id}`

**Тело запроса:**

Нет тела запроса.

**Ответ:**

HTTP статус: `200 OK`

**Тело ответа:**
```json
{
    "id": 2,
    "title": "Subtask 12",
    "createdBy": 1,
    "createdAt": "2025-01-20T18:30:38.143475",
    "redactedAt": "2025-01-20T18:30:38.1435",
    "completed": true
}
```

---

#### **Создать новую подзадачу.**

**POST** `/api/subtasks`

**Тело запроса:**

```json
{
    "subtask": {
        "title": "Subtask 43",
        "completed": true
    },
    "taskId": 1
}
```

**Ответ:**

HTTP статус: `201 CREATED`

**Тело ответа:**
```json
{
    "id": 3,
    "title": "Subtask 43",
    "createdBy": 1,
    "createdAt": "2025-01-20T18:32:49.126116",
    "redactedAt": "2025-01-20T18:32:49.126143",
    "completed": true
}
```

---

#### **Обновить подзадачу по ID.**

**PUT** `/api/subtasks/{id}`

**Тело запроса:**

```json
{
  "title": "Subtask 19",
  "completed": false
}
```

**Ответ:**

HTTP статус: `200 OK`

**Тело ответа:**
```json
{
    "id": 2,
    "title": "Subtask 19",
    "createdBy": 1,
    "createdAt": "2025-01-20T18:32:49.126116",
    "redactedAt": "2025-01-20T18:32:49.126143",
    "completed": false
}
```

---

#### **Отметить статус выполнения подзадачи по ID.**

**PUT** `/api/subtasks/{id}/mark`

**Тело запроса:**

```json
true
```

**Ответ:**

HTTP статус: `200 OK`

**Тело ответа:**
```json
{
    "id": 2,
    "title": "Subtask 19",
    "createdBy": 1,
    "createdAt": "2025-01-20T18:32:49.126116",
    "redactedAt": "2025-01-20T18:32:49.126143",
    "completed": true
}
```

---

#### **Удалить подзадачу по ID.**

**DELETE** `/api/subtasks/{id}`

**Тело запроса:**

Нет тела запроса.

**Ответ:**

HTTP статус: `204 NO CONTENT`

Нет тела ответа.

## Исключения
Обрабатываемые исключения:
- **NoAccessException** - Нет доступа (HTTP 403).
- **NotFoundException** - Данные не найдены (HTTP 404).

## Зависимости
- **Spring Boot**: Основной фреймворк приложения.
- **Spring Security**: Аутентификация и авторизация.
- **PostgreSQL**: Хранилище данных пользователей.
- **Redis**: Кеширование данных.
- **MongoDB**: Хранилище сессий.
- **RabbitMQ**: Обмен сообщениями между сервисами.

## Настройки

### Переменные окружения
**Настройки для PostgreSQL**:
- `DB_HOST`: Хост для подключения (по умолчанию `localhost`).
- `DB_PORT`: Порт для подключения (по умолчанию `5432`).
- `DB_NAME`: Имя базы данных (по умолчанию `task`).
- `DB_USERNAME`: Логин для подключения (по умолчанию `taskapp`).
- `DB_PASSWORD`: Пароль для подключения (по умолчанию `taskpassword`).

**Настройки для MongoDB**:
- `SESSION_DB_HOST`: Хост для подключения (по умолчанию `localhost`).
- `SESSION_DB_PORT`: Порт для подключения (по умолчанию `27017`).
- `SESSION_DB_NAME`: Имя базы данных (по умолчанию `session`).

**Настройки для RabbitMQ**:
- `RABBITMQ_HOST`: Хост для подключения (по умолчанию `localhost`).
- `RABBITMQ_PORT`: Порт для подключения (по умолчанию `5672`).
- `RABBITMQ_USERNAME`: Логин для подключения (по умолчанию `taskapp`).
- `RABBITMQ_PASSWORD`: Пароль для подключения (по умолчанию `taskpassword`).

**Настройки для RabbitMQ**:
- `REDIS_HOST`: Хост для подключения (по умолчанию `localhost`).
- `REDIS_PORT`: Порт для подключения (по умолчанию `6379`).
