package com.kq.mysql.xa;


import com.mysql.cj.jdbc.MysqlXAConnection;
import com.mysql.cj.jdbc.JdbcConnection;
import com.mysql.cj.jdbc.MysqlXid;

import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.Random;
import java.sql.*;

/**
 * @author kq
 * @date 2021-06-01 15:39
 * @since 2020-0630
 */
public class MysqlXaDemo {

    public static void main(String[] args) throws SQLException {

        String url1 = "jdbc:mysql://172.16.5.1:3306/test";
        String url2 = "jdbc:mysql://localhost:3306/test0?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";

        //true表示打印XA语句,，用于调试
        boolean logXaCommands = true;
        // 获得资源管理器操作接口实例 RM1
        Connection conn1 = DriverManager.getConnection
                (url1, "root", "123456");
        XAConnection xaConn1 = new MysqlXAConnection((JdbcConnection) conn1, logXaCommands);
        // MysqlXAConnection
        XAResource rm1 = xaConn1.getXAResource();
        //  MysqlXAConnection extends MysqlPooledConnection implements XAConnection, XAResource
        // 获得资源管理器操作接口实例 RM2
        Connection conn2 = DriverManager.getConnection
                (url2, "root", "123456");
        XAConnection xaConn2 = new MysqlXAConnection((JdbcConnection) conn2, logXaCommands);

        XAResource rm2 = xaConn2.getXAResource();
        // XAConnection和XAResource 都指向MysqlXAConnection 是同一个对象
        // AP请求TM执行一个分布式事务，TM生成全局事务id
        byte[] gtrid = "g12345".getBytes();
        int formatId = 1;
        try {

            int id = new Random().nextInt(100000000);
            String username = "myadmin1";
            System.out.println("generate id is "+id);
            Date date = new Date(System.currentTimeMillis());

            // ==============分别执行RM1和RM2上的事务分支====================
            // TM生成rm1上的事务分支id
            byte[] bqual1 = "b00001".getBytes();
            Xid xid1 = new MysqlXid(gtrid, bqual1, formatId);
            // 执行rm1上的事务分支
            rm1.start(xid1, XAResource.TMNOFLAGS);//One of TMNOFLAGS, TMJOIN, or TMRESUME.
            PreparedStatement ps1 = conn1.prepareStatement(
                    "insert into t_account(id,username,phone,createTime) values(?,?,?,?)");
            ps1.setInt(1,id);
            ps1.setString(2,username);
            ps1.setString(3,"12345678");
            ps1.setDate(4,date);
            ps1.execute();
            rm1.end(xid1, XAResource.TMSUCCESS);

            // TM生成rm2上的事务分支id
            byte[] bqual2 = "b00002".getBytes();
            Xid xid2 = new MysqlXid(gtrid, bqual2, formatId);
            // 执行rm2上的事务分支
            rm2.start(xid2, XAResource.TMNOFLAGS);
            PreparedStatement ps2 = conn2.prepareStatement(
                    "insert into t_account(id,username,phone,createTime) values(?,?,?,?)");
            ps2.setInt(1,id);
            ps2.setString(2,username);
            ps2.setString(3,"1234567890");
//            ps2.setString(3,"12345678901234567890123456789012345678901234567890");
            ps2.setDate(4,date);
            ps2.execute();
            rm2.end(xid2, XAResource.TMSUCCESS);

            // ===================两阶段提交================================
            // phase1：询问所有的RM 准备提交事务分支
            int rm1_prepare = rm1.prepare(xid1);
            int rm2_prepare = rm2.prepare(xid2);
            System.out.println("rm1_prepare="+rm1_prepare+" , rm2_prepare="+rm2_prepare);

            // 故意回滚
            rm2_prepare = 1;

            // phase2：提交所有事务分支
            boolean onePhase = false;
            //TM判断有2个事务分支，所以不能优化为一阶段提交
            if (rm1_prepare == XAResource.XA_OK
                    && rm2_prepare == XAResource.XA_OK) {
                System.out.println("the transaction is commit");
                //所有事务分支都prepare成功，提交所有事务分支
                rm1.commit(xid1, onePhase);  // 这一步成功  rm1会最终生效
                rm2.commit(xid2, onePhase);
            } else {
                System.out.println("the transaction is rollback");
                //如果有事务分支没有成功，则回滚
                rm1.rollback(xid1);
                rm2.rollback(xid2);
            }
        } catch (XAException e) {
            // 如果出现异常，也要进行回滚
            e.printStackTrace();
        }
    }


}
