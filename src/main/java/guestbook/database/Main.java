package guestbook.database;

import static guestbook.database.generated.tables.Posts.POSTS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class Main {

	public static void main(String[] args) {
		Connection conn = null;

		String username = "root";
		String password = "";
		String url = "jdbc:mysql://localhost:3306/guestbook";

		// prepare the connection to db
		try {
			// initialize driver for connecting to database
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(url, username, password);
			
			// let's add a simple query constructed with jOOQ's query DSL:
			DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
			Result<Record> result = create.select().from(POSTS).fetch();
			
			for( Record record : result ) {
				Long id = record.getValue(POSTS.ID);
				String title = record.getValue(POSTS.TITLE);
				String description = record.getValue(POSTS.BODY);
				System.out.println( "ID: "+ id 
								+"\n Title: "+ title
								+"\n Description: "+ description );
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

}
