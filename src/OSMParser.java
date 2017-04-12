
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Coordinate;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.styling.*;
import org.geotools.swing.JMapFrame;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 11.04.2017.
 */
public class OSMParser {


    private Coordinate coordinate;


    public static void main(String[] args) {

        String url = "http://www.openstreetmap.org/api/0.6/map?bbox=18.602856,54.320462,18.607562,54.323349";

        URL website = null;
        try {
            website = new URL(url);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(website.openStream());

            OSMParser osmParser = new OSMParser();
            osmParser.parse(document);


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (SchemaException e) {
            e.printStackTrace();
        }

    }

    private void parse(Document document) throws SchemaException {

        List<Coordinate> coordinates = getCoordinatesFromXML(document);
        List<List<Coordinate>> listOfListOfCoorinates1 = getListOfCoodinatesFromXML(document);
        List<List<Coordinate>> listOfListOfCoorinates2 = getListOfCoodinatesFromXML2(document);

        SimpleFeatureType TYPE_POINT = DataUtilities.createType("Location", "location:Point:srid4326");
        SimpleFeatureType TYPE_WAY = DataUtilities.createType("Lamana", "lamana:LineString:srid4326");
        SimpleFeatureType TYPE_BUD = DataUtilities.createType("Budynek", "ksztalt:Polygon:srid4326");


        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

        SimpleFeatureCollection collection1 = FeatureCollections.newCollection();
        SimpleFeatureCollection collection2 = FeatureCollections.newCollection();
        SimpleFeatureCollection collection3 = FeatureCollections.newCollection();


        SimpleFeatureBuilder pointFeatureBuilder = new SimpleFeatureBuilder(TYPE_POINT);
        SimpleFeatureBuilder lineStringFeatureBuilder = new SimpleFeatureBuilder(TYPE_WAY);
        SimpleFeatureBuilder polygonFeatureBuilder = new SimpleFeatureBuilder(TYPE_BUD);


        for (Coordinate coordinate : coordinates) {
            Point point = geometryFactory.createPoint(coordinate);
            pointFeatureBuilder.add(point);
            SimpleFeature feature = pointFeatureBuilder.buildFeature(null);
            collection1.add(feature);
        }

        for (int i = 0; i < listOfListOfCoorinates1.size(); i++) {

            List<Coordinate> coords = listOfListOfCoorinates1.get(i);
            LineString lineString = geometryFactory.createLineString(coords.toArray(new Coordinate[coords.size()]));
            lineStringFeatureBuilder.add(lineString);
            SimpleFeature feature = lineStringFeatureBuilder.buildFeature(null);
            collection2.add(feature);
        }


        for (int i = 0; i < listOfListOfCoorinates2.size(); i++) {

            List<Coordinate> coords = listOfListOfCoorinates2.get(i);
            LinearRing ring = geometryFactory.createLinearRing(coords.toArray(new Coordinate[coords.size()]));
            Polygon polygon = geometryFactory.createPolygon(ring, null);
            polygonFeatureBuilder.add(polygon);
            SimpleFeature feature = polygonFeatureBuilder.buildFeature(null);
            collection3.add(feature);
        }




        MapContext map = new DefaultMapContext();
        map.setTitle("StyleLab");


        Style pointStyle = SLD.createPointStyle("Circle", Color.red, Color.yellow, 0.8f, 5);
        Style lineStyle = SLD.createLineStyle(Color.blue, 2);
        Style polygonStyle = SLD.createPolygonStyle(Color.black, Color.green, 0.8f);

        map.addLayer(collection1, pointStyle);
        map.addLayer(collection2, lineStyle);
        map.addLayer(collection3, polygonStyle);


        JMapFrame.showMap(map);


    }

    private List<Coordinate> getCoordinatesFromXML(Document document) {

        List<Coordinate> coordinatesList = new ArrayList<>();

        NodeList nodes = document.getElementsByTagName("node");


        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);

            Double lat = Double.valueOf(element.getAttribute("lat"));
            Double lon = Double.valueOf(element.getAttribute("lon"));

            Coordinate coordinate = new Coordinate(lat, lon);

            coordinatesList.add(coordinate);

        }

        return coordinatesList;

    }

    private Coordinate getCoordinateFromXML(Document document, String id) {


        NodeList nodes = document.getElementsByTagName("node");


        for (int i = 0; i < nodes.getLength(); i++) {

            Element element = (Element) nodes.item(i);

            if (id.equals(element.getAttribute("id"))) {
                Double lat = Double.valueOf(element.getAttribute("lat"));
                Double lon = Double.valueOf(element.getAttribute("lon"));
                coordinate = new Coordinate(lat, lon);
            }

        }
        return coordinate;

    }


    private List<List<Coordinate>> getListOfCoodinatesFromXML(Document document) {

        List<List<Coordinate>> listOfListOfCoordinates = new ArrayList<>();

        List<Coordinate> coordinates;


        NodeList nodes = document.getElementsByTagName("way");

        for (int i = 0; i < nodes.getLength(); i++) {

            coordinates = new ArrayList<>();

            NodeList nd = nodes.item(i).getChildNodes();

            for (int j = 0; j < nd.getLength(); j++) {

                if (nd.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) nd.item(j);
                    String ref = element.getAttribute("ref");

                    if (!ref.isEmpty()) {
                        coordinates.add(getCoordinateFromXML(document, ref));
                    }
                }
            }
            listOfListOfCoordinates.add(coordinates);
        }


        return listOfListOfCoordinates;
    }

    private List<List<Coordinate>> getListOfCoodinatesFromXML2(Document document) {

        List<List<Coordinate>> listOfListOfCoordinates = new ArrayList<>();

        List<Coordinate> coordinates;


        NodeList nodes = document.getElementsByTagName("way");

        String wayId = null;

        for (int i = 0; i < nodes.getLength(); i++) {

            NodeList nd = nodes.item(i).getChildNodes();

            for (int j = 0; j < nd.getLength(); j++) {

                if (nd.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) nd.item(j);

                    if (element.getAttribute("k").equals("building")) {
                        wayId = ((Element) nodes.item(i)).getAttribute("id");
                    }
                }
            }

            if(wayId != null) {
                if (wayId.equals(((Element) nodes.item(i)).getAttribute("id"))) {
                    coordinates = new ArrayList<>();
                    for (int j = 0; j < nd.getLength(); j++) {
                        if (nd.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element) nd.item(j);

                            String ref = element.getAttribute("ref");

                            if (!ref.isEmpty()) {
                                coordinates.add(getCoordinateFromXML(document, ref));
                            }
                        }
                    }
                    listOfListOfCoordinates.add(coordinates);
                }
            }
        }

        return listOfListOfCoordinates;
    }

}
