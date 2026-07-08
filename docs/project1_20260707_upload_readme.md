# 课题五合并联调执行顺序

## 1. 合并课题一数字卷宗代码

先拉取或合并课题一分支，卷宗代码改动都在这个分支里：

```text
project1-dev-fzw
```

## 2. 确认基础数据补丁是否已经执行

基础数据补丁昨天已经随课题一分支提交：

```text
sql/project1/dossier/dossier_hyd_tube_mlg_32a_node_data_patch_mysql8.sql
```

如果课题五数据库昨天已经执行过这个补丁，本次不用重复执行。

如果课题五数据库还没有执行过，需要先执行一次。

## 3. 执行本次文件 URL、文件时间和挂接关系补丁

本次新增的数据补丁为：

```text
sql/project1/dossier/dossier_file_url_time_patch_20260707_mysql8.sql
```

该补丁包含：

```text
t1_file_asset              186 条
t1_file_relation           396 条
t1_dossier_content_item      1 条
```

## 4. 放置实际附件文件

将制造数据包 Excel 放到服务器文件目录：

```text
D:/ruoyi/data/dossier/files/2026/01/B-1234/nodes/HYD-TUBE-MLG-32A/MANUFACTURING_DATA_PACKAGE/HYD-TUBE-MLG-32A-MFG-PACK.xlsx
```

如果服务器修改过文件根目录配置，则按服务器实际配置目录放置。

## 5. 重启并验证

重启课题一后端服务。

如果前端重新打包，也同步更新前端。

最后打开数字卷宗详情页，检查主起液压弯管节点的附件材料是否能正常显示和下载。
