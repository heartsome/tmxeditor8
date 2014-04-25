package net.heartsome.cat.ts.ui.rtf;

import java.util.List;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.factory.IPlaceHolderBuilder;

/**
 * 用于替换 xliff 文件中的标记
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class PlaceHolderRTFModeBuilder implements IPlaceHolderBuilder {
	
	public String getPlaceHolder(List<InnerTagBean> innerTagBeans, int index) {
		return RTFConstants.TAG_RTF;
	}

	public int getIndex(List<InnerTagBean> innerTagBeans, String placeHolder) {
		return 0;
	}

}
