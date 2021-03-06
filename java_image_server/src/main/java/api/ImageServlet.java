package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.Image;
import dao.ImageDao;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ImageServlet extends HttpServlet {
    /**
     * 查看图片属性: 既能查看所有图片的属性信息, 也能查看指定图片的属性信息
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /*  考虑到 查看所有图片属性信息 和 查看指定图片属性信息
            通过 URL 中是否带有 imageId 参数来进行区分
            存在 imageId 则查看指定图片属性信息，否则就查看所有图片属性信息
            例如： URL为   /image?imageId=100    那么则查看 imageId 为100的图片的属性信息
                   如果 URL 中不存在 imageId 等号后边的值， 那么返回 null
        */
        String imageId=req.getParameter("imageId");
        if(imageId==null||imageId.equals("")){
            //查看所有图片属性信息
            selectAll(req,resp);
        }else{
            //查看指定图片属性信息
            selectOne(imageId,resp);
        }
    }

    /**
     *  查看所有图片属性信息
     * @param req
     * @param resp
     * @throws IOException
     */
    private void selectAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=utf-8");
        // 1.创建一个 ImageDao 对象，并且查找数据库
        ImageDao imageDao=new ImageDao();
        List<Image> images=imageDao.selectAll();

        // 2.把查找到的结果转成 JSON 格式的字符串，并且传给 resp 对象
        Gson gson=new GsonBuilder().create();
             // jsonData 就是一个 json 格式的字符串了
            // 下面的这行代码是核心操作，gson 帮我们自动完成了大量的格式转换工作
        String jsonData=gson.toJson(images);

        // 3.写响应 （在浏览器界面就可以显示 JSON 格式的字符串了）
        resp.getWriter().write(jsonData);
    }

    /**
     *  查看指定图片属性信息
     * @param imageId
     * @param resp
     * @throws IOException
     */
    private void selectOne(String imageId, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=utf-8");

        // 1.创建 ImageDao 对象
        ImageDao imageDao=new ImageDao();
        Image image=imageDao.selectOne(Integer.parseInt(imageId));

        // 2.创建 Gson 对象，使用 gson 对象吧查到的数据转成 json 格式
        Gson gson=new GsonBuilder().create();
        String jsonData=gson.toJson(image);

        // 3.写响应 （在浏览器界面就可以显示了）
        resp.getWriter().write(jsonData);
    }

    /**
     * 上传图片 （ 包括上传到数据库，包括写到磁盘对应位置 ）
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1.获取图片的属性信息，并且存入数据库
             // a)需要创建一个 factory 对象和 upload 对象，这是为了获取图片的属性信息而做的准备工作，是固定的逻辑用法
        FileItemFactory factory=new DiskFileItemFactory();
        ServletFileUpload upload=new ServletFileUpload(factory);

        // 理论上来说，HTTP 支持一个请求中同时上传多个文件，FileItem 就代表上传的一个文件对象，多个文件用 List 组织
        List<FileItem> items=null;
        try {
            // b)通过 upload 对象进一步解析请求（解析 HTTP 请求中的 body 部分的内容）
            items=upload.parseRequest(req);
        } catch (FileUploadException e) {
            // 出现异常说明上传图片请求解析出错了，需要告诉客户端出现的具体错误是什么
            e.printStackTrace();
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write("{ \"ok\":false,\"reason\":\"上传图片请求解析出错\" }");
            return;
        }
        // c)把 List<FileItem> 中的属性提取出来，转换成 Image 对象，这样才能保存到数据库当中去
        FileItem fileItem=items.get(0);  //我们一次只上传一个，一个图片就是一个 FileItem
        Image image=new Image();
        image.setImageName(fileItem.getName());
        image.setSize((int)fileItem.getSize());
        //手动获取一下当前日期，并转成格式化日期,yyyyMMdd => 20200218
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd");
        image.setUploadTime(simpleDateFormat.format(new Date()));
        image.setContentType(fileItem.getContentType());   //设置图片本身的类型

        image.setMd5(DigestUtils.md5Hex(fileItem.get()));// md5 值是十六进制的字符串
        image.setPath("./image/" + image.getMd5());

        // 最后存到数据库中
        ImageDao imageDao = new ImageDao();
        // 还没有插入前，先看看数据库中是否存在相同的 Md5 值的图片, 不存在返回 null
        Image existImage = imageDao.selectByMd5(image.getMd5());
        //不管怎样都往数据库中插入，往数据库中可以插入多张重复的图片，但是尽管多张，但只在磁盘写一份就行
        imageDao.insert(image);

        // 2.如果没有插入这张图片前，磁盘没有存在这张图片，即 existImage==null，获取图片的内容信息，并且写入磁盘文件
        if (existImage == null) {
            File file = new File(image.getPath());   //往磁盘的那个位置存图片
            try {
                fileItem.write(file);   //把 fileItem 对应的这个图片文件写到相应位置
            } catch (Exception e) {
                e.printStackTrace();
                resp.setContentType("application/json; charset=utf-8");
                resp.getWriter().write("{ \"ok\": false, \"reason\": \"写磁盘失败\" }");
                return;
            }
        }

        // 3. 给客户端返回一个结果数据
//        resp.setContentType("application/json; charset=utf-8");
//        resp.getWriter().write("{ \"ok\": true }");
        // HTTP 采用302 可以重定向 到  index.html 页面

        // 本来显示的是 JSON 格式的数据，但是结果不直接，需要上传成功后跳转到主页面 index.html
        resp.sendRedirect("index.html");
    }

    /**
     * 删除指定图片
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=utf-8");
        // 1.先获取到请求中的 imageId
        String imageId=req.getParameter("imageId");
        if(imageId==null||imageId.equals("")){
            resp.setStatus(200);
            resp.getWriter().write("{ \"ok\":false,\"reason\":\"解析请求中的 imageId 出错\" }");
            return;
        }
        // 2.创建 ImageDao 对象，先查看该 imageId 对应的图片是否存在，若存在则要删除
        //  既要删除数据库中的内容，也要删除磁盘中存储的对应图片，因此要知道该 imageId 对应的图片对象的存储路径
        ImageDao imageDao=new ImageDao();
        Image image=imageDao.selectOne(Integer.parseInt(imageId));
        if(image==null){    //说明输入的 imageId 是错误的，数据库中没有这个 imageId 对应的图片
            resp.getWriter().write("{ \"ok\":false,\"reason\":\"传入的 imageId 在该数据库中没有对应值\" }");
            return;
        }
        // 3.走到这说明数据库中有该 imageId 对应的图片，那么就要删除数据库中的记录
        imageDao.delete(Integer.parseInt(imageId));

        // 4.删除本地磁盘文件（注意：数据库中可能有多张相同的图片，但是磁盘中只写入了一张图片，删除要注意）
        //   刚刚已经从数据库中删除了那张图片，若此时数据库中还存有相同的图片，那么不应该删除磁盘图片文件
        //   相同图片的 md5 值是相同的
        Image existImage=imageDao.selectByMd5(image.getMd5());
        // 如果删除完毕后，数据库中没有相同的图片了，那么就删除磁盘文件，否则就不删除
        if(existImage==null) {
            File file = new File(image.getPath());
            file.delete();
            resp.getWriter().write("{ \"ok\":true }");
        }
    }
}
