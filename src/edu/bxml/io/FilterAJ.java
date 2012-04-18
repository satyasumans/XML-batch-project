package edu.bxml.io;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.browsexml.core.XMLBuildException;
import com.browsexml.core.XmlObject;
import com.browsexml.core.XmlParser;
import com.javalobby.tnt.annotation.attribute;

/**
 * A class must extend filter to participate in a pipeline.  Filter can
 * be used stand alone as a basic copy from System.in to System.out.  All 
 * output of a filter's children will append to the output of the parent
 * filter.
 * @author geoff.ritchey
 *
 */
@attribute(value = "", required = true)
public class FilterAJ extends XmlObject implements Runnable {
	private static Log log = LogFactory.getLog(FilterAJ.class);
	protected InputStream in;
	protected OutputStream out;
	protected String text = null;
	protected boolean lock = false;
	protected boolean isHereFile = false;

	public boolean isHereFile() {
		return isHereFile;
	}

	public void setHereFile(boolean isHereFile) {
		this.isHereFile = isHereFile;
	}

	public boolean getLock() {
		return lock;
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}

	protected String file = null;
	protected String dir = null;
	protected String toFile = null;
	public static Log getLog() {
		return log;
	}

	public static void setLog(Log log) {
		FilterAJ.log = log;
	}

	protected String toDir = null;
	protected String archive = null;
	protected File currentFile = null;  
	protected File outFile = null;

	protected List<FilterAJ> filters = new ArrayList<FilterAJ>();
	
	Boolean closeIn = false;
	Boolean closeOut = false;
	
	public Boolean getCloseIn() {
		return closeIn;
	}

	public void setCloseIn(Boolean closeIn) {
		this.closeIn = closeIn;
	}

	public Boolean getCloseOut() {
		return closeOut;
	}

	public void setCloseOut(Boolean closeOut) {
		this.closeOut = closeOut;
	}

	public void closeIn() {
		if (closeIn)
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public void closeOut() {
		if (closeOut) {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public InputStream getIn() throws XMLBuildException {
		FilterAJ ancestor = this.getAncestorOfType(FilterAJ.class);
		log.debug("get IN for " + this.getClass().getName());
		if (in != null) { 
			return in;
		}
		if (ancestor != null) {
			log.debug("ancestor class is " + ancestor.getClass().getName());
			this.in = ancestor.getIn();
		}
		
		if (getDir() != null) {
			if (getFile() != null) {
				try {
					closeIn = true;
					file = XmlParser.processAttributes(this, this.getFile());
					in = new FileInputStream(new File(this.getDir(), this.getFile()));
					getLog().debug("Filter file = " + getDir() + "/" + getFile());
					log.debug("get IN DONE 1");
					return in;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					throw new XMLBuildException(e.getMessage());
				}
			}
		}
		else if (getDir() == null || getFile() == null){
			//<<HERE file
			if (getText() != null && isHereFile()) {
				log.debug("<<HERE file");
				in = new ByteArrayInputStream(getText().getBytes());
			}
		}
		
		if (in == null) {
			log.debug("System.in");
			in = System.in;
		}
		log.debug("get IN DONE 2");
		return in;
	}

	public OutputStream getOut() {
		FilterAJ ancestor = this.getAncestorOfType(FilterAJ.class);
		log.debug("get OUT for " + this.getClass().getName());
		if (out != null) {
			return out;
		}
		if (ancestor != null) {
			log.debug("ancestor class is " + ancestor.getClass().getName());
			this.out = ancestor.getOut();
		}

		try {
			if (getToDir() != null && getToFile() != null){
				outFile = new File(toDir, toFile);
				out = new FileOutputStream(outFile);
				closeOut = true;
				return out;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		if (out == null) {
			out = System.out;
		}
		return out;
	}

	public FilterAJ() {
	}

	public FilterAJ(InputStream is, OutputStream os) {
		in = is;
		out = os;
	}

	public void setInputStream(InputStream in) {
		log.debug(" set in to " + in);
		this.in = in;
	}

	public void setOutputStream(OutputStream out) {
		this.out = out;
	}
	
	public void finish(OutputStream out) {
		try {
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	/**
	 * Standard input will come from the dir/file combination.
	 * 
	 * @param file
	 */
	@attribute(value = "", required = false, defaultValue="input will come from another class in the pipeline.")
	public void setFile(String file) {
		this.file = file;
	}
	/**
	 * Standard output will go to the toDir/toFile combination.
	 * 
	 * @param file
	 */
	@attribute(value = "", required = false, defaultValue="output will go to the next class in the pipeline or standard output.")
	public void setToDir(String toDir) {
		log.debug("to dir set to " + toDir);
		this.toDir = toDir;
		if (toFile != null) {
			outFile = new File(toDir, toFile);
		}
	}
	
	/**
	 * Standard output will go to the toDir/toFile combination.
	 * 
	 * @param file
	 */
	@attribute(value = "", required = false, defaultValue="output will go to the next class in the pipeline or standard output.")
	public void setToFile(String toFile) {
		this.toFile = toFile;
		if (toDir != null) {
			outFile = new File(toDir, toFile);
		}
	}

	public File getOutFile() {
		return outFile;
	}

	public void setOutFile(File outFile) {
		this.outFile = outFile;
	}

	/**
	 * Move input file to an archive directory.  The archive directory
	 * will be created if it does not exist.
	 * 
	 * @param file
	 */
	@attribute(value = "", required = false, defaultValue="archive directory.  The input file, if specified, will be moved to the directory named.")
	public void setArchive(String archive) {
		this.archive = archive;
	}

	/**
	 * Standard input will come from the dir/file combination.
	 * 
	 * @param file
	 */
	@attribute(value = "", required = false, defaultValue="input will come from another class in the pipeline.")
	public void setDir(java.lang.String dir) {
		this.dir = dir;
	}

	public void run() {
		try {
			log.debug("Starting THREAD: " + getName() + "  " + this.getClass().getName());
			execute();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (XMLBuildException e) {
			e.printStackTrace();
		} 
		finally {
		
			log.debug("Ending THREAD: " + getName() + "  " + this.getClass().getName());
			try {
				out.flush();
				if (!(out instanceof FileOutputStream))
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			log.debug("Ended THREAD: " + getName() + "  " + this.getClass().getName());
		}
	}

	public void execute() throws XMLBuildException  {
		log.debug("filterAJ execute");
		if (in != null) {
			try {
				 byte[] buf = new byte[4096];
				 log.debug("filterAJ execute for...");
				 int len = in.read(buf);
				for (; len > 0; len = in.read(buf)) {
					out.write(buf, 0, len);
				} 
				log.debug("filterAJ finish");
				finish(out);
				log.debug("filterAJ finish DONE");

			} 
			catch (IOException e) {
				e.printStackTrace();
				throw new XMLBuildException(e.getMessage());
			}
		}
		for (FilterAJ filter : filters) {
			log.debug("Filter = " + filter);
			log.debug("Filter in = " + in);
			log.debug("Filter out = " + out);
			filter.setInputStream(in);
			filter.setOutputStream(out);
			filter.execute();
		}
		log.debug("FilterAJ execute DONE");
	}

	public String getText() {
		return text;
	}

	public String getFile() {
		return file;
	}

	public String getDir() {
		return dir;
	}

	public String getToFile() {
		return toFile;
	}

	public String getToDir() {
		return toDir;
	}

	public void setIn(InputStream in) {
		this.in = in;
	}

	public void setOut(OutputStream out) {
		this.out = out;
	}

	public void check() throws XMLBuildException {
	}

	public void setText(String text) {
	}

	public void setFromTextContent(String text) {
		this.text = text;
	}
	
	public InputStream getInFilter(InputStream in) throws IOException {
		return in;
	}
	
	public OutputStream getOutFilter(OutputStream out) throws IOException {
		return out;
	}
	
	public File getCurrentFile() {
		return currentFile;
	}

}
