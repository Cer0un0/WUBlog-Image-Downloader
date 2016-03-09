package main;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

public class AmebaJpeg {
	static URL firstArticle;
	static String ameblo = "http://ameblo.jp/wakeupgirls/"; //ブログのトップページ
	static String saveDirectory = "C:\\workspace\\Java\\WUBlog Image Downloader"; //保存したいディレクトリ
	
	public static StringBuilder getSourceText(URL url) throws IOException { //引数のURLのソースコードの取得
		InputStream in = url.openStream();
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader bf = new BufferedReader(new InputStreamReader(in));
			String s;
			while ((s=bf.readLine())!=null) {
				sb.append(s);
//				System.out.println(s);
			}
		} finally {
			in.close();
		}
		return sb;
	}
	
	public static URL first_Article(StringBuilder sb) throws MalformedURLException{ //一番最新の記事を探す
		int article = sb.indexOf("skin-entryTitle");
		int http = sb.indexOf("http",article);
		int html = sb.indexOf("html",http);
		URL firstArticle = new URL(sb.substring(http,html+4));
		return firstArticle;
	}
	
	public static void folderExist(String s){ //フォルダがあるのかの確認。なければ作成
		File file = new File(saveDirectory + s);
		if(!file.exists())	//フォルダ無し
			file.mkdirs();
	}
	
	public static int fileExist(String s){ //ファイルがあるのかの確認
		File file = new File(saveDirectory + s);
		if(!file.exists())	//ファイル無し
			return -1;
		return 0;
	}
	
	
	public static void saveJpeg(StringBuilder sb) throws URISyntaxException, IOException{ //その記事の画像を全て保存
		int entryBody = sb.indexOf("entryBody");
		int entryEnd = sb.indexOf("entryAd");
		String saveUrl=null, fileName=null;
		String formatName=null;
		URLConnection urlcon;
		BufferedImage bi =null;

		int jpg = sb.indexOf("jpg", entryBody);
		int http = sb.lastIndexOf("http", jpg);
		while(jpg < entryEnd){
			saveUrl = sb.substring(http, jpg+3);
			System.out.println(sb.substring(http, jpg+3));
			if(fileExist("\\images\\" + sb.substring(jpg-20, jpg+3))==-1){
				fileName = sb.substring(jpg-20, jpg+3);
				URI uri =new URI(saveUrl);
				URL url=uri.toURL();
				urlcon =url.openConnection();
				if(urlcon.getContentType().equals("image/jpeg"))formatName ="jpg";
				bi =ImageIO.read(urlcon.getInputStream());
				File saveFile = new File("images\\" + fileName);
				ImageIO.write(bi, formatName,saveFile);
			}
			jpg = sb.indexOf("jpg", jpg+5);
			http = sb.lastIndexOf("http", jpg);
		}
		
	}
	
	public static URL next_Article(StringBuilder sb) throws MalformedURLException{ //次の記事のURL
		int jump = sb.indexOf("keyJumpNav");
		int http = sb.indexOf("http",jump);
		int html = sb.indexOf("html",http);
		URL nextArticle = new URL(sb.substring(http, html+4));
		return nextArticle;
	}
	
	public static int next_Article_Exist(StringBuilder sb){ //次の記事が存在するか
		int exist = sb.indexOf("次のページヘ");
		if(exist!=-1)return 1;
		else return -1;
	}
	
	
	
	public static void main(String[] args) throws MalformedURLException, IOException, URISyntaxException {
		String url = ameblo;
		firstArticle = first_Article(getSourceText(new URL(url)));
		folderExist("\\images");
		StringBuilder source = getSourceText(firstArticle);
		saveJpeg(source);
		StringBuilder next = null;
		while(true){
			next = getSourceText(next_Article(source));
			saveJpeg(next);
			source = next;
			if(next_Article_Exist(source)==-1){
				System.out.println("next");
				break;
			}
		}
	}

}
