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
 * ������������Ŀ�汾�����ߵ�Դ��
 * �������ܣ���ȡ���߰汾�����е��ļ���Ϣ�Լ����߰汾����İ汾�źʹ���ʱ�䣬�������ļ������ļ�·�����ļ��汾���ļ�����޸�ʱ��ȡ�
 * 
 * ���ߣ������
 * ʱ�䣺2016/5/12������
 * */
public class Base_file_Queue {
	public static String[] getAllFiles(String path) throws IOException
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//����ʱ�������ʽ
		Queue<File> queue = new Queue<File>(); //��������
		File dir = new File(path); 
		File[] files = dir.listFiles();//��Ŀ¼���е�ǰ�ļ��к��ļ��ı���
		File_info fd = new File_info();
		MD5 hash = new MD5();
		String[] Version = new String[2];
		Version[1]="9999";//��ʾΪ��
		Version[0]="9999";
		Vector<String> V = new Vector<String>();
		for (File file : files)
		{
			if(file.isDirectory())
			{
				queue.add(file);  //����Ŀ¼�洢��������
			}
			else
			{
				if(file.getName().endsWith("xml"))
				{
					if(XML.readxml1(file)!=null) //��ȡ.xml������Ϣ
					{
						Version = XML.readxml1(file);
					}  				   
				}
				String s1 = file.getName();  //�ļ���
				String s2 = file.getPath();  //�ļ�·��
				String s3 = formatter.format(new Date(file.lastModified())); //�ļ�����޸�ʱ��
				String s4 = File_info.getVersion(file);  //�ļ��汾��
				String s5 = hash.getMD5ofStr(s1+s2+s3+s4);  //��Щ��Ϣ��֮�͵�MD5�룬��hbase�г䵱�м���һ���֡�
				String S0 = Version[0]+","+Version[1]+","+s1+","+s2+","+s3+","+s4+","+s5;
				V.add(S0);
			}
		}
		while(!queue.isEmpty()) //���б���
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
					String s0 = subFile.getName(); //�ļ���
					String s1 = subFile.getPath();  //�ļ�·��
					String s2 = formatter.format(new Date(subFile.lastModified())); //�ļ�����޸�ʱ��
					String s3 = File_info.getVersion(subFile);  //�ļ��汾��
					String s4 = hash.getMD5ofStr(s0+s1+s2+s3);  //��Щ��Ϣ��֮�͵�MD5�룬��hbase�г䵱�м���һ���֡�
					String S1 = Version[0]+","+Version[1]+","+s0+","+s1+","+s2+","+s3+","+s4;
					V.add(S1);
				}
			}
		}
		String[] info = (String[])V.toArray(new String[0]);
		return info;
	}
	/*
	 * output�������������ȡ�����ļ���Ϣ��ָ����.txt�ļ���
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
		//			System.out.println("������߰汾�ļ����ڵ�·����");
		//			String path0 = s.nextLine();
		//			File f1 = new File(path0);
		//			String[] ss = getAllFiles(path0);
		//			Scanner s1 = new Scanner(System.in);
		//			System.out.println("�����ȡ���Ļ��߰汾������Ҫ�����·���Լ��ļ���.txt��");
		//			String path1 = s1.nextLine();			
		//			output(ss,path1);
		//    	    XML.readxml1();

	}
}
/*
 * XML�������Ҫ���ܾ��Ƕ�ȡ���߰汾�������ļ�.xml��������ļ�����Ҫ��ȡ���߰汾�������磺��SDK5.2�����ͻ��߰汾����ʱ�䡣
 * */
class XML
{
	public static String[] readxml1(File file)
	{
		Element element = null;
		String[] V = new String[2];
		//File file = new File("D:\\sobey_intern\\ȫ�汾\\ȫ�汾\\Cutlist SDK 5.2.2\\ReleaseDescription.xml");
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
					//System.out.println("�汾��  " +node2.getAttributes().getNamedItem("version").getNodeValue());
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

