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

        if (value instanceof String) {
            return this.addColumn(table, column, false).append("= ").appendString((String) value).append(" ");
        } else {
            return this.addColumn(table, column, false).append("= ").append(value).append(" ");
        }
    }

    public QueryBuilder addInnerJoin(String table1, String table2, String column1, String column2) {
        return this.append("inner join ").addTable(table2).append(" ")
                .append("on ").addColumn(table1, column1, false).append("= ").addColumn(table2, column2, false);
    }

    public <T> QueryBuilder addFromFunction(String functionName, T... params) {
        this.addFromTable(functionName).append("(");

        for (int i = 0; i < params.length - 1; ++i) {
            if (params[i] instanceof String) {
                this.appendString((String) params[i]).append(", ");
            } else {
                this.append(params[i]).append(", ");
            }
        }

        if (params[params.length - 1] instanceof String) {
            return this.appendString((String) params[params.length - 1]).append(") ");
        } else {
            return this.append(params[params.length - 1]).append(") ");
        }
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

        if (value instanceof String) {
            return this.append(column).append(" = ").appendString((String) value).append(" ");
        } else {
            return this.append(column).append(" = ").append(value).append(" ");
        }
    }

    public QueryBuilder addFilterSetColumn(String table, String column1, String column2) {
        if (!this.currentQuery.toString().contains("set")) {
            this.append(" set ");
        } else {
            this.append(", ");
        }
        return this.append(column1).append(" = ").addColumn(table, column2, false).append(" ");
    }

    public QueryBuilder appendString(String string) {
        return this.append("'").append(string).append("'");
    }

    public String getQuery() {
        return this.currentQuery.toString();
    }
}
