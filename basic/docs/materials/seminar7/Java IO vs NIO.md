# Java IO vs NIO

Java предоставляет два поколения API ввода-вывода: классический IO (`java.io`) и NIO.2 (`java.nio.file`). Этот материал
разбирает оба, их модель работы и когда какое применять; акцент — на NIO.2, на котором пишут современный код.

## Содержание

1. Классический IO: потоки
2. NIO.2: Path
3. NIO.2: Files
4. Обход директорий
5. Чтение и запись: целиком против потоково
6. Обработка ошибок и ресурсы
7. Что выбирать

---

## 1. Классический IO: потоки

`java.io` появился в Java 1.0 и строится на потоках (streams) — последовательностях данных:

- байтовые: `InputStream` / `OutputStream` (`FileInputStream`, `FileOutputStream`);
- символьные: `Reader` / `Writer` (`FileReader`, `FileWriter`).

Потоки часто оборачивают друг в друга для добавления возможностей — например, буферизации:

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

Модель — последовательный доступ от начала к концу. Управление ресурсами ручное (try-with-resources обязателен, иначе
утечка дескрипторов).

---

## 2. NIO.2: Path

`Path` (Java 7) представляет путь в файловой системе как объект, а не строку. Он умеет операции над путями без обращения
к диску:

```java
void example() {
    Path dir = Path.of("data", "instruments");
    Path file = dir.resolve("SBER.json");   // data/instruments/SBER.json
    Path parent = file.getParent();          // data/instruments
    String name = file.getFileName().toString(); // SBER.json
}
```

`Path` привязан к файловой системе (`FileSystem`), что позволяет работать не только с локальным диском, но и, например, с
содержимым zip-архива как с файловой системой.

---

## 3. NIO.2: Files

`Files` — набор статических операций над файлами и директориями. Основные:

```java
void example(Path file, Path dir) throws IOException {
    // Чтение и запись целиком
    String text = Files.readString(file);
    byte[] bytes = Files.readAllBytes(file);
    Files.writeString(file, "content");
    Files.write(file, bytes);

    // Существование и метаданные
    boolean exists = Files.exists(file);
    long size = Files.size(file);

    // Директории и файловые операции
    Files.createDirectories(dir);
    Files.copy(file, dir.resolve("copy.json"));
    Files.move(file, dir.resolve("moved.json"));
    Files.deleteIfExists(file);
}
```

`Files` бросает `IOException` при ошибке, а не возвращает `false` (как `File.mkdirs`/`File.delete`), — ошибку невозможно
случайно проигнорировать.

---

## 4. Обход директорий

`Files.list` возвращает стрим содержимого одной директории; `Files.walk` — рекурсивный обход дерева:

```java
void example(Path dir) throws IOException {
    try (Stream<Path> files = Files.list(dir)) {
        List<Path> jsons = files
                .filter(path -> path.toString().endsWith(".json"))
                .toList();
    }
}
```

Эти стримы держат открытый системный ресурс (дескриптор директории), поэтому их **обязательно** закрывают через
try-with-resources. Забытое закрытие — утечка, которая проявляется не сразу, а под нагрузкой (исчерпание лимита
открытых файлов).

---

## 5. Чтение и запись: целиком против потоково

- **Целиком.** `Files.readString`/`readAllBytes` читают весь файл в память одной операцией. Удобно и быстро для
  небольших файлов (конфиги, наши JSON инструментов).
- **Потоково.** `Files.lines(path)` возвращает ленивый стрим строк, не загружая весь файл в память, — для больших
  файлов (логи, выгрузки):

```java
void example(Path bigLog) throws IOException {
    try (Stream<String> lines = Files.lines(bigLog)) {
        long errors = lines.filter(line -> line.contains("ERROR")).count();
    }
}
```

Выбор — по размеру файла: маленький проще прочитать целиком, большой безопаснее обрабатывать потоково.

---

## 6. Обработка ошибок и ресурсы

Файловые операции бросают `IOException` (проверяемое исключение) — вызывающий код обязан его обработать или пробросить.
Любой ресурс, держащий дескриптор (потоки IO, стримы `Files.list`/`lines`, каналы), закрывается через try-with-resources.
Это не стилистика, а предотвращение утечки дескрипторов операционной системы.

---

## 7. Что выбирать

Для нового кода — NIO.2 (`Path`/`Files`): компактнее, безопаснее по ошибкам, интегрирован со Stream API. Классический IO
знать нужно: он лежит в основе многих библиотек и встречается в существующем коде. Для наших задач (сохранить и
загрузить JSON) достаточно `Files.write`/`readString` и `Files.list` для обхода директории.
