package org.eclipse.mcp.acp.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * Too simplistic, temporary decorator.
 * See: /common/org/eclipse/swt/custom/RTFWriter.java
 */
public class MarkdownWriter {

	Pattern header = Pattern.compile("^(#{1,6})\\s+(.+)$");
	Pattern bold = Pattern.compile("\\*\\*(.+?)\\*\\*|__(.+?)__");
	Pattern italic = Pattern.compile("\\*(.+?)\\*|_(.+?)_");
//	Pattern strikethrough = Pattern.compile("~~(.+?)~~");
//	Pattern inline = Pattern.compile("`(.+?)`");
//	Pattern unorderedList = Pattern.compile("^\\s*[-+*]\\s+(.+)$");
//	Pattern orderedList = Pattern.compile("^\\s*\\d+\\.\\s+(.+)$");
//	Pattern blockquote = Pattern.compile("^>\\s*(.+)$");
//	Pattern codeBlock = Pattern.compile("^```(?:\\s*(\\w+))?([\\s\\S]*?)^```$");
	
	IOConsoleOutputStream normalStream, italicStream, boldStream, boldItalicStream;
	
	public MarkdownWriter(AcpConsole acpConsole) {

		normalStream = acpConsole.newOutputStream();
		
		italicStream = acpConsole.newOutputStream();
		italicStream.setFontStyle(SWT.ITALIC);
		
		boldStream = acpConsole.newOutputStream();
		boldStream.setFontStyle(SWT.BOLD);
		
		boldItalicStream = acpConsole.newOutputStream();
		boldItalicStream.setFontStyle(SWT.BOLD | SWT.ITALIC);
			
	}


	public void write(String s) {
		
		List<Range> ranges = new ArrayList<Range>();
		Set<Integer> skips = new HashSet<Integer>();
		
//		Matcher matcher = header.matcher(s);
//		while (matcher.find()) {
//			 for (int i = 0; i < 6; i++) {
//				 if (s.charAt(matcher.start() + i) == '#') {
//					 skips.add(matcher.start() + i);
//				 } else {
//					 break;
//				 }
//			 }
//			 ranges.add(new Range(matcher.start(), matcher.end(), boldItalicStream));
//		 }
		
		Matcher matcher = bold.matcher(s);
		while (matcher.find()) {
			if (!hasRangeAlready(ranges, matcher)) {
				 ranges.add(new Range(matcher.start(), matcher.end(), boldStream));
				 skips.add(matcher.start()); 
				 skips.add(matcher.start() + 1);
				 skips.add(matcher.end() - 2);
				 skips.add(matcher.end() - 1);
			}
		 }
		
		matcher = italic.matcher(s);
		while (matcher.find()) {
			if (!hasRangeAlready(ranges, matcher)) {
				ranges.add(new Range(matcher.start(), matcher.end(), italicStream));
				skips.add(matcher.start()); 
				skips.add(matcher.end() - 1);
			}
		 }
		
		for (int i = 0; i < s.length(); i++) {
			if (!skips.contains(i)) {
				boolean written = false;
				for (Range range: ranges) {
					if (i >= range.start && i < range.end) {
						write(range.stream, s.charAt(i));
						written = true;
						break;
					}
				}
				if (!written) {
					write(normalStream, s.charAt(i));
				}
			}
		}
	}
	
	private void write(IOConsoleOutputStream stream, char c) {
		if (!stream.isClosed()) {
			try {
				stream.write(c);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean hasRangeAlready(List<Range> ranges, Matcher matcher) {
		for (Range range: ranges) {
			if (range.start <= matcher.start() && range.end >= matcher.end()) {
				return true;
			}
		}
		return false;
	}

	class Range {
		int start;
		int end;
		IOConsoleOutputStream stream;
		
		public Range(int start, int end, IOConsoleOutputStream stream) {
			super();
			this.start = start;
			this.end = end;
			this.stream = stream;
		}
	}
	
	public void dispose() {
		dispose(normalStream);
		dispose(italicStream);
		dispose(boldStream);
		dispose(boldItalicStream);
	}
	
	private void dispose(IOConsoleOutputStream stream) {
		if (stream != null && !stream.isClosed()) {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
