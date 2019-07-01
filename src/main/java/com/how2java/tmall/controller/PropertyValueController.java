package com.how2java.tmall.controller;

import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.PropertyValue;
import com.how2java.tmall.service.ProductService;
import com.how2java.tmall.service.PropertyValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("")
public class PropertyValueController {

    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    ProductService productService;

    /*
    * 通过产品管理界面的设置属性，跳到编辑页面
        调用PropertyValueController的edit方法：
        1. 根据pid获取product对象，因为面包屑导航里需要显示产品的名称和分类的连接。
        2. 初始化属性值： propertyValueService.init(p)。 因为在第一次访问的时候，这些属性值是不存在的，需要进行初始化。
        3. 根据产品，获取其对应的属性值集合。
        4. 服务端跳转到editPropertyValue.jsp 上
        5. 在editPropertyValue.jsp上，用c:forEach遍历出这些属性值
    * */
    @RequestMapping("admin_propertyValue_edit")
    public String edit(Model model, int pid) {
        Product p = productService.get(pid);
        propertyValueService.init(p);
        List<PropertyValue> pvs = propertyValueService.list(p.getId());

        model.addAttribute("p", p);
        model.addAttribute("pvs", pvs);
        return "admin/editPropertyValue";
    }
    @RequestMapping("admin_propertyValue_update")
    @ResponseBody
    /*
    * @ResponseBody是作用在方法上的，@ResponseBody 表示该方法的返回结果直接写入 HTTP response body 中，一般在异步获取数据时使用【也就是AJAX】，
    * 在使用 @RequestMapping后，返回值通常解析为跳转路径，但是加上 @ResponseBody 后返回结果不会被解析为跳转路径，而是直接写入 HTTP response body 中。
    * 比如异步获取 json 数据，加上 @ResponseBody 后，会直接返回 json 数据。
    * @RequestBody 将 HTTP 请求正文插入方法中，使用适合的 HttpMessageConverter 将请求体写入某个对象
    * */
    /*
    * 修改功能采用的是使用post方式提交ajax的异步调用方式
        1. 监听输入框上的keyup事件
        2. 获取输入框里的值
        3. 获取输入框上的自定义属性pvid，这就是当前PropertyValue对应的id
        4. 把边框的颜色修改为黄色，表示正在修改的意思
        5. 借助JQuery的ajax函数 $.post，把id和值，提交到admin_propertyValue_update
        6. admin_propertyValue_update导致PropertyValueController的update方法被调用
        6.1 参数 PropertyValue 获取浏览器Ajax方式提交的参数
        6.2 通过 propertyValueService.update(propertyValue) 更新到数据库
        6.3 结合方法update上的注解@ResponseBody和return "success" 就会向浏览器返回字符串 "success"
        6.4 propertyValueService调用的是propertValueMapper.updateByPrimaryKeySelective(pv); 这个方法只会更新propertyValue存在的字段，而参数PropertyValue只有id和value有值，所以即便这个PropertyValue对象没有pid和ptid值，修改的时候也不会影响该PropertyValue的pid和ptid。
        7 浏览器判断如果返回值是"success",那么就把边框设置为绿色，表示修改成功，否则设置为红色，表示修改失败
    * */
    public String update(PropertyValue pv) {
        propertyValueService.update(pv);
        return "success";
    }
}
