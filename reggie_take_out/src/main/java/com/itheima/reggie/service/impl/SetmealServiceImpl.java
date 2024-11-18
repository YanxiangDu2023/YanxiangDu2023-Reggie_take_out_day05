package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    // 用来操作关联关系的

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            // 在 SetmealDish里的 private Long setmealId;
            return item;
        }).collect(Collectors.toList());


        //保存套餐和菜品的关联信息，操作setmeal_dish,执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    @Transactional
//    声明该方法是事务性的。
//    确保整个方法执行过程中所有操作（如删除套餐和关联菜品）要么全部成功，要么全部失败，保证数据一致性。

    public void removeWithDish(List<Long> ids) {
        //参数 ids：
        //这是一个 List<Long> 类型的集合，表示需要删除的套餐 ID 列表。
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        //查询套餐状态，确定是否可用删除

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();

//        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();

        queryWrapper.in(Setmeal::getId,ids);
        // 条件：套餐 ID 在指定列表中
        queryWrapper.or().eq(Setmeal::getStatus,1);
        // 条件：套餐状态为 1（售卖中）

        int count = this.count(queryWrapper); // 查询符合条件的套餐数量
        if(count > 0){
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }

//        如果查询结果显示存在正在售卖中的套餐，抛出自定义异常 CustomException。
//        通过异常通知调用方，删除操作无法完成，事务回滚。

        //如果可以删除，先删除套餐表中的数据---setmeal
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        //删除关系表中的数据----setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);

//        创建 LambdaQueryWrapper 条件对象：
//        查询 setmeal_dish 表中 setmeal_id 在 ids 列表中的记录。
//        调用 setmealDishService.remove(lambdaQueryWrapper)，批量删除这些关联记录。

    }
}
