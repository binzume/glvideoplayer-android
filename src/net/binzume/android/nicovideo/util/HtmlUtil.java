package net.binzume.android.nicovideo.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTMLを色々するやつ(XMLPullParserが重すぎるので自前でやる予定)
 * 
 * @author kawahira
 */

public class HtmlUtil {
	
	public static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// do not reach here
		}
		return s;
	}
	
    static class HtmlIterator implements Iterable<CharSequence>, Iterator<CharSequence> {
        private Matcher matcher;

        public HtmlIterator(Matcher m) {
            matcher = m;
        }

        public boolean hasNext() {
            return matcher.find();
        }

        public CharSequence next() {
            return matcher.group(2);
        }

        public void remove() {
        }

        public Iterator<CharSequence> iterator() {
            return this;
        }

    }
    
    public static HashMap<String,String> flashVarsToMap(final String data) {
		HashMap<String, String> map = new HashMap<String, String>();
		String[] params = data.split("&");
		for (int i = 0; i < params.length; i++) {
			String[] p = params[i].split("=");
			if (p.length < 2)
				continue;
			try {
				map.put(URLDecoder.decode(p[0], "UTF-8"), URLDecoder.decode(p[1], "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// do not reach here
			}
		}
    	
    	return map;
    }

    public static Iterable<CharSequence> getByTagName(CharSequence data, String name) {
        Matcher matcher = Pattern.compile("<(" + name + ")[^>]*>([^<]*)</\\1>", Pattern.DOTALL).matcher(data);
        return new HtmlIterator(matcher);
    }

    public static CharSequence getFirstTag(CharSequence data, String name) {
        Matcher matcher = Pattern.compile("<(" + name + ")[^>]*>([^<]*)</\\1>", Pattern.DOTALL).matcher(data);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    public static String unescape(CharSequence src) {
        int len = src.length(), p = 0;
        StringBuffer sb = new StringBuffer(len);
        while (p < len) {
            char c = src.charAt(p);
            if (c == '&') {
                int t = p + 1;
                while (t < len) {
                    if (src.charAt(t) == ';') {
                        String s = src.subSequence(p + 1, t).toString();
                        if ("amp".equals(s)) {
                            c = '&';
                        } else if ("quot".equals(s)) {
                            c = '"';
                        } else if ("gt".equals(s)) {
                            c = '>';
                        } else if ("apos".equals(s)) {
                            c = '\'';
                        } else if ("lt".equals(s)) {
                            c = '<';
                        } else if ("#039".equals(s)) {
                            c = '\'';
                        } else {
                            t = p;
                        }
                        p = t;
                        break;
                    }
                    t++;
                }

            }
            sb.append(c);
            p++;
        }
        return sb.toString();
    }

    public static String escape(CharSequence src) {
        int len = src.length(), p = 0;
        StringBuffer sb = new StringBuffer(len + 20);
        while (p < len) {
            char c = src.charAt(p);
            if (c == '&') {
                sb.append("&amp;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '<') {
                sb.append("&lt;");
            } else if (c == '"') {
                sb.append("&quot;");
            } else {
                sb.append(c);
            }
            p++;
        }
        return sb.toString();
    }

}
