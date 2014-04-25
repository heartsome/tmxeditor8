/**
 * OpenRecordUtils.java
 *
 * Version information :
 *
 * Date:2013-9-3
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.tmxeditor.editor.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import net.heartsome.cat.te.tmxeditor.TmxEditorUtils;

import org.eclipse.core.resources.ResourcesPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author  Jason
 * @version 
 * @since   JDK1.6
 */
public final class OpenRecord {
	public final Logger LOGGER = LoggerFactory.getLogger(TmxEditorUtils.class);
	private File recordFile = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation()
			.append(".metadata/.preference/.openfile.1").toOSString());	
	
	public OpenRecord(){		
	}
		
	public String getOpenRecord(){
		if(!recordFile.exists()){
			return null;
		}
		try {
			FileInputStream in = new FileInputStream(recordFile);
			InputStreamReader ir = new InputStreamReader(in);
			BufferedReader r = new BufferedReader(ir);
			String line = r.readLine().trim();
			return line;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void saveOpenRecord(String path){
		File parentFile = recordFile.getParentFile();
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(recordFile);
			output.write(path.getBytes("UTF-8"));
		} catch (Exception e) {
			LOGGER.error("", e);
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (Exception e2) {
				LOGGER.error("", e2);
			}
		}		
	}
	
	public void removeOpenRecord(){
		if(recordFile.exists()){
			recordFile.delete();
		}
	}
}
