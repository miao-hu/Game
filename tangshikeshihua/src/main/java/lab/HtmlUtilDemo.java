package lab;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlBody;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class HtmlUtilDemo {
    public static void main(String[] args) throws IOException {
        //无界面的浏览器(HTTP客户端)
        WebClient webClient=new WebClient(BrowserVersion.CHROME);
        //关闭了浏览器的js执行引擎，不再执行网页中的js脚本
        webClient.getOptions().setJavaScriptEnabled(false);
        //关闭了浏览器的css执行引擎，不再执行网页中的css布局
        webClient.getOptions().setCssEnabled(false);
        HtmlPage page = webClient.getPage("https://so.gushiwen.org/gushi/tangshi.aspx");
        System.out.println(page);

        File file=new File("唐诗三百首\\列表页.html");
        file.delete();
        page.save(new File("唐诗三百首\\列表页.html"));

        //如何从html 中提取我们需要的信息
        HtmlElement body= page.getBody();
        List<HtmlElement> elements=body.getElementsByAttribute(
                "div",
                "class",
                "typecont");
        for(HtmlElement element:elements){
            System.out.println(element);
        }
        /* 打印结果：
        HtmlPage(https://so.gushiwen.org/gushi/tangshi.aspx)@1424108509
        HtmlDivision[<div class="typecont">]
        HtmlDivision[<div class="typecont">]
        HtmlDivision[<div class="typecont">]
        HtmlDivision[<div class="typecont">]
        HtmlDivision[<div class="typecont">]
        HtmlDivision[<div class="typecont">]
        HtmlDivision[<div class="typecont" style="border:0px;">]
         */

        System.out.println("----------------------------------------");
        HtmlElement divElement=elements.get(0);   //取第一个模块
        List<HtmlElement> aElements=divElement.getElementsByAttribute(
                "a",
                "target",
                "_blank");
        for(HtmlElement element:aElements){
            System.out.println(element);
        }
        System.out.println(aElements.size());
        System.out.println(aElements.get(0).getAttribute("href"));
    }
}
