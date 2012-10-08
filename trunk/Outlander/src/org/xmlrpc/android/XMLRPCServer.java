package org.xmlrpc.android;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.Socket;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

public class XMLRPCServer extends XMLRPCCommon {

    private static final String    RESPONSE = "HTTP/1.1 200 OK\n" + "Connection: close\n" + "Content-Type: text/xml\n" + "Content-Length: ";
    private static final String    NEWLINES = "\n\n";
    private final XMLRPCSerializer iXMLRPCSerializer;

    public XMLRPCServer() {
        iXMLRPCSerializer = new XMLRPCSerializer();
    }

    public MethodCall readMethodCall(final Socket socket) throws IOException, XmlPullParserException {
        final MethodCall methodCall = new MethodCall();
        final InputStream inputStream = socket.getInputStream();

        final XmlPullParser pullParser = xmlPullParserFromSocket(inputStream);

        pullParser.nextTag();
        pullParser.require(XmlPullParser.START_TAG, null, Tag.METHOD_CALL);
        pullParser.nextTag();
        pullParser.require(XmlPullParser.START_TAG, null, Tag.METHOD_NAME);

        methodCall.setMethodName(pullParser.nextText());

        pullParser.nextTag();
        pullParser.require(XmlPullParser.START_TAG, null, Tag.PARAMS);
        pullParser.nextTag(); // <param>

        do {
            // Log.d(Tag.LOG, "type=" + pullParser.getEventType() + ", tag=" +
            // pullParser.getName());
            pullParser.require(XmlPullParser.START_TAG, null, Tag.PARAM);
            pullParser.nextTag(); // <value>

            final Object param = iXMLRPCSerializer.deserialize(pullParser);
            methodCall.params.add(param); // add to return value

            pullParser.nextTag();
            pullParser.require(XmlPullParser.END_TAG, null, Tag.PARAM);
            pullParser.nextTag(); // <param> or </params>

        } while (!pullParser.getName().equals(Tag.PARAMS)); // </params>

        return methodCall;
    }

    XmlPullParser xmlPullParserFromSocket(final InputStream socketInputStream) throws IOException, XmlPullParserException {

        String line, xmlRpcText = "";
        final BufferedReader br = new BufferedReader(new InputStreamReader(socketInputStream));
        while (((line = br.readLine()) != null) && (line.length() > 0)) {
            ; // eat the HTTP POST headers
        }
        while (br.ready()) {
            xmlRpcText = xmlRpcText + br.readLine();
            // Log.d(Tag.LOG, "xml received:" + xmlRpcText);
        }

        final InputStream inputStream = new ByteArrayInputStream(xmlRpcText.getBytes("UTF-8"));
        final XmlPullParser pullParser = XmlPullParserFactory.newInstance().newPullParser();
        final Reader streamReader = new InputStreamReader(inputStream);
        pullParser.setInput(streamReader);
        return pullParser;
    }

    public void respond(final Socket socket, final Object[] params) throws IOException {

        final String content = methodResponse(params);
        final String response = XMLRPCServer.RESPONSE + (content.length()) + XMLRPCServer.NEWLINES + content;
        final OutputStream outputStream = socket.getOutputStream();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
        socket.close();
        Log.d(Tag.LOG, "response:" + response);
    }

    private String methodResponse(final Object[] params) throws IllegalArgumentException, IllegalStateException, IOException {
        final StringWriter bodyWriter = new StringWriter();
        serializer.setOutput(bodyWriter);
        serializer.startDocument(null, null);
        serializer.startTag(null, Tag.METHOD_RESPONSE);

        serializeParams(params);

        serializer.endTag(null, Tag.METHOD_RESPONSE);
        serializer.endDocument();

        return bodyWriter.toString();
    }
}