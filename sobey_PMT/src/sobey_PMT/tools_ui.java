package sobey_PMT;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wb.swt.SWTResourceManager;

import file_operation.FileInfoToHbase;
import file_operation.HbaseOperate;
import file_operation.PatchInfoToHbase;
import file_operation.PatchSerach;
import file_operation.filecheck;
import file_operation.newPatchInfoToHbase;
import sobey_readfile.Base_file_Queue;
import sobey_readfile.Patch_file_Queue;
import sobey_readfile.newPatch_file_Queue;



/*
 * 本程序属于项目版本管理工具的源码
 * 本程序功能：生成窗口app，方便用户进行操作
 * 
 * 作者：何天光
 * 时间：2016/5/12（整理）
 * */	

public class tools_ui {

	protected Shell shell;
	private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());
	private Text text_1;
	private Text text;
	private Text text_2;
	private Text text_3;
	private Text text_5;
	/**
	 * Launch the application.
	 * @param args
	 */
	/*
	 * 将某些结果备份到指定的txt文件中
	 */
	public static void output(String ss,String path) throws IOException
    {
 	   File file = new File(path);
 	   FileWriter out = new FileWriter(file);
 	  out.write(ss);
 	   out.close();
    }
	public static void main(String[] args) {
		try {
			tools_ui window = new tools_ui();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 * @throws Exception 
	 */
	public void open() throws Exception {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
			
		}
	}

	/**
	 * Create contents of the window.
	 * @throws Exception 
	 */
	protected void createContents() throws Exception {
		shell = new Shell();
		shell.setSize(565, 465);
		shell.setText("\u9879\u76EE\u6587\u4EF6\u7248\u672C\u7BA1\u7406\u5DE5\u5177");
		
		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setBounds(0, 61, 544, 43);
		CCombo combo = new CCombo(composite_1, SWT.BORDER);
		combo.setText("\u9879\u76EE\u7C7B\u578B");
		combo.setItems(new String[] {"\u57FA\u7EBF\u7248\u672C\u6587\u4EF6\u4FE1\u606F", "\u8865\u4E01\u5305\u6587\u4EF6\u4FE1\u606F", "\u65E7\u8865\u4E01\u5305\u6587\u4EF6\u4FE1\u606F"});
		combo.setBounds(10, 10, 141, 23);
		
		CCombo combo_3 = new CCombo(shell, SWT.BORDER);
		combo_3.setItems(new String[] {"172.16.133.23"});
		combo_3.setBounds(196, 13, 159, 23);
		formToolkit.adapt(combo_3);
		formToolkit.paintBordersFor(combo_3);
		
		Button btnNewButton = formToolkit.createButton(composite_1, "ok", SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(combo_3.getText().length()==0)
				{
					JOptionPane.showMessageDialog(null, "请输入hbase服务器IP地址","提示", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				if("基线版本文件信息".equals(combo.getText()))
				{
					try {
						FileInfoToHbase fith = new FileInfoToHbase(combo_3.getText());
						String rt = fith.createTable("FileInfo","VersionInfo","File_Info","V_Info");
						if("表已存在".equals(rt))
						{
							text_3.setText(rt+"ddd");
							
						}
						else
							text_3.setText(rt);
						String path = text_1.getText();
						Base_file_Queue bfq = new Base_file_Queue();
						String[] data = bfq.getAllFiles(path);
						System.out.println(data[0].split(",").length);
						int cnt = fith.datainput("FileInfo","VersionInfo", data);
						text_3.setText("数据录入完毕：一共"+String.valueOf(cnt)+"行");
					} catch (Exception e1) {
						text_3.setText("出错啦.....");
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				if("补丁包文件信息".equals(combo.getText()))
				{
					try {
						newPatchInfoToHbase newpith = new newPatchInfoToHbase(combo_3.getText());
						String rt = newpith.createTable("newPatchInfo","newPatch_Info","newPatch_illustrate","Patch_Info_property","Patch_Info_describe");
						if("表已存在".equals(rt))
						{
							text_3.setText(rt);
						}
						else
							text_3.setText(rt);
						String path = text_1.getText();
						newPatch_file_Queue newpfq = new newPatch_file_Queue();
						String[] data = newpfq.getAllFiles(path);
						int cnt = newpith.datainput("newPatchInfo","newPatch_illustrate", data);
						text_3.setText("数据录入完毕：一共"+String.valueOf(cnt)+"行");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						text_3.setText("出错啦.....");
						e1.printStackTrace();
						text_3.setText(e1.toString());
					}
				}
				if("旧补丁包文件信息".equals(combo.getText()))
				{
					try {
						PatchInfoToHbase pith = new PatchInfoToHbase(combo_3.getText());
						String rt = pith.createTable("PatchInfo","Patch_Info","Path_illustrate","Patch_ILL");
						if("表已存在".equals(rt))
						{
							text_3.setText(rt);
						}
						else
							text_3.setText(rt);
						String path = text_1.getText();
						Patch_file_Queue pfq = new Patch_file_Queue();
						String[] data = pfq.getAllFiles(path);
						System.out.println(data[0].split(",").length);
						int cnt = pith.datainput("PatchInfo","Path_illustrate", data);
						text_3.setText("数据录入完毕：一共"+String.valueOf(cnt)+"行");
					} catch (Exception e1) {
						text_3.setText("出错啦.....");
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		btnNewButton.setBounds(480, 10, 54, 27);
		
		text_1 = new Text(composite_1, SWT.BORDER);
		text_1.setBounds(217, 10, 184, 25);
		formToolkit.adapt(text_1, true, true);
		
		CLabel label = new CLabel(composite_1, SWT.NONE);
		label.setBounds(157, 10, 54, 23);
		label.setText("\u6587\u4EF6\u8DEF\u5F84");
		formToolkit.adapt(label);
		formToolkit.paintBordersFor(label);
		
		Button btnNewButton_3 = new Button(composite_1, SWT.NONE);
		btnNewButton_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dd=new DirectoryDialog(shell);
				 dd.setMessage("setMessage");
				 dd.setText("setText");
				 dd.setFilterPath("C://");
				 if(dd.open()==null)
				 {
					 return;
				 }
				 text_1.setText(dd.open());
			}
		});
		btnNewButton_3.setBounds(407, 6, 54, 27);
		formToolkit.adapt(btnNewButton_3, true, true);
		btnNewButton_3.setText("\u9009\u62E9");
		
		Composite composite_2 = new Composite(shell, SWT.NONE);
		composite_2.setBounds(0, 128, 544, 43);
		
		text = new Text(composite_2, SWT.BORDER);
		text.setBounds(112, 12, 267, 25);
		formToolkit.adapt(text, true, true);
		Button button = new Button(composite_2, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dd=new DirectoryDialog(shell);
				 dd.setMessage("setMessage");
				 dd.setText("setText");
				 dd.setFilterPath("C://");
				 if(dd.open()==null)
				 {
					 return;
				 }
				 text.setText(dd.open());
			}
		});
		Button btnOk = new Button(composite_2, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(combo_3.getText().length()==0)
				{
					JOptionPane.showMessageDialog(null, "请输入hbase服务器IP地址","提示", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				try {
					filecheck fc = new filecheck(combo_3.getText());
					String path = text.getText();
					Base_file_Queue bfq = new Base_file_Queue();
					String[] data = bfq.getAllFiles(text.getText());
					String result = fc.start_check(data,combo_3.getText());
					text_3.setText("结果："+ result);
					output(result,"./result.txt");
				} catch (Exception e1) {
					text_3.setText("出错啦.....");
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//text_3.setText(result);
			}
		});
		btnOk.setBounds(486, 10, 48, 27);
		formToolkit.adapt(btnOk, true, true);
		btnOk.setText("ok");
		
		CLabel label_6 = new CLabel(composite_2, SWT.NONE);
		label_6.setText("\u57FA\u7EBF\u7248\u672C\u8DEF\u5F84");
		label_6.setBounds(10, 10, 80, 23);
		formToolkit.adapt(label_6);
		formToolkit.paintBordersFor(label_6);
		
		
		button.setText("\u9009\u62E9");
		button.setBounds(412, 10, 54, 27);
		formToolkit.adapt(button, true, true);
		
		CLabel label_1 = new CLabel(shell, SWT.NONE);
		label_1.setText("\u57FA\u7EBF\u7248\u672C\u8BC6\u522B");
		label_1.setBounds(240, 110, 84, 23);
		formToolkit.adapt(label_1);
		formToolkit.paintBordersFor(label_1);
		
		CLabel label_2 = new CLabel(shell, SWT.NONE);
		label_2.setText("\u9879\u76EE\u4FE1\u606F\u5165\u5E93");
		label_2.setBounds(240, 42, 84, 23);
		formToolkit.adapt(label_2);
		formToolkit.paintBordersFor(label_2);
		
		CLabel label_3 = new CLabel(shell, SWT.NONE);
		label_3.setText("\u8865\u4E01\u4FE1\u606F\u67E5\u8BE2");
		label_3.setBounds(240, 177, 84, 23);
		formToolkit.adapt(label_3);
		formToolkit.paintBordersFor(label_3);
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBounds(0, 195, 544, 43);
		formToolkit.adapt(composite);
		formToolkit.paintBordersFor(composite);
		
		CCombo combo_1 = new CCombo(composite, SWT.BORDER);
		combo_1.setText("\u9879\u76EE\u7C7B\u578B");
		combo_1.setItems(new String[] {"\u8865\u4E01\u5305\u540D", "\u8865\u4E01\u65F6\u95F4", "BUG\u7C7B\u578B", "bugID", "\u7528\u4E8ESDK\u7248\u672C", "\u5E94\u7528\u5C42\u7248\u672C", "\u63D0\u4F9B\u4EBA", "\u63A5\u53D7\u4EBA", "\u8865\u4E01\u6765\u6E90", "\u65B0\u7248\u8BF4\u660E\u6A21\u7CCA\u67E5\u8BE2", "\u8865\u4E01\u6587\u4EF6", "\u65E7\u7248\u8BF4\u660E\u6A21\u7CCA\u67E5\u8BE2"});
		combo_1.setBounds(10, 10, 150, 23);
		formToolkit.adapt(combo_1);
		formToolkit.paintBordersFor(combo_1);
		
		CLabel label_4 = new CLabel(composite, SWT.NONE);
		label_4.setText("\u5173\u952E\u5B57");
		label_4.setBounds(166, 10, 42, 23);
		formToolkit.adapt(label_4);
		formToolkit.paintBordersFor(label_4);
		
		text_2 = new Text(composite, SWT.BORDER);
		text_2.setBounds(214, 10, 198, 25);
		formToolkit.adapt(text_2, true, true);
		
		Button btnOk_1 = new Button(composite, SWT.NONE);
		btnOk_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(combo_3.getText().length()==0)
				{
					JOptionPane.showMessageDialog(null, "请输入hbase服务器IP地址","提示", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				try {
					PatchSerach ps = new PatchSerach(combo_3.getText());
					if("补丁时间".equals(combo_1.getText()))
					{
						String result = ps.newPatch_Info_property("P_Time", text_2.getText());
						text_3.setText(result);
						output(result,"./result.txt");
					}
					if("BUG类型".equals(combo_1.getText()))
					{
						String result = ps.newPatch_Info_property("BUG_kind", text_2.getText());
						text_3.setText(result);
						output(result,"./result.txt");
					}
					if("补丁包名".equals(combo_1.getText()))
					{
						String result = ps.newPatch_Info_property("P_Name", text_2.getText());
						text_3.setText(result);
						output(result,"./result.txt");
					}
					if("bugID".equals(combo_1.getText()))
					{
						String result = ps.newPatch_Info_property("BUG_ID", text_2.getText());
						text_3.setText(result);
						output(result,"./result.txt");
					}
					if("用于SDK版本".equals(combo_1.getText()))
					{
						String result = ps.newPatch_Info_property("SDK_Version", text_2.getText());
						text_3.setText(result);
						output(result,"./result.txt");
					}
					if("应用层版本".equals(combo_1.getText()))
					{
						String result = ps.newPatch_Info_property("APP_Version", text_2.getText());
						text_3.setText(result);
						output(result,"./result.txt");
					}
					if("提供人".equals(combo_1.getText()))
					{
						String result = ps.newPatch_Info_property("Provider", text_2.getText());
						text_3.setText(result);
						output(result,"./result.txt");
					}
					if("接受人".equals(combo_1.getText()))
					{
						String result = ps.newPatch_Info_property("Recipient", text_2.getText());
						text_3.setText(result);
						output(result,"./result.txt");
					}
					if("补丁来源".equals(combo_1.getText()))
					{
						String result = ps.newPatch_Info_property("Source", text_2.getText());
						text_3.setText(result);
						output(result,"./result.txt");
					}
					if("新版说明模糊查询".equals(combo_1.getText()))
					{
						String result = ps.newPatch_Info_describe("Illustrate", text_2.getText());
						text_3.setText(result);
						output(result,"./result.txt");
					}
					if("旧版说明模糊查询".equals(combo_1.getText()))
					{
						String result = ps.oldPatch_ILL("document", text_2.getText());
						text_3.setText(result);
						output(result,"./result.txt");
					}
					if("补丁文件".equals(combo_1.getText()))
					{
						String result = ps.dllFile(text_2.getText());
						text_3.setText(result);
						output(result,"./result.txt");
					}
				} catch (Exception e1) {
					text_3.setText("出错啦....");
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnOk_1.setText("ok");
		btnOk_1.setBounds(486, 10, 48, 27);
		formToolkit.adapt(btnOk_1, true, true);
		
		text_3 = new Text(shell, SWT.WRAP|SWT.V_SCROLL);
		text_3.setFont(SWTResourceManager.getFont("微软雅黑", 8, SWT.NORMAL));
		text_3.setBounds(0, 261, 400, 156);
		formToolkit.adapt(text_3, true, true);
		CLabel label_5 = new CLabel(shell, SWT.NONE);
		label_5.setFont(SWTResourceManager.getFont("微软雅黑", 8, SWT.NORMAL));
		label_5.setText("\u7ED3\u679C\u663E\u793A");
		label_5.setBounds(0, 244, 62, 11);
		formToolkit.adapt(label_5);
		formToolkit.paintBordersFor(label_5);
		
		CLabel lblHbaseip = new CLabel(shell, SWT.NONE);
		lblHbaseip.setText("IP\u5730\u5740");
		lblHbaseip.setBounds(129, 10, 50, 23);
		formToolkit.adapt(lblHbaseip);
		formToolkit.paintBordersFor(lblHbaseip);
		
		CLabel lblHbase = new CLabel(shell, SWT.NONE);
		lblHbase.setFont(SWTResourceManager.getFont("微软雅黑", 8, SWT.NORMAL));
		lblHbase.setText("hbase\u6D4B\u8BD5");
		lblHbase.setBounds(442, 244, 65, 21);
		formToolkit.adapt(lblHbase);
		formToolkit.paintBordersFor(lblHbase);
		
		CCombo combo_2 = new CCombo(shell, SWT.BORDER);
		combo_2.setText("\u9009\u62E9");
		combo_2.setItems(new String[] {"\u6240\u6709\u8868", "\u5220\u9664\u8868", "\u8868\u5185\u5BB9", "\u5220\u9664\u6240\u6709\u8868", "\u65B0\u7248\u8865\u4E01\u4E2A\u6570", "\u65E7\u7248\u8865\u4E01\u4E2A\u6570"});
		combo_2.setBounds(406, 272, 87, 23);
		formToolkit.adapt(combo_2);
		formToolkit.paintBordersFor(combo_2);
		
		text_5 = new Text(shell, SWT.BORDER);
		text_5.setBounds(406, 301, 138, 23);
		formToolkit.adapt(text_5, true, true);
		
		Button btnNewButton_1 = new Button(shell, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(combo_3.getText().length()==0)
				{
					JOptionPane.showMessageDialog(null, "请输入hbase服务器IP地址","提示", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				try {
					if("删除表".equals(combo_2.getText()))
					{
						
						HbaseOperate ho =new HbaseOperate(combo_3.getText());
					    String result = ho.deleteTable(text_5.getText());
					    text_3.setText("已删除："+result);
					}
					if("所有表".equals(combo_2.getText()))
					{
						String result = "";
						HbaseOperate ho =new HbaseOperate(combo_3.getText());
						List list = ho.getAllTables();
						for(int i=0 ;i<list.size();i++)
							result = result + list.get(i) + "\r\n";
						text_3.setText("表："+result);
					}
					if("表内容".equals(combo_2.getText()))
					{
						String result = "";
						HbaseOperate ho =new HbaseOperate(combo_3.getText());
						ArrayList<String> al = ho.getAllData(text_5.getText());
						for(int i=0 ;i<al.size();i++)
							result = result + al.get(i) + "\r\n";
						text_3.setText("信息："+result);
						output(result,"./result.txt");
						System.out.println(result);
					}
					if("删除所有表".equals(combo_2.getText()))
					{
						String result = "";
						HbaseOperate ho =new HbaseOperate(combo_3.getText());
						result = ho.getDelAllTables();
						text_3.setText(result);
					}
					if("新版补丁个数".equals(combo_2.getText()))
					{
						String result = "";
						HbaseOperate ho =new HbaseOperate(combo_3.getText());
						result = ho.newPatchCount();
						text_3.setText(result);
						output(result,"./result.txt");
					}
					if("旧版补丁个数".equals(combo_2.getText()))
					{
						String result = "";
						HbaseOperate ho =new HbaseOperate(combo_3.getText());
						result = ho.oldPatchCount();
						text_3.setText(result);
					}
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					text_3.setText("出错啦.....");
				}
			}
		});
		btnNewButton_1.setBounds(499, 271, 50, 27);
		formToolkit.adapt(btnNewButton_1, true, true);
		btnNewButton_1.setText("ok");
		
		Button btnNewButton_2 = new Button(shell, SWT.NONE);
		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				text_1.setText("");
				text_2.setText("");
				text_3.setText("");
				text_5.setText("");
				text.setText("");
			}
		});
		btnNewButton_2.setBounds(406, 360, 138, 27);
		formToolkit.adapt(btnNewButton_2, true, true);
		btnNewButton_2.setText("\u6E05\u9664\u6240\u6709\u6587\u672C\u6846\u4FE1\u606F");
		
		

	}
}
