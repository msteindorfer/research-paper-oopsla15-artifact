package org.rascalmpl.interpreter.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class Timing {

	public static long getCpuTime( ) {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    return bean.isCurrentThreadCpuTimeSupported( ) ?
	        bean.getCurrentThreadCpuTime( ) : 0L;
	}

}
