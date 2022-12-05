
import org.opencv.core.*;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


import javax.imageio.ImageIO;
import javax.imageio.plugins.jpeg.JPEGImageReadParam;
import javax.swing.*;
import java.awt.*;

import org.opencv.core.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import java.io.FileWriter;
import java.io.InputStream;
import java.sql.SQLOutput;
import java.util.*;
import java.util.List;


public class Main {
    public static void main(String[] args) throws Exception {
        initialiseOpenCv();

        // Reading the Image from the file and storing it in to a Matrix object
        String file = "Bilder/Erbsen.jpg";
        Mat image = Imgcodecs.imread(file);

        String fileTemp = "Bilder/ErbseGrey1.jpg";
        Mat templ = Imgcodecs.imread(fileTemp);
        templ = convertToGrey(templ);


        // ConvertToGrey and scale the image
        Mat dst = scalegMat(convertToGrey(image));

        // result matrix
        int result_cols = image.cols() - templ.cols() + 1;
        int result_rows = image.rows() - templ.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        String file4 = "Bilder/result0.jpg";
        Imgcodecs.imwrite(file4, result);

        Point matchLoc;
        List<Point> list = new ArrayList<Point>();
        List<String> listValue = new ArrayList<>();


        /*
        Imgproc.matchTemplate(dst, templ, result, Imgproc.TM_CCOEFF_NORMED);
        Core.normalize(result, result, 0, 1,Core.NORM_MINMAX, -1, new Mat());

        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        matchLoc = mmr.maxLoc;
        list.add(String.valueOf(matchLoc));
        System.out.println("List of points: "+ list);
*/

        //Imgproc.rectangle(imageDisplay, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
              //  new Scalar(0, 0, 0), 2, 8, 0);
        /*
        Imgproc.rectangle(dst, matchLoc, new Point(matchLoc.x + templ.cols(),
                matchLoc.y + templ.rows()), new Scalar(0,0,0), 2,8,0);
        System.out.println(matchLoc);
        System.out.println(templ.cols());
        System.out.println(templ.rows());

        System.out.println(new Point(matchLoc.x + templ.cols(),
                matchLoc.y + templ.rows())); */

        /*String file3 = "Bilder/MatchTemp.jpg";
        Imgcodecs.imwrite(file3, dst); */
        // multiple template matching

        double maxvalue;
        double threshold = 0.95;

        boolean firstrun = true;


        while(true){
            if(firstrun == false){
                Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
                matchLoc = mmr.maxLoc;
                maxvalue = mmr.maxVal;
                System.out.println("Maxval: " + maxvalue);
                System.out.println("matchLoc" + matchLoc);
                //Point matchLocEnd = new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows());
                //System.out.println("matchLocEnd"+ matchLocEnd);


                //d = image.clone();
                if(maxvalue >= threshold){
                    //list.add(String.valueOf(matchLoc));
                    list.add(matchLoc);
                    listValue.add(String.valueOf(maxvalue));
                    System.out.println("Template Matches with input image");
                    // Rectangle all objects over the threshold in the original image
                    //Imgproc.rectangle(dst, matchLoc, new Point(matchLoc.x + templ.cols(),matchLoc.y + templ.rows()),
                    //        new Scalar(0,255,0), 2,8,0);
                    Imgproc.rectangle(result, matchLoc, new Point(matchLoc.x + templ.cols(),matchLoc.y + templ.rows()),
                            new Scalar(0, 255, 0), 2,8,0);
                }else{
                    break;
                }
            }else {
                Imgproc.matchTemplate(dst, templ, result, Imgproc.TM_CCOEFF_NORMED);
                Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
                Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
                matchLoc = mmr.maxLoc;
                maxvalue = mmr.maxVal;
                //String file3 = "Bilder/result2.jpg";
                //Imgcodecs.imwrite(file3, result);

                //list.add(String.valueOf(matchLoc));
                list.add(matchLoc);
                listValue.add(String.valueOf(maxvalue));
                System.out.println("List of points" + list);
                Imgproc.rectangle(dst, matchLoc, new Point(matchLoc.x + templ.cols(),
                        matchLoc.y + templ.rows()), new Scalar(0, 0, 0), 2, 8, 0);
                Imgproc.rectangle(result, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
                        new Scalar(0, 0, 0), 2, 8, 0);
                firstrun = false;
            }

        }


        // todo: x und y values vergleichen, zu nah aneinander löschen bzw nicht anzeigen
        list.sort(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                int result = Double.compare(o1.x, o2.x);
                if (result == 0) result = Double.compare(o1.y, o2.y);
                return result;
            }
        });

        // entfernen zu naher koordinaten
        Point previousPoint = new Point();
        List<Point> totalPoints = new ArrayList<Point>();
        boolean firstrun2 = true;
        // anzeigen der gefundenen Erbsen
        for(Point p : list){
            if (firstrun2){
                previousPoint.x = p.x;
                previousPoint.y = p.y;
                totalPoints.add(p);
                Imgproc.rectangle(dst, p, new Point(p.x + templ.cols(),
                        p.y + templ.rows()), new Scalar(0, 0, 0), 2, 8, 0);
                firstrun2 = false;
                continue;
            }
            if((p.x == previousPoint.x & p.y == previousPoint.y) ||
                    (p.x <= previousPoint.x + 20 & p.y <= previousPoint.y + 20)) {
                System.out.println("gleiche werte");
            }else{
                totalPoints.add(p);
                Imgproc.rectangle(dst, p, new Point(p.x + templ.cols(),
                        p.y + templ.rows()), new Scalar(0, 0, 0), 2, 8, 0);
                previousPoint.x = p.x;
                previousPoint.y = p.y;
            }

        }

        System.out.println(totalPoints.size());

        // store the grey image
        //String file2 = "Bilder/result1.jpg";
        //Imgcodecs.imwrite(file2, result);
        //System.out.println("List of points " + list);
        FileWriter writer = new FileWriter("output.txt");
        for(Point str: list){
            writer.write(str + System.lineSeparator());
        }
        writer.close();

        FileWriter writer3 = new FileWriter("totalpoints.txt");
        for(Point str: totalPoints){
            writer3.write(str + System.lineSeparator());
        }
        writer3.close();

        FileWriter writer2 = new FileWriter("maxValues.txt");
        for(String str: listValue){
            writer2.write(str + System.lineSeparator());
        }
        writer2.close();

        BufferedImage image2 = convertMatToBufImg(dst);
        displayImage(image2);
    }

    public static void initialiseOpenCv(){
        // Loading the OpenCV core library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println(Core.VERSION);
        System.out.println("load success");
    }

    public static Mat convertToGrey(Mat src){
        Mat greyImage = new Mat();
        Imgproc.cvtColor(src, greyImage, Imgproc.COLOR_BGR2GRAY);
        return greyImage;
    }

    public static Mat scalegMat(Mat src){

        // Creating an empty matrix to store the result
        Mat dst = new Mat();
        // Creating the Size object
        Size size = new Size(src.cols()*0.5, src.rows()*0.5);
        // Scaling the Image
        Imgproc.resize(src, dst, size, 0,0, Imgproc.INTER_AREA);
        System.out.println("rescaled");
        return dst;
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