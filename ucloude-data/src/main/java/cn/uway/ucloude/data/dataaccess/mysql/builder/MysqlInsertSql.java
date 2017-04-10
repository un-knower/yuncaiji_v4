package cn.uway.ucloude.data.dataaccess.mysql.builder;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.builder.InsertSql;
import cn.uway.ucloude.data.dataaccess.exception.JdbcException;

/**
 * @author magic.s.g.xie
 */
public class MysqlInsertSql extends InsertSql {

	public MysqlInsertSql(SqlTemplate sqlTemplate) {
		super(sqlTemplate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public InsertSql insert(String table) {
		// TODO Auto-generated method stub
		this.sql.append("INSERT INTO ");
        sql.append("`").append(table).append("`");
        return this;
	}

	@Override
	public InsertSql insertIgnore(String table) {
		// TODO Auto-generated method stub
		this.sql.append("INSERT IGNORE INTO ");
        sql.append("`").append(table).append("`");
        return this;
	}

	@Override
	public InsertSql columns(String... columns) {
		// TODO Auto-generated method stub
		if (columns == null || columns.length == 0) {
            throw new JdbcException("columns must have length");
        }
        if (columnsSize > 0) {
            throw new JdbcException("columns already set");
        }

        columnsSize = columns.length;

        sql.append("(");
        String split = "";
        for (String column : columns) {
            sql.append(split);
            split = ", ";
            sql.append("`").append(column.trim()).append("`");
        }
        sql.append(") VALUES ");

        sql.append("(");
        split = "";
        for (int i = 0; i < columnsSize; i++) {
            sql.append(split);
            split = ",";
            sql.append("?");
        }
        sql.append(")");
        return this;
	}

    

}
