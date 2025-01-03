package com.nexuslogic.downloadmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexuslogic.downloadmanager.dao.DownloadTask;


public interface DownloadManagerRepo extends  JpaRepository<DownloadTask, Integer> {

}
