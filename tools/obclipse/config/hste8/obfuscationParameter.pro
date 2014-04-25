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

# 用于存放各个插件的导出包的类名，public的方法和变量 ################################################
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




##############如果目标平台使用的不是hudson的目标平台，那么使用eclipse export需要添加####################
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


# VTD 改为直接引用源码，不显示相关警告、也不要混淆相关代码
-dontwarn com.ximpleware.**
-dontwarn java_cup.*
-keep class com.ximpleware.** { *;}
-keep class java_cup.** { *;}
