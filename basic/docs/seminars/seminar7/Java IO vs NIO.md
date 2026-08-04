# Java IO vs NIO

---

## Зачем это

Пока состояние приложения живёт только в памяти, оно теряется при перезапуске. Чтобы данные сохранялись, их пишут на
диск. Java даёт для файловых операций два поколения API — классический IO и NIO.2. Новый код пишут на NIO.2; классический
IO полезно знать, потому что он никуда не делся и лежит в основе многого.

---

## Классический IO (java.io)

Появился в Java 1.0. Работает через потоки (streams): последовательности байтов (`InputStream`/`OutputStream`) или
символов (`Reader`/`Writer`). Типична ручная обёртка одного потока в другой и явное управление ресурсами.

```java
void example(File file) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }
}
```

Модель — последовательный поток данных: читаем/пишем от начала к концу.

---

## NIO.2 (java.nio.file)

Появился в Java 7. Два ключевых типа:

- **`Path`** — путь в файловой системе как объект (вместо `String`): умеет `resolve`, `getParent`, `relativize`,
  сравнение, работу с разными файловыми системами.
- **`Files`** — статические операции над файлами и директориями: чтение/запись целиком, создание, копирование,
  перемещение, удаление, обход дерева, атрибуты.

```java
void example(Path file) throws IOException {
    Files.writeString(file, "SBER\nGAZP");           // запись строки целиком
    String content = Files.readString(file);          // чтение целиком
    List<String> lines = Files.readAllLines(file);     // чтение по строкам
    boolean exists = Files.exists(file);
}
```

`Path` создаётся через `Path.of(...)`:

```java
void example() {
    Path dir = Path.of("data", "instruments");
    Path file = dir.resolve("SBER.json");   // data/instruments/SBER.json
}
```

---

## Сравнение

| | Классический IO | NIO.2 |
|---|---|---|
| Пакет | `java.io` | `java.nio.file` |
| Путь | `File` | `Path` |
| Чтение файла целиком | ручной цикл по потоку | `Files.readString` / `readAllBytes` |
| Обход директории | `File.listFiles()` (массив) | `Files.list` / `walk` (стрим) |
| Создание директорий | `mkdirs()` (boolean) | `Files.createDirectories` (исключение при ошибке) |

NIO.2 компактнее, честнее сообщает об ошибках (бросает `IOException` вместо возврата `false`) и лучше интегрирован со
Stream API.

---

## Работа с директориями через NIO

```java
void example(Path dir) throws IOException {
    Files.createDirectories(dir);                     // создаёт все недостающие уровни
    try (Stream<Path> files = Files.list(dir)) {      // стрим содержимого директории
        files.filter(p -> p.toString().endsWith(".json"))
             .forEach(System.out::println);
    }
}
```

`Files.list`/`Files.walk` возвращают стрим, который держит открытый системный ресурс (дескриптор директории), поэтому его
закрывают через try-with-resources. Забытое закрытие — утечка ресурсов.
