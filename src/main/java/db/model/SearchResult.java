package db.model;

import java.util.List;

public record SearchResult(List<Row> rows, boolean usedIndex) {
}
