package com.nexuslogic.downloadmanager.serviceImpl;

import java.util.ArrayList;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nexuslogic.downloadmanager.dao.DownloadTask;
import com.nexuslogic.downloadmanager.model.DownloadInfo;
import com.nexuslogic.downloadmanager.service.DownloadManagerService;


@Service
public class DownloadManagerServiceImpl implements DownloadManagerService {
		
	 @Autowired
	 private DownloadFile downloadFile;
	
	
	@Override
	public String addNewDownload(DownloadInfo dt){
		return downloadFile.startDownload(dt.getUrl());
	}
	
	
	@Override
	public List<DownloadTask> getStatusOfAllCurrentDownload() {
		return new ArrayList<DownloadTask>();
	}
	
	
	@Override
	public String getDownloadIdStatus(int id) {
		return downloadFile.getDownloadStatus(id);
	}
	
	@Override
	public String pauseDownloadTaskId(int id) {
		return "";
	}
	
	@Override
	public String resumeDownloadTaskId(int id) {
		return "";
	}
	
	@Override
	public String cancelDownloadTaskId(int id) {
		return "";
	}

	@Override
	public int getProgressDownloadId(int id) {
		return downloadFile.getDownloadProgress(id);
	}
	
}

