package com.nexuslogic.downloadmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexuslogic.downloadmanager.dao.DownloadTask;
import com.nexuslogic.downloadmanager.model.DownloadInfo;

@Service
public interface DownloadManagerService {

	String addNewDownload(DownloadInfo dt);

	List<DownloadTask> getStatusOfAllCurrentDownload();
	
	int getProgressDownloadId(int id);

	String pauseDownloadTaskId(int id);

	String resumeDownloadTaskId(int id);

	String cancelDownloadTaskId(int id);

	String getDownloadIdStatus(int id);

}