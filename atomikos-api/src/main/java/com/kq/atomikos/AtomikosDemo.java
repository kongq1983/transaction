package com.kq.atomikos;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.jdbc.AtomikosDataSourceBean;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.sql.*;
import java.util.Properties;
import java.util.Random;

/**
 * @author kq
 * @date 2021-06-01 16:59
 * @since 2020-0630
 */
public class AtomikosDemo {

   static final String url1 = "jdbc:mysql://172.16.5.1:3306/test";
    static final String url2 = "jdbc:mysql://localhost:3306/test0?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
    private static int index = 0;

    private static AtomikosDataSourceBean createAtomikosDataSourceBean(String url) {
        String dbName = "test";
        // 连接池基本属性
        Properties p = new Properties();
        p.setProperty("url",url);
        p.setProperty("user", "root");
        p.setProperty("password", "123456");

        // 使用AtomikosDataSourceBean封装com.mysql.cj.jdbc.MysqlXADataSource
        AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
        //设置resourceName 唯一
        ds.setUniqueResourceName(dbName+(index++));
        ds.setXaDataSourceClassName("com.mysql.cj.jdbc.MysqlXADataSource");
        ds.setXaProperties(p);
        return ds;
    }

    public static void main(String[] args) {

        AtomikosDataSourceBean ds1 = createAtomikosDataSourceBean(url1);
        AtomikosDataSourceBean ds2 = createAtomikosDataSourceBean(url2);

        Connection conn1 = null;
        Connection conn2 = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        UserTransaction userTransaction = new UserTransactionImp();
        try {
            // 开启事务
            userTransaction.begin();

            // 执行db1上的sql
            conn1 = ds1.getConnection();

            int id = new Random().nextInt(100000000);
            String username = "atomikos-myadmin-1";
            System.out.println("generate id is "+id);
            Date date = new Date(System.currentTimeMillis());

            ps1 = conn1.prepareStatement(
                    "insert into t_account(id,username,phone,createTime) values(?,?,?,?)");
            ps1.setInt(1,id);
            ps1.setString(2,username);
            ps1.setString(3,"12345678");
            ps1.setDate(4,date);
            ps1.execute();


            // 模拟异常 ，直接进入catch代码块，2个都不会提交
            //int i=1/0;

            // 执行db2上的sql
            conn2 = ds2.getConnection();
            ps2 = conn2.prepareStatement(
                    "insert into t_account(id,username,phone,createTime) values(?,?,?,?)");
            ps2.setInt(1,id);
            ps2.setString(2,username);
//            ps2.setString(3,"1234567890");
            ps2.setString(3,"12345678901234567890123456789012345678901234567890");
            ps2.setDate(4,date);
            ps2.execute();

            // 两阶段提交
            userTransaction.commit();
        } catch (Exception e) {
            try {
                e.printStackTrace();
                userTransaction.rollback();
            } catch (SystemException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                ps1.close();
                ps2.close();
                conn1.close();
                conn2.close();
                ds1.close();
                ds2.close();
            } catch (Exception ignore) {
            }
        }
    }

}
