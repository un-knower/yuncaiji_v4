package cn.uway.ucloude.data.dataaccess.oracle.builder;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.builder.InsertSql;
import cn.uway.ucloude.data.dataaccess.exception.JdbcException;

public class OInsertSql extends InsertSql {

	public OInsertSql(SqlTemplate sqlTemplate) {
		super(sqlTemplate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public InsertSql insert(String table) {
		// TODO Auto-generated method stub
        this.sql.append("INSERT INTO ");
        this.sql.append(table);
        return this;
	}

	@Override
	public InsertSql insertIgnore(String table) {
		// TODO Auto-generated method stub
		this.sql.append("INSERT  INTO "); // INSERT IGNORE INTO
        this.sql.append(table);
        return this;
	}

	@Override
	public InsertSql columns(String... columns) {
		// TODO Auto-generated method stub
		if(columns != null && columns.length != 0) {
            if(this.columnsSize > 0) {
                throw new JdbcException("columns already set");
            } else {
                this.columnsSize = columns.length;
                this.sql.append("(");
                String split = "";
                String[] i = columns;
                int len$ = columns.length;

                for(int i$ = 0; i$ < len$; ++i$) {
                    String column = i[i$];
                    this.sql.append(split);
                    split = ", ";
                    this.sql/*.append("`")*/.append(column.trim())/*.append("`")*/;
                }

                this.sql.append(") VALUES ");
                this.sql.append("(");
                split = "";

                for(int var7 = 0; var7 < this.columnsSize; ++var7) {
                    this.sql.append(split);
                    split = ",";
                    this.sql.append("?");
                }

                this.sql.append(")");
                 return this;
            }
        } else {
            throw new JdbcException("columns must have length");
        }
	}

}
