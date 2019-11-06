package com.mmall.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author YaningLiu
 * @date 2018/9/9/ 10:35
 */
@EqualsAndHashCode(exclude = {"id"})
@Getter
@Setter
public class ProductListVo {

    private Integer id;
    private Integer categoryId;

    private String name;
    private String subtitle;
    private String mainImage;
    private BigDecimal price;

    private Integer status;

    private String imageHost;

}
