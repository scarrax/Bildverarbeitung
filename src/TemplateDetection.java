import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

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

    public RotatedRect edgeDetection(Mat src){

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

        Imgcodecs.imwrite("Bilder/contours4.jpg", contourMat);
        return rotatedRect;
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
        Rect rectCrop = new Rect((int) p2.x, (int) p2.y, (int) (p4.x - p2.x+1), (int) (p4.y-p2.y+1));
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
    public RotatedRect findArea(List<RotatedRect> rotatedRectList){
        rotatedRectList.sort(new Comparator<RotatedRect>() {
            @Override
            public int compare(RotatedRect o1, RotatedRect o2) {
                return Double.compare(o1.size.area(), o2.size.area());
            }
        });

        // sorted with larges value first
        return rotatedRectList.get(rotatedRectList.size()-1);
    }

    public Mat getImage() {
        return image;
    }

    public void setImage(Mat image) {
        this.image = image;
    }
}
