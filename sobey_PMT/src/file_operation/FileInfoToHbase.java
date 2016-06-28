package file_operation;
import java.io.IOException;
import java.util.Vector;

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
 * �������ܣ����õ��Ļ��߰汾��ص�������Ϣȫ������hbase���ݿ⣻�������ű�һ��Ϊ�汾��Ϣ��һ��Ϊ�����ļ���
 * 
 * ���ߣ������
 * ʱ�䣺2016/5/12������
 * */
public class FileInfoToHbase {
	private HBaseAdmin admin = null;
	private HBaseConfiguration cfg = null;
	public FileInfoToHbase(String IP) throws Exception {
		Configuration HBASE_CONFIG = new Configuration();
		HBASE_CONFIG.set("hbase.zookeeper.quorum", IP);
		HBASE_CONFIG.set("hbase.zookeeper.property.clientPort", "2181");
		cfg = new HBaseConfiguration(HBASE_CONFIG);
		admin = new HBaseAdmin(cfg);
	}
	public String createTable(String tablename,String tablename1,String columnFamily,String columnFamily1) throws Exception
	{
		HTableDescriptor TD = new HTableDescriptor(tablename);
		HColumnDescriptor HCD = new HColumnDescriptor(columnFamily);
		HTableDescriptor TD1 = new HTableDescriptor(tablename1);
		HColumnDescriptor HCD1 = new HColumnDescriptor(columnFamily1);
		HCD.setMaxVersions(3); //hbase���ݿ�ֻ���������汾����
		HCD.setInMemory(true);
		TD.setDurability(Durability.SYNC_WAL);
		TD.addFamily(HCD);
		HCD1.setMaxVersions(3);
		HCD1.setInMemory(true);
		TD1.setDurability(Durability.SYNC_WAL); //���ȴ����ڴ�
		TD1.addFamily(HCD1);
		if(admin.tableExists(tablename)||admin.tableExists(tablename1))
		{
			return "���Ѵ���";
		}
		admin.createTable(TD);
		admin.createTable(TD1);
		return "���ѽ��������ڲ�������....";
	}
	public int datainput(String tablename,String tablename1,String[] data) throws IOException
	{
		/*
		 * ���ű���Ż��߰汾�İ汾��Ϣ�ͻ��ߴ���ʱ��
		 * */
		HTable table = new HTable(cfg,tablename);
		MD5 hash = new MD5();
		Vector<String> V = new Vector<String>();
		int currentRow =0;
		for(int i =0;i<data.length;i++)
		{
			final String[] values = data[i].split(",");
			String RowKey = String.format("%s_%s",values[6], values[0].substring(3));
			Put put = new Put(Bytes.toBytes(RowKey));
			put.add(Bytes.toBytes("File_Info"),Bytes.toBytes("FileName"),Bytes.toBytes(values[2]));
			put.add(Bytes.toBytes("File_Info"),Bytes.toBytes("FilePath"),Bytes.toBytes(values[3]));
			put.add(Bytes.toBytes("File_Info"),Bytes.toBytes("FileMT"),Bytes.toBytes(values[4]));
			put.add(Bytes.toBytes("File_Info"),Bytes.toBytes("FileVersion"),Bytes.toBytes(values[5]));
			table.put(put);
			if(currentRow!=0)
			{
				if(!V.contains(values[0])&&!values[0].equals("9999"))
					V.add(values[0]);
				if(!V.contains(values[1])&&!values[1].equals("9999"))
					V.add(values[1]);
			}
			currentRow++;
		}
		table.close();
		/*
		 * ���ű���Ż��߰汾�İ汾��Ϣ�ͻ��ߴ���ʱ��
		 * */
		HTable table1 = new HTable(cfg,tablename1);
		for(int i=0;i<V.size();i+=2)
		{
			String Hash = hash.getMD5ofStr(V.get(i).substring(3));
			String RowKey = String.format("%s_%s",Hash,V.get(i).substring(3));
			Put put = new Put(Bytes.toBytes(RowKey));
			put.add(Bytes.toBytes("V_Info"),Bytes.toBytes("Version"),Bytes.toBytes(V.get(i)));
			put.add(Bytes.toBytes("V_Info"),Bytes.toBytes("Vtime"),Bytes.toBytes(V.get(i+1)));
			table1.put(put);
		}
		table1.close();
		return currentRow;
	}  
}
