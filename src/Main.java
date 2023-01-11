
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

        // Reading the Image from the file and storing it in to a Matrix object
        String file = "Bilder/Erbsen.jpg";
        Mat image = Imgcodecs.imread(file);

        // new main
        // Bild einlesen
        String erbsenFile = "Bilder/Erbsen3.jpg";
        Mat erbsenMat = Imgcodecs.imread(erbsenFile);

        TemplateDetection td = new TemplateDetection();
        erbsenMat = td.scaleMat(erbsenMat);
        RotatedRect rotatedRect = td.edgeDetection(erbsenMat);

        Map<String, Point> resultPoints = td.templatePoints(rotatedRect);
        System.out.println("P1 "+resultPoints.get("P1"));
        System.out.println("P2 "+resultPoints.get("P2"));
        System.out.println("P3 "+resultPoints.get("P3"));
        System.out.println("P4 "+resultPoints.get("P4"));

        Mat template = new Mat();
        template = td.cropTemplate(erbsenMat,
                resultPoints.get("P1"),
                resultPoints.get("P2"),
                resultPoints.get("P3"),
                resultPoints.get("P4"));

        template = td.colorToGray(template);
        Imgcodecs.imwrite("Bilder/templategrey1.jpg", template);

        //template = scalegMat(template);
        Mat dst = td.colorToGray(erbsenMat);

        double threshold = 0.85;
        TemplateMatching tm = new TemplateMatching();
        List<Point> detectedPoints = tm.detectTemplate(dst,template, threshold);
        List totalPoints = tm.removeNearPoints(detectedPoints, dst, template);

        writeTxtFile("detectedPoints.txt", detectedPoints);
        writeTxtFile("totalPoints3.txt", totalPoints);


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