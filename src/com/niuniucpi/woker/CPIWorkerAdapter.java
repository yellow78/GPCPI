package com.niuniucpi.woker;

import com.akdeniz.googleplaycrawler.GooglePlayAPI;

public abstract class CPIWorkerAdapter {
	public abstract void LoginSucceed(CPIWorker sender, GooglePlayAPI service);
	public abstract void LoginFailed(CPIWorker sender, GooglePlayAPI service, Exception e);
	public abstract void DownloadCompleted(CPIWorker sender);
	public abstract void DownloadFailed(CPIWorker sender);
	public abstract void StartDownload();
	public abstract void DisplayMessage(String message);
}
