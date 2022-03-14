package queries;

public class QueryBuilder {
    private final StringBuilder currentQuery;

    public QueryBuilder(String command) {
        currentQuery = new StringBuilder(command).append(" ");
    }

    public <T> QueryBuilder append(T string) {
        currentQuery.append(string);

        return this;
    }

    public <T> QueryBuilder appendData(T data) {
        if (data instanceof String) {
            this.appendString((String) data);
        } else {
            this.append(data);
        }

        return this;
    }

    public QueryBuilder addColumn(String table, String column) {
        return this.addTable(table).append(".\"").append(column).append("\"").append(" ");
    }

    public QueryBuilder addTable(String table) {
        return this.append("public.").append(table);
    }

    public QueryBuilder addFromTable(String table) {
        if (!this.currentQuery.toString().contains("from")) {
            this.append("from ");
        } else {
            this.append(", ");
        }

        return this.addTable(table).append(" ");
    }

    public <T> QueryBuilder addFilterWhere(String table, String column, T value) {
        if (currentQuery.toString().contains("where")) {
            this.append("and ");
        } else {
            this.append("where ");
        }

        return this.addColumn(table, column).append("= ").appendData(value).append(" ");
    }

    public QueryBuilder addFilterWhereTable(String table1, String column1, String table2, String column2) {
        if (currentQuery.toString().contains("where")) {
            this.append("and ");
        } else {
            this.append("where ");
        }

        return this.addColumn(table1, column1).append("= ").addColumn(table2, column2);
    }

    public <T> QueryBuilder addLeftJoin(String table, String column, T data) {
        return this.append("left join ").addTable(table).append(" ")
                .append("on ").addColumn(table, column).append("= ").appendData(data);
    }

    public QueryBuilder addLeftJoinColumn(String table1, String table2, String column1, String column2) {
        return this.append("left join ").addTable(table2).append(" ")
                .append("on ").addColumn(table1, column1).append("= ").addColumn(table2, column2);
    }

    public <T> QueryBuilder addFromFunction(String functionName, T... params) {
        this.addFromTable(functionName).append("(");

        for (int i = 0; i < params.length - 1; ++i) {
            this.appendData(params[i]).append(", ");
        }

        return this.appendData(params[params.length - 1]).append(") ");
    }

    public QueryBuilder all() {
        return this.append("* ");
    }

    public <T> QueryBuilder addFilterSet(String column, T value) {
        if (!this.currentQuery.toString().contains("set")) {
            this.append(" set ");
        } else {
            this.append(", ");
        }

        return this.append(column).append(" = ").appendData(value).append(" ");
    }

    public QueryBuilder addFilterSetColumn(String table, String column1, String column2) {
        if (!this.currentQuery.toString().contains("set")) {
            this.append(" set ");
        } else {
            this.append(", ");
        }
        return this.append(column1).append(" = ").addColumn(table, column2).append(" ");
    }

    public QueryBuilder appendString(String string) {
        return this.append("'").append(string).append("'");
    }

    public QueryBuilder openBrace() {
        return this.append("(");
    }

    public QueryBuilder closeBrace() {
        return this.append(")");
    }

    public QueryBuilder appendComma() {
        return this.append(", ");
    }

    public String getQuery() {
        return this.currentQuery.toString();
    }
}
