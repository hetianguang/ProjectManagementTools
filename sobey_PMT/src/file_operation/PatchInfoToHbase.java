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
 * ������������Ŀ�汾�����ߵ�Դ��
 * �������ܣ����õ��ľɰ油������ص�������Ϣȫ������hbase���ݿ⣻�������ű�һ��Ϊ�����ļ���Ϣ��һ��Ϊ��������txt�ļ����ݱ�
 * 
 * ���ߣ������
 * ʱ�䣺2016/5/12������
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
			return "���Ѵ���";
		}
		admin.createTable(TD);
		admin.createTable(TD1);
		return "���ѽ��������ڲ�������....";
	}
	public int datainput(String tablename,String tablename1,String[] data) throws IOException
	{
		/*
		 * �����ļ���Ϣ����
		 * */
		HTable table = new HTable(cfg,tablename);
		MD5 hash = new MD5();
		int cnt =0;
		int currentRow =0;
		for(int i =0;i<data.length;i++)
		{
			if("txt".equals(data[i]))  //�����ļ���Ϣ�Ͳ���˵����Ϣ
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
		 * ������txt�ļ���Ϣ����
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
