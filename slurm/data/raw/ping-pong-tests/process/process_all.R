remove_outliers <- function(x, na.rm = TRUE, ...) {
  qnt <- quantile(x, probs=c(.25, .75), na.rm = na.rm, ...)
  H <- 1.5 * IQR(x, na.rm = na.rm)
  y <- x
  y[x < (qnt[1] - H)] <- NA
  y[x > (qnt[2] + H)] <- NA
  y
}


wd <- getwd()
setwd(wd)

fin <- NULL

dirs <- dir(".", pattern="ww")
allfiles <- c("processed_rdtrp.csv", "processed_pt2pt.csv")

for (af in allfiles) {
  
  files <- list.files(dirs, pattern=af, recursive=TRUE, full.names=TRUE)
  
  tmpdata <- read.csv(files[1], header=TRUE)
  
  nodeCount = nrow(tmpdata)
  colCnt <- ncol(tmpdata)
  
  t <- matrix(nrow=nodeCount,ncol=length(files))
  
  for (colIdx in 1:colCnt) {
    fileIdx <- 1
    for (f in files){
      fcsv <- read.csv(f, header=TRUE)
      
      if(is.null(fin)){
        fin = fcsv
      }
      
      if(colIdx > 1){
        
        print(paste("processing",colnames(fcsv)[colIdx],"in",f))
        
        for (i in 1:nodeCount){
          val <- tryCatch({
            val = fcsv[i,colIdx];
          },
          error = function(x) {
            return(-1)
          }
          )
          print(paste("val:", val))
          if(!is.na(val) && !is.null(val) && val >= 0){
            t[i,fileIdx] = val
          }
          orem <- remove_outliers(t[i,])
          m = mean(orem, na.rm = TRUE)
          fin[i,colIdx] = m
        }
      }
      fileIdx <- fileIdx + 1 
    }
  }
  
  write.csv(fin, file = af, row.names = FALSE)
  
}
