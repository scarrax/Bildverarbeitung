
import org.opencv.core.*;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

import org.opencv.core.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;


public class Main {
    public static void main(String[] args) throws Exception {
        initialiseOpenCv();

        // Bild einlesen
        String erbsenFile = "Bilder/Erbsen2.jpg";
        Mat erbsenMat = Imgcodecs.imread(erbsenFile);

        TemplateDetection td = new TemplateDetection();

        // Bild verkleinern
        erbsenMat = td.scaleMat(erbsenMat);
        // Rückgabewer ist ein Rechteck, wird benötig um das Template auszuschneiden
        Rect rect = td.edgeDetection(erbsenMat);

        // Template aus dem Originalbild ausschneiden
        Mat template = new Mat();
        template = td.cropTemplate(erbsenMat, rect);

        // Templatefarbe in grau umwandeln
        template = td.colorToGray(template);
        Imgcodecs.imwrite("Bilder/templategrey1.jpg", template);

        // Originalbild in grau umwandeln
        Mat dst = td.colorToGray(erbsenMat);

        // Threshold wählen, 0.85 funktioniert für die bisherigen Tests gut
        double threshold = 0.85;
        TemplateMatching tm = new TemplateMatching();
        // Rückgabewert ist eine Liste von Points wo der maxValue >= threshold ist.
        // als Methode wird TM_CCOOEDD_NORMED verwendet, diese funktioniert mit dem maxValue
        List<Point> detectedPoints = tm.detectTemplate(dst,template, threshold);
        // Es werden überlappende Punkte gefunden, in python wäre die Lösung groupRectangles
        // Hier eine selbstgeschriebene alternative für Java
        List totalPoints = tm.removeNearPoints(detectedPoints, dst, template);

        // Speichert der gefundenen Punkte und der Punkte, nachdem entfernen der Überlappungen
        writeTxtFile("detectedPoints.txt", detectedPoints);
        writeTxtFile("totalPoints3.txt", totalPoints);

        // Zum Anzeigen der Gefunden Objekte muss die Matrix in ein BufferedImage konvertiert werden
        BufferedImage image2 = convertMatToBufImg(dst);
        displayImage(image2);
    }

    public static void writeTxtFile(String filename, List<Point> list) throws IOException {
        FileWriter writer = new FileWriter(filename);
        for(Point p: list){
            writer.write(p + System.lineSeparator());
        }
        writer.close();
    }

    // Für debugging Zwecke
    public static void initialiseOpenCv(){
        // Loading the OpenCV core library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println(Core.VERSION);
        System.out.println("load success");
    }

    public static BufferedImage convertMatToBufImg(Mat image) throws Exception{

        // instantiating an empty MatOfByte class
        MatOfByte matOfByte = new MatOfByte();

        // Converting the Mat object to MatOfByte
        Imgcodecs.imencode(".jpg", image, matOfByte);

        // Converting MatOfByte into a byte array
        byte[] byteArray = matOfByte.toArray();

        // Preparing the InputStream object
        InputStream in = new ByteArrayInputStream(byteArray);

        // Preparing the BufferedImage
        BufferedImage bufImage = ImageIO.read(in);
        System.out.println("covert to bufImage");
        return bufImage;
    }

    public static void displayImage(BufferedImage bufImage){

        // Instantiate JFrame
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set Content to the JFrame
        frame.setPreferredSize(new Dimension(1200,675));
        frame.pack();
        frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        System.out.println("image loaded");
    }
}