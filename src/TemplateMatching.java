import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TemplateMatching {
    private double threshold;
    private Mat srcImage;
    private Mat templateImage;

    public TemplateMatching(Mat srcImage, Mat templateImage, double threshold){
        this.setSrcImage(srcImage);
        this.setTemplateImage(templateImage);
        this.setThreshold(threshold);
    }

    public TemplateMatching(){}

    public List<Point> detectTemplate(Mat srcImage, Mat templateImage, double threshold){

        List<Point> detectedPoints = new ArrayList<Point>();
        List<Double> detectedValue = new ArrayList<>();
        Point matchLoc;
        double maxvalue;
        Mat dst = srcImage.clone();

        // result matrix
        int result_cols = srcImage.cols() - templateImage.cols() + 1;
        int result_rows = srcImage.rows() - templateImage.rows() + 1;
        Mat resultMat = new Mat(result_rows, result_cols, CvType.CV_32FC1);


        Imgproc.matchTemplate(dst, templateImage, resultMat, Imgproc.TM_CCOEFF_NORMED);
        Core.normalize(resultMat, resultMat, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Core.MinMaxLocResult mmr = Core.minMaxLoc(resultMat);
        matchLoc = mmr.maxLoc;
        maxvalue = mmr.maxVal;


        detectedPoints.add(matchLoc);
        detectedValue.add(maxvalue);
        System.out.println("List of points " + detectedPoints);
        System.out.println("List of maxvalue " + detectedValue);


        //Imgproc.rectangle(dst, matchLoc, new Point(matchLoc.x + templateImage.cols(),
        //        matchLoc.y + templateImage.rows()), new Scalar(0, 0, 0), 2, 8, 0);
        Imgproc.rectangle(resultMat, matchLoc, new Point(matchLoc.x + templateImage.cols(), matchLoc.y + templateImage.rows()),
                new Scalar(0, 255, 0), 1, 8, 0);


        while(true){
            mmr = Core.minMaxLoc(resultMat);
            matchLoc = mmr.maxLoc;
            maxvalue = mmr.maxVal;

            if(maxvalue >= threshold){
                detectedPoints.add(matchLoc);
                detectedValue.add(maxvalue);

                //System.out.println("Template Matches with input image");
                // Rectangle all objects over the threshold in the original image
                Imgproc.rectangle(dst, matchLoc, new Point(matchLoc.x + templateImage.cols(),matchLoc.y + templateImage.rows()),
                       new Scalar(0,255,0), 1,8,0);
                Imgproc.rectangle(resultMat, matchLoc, new Point(matchLoc.x + templateImage.cols(),matchLoc.y + templateImage.rows()),
                        new Scalar(0, 255, 0), 1,8,0);
            }else{
                break;
            }
        }


        resultMat.convertTo(resultMat, CvType.CV_8UC1, 255.0);

        String file3 = "Bilder/MultiMatchTemp2.jpg";
        Imgcodecs.imwrite(file3, dst);
        String file5 = "Bilder/result4.jpg";
        Imgcodecs.imwrite(file5, resultMat);

        return detectedPoints;
    }

    //Todo: noch fehler drin, muss verbessert werden. Es werden zu viele Rechecke gelöscht die valid sind
    public List<Point> removeNearPoints(List<Point> listPoints, Mat srcImage, Mat templateImage) throws IOException {
        // sortList(listPoints);
        System.out.println("ListofPoints: " + listPoints);

        FileWriter writer = new FileWriter("ListOfPoints2Sortiert");
        for(Point p: listPoints){
            writer.write(p + System.lineSeparator());
        }
        writer.close();


        // entfernen zu naher Koordinaten
        Point previousPoint = new Point();
        List<Point> totalPoints = new ArrayList<Point>();

        // Startkoordinaten
        previousPoint.x = listPoints.get(0).x;
        previousPoint.y = listPoints.get(0).y;
        totalPoints.add(listPoints.get(0));
        System.out.println("Add Points: " + listPoints.get(0));
        Imgproc.rectangle(srcImage, listPoints.get(0), new Point(previousPoint.x + templateImage.cols(),
                previousPoint.y + templateImage.rows()), new Scalar(0, 0, 0), 1, 8, 0);


        String file5 = "Bilder/firstRectangle.jpg";
        Imgcodecs.imwrite(file5, srcImage);

        boolean inDistance = false;
        double distance = 20;
        for(Point p: listPoints.subList(1, listPoints.size())){
            for(Point n : totalPoints){
                if((p.x == n.x) & (p.y == n.y)){
                    continue;
                }else if (Math.hypot((p.x-n.x), (p.y-n.y)) > distance){
                    inDistance = true;
                }else{
                    inDistance = false;
                    break;
                }
            }
            if(inDistance){
                totalPoints.add(p);
                //System.out.println("Add Points: " + p);
                Imgproc.rectangle(srcImage, p, new Point(p.x + templateImage.cols(),
                        p.y + templateImage.rows()), new Scalar(0, 0, 0), 1, 8, 0);
                System.out.println("nicht gleiche werte: "+ p.x+ " " + p.y);
                System.out.println("totalpointssize: " + totalPoints.size());
                //Collections.reverse(totalPoints);
            }

        }

        String file3 = "Bilder/AfterRemoveNearPoints2.jpg";
        Imgcodecs.imwrite(file3, srcImage);

        FileWriter writer2 = new FileWriter("ListOfTotalPoints2Sortiert");
        for(Point p: totalPoints){
            writer2.write(p + System.lineSeparator());
        }
        writer2.close();

        System.out.println("Es wurden: " + totalPoints.size() + " Objekte gefunden.");
        return totalPoints;
    }

    public static void groupRectangles(MatOfRect rectList, MatOfInt weights, int groupThreshold){

        Mat rectList_mat = rectList;
        Mat weights_mat = weights;
        groupRectangles_1(rectList_mat.nativeObj, weights_mat.nativeObj, groupThreshold);

        return;
    }
    private static native void groupRectangles_1(long rectList_mat_nativeObj, long weights_mat_nativeObj, int groupThreshold);

    public void sortList(List<Point> listPoints){
        // Liste sortieren
        listPoints.sort(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                int result = Double.compare(o1.x, o2.x);
                if (result == 0) result = Double.compare(o1.y, o2.y);
                return result;
            }
        });
    }


    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public Mat getSrcImage() {
        return srcImage;
    }

    public void setSrcImage(Mat srcImage) {
        this.srcImage = srcImage;
    }

    public Mat getTemplateImage() {
        return templateImage;
    }

    public void setTemplateImage(Mat templateImage) {
        this.templateImage = templateImage;
    }
}
