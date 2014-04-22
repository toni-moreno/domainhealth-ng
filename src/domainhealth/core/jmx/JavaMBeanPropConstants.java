//Copyright (C) 2008-2013 Paul Done . All rights reserved.
//This file is part of the DomainHealth software distribution. Refer to the  
//file LICENSE in the root of the DomainHealth distribution.
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
//AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
//ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE 
//LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
//SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
//INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
//CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
//POSSIBILITY OF SUCH DAMAGE.
package domainhealth.core.jmx;

/**
 * Common Java Lang MBeans property names.
 */
public interface JavaMBeanPropConstants {
	/*
	 * These are not real  MBean  name properties Only ID's for metrics.
	 */
	public final static String J_CURRENT_LOADED_CLASS_COUNT	= "CL.CurrentClassCount";
	public final static String J_TOTAL_LOADED_CLASS_COUNT  	= "CL.TotalLoadedClassCount";
	public final static String J_TOTAL_UNLOADED_CLASS_COUNT	= "CL.TotalUnloadedClassCount";

	public final static String J_TOTAL_COMPILATION_TIME_CLASS  = "CMP.CompTotalTime";

	public final static String J_OLD_COLLECTION_COUNT	= "GC.OldCollectionCount";
	public final static String J_OLD_COLLECTION_TIME	= "GC.OldCollectionTime";
	public final static String J_YOUNG_COLLECTION_COUNT	= "GC.YoungCollectionCount";
	public final static String J_YOUNG_COLLECTION_TIME	= "GC.YoungCollectionTime";

	public final static String J_HEAP_COMMITTED  		= "Mem.Heap.Committed";
	public final static String J_HEAP_INIT  		= "Mem.Heap.Init";
	public final static String J_HEAP_MAX  			= "Mem.Heap.Max";
	public final static String J_HEAP_USED  		= "Mem.Heap.Used";

	public final static String J_NOT_HEAP_COMMITTED  	= "Mem.NonHeap.Committed";
	public final static String J_NOT_HEAP_MAX 		= "Mem.NonHeap.Max";
	public final static String J_NOT_HEAP_INIT 		= "Mem.NonHeap.Init";
	public final static String J_NOT_HEAP_USED 		= "Mem.NonHeap.Used";

	public final static String J_MEMPOOL_CM_COMMITTED 	= "MemPool.Class.Committed";
	public final static String J_MEMPOOL_CM_MAX 		= "MemPool.Class.Max";
	public final static String J_MEMPOOL_CM_INIT 		= "MemPool.Class.Init";
	public final static String J_MEMPOOL_CM_USED 		= "MemPool.Class.Used";

	public final static String J_MEMPOOL_CB_COMMITTED 	= "MemPool.ClassBlock.Committed";
	public final static String J_MEMPOOL_CB_MAX 		= "MemPool.ClassBlock.Max";
	public final static String J_MEMPOOL_CB_INIT 		= "MemPool.ClassBlock.Init";
	public final static String J_MEMPOOL_CB_USED 		= "MemPool.ClassBlock.Used";
	
	public final static String J_MEMPOOL_NURSERY_COMMITTED 	= "MemPool.Nursery.Committed";
	public final static String J_MEMPOOL_NURSERY_MAX 	= "MemPool.Nursery.Max";
	public final static String J_MEMPOOL_NURSERY_INIT 	= "MemPool.Nursery.Init";
	public final static String J_MEMPOOL_NURSERY_USED 	= "MemPool.Nursery.Used";


	public final static String J_MEMPOOL_OLD_COMMITTED 	= "MemPool.Old.Committed";
	public final static String J_MEMPOOL_OLD_MAX 		= "MemPool.Old.Max";
	public final static String J_MEMPOOL_OLD_INIT 		= "MemPool.Old.Init";
	public final static String J_MEMPOOL_OLD_USED 		= "MemPool.Old.Used";

	public final static String J_CUR_DAEMON_THREAD_COUNT 	= "Thread.DaemonCurCount";
	public final static String J_CUR_NON_DAEMON_THREAD_COUNT = "Thread.NonDaemonCurCount";
	public final static String J_CUR_TOTAL_THREAD_COUNT 	= "Thread.TotalCurCount";
	public final static String J_TOTAL_STARTED_THREAD_COUNT ="Thread.TotalStartedCount";


}
