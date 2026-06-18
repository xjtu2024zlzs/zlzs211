package com.ruoyi.flowable.workflow.config;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteUserService;
import com.ruoyi.system.api.domain.SysDept;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.domain.SysUser;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Flowable ר�õ��û�Զ�̲�ѯ�ͻ��ˣ�������Ⱦ������ API �ӿڡ�
 */
@Component
public class FlowableRemoteUserClient {

    private final RemoteUserService remoteUserService;

    public FlowableRemoteUserClient(RemoteUserService remoteUserService) {
        this.remoteUserService = remoteUserService;
    }

    public R<SysUser> getUserById(Long userId) {
        return remoteUserService.getUserInfoByUserId(userId, SecurityConstants.INNER);
    }

    public R<SysRole> getRoleById(Long roleId) {
        return remoteUserService.getRoleByRoleId(roleId, SecurityConstants.INNER);
    }

    public R<SysDept> getDeptById(Long deptId) {
        return remoteUserService.getDeptByDeptId(deptId, SecurityConstants.INNER);
    }

    public R<List<SysUser>> listUsersByRoleIds(List<Long> roleIds) {
        return remoteUserService.listByRoleIds(roleIds, SecurityConstants.INNER);
    }

    public R<List<SysUser>> listUsersByDeptIds(List<Long> deptIds) {
        return remoteUserService.listByDeptIds(deptIds, SecurityConstants.INNER);
    }
}