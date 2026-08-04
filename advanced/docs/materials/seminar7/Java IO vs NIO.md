# Java IO vs NIO

Java даёт два поколения ввода-вывода: классический IO (`java.io`, потоки) и NIO — двухслойный: низкоуровневые каналы и
буферы (`java.nio.channels`) и высокоуровневый файловый API NIO.2 (`java.nio.file`). Этот материал разбирает все три
слоя, их модель и границы применения.

## Содержание

1. Классический IO: потоки
2. NIO.2: Path и Files
3. Обход директорий
4. Каналы и буферы
5. FileChannel и прямой доступ
6. Чтение целиком против потоково
7. Ресурсы и ошибки
8. Что выбирать

---

## 1. Классический IO: потоки

`java.io` строится на потоках — последовательностях байтов (`InputStream`/`OutputStream`) или символов
(`Reader`/`Writer`), которые оборачивают друг друга (декоратор):

```java
void example(File file) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String line = reader.readLine();
    }
}
```

Модель — последовательный однонаправленный поток; управление ресурсами ручное.

---

## 2. NIO.2: Path и Files

`Path` (Java 7) — путь как объект (`resolve`, `getParent`, `relativize`), привязанный к `FileSystem` (можно работать не
только с локальным диском, но и, например, с zip-архивом как файловой системой). `Files` — статические операции:

```java
void example(Path dir) throws IOException {
    Files.createDirectories(dir);
    Path file = dir.resolve("SBER.json");
    Files.write(file, "{...}".getBytes(StandardCharsets.UTF_8));
    byte[] bytes = Files.readAllBytes(file);
}
```

`Files` бросает `IOException` вместо возврата `false` — ошибку нельзя случайно проигнорировать.

---

## 3. Обход директорий

```java
void example(Path dir) throws IOException {
    try (Stream<Path> files = Files.list(dir)) {          // одна директория
        files.filter(p -> p.toString().endsWith(".json")).forEach(System.out::println);
    }
    try (Stream<Path> tree = Files.walk(dir)) {           // рекурсивно
        long count = tree.filter(Files::isRegularFile).count();
    }
}
```

Стримы `list`/`walk` держат открытый дескриптор директории и закрываются через try-with-resources; иначе — утечка,
проявляющаяся под нагрузкой исчерпанием лимита открытых файлов.

---

## 4. Каналы и буферы

Нижний слой NIO — каналы (`Channel`) и буферы (`ByteBuffer`). В отличие от потоков, канал двунаправлен, работает
**блоками** через буфер и поддерживает **неблокирующий** режим (основа сетевой модели с `Selector`).

`ByteBuffer` имеет три указателя: `position` (текущая позиция), `limit` (граница данных), `capacity` (ёмкость). Ключевой
метод — `flip()`: после записи в буфер он переключает буфер в режим чтения (`limit = position; position = 0`).

```java
void example(FileChannel channel) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    int read = channel.read(buffer);   // канал заполняет буфер
    buffer.flip();                     // готовим буфер к чтению
    while (buffer.hasRemaining()) {
        byte b = buffer.get();
    }
    buffer.clear();                    // возвращаем в режим записи
}
```

`ByteBuffer.allocateDirect(...)` создаёт «прямой» буфер вне кучи JVM — данные не копируются между кучей и ядром, что
выгодно для частого I/O, но выделение дороже.

---

## 5. FileChannel и прямой доступ

`FileChannel` даёт возможности сверх `Files`: позиционный доступ (`read`/`write` со смещением), блокировки файла
(`lock`), эффективную передачу между каналами (`transferTo`/`transferFrom`, потенциально zero-copy) и отображение файла в
память (`map`, см. материал «Эффективная работа с диском»).

```java
void example(Path from, Path to) throws IOException {
    try (FileChannel in = FileChannel.open(from, StandardOpenOption.READ);
         FileChannel out = FileChannel.open(to, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
        in.transferTo(0, in.size(), out);   // копирование каналом, без промежуточного буфера в коде
    }
}
```

---

## 6. Чтение целиком против потоково

Маленький файл — целиком (`Files.readAllBytes`/`readString`); большой — потоково (`Files.lines`, буферизованный канал),
не загружая в память. Выбор диктуется размером: `readString` большого лога — риск `OutOfMemoryError`.

---

## 7. Ресурсы и ошибки

Всё, что держит дескриптор (потоки, каналы, стримы `Files.list`/`lines`), закрывается через try-with-resources.
Файловые операции бросают `IOException` — проверяемое исключение, которое вызывающий обязан обработать или пробросить.

---

## 8. Что выбирать

- Прикладной код (сохранить/загрузить JSON) — NIO.2 (`Path`/`Files`).
- Контроль над блоками, mmap, zero-copy, неблокирующий режим — каналы (`FileChannel`, `SocketChannel`) и `ByteBuffer`.
- Классический IO — legacy и совместимость.
