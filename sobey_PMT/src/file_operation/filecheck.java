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
 * ������������Ŀ�汾�����ߵ�Դ��
 * �������ܣ���δ֪�Ļ��߰汾��hbase���ݿ��а汾����ʶ��
 * 
 * ���ߣ������
 * ʱ�䣺2016/5/12������
 * */
public class filecheck {
	private HBaseAdmin admin = null;
	// �������ö���HBaseConfiguration
	private HBaseConfiguration cfg = null;
	static public String RR = "";   //����Ҫ���������ȫ���ӵ�����ַ�����

	public filecheck(String IP) throws Exception {
		Configuration HBASE_CONFIG = new Configuration();

		HBASE_CONFIG.set("hbase.zookeeper.quorum", IP); //hbase������IP��ַ
		HBASE_CONFIG.set("hbase.zookeeper.property.clientPort", "2181");  //zookeeper�˿ں�
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
			if(values[3].matches(".*x64.*"))  //����64λ��32λ��dllwe
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
			V.add(values[3]);  //ֻ��Ҫ�ļ������ļ��汾�ţ��ļ�·�� ����Աȼ���
		}
		value = (String[])V.toArray(new String[0]);
		return value;
	}
	/*
	 * �����н���ѯ�õ�һ�����߰汾���������ļ���ȫ����Ϣ����Աȼ��㣬������� ����������Ҫ��ѯ���м�����
	 * */
	public String[] RowKeyCheck(String tablename,String row) throws IOException
	{
		HTable table = new HTable(cfg,tablename);
		String[] Value = null;
		Vector<String> V = new Vector<String>();
		FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);
		SingleColumnValueFilter filter1 = new SingleColumnValueFilter(Bytes.toBytes("File_Info"),Bytes.toBytes("FileVersion"),
				CompareOp.NOT_EQUAL,Bytes.toBytes("9999"));////û�а汾�ŵĲ����
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
				if(i==1||i==3||i==2)  //ֻ��Ҫ�ļ������ļ��汾�ţ��ļ�·��  
				{
					V.add(Bytes.toString(CellUtil.cloneValue(cell)));  //��÷���������value��ֵ������������� 
				}
				i++;
			}
		}
		table.close();
		Value = (String[])V.toArray(new String[0]);
		return Value;
	}
	/*
	 * �õ����ݿ��л��߰汾�ĸ����Լ��汾�źʹ���ʱ�䣬
	 * */
	public String[] V_RowKeyCheck(String tablename) throws IOException
	{
		///////////////////////////////////////////////////////////////
		HTable table = new HTable(cfg,tablename);
		String[] VN = null;
		Vector<String> V = new Vector<String>();
		Scan scan = new Scan();
		scan.setMaxVersions(1); //ֻ��ȡ����hbase���°汾
		ResultScanner rs = table.getScanner(scan);
		for(Result r : rs)
		{
			for(Cell cell : r.rawCells())
			{
				V.add(Bytes.toString(CellUtil.cloneValue(cell)));  //��÷���������value��ֵ������������� 
			}
		}
		Collections.sort(V);
		VN = (String[])V.toArray(new String[0]);
		table.close();
		return VN;
	}
	/*
	 * ���Ѿ�ʶ����汾�ţ����Ƿ���δ֪�汾�в����ļ���������Ҫ�����������ļ�
	 * ͨ���ļ��汾�ţ��н�ĩβ�����ļ�������ֵ������������ѯ�ļ������Ĳ������Լ���Ϣ
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
						RR = RR +"����ļ��ڲ�����(��)��·����"+ Bytes.toString(CellUtil.cloneQualifier(cell))+
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
								RR = RR +"����������(��)��˵����"+ Bytes.toString(CellUtil.cloneValue(cell1)) + "\r\n";
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

		//////////////////////////////////////�²�������ѯ////////////////////////////////	
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
						RR = RR + "����ļ��ڲ�����(��)��·����"+Bytes.toString(CellUtil.cloneQualifier(cell))+" :"+Bytes.toString(CellUtil.cloneValue(cell))+"\r\n";

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
						RR = RR +"����������(��)˵����Ϣ��"+ Bytes.toString(CellUtil.cloneValue(cell)) + "\r\n";
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
		int file_base=0;  //file_base>0 ��ʾ���ݿ��л��ߵ��ļ��汾��δ֪���߰汾��
		int 	file_Target=0;  //file_Target>0 ��ʾδ֪���ߵ��ļ��汾�����ݿ��л��߰汾��
		int flag = 2;       
		for(int i=0;i<Value.length;i+=3)
		{
			if(Value[i+1].matches(".*x64.*"))  //ͬһ���ļ���������64λ��32λ�İ汾���ظ��������ظ�
			{
				base_dataMap.put(Value[i]+"_64", Value[i+2]);//����MAP��
				V1.add(Value[i+1]);
			}
			else if(!V1.contains(Value[i+1])&&base_dataMap.containsKey(Value[i]))//����ͬһ�ļ����ڲ�ͬ�ļ����д���
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
						base++;  //��ǰ���ݿ��л��߰汾��һ���ļ��汾�Ÿ��ڵ�ǰδ֪���߰汾��һ���ļ��汾��
						break;
					}
					if((Integer.parseInt(base_values[j])-Integer.parseInt(target_values[j])<0))
					{
						Target++; //��ǰ���ݿ��л��߰汾��һ���ļ��汾�ŵ��ڵ�ǰδ֪���߰汾��һ���ļ��汾��
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
				System.out.println("��ǰ�汾���µ��ļ�����"+ V.get(k));
				RR = RR + "��ǰ�汾���µ��ļ�����"+ V.get(k) + "\r\n";
				System.out.println("��ǰ�汾���µ��ļ��汾��"+ V.get(k+1));
				RR = RR + "��ǰ�汾���µ��ļ��汾��"+ V.get(k+1) + "\r\n";
				System.out.println("��ǰ�汾���µ��ļ�·����"+ V.get(k+2));
				RR = RR + "��ǰ�汾���µ��ļ�·����"+ V.get(k+2)+ "\r\n";
				String row = ".*_"+V.get(k+1)+"$";
				String FName = V.get(k);
				fc.PatchCheck("PatchInfo",row,FName);//������������Ϣ��ѯ
				RR =RR + "\r\n" + "\r\n";
			}
			System.out.println("file_Target �ۼƣ�"+     file_Target);
			RR = RR + "file_Target �ۼƣ�"+     file_Target + "\r\n";
			System.out.println("file_base �ۼƣ�"+     file_base);
			RR = RR + "file_base �ۼƣ�"+     file_base + "\r\n";
			return(RR);
		}
		return String.valueOf(flag);
	}
	public  String start_check(String[] data,String IP) throws Exception
	{
		filecheck fc = new filecheck(IP);
		String[] target = readdata(data);
		String[] VN = fc.V_RowKeyCheck("VersionInfo");
		int ok = 0;  //��־�Ƿ��Ѿ��鴦���߰汾�ţ��Լ������Ϣ
		for(int i = (VN.length/2);i<VN.length;)//flag = 0 ��ʾ target > base;flag = 1 ��ʾ target =  base;flag = 2  ��ʾ target < base��
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
				System.out.println("����Ŀ���߰汾Ϊ��"   + VN[i]);
				System.out.println("����ʱ�䣺"   + VN[i-(VN.length/2)]);
				RR = RR + "����Ŀ���߰汾Ϊ��"   + VN[i] + "\r\n";
				RR = RR + "����ʱ�䣺"   + VN[i-(VN.length/2)] + "\r\n";
				ok = 1;
				break;
			}
			while(flag==0)
			{
				i++;
				if(!(i<VN.length))
				{
					System.out.println("����Ŀ���߰汾Ϊ��"   + VN[i-1]);
					System.out.println("����ʱ�䣺"   + VN[i-(VN.length/2)-1]);
					RR = RR + "����Ŀ���߰汾Ϊ��"   + VN[i-1] + "\r\n";
					RR = RR + "����ʱ�䣺"   + VN[i-(VN.length/2)-1] + "\r\n";
					ok = 1;
					String Row111 = ".*_" + VN[i-1].substring(3)+"$";   //Ϊ������ȴ˰汾���µİ汾�ļ�����Ҫ�ٴν����ѯ�ͱȶԼ��㡣�˷��������Ż���
					String[] value111 = fc.RowKeyCheck("FileInfo",Row111);
					VersionCheck(IP,value111,target,1);
					break;
				}
				String Row1 = ".*_" + VN[i].substring(3)+"$";
				String[] value1 = fc.RowKeyCheck("FileInfo",Row1);
				flag = Integer.valueOf(VersionCheck(IP,value1,target,0));
				if(flag==1)
				{
					System.out.println("����Ŀ���߰汾Ϊ��"   + VN[i]);
					System.out.println("����ʱ�䣺"   + VN[i-(VN.length/2)]);
					RR = RR + "����Ŀ���߰汾Ϊ��"   + VN[i] + "\r\n";
					RR = RR + "����ʱ�䣺"   + VN[i-(VN.length/2)] + "\r\n";
					ok = 1;
					break;
				}
				if(flag==2)
				{
					System.out.println("����Ŀ���߰汾Ϊ��"   + VN[i-1]);
					System.out.println("����ʱ�䣺"   + VN[i-(VN.length/2)-1]);
					RR = RR + "����Ŀ���߰汾Ϊ��"   + VN[i-1] + "\r\n";
					RR = RR + "����ʱ�䣺"   + VN[i-(VN.length/2)-1] + "\r\n";
					ok = 1;
					String Row11 = ".*_" + VN[i-1].substring(3)+"$"; //Ϊ������ȴ˰汾���µİ汾�ļ�����Ҫ�ٴν����ѯ�ͱȶԼ��㡣�˷��������Ż���
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
			System.out.println("����Ŀ���߰汾δ֪");
			RR = RR + "����Ŀ���߰汾δ֪" + "\r\n";
		}
		return RR;
	}
}