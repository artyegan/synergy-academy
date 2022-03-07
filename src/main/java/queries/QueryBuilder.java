package queries;

public class QueryBuilder {
    private final StringBuilder currentQuery;

    public QueryBuilder(String command) {
        currentQuery = new StringBuilder(command).append(" ");
    }

    public QueryBuilder append(String string) {
        currentQuery.append(string);

        return this;
    }

    public QueryBuilder addColumn(String table, String column, boolean addComma) {
        this.addTable(table).append(".\"").append(column).append("\"");

        if (addComma) {
            return this.append(", ");
        } else {
            return this.append(" ");
        }
    }

    public QueryBuilder addTable(String table) {
        return this.append("public.").append(table);
    }

    public QueryBuilder addFromTable(String table) {
        return this.append("from ").addTable(table).append(" ");
    }

    public QueryBuilder addFilterWhere(String table, String column, String value) {
        if (currentQuery.toString().contains("where")) {
            this.append("and ");
        } else {
            this.append("where ");
        }
        return this.addColumn(table, column, false).append("= ").append(value).append(" ");
    }

    public QueryBuilder addInnerJoin(String table1, String table2, String column1, String column2) {
        return this.append("inner join ").addTable(table2).append(" ")
                .append("on ").addColumn(table1, column1, false).append("= ").addColumn(table2, column2, false);
    }

    public QueryBuilder addFromFunction(String functionName, String... params) {
        this.addFromTable(functionName).append("(");

        for (int i = 0; i < params.length - 1; ++i) {
            this.append(params[i]).append(", ");
        }
        return this.append(params[params.length - 1]).append(") ");
    }

    public String getQuery() {
        return this.currentQuery.toString();
    }
}
