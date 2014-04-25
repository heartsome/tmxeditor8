/**
 * TmxUtil.java
 *
 * Version information :
 *
 * Date:2013-12-2
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.converter;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.ModifyException;
import com.ximpleware.ParseException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.XMLModifier;

import net.heartsome.cat.common.util.FileEncodingDetector;
import net.heartsome.xml.vtdimpl.EmptyFileException;
import net.heartsome.xml.vtdimpl.VTDLoader;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class TmxUtil {

	private static Logger LOGGER = LoggerFactory.getLogger(TmxUtil.class);

	/**
	 * 清理不能解析的字符
	 * @param tmxPath
	 *            ;
	 */
	public static VTDGen clearUnableParseChars(String tmxPath) {
		File f = new File(tmxPath);
		if (!f.exists() || !f.canWrite()) {
			LOGGER.info(tmxPath + "can not write");
			return null;
		}
		String detectFileEncoding = FileEncodingDetector.detectFileEncoding(f);
		try {
			VTDGen loadVTDGen = VTDLoader.loadVTDGen(f, detectFileEncoding);
			XMLModifier modeModifier = new XMLModifier(loadVTDGen.getNav());
			modeModifier.output(tmxPath);
			return loadVTDGen;
		} catch (ParseException e) {
			LOGGER.error("parse " + tmxPath + "failed");
		} catch (IOException e) {
			LOGGER.error("io error" + e.getMessage());
		} catch (EmptyFileException e) {
			LOGGER.error(tmxPath + "is empty");
		} catch (ModifyException e) {
			LOGGER.error("", e);
		} catch (TranscodeException e) {
			LOGGER.error("", e);
		}
		return null;
	}

}
