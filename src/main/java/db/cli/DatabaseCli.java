package db.cli;

import db.engine.DatabaseEngine;
import db.engine.QueryResult;
import db.parser.ParseException;
import db.parser.SqlParser;
import db.parser.command.Command;

import java.util.Scanner;

public class DatabaseCli {
    private final DatabaseEngine engine;
    private final SqlParser parser = new SqlParser();

    public DatabaseCli(DatabaseEngine engine) {
        this.engine = engine;
    }

    public void run() {
        System.out.println("MiniDatabaseEngine");
        System.out.println("Type help for commands. SQL statements end with semicolon.");

        Scanner scanner = new Scanner(System.in);
        StringBuilder buffer = new StringBuilder();

        while (true) {
            System.out.print(buffer.length() == 0 ? "db> " : "... ");
            if (!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) {
                break;
            }
            if (line.equalsIgnoreCase("help")) {
                printHelp();
                continue;
            }
            if (line.equalsIgnoreCase("tables")) {
                System.out.println(engine.listTables());
                continue;
            }
            if (line.toLowerCase().startsWith("describe ")) {
                System.out.println(engine.describe(line.substring("describe ".length()).trim()));
                continue;
            }

            buffer.append(line).append(' ');
            if (!line.endsWith(";")) {
                continue;
            }

            String sql = buffer.toString();
            buffer.setLength(0);

            try {
                Command command = parser.parse(sql);
                QueryResult result = engine.execute(command);
                System.out.println(result);
            } catch (ParseException | IllegalArgumentException ex) {
                System.out.println("error: " + ex.getMessage());
            } catch (RuntimeException ex) {
                System.out.println("error: " + ex.getMessage());
            }
        }
    }

    private void printHelp() {
        System.out.println("CREATE TABLE users (id INT, name STRING, active BOOLEAN);");
        System.out.println("INSERT INTO users VALUES (1, \"Ido\", true);");
        System.out.println("SELECT * FROM users WHERE id = 1;");
        System.out.println("UPDATE users SET active = false WHERE id = 1;");
        System.out.println("DELETE FROM users WHERE id = 1;");
        System.out.println("tables");
        System.out.println("describe tableName");
        System.out.println("exit");
    }
}
