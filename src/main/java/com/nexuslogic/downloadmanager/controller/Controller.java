package com.nexuslogic.downloadmanager.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nexuslogic.downloadmanager.dao.DownloadTask;
import com.nexuslogic.downloadmanager.model.DownloadInfo;
import com.nexuslogic.downloadmanager.serviceImpl.DownloadManagerServiceImpl;

@RestController
public class Controller {
	
	/*POST /downloads: Add a new file to download.
	GET /downloads: Get the status of all current downloads.
	GET /downloads/{id}: Get the progress of a specific download.
	PUT /downloads/{id}/pause: Pause a download.
	PUT /downloads/{id}/resume: Resume a paused download.
	DELETE /downloads/{id}: Cancel a download. */
	 
	@Autowired
	private DownloadTask downloadTask;
	
	@Autowired
	private DownloadManagerServiceImpl downloadManagerServiceImpl;
	
	@PostMapping("/downloads")
	public ResponseEntity<DownloadTask> startdownload(@RequestBody DownloadInfo dt) {
		String id = downloadManagerServiceImpl.addNewDownload(dt);
		downloadTask.setId(Integer.valueOf(id));
		return new ResponseEntity<DownloadTask>(downloadTask ,HttpStatusCode.valueOf(201));
	}
	
	
	@GetMapping("/downloads/{id}/progress")
	public ResponseEntity<DownloadTask> getProgressDownloadId(@PathVariable int id) {
		downloadTask.setProgress(downloadManagerServiceImpl.getProgressDownloadId(id));
		return new ResponseEntity<DownloadTask>(downloadTask ,HttpStatusCode.valueOf(200));
	}
	
	@GetMapping("/downloads/{id}")
	public ResponseEntity<DownloadTask> getDownloadStatusId(@PathVariable int id) {
		String currentStatus = downloadManagerServiceImpl.getDownloadIdStatus(id);
		if(currentStatus.isEmpty() || currentStatus.isBlank()) {
			return new ResponseEntity<DownloadTask>(downloadTask ,HttpStatusCode.valueOf(202));
		}
		
		downloadTask.setStatus(downloadManagerServiceImpl.getDownloadIdStatus(id));
		downloadTask.setId(id);
		return new ResponseEntity<DownloadTask>(downloadTask ,HttpStatusCode.valueOf(200));
	}
	
	
	@PutMapping("downloads/{id}/pause")
	public void pauseDownload(@PathVariable int id) {
		
	}
	
	
	@PutMapping("downloads/{id}/resume")
	public void resumeDownload(@PathVariable int id) {
		
	}
	
	
	@DeleteMapping("/downloads/{id}")
	public void deleteDownload(@PathVariable int id) {
		
	}
	
}
