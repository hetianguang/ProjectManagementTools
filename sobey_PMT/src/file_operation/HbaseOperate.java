package file_operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
/*
 * ������������Ŀ�汾�����ߵ�Դ��
 * �������ܣ���hbase����в���
 * 
 * ���ߣ������
 * ʱ�䣺2016/5/12������
 * */
public class HbaseOperate {
	private HBaseAdmin admin = null;
	private HBaseConfiguration cfg = null;
	public HbaseOperate(String IP) throws Exception {
		Configuration HBASE_CONFIG = new Configuration();
		HBASE_CONFIG.set("hbase.zookeeper.quorum", IP);
		HBASE_CONFIG.set("hbase.zookeeper.property.clientPort", "2181");
		cfg = new HBaseConfiguration(HBASE_CONFIG);
		admin = new HBaseAdmin(cfg);
	}
	/*
	 * Hbase��ȡ���еı���Ϣ
	 * */ 
	public List getAllTables() {
		List<String> tables = null;
		if (admin != null) {
			try {
				HTableDescriptor[] allTable = admin.listTables();
				if (allTable.length > 0)
					tables = new ArrayList<String>();
				for (HTableDescriptor hTableDescriptor : allTable) {
					tables.add(hTableDescriptor.getNameAsString());
					System.out.println(hTableDescriptor.getNameAsString());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			tables.add("û�б�");
		}
		return tables;
	}
	/*
	 * ɾ�����б�
	 * */
	public String getDelAllTables()
	{
		if (admin != null) {
			try {
				HTableDescriptor[] allTable = admin.listTables();
				if (allTable.length > 0)
					for (HTableDescriptor hTableDescriptor : allTable)
					{
						admin.disableTable(hTableDescriptor.getNameAsString());
						admin.deleteTable(hTableDescriptor.getNameAsString());
					}
				return "ȫ��ɾ����";
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else{
			return "û��ɾ���ı�";
		}
		return "";
	}
	/*
	 * ��ʾ�������ݣ�ͨ��HTable Scan���ȡ���б����Ϣ
	 * */ 
	public ArrayList<String> getAllData(String tableName) throws Exception {
		ArrayList<String> Value = new ArrayList<String>();
		HTable table = new HTable(cfg, tableName);
		String[] value = null;
		Scan scan = new Scan();
		ResultScanner rs = table.getScanner(scan);
		for (Result r : rs) {
			for(Cell cell : r.rawCells())
			{
				Value.add(Bytes.toString(CellUtil.cloneValue(cell)));  //��÷���������value��ֵ������������� 
			}
		}
		return Value;
	}

	/*
	 * Hbase�б��ɾ��
	 * */ 
	public String deleteTable(String table) {
		try {
			if (admin.tableExists(table)) {
				admin.disableTable(table);
				admin.deleteTable(table);
			}
			else{
				return "�����ڱ�����"+ table;
			}

		} catch (IOException e) {
		}
		return table;
	}
	/*
	 * Hbase���²������ڵĲ���������
	 * */ 
	public String newPatchCount() throws IOException
	{
		int cnt = 0;
		String result = "";
		if(admin.tableExists("newPatch_illustrate"))
		{
			HTable table  = new HTable(cfg,"newPatch_illustrate");
			Scan scan = new Scan();
			scan.setMaxVersions(1);  //��ѯ���°汾
			ResultScanner rs = table.getScanner(scan);
			for (Result r:rs)
			{
				for(Cell cell : r.rawCells())
				{
					if("P_Name".equals(Bytes.toString(CellUtil.cloneQualifier(cell))))
					{
						result = result + Bytes.toString(CellUtil.cloneValue(cell))+"\r\n";
						cnt++;
					}
				}
			}
			result = result+ String.valueOf(cnt)+"\r\n";
			return result;
		}
		return "û�д˱�";

	}
	/*
	 * Hbase���²������ڵĲ���������
	 * */ 
	public String oldPatchCount() throws IOException
	{
		int cnt = 0;
		String result = "";
		if(admin.tableExists("Path_illustrate"))
		{
			HTable table  = new HTable(cfg,"Path_illustrate");
			Scan scan = new Scan();
			scan.setMaxVersions(1);  //��ѯ���°汾
			ResultScanner rs = table.getScanner(scan);
			for (Result r:rs)
			{
						cnt++;
			}
			result = result+ String.valueOf(cnt)+"\r\n";
			return result;
		}
		return "û�д˱�";

	}
	/*
	 * ���Ժ���
	 * */ 
	public static void main(String[] args) {
		try {
			//HbaseTest hbase = new HbaseTest("172.16.133.18");
			//hbase.createTable("student", "fam1");
			//hbase.getAllTables();

			// hbase.addOneRecord("student","id1","fam1","name","Jack".getBytes());
			// hbase.addOneRecord("student","id1","fam1","address","HZ".getBytes());
			// hbase.getValueFromKey("student","id1");
			//			ArrayList<String> result = hbase.getAllData("PatchInfo");
			//			 for(int i = 0;i<result.size();i++)
			//				 System.out.println(result.get(i));

			//hbase.deleteRecord("student", "id1");

			//			hbase.deleteTable("newPatch_illustrate");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}