
import org.opencv.core.*;

import org.opencv.imgcodecs.Imgcodecs;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

import org.opencv.core.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @Titel   Bildverarbeitungsapp
 * @Semester Ingenieurinformatik Winter 2023
 *
 * @author  Niklas Hübner
 * @Email   niklas.huebner@fh-bielefeld.de
 */
public class Main {
    public static void main(String[] args) throws Exception {
        /**
         * OpenCV Initialisieren
         */
        initialiseOpenCv();

        /**
         * Bild als Mat einlesen
         */
        String imgFile = "Bilder/stift1.jpg";
        Mat imgMat = Imgcodecs.imread(imgFile);

        /**
         * Bild vorbereiten für TemplateDetection.
         * Aufruf edgeDetection für das Rechteck welches im nächsten Schritt benötigt wird.
         * Aufruf cropTemplate um das Template zu erhalten.
         * Template in Graustufenbild konvertieren.
         * Originalbild in Graustufenbild konvertieren.
         */
        TemplateDetection td = new TemplateDetection();
        imgMat = td.scaleMat(imgMat);
        Rect rect = td.edgeDetection(imgMat);
        Mat template = td.cropTemplate(imgMat, rect);
        template = td.colorToGray(template);
        Mat dst = td.colorToGray(imgMat);

        Imgcodecs.imwrite("Bilder/templategrey1.jpg", template);



        /**
         * Threshold wählen, 0.6-0.85 funktioniert für die bisherigen Tests gut
         */
        double threshold = 0.7;
        TemplateMatching tm = new TemplateMatching();

        /**
         * Rückgabewert ist eine Liste von Points wo der maxValue >= threshold ist.
         * Als Methode wird TM_CCOOEDD_NORMED verwendet, diese funktioniert mit dem maxValue.
         */
        List<Point> detectedPoints = tm.detectTemplate(dst,template, threshold);
        /**
         * Es werden überlappende Punkte gefunden, in python wäre die Lösung groupRectangles.
         */
        List<Point> totalPoints = tm.removeNearPoints(detectedPoints, dst, template);

        /**
         * Speichert der gefundenen Punkte und der Punkte, nachdem entfernen der Überlappungen.
         */
        writeTxtFile("detectedPoints.txt", detectedPoints);
        writeTxtFile("totalPoints3.txt", totalPoints);


        /**
         * Zum Anzeigen der gefundenen Objekte muss die Matrix in ein BufferedImage konvertiert werden.
         */
        BufferedImage image2 = convertMatToBufImg(dst);
        displayImage(image2);
    }

    /**
     * Zum Speichern von Textdateien.
     *
     * @param filename     Textdateiname
     * @param list         Liste von Punkten
     * @throws IOException
     */
    public static void writeTxtFile(String filename, List<Point> list) throws IOException {
        FileWriter writer = new FileWriter(filename);
        for(Point p: list){
            writer.write(p + System.lineSeparator());
        }
        writer.close();
    }

    /**
     * Laden der OpenCV Library.
     */
    public static void initialiseOpenCv(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println(Core.VERSION);
        System.out.println("load success");
    }

    /**
     * Konvertieren der Matrix zu ein BufferedImage, wird zum Anzeigen benötigt
     *
     * @param image      Mat Bild
     * @return           BufferedImage
     * @throws Exception
     */
    public static BufferedImage convertMatToBufImg(Mat image) throws Exception{

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

    /**
     * Bild anzeigen
     *
     * @param bufImage Bufferedimage Bild
     */
    public static void displayImage(BufferedImage bufImage){

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