package org.xmlrpc.android;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

class XMLRPCCommon {

    protected XmlSerializer     serializer;
    protected IXMLRPCSerializer iXMLRPCSerializer;

    XMLRPCCommon() {
        serializer = Xml.newSerializer();
        iXMLRPCSerializer = new XMLRPCSerializer();
    }

    /**
     * Sets custom IXMLRPCSerializer serializer (in case when server doesn't
     * support standard XMLRPC protocol)
     * 
     * @param serializer
     *            custom serializer
     */
    public void setSerializer(final IXMLRPCSerializer serializer) {
        iXMLRPCSerializer = serializer;
    }

    protected void serializeParams(final Object[] params)
            throws IllegalArgumentException, IllegalStateException, IOException {
        if ((params != null) && (params.length != 0)) {
            // set method params
            serializer.startTag(null, Tag.PARAMS);
            for (final Object param : params) {
                serializer.startTag(null, Tag.PARAM).startTag(null,
                        IXMLRPCSerializer.TAG_VALUE);
                iXMLRPCSerializer.serialize(serializer, param);
                serializer.endTag(null, IXMLRPCSerializer.TAG_VALUE).endTag(
                        null, Tag.PARAM);
            }
            serializer.endTag(null, Tag.PARAMS);
        }
    }

}
