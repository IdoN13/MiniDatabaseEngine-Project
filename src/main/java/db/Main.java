package db;

import db.cli.DatabaseCli;
import db.engine.DatabaseEngine;
import db.storage.FileStorage;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Path dataDir = args.length > 0 ? Path.of(args[0]) : Path.of("data");
        DatabaseEngine engine = new DatabaseEngine(new FileStorage(dataDir));
        new DatabaseCli(engine).run();
    }
}
