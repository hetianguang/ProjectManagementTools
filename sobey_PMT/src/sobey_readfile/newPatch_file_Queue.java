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
 * 本程序功能：读取新版补丁包(含有.xml文件)内的文件（dll等）信息以及补丁包本身的描述信息，包括：文件名，文件路径，文件版本，文件最后修改时间等。
 * 补丁包本身的描述信息是通过包内的.xml文件获取的，分为两类，一类为包的属性，如：包名字，包创建时间，包类型，BUGID等；一类为包的说明信息，包括
 * ：解决的问题，问题的原因等说明字段。
 * 作者：何天光
 * 时间：2016/5/12（整理）
 * */
public class newPatch_file_Queue {
	public static String[] getAllFiles(String path) throws IOException
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //输出时间的格式
		Queue<File> queue = new Queue<File>();//创建一个队列
		File dir = new File(path);
		File[] files = dir.listFiles(); //对目录进行当前文件夹和文件的遍历
		MD5 hash = new MD5(); 
		String[] Version = null;
		String PM =null;
		Vector<String> V = new Vector<String>();
		Vector<String> V1 = new Vector<String>();
		for (File file : files)
		{
			if(file.isDirectory())
			{
				queue.add(file);  //将子目录存储到队列中
			}else{
				if(file.getName().endsWith("xml"))
				{
					if(newPatch_XML.readxml1(file)!=null)   //获取.xml文件的内容
					{
						Version = newPatch_XML.readxml1(file);
						PM=Version[0];
						V1.add(Version[1]);    
						continue;
					}
				}
				if(file.getName().endsWith("txt"))   //遇到.txt文件直接跳过，因为txt文件的内容已经在xml文件中存在了。
				{
					continue;
				}
				String s1 = file.getName();  // 文件名
				String s2 = file.getPath();  //文件路径
				String s3 = formatter.format(new Date(file.lastModified())); //文件最后修改时间
				String s4 = File_info.getVersion(file);// 文件版本号
				String s5 = hash.getMD5ofStr(s1+s2+s3+s4);  //MD5码，将作为行键
				String S0 = PM+","+s1+","+s2+","+s3+","+s4+","+s5;
				V.add(S0);
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
					if(subFile.getName().endsWith("xml"))   //获取.xml文件的内容
					{
						if(newPatch_XML.readxml1(subFile)!=null)
						{
							Version = newPatch_XML.readxml1(subFile);
							V1.add(Version[1]);
							PM = Version[0];
							continue;
						}
					}
					if(subFile.getName().endsWith("txt"))   //遇到.txt文件直接跳过，因为txt文件的内容已经在xml文件中存在了。
					{
						continue;
					}
					String s0 = subFile.getName();  // 文件名
					String s1 = subFile.getPath();   //文件路径
					String s2 = formatter.format(new Date(subFile.lastModified()));  //文件最后修改时间
					String s3 = File_info.getVersion(subFile);  // 文件版本号
					String s4 = hash.getMD5ofStr(s0+s1+s2+s3);  //MD5码，将作为行键
					String S1 = PM+","+s0+","+s1+","+s2+","+s3+","+s4;
					V.add(S1);
				}
			}
		}
		V.add("xml");
		for(int j = 0 ; j < V1.size(); j++)
		{
			V.add(V1.get(j));
		}
		String[] info = (String[])V.toArray(new String[0]);
		return info;
	}
	/*
	 * 将读取到的信息存入txt文件
	 * */
	public static void output(String[] ss) throws IOException
	{
		Scanner s = new Scanner(System.in);
		System.out.println("输入需要保存读取到补丁包文件信息的数据的文件路径以及文件名.txt：");
		String path = s.nextLine();
		File file = new File(path);
		FileWriter out = new FileWriter(file);
		out.write("补丁包名"+","+"补丁的名字"+","+"补丁的路径"+","+"补丁最后修改时间"+","+"补丁版本"+","+"补丁的MD5码"+","+"\r\n");
		int cnt=0;
		for(int i = 0; i<ss.length;i++)
		{
			if("xml".equals(ss[i]))
			{
				cnt = i;
				break;
			} 
			out.write(ss[i]+"\r\n");
		}
		out.close();
		output1(ss,cnt);
	}
	public static void output1(String[] ss,int cnt) throws IOException
	{
		Scanner s = new Scanner(System.in);
		System.out.println("输入需要保存读取到补丁包的补丁说明文档的数据的文件路径以及文件名.txt：");
		String path = s.nextLine();
		File file = new File(path);
		FileWriter out = new FileWriter(file);
		out.write("补丁类型"+","+"补丁包名"+","+"补丁时间"+","+"BUG类型"+","+"bugID"+","+"SDK版本"+","+"应用层版本"+","+"提供人"+","+"接受人"+","+"来源"+","+"说明"+","+"\r\n");
		for(int i = cnt+1; i<ss.length;i++)
		{
			out.write(ss[i]+"\r\n");
		}
		out.close();
	}
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
		Scanner s = new Scanner(System.in);
		System.out.println("输入路径：");
		String path = s.nextLine();
		String[] ss = getAllFiles(path);
		output(ss);
	}
}
/*
 * 根据xml文件的格式，进行读取内容；读取的内容主要是补丁包是属性和说明信息。
 * */
class newPatch_XML
{
	public static String[] readxml1(File file)
	{
		Element element = null;
		String V1 = null;
		String[] V2 =  new String[2];
		Vector<String> V = new Vector<String>();
		String T = "";
		// File file = new File("D:\\sobey_intern\\正式补丁\\test\\20151021正式补丁(杨军)\\ReleaseDescriptiontestFP.xml");
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
				for(int i =0;i<nodeDetail.getLength();i++)
				{
					Node node2 = nodeDetail.item(i);
					if("SDKPatch".equals(node2.getNodeName()))
					{
						System.out.println("类型：  " +node2.getAttributes().getNamedItem("Type").getNodeValue());
						V.add(node2.getAttributes().getNamedItem("Type").getNodeValue());
						NodeList nodeNext = node2.getChildNodes();
						for(int j=0;j<nodeNext.getLength();j++)
						{
							Node node3 = nodeNext.item(j);
							if("Patch".equals(node3.getNodeName()))
							{
								System.out.println("补丁包名字：  " +node3.getAttributes().getNamedItem("Name").getNodeValue());
								V.add(node3.getAttributes().getNamedItem("Name").getNodeValue());
								System.out.println("补丁包时间：  " +node3.getAttributes().getNamedItem("Time").getNodeValue());
								V.add(node3.getAttributes().getNamedItem("Time").getNodeValue());
							}
							NodeList nodeNext1 = node3.getChildNodes();
							for(int k=0;k<nodeNext1.getLength();k++)
							{
								Node node4 = nodeNext1.item(k);
								if("releasenotes".equals(node4.getNodeName()))
								{
									NodeList nodeNext3 = node4.getChildNodes();
									for(int m=0;m<nodeNext3.getLength();m++)
									{
										Node node5 = nodeNext3.item(m);
										if("BugType".equals(node5.getNodeName()))
										{
											System.out.println("BugType: "+ node5.getTextContent());
											V.add(node5.getTextContent());
										}
										if("BugID".equals(node5.getNodeName()))
										{
											System.out.println("BugID: "+ node5.getTextContent());
											V.add(node5.getTextContent());
										}
										if("SDKVersion".equals(node5.getNodeName()))
										{
											System.out.println("SDKVersion: "+ node5.getTextContent());
											V.add(node5.getTextContent());
										}
										if("ApplicationVersion".equals(node5.getNodeName()))
										{
											System.out.println("ApplicationVersion: "+ node5.getTextContent());
											V.add(node5.getTextContent());
										}
										if("Provider".equals(node5.getNodeName()))
										{
											System.out.println("Provider: "+ node5.getTextContent());
											V.add(node5.getTextContent());
										}
										if("recipient".equals(node5.getNodeName()))
										{
											System.out.println("recipient: "+ node5.getTextContent());
											V.add(node5.getTextContent());
										}
										if("Issue".equals(node5.getNodeName()))
										{
											System.out.println("Issue: "+ node5.getTextContent());
											T = T +" "+"问题："+node5.getTextContent();
										}
										if("Cause".equals(node5.getNodeName()))
										{
											System.out.println("Cause: "+ node5.getTextContent());
											T = T +" "+"问题原因："+node5.getTextContent();
										}
										if("HowToFixed".equals(node5.getNodeName()))
										{
											System.out.println("HowToFixed: "+ node5.getTextContent());
											T = T +" "+"解决方案："+node5.getTextContent();
										}
										if("InfluenceRange".equals(node5.getNodeName()))
										{
											System.out.println("InfluenceRange: "+ node5.getTextContent());
											T = T +" "+"影响："+node5.getTextContent();
										}
										if("instructions".equals(node5.getNodeName()))
										{
											System.out.println("instructions: "+ node5.getTextContent());
											T = T +" "+"说明："+node5.getTextContent();
										}
										if("FomeWhichTestPatch".equals(node5.getNodeName()))
										{
											System.out.println("FomeWhichTestPatch: "+ node5.getTextContent());
											V.add(node5.getTextContent());
										}
									}
								}
							}
						}
					}
				}

				V1 = V.get(0);
				for(int i=1;i<V.size();i++)
				{
					V1 = V1+","+V.get(i);
				}
			}
			V2[0]=V.get(1);V2[1] = V1+","+T;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return V2;
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
