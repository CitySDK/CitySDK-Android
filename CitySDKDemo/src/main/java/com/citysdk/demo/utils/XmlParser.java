package com.citysdk.demo.utils;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;

final public class XmlParser  {

    private XmlParser() {}

    static public Document processXMLAssets(Context ctx, String file) {
        Document doc = null;
        InputSource is=null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            is = new InputSource(ctx.getResources().getAssets().open(file));
            DocumentBuilder db;
            db = dbf.newDocumentBuilder();
            doc = db.parse(is);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return doc;
    }
    
    static public Document processXMLInternalStorage(Context ctx, String file) {
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            File f = ctx.getFileStreamPath(file);
            DocumentBuilder db;
            db = dbf.newDocumentBuilder();
            doc = db.parse(f);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        doc.getDocumentElement().normalize();
        return doc;
    }

}