# Java IO vs NIO

---

## Два поколения API

Состояние приложения, живущее только в памяти, теряется при перезапуске; чтобы данные сохранялись, их пишут на диск.
Java даёт два поколения ввода-вывода: классический IO (`java.io`, Java 1.0) и NIO. NIO при этом двухслоен — низкоуровневые
каналы (`java.nio.channels`, Java 1.4) и высокоуровневый файловый API NIO.2 (`java.nio.file`, Java 7).

---

## Классический IO (java.io)

Потоки байтов/символов, последовательная модель, ручная обёртка ресурсов:

```java
void example(File file) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String line = reader.readLine();
    }
}
```

---

## NIO.2 (java.nio.file)

Высокоуровневый файловый API: `Path` (путь как объект) и `Files` (операции над файлами и директориями). Компактный,
бросает `IOException` вместо возврата `false`, интегрирован со Stream API.

```java
void example(Path dir) throws IOException {
    Files.createDirectories(dir);
    Path file = dir.resolve("SBER.json");
    Files.writeString(file, "{...}");
    try (Stream<Path> files = Files.list(dir)) {   // стрим держит дескриптор — закрываем
        files.filter(p -> p.toString().endsWith(".json")).forEach(System.out::println);
    }
}
```

---

## Каналы и буферы (java.nio.channels)

Нижний слой NIO — каналы (`Channel`) и буферы (`ByteBuffer`). В отличие от потоков классического IO, канал двунаправлен и
работает **блоками** через буфер, а не по одному байту, и умеет **неблокирующий** режим.

```java
void example(Path file) throws IOException {
    try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = channel.read(buffer);   // читаем блок в буфер
        buffer.flip();                     // переключаем буфер из режима записи в режим чтения
    }
}
```

`ByteBuffer` имеет позицию (`position`), предел (`limit`) и ёмкость (`capacity`); `flip()` готовит заполненный буфер к
чтению. Каналы — основа как файлового (`FileChannel`), так и сетевого (`SocketChannel`) ввода-вывода; на неблокирующих
`SocketChannel` держится модель с `Selector`.

---

## Что где применять

- Прикладной код проекта (сохранить/загрузить JSON) — NIO.2 (`Path`/`Files`): лаконично и безопасно.
- Каналы напрямую (`FileChannel`, `ByteBuffer`) — когда нужен контроль над блоками, memory-mapping или неблокирующий
  режим.
- Классический IO — legacy и совместимость; полезно понимать, так как лежит в основе многого.
