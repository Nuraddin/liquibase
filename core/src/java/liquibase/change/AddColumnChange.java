package liquibase.change;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.sql.*;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Adds a column to an existing table.
 */
public class AddColumnChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private ColumnConfig column;

    public AddColumnChange() {
        super("addColumn", "Add Column");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ColumnConfig getColumn() {
        return column;
    }

    public void setColumn(ColumnConfig column) {
        this.column = column;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        List<SqlStatement> sql = new ArrayList<SqlStatement>();

        Set<ColumnConstraint> constraints = new HashSet<ColumnConstraint>();

        if (column.getConstraints() != null) {
            if (column.getConstraints().isNullable()!= null && !column.getConstraints().isNullable()) {
                constraints.add(new NotNullConstraint());
            }
        }

        AddColumnStatement addColumnStatement = new AddColumnStatement(getSchemaName(),
                getTableName(),
                getColumn().getName(),
                getColumn().getType(),
                getColumn().getDefaultValueObject(),
                constraints.toArray(new ColumnConstraint[constraints.size()]));

        sql.add(addColumnStatement);
        if (database instanceof DB2Database) {
            sql.add(new ReorganizeTableStatement(getSchemaName(), getTableName()));
        }

        if (getColumn().getConstraints() != null) {
            if (getColumn().getConstraints().isPrimaryKey() != null && getColumn().getConstraints().isPrimaryKey()) {
                AddPrimaryKeyChange change = new AddPrimaryKeyChange();
                change.setSchemaName(getSchemaName());
                change.setTableName(getTableName());
                change.setColumnNames(getColumn().getName());

                sql.addAll(Arrays.asList(change.generateStatements(database)));
            }
        }

        return sql.toArray(new SqlStatement[sql.size()]);
    }

    protected Change[] createInverses() {
        List<Change> inverses = new ArrayList<Change>();

        if (column.hasDefaultValue()) {
            DropDefaultValueChange dropChange = new DropDefaultValueChange();
            dropChange.setTableName(getTableName());
            dropChange.setColumnName(getColumn().getName());

            inverses.add(dropChange);
        }


        DropColumnChange inverse = new DropColumnChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setColumnName(getColumn().getName());
        inverse.setTableName(getTableName());
        inverses.add(inverse);

        return inverses.toArray(new Change[inverses.size()]);
    }

    public String getConfirmationMessage() {
        return "Column " + column.getName() + "(" + column.getType() + ") added to " + tableName;
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("addColumn");
        if (getSchemaName() != null) {
            node.setAttribute("schemaName", getSchemaName());
        }
        node.setAttribute("tableName", getTableName());
        node.appendChild(getColumn().createNode(currentChangeLogFileDOM));

        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Column column = new Column();

        Table table = new Table(getTableName());
        column.setTable(table);

        column.setName(this.column.getName());

        return new HashSet<DatabaseObject>(Arrays.asList(table, column));

    }
}
