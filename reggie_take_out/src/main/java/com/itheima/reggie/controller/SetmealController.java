package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);

        setmealService.saveWithDish(setmealDto);
        // SetmealService接口———接口里的savewithdish方法——SetmealServiceImpl具体实现

        return R.success("新增" +
                "套餐成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name != null,Setmeal::getName,name);
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        // 把pageInfo copy到dtoPage上，把records忽略到，因为范型不一样

        // 这段代码的核心作用是将分页查询结果 pageInfo 中的 records 数据（List<Setmeal> 类型）转换为 List<SetmealDto> 类型，
        // 并为 SetmealDto 补充额外的信息（如分类名称 categoryName），最终将结果设置到 dtoPage 对象中。


        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {

            // 作用：使用 Java 的 Stream API 遍历 records 列表中的每个 Setmeal 对象 (item)，并将其转换为 SetmealDto 类型。
//            创建一个新的 SetmealDto 对象 (setmealDto)。
//            每次遍历时，对当前的 Setmeal 对象 (item) 进行处理。

            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item,setmealDto);
            //分类id

//            作用：将 item 中的字段值拷贝到 setmealDto 中。
//            工具：BeanUtils.copyProperties 是 Spring 提供的工具方法，可以快速将一个对象的字段值赋值给另一个对象。

            Long categoryId = item.getCategoryId();
            // 从 Setmeal 对象中获取 categoryId（分类的主键 ID）。
            //根据分类id查询分类对象

            Category category = categoryService.getById(categoryId);

            //调用 categoryService.getById(categoryId) 方法，根据分类 ID 查询对应的 Category 对象。


            if(category != null){
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            // 如果查询到分类信息（category 不为空），则获取分类的名称并设置到 setmealDto 中的 categoryName 字段。

            return setmealDto;
        }).collect(Collectors.toList());

        // 将 Stream 中的所有 SetmealDto 对象收集到一个 List<SetmealDto> 中。

        dtoPage.setRecords(list);
        return R.success(dtoPage);
        // 将转换后的 list 设置到 dtoPage 的 records 字段中。
//        dtoPage 现在包含：
//        分页的基本信息（如总记录数、当前页码、每页记录数等），这些信息从 pageInfo 中拷贝而来。
//        转换后的 records 列表，即 List<SetmealDto> 类型。
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        setmealService.removeWithDish(ids);

        return R.success("套餐数据删除成功");
    }


    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        log.info("setmeal:{}", setmeal);
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(setmeal.getName()), Setmeal::getName, setmeal.getName());
        queryWrapper.eq(null != setmeal.getCategoryId(), Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(null != setmeal.getStatus(), Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        return R.success(setmealService.list(queryWrapper));
    }
}
