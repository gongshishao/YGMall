package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import com.sun.org.apache.regexp.internal.RE;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;

    /**
     * 查询所有数据
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbBrand> findAll() {
        return brandService.findAll();
    }

    /**
     *   分页查询返回全部列表
     * @param pageNum   --当前页码
     * @param pageSize  --每页显示数量
     * @return
     */
    @RequestMapping("/findByPage")
    public PageResult findByPage(int pageNum,int pageSize) {
        return brandService.findByPage(pageNum, pageSize);
    }

    /**
     * 增加品牌
     * @param brand
     * @return
     */
    @RequestMapping("/addBrand")
    public Result addBrand(@RequestBody TbBrand brand) {
        Result result = new Result(true, "增加成功");
        try {
            brandService.addBrand(brand);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
            result.setMessage("增加失败");
            return  result;
        }
    }

    @RequestMapping("/findOne")
    public TbBrand findOne(Long id) {
        return  null;
    }


}
