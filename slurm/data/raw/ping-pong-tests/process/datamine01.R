#install.packages("rstudioapi")
#wd <- dirname(rstudioapi::getSourceEditorContext()$path)
wd <- getwd()
setwd(wd)

outfiles <- c("./processed_rdtrp.csv", "./processed_pt2pt.csv")
filePatterns <- c("^rdtrp.csv", "^p2p.csv")
nodesCol <- c()
patterns <- c("socket500_", "mpi500_", "mpiontcp500_")

outIdx <- 1;
for(fpat in filePatterns){
  results <- c()
  for (pat in patterns){
    nodesCol <- c()
    res <- c()
    
    dirs <- dir(".", pattern=pat)
    idx <- -1
    for (d in dirs) {
      idx <- idx + 1
      if(idx == 0){
        nodeCount <- 10
      } else {
        nodeCount <- idx
      }
      nodesCol <- c(nodesCol, nodeCount)
      
      files <- list.files(d, pattern=fpat, recursive=TRUE, full.names=TRUE)
      dat <- c()
      for(f in files){
        m <- tryCatch({
          fcsv <- read.csv(f, header=FALSE)
          m <- mean(fcsv$V7, na.rm = TRUE)
        },
        error = function(x) {
          return(-1)
        }
        )
        print(paste(f,":",m))
        dat <- c(dat, m)
      }
      res <- c(res, dat)
    }
    results <- c(results, res)
  }
  results <- c(nodesCol, results)
  
  print(results)
  
  t <- matrix(results,ncol=4,byrow=FALSE)
  colnames(t) <- c("nodecount","socket_tcp","mpi_infini","mpi_tcp")
  rownames(t) <- nodesCol
  t <- as.data.frame(t)
  t <- t[order(t$nodecount),]
  write.csv(t, file = outfiles[outIdx], row.names = FALSE)
  
  outIdx <- outIdx + 1;
}
