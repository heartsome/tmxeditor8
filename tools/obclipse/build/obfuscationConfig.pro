   -libraryjars "/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/rt.jar"
   -printmapping "/Users/Mac/r8PackGit/tePack/translation-studio/tools/obclipse/build/obfuscate.map"
   -applymapping "/Users/Mac/r8PackGit/tePack/translation-studio/tools/obclipse/config/hste8/obfuscate.map"
-dontshrink
-dontoptimize
-defaultpackage obClasses
-allowaccessmodification
-useuniqueclassmembernames
-dontusemixedcaseclassnames
-keeppackagenames

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}

-keep public interface * extends com.sun.jna.Library {*;}

-keep class **.Messages
-keep class org.eclipse.ui.internal.navigator.resources.resource.WorkbenchNavigatorMessages {
	public <fields>;
}

# ??¨ä??å­???¾å??ä¸????ä»¶ç??å¯¼å?ºå?????ç±»å??ï¼?public?????¹æ??????????? ################################################
#################################################################################################
## net.heartsome.cat.common.core
-keep class net.heartsome.cat.common.bean.*, net.heartsome.cat.common.core.*,
 net.heartsome.cat.common.core.exception.*, net.heartsome.cat.common.file.*,
 net.heartsome.cat.common.innertag.*, net.heartsome.cat.common.innertag.factory.*,
 net.heartsome.cat.common.locale.*, net.heartsome.cat.common.operator.*,
 net.heartsome.cat.common.resources.*, net.heartsome.cat.common.tm.*,
 net.heartsome.cat.common.util.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.common.ui
-keep class net.heartsome.cat.common.ui.*, net.heartsome.cat.common.ui.dialog.*,
net.heartsome.cat.common.ui.handlers.*, net.heartsome.cat.common.ui.innertag.*,
net.heartsome.cat.common.ui.listener.*, net.heartsome.cat.common.ui.utils.*,
net.heartsome.cat.common.ui.wizard.*, net.heartsome.cat.common.ui.languagesetting.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.database
-keep class net.heartsome.cat.database.*, net.heartsome.cat.database.bean.*,
net.heartsome.cat.database.service.*, net.heartsome.cat.database.tmx.*,
net.heartsome.cat.document.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.database.ui
-keep class net.heartsome.cat.database.ui.bean.*, net.heartsome.cat.database.ui.core.*,
net.heartsome.cat.database.ui.dialog.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.p2update
-keep class net.heartsome.cat.p2update.autoupdate.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.te.core
-keep class net.heartsome.cat.te.core.*, net.heartsome.cat.te.core.bean.*,
 net.heartsome.cat.te.core.converter.*, net.heartsome.cat.te.core.qa.*,
 net.heartsome.cat.te.core.tmxdata.*, net.heartsome.cat.te.core.utils.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.te.tmxeditor
-keep class net.heartsome.cat.te.tmxeditor.*, net.heartsome.cat.te.tmxeditor.editor.*,
 net.heartsome.cat.te.tmxeditor.editor.history.*, net.heartsome.cat.te.tmxeditor.view.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.te.ui
-keep class net.heartsome.cat.te.ui.*, net.heartsome.cat.te.ui.preferencepage.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.cat.thirdpartlibrary  not added...    added by robert 2013-12-03

## net.heartsome.cat.te.qa
-keep class net.heartsome.cat.ts.help.*, net.heartsome.license.resource.*
{
	public <fields>;
	public <methods>;
}

## net.heartsome.xml
-keep class net.heartsome.xml.vtdimpl.*
{
	public <fields>;
	public <methods>;
}

## net.sourceforge.nattable.core
-keep class net.sourceforge.nattable.*, net.sourceforge.nattable.blink.*,
 net.sourceforge.nattable.blink.command.*, net.sourceforge.nattable.blink.event.*,
 net.sourceforge.nattable.columnCategories.*, net.sourceforge.nattable.columnChooser.*,
 net.sourceforge.nattable.columnChooser.command.*, net.sourceforge.nattable.columnChooser.gui.*,
 net.sourceforge.nattable.columnRename.*, net.sourceforge.nattable.command.*,
 net.sourceforge.nattable.config.*, net.sourceforge.nattable.coordinate.*,
 net.sourceforge.nattable.copy.action.*, net.sourceforge.nattable.copy.command.*,
 net.sourceforge.nattable.copy.serializing.*, net.sourceforge.nattable.data.*,
 net.sourceforge.nattable.data.convert.*, net.sourceforge.nattable.data.validate.*,
 net.sourceforge.nattable.edit.*, net.sourceforge.nattable.edit.action.*,
 net.sourceforge.nattable.edit.command.*, net.sourceforge.nattable.edit.config.*,
 net.sourceforge.nattable.edit.editor.*, net.sourceforge.nattable.edit.event.*,
 net.sourceforge.nattable.edit.gui.*, net.sourceforge.nattable.export.excel.*,
 net.sourceforge.nattable.export.excel.action.*, net.sourceforge.nattable.export.excel.command.*,
 net.sourceforge.nattable.export.excel.config.*, net.sourceforge.nattable.filterrow.command.*,
 net.sourceforge.nattable.freeze.*, net.sourceforge.nattable.freeze.action.*,
 net.sourceforge.nattable.freeze.command.*, net.sourceforge.nattable.freeze.config.*,
 net.sourceforge.nattable.freeze.event.*, net.sourceforge.nattable.grid.*,
 net.sourceforge.nattable.grid.cell.*, net.sourceforge.nattable.grid.command.*,
 net.sourceforge.nattable.grid.data.*, net.sourceforge.nattable.grid.layer.*,
 net.sourceforge.nattable.grid.layer.config.*, net.sourceforge.nattable.grid.layer.event.*,
 net.sourceforge.nattable.group.*, net.sourceforge.nattable.group.action.*,
 net.sourceforge.nattable.group.command.*, net.sourceforge.nattable.group.config.*,
 net.sourceforge.nattable.group.event.*, net.sourceforge.nattable.group.gui.*,
 net.sourceforge.nattable.group.painter.*, net.sourceforge.nattable.hideshow.*,
 net.sourceforge.nattable.hideshow.command.*, net.sourceforge.nattable.hideshow.event.*,
 net.sourceforge.nattable.layer.*, net.sourceforge.nattable.layer.cell.*,
 net.sourceforge.nattable.layer.config.*, net.sourceforge.nattable.layer.event.*,
 net.sourceforge.nattable.layer.stack.*, net.sourceforge.nattable.painter.*,
 net.sourceforge.nattable.painter.cell.*, net.sourceforge.nattable.painter.cell.decorator.*,
 net.sourceforge.nattable.painter.layer.*, net.sourceforge.nattable.persistence.*,
 net.sourceforge.nattable.print.*, net.sourceforge.nattable.print.action.*,
 net.sourceforge.nattable.print.command.*, net.sourceforge.nattable.print.config.*,
 net.sourceforge.nattable.reorder.*, net.sourceforge.nattable.reorder.action.*,
 net.sourceforge.nattable.reorder.command.*, net.sourceforge.nattable.reorder.config.*,
 net.sourceforge.nattable.reorder.event.*, net.sourceforge.nattable.resize.*,
 net.sourceforge.nattable.resize.action.*, net.sourceforge.nattable.resize.command.*,
 net.sourceforge.nattable.resize.config.*, net.sourceforge.nattable.resize.event.*,
 net.sourceforge.nattable.resize.mode.*, net.sourceforge.nattable.search.*,
 net.sourceforge.nattable.search.action.*, net.sourceforge.nattable.search.command.*,
 net.sourceforge.nattable.search.config.*, net.sourceforge.nattable.search.event.*,
 net.sourceforge.nattable.search.gui.*, net.sourceforge.nattable.search.strategy.*,
 net.sourceforge.nattable.selection.*, net.sourceforge.nattable.selection.action.*,
 net.sourceforge.nattable.selection.command.*, net.sourceforge.nattable.selection.config.*,
 net.sourceforge.nattable.selection.event.*, net.sourceforge.nattable.serializing.*,
 net.sourceforge.nattable.sort.*, net.sourceforge.nattable.sort.action.*,
 net.sourceforge.nattable.sort.command.*, net.sourceforge.nattable.sort.config.*,
 net.sourceforge.nattable.sort.event.*, net.sourceforge.nattable.sort.painter.*,
 net.sourceforge.nattable.style.*, net.sourceforge.nattable.style.editor.*,
 net.sourceforge.nattable.style.editor.command.*, net.sourceforge.nattable.summaryrow.*,
 net.sourceforge.nattable.tickupdate.*, net.sourceforge.nattable.tickupdate.action.*,
 net.sourceforge.nattable.tickupdate.command.*, net.sourceforge.nattable.tickupdate.config.*,
 net.sourceforge.nattable.ui.*, net.sourceforge.nattable.ui.action.*,
 net.sourceforge.nattable.ui.binding.*, net.sourceforge.nattable.ui.matcher.*,
 net.sourceforge.nattable.ui.menu.*, net.sourceforge.nattable.ui.mode.*,
 net.sourceforge.nattable.ui.util.*, net.sourceforge.nattable.util.*,
 net.sourceforge.nattable.viewport.*, net.sourceforge.nattable.viewport.action.*,
 net.sourceforge.nattable.viewport.command.*, net.sourceforge.nattable.viewport.event.*,
 net.sourceforge.nattable.widget.*
{
	public <fields>;
	public <methods>;
}

## org.eclipse.nebula.widgets.tablecombo
-keep class org.eclipse.nebula.jface.tablecomboviewer.*, org.eclipse.nebula.widgets.tablecombo.*
{
	public <fields>;
	public <methods>;
}



#################################################################################################
##########################################################################################




##############å¦??????????å¹³å?°ä½¿??¨ç??ä¸????hudson?????????å¹³å?°ï????£ä??ä½¿ç??eclipse export???è¦?æ·»å??####################
#-keep public class * implements org.osgi.framework.BundleActivator {*;}
#-keep class net.heartsome.cat.ts.Application
###########################################################################################

-keep class net.heartsome.cat.te.core.qa.NumberConsistenceQA
-keep class net.heartsome.cat.te.core.qa.SpaceOfParaCheckQA
-keep class net.heartsome.cat.te.core.qa.SrcSameButTgtQA
-keep class net.heartsome.cat.te.core.qa.SrcSameWithTgtQA
-keep class net.heartsome.cat.te.core.qa.TagConsistenceQA
-keep class net.heartsome.cat.te.core.qa.TgtNullQA
-keep class net.heartsome.cat.te.core.qa.TgtSameButSrcQA
-keep class net.heartsome.cat.te.core.qa.QAConstant


-dontnote

-keep class net.heartsome.license.webservice.IService {
    public <methods>;
}

-keep public class * implements java.beans.PropertyChangeListener {*;}
-keep public class * extends net.heartsome.cat.converter.util.AbstractModelObject {*;}
-keep public class * extends de.jaret.util.misc.PropertyObservableBase {*;}


# VTD ??¹ä¸º??´æ?¥å????¨æ?????ï¼?ä¸???¾ç¤º??¸å?³è????????ä¹?ä¸?è¦?æ··æ????¸å?³ä»£???
-dontwarn com.ximpleware.**
-dontwarn java_cup.*
-keep class com.ximpleware.** { *;}
-keep class java_cup.** { *;}
   -libraryjars "/Users/Mac/r8PackGit/tePack/translation-studio/tools/obclipse/resources/obfuscate/annotations.jar"
   -include "/Users/Mac/r8PackGit/tePack/translation-studio/tools/obclipse/resources/obfuscate/annotations.pro"
   -libraryjars "/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/jce.jar"
   -libraryjars "/Users/Mac/r8PackGit/tePack/translation-studio/tools/obclipse/resources/hste8/log4j-1.2.15.jar"
   -libraryjars "/Users/Mac/r8PackGit/tePack/translation-studio/tools/obclipse/resources/hste8/slf4j-api-1.5.8.jar"
   -libraryjars "/Users/Mac/r8PackGit/tePack/translation-studio/tools/obclipse/resources/hste8/slf4j-log4j12-1.5.8.jar"
   -libraryjars "/Users/Mac/r8PackGit/tePack/translation-studio/tools/obclipse/resources/hste8/junit.jar"
   -libraryjars "/Users/Mac/r8PackGit/tePack/translation-studio/tools/obclipse/resources/hste8/hslibrary3.jar"
   -libraryjars "/Users/Mac/r8PackGit/tePack/translation-studio/tools/obclipse/resources/hste8/xercesImpl.jar"
   -libraryjars "/Users/Mac/r8PackGit/tePack/translation-studio/tools/obclipse/resources/hste8/resolver.jar"

   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/com.ibm.icu_4.4.2.v20110823.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/heartsome.java.tools.plugin_8.0.0.R8b_v20121025.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/javax.servlet.jsp_2.0.0.v201101211617.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/javax.servlet_2.5.0.v201103041518.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.thirdpartlibrary_8.0.0.R8b_v20121113.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.sourceforge.nattable.core_8.0.0.R8b_v20121203.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.apache.commons.codec_1.3.0.v201101211617.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.apache.commons.el_1.0.0.v201101211617.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.apache.commons.httpclient_3.1.0.v201012070820.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.apache.commons.lang_2.4.0.v201005080502.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.apache.commons.logging_1.0.4.v201101211617.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.apache.jasper_5.5.17.v201101211617.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.apache.lucene.analysis_2.9.1.v201101211721.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.apache.lucene.core_2.9.1.v201101211721.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.apache.lucene_2.9.1.v201101211721.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.compare.core_3.5.200.I20110208-0800.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.commands_3.6.0.I20110111-0800.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.contenttype_3.4.100.v20110423-0524.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.databinding.beans_1.2.100.I20100824-0800.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.databinding.observable_1.4.0.I20110222-0800.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.databinding.property_1.4.0.I20110222-0800.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.databinding_1.4.0.I20110111-0800.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.expressions_3.4.300.v20110228.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.filebuffers_3.5.200.v20110928-1504.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.filesystem.linux.x86_1.4.0.v20110423-0524.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.filesystem.linux.x86_64_1.2.0.v20110423-0524.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.filesystem.win32.x86_1.1.300.v20110423-0524.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.filesystem.win32.x86_64_1.1.300.v20110423-0524.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.filesystem_1.3.100.v20110423-0524.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.jobs_3.5.101.v20120113-1953.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.net.linux.x86_1.1.200.I20110419-0800.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.net.linux.x86_64_1.1.0.I20110331-0827.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.net.nl_zh_8.0.1.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.net.win32.x86_1.0.100.I20110331-0827.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.net.win32.x86_64_1.0.100.I20110331-0827.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.net_1.2.100.I20110511-0800.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.resources.nl_zh_8.0.1.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.resources_3.7.101.v20120125-1505.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.runtime.compatibility.auth_3.2.200.v20110110.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.core.runtime_3.7.0.v20110110.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ecf.filetransfer_5.0.0.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ecf.identity_3.1.100.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ecf.provider.filetransfer.httpclient.ssl_1.0.0.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ecf.provider.filetransfer.httpclient_4.0.0.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ecf.provider.filetransfer.ssl_1.0.0.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ecf.provider.filetransfer_3.2.0.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ecf.ssl_1.0.100.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ecf_3.1.300.v20110531-2218.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.app_1.3.100.v20110321.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.common_3.6.0.v20110523.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.ds_1.3.1.R37x_v20110701.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.frameworkadmin.equinox_1.0.300.v20110815-1438.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.frameworkadmin_2.0.0.v20110815-1438.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.http.jetty_2.0.100.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.http.registry_1.1.100.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.http.servlet_1.1.200.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.jsp.jasper.registry_1.0.200.v20100503.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.jsp.jasper_1.0.300.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.launcher.cocoa.macosx.x86_64_1.1.101.v20120109-1504.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.launcher.cocoa.macosx_1.1.101.v20120109-1504.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.launcher.gtk.linux.x86_1.1.100.v20110505.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.launcher.gtk.linux.x86_64_1.1.100.v20110505.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.launcher.win32.win32.x86_1.1.100.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.launcher.win32.win32.x86_64_1.1.100.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.launcher_1.2.0.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.artifact.repository_1.1.101.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.console_1.0.300.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.core_2.1.1.v20120113-1346.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.director_2.1.1.v20111126-0211.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.engine_2.1.1.R37x_v20111003.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.garbagecollector_1.0.200.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.jarprocessor_1.0.200.v20110815-1438.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.metadata.repository_1.2.0.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.metadata_2.1.0.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.operations_2.1.1.R37x_v20111111.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.ql_2.0.100.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.repository_2.1.1.v20120113-1346.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.touchpoint.eclipse_2.1.1.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.touchpoint.natives_1.0.300.v20110815-1419.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.transport.ecf_1.0.0.v20111128-0624.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.ui.nl_zh_8.0.1.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.p2.ui_2.1.1.v20120113-1346.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.preferences_3.4.2.v20120111-2020.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.registry_3.5.101.R37x_v20110810-1611.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.security.macosx_1.100.100.v20100503.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.security.ui_1.1.0.v20101004.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.security.win32.x86_1.0.200.v20100503.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.security.win32.x86_64_1.0.0.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.security_1.1.1.R37x_v20110822-1018.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.simpleconfigurator.manipulator_2.0.0.v20110815-1438.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.simpleconfigurator_1.0.200.v20110815-1438.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.equinox.util_1.0.300.v20110502.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.help.base_3.6.2.v201202080800.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.help.ui_3.5.101.r37_20110819.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.help.webapp.nl_zh_8.0.1.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.help.webapp_3.6.1.r37_20110929.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.help_3.5.100.v20110426.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.jface.databinding_1.5.0.I20100907-0800.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.jface.nl_en_8.0.2.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.jface.nl_zh_8.0.2.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.jface.text_3.7.2.v20111213-1208.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.jface_3.7.0.v20110928-1505.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ltk.core.refactoring.nl_en_8.0.2.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ltk.core.refactoring.nl_zh_8.0.2.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ltk.core.refactoring_3.5.201.r372_v20111101-0700.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.nebula.widgets.tablecombo_8.0.0.R8b_v20121203.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.osgi.nl_en_8.0.1.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.osgi.nl_zh_8.0.1.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.osgi.services_3.3.0.v20110513.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.osgi_3.7.2.v20120110-1415.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.rcp_3.7.2.v201202080800.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.swt.cocoa.macosx.x86_64_3.102.0.v20130605-1544.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.swt.cocoa.macosx_3.102.0.v20130605-1544.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.swt.gtk.linux.x86_3.7.2.v3740f.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.swt.gtk.linux.x86_64_3.7.2.v3740f.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.swt.win32.win32.x86_3.7.2.v3740f.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.swt.win32.win32.x86_64_3.7.2.v3740f.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.swt_3.7.2.v3740f.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.text_3.5.101.v20110928-1504.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui.cocoa_1.1.0.I20101109-0800.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui.editors_3.7.0.v20110928-1504.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui.forms_3.5.101.v20111011-1919.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui.ide.nl_en_8.0.3.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui.ide.nl_zh_8.0.3.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui.ide_3.7.0.v20110928-1505.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui.navigator_3.5.101.v20120106-1355.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui.net.nl_zh_8.0.1.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui.net_1.2.100.v20111208-1155.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui.nl_zh_8.0.1.R8b_v20131209.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui.views.properties.tabbed_3.5.200.v20110928-1505.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui.views_3.6.0.v20110928-1505.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui.workbench.nl_zh_8.0.2.R8b_v20130906.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui.workbench.texteditor_3.7.0.v20110928-1504.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui.workbench_3.7.1.v20120104-1859.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.ui_3.7.0.v20110928-1505.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.eclipse.update.configurator_3.3.100.v20100512.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.mortbay.jetty.server_6.1.23.v201012071420.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.mortbay.jetty.util_6.1.23.v201012071420.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.sat4j.core_2.3.0.v20110329.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/org.sat4j.pb_2.3.0.v20110329.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.common.core_8.0.3.R8b_v20140102/lib/antlr-2.7.4.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.common.core_8.0.3.R8b_v20140102/lib/chardet-1.0.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.common.core_8.0.3.R8b_v20140102/lib/cpdetector_1.0.10.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.common.core_8.0.3.R8b_v20140102/lib/jargs-1.0.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.database.hsql_8.0.0.R8b_v20121205/lib/hsqldb.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.database.mssql_8.0.0.R8b_v20121203/lib/jtds-1.1.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.database.mysql_8.0.0.R8b_v20121203/lib/mysql-connector-java-5.1.10-bin.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.database.oracle_8.0.3.R8b_v20140106/lib/ojdbc14.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.database.postgreSQL_8.0.0.R8b_v20121106/lib/postgresql-8.4-701.jdbc4.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.database.sqlite_8.0.1.R8b_v20130502/lib/sqlite-jdbc-3.7.15-SNAPSHOT.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.core_8.0.0.R8b_v20140103/lib/dom4j-1.6.1.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.core_8.0.0.R8b_v20140103/lib/poi-3.9-20121203.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.core_8.0.0.R8b_v20140103/lib/poi-excelant-3.9-20121203.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.core_8.0.0.R8b_v20140103/lib/poi-ooxml-3.9-20121203.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.core_8.0.0.R8b_v20140103/lib/poi-ooxml-schemas-3.9-20121203.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.core_8.0.0.R8b_v20140103/lib/poi-scratchpad-3.9-20121203.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.core_8.0.0.R8b_v20140103/lib/saxon9he.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.core_8.0.0.R8b_v20140103/lib/stax-api-1.0.1.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.core_8.0.0.R8b_v20140103/lib/xercesImpl.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.core_8.0.0.R8b_v20140103/lib/xmlbeans-2.3.0.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.qa_8.0.2.R8b_v20131218/lib/bcprov-jdk14-136.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.qa_8.0.2.R8b_v20131218/lib/commons-codec-1.3.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.qa_8.0.2.R8b_v20131218/lib/commons-httpclient-3.0.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.qa_8.0.2.R8b_v20131218/lib/commons-logging-1.0.4.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.qa_8.0.2.R8b_v20131218/lib/jdom-1.0.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.qa_8.0.2.R8b_v20131218/lib/jug.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.qa_8.0.2.R8b_v20131218/lib/log4j-1.2.14.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.qa_8.0.2.R8b_v20131218/lib/wsdl4j-1.5.1.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.qa_8.0.2.R8b_v20131218/lib/xfire-all-1.2.6.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.tmxeditor_8.0.0.R8b_v20140103/lib/KTable.jar"
   -libraryjars "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.xml_8.0.1.R8b_v20130424/lib/dom4j-1.6.1.jar"

   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.common.ui.shield.help_8.0.0.R8b_v20121112"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.common.ui.shield.help_8.0.0.R8b_v20121112"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.database.oracle_8.0.3.R8b_v20140106"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.database.oracle_8.0.3.R8b_v20140106"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.core_8.0.0.R8b_v20140103"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.te.core_8.0.0.R8b_v20140103"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.common.core_8.0.3.R8b_v20140102"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.common.core_8.0.3.R8b_v20140102"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.xml_8.0.1.R8b_v20130424"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.xml_8.0.1.R8b_v20130424"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.common.ui.shield.resources_8.0.0.R8b_v20121112"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.common.ui.shield.resources_8.0.0.R8b_v20121112"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.database.mysql_8.0.0.R8b_v20121203"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.database.mysql_8.0.0.R8b_v20121203"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.database.postgreSQL_8.0.0.R8b_v20121106"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.database.postgreSQL_8.0.0.R8b_v20121106"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.common.ui.shield_8.0.0.R8b_v20121112"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.common.ui.shield_8.0.0.R8b_v20121112"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.database.hsql_8.0.0.R8b_v20121205"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.database.hsql_8.0.0.R8b_v20121205"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.common.ui.shield.workbench_8.0.0.R8b_v20121112"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.common.ui.shield.workbench_8.0.0.R8b_v20121112"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.database.mssql_8.0.0.R8b_v20121203"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.database.mssql_8.0.0.R8b_v20121203"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.database.ui_8.0.1.R8b_v20121220"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.database.ui_8.0.1.R8b_v20121220"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.p2update_8.0.1.R8b_v20121214"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.p2update_8.0.1.R8b_v20121214"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.qa_8.0.2.R8b_v20131218"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.te.qa_8.0.2.R8b_v20131218"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.ui.help_8.0.0.R8b_v20131210"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.te.ui.help_8.0.0.R8b_v20131210"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.database_8.0.3.R8b_v20140106"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.database_8.0.3.R8b_v20140106"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.ui_8.0.0.R8b_v20140103"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.te.ui_8.0.0.R8b_v20140103"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te.tmxeditor_8.0.0.R8b_v20140103"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.te.tmxeditor_8.0.0.R8b_v20140103"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.common.ui_8.0.2.R8b_v20131225"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.common.ui_8.0.2.R8b_v20131225"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.te_8.0.0.R8b_v20140106"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.te_8.0.0.R8b_v20140106"
   -injars  "/Users/Mac/Desktop/te_pack/repository/plugins/net.heartsome.cat.database.sqlite_8.0.1.R8b_v20130502"(*.jar;plugins/**,heartsome/**,net/heartsome/**,org/eclipse/ui/**)
   -outjars "/Users/Mac/Desktop/te_pack/repository/plugins/tmpOb/net.heartsome.cat.database.sqlite_8.0.1.R8b_v20130502"
