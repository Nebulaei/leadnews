package com.heima.common.tess4j;

import net.sourceforge.tess4j.Tesseract;

import java.io.File;

public class Tess4jTest {

    public static void main(String[] args) {
        Tesseract tesseract = new Tesseract();

        tesseract.setDatapath("/Users/dongenzhe/Documents/Program/toutiao/tessdata");
        tesseract.setLanguage("shi_sim");
//        String res = tesseract.doOCR(new File());
    }
}