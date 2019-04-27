package com.hou.gradproj.docmanagesys.util;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.Field;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.core.FileURIResolver;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;

public class OfficeFileConverter {
    public static void convert(com.hou.gradproj.docmanagesys.model.File file) throws Throwable {
        String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
        switch (file.getType().getName()) {
            case FILE_TYPE_DOC:
                if (suffix.equals("doc")) {
                    convertWord03ToHtml(file);
                } else if (suffix.equals("docx")) {
                    convertWord07ToHtml(file);
                }
                break;
        }
    }

    private static void convertWord03ToHtml(com.hou.gradproj.docmanagesys.model.File file) throws Throwable {
        InputStream input = new FileInputStream(file.getPath() + "/" + file.getName());
        //实例化WordToHtmlConverter，为图片等资源文件做准备
        WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument());
        wordToHtmlConverter.setPicturesManager((content, pictureType, suggestedName, widthInches, heightInches) -> suggestedName);
        // docx
        HWPFDocument wordDocument = new HWPFDocument(input);
        wordToHtmlConverter.processDocument(wordDocument);
        //处理图片，会在同目录下生成 image/media/ 路径并保存图片
        List pics = wordDocument.getPicturesTable().getAllPictures();
        File imgPath = new File(file.getPath()/* + "/imgs"*/);
//        if (!imgPath.exists()) {
//            imgPath.mkdirs();
//        }
        if (!pics.isEmpty()) {
            for (Object o : pics) {
                Picture pic = (Picture) o;
                try {
                    pic.writeImageContent(new FileOutputStream(imgPath + "/"
                            + pic.suggestFullFileName()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        // 转换
        Document htmlDocument = wordToHtmlConverter.getDocument();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        DOMSource domSource = new DOMSource(htmlDocument);
        StreamResult streamResult = new StreamResult(outStream);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");//编码格式
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");//是否用空白分割
        serializer.setOutputProperty(OutputKeys.METHOD, "html");//输出类型
        serializer.transform(domSource, streamResult);
        outStream.close();
        String content = new String(outStream.toByteArray());
        FileUtils.writeStringToFile(new File(
                        file.getPath(),
                        file.getName().substring(0, file.getName().lastIndexOf(".")) + ".html"
                ),
                content,
                "utf-8");

    }

    private static void convertWord07ToHtml(com.hou.gradproj.docmanagesys.model.File file) throws IOException {
        File f = new File(file.getPath() + "/" + file.getName());
        if (!f.exists()) {
            System.out.println("Sorry File does not Exists!");
        } else {
            // 1) 加载XWPFDocument及文件
            InputStream in = new FileInputStream(f);
            XWPFDocument document = new XWPFDocument(in);
            // 2) 实例化XHTML内容(这里将会把图片等文件放到生成的"word/media"目录)
            File imageFolderFile = new File(file.getPath() /*+ "/imgs"*/);
//            if (!imageFolderFile.exists()) {
//                imageFolderFile.mkdirs();
//            }
            XHTMLOptions options = XHTMLOptions.create().URIResolver(
                    new FileURIResolver(imageFolderFile));
            options.setExtractor(new FileImageExtractor(imageFolderFile));
            //options.setIgnoreStylesIfUnused(false);
            //options.setFragment(true);
            // 3) 将XWPFDocument转成XHTML并生成文件
            OutputStream out = new FileOutputStream(new File(
                    file.getPath()
                            + "/"
                            + file.getName().substring(0, file.getName().lastIndexOf("."))
                            + ".html"
            ));
            XHTMLConverter.getInstance().convert(document, out, null);
        }
    }
}
