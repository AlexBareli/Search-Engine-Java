package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A ThreadSafe implementation of an InvertedIndex using a custom MultiReaderLock lock object, that extends the InvertedIndex class,
 * for use with MultiThreading.
 *
 * @author Alexander Bareli
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
	
	/** The lock used to protect concurrent access to the underlying InvertedIndex */
	private final MultiReaderLock lock;
	
	/**
	 * Initializes the thread-safe InvertedIndex
	 */
	public ThreadSafeInvertedIndex() {
		super();
		this.lock = new MultiReaderLock();
	}
		
	@Override
	public List<QueryMetaData> exactSearch(Set<String> queries){
		lock.readLock().lock();
		try {
			return super.exactSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public List<QueryMetaData> partialSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.partialSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public void add(String stem, String locations, int positions) {
		lock.writeLock().lock();
		try {
			super.add(stem, locations, positions);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public void addAll(InvertedIndex index) {
		lock.writeLock().lock();
		try {
			super.addAll(index);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public Map<String, Number> getCounts(){
		lock.readLock().lock();
		try {
			return super.getCounts();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public Integer getCount(String locations) {
		lock.readLock().lock();
		try {
			return super.getCount(locations);
		} finally {
			lock.readLock().unlock();
		}
	}
			
	@Override
	public Set<String> getWords(){ 
		lock.readLock().lock();
		try {
			return super.getWords();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public Set<String> getPaths(String word) {
		lock.readLock().lock();
		try {
			return super.getWords();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public Collection<Integer> getPositions(String word, String locations) {
		lock.readLock().lock();
		try {
			return super.getPositions(word, locations);
		} finally {
			lock.readLock().unlock();
		}
	}
		
	@Override
	public int countsSize() {
		lock.readLock().lock();
		try {
			return super.countsSize();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public int indexSize() {
		lock.readLock().lock();
		try {
			return super.indexSize();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int indexWordSize(String word) {
		lock.readLock().lock();
		try {
			return super.indexWordSize(word);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public int indexPositionsSize(String word, String locations) {
		lock.readLock().lock();
		try {
			return super.indexPositionsSize(word, locations);
		} finally {
			lock.readLock().unlock();
		}
	}
			
	@Override
	public boolean containsCount(String locations) {
		lock.readLock().lock();
		try {
			return super.containsCount(locations);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public boolean hasWord(String word) {
		lock.readLock().lock();
		try {
			return super.hasWord(word);
		} finally {
			lock.readLock().unlock();
		}
	}
		
	@Override
	public boolean hasStemFile(String word, String locations) {
		lock.readLock().lock();
		try {
			return super.hasStemFile(word, locations);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public boolean hasStemFilePosition(String word, String locations, int position) {
		lock.readLock().lock();
		try {
			return super.hasStemFilePosition(word, locations, position);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public void writeIndex(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.writeIndex(path);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public void writeCounts(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.writeCounts(path);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			return super.toString();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public String countsToString() {
		lock.readLock().lock();
		try {
			return super.countsToString();
		} finally {
			lock.readLock().unlock();
		}
	}
}
