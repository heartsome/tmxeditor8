package net.heartsome.cat.ts;

import net.heartsome.cat.ts.help.SystemResourceUtil;

import org.eclipse.ui.IStartup;

public class TsStartup implements IStartup {

	public void earlyStartup() {
		SystemResourceUtil.load(false);
	}

}
