package com.nexuslogic.downloadmanager.dao;

import org.springframework.stereotype.Component;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Component
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DownloadTask {

	@Id
	private int id ;
	private String url;
	private String status;
	private int progress ;
	
}
