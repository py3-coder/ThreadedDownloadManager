package com.nexuslogic.downloadmanager.serviceImpl;


import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

import org.springframework.stereotype.Service;

@Service
public class DownloadFile {
    
	private static final Logger logger = Logger.getLogger(DownloadFile.class.getName());
    private static final int THREAD_POOL_SIZE = 3; 
    private static final int QUEUE_SIZE = THREAD_POOL_SIZE * 2; 
    private final ConcurrentHashMap<Integer, String> downloadStatusMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Integer> downloadProgressMap = new ConcurrentHashMap<>();

    
    private final static ThreadPoolExecutor executorService = new ThreadPoolExecutor(
    		THREAD_POOL_SIZE, 
            THREAD_POOL_SIZE, 
            60L, TimeUnit.SECONDS, 
            new ArrayBlockingQueue<>(QUEUE_SIZE),
            new DownloadRejectedExecutionHandler()
    		) {
    	 @Override
         protected void afterExecute(Runnable r, Throwable t) {
             super.afterExecute(r, t);
             checkQueueForPendingTasks();
    	 }
    };
    
    // check queue for pending task
    private static void checkQueueForPendingTasks() {
    	logActiveThreads();
        if (!executorService.getQueue().isEmpty()) {
            Runnable task = executorService.getQueue().poll();
            if (task != null) {
                executorService.execute(task);
            }
        }
    }
    
    // start the download process
    public String startDownload(String fileUrl) {
    	
    	 logActiveThreads();
    	 startLoggingActiveThreads();
    	 int downloadId = generateUniqueId(fileUrl);
         logger.info("Starting download for URL: " + fileUrl + " with ID: " + downloadId);

         DownloadItem downloadItem = new DownloadItem(fileUrl, downloadId);
         if (executorService.getActiveCount() < THREAD_POOL_SIZE) {
             executorService.execute(downloadItem);
             logActiveThreads();
         } else {
             boolean addedToQueue = executorService.getQueue().offer(downloadItem);
             if (!addedToQueue) {
                 logger.warning("Task queue is full. Download request rejected for URL: " + fileUrl);
             }
         }
         logger.info("Current download status: " + downloadStatusMap);
         return String.valueOf(downloadId);
    }
    
    // Download file in a separate thread
    private void downloadFile(String fileUrl, int downloadId) {
        downloadStatusMap.put(downloadId, "InProgress");
        try {
            // Directory to store downloaded file
        	logActiveThreads();
            String downloadDirectory = "E:" + File.separator + "Java" + File.separator + "SpringBoot";
            File downloadFolder = new File(downloadDirectory);
            if (!downloadFolder.exists()) {
                downloadFolder.mkdirs();
            }
            //checking file extension  
            String fileExtension = getFileExtensionFromUrl(fileUrl);
            if (fileExtension.isEmpty()) {
                downloadStatusMap.put(downloadId, "Failed");
                logger.warning("Unable to determine file extension for " + fileUrl);
                return;
            }
            
            // Setup URL and connection
            URL url = new URL(fileUrl);
            URLConnection connection = url.openConnection();
            int totalBytes = connection.getContentLength();
            if (totalBytes == -1) {
                logger.warning("Content length is unknown.");
            }
            logger.info("Total File Size: " + totalBytes * 1.0 / (1024.0 * 1024.0) + " MB");
        
            
          
            File outputFile = new File(downloadFolder, downloadId + "." + fileExtension);
            logger.info("Download file path: " + outputFile.getAbsolutePath());
            
            // Download file and track progress
            int bytesDownloaded = 0;
            int lastLoggedProgress = 0;
            
            // Open input stream and set output file path
            InputStream in = connection.getInputStream();

            
            try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    bytesDownloaded += bytesRead;

                    // Calculate and log download progress percentage
                    int progress = (int) ((bytesDownloaded / (double) totalBytes) * 100);
                    
                    downloadProgressMap.put(downloadId, progress);
                    if (progress >= lastLoggedProgress + 10) {
                        lastLoggedProgress = progress - (progress % 10); 
                        logActiveThreads();
                        logger.info("Download Progress for ID " + downloadId + ": " + lastLoggedProgress + "%");
                    }

                    // If progress reaches 100%, mark as complete
                    if (progress >= 100) {
                        downloadProgressMap.put(downloadId, 100);
                        downloadStatusMap.put(downloadId, "Completed");
                        break;
                    }
                }
                downloadStatusMap.put(downloadId, "Completed");
                downloadProgressMap.put(downloadId, 100);
                logger.info("Download completed: " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                downloadStatusMap.put(downloadId, "Failed: " + e.getMessage());
                logger.severe("Error while downloading file: " + e.getMessage());
            } finally {
                in.close();
            }
        } catch (IOException e) {
            downloadStatusMap.put(downloadId, "Failed: " + e.getMessage());
            logger.severe("Error with URL or connection: " + e.getMessage());
        }
        finally {
        	shutdown();
		}
    }


    //  method to get file extension from the URL
    private String getFileExtensionFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            int dotIndex = path.lastIndexOf(".");
            if (dotIndex != -1) {
                return path.substring(dotIndex + 1);  
            } else {
                return "";  
            }
        } catch (MalformedURLException e) {
            return "";  
        }
    }

    // Retrieve the download status using download ID
    public String getDownloadStatus(int downloadId) {
    	if(downloadStatusMap.containsKey(downloadId)) {
    		return downloadStatusMap.get(downloadId);
    	}else if(isDownloadInQueue(downloadId)) {
    		return "InQueue";
    	}
        return "No Download downl ID";
    }

    
    public boolean isDownloadInQueue(int downloadId) {
        for (Runnable task : executorService.getQueue()) {
            if (task instanceof DownloadItem) {
            	DownloadItem downloadItem = (DownloadItem) task;
                if (downloadItem.getDownloadId() == downloadId) {
                    return true; 
                }
            }
        }
        return false;
    }
    
    //get download progress
    public int getDownloadProgress(int downloadId) {
    	if(downloadProgressMap.containsKey(downloadId)) {
    		return downloadProgressMap.get(downloadId);
    	}else if(isDownloadInQueue(downloadId)) {
    		return 0;
    	}
    	return -1;
    }
    
    // Generate a unique download ID based on the file URL
    public int generateUniqueId(String input) {
        int hashCode = input.hashCode();
        return hashCode & 0x7FFFFFFF;  
    }

    public void shutdown() {
        logger.info("Shutting down ExecutorService...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warning("Timeout reached, forcing shutdown now...");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

  
    public static void logActiveThreads() {
		if (executorService instanceof ThreadPoolExecutor) {
		    ThreadPoolExecutor pool = (ThreadPoolExecutor) executorService;
		    logger.info("Active threads: " + pool.getActiveCount() + ", Total threads: " + pool.getPoolSize());
		}
    }

    public void startLoggingActiveThreads() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(()  -> logActiveThreads(), 0, 10, TimeUnit.SECONDS);
    }
    private static class DownloadRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            DownloadFile.logger.warning("Task rejected due to full queue and pool capacity.");
        }
    }
    
    //class to push the data in queue :-  
    private class DownloadItem implements Runnable {
        private final String fileUrl;
        private final int downloadId;

        public DownloadItem(String fileUrl, int downloadId) {
            this.fileUrl = fileUrl;
            this.downloadId = downloadId;
        }

        public int getDownloadId() {
			return this.downloadId;
		}

		@Override
        public void run() {
            downloadStatusMap.put(downloadId, "InProgress");
            downloadFile(fileUrl, downloadId);
        }
    }
}

