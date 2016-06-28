package file_operation;
import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import beartool.MD5;

/*
 * 本程序属于项目版本管理工具的源码
 * 本程序功能：将未知的基线版本跟hbase数据库中版本进行识别
 * 
 * 作者：何天光
 * 时间：2016/5/12（整理）
 * */
public class filecheck {
	private HBaseAdmin admin = null;
	// 定义配置对象HBaseConfiguration
	private HBaseConfiguration cfg = null;
	static public String RR = "";   //将需要输出的数据全部加到这个字符串中

	public filecheck(String IP) throws Exception {
		Configuration HBASE_CONFIG = new Configuration();

		HBASE_CONFIG.set("hbase.zookeeper.quorum", IP); //hbase服务器IP地址
		HBASE_CONFIG.set("hbase.zookeeper.property.clientPort", "2181");  //zookeeper端口号
		cfg = new HBaseConfiguration(HBASE_CONFIG);
		admin = new HBaseAdmin(cfg);
	}
	public static String[] readdata(String[] data) throws IOException
	{
		String[] value = null;
		Vector<String> V = new Vector<String>();
		for(int i = 0;i<data.length;i++)
		{
			final String[] values = data[i].split(",");
			if(values[3].matches(".*x64.*"))  //区分64位和32位的dllwe
				V.add(values[2]+"_64");
			else if(!V.contains(values[3])&&V.contains(values[2]))
			{
				V.add(values[2]+"*");
			}
			else
			{
				V.add(values[2]);
			}
			V.add(values[5]);
			V.add(values[3]);  //只需要文件名，文件版本号，文件路径 参与对比计算
		}
		value = (String[])V.toArray(new String[0]);
		return value;
	}
	/*
	 * 根据行建查询得到一个基线版本号内所有文件的全部信息参与对比计算，输入参数 表名，和需要查询的行键特征
	 * */
	public String[] RowKeyCheck(String tablename,String row) throws IOException
	{
		HTable table = new HTable(cfg,tablename);
		String[] Value = null;
		Vector<String> V = new Vector<String>();
		FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);
		SingleColumnValueFilter filter1 = new SingleColumnValueFilter(Bytes.toBytes("File_Info"),Bytes.toBytes("FileVersion"),
				CompareOp.NOT_EQUAL,Bytes.toBytes("9999"));////没有版本号的不输出
		RowFilter filter = new RowFilter(CompareFilter.CompareOp.EQUAL,new RegexStringComparator(row));
		filters.addFilter(filter);
		filters.addFilter(filter1);
		Scan scan = new Scan();
		scan.setMaxVersions(1);
		scan.setFilter(filters);
		ResultScanner rs = table.getScanner(scan);
		for(Result r : rs)
		{
			int i =0;
			for(Cell cell : r.rawCells())
			{
				if(i==1||i==3||i==2)  //只需要文件名，文件版本号，文件路径  
				{
					V.add(Bytes.toString(CellUtil.cloneValue(cell)));  //获得返回数据中value的值，方便后续处理 
				}
				i++;
			}
		}
		table.close();
		Value = (String[])V.toArray(new String[0]);
		return Value;
	}
	/*
	 * 得到数据库中基线版本的个数以及版本号和创建时间，
	 * */
	public String[] V_RowKeyCheck(String tablename) throws IOException
	{
		///////////////////////////////////////////////////////////////
		HTable table = new HTable(cfg,tablename);
		String[] VN = null;
		Vector<String> V = new Vector<String>();
		Scan scan = new Scan();
		scan.setMaxVersions(1); //只获取最新hbase最新版本
		ResultScanner rs = table.getScanner(scan);
		for(Result r : rs)
		{
			for(Cell cell : r.rawCells())
			{
				V.add(Bytes.toString(CellUtil.cloneValue(cell)));  //获得返回数据中value的值，方便后续处理 
			}
		}
		Collections.sort(V);
		VN = (String[])V.toArray(new String[0]);
		table.close();
		return VN;
	}
	/*
	 * 当已经识别出版本号，但是发现未知版本有补丁文件，于是需要输出这个补丁文件
	 * 通过文件版本号（行建末尾）和文件名（列值）两个条件查询文件所属的补丁包以及信息
	 * */
	public void  PatchCheck(String tablename,String row,String FName) throws IOException
	{
		if(admin.tableExists(tablename))
		{
			HTable table  = new HTable(cfg,tablename);
			FilterList  filterlist = new FilterList (FilterList.Operator.MUST_PASS_ALL);
			RowFilter filter = new RowFilter(CompareFilter.CompareOp.EQUAL,new RegexStringComparator(row));
			SingleColumnValueFilter filter0 = new SingleColumnValueFilter(
					Bytes.toBytes("Patch_Info"),
					Bytes.toBytes("FileName"),
					CompareOp.EQUAL,
					Bytes.toBytes(FName));
			filterlist.addFilter(filter0);
			filterlist.addFilter(filter);
			Scan scan = new Scan();
			scan.setMaxVersions(1);
			scan.setFilter(filterlist);
			ResultScanner rs = table.getScanner(scan);  
			for (Result r:rs)
			{
				int i=0;
				for(Cell cell : r.rawCells())
				{
					if(i==2)
					{
						System.out.println(Bytes.toString(CellUtil.cloneQualifier(cell))+
								" :"+Bytes.toString(CellUtil.cloneValue(cell)));
						RR = RR +"这个文件在补丁库(旧)的路径："+ Bytes.toString(CellUtil.cloneQualifier(cell))+
								" :"+Bytes.toString(CellUtil.cloneValue(cell)) + "\r\n";
					}
					if(i==5)
					{
						HTable table1  = new HTable(cfg,"Path_illustrate");
						Scan scan1 = new Scan();
						scan1.setMaxVersions(1);
						scan1.setRowPrefixFilter(Bytes.toBytes(Bytes.toString(CellUtil.cloneValue(cell))));
						ResultScanner rs1 = table1.getScanner(scan1);    
						for (Result r1:rs1)
						{
							for(Cell cell1 : r1.rawCells())
							{
								System.out.printf("%s",
										"   Value :"+Bytes.toString(CellUtil.cloneValue(cell1)));
								RR = RR +"所属补丁包(旧)的说明："+ Bytes.toString(CellUtil.cloneValue(cell1)) + "\r\n";
								RR = RR + "\r\n";
							}
						}
						table1.close();
					}
					i++;
				}
			}
			table.close();
		}

		//////////////////////////////////////新补丁包查询////////////////////////////////	
		if(admin.tableExists("newPatchInfo"))
		{
			MD5 hash = new MD5();
			HTable newtable  = new HTable(cfg,"newPatchInfo");
			Vector<String> V1 = new Vector<String>();
			FilterList  filterlist1 = new FilterList (FilterList.Operator.MUST_PASS_ALL);
			RowFilter filter1 = new RowFilter(CompareFilter.CompareOp.EQUAL,new RegexStringComparator(row));
			SingleColumnValueFilter filter01 = new SingleColumnValueFilter(
					Bytes.toBytes("newPatch_Info"),
					Bytes.toBytes("FileName"),
					CompareOp.EQUAL,
					Bytes.toBytes(FName));
			filterlist1.addFilter(filter01);
			filterlist1.addFilter(filter1);
			Scan scan1 = new Scan();
			scan1.setMaxVersions(1);
			scan1.setFilter(filterlist1);
			ResultScanner rs1 = newtable.getScanner(scan1);  
			for (Result r:rs1)
			{
				int i=0;
				for(Cell cell : r.rawCells())
				{
					if(i==2)
					{
						System.out.println(
								Bytes.toString(CellUtil.cloneQualifier(cell))+
								" :"+Bytes.toString(CellUtil.cloneValue(cell)));
						RR = RR + "这个文件在补丁库(新)的路径："+Bytes.toString(CellUtil.cloneQualifier(cell))+" :"+Bytes.toString(CellUtil.cloneValue(cell))+"\r\n";

					}
					if(i==4)
						V1.add(Bytes.toString(CellUtil.cloneValue(cell)));
					i++;
				}
			}
			newtable.close();
			HTable newtable1  = new HTable(cfg,"newPatch_illustrate");
			for(int j = 0;j<V1.size();j+=1)
			{
				Scan scan2 = new Scan();
				scan2.setMaxVersions(1);
				scan2.setRowPrefixFilter(Bytes.toBytes(hash.getMD5ofStr(V1.get(j))));
				ResultScanner rs2 = newtable1.getScanner(scan2);    
				for (Result r:rs2)
				{
					for(Cell cell : r.rawCells())
					{
						System.out.printf("%s",
								"   Value :"+Bytes.toString(CellUtil.cloneValue(cell)));
						RR = RR +"所属补丁包(新)说明信息："+ Bytes.toString(CellUtil.cloneValue(cell)) + "\r\n";
						RR = RR + "\r\n";
						//dataout(Bytes.toString(CellUtil.cloneValue(cell)));
					}
				}
			}

			newtable1.close();

		}
	}
	public static void dataout(String hist) throws IOException
	{
		File file = new File("etc/TXT1.txt");
		FileWriter out = new FileWriter ("TXT1.txt",true);
		out.write(hist+"\t\n");
		out.close();
	}
	public static String  VersionCheck(String IP,String[] Value,String[] target,int go) throws Exception
	{
		filecheck fc = new filecheck(IP);
		Vector<String> V = new Vector<String>();
		Vector<String> V1 = new Vector<String>();
		Map<String,String> base_dataMap = new HashMap<String , String>();
		int file_base=0;  //file_base>0 表示数据库中基线的文件版本比未知基线版本高
		int 	file_Target=0;  //file_Target>0 表示未知基线的文件版本比数据库中基线版本高
		int flag = 2;       
		for(int i=0;i<Value.length;i+=3)
		{
			if(Value[i+1].matches(".*x64.*"))  //同一个文件名可能在64位和32位的版本中重复，避免重复
			{
				base_dataMap.put(Value[i]+"_64", Value[i+2]);//存入MAP中
				V1.add(Value[i+1]);
			}
			else if(!V1.contains(Value[i+1])&&base_dataMap.containsKey(Value[i]))//避免同一文件名在不同文件卡中存在
			{
				base_dataMap.put(Value[i]+"*", Value[i+2]);
				V1.add(Value[i+1]);
			}
			else
			{
				base_dataMap.put(Value[i], Value[i+2]);	
				V1.add(Value[i+1]);
			}
		}
		for(int i=0;i<target.length;i+=3)
		{
			int base=0, Target=0;	
			if(!target[i+1].equals("9999")&&base_dataMap.get(target[i])!=null)
			{
				String[] target_values = target[i+1].split("\\.");
				String[] base_values = base_dataMap.get(target[i]).split("\\.");
				int j=0;
				while(j<base_values.length)
				{
					if((Integer.parseInt(base_values[j])-Integer.parseInt(target_values[j])>0))
					{
						base++;  //当前数据库中基线版本的一个文件版本号高于当前未知基线版本的一个文件版本号
						break;
					}
					if((Integer.parseInt(base_values[j])-Integer.parseInt(target_values[j])<0))
					{
						Target++; //当前数据库中基线版本的一个文件版本号低于当前未知基线版本的一个文件版本号
						if(go==1)
						{
							V.add(target[i]);
							V.add(target[i+1]);
							V.add(target[i+2]);
						}	
						break;
					}
					if((Integer.parseInt(base_values[j])-Integer.parseInt(target_values[j])==0))
						j++;
				}
				if(base!=0)
					file_base++;
				if(Target!=0)
					file_Target++;
			}else if(base_dataMap.get(target[i])==null && target[i+1].equals("9999")){
			}else if(target[i+1].equals("9999")&&base_dataMap.get(target[i])!=null){
				file_base++;
			}else if (!target[i+1].equals("9999")&&base_dataMap.get(target[i])==null){
				file_Target++;
				if(go==1)
				{
					V.add(target[i]);
					V.add(target[i+1]);
					V.add(target[i+2]);
				}	
			}
		}
		if(file_Target> 0&&file_base==0)
			flag = 0;
		if(file_Target==0&&file_base==0)
		{
			flag=1;
		}

		if(go==1)
		{
			for(int k=0;k<V.size();k+=3)
			{
				System.out.println("当前版本更新的文件名："+ V.get(k));
				RR = RR + "当前版本更新的文件名："+ V.get(k) + "\r\n";
				System.out.println("当前版本更新的文件版本："+ V.get(k+1));
				RR = RR + "当前版本更新的文件版本："+ V.get(k+1) + "\r\n";
				System.out.println("当前版本更新的文件路径："+ V.get(k+2));
				RR = RR + "当前版本更新的文件路径："+ V.get(k+2)+ "\r\n";
				String row = ".*_"+V.get(k+1)+"$";
				String FName = V.get(k);
				fc.PatchCheck("PatchInfo",row,FName);//所属补丁包信息查询
				RR =RR + "\r\n" + "\r\n";
			}
			System.out.println("file_Target 累计："+     file_Target);
			RR = RR + "file_Target 累计："+     file_Target + "\r\n";
			System.out.println("file_base 累计："+     file_base);
			RR = RR + "file_base 累计："+     file_base + "\r\n";
			return(RR);
		}
		return String.valueOf(flag);
	}
	public  String start_check(String[] data,String IP) throws Exception
	{
		filecheck fc = new filecheck(IP);
		String[] target = readdata(data);
		String[] VN = fc.V_RowKeyCheck("VersionInfo");
		int ok = 0;  //标志是否已经查处基线版本号，以及输出信息
		for(int i = (VN.length/2);i<VN.length;)//flag = 0 表示 target > base;flag = 1 表示 target =  base;flag = 2  表示 target < base。
		{
			int flag ;
			String Row = ".*_" + VN[i].substring(3)+"$";
			String[] value = fc.RowKeyCheck("FileInfo",Row);
			flag = Integer.valueOf(VersionCheck(IP,value,target,0));
			if(flag == 2)
			{
				i++;
			}
			if(flag == 1){
				System.out.println("该项目基线版本为："   + VN[i]);
				System.out.println("建立时间："   + VN[i-(VN.length/2)]);
				RR = RR + "该项目基线版本为："   + VN[i] + "\r\n";
				RR = RR + "建立时间："   + VN[i-(VN.length/2)] + "\r\n";
				ok = 1;
				break;
			}
			while(flag==0)
			{
				i++;
				if(!(i<VN.length))
				{
					System.out.println("该项目基线版本为："   + VN[i-1]);
					System.out.println("建立时间："   + VN[i-(VN.length/2)-1]);
					RR = RR + "该项目基线版本为："   + VN[i-1] + "\r\n";
					RR = RR + "建立时间："   + VN[i-(VN.length/2)-1] + "\r\n";
					ok = 1;
					String Row111 = ".*_" + VN[i-1].substring(3)+"$";   //为了输出比此版本更新的版本文件，需要再次进入查询和比对计算。此方法可以优化。
					String[] value111 = fc.RowKeyCheck("FileInfo",Row111);
					VersionCheck(IP,value111,target,1);
					break;
				}
				String Row1 = ".*_" + VN[i].substring(3)+"$";
				String[] value1 = fc.RowKeyCheck("FileInfo",Row1);
				flag = Integer.valueOf(VersionCheck(IP,value1,target,0));
				if(flag==1)
				{
					System.out.println("该项目基线版本为："   + VN[i]);
					System.out.println("建立时间："   + VN[i-(VN.length/2)]);
					RR = RR + "该项目基线版本为："   + VN[i] + "\r\n";
					RR = RR + "建立时间："   + VN[i-(VN.length/2)] + "\r\n";
					ok = 1;
					break;
				}
				if(flag==2)
				{
					System.out.println("该项目基线版本为："   + VN[i-1]);
					System.out.println("建立时间："   + VN[i-(VN.length/2)-1]);
					RR = RR + "该项目基线版本为："   + VN[i-1] + "\r\n";
					RR = RR + "建立时间："   + VN[i-(VN.length/2)-1] + "\r\n";
					ok = 1;
					String Row11 = ".*_" + VN[i-1].substring(3)+"$"; //为了输出比此版本更新的版本文件，需要再次进入查询和比对计算。此方法可以优化。
					String[] value11 = fc.RowKeyCheck("FileInfo",Row11);
					VersionCheck(IP,value11,target,1);
					break;
				}
			}
			if(ok==1)
			{
				break;
			}
		}
		if(ok==0)
		{
			System.out.println("该项目基线版本未知");
			RR = RR + "该项目基线版本未知" + "\r\n";
		}
		return RR;
	}
}