package essent

import collection.mutable.ArrayBuffer

// Directed graph class to be used as base for others
//  - uses numeric vertex identifiers (Int)
//  - tracks edges both in outgoing and incomming directions

class BareGraph {
  // TODO: define NodeID type for code documentation clarity
  type AdjacencyList = ArrayBuffer[ArrayBuffer[Int]]
  
  // Internal data structures
  //----------------------------------------------------------------------------
  // numeric vertex ID -> list of incoming vertex IDs (dependencies)
  val inNeigh: AdjacencyList = ArrayBuffer[ArrayBuffer[Int]]()
  // numeric vertex ID -> list outgoing vertex IDs (consumers)
  val outNeigh: AdjacencyList = ArrayBuffer[ArrayBuffer[Int]]()


  // Graph building
  //----------------------------------------------------------------------------
  def addEdge(sourceID: Int, destID: Int) {
    def growNeighsIfNeeded(id: Int, neighs: AdjacencyList) {
      assert(id >= 0)
      if (id >= neighs.size) {
        val numElemsToGrow = id - neighs.size + 1
        neighs.appendAll(ArrayBuffer.fill(numElemsToGrow)(ArrayBuffer[Int]()))
      }
    }
    val maxID = math.max(sourceID, destID)
    growNeighsIfNeeded(maxID, outNeigh)
    growNeighsIfNeeded(maxID, inNeigh)
    outNeigh(sourceID) += destID
    inNeigh(destID) += sourceID
  }

  def addEdgeIfNew(sourceID: Int, destID: Int) {
    if ((sourceID >= outNeigh.size) || !outNeigh(sourceID).contains(destID))
      addEdge(sourceID, destID)
  }


  // Mutators
  //----------------------------------------------------------------------------
  def removeDuplicateEdges() {
    // will not remove self-loops
    def uniquifyNeighs(neighs: AdjacencyList) {
      (0 until neighs.size) foreach { id => neighs(id) = neighs(id).distinct }
    }
    uniquifyNeighs(outNeigh)
    uniquifyNeighs(inNeigh)
  }

  def mergeNodesMutably(idsToMerge: Seq[Int]) {
    val mergedID = idsToMerge.head
    val idsToRemove = idsToMerge.tail
    def mergeNodeOneDirection(neighA: AdjacencyList, neighB: AdjacencyList) {
      val combinedDirNeigh = idsToMerge.flatMap(neighA(_)).distinct diff idsToMerge
      combinedDirNeigh foreach { id => {
        neighB(id) --= idsToRemove
        // TODO: reduce redundancy with AddEdgeIfNew
        if (!neighB(id).contains(mergedID)) neighB(id) += mergedID
      }}
      neighA(mergedID) = combinedDirNeigh.to[ArrayBuffer]
      idsToRemove foreach { deleteID => neighA(deleteID).clear() }
    }
    mergeNodeOneDirection(inNeigh, outNeigh)
    mergeNodeOneDirection(outNeigh, inNeigh)
  }


  // Stats
  //----------------------------------------------------------------------------
  def numNodes() = math.max(outNeigh.size, inNeigh.size)

  def computeDegrees(neighs: AdjacencyList) = {
    neighs map { _.size }
  }

  def numEdges() = computeDegrees(outNeigh).sum
}