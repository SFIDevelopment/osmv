package org.xmlrpc.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

// import android.util.Log;

class XMLRPCSerializer implements IXMLRPCSerializer {

    static SimpleDateFormat dateFormat = new SimpleDateFormat(IXMLRPCSerializer.DATETIME_FORMAT);

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(final XmlSerializer serializer, final Object object) throws IOException {
        // check for scalar types:
        if ((object instanceof Integer) || (object instanceof Short) || (object instanceof Byte)) {
            serializer.startTag(null, IXMLRPCSerializer.TYPE_I4).text(object.toString()).endTag(null, IXMLRPCSerializer.TYPE_I4);
        }
        else if (object instanceof Long) {
            serializer.startTag(null, IXMLRPCSerializer.TYPE_I8).text(object.toString()).endTag(null, IXMLRPCSerializer.TYPE_I8);
        }
        else if ((object instanceof Double) || (object instanceof Float)) {
            serializer.startTag(null, IXMLRPCSerializer.TYPE_DOUBLE).text(object.toString()).endTag(null, IXMLRPCSerializer.TYPE_DOUBLE);
        }
        else if (object instanceof Boolean) {
            final Boolean bool = (Boolean) object;
            final String boolStr = bool.booleanValue() ? "1" : "0";
            serializer.startTag(null, IXMLRPCSerializer.TYPE_BOOLEAN).text(boolStr).endTag(null, IXMLRPCSerializer.TYPE_BOOLEAN);
        }
        else if (object instanceof String) {
            serializer.startTag(null, IXMLRPCSerializer.TYPE_STRING).text(object.toString()).endTag(null, IXMLRPCSerializer.TYPE_STRING);
        }
        else if ((object instanceof Date) || (object instanceof Calendar)) {
            final String dateStr = XMLRPCSerializer.dateFormat.format(object);
            serializer.startTag(null, IXMLRPCSerializer.TYPE_DATE_TIME_ISO8601).text(dateStr).endTag(null, IXMLRPCSerializer.TYPE_DATE_TIME_ISO8601);
        }
        else if (object instanceof byte[]) {
            final String value = new String(Base64Coder.encode((byte[]) object));
            serializer.startTag(null, IXMLRPCSerializer.TYPE_BASE64).text(value).endTag(null, IXMLRPCSerializer.TYPE_BASE64);
        }
        else if (object instanceof List) {
            serializer.startTag(null, IXMLRPCSerializer.TYPE_ARRAY).startTag(null, IXMLRPCSerializer.TAG_DATA);
            final List<Object> list = (List<Object>) object;
            final Iterator<Object> iter = list.iterator();
            while (iter.hasNext()) {
                final Object o = iter.next();
                serializer.startTag(null, IXMLRPCSerializer.TAG_VALUE);
                serialize(serializer, o);
                serializer.endTag(null, IXMLRPCSerializer.TAG_VALUE);
            }
            serializer.endTag(null, IXMLRPCSerializer.TAG_DATA).endTag(null, IXMLRPCSerializer.TYPE_ARRAY);
        }
        else if (object instanceof Object[]) {
            serializer.startTag(null, IXMLRPCSerializer.TYPE_ARRAY).startTag(null, IXMLRPCSerializer.TAG_DATA);
            final Object[] objects = (Object[]) object;
            for (final Object o : objects) {
                serializer.startTag(null, IXMLRPCSerializer.TAG_VALUE);
                serialize(serializer, o);
                serializer.endTag(null, IXMLRPCSerializer.TAG_VALUE);
            }
            serializer.endTag(null, IXMLRPCSerializer.TAG_DATA).endTag(null, IXMLRPCSerializer.TYPE_ARRAY);
        }
        else if (object instanceof Map) {
            serializer.startTag(null, IXMLRPCSerializer.TYPE_STRUCT);
            final Map<String, Object> map = (Map<String, Object>) object;
            final Iterator<Entry<String, Object>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                final Entry<String, Object> entry = iter.next();
                final String key = entry.getKey();
                final Object value = entry.getValue();

                serializer.startTag(null, IXMLRPCSerializer.TAG_MEMBER);
                serializer.startTag(null, IXMLRPCSerializer.TAG_NAME).text(key).endTag(null, IXMLRPCSerializer.TAG_NAME);
                serializer.startTag(null, IXMLRPCSerializer.TAG_VALUE);
                serialize(serializer, value);
                serializer.endTag(null, IXMLRPCSerializer.TAG_VALUE);
                serializer.endTag(null, IXMLRPCSerializer.TAG_MEMBER);
            }
            serializer.endTag(null, IXMLRPCSerializer.TYPE_STRUCT);
        }
        else if (object instanceof XMLRPCSerializable) {
            final XMLRPCSerializable serializable = (XMLRPCSerializable) object;
            serialize(serializer, serializable.getSerializable());
        }
        else {
            throw new IOException("Cannot serialize " + object);
        }
    }

    @Override
    public Object deserialize(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, IXMLRPCSerializer.TAG_VALUE);

        if (parser.isEmptyElementTag()) {
            // degenerated <value />, return empty string
            return "";
        }

        Object obj;
        boolean hasType = true;
        String typeNodeName = null;
        try {
            parser.nextTag();
            typeNodeName = parser.getName();
            if (typeNodeName.equals(IXMLRPCSerializer.TAG_VALUE) && (parser.getEventType() == XmlPullParser.END_TAG)) {
                // empty <value></value>, return empty string
                return "";
            }
        }
        catch (final XmlPullParserException e) {
            hasType = false;
        }
        if (hasType) {
            if (typeNodeName.equals(IXMLRPCSerializer.TYPE_INT) || typeNodeName.equals(IXMLRPCSerializer.TYPE_I4)) {
                final String value = parser.nextText();
                obj = Integer.parseInt(value);
            }
            else if (typeNodeName.equals(IXMLRPCSerializer.TYPE_I8)) {
                final String value = parser.nextText();
                obj = Long.parseLong(value);
            }
            else if (typeNodeName.equals(IXMLRPCSerializer.TYPE_DOUBLE)) {
                final String value = parser.nextText();
                obj = Double.parseDouble(value);
            }
            else if (typeNodeName.equals(IXMLRPCSerializer.TYPE_BOOLEAN)) {
                final String value = parser.nextText();
                obj = value.equals("1") ? Boolean.TRUE : Boolean.FALSE;
            }
            else if (typeNodeName.equals(IXMLRPCSerializer.TYPE_STRING)) {
                obj = parser.nextText();
            }
            else if (typeNodeName.equals(IXMLRPCSerializer.TYPE_DATE_TIME_ISO8601)) {
                final String value = parser.nextText();
                try {
                    obj = XMLRPCSerializer.dateFormat.parseObject(value);
                }
                catch (final ParseException e) {
                    throw new IOException("Cannot deserialize dateTime " + value);
                }
            }
            else if (typeNodeName.equals(IXMLRPCSerializer.TYPE_BASE64)) {
                final String value = parser.nextText();
                final BufferedReader reader = new BufferedReader(new StringReader(value));
                String line;
                final StringBuffer sb = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                obj = Base64Coder.decode(sb.toString());
            }
            else if (typeNodeName.equals(IXMLRPCSerializer.TYPE_ARRAY)) {
                parser.nextTag(); // TAG_DATA (<data>)
                parser.require(XmlPullParser.START_TAG, null, IXMLRPCSerializer.TAG_DATA);

                parser.nextTag();
                final List<Object> list = new ArrayList<Object>();
                while (parser.getName().equals(IXMLRPCSerializer.TAG_VALUE)) {
                    list.add(deserialize(parser));
                    parser.nextTag();
                }
                parser.require(XmlPullParser.END_TAG, null, IXMLRPCSerializer.TAG_DATA);
                parser.nextTag(); // TAG_ARRAY (</array>)
                parser.require(XmlPullParser.END_TAG, null, IXMLRPCSerializer.TYPE_ARRAY);
                obj = list.toArray();
            }
            else if (typeNodeName.equals(IXMLRPCSerializer.TYPE_STRUCT)) {
                parser.nextTag();
                final Map<String, Object> map = new HashMap<String, Object>();
                while (parser.getName().equals(IXMLRPCSerializer.TAG_MEMBER)) {
                    String memberName = null;
                    Object memberValue = null;
                    while (true) {
                        parser.nextTag();
                        final String name = parser.getName();
                        if (name.equals(IXMLRPCSerializer.TAG_NAME)) {
                            memberName = parser.nextText();
                        }
                        else if (name.equals(IXMLRPCSerializer.TAG_VALUE)) {
                            memberValue = deserialize(parser);
                        }
                        else {
                            break;
                        }
                    }
                    if ((memberName != null) && (memberValue != null)) {
                        map.put(memberName, memberValue);
                    }
                    parser.require(XmlPullParser.END_TAG, null, IXMLRPCSerializer.TAG_MEMBER);
                    parser.nextTag();
                }
                parser.require(XmlPullParser.END_TAG, null, IXMLRPCSerializer.TYPE_STRUCT);
                obj = map;
            }
            else {
                throw new IOException("Cannot deserialize " + parser.getName());
            }
        }
        else {
            // TYPE_STRING (<string>) is not required
            obj = parser.getText();
        }
        parser.nextTag(); // TAG_VALUE (</value>)
        parser.require(XmlPullParser.END_TAG, null, IXMLRPCSerializer.TAG_VALUE);
        return obj;
    }
}
