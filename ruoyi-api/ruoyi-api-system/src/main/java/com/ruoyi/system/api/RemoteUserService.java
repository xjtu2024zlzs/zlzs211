package com.ruoyi.system.api;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.domain.SysDept;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.factory.RemoteUserFallbackFactory;
import com.ruoyi.system.api.model.LoginUser;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 用户服务
 * 
 * @author ruoyi
 */
@FeignClient(contextId = "remoteUserService", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = RemoteUserFallbackFactory.class)
public interface RemoteUserService
{
    /**
     * 通过用户名查询用户信息
     *
     * @param username 用户名
     * @param source 请求来源
     * @return 结果
     */
    @GetMapping("/user/info/{username}")
    R<LoginUser> getUserInfo(@PathVariable("username") String username, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 通过用户编号查询用户信息
     *
     * @param userId 用户编号
     * @param source 请求来源
     * @return 结果
     */
    @GetMapping("/user/inner/{userId}")
    R<SysUser> getUserInfoByUserId(@PathVariable("userId") Long userId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 通过角色编号查询角色信息
     *
     * @param roleId 角色编号
     * @param source 请求来源
     * @return 结果
     */
    @GetMapping("/role/inner/{roleId}")
    R<SysRole> getRoleByRoleId(@PathVariable("roleId") Long roleId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 通过部门编号查询部门信息
     *
     * @param deptId 部门编号
     * @param source 请求来源
     * @return 结果
     */
    @GetMapping("/dept/inner/{deptId}")
    R<SysDept> getDeptByDeptId(@PathVariable("deptId") Long deptId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 根据角色编号集合查询用户列表
     *
     * @param roleIds 角色编号集合
     * @param source 请求来源
     * @return 结果
     */
    @PostMapping("/user/inner/listByRoleIds")
    R<List<SysUser>> listByRoleIds(@RequestBody List<Long> roleIds, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 根据部门编号集合查询用户列表
     *
     * @param deptIds 部门编号集合
     * @param source 请求来源
     * @return 结果
     */
    @PostMapping("/user/inner/listByDeptIds")
    R<List<SysUser>> listByDeptIds(@RequestBody List<Long> deptIds, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 注册用户信息
     *
     * @param sysUser 用户信息
     * @param source 请求来源
     * @return 结果
     */
    @PostMapping("/user/register")
    R<Boolean> registerUserInfo(@RequestBody SysUser sysUser, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 记录用户登录IP地址和登录时间
     *
     * @param sysUser 用户信息
     * @param source 请求来源
     * @return 结果
     */
    @PutMapping("/user/recordlogin")
    R<Boolean> recordUserLogin(@RequestBody SysUser sysUser, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
