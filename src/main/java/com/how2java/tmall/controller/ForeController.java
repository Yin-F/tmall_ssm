package com.how2java.tmall.controller;

import com.github.pagehelper.PageHelper;
import com.how2java.tmall.pojo.*;
import com.how2java.tmall.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;
import tmall.comparator.*;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("")
public class ForeController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ReviewService reviewService;

    @RequestMapping("forehome")
    /*
        1. 查询所有分类
        2. 为这些分类填充产品集合
        3. 为这些分类填充推荐产品集合
        4. 服务端跳转到home.jsp
     */
    public String home(Model model) {
        List<Category> cs= categoryService.list();
        productService.fill(cs);
        productService.fillByRow(cs);
        model.addAttribute("cs", cs);
        return "fore/home";
    }


    @RequestMapping("foreregister")
    /*
        1. 通过参数User获取浏览器提交的账号密码
        2. 通过HtmlUtils.htmlEscape(name);把账号里的特殊符号进行转义
        3. 判断用户名是否存在
        3.1 如果已经存在，就服务端跳转到reigster.jsp，并且带上错误提示信息
        3.2 如果不存在，则加入到数据库中，并服务端跳转到registerSuccess.jsp页面
        注:为什么要用 HtmlUtils.htmlEscape？ 因为有些同学在恶意注册的时候，会使用诸如 <script>alert('papapa')</script> 这样的名称，会导致网页打开就弹出一个对话框。 那么在转义之后，就没有这个问题了。
        注:model.addAttribute("user", null); 这句话的用处是当用户存在，服务端跳转到register.jsp的时候不带上参数user, 否则当注册失败的时候，会在原本是“请登录”的超链位置显示刚才注册的名称。 可以试试把这一条语句注释掉观察这个现象
    */
    public String register(Model model, User user) {
        String name =  user.getName();
        name = HtmlUtils.htmlEscape(name);
        user.setName(name);
        boolean exist = userService.isExist(name);

        if(exist){
            String m ="用户名已经被使用,不能使用";
            model.addAttribute("msg", m);
            model.addAttribute("user", null);
            return "fore/register";
        }
        userService.add(user);

        return "redirect:registerSuccessPage";
    }


    @RequestMapping("forelogin")
    /*
        loginPage.jsp的form提交数据到路径 forelogin,导致ForeController.login()方法被调用
            1. 获取账号密码
            2. 把账号通过HtmlUtils.htmlEscape进行转义
            3. 根据账号和密码获取User对象
            3.1 如果对象为空，则服务端跳转回login.jsp，也带上错误信息，并且使用 loginPage.jsp 中的办法显示错误信息
            3.2 如果对象存在，则把对象保存在session中，并客户端跳转到首页"forehome"
            注： 为什么要用 HtmlUtils.htmlEscape？ 因为注册的时候，ForeController.register()，就进行了转义，所以这里也需要转义。
                有些同学在恶意注册的时候，会使用诸如 <script>alert('papapa')</script> 这样的名称，会导致网页打开就弹出一个对话框。 那么在转义之后，就没有这个问题了。
    */
    public String login(@RequestParam("name") String name, @RequestParam("password") String password, Model model, HttpSession session) {
        name = HtmlUtils.htmlEscape(name);
        User user = userService.get(name,password);

        if(null==user){
            model.addAttribute("msg", "账号密码错误");
            return "fore/login";
        }
        session.setAttribute("user", user);
        return "redirect:forehome";
    }


    @RequestMapping("forelogout")
    /*
        通过访问退出路径：
        http://127.0.0.1:8080/tmall_ssm/forelogout
        导致ForeController.logout()方法被调用
        1. 在session中去掉"user"
        session.removeAttribute("user");
        2. 客户端跳转到首页:
        return "redirect:forehome";
    */
    public String logout( HttpSession session) {
        session.removeAttribute("user");
        return "redirect:forehome";
    }


    @RequestMapping("foreproduct")
    /*
        通过访问地址
        http://127.0.0.1:8080/tmall_ssm/foreproduct?pid=844
        导致ForeController.product() 方法被调用
        1. 获取参数pid
        2. 根据pid获取Product 对象p
        3. 根据对象p，获取这个产品对应的单个图片集合
        4. 根据对象p，获取这个产品对应的详情图片集合
        5. 获取产品的所有属性值
        6. 获取产品对应的所有的评价
        7. 设置产品的销量和评价数量
        8. 把上述取值放在request属性上
        9. 服务端跳转到 "product.jsp" 页面
    */
    public String product( int pid, Model model) {
        Product p = productService.get(pid);

        List<ProductImage> productSingleImages = productImageService.list(p.getId(), ProductImageService.type_single);
        List<ProductImage> productDetailImages = productImageService.list(p.getId(), ProductImageService.type_detail);
        p.setProductSingleImages(productSingleImages);
        p.setProductDetailImages(productDetailImages);

        List<PropertyValue> pvs = propertyValueService.list(p.getId());
        List<Review> reviews = reviewService.list(p.getId());
        productService.setSaleAndReviewNumber(p);
        model.addAttribute("reviews", reviews);
        model.addAttribute("p", p);
        model.addAttribute("pvs", pvs);
        return "fore/product";
    }

    @RequestMapping("forecheckLogin")
    @ResponseBody
    /*
        在上一步的ajax访问路径/forecheckLogin会导致ForeController.checkLogin()方法被调用。
        获取session中的"user"对象
            如果不为空，即表示已经登录，返回字符串"success"
            如果为空，即表示未登录，返回字符串"fail"
    */
    public String checkLogin( HttpSession session) {
        User user =(User)  session.getAttribute("user");
        if(null!=user)
            return "success";
        return "fail";
    }

    @RequestMapping("foreloginAjax")
    @ResponseBody
    /*
        在上一步modal.jsp中，点击了登录按钮之后，访问路径/foreloginAjax,导致ForeController.loginAjax()方法被调用
        1. 获取账号密码
        2. 通过账号密码获取User对象
        2.1 如果User对象为空，那么就返回"fail"字符串。
        2.2 如果User对象不为空，那么就把User对象放在session中，并返回"success" 字符串
    */
    public String loginAjax(@RequestParam("name") String name, @RequestParam("password") String password,HttpSession session) {
        name = HtmlUtils.htmlEscape(name);
        User user = userService.get(name,password);

        if(null==user){
            return "fail";
        }
        session.setAttribute("user", user);
        return "success";
    }

    @RequestMapping("forecategory")
    /*
        1. 获取参数cid
        2. 根据cid获取分类Category对象 c
        3. 为c填充产品
        4. 为产品填充销量和评价数据
        5. 获取参数sort
        5.1 如果sort==null，即不排序
        5.2 如果sort!=null，则根据sort的值，从5个Comparator比较器中选择一个对应的排序器进行排序
        6. 把c放在model中
        7. 服务端跳转到 category.jsp
    */
    public String category(int cid,String sort, Model model) {
        Category c = categoryService.get(cid);
        productService.fill(c);
        productService.setSaleAndReviewNumber(c.getProducts());

        if(null!=sort){
            switch(sort){
                case "review":
                    Collections.sort(c.getProducts(),new ProductReviewComparator());
                    break;
                case "date" :
                    Collections.sort(c.getProducts(),new ProductDateComparator());
                    break;

                case "saleCount" :
                    Collections.sort(c.getProducts(),new ProductSaleCountComparator());
                    break;

                case "price":
                    Collections.sort(c.getProducts(),new ProductPriceComparator());
                    break;

                case "all":
                    Collections.sort(c.getProducts(),new ProductAllComparator());
                    break;
            }
        }

        model.addAttribute("c", c);
        return "fore/category";
    }

    @RequestMapping("foresearch")
    /*
        通过search.jsp或者simpleSearch.jsp提交数据到路径 /foresearch， 导致ForeController.search()方法被调用
            1. 获取参数keyword
            2. 根据keyword进行模糊查询，获取满足条件的前20个产品
            3. 为这些产品设置销量和评价数量
            4. 把产品结合设置在model的"ps"属性上
            5. 服务端跳转到 searchResult.jsp 页面
    */
    public String search( String keyword,Model model){

        PageHelper.offsetPage(0,20);
        List<Product> ps= productService.search(keyword);
        productService.setSaleAndReviewNumber(ps);
        model.addAttribute("ps",ps);
        return "fore/searchResult";
    }
}







