package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {
    //调用远程服务使用reference关联注解
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
    public PageResult<TbBrand> findByPage(int pageNum,int pageSize) {
        return brandService.findByPage(pageNum, pageSize);
    }

    /**
     * 根据id查询品牌信息
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public TbBrand findOne(Long id) {
        return brandService.findOne(id);
    }

    //***************************************除查询外，增加，删除，修改都需要捕捉异常并提示操作结果,返回值为Result********************
    /**
     * 增加品牌
     * @param brand     -- 品牌
     * @return
     */
    @RequestMapping("/addBrand")
    public Result addBrand(@RequestBody TbBrand brand) {
        try {
            brandService.addBrand(brand);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    /**
     * 更新品牌信息
     * @param brand
     * @return
     */
    @RequestMapping("/update")
    public Result upadate(@RequestBody TbBrand brand) {
        try {
            brandService.updateById(brand);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }

    /**
     * 根据id批量删除品牌信息
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            brandService.deleteByIds(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    /**
     *   分页查询返回全部列表
     * @param pageNum   --当前页码
     * @param pageSize  --每页显示数量
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbBrand brand,int pageNum,int pageSize) {
        return brandService.findByPage(brand,pageNum, pageSize);
    }



}
