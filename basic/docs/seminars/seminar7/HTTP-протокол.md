# HTTP-протокол

---

## Что это

HTTP — текстовый протокол прикладного уровня по модели «запрос-ответ»: клиент отправляет запрос, сервер возвращает
ответ. На нём работает почти всё сетевое взаимодействие с веб-API, включая T-Invest API. Для похода в API достаточно
понимать структуру запроса и ответа.

---

## Запрос

Состоит из четырёх частей:

- **Метод** — что делаем: `GET` (получить), `POST` (отправить данные / вызвать операцию), реже `PUT`/`DELETE`.
- **URL** — адрес ресурса.
- **Заголовки** — метаданные (`ключ: значение`).
- **Тело** — данные запроса (у `POST`; у `GET` обычно пусто).

```
POST /rest/tinkoff.public.invest.api.contract.v1.MarketDataService/GetLastPrices HTTP/1.1
Host: invest-public-api.tinkoff.ru
Authorization: Bearer <token>
Content-Type: application/json

{ "instrumentId": ["<figi>"] }
```

---

## Ответ

- **Статус-код** — результат:
  - `2xx` — успех (`200 OK`);
  - `4xx` — ошибка клиента (`401 Unauthorized` — нет/неверный токен, `404 Not Found`, `429 Too Many Requests` —
    превышен лимит запросов);
  - `5xx` — ошибка сервера.
- **Заголовки** — метаданные ответа (`Content-Type`, длина и т.д.).
- **Тело** — данные ответа (у API обычно JSON).

Первое, что проверяют после запроса, — статус-код: `2xx` означает, что телу можно доверять.

---

## Заголовки, важные для API

- `Authorization: Bearer <token>` — аутентификация по токену. Сервер по нему понимает, кто запрашивает и что ему
  разрешено.
- `Content-Type: application/json` — формат тела запроса.
- `Accept: application/json` — желаемый формат ответа.

---

## HTTP в Java: HttpClient

Стандартная библиотека содержит `java.net.http.HttpClient` (Java 11+) — сторонние библиотеки не нужны. Он собирает
запрос, отправляет и возвращает ответ:

```java
void example(String token, String figi) throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://invest-public-api.tinkoff.ru/rest/..."))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{ \"instrumentId\": [\"" + figi + "\"] }"))
            .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    int status = response.statusCode();   // проверяем: 200?
    String body = response.body();         // JSON-ответ
}
```

`send(...)` — блокирующий вызов: поток ждёт ответа. Для CLI-утилиты этого достаточно. Есть и `sendAsync(...)`,
возвращающий `CompletableFuture`, — для асинхронной обработки.
