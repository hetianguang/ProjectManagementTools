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
 * ������������Ŀ�汾�����ߵ�Դ��
 * �������ܣ���ȡ�°油����(����.xml�ļ�)�ڵ��ļ���dll�ȣ���Ϣ�Լ������������������Ϣ���������ļ������ļ�·�����ļ��汾���ļ�����޸�ʱ��ȡ�
 * �����������������Ϣ��ͨ�����ڵ�.xml�ļ���ȡ�ģ���Ϊ���࣬һ��Ϊ�������ԣ��磺�����֣�������ʱ�䣬�����ͣ�BUGID�ȣ�һ��Ϊ����˵����Ϣ������
 * ����������⣬�����ԭ���˵���ֶΡ�
 * ���ߣ������
 * ʱ�䣺2016/5/12������
 * */
public class newPatch_file_Queue {
	public static String[] getAllFiles(String path) throws IOException
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //���ʱ��ĸ�ʽ
		Queue<File> queue = new Queue<File>();//����һ������
		File dir = new File(path);
		File[] files = dir.listFiles(); //��Ŀ¼���е�ǰ�ļ��к��ļ��ı���
		MD5 hash = new MD5(); 
		String[] Version = null;
		String PM =null;
		Vector<String> V = new Vector<String>();
		Vector<String> V1 = new Vector<String>();
		for (File file : files)
		{
			if(file.isDirectory())
			{
				queue.add(file);  //����Ŀ¼�洢��������
			}else{
				if(file.getName().endsWith("xml"))
				{
					if(newPatch_XML.readxml1(file)!=null)   //��ȡ.xml�ļ�������
					{
						Version = newPatch_XML.readxml1(file);
						PM=Version[0];
						V1.add(Version[1]);    
						continue;
					}
				}
				if(file.getName().endsWith("txt"))   //����.txt�ļ�ֱ����������Ϊtxt�ļ��������Ѿ���xml�ļ��д����ˡ�
				{
					continue;
				}
				String s1 = file.getName();  // �ļ���
				String s2 = file.getPath();  //�ļ�·��
				String s3 = formatter.format(new Date(file.lastModified())); //�ļ�����޸�ʱ��
				String s4 = File_info.getVersion(file);// �ļ��汾��
				String s5 = hash.getMD5ofStr(s1+s2+s3+s4);  //MD5�룬����Ϊ�м�
				String S0 = PM+","+s1+","+s2+","+s3+","+s4+","+s5;
				V.add(S0);
			}
		}
		//��������
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
					if(subFile.getName().endsWith("xml"))   //��ȡ.xml�ļ�������
					{
						if(newPatch_XML.readxml1(subFile)!=null)
						{
							Version = newPatch_XML.readxml1(subFile);
							V1.add(Version[1]);
							PM = Version[0];
							continue;
						}
					}
					if(subFile.getName().endsWith("txt"))   //����.txt�ļ�ֱ����������Ϊtxt�ļ��������Ѿ���xml�ļ��д����ˡ�
					{
						continue;
					}
					String s0 = subFile.getName();  // �ļ���
					String s1 = subFile.getPath();   //�ļ�·��
					String s2 = formatter.format(new Date(subFile.lastModified()));  //�ļ�����޸�ʱ��
					String s3 = File_info.getVersion(subFile);  // �ļ��汾��
					String s4 = hash.getMD5ofStr(s0+s1+s2+s3);  //MD5�룬����Ϊ�м�
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
	 * ����ȡ������Ϣ����txt�ļ�
	 * */
	public static void output(String[] ss) throws IOException
	{
		Scanner s = new Scanner(System.in);
		System.out.println("������Ҫ�����ȡ���������ļ���Ϣ�����ݵ��ļ�·���Լ��ļ���.txt��");
		String path = s.nextLine();
		File file = new File(path);
		FileWriter out = new FileWriter(file);
		out.write("��������"+","+"����������"+","+"������·��"+","+"��������޸�ʱ��"+","+"�����汾"+","+"������MD5��"+","+"\r\n");
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
		System.out.println("������Ҫ�����ȡ���������Ĳ���˵���ĵ������ݵ��ļ�·���Լ��ļ���.txt��");
		String path = s.nextLine();
		File file = new File(path);
		FileWriter out = new FileWriter(file);
		out.write("��������"+","+"��������"+","+"����ʱ��"+","+"BUG����"+","+"bugID"+","+"SDK�汾"+","+"Ӧ�ò�汾"+","+"�ṩ��"+","+"������"+","+"��Դ"+","+"˵��"+","+"\r\n");
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
		System.out.println("����·����");
		String path = s.nextLine();
		String[] ss = getAllFiles(path);
		output(ss);
	}
}
/*
 * ����xml�ļ��ĸ�ʽ�����ж�ȡ���ݣ���ȡ��������Ҫ�ǲ����������Ժ�˵����Ϣ��
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
		// File file = new File("D:\\sobey_intern\\��ʽ����\\test\\20151021��ʽ����(���)\\ReleaseDescriptiontestFP.xml");
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
						System.out.println("���ͣ�  " +node2.getAttributes().getNamedItem("Type").getNodeValue());
						V.add(node2.getAttributes().getNamedItem("Type").getNodeValue());
						NodeList nodeNext = node2.getChildNodes();
						for(int j=0;j<nodeNext.getLength();j++)
						{
							Node node3 = nodeNext.item(j);
							if("Patch".equals(node3.getNodeName()))
							{
								System.out.println("���������֣�  " +node3.getAttributes().getNamedItem("Name").getNodeValue());
								V.add(node3.getAttributes().getNamedItem("Name").getNodeValue());
								System.out.println("������ʱ�䣺  " +node3.getAttributes().getNamedItem("Time").getNodeValue());
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
											T = T +" "+"���⣺"+node5.getTextContent();
										}
										if("Cause".equals(node5.getNodeName()))
										{
											System.out.println("Cause: "+ node5.getTextContent());
											T = T +" "+"����ԭ��"+node5.getTextContent();
										}
										if("HowToFixed".equals(node5.getNodeName()))
										{
											System.out.println("HowToFixed: "+ node5.getTextContent());
											T = T +" "+"���������"+node5.getTextContent();
										}
										if("InfluenceRange".equals(node5.getNodeName()))
										{
											System.out.println("InfluenceRange: "+ node5.getTextContent());
											T = T +" "+"Ӱ�죺"+node5.getTextContent();
										}
										if("instructions".equals(node5.getNodeName()))
										{
											System.out.println("instructions: "+ node5.getTextContent());
											T = T +" "+"˵����"+node5.getTextContent();
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
