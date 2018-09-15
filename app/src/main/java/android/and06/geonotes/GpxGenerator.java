package android.and06.geonotes;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class GpxGenerator {

    public Uri createGpxFile(ArrayList<GeoNotesDatabaseHelper.Note> notes, String projectName){
        Document doc = this.createGpxDocument();
        createRootElement(doc);
        createMetadata(doc, projectName);
        appendTrackPoints(doc, notes);
        return serialize(doc);
    }

    private Document createGpxDocument(){
        //Declare an instance of type Document
        Document gpxDocument = null;
        //Now, to create an instance of type Document, we must first create an instance of type
        //DocumentBuilderFactory. We do this with the static method newInstance() of the factory
        //class DocumentBuilderFactory and we call the instance "factory"
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try{
            //With our factory instance, we create an instance of type DocumentBuilder, with the help
            //of the non-static method newDocumentBuilder() of the class DocumentBuilderFactory
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            //With the instance of type gpxDocument, we can finally create an instance of type
            //document, with the non-static method newDocument() of the class DocumentBuilder
            gpxDocument = docBuilder.newDocument();
        } catch (ParserConfigurationException ex) {
            Log.e(getClass().getSimpleName(), ex.toString());
        }
        //We return the created document
        return gpxDocument;
    }

    //This method associates metadata to the given gpx document. This metadata holds a child element
    //of type desc (description), containing the name of the project
    private void createMetadata(Document doc, String projectName){
        Element metadata = doc.createElement("metadata");
        doc.getDocumentElement().appendChild(metadata);
        Element name = doc.createElement("desc");
        name.appendChild(doc.createTextNode(projectName));
        metadata.appendChild(name);
    }

    private void createRootElement(Document doc){
        Element root = doc.createElement("gpx");
        root.setAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xsi:schemaLocation", "http://www.topografix.com/GPX/1/1/gpx.xsd");
        root.setAttribute("version", "1.1");
        root.setAttribute("creator", "GeoNotes");
        doc.appendChild(root);
    }

    private void appendTrackPoints(Document doc, ArrayList<GeoNotesDatabaseHelper.Note> notes){
        Element trk = doc.createElement("trk");
        doc.getDocumentElement().appendChild(trk);
        Element trkseg = doc.createElement("trkseg");
        trk.appendChild(trkseg);
        for (GeoNotesDatabaseHelper.Note note:notes){
            //For each note, we add an trkpt element as a child of the element trkseg in the gpx file,
            //that holds two attributes with the position where the note was taken
            Element trkpt = doc.createElement("trkpt");
            trkpt.setAttribute("lat", Double.toString(note.latitude));
            trkpt.setAttribute("lon", Double.toString(note.longitude));
            //For each note, we add a time element as a child of the element trkpt in the gpx file,
            //that holds time when the note was taken
            Element time = doc.createElement("time");
            time.appendChild(doc.createTextNode(toUTCString(note.id)));
            trkpt.appendChild(time);
            //For each note, we add a name element as child of the element trkpt in the gpx file,
            //that holds the title of the note
            Element name = doc.createElement("name");
            name.appendChild(doc.createTextNode(note.getSubject()));
            trkpt.appendChild(name);
            //For each note, we add a description element as child of the element trkpt in the gpx file,
            //that holds the description of the note
            Element desc = doc.createElement("desc");
            name.appendChild(doc.createTextNode(note.getNote()));
            trkpt.appendChild(desc);
            //Add the trkpt element (that represents the note) as child of the trkseg element to the document
            trkseg.appendChild(trkpt);
        }
    }

    //This method converts a give time stamp in long notation (given by java) in a UTC-String (needed by the GPX-Spec)
    private String toUTCString(long time){
        Date date = new Date(time);
        return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").format(date);
    }

    private Uri serialize(Document doc){
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/" + "geonotes.gpx";
        File destFile = new File(filePath);
        //Declare an instance of type uri
        Uri uri = null;
        //We must create an instance of type
        //TransformerFactory. We do this with the static method newInstance() of the factory
        //class TransformerFactory and we call the instance "factory"
        TransformerFactory factory = TransformerFactory.newInstance();
        try{
            //With our factory instance, we create an instance of type Transformer, with the help
            //of the non-static method newTransformer() of the class Transformer
            Transformer transformer = factory.newTransformer();
            //With the instance of type Transformer, we can finally transofrm the model located on the
            //RAM of the Smartphone into a File, with the non-static transform method of the class Transformer
            //AND08D S.57 Aufg.3.9.
            transformer.transform(new DOMSource(doc.getDocumentElement()), new StreamResult(destFile));
            //we get the uri of the file with the static method fromFile of the class android.net.Uri
            uri = Uri.fromFile(destFile);
        } catch (TransformerConfigurationException ex) {
            Log.d(getClass().getSimpleName(), ex.toString());
        } catch (TransformerException ex){
            Log.d(getClass().getSimpleName(), ex.toString());
        }
        return uri;
    }
}
