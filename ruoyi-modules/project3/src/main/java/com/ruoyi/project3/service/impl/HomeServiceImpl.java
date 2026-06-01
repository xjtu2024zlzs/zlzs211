package com.ruoyi.project3.service.impl;

import com.ruoyi.project3.domain.HomeOverview;
import com.ruoyi.project3.domain.HomeSummary;
import com.ruoyi.project3.mapper.HomeMapper;
import com.ruoyi.project3.service.HomeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;


@Service
public class HomeServiceImpl implements HomeService {

    @Resource
    private HomeMapper homeMapper;

    @Override
    public HomeOverview get_home_ovw() {
        HomeOverview home_overview = new HomeOverview();
        HomeSummary summary = new HomeSummary();
        summary.set_air_count(homeMapper.cnt_aircraft());
        summary.set_sub_count(homeMapper.cnt_subsys());
        summary.set_dev_count(homeMapper.cnt_equip());
        summary.set_comp_count(homeMapper.cnt_comp());
        summary.set_part_cnt(homeMapper.cnt_part());

        home_overview.set_sum(summary);

        return home_overview;
    }
}

