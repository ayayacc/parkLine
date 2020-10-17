package com.kl.parkLine.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.component.CompareUtil;
import com.kl.parkLine.dao.IMenuDao;
import com.kl.parkLine.entity.Menu;
import com.kl.parkLine.entity.QMenu;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.predicate.MenuPredicates;
import com.kl.parkLine.vo.MenuVo;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * @author chenc
 *
 */
@Service("menuService")
public class MenuService
{
    @Autowired
    private IMenuDao menuDao;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CompareUtil compareUtil;
    
    @Autowired
    private MenuPredicates menuPredicates;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    /**
     * 保存一个菜单
     * @param 被保存的菜单
     * @throws BusinessException 
     */
    @Transactional
    public void save(Menu menu) throws BusinessException
    {
        //编辑菜单，//合并字段
        Optional<Menu> menuDst = menuDao.findById(menu.getMenuId());
        
        if (false == menuDst.isPresent())
        {
            throw new BusinessException(String.format("无效的菜单 Id: %d", menu.getMenuId()));
        }
        
        //记录不同点
        BeanUtils.copyProperties(menu, menuDst.get(), compareUtil.getNullPropertyNames(menu));
        
        menu = menuDst.get();
        
        //保存数据
        menuDao.save(menu);
    }
    
    /**
     * 模糊匹配菜单
     * @param menu  
     * @param pageable
     * @param auth
     * @return
     */
    @Transactional(readOnly = true)
    public Page<MenuVo> fuzzyFindPage(MenuVo menuVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        Predicate searchPred = menuPredicates.fuzzy(menuVo, user);
        
        QMenu qMenu = QMenu.menu;
        QueryResults<Tuple> queryResults = jpaQueryFactory
                .select(
                        qMenu.menuId,
                        qMenu.name,
                        qMenu.enabled
                )
                .from(qMenu)
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        //转换成vo
        List<MenuVo> menuVos = queryResults
                .getResults()
                .stream()
                .map(tuple -> MenuVo.builder()
                        .menuId(tuple.get(qMenu.menuId))
                        .name(tuple.get(qMenu.name))
                        .build()
                        )
                .collect(Collectors.toList());
        return new PageImpl<>(menuVos, pageable, queryResults.getTotal());
    }
}
