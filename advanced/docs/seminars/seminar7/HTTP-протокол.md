# HTTP-протокол

---

## Модель запрос-ответ

HTTP — текстовый протокол прикладного уровня: клиент отправляет запрос, сервер возвращает ответ. На нём работает
взаимодействие с T-Invest API. Запрос: метод, URL, заголовки, тело. Ответ: статус-код, заголовки, тело.

```
POST /rest/.../MarketDataService/GetLastPrices HTTP/1.1
Host: invest-public-api.tinkoff.ru
Authorization: Bearer <token>
Content-Type: application/json

{ "instrumentId": ["<figi>"] }
```

---

## Методы и статусы

- **Методы:** `GET` — получить, `POST` — отправить/вызвать операцию (T-Invest REST — через `POST`).
- **Статусы:** `2xx` — успех; `4xx` — ошибка клиента (`401` — токен, `429` — лимит запросов); `5xx` — ошибка сервера.
  После запроса первым делом проверяют статус.

---

## Заголовки

- `Authorization: Bearer <token>` — аутентификация по токену.
- `Content-Type: application/json` — формат тела.

---

## HttpClient

`java.net.http.HttpClient` (Java 11+) — в стандартной библиотеке, без зависимостей:

```java
void example(String token, String body) throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://invest-public-api.tinkoff.ru/rest/..."))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
}
```

`send(...)` блокирующий; `sendAsync(...)` возвращает `CompletableFuture`. Важно: `HttpClient` — высокоуровневая обёртка;
под ней неблокирующие каналы и `Selector`, скрытые от прикладного кода. Их устройство — отдельная тема (см.
«Selector и AsynchronousSocketChannel»).
