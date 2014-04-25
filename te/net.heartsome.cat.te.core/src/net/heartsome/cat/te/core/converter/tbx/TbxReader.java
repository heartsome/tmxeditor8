/**
 * TbxReader.java
 *
 * Version information :
 *
 * Date:2013-7-1
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.converter.tbx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.document.DocUtils;
import net.heartsome.xml.vtdimpl.VTDUtils;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class TbxReader {

	/**
	 * tbx文件路径
	 */
	private String filePath;

	private VTDUtils vtdUtil;

	private AutoPilot tuAp;

	/**
	 * tbx的源语言
	 */
	private String srcLang;

	private VTDNav vn;
	/**
	 * 当前termEntry的位置
	 */
	private int currentPostion = 0;
	/**
	 * termEntry的总个数
	 */
	private int termEntryCount;

	public String getSrcLang() {
		return srcLang;
	}

	public TbxReader(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * 读取下一个TermEntry
	 * @return
	 * @throws Exception
	 *             ;
	 */
	public TmxTU[] readNextTermEntry() throws Exception {
		if (tuAp.evalXPath() != -1) {
			++currentPostion;
			return readNext();
		}
		return new TmxTU[0];
	}

	/**
	 * 是否到达文件的结束
	 * @return ;
	 */
	public boolean hasNext() {
		return currentPostion != termEntryCount;
	}

	/**
	 * 调用reader前，应该先初始化相关数据
	 * @return
	 * @throws NavException
	 * @throws ParseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 *             ;
	 */
	boolean start() throws Exception {
		vtdUtil = new VTDUtils();
		File file = new File(filePath);
		vtdUtil.parseFile(file, false);
		if (validate()) {
			vn = vtdUtil.getVTDNav();
			int pilot = vtdUtil.pilot("/martif");
			if (-1 != pilot) {
				int attrIndex = vn.getAttrVal("xml:lang");
				if (-1 != attrIndex) {
					setSrcLang(vn.toString(attrIndex));
					setTermEntryCount();
				}
			}
			tuAp = new AutoPilot(vn);
			tuAp.selectXPath("/martif/text/body/termEntry");
			return true;
		}
		return false;
	}

	/**
	 * 验证TBX的文件格式是否正确
	 * @return ;
	 */
	private boolean validate() {
		// has header
		if (-1 == vtdUtil.pilot("/martif/martifHeader")) {
			return false;
		}
		;
		// has body
		if (-1 == vtdUtil.pilot("/martif/text/body/termEntry/langSet//term")) {
			return false;
		}

		return true;
	}

	/**
	 * 设置源语言
	 * @param srcLang
	 *            ;
	 */
	private void setSrcLang(String srcLang) {
		this.srcLang = srcLang;
	}

	/**
	 * 设置termEntry的总个数
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 * @throws NavException
	 *             ;
	 */
	private void setTermEntryCount() throws XPathParseException, XPathEvalException, NavException {
		tuAp = new AutoPilot(vn);
		tuAp.selectXPath("/martif/text/body/termEntry");
		while (tuAp.evalXPath() != -1) {
			termEntryCount++;
		}

	}

	/**
	 * 获得/langset/tig/term格式的TmxTU,这个格式一般只对应一个TmxTU
	 * @return
	 * @throws Exception
	 *             ;
	 */
	private TmxTU[] readTuByTig() throws Exception {
		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./langSet");
		AutoPilot childAp = null;
		TmxTU[] tus = null;
		String tempLang = null;
		TmxTU tu = new TmxTU();
		VTDUtils vu = new VTDUtils(vn);
		while (ap.evalXPath() != -1) {
			tempLang = getAttValue("xml:lang");
			childAp = new AutoPilot(vn);
			childAp.selectXPath("./tig/term");
			TmxSegement seg = null;
			vn.push();
			while (childAp.evalXPath() != -1) {
				seg = new TmxSegement();
				seg.setFullText(vtdUtil.getElementContent());
				seg.setPureText(DocUtils.getTmxTbxPureText(vu));
				seg.setLangCode(tempLang);
			}
			vn.pop();
			if(null != seg){				
				tu.appendSegement(seg);
			}
		}
		tus = new TmxTU[1];
		tus[0] = tu;
		vn.pop();
		return tus;
	}

	/**
	 * 读取两种格式的TmxTU
	 * @return
	 * @throws Exception
	 *             ;
	 */
	private TmxTU[] readNext() throws Exception {
		TmxTU[] rs = null;
		rs = readTuByTig();
		if (rs != null && rs.length != 0) {
			return rs;
		}
		return readTuByNtig();
	}

	/**
	 * 将<code>xpath:/langset/ntig/termGrp/term</code>下面的术语解析成TmxTU<br/>
	 * 这个格式的术语文件，可能返回多个TmxTU<br/>
	 * @see TbxReader#getTmxTus(List)
	 * @return
	 * @throws Exception
	 *             ;
	 */

	private TmxTU[] readTuByNtig() throws Exception {
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./langSet");
		AutoPilot childAp = null;
		String tempLang = null;
		List<TmxSegement> oneGroupSegs = null;
		List<List<TmxSegement>> rs = new ArrayList<List<TmxSegement>>();
		VTDUtils vu = new VTDUtils(vn);
		vn.push();
		while (ap.evalXPath() != -1) {
			tempLang = getAttValue("xml:lang");
			childAp = new AutoPilot(vn);
			childAp.selectXPath("./ntig/termGrp/term");
			oneGroupSegs = new ArrayList<TmxSegement>();
			TmxSegement seg = null;
			vn.push();
			while (childAp.evalXPath() != -1) {
				seg = new TmxSegement();
				seg.setFullText(vtdUtil.getElementContent());
				seg.setPureText(DocUtils.getTmxTbxPureText(vu));
				seg.setLangCode(tempLang);
				oneGroupSegs.add(seg);
			}
			vn.pop();
			rs.add(oneGroupSegs);
		}
		vn.pop();
		if (rs.isEmpty()) {
			return new TmxTU[0];
		}
		return getTmxTus(rs);
	}

	private String getAttValue(String name) throws NavException {
		int langIndex = vn.getAttrVal(name);
		if (langIndex != -1) {
			return vn.toString(langIndex);
		}
		return null;
	}

	/**
	 * 求术语多对多的笛卡尔乘积
	 * @param param
	 * @return ;
	 */
	private TmxTU[] getTmxTus(List<List<TmxSegement>> param) {

		List<List<TmxSegement>> rs = new ArrayList<List<TmxSegement>>();
		rs.add(param.get(0));

		List<List<TmxSegement>> temp = new ArrayList<List<TmxSegement>>();
		List<TmxSegement> group = null;
		for (int i = 1; i < param.size(); i++) {
			temp.clear();
			// 一个组
			for (TmxSegement paramSeg : param.get(i)) {

				for (List<TmxSegement> list : rs) {
					// 初始化
					if (rs.size() == 1) {
						for (TmxSegement seg : list) {
							group = new ArrayList<TmxSegement>();
							group.add(paramSeg);
							group.add(seg);
							temp.add(group);
						}
					} else {
						group = new ArrayList<TmxSegement>();
						group.add(paramSeg);
						group.addAll(list);
						temp.add(group);
					}

				}
			}

			rs.clear();
			rs.addAll(temp);

		}
		List<TmxTU> tus = new ArrayList<TmxTU>();
		TmxTU tempTU = null;

		for (List<TmxSegement> list : rs) {
			tempTU = new TmxTU();
			for (TmxSegement tuv : list) {
				tempTU.appendSegement(tuv);
			}
			tus.add(tempTU);
		}
		return tus.toArray(new TmxTU[tus.size()]);
	}

	public int getTotal() {
		return termEntryCount;
	}

}
