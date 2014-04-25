package net.heartsome.cat.ts.ui.rtf.exporter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.rtf.RTFCodeStringFormat;
import net.heartsome.cat.ts.ui.rtf.RTFConstants;
import net.heartsome.cat.ts.ui.rtf.Rtf;
import net.heartsome.cat.ts.ui.rtf.innertag.InnerTagUtil;
import net.heartsome.cat.ts.ui.rtf.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;
import net.sourceforge.rtf.RTFTemplate;
import net.sourceforge.rtf.format.rtfcode.RTFCodeString;
import net.sourceforge.rtf.template.IContext;
import net.sourceforge.rtf.usecases.AbstractRTFUseCase;
import net.sourceforge.rtf.util.StringUtils;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 用于导出 RTF 文件
 * @author peason
 * @version
 * @since JDK1.6
 */
public class RTFTemplateExporter extends AbstractRTFUseCase {

	private static final Logger LOGGER = LoggerFactory.getLogger(RTFTemplateExporter.class.getName());

	/** XLIFF 文件路径 */
	private String xliffPath;

	/** 源语言 */
	private String srcLang;

	/** 目标语言 */
	private String tgtLang;

	/** 是否导出批注 */
	private boolean isExportStatus;

	/** 是否导出文本段状态 */
	private boolean isExportComment;

	private boolean isExcludeOrOnlyExport;

	private ArrayList<Boolean> lstSelection;

	/**
	 * 构造方法
	 * @param outDirectory
	 *            导出目录
	 * @param xliffPath
	 *            xliff 文件路径
	 * @param srcLang
	 *            源语言
	 * @param tgtLang
	 *            目标语言
	 * @param isExportStatus
	 *            是否导出文本段状态
	 * @param isExportComment
	 *            是否导出文本段批注
	 */
	public RTFTemplateExporter(String outDirectory, String xliffPath, String srcLang, String tgtLang,
			boolean isExportStatus, boolean isExportComment, boolean isExcludeOrOnlyExport,
			ArrayList<Boolean> lstSelection) {
		super(outDirectory);
		this.xliffPath = xliffPath;
		this.srcLang = srcLang;
		this.tgtLang = tgtLang;
		this.isExportStatus = isExportStatus;
		this.isExportComment = isExportComment;
		this.isExcludeOrOnlyExport = isExcludeOrOnlyExport;
		this.lstSelection = lstSelection;
	}

	@Override
	protected void putDefaultFormat(RTFTemplate rtfTemplate) {
		rtfTemplate.setDefaultFormat(Date.class, DateFormat.getDateInstance());
		rtfTemplate.setDefaultFormat(RTFCodeString.class, new RTFCodeStringFormat());
	}

	@Override
	protected void putContext(IContext context) {
		context.put("date", new Date());
		context.put("header_id", RTFConstants.RTF_MODEL_COLUMN_ID);
		context.put("header_source", srcLang);
		context.put("header_target", tgtLang);
		context.put("header_comment", RTFConstants.RTF_MODEL_COLUMN_COMMENTS);
		context.put("header_status", RTFConstants.RTF_MODEL_COLUMN_STATUS);

		List<Export> es = new ArrayList<Export>();
		VTDGen vg = new VTDGen();
		if (vg.parseFile(xliffPath, true)) {
			VTDNav vn = vg.getNav();
			AutoPilot ap = new AutoPilot(vn);
			ap.declareXPathNameSpace(XLFHandler.hsNSPrefix, XLFHandler.hsR7NSUrl);
			try {
				VTDUtils vu = new VTDUtils(vn);
				String xpath = "/xliff/file[@source-language='" + srcLang + "' and @target-language='" + tgtLang
						+ "']/body/descendant::trans-unit";
				ap.selectXPath(xpath);

				AutoPilot ap2 = new AutoPilot(vn);
				ap2.declareXPathNameSpace(XLFHandler.hsNSPrefix, XLFHandler.hsR7NSUrl);

				while (ap.evalXPath() != -1) {
					String id = vu.getCurrentElementAttribut("id", null);
					String srcText = vu.getValue("./source/text()");
					if (srcText == null || srcText.equalsIgnoreCase("")) {
						continue;
					}

					// 过滤条件
					if (isExcludeOrOnlyExport) {
						// 排除
						if (lstSelection.get(0)) {
							// 排除锁定文本段
							vn.push();
							String locked = vu.getCurrentElementAttribut("translate", "yes");
							// locked 为 no 表示锁定
							if (locked.equalsIgnoreCase("no")) {
								vn.pop();
								continue;
							} else {
								vn.pop();
							}
						}
						if (lstSelection.get(1)) {
							// 排除上下文匹配文本段
							vn.push();
							ap2.selectXPath("count(./alt-trans[@match-quality='101'])");
							int count101 = (int) ap2.evalXPathToNumber();
							if (count101 > 0) {
								vn.pop();
								continue;
							} else {
								vn.pop();
							}
						}
						if (lstSelection.get(2)) {
							// 排除完全匹配文本段
							vn.push();
							ap2.selectXPath("count(./alt-trans[@match-quality='100'])");
							int count100 = (int) ap2.evalXPathToNumber();
							if (count100 > 0) {
								vn.pop();
								continue;
							} else {
								vn.pop();
							}
						}
					} else {
						// 仅导出
						if (lstSelection.get(0)) {
							// 仅导出带批注的文本段
							vn.push();
							Vector<String> lstComment = vu.getChildrenContent("note");
							if (lstComment == null || lstComment.size() == 0) {
								vn.pop();
								continue;
							} else {
								vn.pop();
							}
						}
						if (lstSelection.get(1)) {
							// 仅导出带疑问的文本段
							vn.push();
							String needReview = vu.getCurrentElementAttribut("hs:needs-review", null);
							// 疑问行
							if (needReview == null || needReview.equalsIgnoreCase("no")) {
								vn.pop();
								continue;
							} else {
								vn.pop();
							}
						}
					}

					StringBuffer sbSrc = new StringBuffer(srcText);
					InnerTagUtil.parseXmlToDisplayValue(sbSrc);
					String srcHexString = TextUtil.encodeHexString(sbSrc.toString());
					srcText = Rtf.asRtf(TextUtil.decodeHexString(Rtf.replaceInvisibleChar(srcHexString)));
					srcText = replateTag(srcText);

					String tgtText = vu.getValue("./target/text()");
					if (tgtText != null) {
						StringBuffer sbTgt = new StringBuffer(tgtText);
						InnerTagUtil.parseXmlToDisplayValue(sbTgt);
						String tgtHexString = TextUtil.encodeHexString(sbTgt.toString());
						tgtText = Rtf.asRtf(TextUtil.decodeHexString(Rtf.replaceInvisibleChar(tgtHexString)));
						tgtText = replateTag(tgtText);
					} else {
						tgtText = "";
					}

					Export bean = new Export(id, new RTFCodeString(srcText, false), new RTFCodeString(tgtText, false));

					if (isExportStatus) {
						vn.push();
						ArrayList<Integer> lstStatus = new ArrayList<Integer>();
						String status = null;
						String needReview = vu.getCurrentElementAttribut("hs:needs-review", null);
						// 疑问行
						if (needReview != null && needReview.equalsIgnoreCase("yes")) {
							lstStatus.add(RTFConstants.STATUS_NEED_REVIEW);
						}
						// 不添加到记忆库
						String sendToTM = vu.getCurrentElementAttribut("hs:send-to-tm", null);
						if (sendToTM != null && sendToTM.equalsIgnoreCase("no")) {
							lstStatus.add(RTFConstants.STATUS_NOT_SEND_TO_TM);
						}

						String locked = vu.getCurrentElementAttribut("translate", "yes");
						// locked 为 no 表示锁定
						if (locked.equalsIgnoreCase("no")) {
							lstStatus.add(RTFConstants.STATUS_LOCKED);
						}
						String approved = vu.getCurrentElementAttribut("approved", "no");
						String state = vu.getValue("./target/@state");

						if (tgtText == null || tgtText.equals("")) {
							// 未翻译
							lstStatus.add(RTFConstants.STATUS_NOT_TRANSLATE);
						} else {
							if (state != null) {
								if (state.equalsIgnoreCase("signed-off")) {
									// 已签发
									lstStatus.add(RTFConstants.STATUS_SIGNED_OFF);
								} else if (approved.equalsIgnoreCase("yes")) {
									// 已批准
									lstStatus.add(RTFConstants.STATUS_APPROVED);
								} else if (!approved.equalsIgnoreCase("yes") && state.equalsIgnoreCase("translated")) {
									// 完成翻译
									lstStatus.add(RTFConstants.STATUS_TRANSLATED);
								} else if (state.equalsIgnoreCase("new")) {
									// 草稿
									lstStatus.add(RTFConstants.STATUS_NEW);
								}
							} else {
								if (approved.equalsIgnoreCase("yes")) {
									// 已批准
									lstStatus.add(RTFConstants.STATUS_APPROVED);
								} else {
									// 未翻译
									lstStatus.add(RTFConstants.STATUS_NOT_TRANSLATE);
								}
							}
						}
						vn.pop();
						if (lstStatus.size() > 0) {
							Collections.sort(lstStatus);
							status = lstStatus.toString().substring(1, lstStatus.toString().length() - 1);
							// 各状态之间以 & 相连
							status = status.replaceAll(",", " &");
						}
						bean.setStatus(Rtf.asRtf(status));
					}
					if (isExportComment) {
						vn.push();
						String note = "";
						Vector<String> lstComment = vu.getChildrenContent("note");
						if (lstComment != null && lstComment.size() > 0) {
							if (lstComment.size() == 1) {
								String strNote = lstComment.get(0);
								if (strNote.indexOf(":") != -1) {
									note = strNote.substring(strNote.indexOf(":") + 1);
								} else {
									note = strNote;
								}
							} else {
								for (int i = 0; i < lstComment.size(); i++) {
									String strNote = lstComment.get(i);
									if (strNote.indexOf(":") != -1) {
										strNote = strNote.substring(strNote.indexOf(":") + 1);
									}
									note += (i + 1) + ". " + strNote;
									if (i != lstComment.size() - 1) {
										note += "\n";
									}
								}
							}
						}

						vn.pop();
						bean.setComment(new RTFCodeString(replateTag(Rtf.asRtf(note)), false));
					}
					es.add(bean);
				}
				if (es.size() == 0) {
					Export bean = new Export("", new RTFCodeString("", false), new RTFCodeString("", false), "",
							new RTFCodeString("", false));
					es.add(bean);
				}
				context.put("es", es);
			} catch (NavException e) {
				LOGGER.error(Messages.getString("exporter.RTFTemplateExporter.logger"), e);
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
						Messages.getString("exporter.RTFTemplateExporter.msg.title"),
						Messages.getString("exporter.RTFTemplateExporter.msg"));
			} catch (XPathParseException e) {
				LOGGER.error(Messages.getString("exporter.RTFTemplateExporter.logger"), e);
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
						Messages.getString("exporter.RTFTemplateExporter.msg.title"),
						Messages.getString("exporter.RTFTemplateExporter.msg"));
			} catch (XPathEvalException e) {
				LOGGER.error(Messages.getString("exporter.RTFTemplateExporter.logger"), e);
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
						Messages.getString("exporter.RTFTemplateExporter.msg.title"),
						Messages.getString("exporter.RTFTemplateExporter.msg"));
			}
		}
	}

	/**
	 * 用于将 text 中的标记加粗
	 * @param text
	 * @return ;
	 */
	private String replateTag(String text) {
		if (text != null) {
			// text = text.replaceAll("\\}", "\\\\\\\\}");
			// text = text.replaceAll("\\{", "\\\\\\\\{");
			text = StringUtils.sub(text, RTFConstants.TAG_RTF, "{" + RTFConstants.RTF_COLOR_RED + " "
					+ RTFConstants.TAG_RTF + "}");
			text = TextUtil.xmlToString(text);
		}
		return text;
	}
}
