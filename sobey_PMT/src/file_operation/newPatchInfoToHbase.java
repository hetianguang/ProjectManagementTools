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
 * �������ܣ����õ����°油������ص�������Ϣȫ������hbase���ݿ⣻�������ű�һ��Ϊ�����ļ���Ϣ��һ��Ϊ���������Ժ�˵����Ϣ���˱�����������
 * 
 * ���ߣ������
 * ʱ�䣺2016/5/12������
 * */
public class newPatchInfoToHbase {
	private HBaseAdmin admin = null;
	private HBaseConfiguration cfg = null;
	public newPatchInfoToHbase(String IP) throws Exception {
		Configuration HBASE_CONFIG = new Configuration();
		HBASE_CONFIG.set("hbase.zookeeper.quorum", IP);
		HBASE_CONFIG.set("hbase.zookeeper.property.clientPort", "2181");
		cfg = new HBaseConfiguration(HBASE_CONFIG);
		admin = new HBaseAdmin(cfg);
	}
	public String createTable(String tablename,String columnFamily,String tablename1,String columnFamily1,String columnFamily2) throws Exception
	{
		HTableDescriptor TD = new HTableDescriptor(tablename);
		HColumnDescriptor HCD = new HColumnDescriptor(columnFamily);
		HCD.setMaxVersions(3); //hbase����汾��
		HCD.setInMemory(true);
		TD.setDurability(Durability.SYNC_WAL);
		TD.addFamily(HCD);
		HTableDescriptor TD1 = new HTableDescriptor(tablename1);
		HColumnDescriptor HCD1 = new HColumnDescriptor(columnFamily1);
		HCD1.setMaxVersions(3);//hbase����汾��
		HCD1.setInMemory(true);
		HColumnDescriptor HCD2 = new HColumnDescriptor(columnFamily2);
		HCD2.setMaxVersions(3);//hbase����汾��
		HCD2.setInMemory(true);
		TD1.setDurability(Durability.SYNC_WAL);
		TD1.addFamily(HCD1);
		TD1.addFamily(HCD2);
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
		 * �����ļ���Ϣ����
		 * */
		HTable table = new HTable(cfg,tablename);
		MD5 hash = new MD5();
		int cnt = 0;
		int currentRow =0;
		for(int i =0;i<data.length;i++)
		{
			if("xml".equals(data[i]))  //���ڴ��������ַ��������д��ڲ������ļ���Ϣ��xml�ļ��������Ϣ��������Ҫ�ֿ�����hbase���ݿ�
			{
				cnt = i;
				break;
			}
			final String[] values = data[i].split(",");
			String RowKey = String.format("%s_%s",values[5], values[4]);
			Put put = new Put(Bytes.toBytes(RowKey));
			put.add(Bytes.toBytes("newPatch_Info"),Bytes.toBytes("P_Name"),Bytes.toBytes(values[0]));
			put.add(Bytes.toBytes("newPatch_Info"),Bytes.toBytes("FileName"),Bytes.toBytes(values[1]));
			put.add(Bytes.toBytes("newPatch_Info"),Bytes.toBytes("FilePath"),Bytes.toBytes(values[2]));
			put.add(Bytes.toBytes("newPatch_Info"),Bytes.toBytes("FileMT"),Bytes.toBytes(values[3]));
			put.add(Bytes.toBytes("newPatch_Info"),Bytes.toBytes("FileVersion"),Bytes.toBytes(values[4]));
			table.put(put);
			currentRow++;
		}
		table.close();
		/*
		 * ��������Ϣ����
		 * */
		HTable table1 = new HTable(cfg,tablename1);
		for(int j =cnt+1;j<data.length;j++)
		{
			final String[] values = data[j].split(",");
			String  RowKey = hash.getMD5ofStr(values[1]) ;
			Put put = new Put(Bytes.toBytes(RowKey));
			put.add(Bytes.toBytes("Patch_Info_property"),Bytes.toBytes("P_Kind"),Bytes.toBytes(values[0]));
			put.add(Bytes.toBytes("Patch_Info_property"),Bytes.toBytes("P_Name"),Bytes.toBytes(values[1]));
			put.add(Bytes.toBytes("Patch_Info_property"),Bytes.toBytes("P_Time"),Bytes.toBytes(values[2]));
			put.add(Bytes.toBytes("Patch_Info_property"),Bytes.toBytes("BUG_kind"),Bytes.toBytes(values[3]));
			put.add(Bytes.toBytes("Patch_Info_property"),Bytes.toBytes("BUG_ID"),Bytes.toBytes(values[4]));
			put.add(Bytes.toBytes("Patch_Info_property"),Bytes.toBytes("SDK_Version"),Bytes.toBytes(values[5]));
			put.add(Bytes.toBytes("Patch_Info_property"),Bytes.toBytes("APP_Version"),Bytes.toBytes(values[6]));
			put.add(Bytes.toBytes("Patch_Info_property"),Bytes.toBytes("Provider"),Bytes.toBytes(values[7]));
			put.add(Bytes.toBytes("Patch_Info_property"),Bytes.toBytes("Recipient"),Bytes.toBytes(values[8]));
			put.add(Bytes.toBytes("Patch_Info_property"),Bytes.toBytes("Source"),Bytes.toBytes(values[9]));
			put.add(Bytes.toBytes("Patch_Info_describe"),Bytes.toBytes("Illustrate"),Bytes.toBytes(values[10]));
			table1.put(put);
			currentRow++;
		}
		//System.out.println("���������ϣ�һ����"+ currentRow +"��");
		table1.close();
		return currentRow;
	}  
	public static void main(String[] args) throws Exception
	{
		//  newPatchInfoToHbase newpith = new newPatchInfoToHbase();
		// String Path = "etc/2.txt";
		// newpith.createTable("newPatchInfo","newPatch_Info","newPatch_illustrate","Patch_Info_property","Patch_Info_describe");
		//newpith.datainput("newPatchInfo","newPatch_illustrate",data);
	}
}
