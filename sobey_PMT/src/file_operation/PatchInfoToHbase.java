package file_operation;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import beartool.MD5;

/*
 * 本程序属于项目版本管理工具的源码
 * 本程序功能：将得到的旧版补丁包相关的所有信息全部导入hbase数据库；生成两张表，一张为补丁文件信息；一张为补丁包内txt文件内容表
 * 
 * 作者：何天光
 * 时间：2016/5/12（整理）
 * */
public class PatchInfoToHbase {
	private HBaseAdmin admin = null;
	private HBaseConfiguration cfg = null;
	public PatchInfoToHbase(String IP) throws Exception {
		Configuration HBASE_CONFIG = new Configuration();
		HBASE_CONFIG.set("hbase.zookeeper.quorum", IP);
		HBASE_CONFIG.set("hbase.zookeeper.property.clientPort", "2181");
		cfg = new HBaseConfiguration(HBASE_CONFIG);
		admin = new HBaseAdmin(cfg);
	}
	public String createTable(String tablename,String columnFamily,String tablename1,String columnFamily1) throws Exception
	{
		HTableDescriptor TD = new HTableDescriptor(tablename);
		HColumnDescriptor HCD = new HColumnDescriptor(columnFamily);
		HCD.setMaxVersions(3);
		HCD.setInMemory(true);
		TD.setDurability(Durability.SYNC_WAL);
		TD.addFamily(HCD);
		HTableDescriptor TD1 = new HTableDescriptor(tablename1);
		HColumnDescriptor HCD1 = new HColumnDescriptor(columnFamily1);
		HCD1.setMaxVersions(3);
		HCD1.setInMemory(true);
		TD1.setDurability(Durability.SYNC_WAL);
		TD1.addFamily(HCD1);
		if(admin.tableExists(tablename)||admin.tableExists(tablename1))
		{
			System.out.println("Table Exists");
			return "表已存在";
		}
		admin.createTable(TD);
		admin.createTable(TD1);
		return "表已建立，正在插入数据....";
	}
	public int datainput(String tablename,String tablename1,String[] data) throws IOException
	{
		/*
		 * 补丁文件信息灌入
		 * */
		HTable table = new HTable(cfg,tablename);
		MD5 hash = new MD5();
		int cnt =0;
		int currentRow =0;
		for(int i =0;i<data.length;i++)
		{
			if("txt".equals(data[i]))  //区分文件信息和补丁说明信息
			{
				cnt = i;
				break;
			}
			final String[] values = data[i].split(",");
			String RowKey = String.format("%s_%s",values[6], values[5]);
			Put put = new Put(Bytes.toBytes(RowKey));
			put.add(Bytes.toBytes("Patch_Info"),Bytes.toBytes("P_Kind"),Bytes.toBytes(values[0]));
			put.add(Bytes.toBytes("Patch_Info"),Bytes.toBytes("P_Name"),Bytes.toBytes(values[1]));
			put.add(Bytes.toBytes("Patch_Info"),Bytes.toBytes("FileName"),Bytes.toBytes(values[2]));
			put.add(Bytes.toBytes("Patch_Info"),Bytes.toBytes("FilePath"),Bytes.toBytes(values[3]));
			put.add(Bytes.toBytes("Patch_Info"),Bytes.toBytes("FileMT"),Bytes.toBytes(values[4]));
			put.add(Bytes.toBytes("Patch_Info"),Bytes.toBytes("FileVersion"),Bytes.toBytes(values[5]));
			table.put(put);
			currentRow++;
		}
		table.close();
		/*
		 * 补丁包txt文件信息灌入
		 * */
		HTable table1 = new HTable(cfg,tablename1);
		for(int j =cnt+1;j<data.length;j++)
		{
			final String[] values = data[j].split(",");
			String RowKey = values[0];
			Put put = new Put(Bytes.toBytes(RowKey));
			put.add(Bytes.toBytes("Patch_ILL"),Bytes.toBytes("document"),Bytes.toBytes(values[1]));
			table1.put(put);
			currentRow++;
		}
		table1.close();
		return currentRow;
	}  
	public static void main(String[] args) throws Exception
	{
		//	    	  PatchInfoToHbase pith = new PatchInfoToHbase("172.16.133.18");
		//	    	  String Path = "etc/P_info.txt";
		//	    	  pith.createTable("FileInfo","VersionInfo","VersionInfo1","Patch_ILL");
		// pith.datainput("PatchInfo","Patch_Info",Path);
	}
}
