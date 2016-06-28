package sobey_readfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.joyce.util.File_info;

import beartool.MD5;

/*
 * 本程序属于项目版本管理工具的源码
 * 本程序功能：读取基线版本中所有的文件信息以及基线版本本身的版本号和创建时间，包括：文件名，文件路径，文件版本，文件最后修改时间等。
 * 
 * 作者：何天光
 * 时间：2016/5/12（整理）
 * */
public class Base_file_Queue {
	public static String[] getAllFiles(String path) throws IOException
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//声明时间输出格式
		Queue<File> queue = new Queue<File>(); //创建队列
		File dir = new File(path); 
		File[] files = dir.listFiles();//对目录进行当前文件夹和文件的遍历
		File_info fd = new File_info();
		MD5 hash = new MD5();
		String[] Version = new String[2];
		Version[1]="9999";//表示为空
		Version[0]="9999";
		Vector<String> V = new Vector<String>();
		for (File file : files)
		{
			if(file.isDirectory())
			{
				queue.add(file);  //将子目录存储到队列中
			}
			else
			{
				if(file.getName().endsWith("xml"))
				{
					if(XML.readxml1(file)!=null) //读取.xml描述信息
					{
						Version = XML.readxml1(file);
					}  				   
				}
				String s1 = file.getName();  //文件名
				String s2 = file.getPath();  //文件路径
				String s3 = formatter.format(new Date(file.lastModified())); //文件最后修改时间
				String s4 = File_info.getVersion(file);  //文件版本号
				String s5 = hash.getMD5ofStr(s1+s2+s3+s4);  //这些信息的之和的MD5码，在hbase中充当行键的一部分。
				String S0 = Version[0]+","+Version[1]+","+s1+","+s2+","+s3+","+s4+","+s5;
				V.add(S0);
			}
		}
		while(!queue.isEmpty()) //队列遍历
		{
			File subDir = queue.get();
			File[] subFiles = subDir.listFiles();
			for (File subFile :subFiles)
			{
				if(subFile.isDirectory())
				{
					queue.add(subFile);
				}
				else
				{
					if(subFile.getName().endsWith("xml"))
					{
						if(XML.readxml1(subFile)!=null)
						{
							Version = XML.readxml1(subFile);
						}
					}
					String s0 = subFile.getName(); //文件名
					String s1 = subFile.getPath();  //文件路径
					String s2 = formatter.format(new Date(subFile.lastModified())); //文件最后修改时间
					String s3 = File_info.getVersion(subFile);  //文件版本号
					String s4 = hash.getMD5ofStr(s0+s1+s2+s3);  //这些信息的之和的MD5码，在hbase中充当行键的一部分。
					String S1 = Version[0]+","+Version[1]+","+s0+","+s1+","+s2+","+s3+","+s4;
					V.add(S1);
				}
			}
		}
		String[] info = (String[])V.toArray(new String[0]);
		return info;
	}
	/*
	 * output（）方法输出读取到的文件信息到指定的.txt文件中
	 * */
	public static void output(String[] ss,String path) throws IOException
	{
		File file = new File(path);
		FileWriter out = new FileWriter(file);
		for(int i = 0; i<ss.length;i++)
		{
			out.write(ss[i]+"\r\n");
		}
		out.close();
	}
	public static void main (String[] args) throws IOException
	{
		//			Scanner s = new Scanner(System.in);
		//			System.out.println("输入基线版本文件所在的路径：");
		//			String path0 = s.nextLine();
		//			File f1 = new File(path0);
		//			String[] ss = getAllFiles(path0);
		//			Scanner s1 = new Scanner(System.in);
		//			System.out.println("输入读取到的基线版本数据需要保存的路径以及文件名.txt：");
		//			String path1 = s1.nextLine();			
		//			output(ss,path1);
		//    	    XML.readxml1();

	}
}
/*
 * XML这个类主要功能就是读取基线版本的描述文件.xml，在这个文件中主要获取基线版本的名字如：“SDK5.2”，和基线版本创建时间。
 * */
class XML
{
	public static String[] readxml1(File file)
	{
		Element element = null;
		String[] V = new String[2];
		//File file = new File("D:\\sobey_intern\\全版本\\全版本\\Cutlist SDK 5.2.2\\ReleaseDescription.xml");
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

			if("SDK".equals(node1.getNodeName())&&"1".equals(node1.getAttributes().getNamedItem("Attributes").getNodeValue()))
			{
				NodeList nodeDetail = node1.getChildNodes();
				Node node2 = nodeDetail.item(3);
				if("SDKBaseLine".equals(node2.getNodeName()))
				{
					V[0] = node2.getAttributes().getNamedItem("version").getNodeValue();
					//System.out.println("版本：  " +node2.getAttributes().getNamedItem("version").getNodeValue());
					NodeList nodeNext = node2.getChildNodes();
					for(int i=0;i<nodeNext.getLength();i++)
					{
						Node node = nodeNext.item(i);
						if("Time".equals(node.getNodeName()))
						{
							V[1] = node.getTextContent();
						}
					}
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

class Queue<E>
{
	private LinkedList<E> link;
	public Queue()
	{
		link = new LinkedList<E>();
	}
	public void add(E o)
	{
		link.addFirst(o); 
	}
	public E get(){
		//return link.removeLast();
		return link.remove(0);  
	}
	public boolean isEmpty(){
		return link.isEmpty();
	}
}

