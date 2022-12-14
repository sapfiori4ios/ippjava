package com.neoprojectdemo;


import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.attribute.Attribute;
import javax.print.attribute.EnumSyntax;

import de.lohndirekt.print.attribute.AttributeWriter;
import de.lohndirekt.print.attribute.IppAttributeName;
import de.lohndirekt.print.attribute.IppDelimiterTag;
import de.lohndirekt.print.attribute.IppValueTag;
import de.lohndirekt.print.attribute.ipp.Charset;
import de.lohndirekt.print.exception.EndOfAttributesException;

/**
 * @author bpusch
 *
 */
public final class AttributeParser {

	private final static EndOfAttributesException END_OF_ATTRIBUTES_EXCEPTION = new EndOfAttributesException();
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private final static Logger log = Logger.getLogger(AttributeParser.class.getName());

	

	/**
	 * @param name
	 * @param values
	 * @return
	 */
	private static Attribute getAttribute(String name, Object[] values) {
		Attribute attribute = null;
		IppAttributeName attrName = IppAttributeName.get(name);
		Class attrClass = attrName.getAttributeClass();
		Class superClass = attrClass.getSuperclass();
		if (superClass != null) {
			if (superClass.equals(EnumSyntax.class)) {
				try {
					Field[] fields = attrClass.getDeclaredFields();
					for (int i = 0; i < fields.length; i++) {
						Field field = fields[i];
						if (field.getType().equals(attrClass)) {
							EnumSyntax attr = (EnumSyntax) field.get(null);
							if (values[0] instanceof String) {
								if (attr.toString().equals(values[0])) {
									attribute = (Attribute) attr;
									break;
								}
							} else {
								if (attr.getValue() == ((Integer) values[0]).intValue()) {
									attribute = (Attribute) attr;
									break;
								}
							}
						}
					}
				} catch (SecurityException e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				} catch (IllegalArgumentException e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				} catch (IllegalAccessException e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				}

			} else {
				Class[] parameters = toClassArray(values);
				try {
					Constructor constructor = attrClass.getDeclaredConstructor(parameters);
					attribute = (Attribute) constructor.newInstance(values);
				} catch (SecurityException e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				} catch (NoSuchMethodException e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				} catch (IllegalArgumentException e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				} catch (InstantiationException e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				} catch (IllegalAccessException e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				} catch (InvocationTargetException e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}

		return attribute;
	}

	/**
	     * @param byteArray
	     * @param byteCount
	     * @param lastAttribute
	     * @return
	     */
	private static Attribute parseAttribute(InputStream in, Attribute lastAttribute)
		throws IOException, EndOfAttributesException {

		int valueTag;
		while ((valueTag = in.read()) < IppValueTag.UNSUPPORTED_VALUE.getValue()) {
			if (valueTag == IppDelimiterTag.END_ATTRIBUTES.getValue()) {
				throw END_OF_ATTRIBUTES_EXCEPTION;
			}
		}

		int nameLength = parseInt2(in);
		//          parse the Attribute-Name
		String name;
		if (nameLength == 0) {
			name = lastAttribute.getName();
		} else {
			name = parseString(in, nameLength);
		}
		

		Object[] values = parseValues(valueTag, in);

		return getAttribute(name, values);
	}
    
    private static String parseString(InputStream in, int nameLength) throws IOException{
        return parseString(in, nameLength, Charset.US_ASCII.getValue());
    }
    
    private static String parseNameAndTextString(InputStream in, int nameLength) throws IOException{
        return parseString(in, nameLength, AttributeWriter.DEFAULT_CHARSET.getValue());
    }

	/**
	 * @param in
	 * @param nameLength
	 */
	private static String parseString(InputStream in, int nameLength, String charsetName) throws IOException {
		byte[] bytes = new byte[nameLength];
		in.read(bytes);
		return new String(bytes, charsetName);

	}
    
    
    
    
	/**
	 * @param byteArray
	 * @param valueOffset
	 * @return
	 */
	private static Date parseDate(InputStream in) throws IOException {
		DecimalFormat twoDigits = new DecimalFormat("00");
        DecimalFormat threeDigits = new DecimalFormat("000");
		DecimalFormat fourDigits = new DecimalFormat("0000");
		//year is encoded in network-byte order
		int year = parseInt2(in);
		int month = in.read();
		int day = in.read();
		int hour = in.read();
		int minute = in.read();
		int second = in.read();
		int deci = in.read();
		int mili = deci * 100;
		char direction = (char) in.read();
		int hoursFromUtc = in.read();
		int minutesFromUtc = in.read();

		String yearString = fourDigits.format(year);
		String monthString = twoDigits.format(month);
		String dayString = twoDigits.format(day);
		String hourString = twoDigits.format(hour);
		String minuteString = twoDigits.format(minute);
		String secondString = twoDigits.format(second);
		String miliString = threeDigits.format(mili);
		String timeZone = direction + twoDigits.format(hoursFromUtc) + twoDigits.format(minutesFromUtc);
		String dateString =
			yearString
				+ "-"
				+ monthString
				+ "-"
				+ dayString
				+ " "
				+ hourString
				+ ":"
				+ minuteString
				+ ":"
				+ secondString
				+ ":"
				+ miliString
				+ timeZone;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSZ");
		Date date = null;
		try {
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return date;
	}

	private static int parseInt4(InputStream in) throws IOException {

		//Same parsing as in java.io.DataInput readInt()
		int a = in.read();
		int b = in.read();
		int c = in.read();
		int d = in.read();
		int value = (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
		return value;
	}

	/**
	 * @param bytes
	 * @param offset
	 * @return
	 */
	private static int parseInt4(byte[] bytes, int offset) {

		//Same parsing as in java.io.DataInput readInt()
		byte a = bytes[offset++];
		byte b = bytes[offset++];
		byte c = bytes[offset++];
		byte d = bytes[offset++];
		int value = (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
		return value;
	}

	private static int parseInt2(InputStream in) throws IOException {

		//Same parsing as in java.io.DataInput readInt()
		int a = in.read();
		int b = in.read();
		int value = ((a & 0xff) << 8) | ((b & 0xff));
		return value;
	}

	/**
	 * @param bytes
	 * @return map of attributes (key -> category, value -> Set with attributes)
	 * 
	 *
	 */
	public static Map parseRequest(InputStream response) throws IOException {
		Map attributes = new HashMap();
		long start = System.currentTimeMillis();
		Attribute lastAttribute = null;
		boolean finished = false;
		response.read();

		while (!finished) {
			Attribute attribute = null;
			try {
				attribute = parseAttribute(response, lastAttribute);
				if (attribute != null) {
					lastAttribute = attribute;
					attributes = put(attributes, attribute);
					if (log.isLoggable(Level.FINEST)) {
						log.finest("parsed attribute(" + attribute.getName() + "): " + attribute.toString());
					}
				} else {
					if (log.isLoggable(Level.FINEST)) {

						log.finest("Attribute was null");
					}
				}
			} catch (EndOfAttributesException e) {

				finished = true;
				if (log.isLoggable(Level.INFO)) {
					log.info("--- Attribute parsing finished ---");
				}
			}
		}
		long end = System.currentTimeMillis();
		if (log.isLoggable(Level.INFO)) {
		    log.info("Parsing took " + (end - start) + "ms.");
		}
		return attributes;
	}

	
	/**
     * @param byteArray
     * @param valueOffset
     * @param valueLength
     * @return
     */
	private static URI parseUri(InputStream in, int valueLength) throws IOException {
		String uriString = parseString(in, valueLength);
		URI uri = null;
		try {
			uri = new URI(uriString);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return uri;
	}

	
	/**
     * @param valueTag
     * @param byteArray
     * @param valueOffset
     * @param valueLength
     * @return
     */
	private static Object[] parseValues(int valueTag, InputStream in) throws IOException {
        int valueLength = parseInt2(in);
		Object[] values = null;
		if (valueTag == IppValueTag.INTEGER.getValue() || valueTag == IppValueTag.ENUM.getValue()) {
			Integer number = new Integer(parseInt4(in));
			values = new Object[] { number };
		} else if (
			valueTag == IppValueTag.STRING.getValue()
				|| valueTag == IppValueTag.TEXT.getValue()
				|| valueTag == IppValueTag.NAME.getValue()){
            String word = parseNameAndTextString(in, valueLength);
            values = new Object[] { word, Locale.getDefault()};                    
        } else if (
            valueTag == IppValueTag.CHARSET.getValue()
                || valueTag == IppValueTag.LANGUAGE.getValue()
				|| valueTag == IppValueTag.MIMETYPE.getValue()) {
			String word = parseString(in, valueLength);
			values = new Object[] { word, Locale.getDefault()};
		} else if (valueTag == IppValueTag.URI.getValue()) {
			URI uri = parseUri(in, valueLength);
			values = new Object[] { uri };
		} else if (valueTag == IppValueTag.KEYWORD.getValue()) {
			String word = parseString(in, valueLength);
			values = new Object[] { word, Locale.getDefault()};
		} else if (valueTag == IppValueTag.BOOLEAN.getValue()) {
			Integer bool = new Integer(in.read());
			values = new Object[] { bool };
		} else if (valueTag == IppValueTag.RANGE.getValue()) {
			Integer lowerBound = new Integer(parseInt4(in));
			Integer upperBound = new Integer(parseInt4(in));
			values = new Object[] { lowerBound, upperBound };
		} else if (valueTag == IppValueTag.DATE.getValue()) {

			Date date = parseDate(in);
			values = new Object[] { date };
		} else if (valueTag == IppValueTag.NOVALUE.getValue()) {
			values = new Object[] {
			};
		} else {
			throw new IllegalArgumentException("\"" + Integer.toHexString(valueTag) + "\" is not a valid value-tag");
		}
		return values;
	}


	/**
	 * @param attributes
	 * @param attribute
	 */
	private static Map put(Map attributes, Attribute attribute) {
		Set values = (Set) attributes.get(attribute.getCategory());
		if (values == null) {
			values = new HashSet();
			attributes.put(attribute.getCategory(), values);
		}
		values.add(attribute);
		return attributes;
	}

	/**
	 * @param values
	 * @return
	 */
	private static Class[] toClassArray(Object[] values) {
		Class[] classes = new Class[values.length];
		for (int i = 0; i < values.length; i++) {
			Class clazz = values[i].getClass();
			if (clazz.equals(Integer.class)) {
				clazz = int.class;
			}
			classes[i] = clazz;
		}
		return classes;
	}

}
