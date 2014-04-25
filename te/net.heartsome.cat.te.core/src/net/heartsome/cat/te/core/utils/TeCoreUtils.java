/**
 * TeCoreUtils.java
 *
 * Version information :
 *
 * Date:2013/5/17
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */

package net.heartsome.cat.te.core.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.innertag.TmxInnerTagParser;
import net.heartsome.cat.te.core.TeCoreConstant;
import net.heartsome.cat.te.core.tmxdata.TmxLargeFileContainer;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;

public final class TeCoreUtils {

	public static final int OS_LINUX = 1;
	public static final int OS_MAC = 2;
	public static final int OS_WINDOWS = 3;

	public static void validateTmxFile(String tmxfile) {

	}

	public static void deleteFolder(File dir) {
		File filelist[] = dir.listFiles();
		int listlen = filelist.length;
		for (int i = 0; i < listlen; i++) {
			if (filelist[i].isDirectory()) {
				deleteFolder(filelist[i]);
			} else {
				filelist[i].delete();
			}
		}
		dir.delete();// 删除当前目录
	}

	/**
	 * 生成索引文件，即将符合标准的 tu 取出其纯文本，放置到一个新的临时文件中去 robert 2013-07-17 <div style="color:red">该方法调用完后，必删除临时文件</div>
	 * 修改：yule.向添加临时文件中，添加TU的最新修改时间
	 * @param container
	 * @param srcLang
	 * @param tgtLang
	 * @param ignoreTag
	 *            是否忽略标记
	 * @return ;
	 */
	public static String createIndexFile(IProgressMonitor monitor, TmxLargeFileContainer container, String srcLang,
			String tgtLang, boolean ignoreTag) {
		long time1 = System.currentTimeMillis();

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", container.getSubFiles().size());

		srcLang = srcLang.toLowerCase();
		tgtLang = tgtLang.toLowerCase();

		TmxInnerTagParser parser = TmxInnerTagParser.getInstance();
		FileOutputStream output = null;
		BufferedOutputStream buffer = null;

		String tempFileLC = null;
		try {
			tempFileLC = File.createTempFile("tmxEditorIndexFile", ".tmx").getAbsolutePath();
			VTDUtils vu = null;
			VTDNav vn = null;
			AutoPilot ap = new AutoPilot();

			output = new FileOutputStream(tempFileLC);
			buffer = new BufferedOutputStream(output);

			buffer.write("<tus>\n".getBytes("UTF-8"));

			int subFileINdex = 0;
			for (String subFile : container.getSubFiles()) {
				StringBuffer sb = new StringBuffer();
				long time2 = System.currentTimeMillis();
				vu = container.getVTDUtils(subFile);
				vn = vu.getVTDNav();
				vn.push();
				ap.bind(vn);
				ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);

				String tuXpath = "/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang
						+ "'] and tuv[lower-case(@xml:lang)='" + tgtLang + "']]/tuv[lower-case(@xml:lang)='" + srcLang
						+ "']/seg";
				ap.selectXPath(tuXpath);

				String text = "";
				String hsid = "";
				String id = "";
				int index = -1;
				Map<String, String> srcMap = new LinkedHashMap<String, String>();
				while (ap.evalXPath() != -1) {
					vn.push();
					vn.toElement(VTDNav.PARENT);
					vn.toElement(VTDNav.PARENT);
					if ((index = vn.getAttrVal("hsid")) != -1) {
						hsid = vn.toString(index);
					}
					vn.pop();
					if (index == -1) {
						continue;
					}
					// 为是节省索引文件的空间，这里使用　序列号的方式代替　subFile loction 的方式来处理。
					id = subFileINdex + TeCoreConstant.ID_MARK + hsid;

					text = vu.getElementContent();
					if (text == null) {
						text = "";
					}
					if (ignoreTag) {
						text = parser.getTmxPureText(text);
						text = cleanSpecialChar(text);
					} else {
						text = cleanSpecialChar(text);
					}
					srcMap.put(id, text);
				}

				tuXpath = "/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang + "'] and tuv[lower-case(@xml:lang)='"
						+ tgtLang + "']]/tuv[lower-case(@xml:lang)='" + tgtLang + "']/seg";
				ap.selectXPath(tuXpath);
				String lastTuModifyTime = "NULL";
				while (ap.evalXPath() != -1) {
					vn.push();
					vn.toElement(VTDNav.PARENT);
					vn.toElement(VTDNav.PARENT);
					if ((index = vn.getAttrVal("hsid")) != -1) {
						hsid = vn.toString(index);
					}
					// update by yule 2013-8-16
					int tempIndex = vn.getAttrVal("changedate");
					if (-1 != tempIndex) {
						lastTuModifyTime = vn.toString(tempIndex);
					} else {
						lastTuModifyTime = "NULL";
					}

					vn.pop();
					if (index == -1) {
						continue;
					}
					id = subFileINdex + TeCoreConstant.ID_MARK + hsid;

					text = vu.getElementContent();
					if (text == null) {
						text = "";
					}
					if (ignoreTag) {
						text = parser.getTmxPureText(text);
						text = cleanSpecialChar(text);
					} else {
						text = cleanSpecialChar(text);
					}
					// update by yule 2013-8-16
					sb.append("<tu id=\"").append(id).append("\" src=\"").append(srcMap.get(id)).append("\" tgt=\"")
							.append(text).append("\" changedate=\"").append(lastTuModifyTime).append("\"/>\n");
					buffer.write(sb.toString().getBytes("UTF-8"));
					sb = new StringBuffer();
					srcMap.remove(id);
				}

				vn.pop();
				subFileINdex++;
				System.out.println(System.currentTimeMillis() - time2);

				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				monitor.worked(1);
			}
			buffer.write("</tus>".getBytes("UTF-8"));
			buffer.flush();

			System.out.println("生成索引文件所花时间 = " + (System.currentTimeMillis() - time1));
			return tempFileLC;
		} catch (Exception e) {
			e.printStackTrace();
			if (new File(tempFileLC).exists()) {
				new File(tempFileLC).delete();
			}
			return null;
		} finally {
			try {
				if (output != null) {
					output.close();
				}
				if (buffer != null) {
					buffer.close();
				}
				monitor.done();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

	}

	/**
	 * 通过二叉查询方法，从一个装满　tu index 的　list 中获取当前　tu(以index 表示) 的序号 <div style="color:red">由于是获取的　tu 在body 节点中的　position，而
	 * position是从1 开始的，故，此处取回的结果值已经做　加　1　处理 </div> robert 2013-07-17
	 * @param indexList
	 *            是保存所有　tu 节点的　index 的集合
	 * @param index
	 *            当前要查询　position 的　tu 的　index　值
	 * @return ;
	 */
	public static int getTuPosition(List<Integer> indexList, int index) {
		int low = 0;
		int high = indexList.size() - 1;
		while (low <= high) {
			int middle = (low + high) / 2;
			if (index == indexList.get(middle)) {
				return middle + 1; // 备注：加 1　是因为　position 是从 1 开始的
			} else if (index < indexList.get(middle)) {
				high = middle - 1;
			} else {
				low = middle + 1;
			}
		}
		return -1;
	}

	/**
	 * 清除文本中的特殊字符，当前方法只用于当前类,　与类 {@link net.heartsome.cat.common.util.TextUtil#cleanSpecialString(String)}
	 * 效率高出　9　秒左右(一百六十M的文件) robert 2013-07-18
	 * @param text
	 * @return ;
	 */
	private static String cleanSpecialChar(String text) {
		char[] strArray = text.toCharArray();
		StringBuffer sb = new StringBuffer();
		char curChar;
		for (int i = 0; i < strArray.length; i++) {
			curChar = strArray[i];
			if (curChar == '&') {
				sb.append("&amp;");
			} else if (curChar == '<') {
				sb.append("&lt;");
			} else if (curChar == '>') {
				sb.append("&gt;");
			} else if (curChar == '"') {
				sb.append("&quot;");
			} else {
				sb.append(curChar);
			}
		}

		return sb.toString();
	}

	/**
	 * Parse TU identifier
	 * @param identifier
	 * @return two String in array, first is SubFile and next is TU position in SubFile;
	 */
	public static String[] parseTuIndentifier(String identifier) {
		if (identifier == null || identifier.length() < 2) {
			return null;
		}
		String[] strs = identifier.split(TeCoreConstant.ID_MARK);
		if (strs.length != 2) {
			return null;
		}
		return strs;
	}

	/**
	 * 得到当前的操作系统。
	 * @return 操作系统，值为 {@link #OS_LINUX}、{@link #OS_MAC}、{@link #OS_WINDOWS};
	 */
	public static int getCurrentOS() {
		if (System.getProperty("file.separator").equals("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
			// Windows
			return OS_WINDOWS;
		} else if (System.getProperty("user.home").startsWith("/Users")) { //$NON-NLS-1$ //$NON-NLS-2$
			// Mac
			return OS_MAC;
		} else {
			// Linux
			return OS_LINUX;
		}
	}

	/**
	 * 得到文件分隔符
	 * @return 在 UNIX 系统值为 <code>'/'</code>; 在 Windows 系统值为 <code>'\'</code>。
	 */
	public static String getFileSeparator() {
		return System.getProperty("file.separator");
	}

	/**
	 * 得到行分隔符
	 * @return Linux 系统值为 <code>'\n'</code>; Mac 系统值为 <code>'\r'</code>；Windows 系统值为 <code>'\r\n'</code>。
	 */
	public static String getLineSeparator() {
		return System.getProperty("line.separator");
	}

	public static boolean validateTmxContainer(TmxLargeFileContainer conater) {
		return true;
	}
	/**
	 * 关闭　qa 视图，当进行 qa 时再打开	robert	2013-09-27
	 */
	public static void closeQAViewer(){
		String viewId = TeCoreConstant.QAResultViewer_ID;
		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart view = workbenchPage.findView(viewId);
		if (view != null) {
			workbenchPage.hideView(view);
		}
	}
	
}
