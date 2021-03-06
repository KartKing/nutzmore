package org.nutz.plugins.sqltpl.impl.beetl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nutz.dao.SqlManager;
import org.nutz.dao.Sqls;
import org.nutz.dao.impl.FileSqlManager;
import org.nutz.dao.impl.sql.NutSql;
import org.nutz.dao.sql.Sql;

public class BeetlSqlTplTest {
    
    @Before
    public void before(){
        Sqls.setSqlBorning(BeetlSqlTpl.class);
    }
    
    @After
    public void after(){
        Sqls.setSqlBorning(NutSql.class);
    }
    
    @Test
    public void testC() {
        String source = "select * from t_user where 1=1 <%if (has('id')) {%>and id > @id<%}%>";
        
        Sql sql = Sqls.create(source);
        sql.params().set("id", 1000); // 带指定参数,那么对应的and子句应该出现
        
        assertEquals("select * from t_user where 1=1 and id > ?", sql.toPreparedStatement());
        
        sql = Sqls.create(source);
        //sql.params().set("id", 1000); // 不带指定参数,所以没有and子句
        assertEquals("select * from t_user where 1=1", sql.toPreparedStatement());
    }

    @Test
    public void testC_2() {
        String source = "select * from t_user <% if(params.~size>0) {%>where 1=1 <%if (has('id')) {%>and id > @id<%}%><%}%>";
        
        Sql sql = Sqls.create(source);
        //sql.params().set("id", 1000); // 不添加任何参数,那么where语句不应该出现
        assertEquals("select * from t_user", sql.toPreparedStatement());
    }
    
    @Test
    public void test_c3() {
        SqlManager sqlm = new FileSqlManager("org/nutz/plugins/sqltpl/impl/beetl/sqls");
        assertTrue(sqlm.count() > 0);
        Sql sql = sqlm.create("user.fetch");
        
        // 首先测试没有传任何参数
        assertEquals("select * from t_user", sql.toPreparedStatement().trim());
        
        // 带name和passwd参数
        sql = sqlm.create("user.fetch");
        sql.params().set("name", "wendal");
        sql.params().set("passwd", "123456");
        String dst = sql.toPreparedStatement().replaceAll("[ \\t\\n\\r]", "");
        assertEquals("select * from t_user where name = ? and passwd = ?".replaceAll(" ", ""), dst);
        
        // 带token参数
        sql = sqlm.create("user.fetch");
        sql.params().set("token", "_123456");
        dst = sql.toPreparedStatement().replace('\t', ' ').replace('\r', ' ').replace('\n', ' ').replaceAll(" ", "").trim();
        assertEquals("select * from t_user where token = ?".replaceAll(" ", ""), dst);
    }
    
    /**
     * 测试layout功能
     */
    @Test
    public void test_issue_70() {
        SqlManager sqlm = new FileSqlManager("org/nutz/plugins/sqltpl/impl/beetl/sqls");
        assertTrue(sqlm.count() > 0);
        Sql sql = sqlm.create("user.select");
        
        // 首先测试没有传任何参数
        System.out.println(sql.toPreparedStatement());
        assertEquals("select age from t_user where name = \"wendal\" order by age asc", sql.toPreparedStatement().replace("\r", "").replace("\n", ""));
    }
}
