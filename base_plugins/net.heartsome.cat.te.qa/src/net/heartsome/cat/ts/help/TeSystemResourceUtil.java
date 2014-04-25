package net.heartsome.cat.ts.help;

import java.io.File;

import net.heartsome.license.InvalidateDialog;
import net.heartsome.license.LicenseAgreementDialog;
import net.heartsome.license.LicenseManageDialog;
import net.heartsome.license.LocalAuthorizationValidator;
import net.heartsome.license.constants.Constants;
import net.heartsome.license.utils.DateUtils;
import net.heartsome.license.utils.FileUtils;
import net.heartsome.license.webservice.ServiceUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;

public class TeSystemResourceUtil {

	public static String dateTemp;

	private static LocalAuthorizationValidator v = new LocalAuthorizationValidator();
	private static int re;
	private static boolean isExsit = false;
	private static String date;

	public static void beforeload() {
		isExsit = FileUtils.isExsit();
		if (isExsit) {
			re = v.checkLicense();
		}
	}

	public static void load(boolean isShow) {
		if (!isExsit) {
			return;
		}
		if (Constants.STATE_VALID == re) {
			try {
				int reRemote = ServiceUtil.check(v.getLicenseId(), v.getMacCode(), v.getInstall());
				date = ServiceUtil.getTempEndDate(v.getLicenseId());
				if (reRemote == Constants.STATE_INVALID) {
					FileUtils.removeFile();
				}
				re = reRemote;
			} catch (Exception e) {
				e.printStackTrace();
				if (v.isTrial()) {
					Throwable t = e;
					while (t.getCause() != null) {
						t = t.getCause();
					}
					if (t instanceof java.security.cert.CertificateException) {
						re = Constants.EXCEPTION_INT16;
					} else {
						re = Constants.EXCEPTION_INT17;
					}
				}
			}
		}
	}

	public static void load() {
		new Thread(new Runnable() {
			public void run() {
				if (DateUtils.getDate().equals(dateTemp)) {
					return;
				} else {
					dateTemp = DateUtils.getDate();
				}

				if (FileUtils.isExsit()) {
					final LocalAuthorizationValidator v = new LocalAuthorizationValidator();
					re = v.checkLicense();
					if (Constants.STATE_VALID == re) {
						try {
							int reRemote = ServiceUtil.check(v.getLicenseId(), v.getMacCode(), v.getInstall());
							date = ServiceUtil.getTempEndDate(v.getLicenseId());
							if (reRemote == Constants.STATE_INVALID) {
								FileUtils.removeFile();
							}
							re = reRemote;
						} catch (Exception e) {
							e.printStackTrace();
							if (v.isTrial()) {
								Throwable t = e;
								while (t.getCause() != null) {
									t = t.getCause();
								}
								if (t instanceof java.security.cert.CertificateException) {
									re = Constants.EXCEPTION_INT16;
								} else {
									re = Constants.EXCEPTION_INT17;
								}
							}
						}
					}
				}
			}
		}).start();

	}

	public static String[] load(IProgressMonitor monitor) {
		String[] str = new String[3];
		if (monitor != null) {
			if (monitor.isCanceled()) {
				str[0] = String.valueOf(Constants.CANCEL);
				return str;
			}
			monitor.worked(1);
		}

		if (FileUtils.isExsit()) {
			if (monitor != null) {
				if (monitor.isCanceled()) {
					str[0] = String.valueOf(Constants.CANCEL);
					return str;
				}
				monitor.worked(1);
			}
			LocalAuthorizationValidator v = new LocalAuthorizationValidator();
			int re = v.checkLicense();
			if (monitor != null) {
				if (monitor.isCanceled()) {
					str[0] = String.valueOf(Constants.CANCEL);
					return str;
				}
				monitor.worked(2);
			}
			if (Constants.STATE_VALID == re) {
				try {
					str[2] = v.getLicenseId();
					int ret = ServiceUtil.check(v.getLicenseId(), v.getMacCode(), v.getInstall());
					str[0] = String.valueOf(ret);
					if (monitor != null) {
						if (monitor.isCanceled()) {
							str[0] = String.valueOf(Constants.CANCEL);
							return str;
						}
						monitor.worked(2);
					}
					String date = ServiceUtil.getTempEndDate(v.getLicenseId());
					if (monitor != null) {
						if (monitor.isCanceled()) {
							str[0] = String.valueOf(Constants.CANCEL);
							return str;
						}
						monitor.worked(2);
					}
					str[1] = date;
					return str;
				} catch (Exception e) {
					e.printStackTrace();
					if (v.isTrial()) {
						if (monitor != null) {
							if (monitor.isCanceled()) {
								str[0] = String.valueOf(Constants.CANCEL);
								return str;
							}
							monitor.worked(1);
						}
						Throwable t = e;
						while (t.getCause() != null) {
							t = t.getCause();
						}

						if (t instanceof java.security.cert.CertificateException) {
							str[0] = String.valueOf(Constants.EXCEPTION_INT16);
						} else {
							str[0] = String.valueOf(Constants.EXCEPTION_INT17);
						}
						return str;
					} else {
						if (monitor != null) {
							if (monitor.isCanceled()) {
								str[0] = String.valueOf(Constants.CANCEL);
								return str;
							}
							monitor.worked(1);
						}
						str[0] = String.valueOf(Constants.STATE_VALID);
						return str;
					}
				}
			} else if (Constants.STATE_INVALID == re) {
				if (monitor != null) {
					if (monitor.isCanceled()) {
						str[0] = String.valueOf(Constants.CANCEL);
						return str;
					}
					monitor.worked(1);
				}
				str[0] = String.valueOf(Constants.STATE_INVALID);
				return str;
			} else {
				if (monitor != null) {
					if (monitor.isCanceled()) {
						str[0] = String.valueOf(Constants.CANCEL);
						return str;
					}
					monitor.worked(1);
				}
				str[2] = v.getLicenseId();
				str[0] = String.valueOf(re);
				return str;
			}
		} else {
			if (monitor != null) {
				if (monitor.isCanceled()) {
					str[0] = String.valueOf(Constants.CANCEL);
					return str;
				}
				monitor.worked(1);
			}

			str[0] = String.valueOf(Constants.STATE_FILE_NOT_EXSIT);
			return str;
		}
	}

	public static void showDialog(String[] str) {
		int re = Integer.parseInt(str[0]);
		if (Constants.STATE_VALID == re) {
			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(),
					Constants.STATE_VALID, str[1], str[2]);
			dialog.open();
		} else if (Constants.STATE_INVALID == re) {
			FileUtils.removeFile();
			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(),
					Constants.STATE_INVALID, null, null);
			dialog.open();
		} else if (Constants.STATE_EXPIRED == re) {
			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(),
					Constants.STATE_EXPIRED, str[1], null);
			dialog.open();
		} else if (Constants.EXCEPTION_INT16 == re || Constants.EXCEPTION_INT17 == re) {
			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), re, null,
					str[2]);
			dialog.open();
		} else if (Constants.CANCEL == re) {
			return;
		} else if (Constants.STATE_FILE_NOT_EXSIT == re) {
			LicenseAgreementDialog dialog = new LicenseAgreementDialog(Display.getDefault().getActiveShell());
			dialog.open();
		} else if (Constants.EXCEPTION_INT15 == re || Constants.EXCEPTION_INT1 == re || Constants.EXCEPTION_INT2 == re
				|| Constants.EXCEPTION_INT3 == re || Constants.EXCEPTION_INT4 == re) {
			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), re, null, null);
			dialog.open();
		} else if (Constants.EXCEPTION_INT14 == re) {
			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), re, null,
					str[2]);
			dialog.open();
		} else {
			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), re, null,
					str[2]);
			dialog.open();
		}
	}

	public static boolean checkSystemResource() {
		if (re == Constants.STATE_VALID) {
			return true;
		}
		re = re == Constants.STATE_FILE_NOT_EXSIT ? Constants.STATE_NOT_ACTIVATED : re;
		return false;
	}

	public static void checkResult() {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				if (re == Constants.STATE_NOT_ACTIVATED) {
					InvalidateDialog dlg = new InvalidateDialog(Display.getDefault().getActiveShell(), re, null, null);
					dlg.open();
				} else if (re == Constants.STATE_INVALID) {
					InvalidateDialog dlg = new InvalidateDialog(Display.getDefault().getActiveShell(), re, null, null);
					dlg.open();
				} else if (re == Constants.STATE_EXPIRED) {
					InvalidateDialog dlg = new InvalidateDialog(Display.getDefault().getActiveShell(), re, date, v
							.getLicenseId());
					dlg.open();
				} else if (re == Constants.EXCEPTION_INT16 || re == Constants.EXCEPTION_INT17) {
					InvalidateDialog dlg = new InvalidateDialog(Display.getDefault().getActiveShell(), re, null, null);
					dlg.open();
				} else if (re == Constants.EXCEPTION_INT14) {
					InvalidateDialog dlg = new InvalidateDialog(Display.getDefault().getActiveShell(), re, null, v
							.getLicenseId());
					dlg.open();
				} else if (re == Constants.EXCEPTION_INT15) {
					InvalidateDialog dlg = new InvalidateDialog(Display.getDefault().getActiveShell(), re, null, null);
					dlg.open();
				} else if (re == Constants.EXCEPTION_INT1 || re == Constants.EXCEPTION_INT2
						|| re == Constants.EXCEPTION_INT3 || re == Constants.EXCEPTION_INT4) {
					InvalidateDialog dlg = new InvalidateDialog(Display.getDefault().getActiveShell(), re, null, null);
					dlg.open();
				} else {
					InvalidateDialog dlg = new InvalidateDialog(Display.getDefault().getActiveShell(), re, date, v
							.getLicenseId());
					dlg.open();
				}
			}
		});
	}

	public static boolean validateFile(File f) {
		long l = f.length();
		if (l > 10240 && !checkSystemResource()) {
			checkResult();
			if (!checkSystemResource()) {
				return false;
			}
		}
		return true;
	}
}
