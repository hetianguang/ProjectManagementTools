package sobey_readfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.io.FileReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import beartool.*;
import com.joyce.util.File_info;
import java.util.Scanner;
/*
 * 本程序属于项目版本管理工具的源码
 * 本程序功能：读取老版补丁包文件信息以及补丁信息。老版补丁定义：每个补丁包没有xml描述文件，但是每一类补丁包有一个xml文件秒这类补丁是什么类型。
 * 作者：何天光
 * 时间：2016/5/12（整理）
 * */
public class Patch_file_Queue {
       public static String[] getAllFiles(String path) throws IOException
       {
    	   SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");  //输出时间格式
    	   Queue<File> queue = new Queue<File>();  //创建一个队列
    	   File dir = new File(path);
    	   File[] files = dir.listFiles(); //对目录进行当前文件夹和文件的遍历
		   MD5 hash = new MD5();
		   String Version = null;
		   String s6 = null;
		   String Patch_illustrate = "";
		   Vector<String> V = new Vector<String>();
		   Vector<String> V1 = new Vector<String>();
    	   for (File file : files)
    	   {
    		   if(file.isDirectory())
    		   {
				   queue.add(file);  //将子目录存储到队列中
    		   }else{
    			   if(file.getName().endsWith("xml"))  //判断是否为xml文件
    			   {
    				   if(Patch_XML.readxml1(file)!=null) 
    				   {
    					    Version = Patch_XML.readxml1(file); //获取xml文件的信息，这个信息是一类补丁包的类型信息而不是每个补丁包的xml描述信息
    				   }
    			   }
				   if(file.getName().endsWith("txt"))    //获取txt文件的内容
				   {
					    s6 = hash.getMD5ofStr(file.getPath());
				   }
            	   String s1 = file.getName();  //文件名
            	   String s2 = file.getPath();  //文件路径
            	   String s3 = formatter.format(new Date(file.lastModified()));//文件最后修改时间
				   String s4 = File_info.getVersion(file); //文件版本号
				   String s5 = hash.getMD5ofStr(s1+s2+s3+s4);
				   String S0 = Version+","+s6+","+s1+","+s2+","+s3+","+s4+","+s5;
				   V.add(S0);
    		   }
			   if(file.getName().endsWith("txt"))
			      {
				   Patch_illustrate = readTxt(file);
				   String T1 = file.getPath();
				   String T2 = hash.getMD5ofStr(T1) + "," + Patch_illustrate;				   
				   V1.add(T2);
			      }
    	   }
    	   //遍历队列
    	   while(!queue.isEmpty())
    	   {
    		   File subDir = queue.get();
    		   File[] subFiles = subDir.listFiles();
    		   for (File subFile :subFiles)
    		   {
    			   if(subFile.isDirectory())
    			   {
					   queue.add(subFile);
    			   }else
    			     {
						if(subFile.getName().endsWith("xml"))
						   {
							   if(Patch_XML.readxml1(subFile)!=null)
							   {
								   Version = Patch_XML.readxml1(subFile);
							   }
						   }
						if(subFile.getName().endsWith("txt"))
				           {
					           s6 = hash.getMD5ofStr(subFile.getPath());
				           }
    				   String s0 = subFile.getName();
    				   String s1 = subFile.getPath();
    				   String s2 = formatter.format(new Date(subFile.lastModified()));
					   String s3 = File_info.getVersion(subFile);
					   String s4 = hash.getMD5ofStr(s0+s1+s2+s3);
					   String S1 = Version+","+s6+","+s0+","+s1+","+s2+","+s3+","+s4;
					   V.add(S1);
    			   }
				   if(subFile.getName().endsWith("txt"))
				   {
					Patch_illustrate = readTxt(subFile);
					String T1 = subFile.getPath();
					String T2 = hash.getMD5ofStr(T1) + "," + Patch_illustrate;
					V1.add(T2);
				   }
    		   }
    	   }
    	   V.add("txt");//将txt文件的内容和补丁文件信息做一个区分标志
    	   for(int j = 0 ; j < V1.size(); j++)
    	   {
    		   V.add(V1.get(j));
    	   }
    	   String[] info = (String[])V.toArray(new String[0]);
    	   return info;
       }
       /*
        * 根据读到的信息分类存到txt文件中
        * */
       public static void output(String[] ss) throws IOException
       {
			Scanner s = new Scanner(System.in);
			System.out.println("输入需要保存读取到补丁包文件信息的数据的文件路径以及文件名.txt：");
			String path = s.nextLine();
    	   File file = new File(path);
    	   FileWriter out = new FileWriter(file);
    	   int cnt=0;
    	   for(int i = 0; i<ss.length;i++)
    	   {
    		   if("txt".equals(ss[i]))
    		   {
    			   cnt = i;
    			   break;
    		   } 
    		   out.write(ss[i]+"\r\n");
    	   }
    	   output1(ss,cnt);
    	   out.close();
       }
	  public static void output1(String[] ss,int cnt) throws IOException
       {
			Scanner s = new Scanner(System.in);
			System.out.println("输入需要保存读取到补丁包的补丁说明文档的数据的文件路径以及文件名.txt：");
			String path = s.nextLine();
    	   File file = new File(path);
    	   FileWriter out = new FileWriter(file);
    	   for(int i = cnt+1; i<ss.length;i++)
    	   {
    		   out.write(ss[i]+"\r\n");
    	   }
    	   out.close();
       }
	  /*
	   * 补丁中的txt文件的读取工作
	   * */
	   public static String readTxt(File file)throws IOException
	   {
		   String Patch_illustrate = "";
		   BufferedReader reader = new BufferedReader(new FileReader(file));
		   String line = reader.readLine();
		   while(line!=null)
		   {
			   Patch_illustrate = Patch_illustrate+line+"  ";
               line = reader.readLine();			   
		   }
		   return Patch_illustrate;
	   }
    
       public static void main (String[] args) throws IOException
       {
//			Scanner s = new Scanner(System.in);
//			System.out.println("输入补丁包文件的路径：");
//			String path = s.nextLine();
//			String[] ss = getAllFiles(path);
//			output(ss);
//    	   XML.readxml1();
    	   
       }
}

/*
 * xml文件的读取工作
 * 
 * */
class Patch_XML
{
	public static String readxml1(File file)
	{
		Element element = null;
		String V = null;
		//File file = new File("D:\\sobey_intern\\File_Info_check Project\\Test\\测试补丁\\测试补丁\\ReleaseDescription_TP.xml");
		DocumentBuilder db = null;
		DocumentBuilderFactory dbf = null;
		try
		{
			dbf = DocumentBuilderFactory.newInstance();
			db = dbf.newDocumentBuilder();
			Document dt = db.parse(file);
			element = dt.getDocumentElement();
			NodeList childNodes = element.getChildNodes();
			Node node1 = childNodes.item(1);

			if("SDK".equals(node1.getNodeName())&&"2".equals(node1.getAttributes().getNamedItem("Attributes").getNodeValue()))
			{
				NodeList nodeDetail = node1.getChildNodes();
				Node node2 = nodeDetail.item(3);
				if("SDKPatch".equals(node2.getNodeName()))
				{
					V = node2.getAttributes().getNamedItem("Type").getNodeValue();
				}
			}
			else
			{
				return null;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return V;
	}
}

//class Queue<E>
//{
//	private LinkedList<E> link;
//	public Queue()
//	{
//		link = new LinkedList<E>();
//	}
//	public void add(E o)
//	{
//		link.addFirst(o); 
//	}
//	public E get(){
//		//return link.removeLast();
//		return link.remove(0);
//	}
//	public boolean isEmpty(){
//		return link.isEmpty();
//	}
//}

