package net.heartsome.cat.ts.ui.rtf.innertag;

import java.util.Comparator;

/**
 * 对分割的标记进行排序的类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
@SuppressWarnings("rawtypes")
public class SegmentComparator implements Comparator {

	public int compare(Object o1,Object o2) {
		SegmentText st1 = (SegmentText)o1;
		SegmentText st2 = (SegmentText)o2;
		if (st1.getStartIndex() > st2.getStartIndex()) {
			return 1;
		} else if (st1.getStartIndex() < st2.getStartIndex()) {
			return -1;
		} else {
			return 0;
		}
	}

}
