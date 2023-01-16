import javafx.scene.layout.Background;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import java.awt.image.BufferedImage;
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

    public Rect edgeDetection(Mat src) throws IOException {

        // convert into grayscale
        Mat gray = colorToGray(src);

        // Gaussian Blur
        Mat blur = new Mat();
        Imgproc.GaussianBlur(gray, blur, new Size(3,3), 1);
        Imgcodecs.imwrite("Bilder/Blur7x7x1.jpg", blur);

        // Detecting the edges
        Mat edges = new Mat();
        Imgproc.Canny(blur, edges, 75, 150,3);
        Imgcodecs.imwrite("Bilder/canny.jpg", edges);

        // find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Point contour_line = new Point();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //Imgcodecs.imwrite("Bilder/drawcontours3.jpg", MoP2f);
        System.out.println("Hierarch elementsize: " + hierarchy.elemSize());

        // Draw contours in contourMat
        Mat contourMat = Mat.zeros(gray.size(), CvType.CV_8UC3);
        Scalar white = new Scalar(255, 255, 255);
        Imgproc.drawContours(contourMat, contours, -1, white);

        Imgcodecs.imwrite("Bilder/drawcontours1.jpg", contourMat);

        for(int i = 0; i < contours.size(); i++){
            System.out.println("contourmat: " + contours.get(i).size().area());
            if(contours.get(i).size().area() > 80){
                Imgproc.drawContours(contourMat, contours, i, new Scalar(0,255,0), -1);
            }
        }
        Imgcodecs.imwrite("Bilder/drawcontours2.jpg", contourMat);

        // fill
        for(MatOfPoint contour: contours){
            Imgproc.fillPoly(contourMat, Arrays.asList(contour), white);
        }
        Imgcodecs.imwrite("Bilder/filled2.jpg", contourMat);

        //Creating destination matrix

        //Preparing the kernel matrix object
        /*
        Mat kernel = Mat.ones(5,5, CvType.CV_32F);
        Imgproc.morphologyEx(contourMat, contourMat, Imgproc.MORPH_OPEN, kernel);
        Imgcodecs.imwrite("Bilder/morphOpen.jpg", contourMat);
        */

        // Draw rectangle around the contours
        Scalar green = new Scalar(81,180,0);
        List<RotatedRect> rotatedRectList = new ArrayList<RotatedRect>();
        List<Rect> rectList = new ArrayList<>();

        for(MatOfPoint contour: contours){
            Rect rect = Imgproc.boundingRect(new MatOfPoint2f(contour.toArray()));
            rectList.add(rect);
            Imgproc.rectangle(contourMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), green, 2);
        }
        Imgcodecs.imwrite("Bilder/rect3.jpg", contourMat);



        //Todo: vielleicht mittelwert überalle bilden und die area verwenden
        //RotatedRect rotatedRect = findArea(rotatedRectList);
        //System.out.println("Größe?: " + rotatedRect.size.area());

        Rect area = averageArea(rectList);
        System.out.println("Area: "+ area.area());


        Imgcodecs.imwrite("Bilder/contours4.jpg", contourMat);
        return area;
    }

    public Mat cropTemplate(Mat image_original, Rect rect){

        Mat image_output = image_original.submat(rect);
        Imgcodecs.imwrite("Bilder/image_output2.jpg", image_output);
        Imgcodecs.imwrite("Bilder/image_recangle2.jpg", image_original);
        return image_output;
    }

    public List<Rect> removeZeros(List<Rect> rectList){

        rectList.removeIf(r -> r.area() == 0);

        return rectList;
    }

    public Rect averageArea(List<Rect> rectList){
        double mean = 0;
        double sum = 0;

        rectList = removeZeros(rectList);

        for(Rect r: rectList){
            sum += r.area();
        }
        mean = sum/rectList.size();

        // gucken welches element am nächsten beim mittelwert ist
        double distance = Math.abs(rectList.get(0).area() - mean);
        int index = 0;
        for(int c = 1; c < rectList.size(); c++){
            double cdistance = Math.abs(rectList.get(c).area()-mean);
            if(cdistance < distance){
                index = c;
                distance = cdistance;
            }
        }

        double finalArea = rectList.get(index).area();
        Rect rect = rectList.get(index);
        System.out.println("Mittelwert: "+ mean+ " FinalArea: " + finalArea);
        return rect;
    }

    public Mat getImage() {
        return image;
    }

    public void setImage(Mat image) {
        this.image = image;
    }
}
