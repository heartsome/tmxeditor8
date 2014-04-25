package net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor;

import java.util.List;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TagStyle;
import net.heartsome.cat.common.innertag.TmxInnerTagParser;
import net.heartsome.cat.common.ui.innertag.InnerTag;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * 内部标记工具类。占位符使用 Unicode 码，范围为 {@link #MIN} 到 {@link #MAX}。
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class TmxInnerTagUtils {

	/**
	 * 根据占位符得到内部标记控件
	 * @param innerTags
	 *            内部标记控件集合
	 * @param placeHolder
	 *            占位符
	 * @return 内部标记控件;
	 */
	public static InnerTag getInnerTagControl(List<InnerTag> innerTags, String placeHolder) {
		int index = TmxInnerTagParser.getInstance().getPlaceHolderBuilder().getIndex(null, placeHolder);
		if (index > -1 && index < innerTags.size()) {
			return innerTags.get(index);
		}
		return null;
	}

	/**
	 * 根据内部标记实体得到占位符
	 * @param innerTags
	 *            内部标记控件集合
	 * @param innerTagBean
	 *            内部标记实体
	 * @return 占位符;
	 */
	public static String getPlaceHolder(List<InnerTag> innerTags, InnerTagBean innerTagBean) {
		if (innerTagBean == null || innerTags == null || innerTags.size() == 0) {
			return null;
		}
		for (int i = 0; i < innerTags.size(); i++) {
			InnerTagBean bean = innerTags.get(i).getInnerTagBean();
			if (innerTagBean.equals(bean)) {
				return TmxInnerTagParser.getInstance().getPlaceHolderBuilder().getPlaceHolder(null, i);
			}
		}
		return null;
	}

	/**
	 * 创建内部标记控件
	 * @param parent
	 *            父容器
	 * @param innerTagBean
	 *            内部标记实体
	 * @return 内部标记控件;
	 */
	public static InnerTag createInnerTagControl(Composite parent, InnerTagBean innerTagBean, TagStyle tagStyle) {
		final InnerTag innerTag = new InnerTag(parent, SWT.NONE, innerTagBean, tagStyle);
		innerTag.pack();
		return innerTag;
	}
}
