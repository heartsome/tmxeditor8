/**
 * TestDiffFileEncoding.java
 *
 * Version information :
 *
 * Date:2013-11-29
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.xml.vtdimpl.test;

import net.heartsome.xml.vtdimpl.VTDUtils;

import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class TestDiffFileEncoding {

	static String utf_16be = "testSrc/net/heartsome/xml/vtdimpl/test/utf-16be.tmx";
	static String utf_16le = "testSrc/net/heartsome/xml/vtdimpl/test/utf-16le.tmx";
	static String utf_8 = "testSrc/net/heartsome/xml/vtdimpl/test/test.tmx";

	/**
	 * @param args
	 *            ;
	 * @throws NavException
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		VTDGen vg = new VTDGen();
		if (vg.parseFile(utf_8, true)) {
			VTDNav vn = vg.getNav();
			try {
				VTDUtils vu = new VTDUtils(vn);
			} catch (NavException e) {
				e.printStackTrace();
			}
		}
	}

}
