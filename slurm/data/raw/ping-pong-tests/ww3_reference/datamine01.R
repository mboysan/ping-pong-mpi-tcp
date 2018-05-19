#install.packages("rstudioapi")
#wd <- dirname(rstudioapi::getSourceEditorContext()$path)
wd <- getwd()
setwd(wd)

nodesCol <- c()
patterns <- c("socket500_", "mpi500_", "mpiontcp500_")

allRDTRP <- c()
allP2P <- c()
for (pat in patterns){
  nodesCol <- c()
  resRDTRP <- c()
  resP2P <- c()
  
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
    
    #----------------- rdtrp data
    files <- list.files(d, pattern="^rdtrp.csv", recursive=TRUE, full.names=TRUE)
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
    resRDTRP <- c(resRDTRP, dat)
    
    #----------------- Pt2Pt data
    files <- list.files(d, pattern="^p2p.csv", recursive=TRUE, full.names=TRUE)
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
    resP2P <- c(resP2P, dat)
  }
  allRDTRP <- c(allRDTRP, resRDTRP)
  allP2P <- c(allP2P, resP2P)
}
allRDTRP <- c(nodesCol, allRDTRP)
allP2P <- c(nodesCol, allP2P)

print(allRDTRP)
print(allP2P)

t <- matrix(allRDTRP,ncol=4,byrow=FALSE)
colnames(t) <- c("nodecount","socket_tcp","mpi_infini","mpi_tcp")
rownames(t) <- nodesCol
t <- as.data.frame(t)
t <- t[order(t$nodecount),]
write.csv(t, file = "./processed_rdtrp.csv", row.names = FALSE)

t <- matrix(allP2P,ncol=4,byrow=FALSE)
colnames(t) <- c("nodecount","socket_tcp","mpi_infini","mpi_tcp")
rownames(t) <- nodesCol
t <- as.data.frame(t)
t <- t[order(t$nodecount),]
write.csv(t, file = "./processed_pt2pt.csv", row.names = FALSE)
