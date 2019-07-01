package com.how2java.tmall.service;

import com.how2java.tmall.pojo.ProductImage;

import java.util.List;

public interface ProductImageService {

    //提供了两个常量，分别表示单个图片和详情图片：
    String type_single = "type_single";
    String type_detail = "type_detail";

    void add(ProductImage pi);
    void delete(int id);
    void update(ProductImage pi);
    ProductImage get(int id);

    //提供了根据产品id和图片类型查询的list方法
    List list(int pid, String type);
}
