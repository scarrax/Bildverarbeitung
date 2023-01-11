import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TemplateDetection {

    private Mat image;

    public TemplateDetection(Mat image){
        this.setImage(image);
    }

    public TemplateDetection(){}

    public Mat scaleMat(Mat src){

        // Creating an empty matrix to store the result
        Mat dst = new Mat();
        // Creating the Size object
        Size size = new Size(src.cols()*0.5, src.rows()*0.5);
        // Scaling the Image
        Imgproc.resize(src, dst, size, 0,0, Imgproc.INTER_AREA);
        System.out.println("rescaled");
        return dst;
    }

    public Mat colorToGray(Mat src){
        // convert into gray image
        Mat grayMat = new Mat();
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
        return grayMat;
    }

    public RotatedRect edgeDetection(Mat src) throws IOException {

        // convert into grayscale
        Mat gray = colorToGray(src);

        // Gaussian Blur
        Mat blur = new Mat();
        Imgproc.GaussianBlur(gray, blur, new Size(3,3), 0);

        // Detecting the edges
        Mat edges = new Mat();
        Imgproc.Canny(blur, edges, 75, 150);

        // find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        System.out.println("Hierarch elementsize: " + hierarchy.elemSize());

        // Draw contours in contourMat
        Mat contourMat = Mat.zeros(gray.size(), CvType.CV_8UC3);
        Scalar white = new Scalar(255, 255, 255);
        Imgproc.drawContours(contourMat, contours, -1, white);

        // fill
        /*
        for(MatOfPoint contour: contours){
            Imgproc.fillPoly(contourMat, Arrays.asList(contour), white);
        } */

        // Draw rectangle around the contours
        Scalar green = new Scalar(81,180,0);
        List<RotatedRect> rotatedRectList = new ArrayList<RotatedRect>();

        for(MatOfPoint contour: contours){
            RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            rotatedRectList.add(rotatedRect);
            drawRotatedRect(contourMat, rotatedRect, green, 4);
        }

        // Rectangle with larges area
        //Todo: vielleicht mittelwert überalle bilden und die area verwenden
        RotatedRect rotatedRect = findArea(rotatedRectList);
        System.out.println("Größe?: " + rotatedRect.size.area());

        RotatedRect area = averageArea(rotatedRectList);
        System.out.println("Area: "+ area.size.area());


        Imgcodecs.imwrite("Bilder/contours4.jpg", contourMat);
        return area;
    }

    public Map<String, Point> templatePoints(RotatedRect rotatedRect){
        Map<String, Point> result = new HashMap<>();
        Point templateP1 = coordinates(rotatedRect, 0, 0);
        Point templateP2 = coordinates(rotatedRect, 1, 0);
        Point templateP3 = coordinates(rotatedRect, 2, 0);
        Point templateP4 = coordinates(rotatedRect, 3, 0);

        result.put("P1", templateP1);
        result.put("P2", templateP2);
        result.put("P3", templateP3);
        result.put("P4", templateP4);

        return result;
    }

    public Mat cropTemplate(Mat image_original, Point p1, Point p2, Point p3, Point p4){
        System.out.println("p1 " + p1);
        System.out.println(p2);
        System.out.println(p3);
        System.out.println(p4);
        //Todo: Punkt links oben berechnen, dann die Punkte die auf der x-Achse am weitesten auseinander liegen
        int width = (int)coordinateWidth(p1, p2, p3, p4);
        int height = (int)coordinateHeight(p1, p2, p3, p4);
        int startCoordinateX = (int)coordinateStartX(p1, p2, p3, p4);
        int startCoordinateY = (int)coordinateStartY(p1, p2, p3, p4);
        //System.out.println("Höhe ohne funktion: "+ (p4.y-p2.y+1));
        System.out.println("StartY ohne funktion: "+ p2.y);
        Rect rectCrop = new Rect(startCoordinateX, startCoordinateY, width, height);

//        Imgproc.rectangle (
//                image_original,                    //Matrix obj of the image
//                p4,        //p1
//                p4,       //p2
//                new Scalar(0, 0, 255),     //Scalar object for color
//                5                          //Thickness of the line
//        );
        Mat image_output = image_original.submat(rectCrop);
        Imgcodecs.imwrite("Bilder/image_output2.jpg", image_output);
        Imgcodecs.imwrite("Bilder/image_recangle2.jpg", image_original);
        return image_output;
    }

    public double coordinateWidth(Point p1, Point p2,Point p3,Point p4){
        double width = 0;

        if(Math.abs(p1.x-p3.x) > width){
            width = Math.abs(p1.x-p3.x);
        }else if (Math.abs(p1.x-p4.x) > width){
            width = Math.abs(p1.x-p4.x);
        }else if (Math.abs(p2.x-p3.x) > width){
            width = Math.abs(p2.x-p3.x);
        }else if (Math.abs(p2.x-p4.x) > width){
            width = Math.abs(p2.x-p4.x);
        }
        System.out.println("Breite: "+width);
        return width;
    }

    public double coordinateHeight(Point p1, Point p2,Point p3,Point p4){
        double height = 0;

        if(Math.abs(p2.y-p1.y) > height){
            height = Math.abs(p2.y-p1.y);
            System.out.println("Höhe2-1: "+height);
        }if (Math.abs(p2.y-p4.y) > height){
            height = Math.abs(p2.y-p4.y);
            System.out.println("Höhe2-4: "+height);
        }if (Math.abs(p3.y-p1.y) > height){
            height = Math.abs(p3.y-p1.y);
            System.out.println("Höhe3-1: "+height);
        }if (Math.abs(p3.y-p4.y) > height){
            height = Math.abs(p3.y-p4.y);
            System.out.println("Höhe3-4: "+height);
        }
        System.out.println("Höhe: "+height);
        return height;
    }

    public double coordinateStartX(Point p1, Point p2,Point p3,Point p4){

        double startX = Math.min(p1.x, p2.x);
        startX = Math.min(startX, p3.x);
        startX = Math.min(startX, p4.x);

        System.out.println("StartX: "+startX);
        // Die Rechteckkoordinaten von den Konturen,können ins negative gehen. Deswegen Prüfen und 0 setzen
        if(startX < 0) startX=0;
        return startX;
    }

    public double coordinateStartY(Point p1, Point p2,Point p3,Point p4){
        double startY = Math.min(p1.y, p2.y);
        startY = Math.min(startY, p3.y);
        startY = Math.min(startY, p4.y);

        System.out.println("StartY: "+startY);
        // Die Rechteckkoordinaten von den Konturen,können ins negative gehen. Deswegen Prüfen und 0 setzen
        if(startY < 0) startY=0;
        return startY;
    }

    public Point coordinates(RotatedRect rotatedRect, int row, int col){
        Point[] vertices = new Point[4];
        rotatedRect.points(vertices);
        MatOfPoint points = new MatOfPoint(vertices);
        System.out.println("last points: " + Arrays.toString(points.toArray()));

        String str = Arrays.toString(points.get(row, col));
        str = str.replaceAll("[\\[\\](){}\\s]","");
        System.out.println("str: " + str );

        List<String> coordinates = Arrays.asList(str.split(","));
        double px = Double.parseDouble(coordinates.get(0));
        double py = Double.parseDouble(coordinates.get(1));

        return new Point(px, py);
    }

    public static void drawRotatedRect(Mat image, RotatedRect rotatedRect, Scalar color,int thickness){
        Point[] vertices = new Point[4];
        rotatedRect.points(vertices);
        MatOfPoint points = new MatOfPoint(vertices);
        Imgproc.drawContours(image, Arrays.asList(points), -1, color, thickness);
        //System.out.println("points + " + Arrays.toString(points.toArray()));
    }
    public RotatedRect findArea(List<RotatedRect> rotatedRectList) throws IOException {
        rotatedRectList.sort(new Comparator<RotatedRect>() {
            @Override
            public int compare(RotatedRect o1, RotatedRect o2) {
                return Double.compare(o1.size.area(), o2.size.area());
            }
        });

        // alle Rechteckte mit area 0 entfernen
        rotatedRectList = removeZeros(rotatedRectList);
        double averageArea;



        List<RotatedRect> angleList = new ArrayList<>();
        for(RotatedRect r: rotatedRectList){
            if((r.angle == 90) || r.angle < 100 && r.angle > 80){
                angleList.add(r);
            }
        }
        FileWriter writer = new FileWriter("rotatedRectAllArea0");
        for(RotatedRect r: rotatedRectList){
            writer.write(r.size.area() + System.lineSeparator());
        }
        writer.close();
        // sorted with larges value first
        return rotatedRectList.get(rotatedRectList.size()-1);
    }

    public List<RotatedRect> removeZeros(List<RotatedRect> rotatedRectList){

        rotatedRectList.removeIf(r -> r.size.area() == 0);

        return rotatedRectList;
    }

    public RotatedRect averageArea(List<RotatedRect> rotatedRectList){
        double mean = 0;
        double sum = 0;

        rotatedRectList = removeZeros(rotatedRectList);

        for(RotatedRect r: rotatedRectList){
            sum += r.size.area();
        }
        mean = sum/rotatedRectList.size();

        // gucken welches element am nächsten beim mittelwert ist
        double distance = Math.abs(rotatedRectList.get(0).size.area() - mean);
        int index = 0;
        for(int c = 1; c < rotatedRectList.size(); c++){
            double cdistance = Math.abs(rotatedRectList.get(c).size.area()-mean);
            if(cdistance < distance){
                index = c;
                distance = cdistance;
            }
        }

        double finalArea = rotatedRectList.get(index).size.area();
        RotatedRect rotatedRect = rotatedRectList.get(index);
        System.out.println("Mittelwert: "+ mean+ " FinalArea: " + finalArea);
        return rotatedRect;
    }

    public Mat getImage() {
        return image;
    }

    public void setImage(Mat image) {
        this.image = image;
    }
}
