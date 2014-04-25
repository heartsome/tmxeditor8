/**
 * TmxTuCache.java
 *
 * Version information :
 *
 * Date:2013-7-8
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.tmxdata;

import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.xml.vtdimpl.VTDUtils;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

/**
 * TmxEditor implement with nattable UI data cache, the initial capacity is 1000;<br>
 * Key： Nattable rowIndex Value: Row Object, TmxTU
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxTuCache {
	/**
	 * The size of the ArrayList (the number of elements it contains).
	 */
	private int size;

	/**
	 * 指向最大行号
	 */
	private int index;

	private TmxTuCacheElement[] cache;

	public TmxTuCache() {
		this(1000);
	}

	public TmxTuCache(int initialCapacity) {
		cache = new TmxTuCacheElement[initialCapacity];
	}

	public void clear() {
		for (int i = 0; i < size; i++) {
			cache[i] = null;
		}
		size = index = 0;
	}

	public void addElement(int rowIndex, TmxTU tmxtu) {
		if (!isFull()) { // 缓存未满
			index = size;
			cache[size++] = new TmxTuCacheElement(rowIndex, tmxtu);
			return;
		}
		if (index == cache.length - 1) {
			index = 0;
		} else {
			index++;
		}
		cache[index].key = rowIndex;
		cache[index].value = tmxtu;
		// if(cache[size -1].getKey() < rowIndex){
		// // 大于最大行
		// TmxTuCacheElement[] temp = cache;
		// cache = new TmxTuCacheElement[cache.length];
		// int from = 1;
		// int to = cache.length;
		// int newLength = to - from;
		// if (newLength < 0)
		// throw new IllegalArgumentException(from + " > " + to);
		// System.arraycopy(temp, from, cache, 0,
		// Math.min(temp.length - from, newLength));
		// cache[cache.length - 1] = new TmxTuCacheElement(rowIndex, tmxtu);
		// } else {
		// TmxTuCacheElement[] temp = cache;
		// cache = new TmxTuCacheElement[cache.length];
		// int from = 0;
		// int to = cache.length - 1;
		// int newLength = to - from;
		// if (newLength < 0)
		// throw new IllegalArgumentException(from + " > " + to);
		// System.arraycopy(temp, from, cache, 1,
		// Math.min(temp.length - from, newLength));
		// cache[0] = new TmxTuCacheElement(rowIndex, tmxtu);
		// }
	}

	public TmxTU getElement(int rowIdex) {
		if (index < 0 || size == 0) {
			return null;
		}
		for (int i = 0; i < size; i++) {
			TmxTuCacheElement ce = cache[i];
			if (ce.getKey() == rowIdex) {
				index = i;
				return ce.getValue();
			}
		}
		return null;
	}

	private boolean isFull() {
		return size == cache.length;
	}

	public static void main(String[] args) {
		String fileLC = "E:\\1\\TMX_edit\\medicaldevicesc.tmx";
		VTDGen vg = new VTDGen();
		vg.parseFile(fileLC, true);

		try {
//			VTDNav vn = vg.getNav();
//			VTDUtils vu = new VTDUtils(vn);
//
//			long time2 = System.currentTimeMillis();
//			for(int i = 100000 ; i < 100005; i++){
//				time2 = System.currentTimeMillis();
//				readTu(i, vu);
//				AutoPilot ap = new AutoPilot(vn);
//				String xp = "/tmx/body/tu["+i+"]";
//				try {
//					ap.selectXPath("/tmx/body/tu["+i+"]");
//					if (ap.evalXPath() != -1) {
//						System.out.println(vu.getElementFragment());
//						TmxFileDataAccessUtils.readTUAttr4VTDNav(vu, tu);
//						TmxFileDataAccessUtils.readTUNote4VTDNav(vu, tu);
//						TmxFileDataAccessUtils.readTUProp4VTDNav(vu, tu);
//						TmxFileDataAccessUtils.readTUTuv4VTDNav(vu, tu, super.currSrcLang, super.currTgtLang);
//					}
//				} catch (VTDException e1) {
//					e1.printStackTrace();
//				}
//				vu.pilot("/tmx/body/tu["+i+"]");
//				System.out.println(System.currentTimeMillis() - time2);
//			}
//			
//			int i = 300000;
//			while(i < 300004){
//				time2 = System.currentTimeMillis();
//				i++;
//				vu.pilot("/tmx/body/tu["+i+"]");
//				System.out.println(System.currentTimeMillis() - time2);
//			}
			
////			
//			vu.pilot("/tmx/body/tu[300000]");
//			System.out.println(System.currentTimeMillis() - time2);
//			time2 = System.currentTimeMillis();
//			vu.pilot("/tmx/body/tu[300001]");
//			System.out.println(System.currentTimeMillis() - time2);
//			time2 = System.currentTimeMillis();
//			vu.pilot("/tmx/body/tu[300002]");
//			System.out.println(System.currentTimeMillis() - time2);
//			time2 = System.currentTimeMillis();
//			vu.pilot("/tmx/body/tu[300003]");
//			System.out.println(System.currentTimeMillis() - time2);
			
			VTDNav vn = vg.getNav();

			long time1 = System.currentTimeMillis();
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("/tmx/body/tu[" + 300000 + "]");
			if (ap.evalXPath() != -1) {
				System.out.println("----------===");
			}
			System.out.println(System.currentTimeMillis() - time1);
			
			long time2 = System.currentTimeMillis();
			//following
			ap.selectXPath("/tmx/body/tu[" + 300015 + "]");
			if (ap.evalXPath() != -1) {
				System.out.println("----------");
			}
			
			
			System.out.println(System.currentTimeMillis() - time2);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	static final String xpath = "/tmx/body/tu[__id__]";
	public static void readTu(int id, VTDUtils vu){
		VTDNav vn = vu.getVTDNav();
		AutoPilot ap = new AutoPilot(vn);
		String xp = "/tmx/body/tu["+id+"]";
		try {
			ap.selectXPath(xp);
			if (ap.evalXPath() != -1) {
//				System.out.println(vu.getElementFragment());
//				TmxFileDataAccessUtils.readTUAttr4VTDNav(vu, tu);
//				TmxFileDataAccessUtils.readTUNote4VTDNav(vu, tu);
//				TmxFileDataAccessUtils.readTUProp4VTDNav(vu, tu);
//				TmxFileDataAccessUtils.readTUTuv4VTDNav(vu, tu, super.currSrcLang, super.currTgtLang);
			}
		} catch (VTDException e1) {
			e1.printStackTrace();
		}
	}

	private class TmxTuCacheElement {
		private int key;
		private TmxTU value;

		public TmxTuCacheElement(int key, TmxTU value) {
			this.key = key;
			this.value = value;
		}

		public TmxTU getValue() {
			return value;
		}

		public int getKey() {
			return key;
		}

		@Override
		public String toString() {
			return "rowIndex : " + key;
		}
	}
}
