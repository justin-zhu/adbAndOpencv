package com.justin.mavenTest;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class Test {
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat source, template;
        //将文件读入为OpenCV的Mat格式
        source = Highgui.imread("D:\\autotest\\test0\\modelPath\\tempImage\\1.jpg");
       
        template = Highgui.imread("D:\\autotest\\test0\\modelPath\\tempImage\\2.jpg");
        //创建于原图相同的大小，储存匹配度
        System.out.println(source.empty());
        System.out.println("source.rows()"+source.rows()+","+(template.rows() + 1));
        System.out.println("source.cols()"+source.cols()+","+(template.cols() + 1));
        int srcNum = source.rows() - template.rows() + 1;
        int tempNum = source.cols() - template.cols() + 1;
        System.out.println(srcNum);
        System.out.println(tempNum);
        System.out.println(CvType.CV_32FC1);
        Mat result = Mat.zeros(srcNum, tempNum, CvType.CV_32FC1);
        //调用模板匹配方法
        Imgproc.matchTemplate(source, template, result, Imgproc.TM_SQDIFF_NORMED);
        //规格化
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1);
        //获得最可能点，MinMaxLocResult是其数据格式，包括了最大、最小点的位置x、y
        Core.MinMaxLocResult mlr = Core.minMaxLoc(result);
        Point matchLoc = mlr.minLoc;
        //在原图上的对应模板可能位置画一个绿色矩形
        Core.rectangle(source, matchLoc, new Point(matchLoc.x + template.width(), matchLoc.y + template.height()), new Scalar(0, 255, 0));
        //将结果输出到对应位置
        Highgui.imwrite("D:\\autotest\\test0\\modelPath\\tempImage\\22.jpg", source);
	}
}
