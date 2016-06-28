package file_operation;

import java.io.IOException;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;
/*
 * 本程序属于项目版本管理工具的源码
 * 本程序功能：在hbase表中进行各种查询
 * 
 * 作者：何天光
 * 时间：2016/5/12（整理）
 * */
public class PatchSerach {
	private HBaseAdmin admin = null;
	private HBaseConfiguration cfg = null;
	static public String RR = null;
	public PatchSerach(String IP) throws Exception {
		Configuration HBASE_CONFIG = new Configuration();
		HBASE_CONFIG.set("hbase.zookeeper.quorum", IP);
		HBASE_CONFIG.set("hbase.zookeeper.property.clientPort", "2181");
		cfg = new HBaseConfiguration(HBASE_CONFIG);
		admin = new HBaseAdmin(cfg);
	}
	/*
	 * 新版补丁包本身信息的属性查询，如：包名，包创建时间等
	 * */
	public String newPatch_Info_property(String Colunm,String key) throws IOException
	{
		if(admin.tableExists("newPatch_illustrate"))
		{
			String pn = null;
			HTable table  = new HTable(cfg,"newPatch_illustrate");
			String result = "";
			Vector<String> V = new Vector<String>();
			Filter filter = new SingleColumnValueFilter(
					Bytes.toBytes("Patch_Info_property"),
					Bytes.toBytes(Colunm),
					CompareOp.EQUAL,
					new SubstringComparator(key));
			Scan scan = new Scan();
			scan.setMaxVersions(1);  //查询最新版本
			scan.setFilter(filter);
			ResultScanner rs = table.getScanner(scan);
			for (Result r:rs)
			{
				for(Cell cell : r.rawCells())
				{
					V.add(Bytes.toString(CellUtil.cloneQualifier(cell))+ "：" +Bytes.toString(CellUtil.cloneValue(cell)));
					if("P_Name".equals(Bytes.toString(CellUtil.cloneQualifier(cell))))
					{
						pn = Bytes.toString(CellUtil.cloneValue(cell));
					}
				}
				/*
				 * 获取这个补丁包内文件的信息
				 */
				HTable table1  = new HTable(cfg,"newPatchInfo");
				Filter filter1 = new SingleColumnValueFilter(
						Bytes.toBytes("newPatch_Info"),
						Bytes.toBytes("P_Name"),
						CompareOp.EQUAL,
						new SubstringComparator(pn));
				Scan scan1 = new Scan();
				scan1.setMaxVersions(1);
				scan1.setFilter(filter1);
				ResultScanner rs1 = table1.getScanner(scan1);
				V.add("补丁包内的dll文件");
				for (Result r1:rs1)
				{
					for(Cell cell1 : r1.rawCells())
					{
						if("P_Name".equals(Bytes.toString(CellUtil.cloneQualifier(cell1))))
						{}
						else
							V.add(Bytes.toString(CellUtil.cloneQualifier(cell1))+ "：" +Bytes.toString(CellUtil.cloneValue(cell1)));
					}
				}
				table1.close();
				V.add("\r\n");
			}
			if(V.size()==0)
			{
				V.add("没找到！");
			}
			for(int j =0;j<V.size();j++)
			{
				result = result +V.get(j) +"\r\n";
			}
			table.close();
			return result;
		}
		else
			return "没有表！";

	}
	/*
	 * 新版补丁包本身信息的说明信息模糊查询，
	 */
	public String newPatch_Info_describe(String Colunm,String key) throws IOException
	{
		if(!admin.tableExists("newPatch_illustrate"))
			return "没有表！";
		HTable table  = new HTable(cfg,"newPatch_illustrate");
		String result = "";
		Vector<String> V = new Vector<String>();
		Vector<String> V1 = new Vector<String>();
		Filter filter = new SingleColumnValueFilter(
				Bytes.toBytes("Patch_Info_describe"),
				Bytes.toBytes(Colunm),
				CompareOp.EQUAL,
				new SubstringComparator(key));
		Scan scan = new Scan();
		scan.setMaxVersions(1);
		scan.setFilter(filter);
		ResultScanner rs = table.getScanner(scan);
		for (Result r:rs)
		{
			for(Cell cell : r.rawCells())
			{
				V.add(Bytes.toString(CellUtil.cloneQualifier(cell))+ "：" +Bytes.toString(CellUtil.cloneValue(cell)));
				if("P_Name".equals(Bytes.toString(CellUtil.cloneQualifier(cell))))
				{
					HTable table1  = new HTable(cfg,"newPatchInfo");
					Filter filter1 = new SingleColumnValueFilter(
							Bytes.toBytes("newPatch_Info"),
							Bytes.toBytes("P_Name"),
							CompareOp.EQUAL,
							new SubstringComparator(Bytes.toString(CellUtil.cloneValue(cell))));
					Scan scan1 = new Scan();
					scan1.setMaxVersions(1);
					scan1.setFilter(filter1);
					ResultScanner rs1 = table1.getScanner(scan1);
					for (Result r1:rs1)
					{
						for(Cell cell1 : r1.rawCells())
						{
							V.add(Bytes.toString(CellUtil.cloneQualifier(cell1))+ "：" +Bytes.toString(CellUtil.cloneValue(cell1)));
						}

					}

					table1.close();
				}
			}
			V.add("\r\n");
		}
		if(V.size()==0)
		{
			V.add("没找到！");
		}
		for(int j =0;j<V.size();j++)
		{
			result = result +V.get(j) +"\r\n";
		}
		table.close();
		return result;

	}
	/*
	 * 旧版补丁包的txt文件模糊查询
	 */
	public String oldPatch_ILL(String Colunm,String key) throws IOException
	{
		if(!admin.tableExists("Path_illustrate"))
			return "没有表！";
		HTable table  = new HTable(cfg,"Path_illustrate");
		String result = "";
		Vector<String> V = new Vector<String>();
		Filter filter = new SingleColumnValueFilter(
				Bytes.toBytes("Patch_ILL"),
				Bytes.toBytes(Colunm),
				CompareOp.EQUAL,
				new SubstringComparator(key));
		Scan scan = new Scan();
		scan.setMaxVersions(1);
		scan.setFilter(filter);
		ResultScanner rs = table.getScanner(scan);
		for (Result r:rs)
		{
			for(Cell cell : r.rawCells())
			{
				V.add(Bytes.toString(CellUtil.cloneQualifier(cell))+ "：" +Bytes.toString(CellUtil.cloneValue(cell)));
				/*
				 * 查询这个txt所在的补丁包的文件信息
				 */
				HTable table1  = new HTable(cfg,"PatchInfo");
				Filter filter1 = new SingleColumnValueFilter(
						Bytes.toBytes("Patch_Info"),
						Bytes.toBytes("P_Name"),
						CompareOp.EQUAL,
						new SubstringComparator(Bytes.toString(r.getRow())));
				Scan scan1 = new Scan();
				scan1.setMaxVersions(1);
				scan1.setFilter(filter1);
				ResultScanner rs1 = table1.getScanner(scan1);
				for (Result r1:rs1)
				{
					for(Cell cell1 : r1.rawCells())
					{
						V.add(Bytes.toString(CellUtil.cloneQualifier(cell1))+ "：" +Bytes.toString(CellUtil.cloneValue(cell1)));
					}

				}
				V.add("\r\n");
				table1.close();
			}
		}
		if(V.size()==0)
		{
			V.add("没找到！");
		}
		for(int j =0;j<V.size();j++)
		{
			result = result +V.get(j) +"\r\n";
		}
		table.close();
		return result;

	}
	/*
	 * 根据补丁包内的dll文件查询，查询首先在新版补丁包对应的表中进行查询，然后再对旧版补丁对应的表中进行查询。
	 */
	public String dllFile(String Filename) throws IOException
	{
		String result = "";
		Vector<String> V = new Vector<String>();
		Vector<String> V1 = new Vector<String>();
		/*
		 * 新版补丁包文件信息表查询
		 */
		HTable table  = new HTable(cfg,"newPatchInfo");
		Filter filter = new SingleColumnValueFilter(
				Bytes.toBytes("newPatch_Info"),
				Bytes.toBytes("FileName"),
				CompareOp.EQUAL,
				new SubstringComparator(Filename));
		Scan scan = new Scan();
		scan.setMaxVersions(1);
		scan.setFilter(filter);
		ResultScanner rs = table.getScanner(scan);
		for (Result r:rs)
		{
			for(Cell cell : r.rawCells())
			{
				V.add(Bytes.toString(CellUtil.cloneQualifier(cell))+ "：" +Bytes.toString(CellUtil.cloneValue(cell)));
				if("P_Name".equals(Bytes.toString(CellUtil.cloneQualifier(cell))))
				{
					HTable table2  = new HTable(cfg,"newPatch_illustrate");
					Filter filter2 = new SingleColumnValueFilter(
							Bytes.toBytes("Patch_Info_property"),
							Bytes.toBytes("P_Name"),
							CompareOp.EQUAL,
							new SubstringComparator(Bytes.toString(CellUtil.cloneValue(cell))));
					Scan scan2 = new Scan();
					scan2.setMaxVersions(1);
					scan2.setFilter(filter2);
					ResultScanner rs2 = table2.getScanner(scan2);
					for (Result r2:rs2)
					{
						for(Cell cell2 : r2.rawCells())
						{
							V.add(Bytes.toString(CellUtil.cloneQualifier(cell2))+ "：" +Bytes.toString(CellUtil.cloneValue(cell2)));
						}
					}
					V.add("\r\n");
					table2.close();
				}
			}
		}
		table.close();
		/*
		 * 旧版版补丁包文件信息表查询
		 */
		if(!admin.tableExists("PatchInfo"))
			V1.add("没有新表！");
		else
		{
			HTable table1  = new HTable(cfg,"PatchInfo");
			Filter filter1 = new SingleColumnValueFilter(
					Bytes.toBytes("Patch_Info"),
					Bytes.toBytes("FileName"),
					CompareOp.EQUAL,
					new SubstringComparator(Filename));
			Scan scan1 = new Scan();
			scan1.setMaxVersions(1);
			scan1.setFilter(filter1);
			ResultScanner rs1 = table1.getScanner(scan1);
			for (Result r:rs1)
			{
				for(Cell cell : r.rawCells())
				{
					V1.add(Bytes.toString(CellUtil.cloneQualifier(cell))+ "：" +Bytes.toString(CellUtil.cloneValue(cell)));
					if("P_Name".equals(Bytes.toString(CellUtil.cloneQualifier(cell))))
					{
						HTable table3  = new HTable(cfg,"Path_illustrate");
						Filter filter3 = new RowFilter(CompareFilter.CompareOp.EQUAL ,new SubstringComparator(Bytes.toString(CellUtil.cloneValue(cell))));
						Scan scan3 = new Scan();
						scan3.setMaxVersions(1);
						scan3.setFilter(filter3);
						ResultScanner rs3 = table3.getScanner(scan3);
						for (Result r3:rs3)
						{
							for(Cell cell3 : r3.rawCells())
							{
								V1.add(Bytes.toString(CellUtil.cloneQualifier(cell3))+ "：" +Bytes.toString(CellUtil.cloneValue(cell3)));
							}
						}
						V1.add("\r\n");
						table3.close();
					}
				}
			}
			table1.close();
		}

		if(V1.size()==0)
		{
			V1.add("没找到！");
		}

		if(V.size()==0)
		{
			V.add("没找到！");
		}
		V.add("旧版本的补丁包的信息提取：");
		for(int b = 0;b<V1.size();b++)
		{
			V.add(V1.get(b));
		}

		for(int j =0;j<V.size();j++)
		{
			result = result +V.get(j) +"\r\n";
		}
		return result;
	}

	public static void main(String[] args) throws Exception
	{
		//		PatchSerach ps = new PatchSerach("172.16.133.18");
		//		String a = ps.oldPatch_ILL("document", "v5.3.1");
		//		System.out.println(a);

	}
}
