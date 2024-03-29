package com.digero.common.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import sun.awt.shell.ShellFolder;

public final class Util {
	private Util() {
		// Can't instantiate class
	}

	public static Color grayscale(Color orig) {
		float[] hsb = Color.RGBtoHSB(orig.getRed(), orig.getGreen(), orig.getBlue(), null);
		return Color.getHSBColor(0.0f, 0.0f, hsb[2]);
	}

	public static final String ELLIPSIS = "...";

	@SuppressWarnings("deprecation")
	public static String ellipsis(String text, float maxWidth, Font font) {
		FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
		Pattern prevWord = Pattern.compile("\\w*\\W*$");
		Matcher matcher = prevWord.matcher(text);

		float width = metrics.stringWidth(text);
		if (width < maxWidth)
			return text;

		int len = 0;
		int seg = text.length();
		String fit = "";

		// find the longest string that fits into
		// the control boundaries using bisection method 
		while (seg > 1) {
			seg -= seg / 2;

			int left = len + seg;
			int right = text.length();

			if (left > right)
				continue;

			// trim at a word boundary using regular expressions 
			matcher.region(0, left);
			if (matcher.find())
				left = matcher.start();

			// build and measure a candidate string with ellipsis
			String tst = text.substring(0, left) + ELLIPSIS;

			width = metrics.stringWidth(tst);

			// candidate string fits into boundaries, try a longer string
			// stop when seg <= 1
			if (width <= maxWidth) {
				len += seg;
				fit = tst;
			}
		}

		// string can't fit
		if (len == 0)
			return ELLIPSIS;

		return fit;
	}

	public static File getUserDocumentsPath() {
		String userHome = System.getProperty("user.home", "");
		File docs = new File(userHome + "/Documents");
		if (docs.isDirectory())
			return docs;
		docs = new File(userHome + "/My Documents");
		if (docs.isDirectory())
			return docs;
		return new File(userHome);
	}

	public static File getUserMusicPath() {
		String userHome = System.getProperty("user.home", "");
		File music = new File(userHome + "/Music");
		if (music.isDirectory())
			return music;
		music = new File(userHome + "/My Documents/My Music");
		if (music.isDirectory())
			return music;

		return getUserDocumentsPath();
	}

	public static File getLotroMusicPath(boolean create) {
		File docs = getUserDocumentsPath();
		File lotro = new File(docs.getAbsolutePath() + "/The Lord of the Rings Online");
		if (lotro.isDirectory()) {
			File music = new File(lotro.getAbsolutePath() + "/Music");
			if (music.isDirectory() || create && music.mkdir())
				return music;

			return lotro;
		}
		return docs;
	}

	public static int clamp(int value, int min, int max) {
		assert min <= max;
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	public static long clamp(long value, long min, long max) {
		assert min <= max;
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	public static double clamp(double value, double min, double max) {
		assert min <= max;
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	public static float clamp(float value, float min, float max) {
		assert min <= max;
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	/** Greatest Common Divisor */
	public static int gcd(int a, int b) {
		while (b != 0) {
			int t = b;
			b = a % b;
			a = t;
		}
		return a;
	}

	/** Greatest Common Divisor */
	public static long gcd(long a, long b) {
		while (b != 0) {
			long t = b;
			b = a % b;
			a = t;
		}
		return a;
	}

	/** Least Common Multiple */	
	public static int lcm(int a, int b) {
		return (a / gcd(a, b)) * b;
	}

	/** Least Common Multiple */	
	public static long lcm(long a, long b) {
		return (a / gcd(a, b)) * b;
	}

	public static boolean openURL(String url) {
		try {
			if (System.getProperty("os.name").startsWith("Windows")) {
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
				return true;
			}
		}
		catch (Exception e) {}
		return false;
	}

	public static File resolveShortcut(File file) {
		if (file.getName().toLowerCase().endsWith(".lnk")) {
			try {
				return ShellFolder.getShellFolder(file).getLinkLocation();
			}
			catch (Exception e) {}
		}
		return file;
	}

	public static void initWinBounds(final JFrame frame, final Preferences prefs, int defaultW, int defaultH) {
		Dimension mainScreen = Toolkit.getDefaultToolkit().getScreenSize();

		int width = prefs.getInt("width", defaultW);
		int height = prefs.getInt("height", defaultH);
		int x = prefs.getInt("x", (mainScreen.width - width) / 2);
		int y = prefs.getInt("y", (mainScreen.height - height) / 2);

		// Handle the case where the window was last saved on
		// a screen that is no longer connected
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		Rectangle onScreen = null;
		for (int i = 0; i < gs.length; i++) {
			Rectangle monitorBounds = gs[i].getDefaultConfiguration().getBounds();
			if (monitorBounds.intersects(x, y, width, height)) {
				onScreen = monitorBounds;
				break;
			}
		}
		if (onScreen == null) {
			x = (mainScreen.width - width) / 2;
			y = (mainScreen.height - height) / 2;
		}
		else {
			if (x < onScreen.x)
				x = onScreen.x;
			else if (x + width > onScreen.x + onScreen.width)
				x = onScreen.x + onScreen.width - width;

			if (y < onScreen.y)
				y = onScreen.y;
			else if (y + height > onScreen.y + onScreen.height)
				y = onScreen.y + onScreen.height - height;
		}

		frame.setBounds(x, y, width, height);

		int maximized = prefs.getInt("maximized", 0);
		frame.setExtendedState((frame.getExtendedState() & ~JFrame.MAXIMIZED_BOTH) | maximized);

		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == 0) {
					prefs.putInt("width", frame.getWidth());
					prefs.putInt("height", frame.getHeight());
				}
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == 0) {
					prefs.putInt("x", frame.getX());
					prefs.putInt("y", frame.getY());
				}
			}
		});

		frame.addWindowStateListener(new WindowStateListener() {
			@Override
			public void windowStateChanged(WindowEvent e) {
				prefs.putInt("maximized", e.getNewState() & JFrame.MAXIMIZED_BOTH);
			}
		});
	}

	public static String formatDuration(long micros) {
		return formatDuration(micros, 0);
	}

	public static String formatDuration(long micros, long maxMicros) {
		StringBuilder s = new StringBuilder(5);

		int t = (int) (micros / (1000 * 1000));
		int hr = t / (60 * 60);
		t %= 60 * 60;
		int min = t / 60;
		t %= 60;
		int sec = t;

		int tMax = (int) (maxMicros / (1000 * 1000));
		int hrMax = tMax / (60 * 60);
		tMax %= 60 * 60;
		int minMax = tMax / 60;

		if (hrMax > 0) {
			s.append(hr).append(':');
			if (min < 10) {
				s.append('0');
			}
		}
		else if (minMax >= 10 && min < 10) {
			s.append('0');
		}
		s.append(min).append(':');
		if (sec < 10) {
			s.append('0');
		}
		s.append(sec);

		return s.toString();
	}

	public static String quote(String in) {
		return "\"" + in.replace("\"", "\\\"") + "\"";
	}
}
