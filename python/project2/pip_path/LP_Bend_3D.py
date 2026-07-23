import time
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
from matplotlib.patches import Patch
from matplotlib.lines import Line2D

DEFAULT_PIPE_COLORS = ["#e14b4b", "#2f80ed", "#24a148"]
DEFAULT_GRID_SHAPE = [12, 12, 8]
DEFAULT_BOUNDARY_WALLS = ["floor", "left", "back"]


def solve_pipe_routing_3d_with_bends(grid_shape, pipes, obstacles, bend_weight=2.0, plot=True, time_limit=None):
    import pulp

    """
    三维管线路径规划（整数规划）
    -------------------------------------------------------
    参数：
    grid_shape : (nx, ny, nz)
        三维网格尺寸
    pipes : [((x1,y1,z1), (x2,y2,z2)), ...]
        每条管路的起点和终点
    obstacles : [(x_min,y_min,z_min,x_max,y_max,z_max), ...]
        障碍物长方体，闭区间
    bend_weight : float
        弯角惩罚权重
    time_limit : int
        求解时间限制（秒）
    plot : bool
        是否绘制三维结果
    """

    if isinstance(grid_shape, int):
        grid_shape = (grid_shape, grid_shape, grid_shape)

    nx_dim, ny_dim, nz_dim = grid_shape

    print(f"\n开始建立三维模型 (网格: {nx_dim}x{ny_dim}x{nz_dim}, 转弯权重: {bend_weight})...")
    t0 = time.time()
    prob = pulp.LpProblem("PipeRouting_3D_Obstacles_And_Bends", pulp.LpMinimize)

    # -------------------------------------------------------
    # 1. 障碍物处理
    # -------------------------------------------------------
    obstacle_cells = set()
    for x_min, y_min, z_min, x_max, y_max, z_max in obstacles:
        for x in range(x_min, x_max + 1):
            for y in range(y_min, y_max + 1):
                for z in range(z_min, z_max + 1):
                    obstacle_cells.add((x, y, z))

    def in_bounds(u):
        x, y, z = u
        return 0 <= x < nx_dim and 0 <= y < ny_dim and 0 <= z < nz_dim

    # 检查起终点合法性
    for k, (start, end) in enumerate(pipes):
        if not in_bounds(start):
            raise ValueError(f"管路 {k} 的起点 {start} 超出网格范围")
        if not in_bounds(end):
            raise ValueError(f"管路 {k} 的终点 {end} 超出网格范围")
        if start in obstacle_cells:
            raise ValueError(f"管路 {k} 的起点 {start} 落在障碍物内")
        if end in obstacle_cells:
            raise ValueError(f"管路 {k} 的终点 {end} 落在障碍物内")

    # 所有可用节点
    nodes = [
        (x, y, z)
        for x in range(nx_dim)
        for y in range(ny_dim)
        for z in range(nz_dim)
        if (x, y, z) not in obstacle_cells
    ]

    # -------------------------------------------------------
    # 2. 构建有向边（6邻接）并记录边属于哪个轴
    #    axis = 0 -> x轴
    #    axis = 1 -> y轴
    #    axis = 2 -> z轴
    # -------------------------------------------------------
    directions = [
        (-1, 0, 0, 0), (1, 0, 0, 0),   # x 轴方向
        (0, -1, 0, 1), (0, 1, 0, 1),   # y 轴方向
        (0, 0, -1, 2), (0, 0, 1, 2)    # z 轴方向
    ]

    edges = []
    edge_axis = {}

    node_set = set(nodes)

    for u in nodes:
        x, y, z = u
        for dx, dy, dz, axis in directions:
            v = (x + dx, y + dy, z + dz)
            if v in node_set:
                edge = (u, v)
                edges.append(edge)
                edge_axis[edge] = axis

    pipe_ids = range(len(pipes))
    axes = range(3)

    # 为了加速按节点取入边/出边
    outgoing_edges = {u: [] for u in nodes}
    incoming_edges = {u: [] for u in nodes}

    for u, v in edges:
        outgoing_edges[u].append((u, v))
        incoming_edges[v].append((u, v))

    # -------------------------------------------------------
    # 3. 决策变量
    # x[k,u,v] : 管路k是否经过有向边 u->v
    # b[k,u]   : 管路k在节点u是否发生弯折
    # t[k,u]   : 管路k是否“穿过”中间节点u（内部节点使用标志）
    # s[k,u,a] : 管路k在节点u是否沿轴a直行
    # -------------------------------------------------------
    x = pulp.LpVariable.dicts(
        "x",
        ((k, u, v) for k in pipe_ids for (u, v) in edges),
        cat="Binary"
    )

    b = pulp.LpVariable.dicts(
        "b",
        ((k, u) for k in pipe_ids for u in nodes),
        cat="Binary"
    )

    t = pulp.LpVariable.dicts(
        "t",
        ((k, u) for k in pipe_ids for u in nodes),
        cat="Binary"
    )

    s = pulp.LpVariable.dicts(
        "s",
        ((k, u, a) for k in pipe_ids for u in nodes for a in axes),
        cat="Binary"
    )

    # -------------------------------------------------------
    # 4. 目标函数：总长度 + 转弯惩罚
    # -------------------------------------------------------
    total_length = pulp.lpSum(x.values())
    total_bends = pulp.lpSum(b.values())
    prob += total_length + bend_weight * total_bends

    # -------------------------------------------------------
    # 5. 流量守恒
    # -------------------------------------------------------
    for k, (start, end) in enumerate(pipes):
        for u in nodes:
            out_flow = pulp.lpSum(x[k, uu, vv] for (uu, vv) in outgoing_edges[u])
            in_flow = pulp.lpSum(x[k, uu, vv] for (uu, vv) in incoming_edges[u])

            if u == start:
                prob += out_flow - in_flow == 1
                prob += in_flow == 0
                prob += b[k, u] == 0
                prob += t[k, u] == 0
                for a in axes:
                    prob += s[k, u, a] == 0

            elif u == end:
                prob += out_flow - in_flow == -1
                prob += out_flow == 0
                prob += b[k, u] == 0
                prob += t[k, u] == 0
                for a in axes:
                    prob += s[k, u, a] == 0

            else:
                # 中间节点流守恒
                prob += out_flow - in_flow == 0

                # 是否被该管路作为中间节点使用
                prob += t[k, u] == in_flow
                prob += t[k, u] == out_flow

    # -------------------------------------------------------
    # 6. 节点排他性：任意节点最多只能被一条管路占用
    #    这里沿用你原模型的思路
    # -------------------------------------------------------
    for u in nodes:
        node_in_flow_all_pipes = pulp.lpSum(
            x[k, uu, vv]
            for k in pipe_ids
            for (uu, vv) in incoming_edges[u]
        )

        is_start_node = sum(1 for start, _ in pipes if start == u)

        prob += node_in_flow_all_pipes + is_start_node <= 1

    # -------------------------------------------------------
    # 7. 三维弯角检测
    #
    # 直行定义：
    #   在某个中间节点u，如果“进入u”和“离开u”都发生在同一个轴上，
    #   则视为直行；否则视为转弯
    #
    # b = t - sum_a s[a]
    # -------------------------------------------------------
    for k, (start, end) in enumerate(pipes):
        for u in nodes:
            if u == start or u == end:
                continue

            for a in axes:
                axis_in = pulp.lpSum(
                    x[k, uu, vv]
                    for (uu, vv) in incoming_edges[u]
                    if edge_axis[(uu, vv)] == a
                )
                axis_out = pulp.lpSum(
                    x[k, uu, vv]
                    for (uu, vv) in outgoing_edges[u]
                    if edge_axis[(uu, vv)] == a
                )

                # s[k,u,a] = 1 当且仅当该节点沿轴a直行
                prob += s[k, u, a] <= axis_in
                prob += s[k, u, a] <= axis_out
                prob += s[k, u, a] >= axis_in + axis_out - 1

            prob += b[k, u] == t[k, u] - pulp.lpSum(s[k, u, a] for a in axes)

    # -------------------------------------------------------
    # 8. 求解
    # -------------------------------------------------------
    print("模型构建完毕，正在调用求解器...")
    solver = pulp.PULP_CBC_CMD(msg=0, timeLimit=time_limit) if time_limit else pulp.PULP_CBC_CMD(msg=0)
    status = prob.solve(solver)

    elapsed = time.time() - t0
    print(f"求解耗时: {elapsed:.2f} 秒")

    if pulp.LpStatus[status] != "Optimal":
        print(f"❌ 未能找到最优可行解，状态: {pulp.LpStatus[status]}")
        return {
            "status": "FAILED",
            "solverStatus": pulp.LpStatus[status],
            "errorMessage": f"未能找到最优可行解，状态: {pulp.LpStatus[status]}",
            "elapsedSeconds": round(elapsed, 3),
        }

    length_val = int(pulp.value(total_length)) + len(pipes)  # 边数 + 起点格
    bends_val = int(pulp.value(total_bends))

    print("🎉 最优解达成！")
    print(f"   -> 总占据体素数: {length_val}")
    print(f"   -> 总计转弯次数: {bends_val}")

    # -------------------------------------------------------
    # 9. 提取每条管路的路径
    # -------------------------------------------------------
    active_edges_by_pipe = {k: [] for k in pipe_ids}
    for k in pipe_ids:
        for u, v in edges:
            val = pulp.value(x[k, u, v])
            if val is not None and val > 0.5:
                active_edges_by_pipe[k].append((u, v))

    def reconstruct_path(active_edges, start, end):
        """按有向边从 start 追到 end，恢复路径顺序"""
        succ = {u: v for u, v in active_edges}
        path = [start]
        cur = start
        visited = {start}

        while cur != end:
            if cur not in succ:
                break
            nxt = succ[cur]
            path.append(nxt)
            if nxt in visited and nxt != end:
                # 防止异常循环
                break
            visited.add(nxt)
            cur = nxt

        return path

    paths = {}
    for k, (start, end) in enumerate(pipes):
        paths[k] = reconstruct_path(active_edges_by_pipe[k], start, end)

    # -------------------------------------------------------
    # 10. 三维可视化
    # -------------------------------------------------------
    if plot:
        # filled[x, y, z] = 该体素是否显示
        filled = np.zeros((nx_dim, ny_dim, nz_dim), dtype=bool)
        facecolors = np.empty((nx_dim, ny_dim, nz_dim), dtype=object)

        # 先画障碍物
        for (x0, y0, z0) in obstacle_cells:
            filled[x0, y0, z0] = True
            facecolors[x0, y0, z0] = mcolors.to_rgba("black", alpha=0.85)

        # 再画每条管路路径
        colors = ['red', 'blue', 'green', 'orange', 'purple', 'cyan', 'magenta', 'brown']

        for k, path in paths.items():
            color = colors[k % len(colors)]
            rgba = mcolors.to_rgba(color, alpha=0.95)

            for (x0, y0, z0) in path:
                filled[x0, y0, z0] = True
                facecolors[x0, y0, z0] = rgba

        fig = plt.figure(figsize=(14, 10))
        ax = fig.add_subplot(111, projection='3d')

        # 体素绘制
        ax.voxels(
            filled,
            facecolors=facecolors,
            edgecolor='gray',
            linewidth=0.6,
            shade=False
        )

        # 图例句柄
        legend_handles = []

        # 障碍物图例
        legend_handles.append(
            Patch(facecolor='black', edgecolor='black', label='Obstacle')
        )

        # 起点、终点统一图例
        # legend_handles.append(
        #     Line2D([0], [0], marker='o', color='w',
        #            markerfacecolor='yellow', markeredgecolor='black',
        #            markersize=8, linestyle='None', label='Start / 起点')
        # )
        # legend_handles.append(
        #     Line2D([0], [0], marker='^', color='w',
        #            markerfacecolor='lime', markeredgecolor='black',
        #            markersize=8, linestyle='None', label='End / 终点')
        # )

        # 可选：叠加路径中心线、起点终点标记、文字
        for k, path in paths.items():
            if len(path) == 0:
                continue

            color = colors[k % len(colors)]
            xs = [p[0] + 0.5 for p in path]
            ys = [p[1] + 0.5 for p in path]
            zs = [p[2] + 0.5 for p in path]

            # 画路径中心线
            ax.plot(xs, ys, zs, color=color, linewidth=2.0)

            start, end = pipes[k]

            # 起点和终点单独打标记
            ax.scatter(start[0] + 0.5, start[1] + 0.5, start[2] + 0.5,
                       color='yellow', edgecolors='black', marker='o', s=60)

            ax.scatter(end[0] + 0.5, end[1] + 0.5, end[2] + 0.5,
                       color='lime', edgecolors='black', marker='^', s=70)

            # 起终点文字标签
            # ax.text(start[0] + 0.5, start[1] + 0.5, start[2] + 0.5,
            #         f'S{k}', color='black', ha='center', va='center',
            #         fontsize=9, weight='bold')
            #
            # ax.text(end[0] + 0.5, end[1] + 0.5, end[2] + 0.5,
            #         f'E{k}', color='black', ha='center', va='center',
            #         fontsize=9, weight='bold')

            # 每条管路增加一条图例，写出颜色和起终点
            legend_handles.append(
                Line2D([0], [0], color=color, lw=2,
                       label=f'Pipe {k}: S{start} → E{end}')
            )

        # 坐标轴按“顶点”标刻度
        ax.set_xlim(0, nx_dim)
        ax.set_ylim(0, ny_dim)
        ax.set_zlim(0, nz_dim)

        ax.set_xticks(np.arange(0, nx_dim + 1, 1))
        ax.set_yticks(np.arange(0, ny_dim + 1, 1))
        ax.set_zticks(np.arange(0, nz_dim + 1, 1))

        ax.set_xlabel("X")
        ax.set_ylabel("Y")
        ax.set_zlabel("Z")

        ax.set_title("3D Pipe Routing", fontsize=15)
        ax.set_box_aspect((nx_dim, ny_dim, nz_dim))
        # 图例往里收一点，避免超出画布
        ax.legend(handles=legend_handles,
                  loc='center left',
                  bbox_to_anchor=(0.98, 0.5),
                  fontsize=15,
                  frameon=True,
                  borderaxespad=0.6)

        # 手动给右侧图例预留空间，同时压缩标题与图之间的空隙
        fig.subplots_adjust(right=0.65, top=0.92)

        plt.show()



    return {
        "status": "SUCCESS",
        "solverStatus": pulp.LpStatus[status],
        "paths": paths,
        "lengthCells": length_val,
        "bendCount": bends_val,
        "elapsedSeconds": round(elapsed, 3),
    }


# ================= 测试运行 =================
def default_routing_payload():
    return {
        "algorithmKey": "lp_bend_3d",
        "algorithmCode": "lp_bend_3d",
        "algorithmSource": "project2_service",
        "algorithmName": "LP_Bend_3D 三维管线路径规划算法",
        "gridShape": list(DEFAULT_GRID_SHAPE),
        "gridUnitMm": 50.0,
        "bendWeight": 2.0,
        "solverMode": "milp",
        "timeLimitSeconds": 60,
        "pipeOuterDiameterMm": 9.53,
        "pipeInnerDiameterMm": 7.73,
        "bendRadiusMm": 20.0,
        "wallThicknessMm": 4.0,
        "boundaryWalls": list(DEFAULT_BOUNDARY_WALLS),
        "objectives": [],
        "constraints": [],
        "objectiveWeights": {},
        "constraintParams": {
            "minClearanceMm": 40.0,
            "minBendRadiusMm": 20.0,
            "clampSpacingMaxMm": 250.0,
            "serviceMarginMinMm": 30.0,
            "avoidForbiddenZones": True,
            "enforceBoundary": True,
            "endpointsFixed": True,
        },
        "droppedObjectiveConstraintCodes": [],
        "pipes": [
            {"name": "管路 1", "start": [0, 0, 0], "end": [11, 11, 7], "color": DEFAULT_PIPE_COLORS[0]},
            {"name": "管路 2", "start": [0, 1, 1], "end": [10, 2, 6], "color": DEFAULT_PIPE_COLORS[1]},
            {"name": "管路 3", "start": [2, 0, 7], "end": [9, 10, 0], "color": DEFAULT_PIPE_COLORS[2]},
        ],
        "obstacles": [
            {"name": "障碍物 1", "min": [3, 3, 1], "max": [5, 5, 4]},
            {"name": "障碍物 2", "min": [7, 2, 0], "max": [8, 8, 2]},
            {"name": "障碍物 3", "min": [1, 8, 2], "max": [4, 10, 6]},
        ],
    }


def solve_cable_routing(payload):
    started = time.time()
    cfg = normalize_platform_payload(payload or {})
    pipes_for_solver = [(tuple(item["start"]), tuple(item["end"])) for item in cfg["pipes"]]
    obstacles_for_solver = [tuple(item["min"] + item["max"]) for item in cfg["obstacles"]]

    raw = solve_pipe_routing_3d_with_bends(
        grid_shape=tuple(cfg["gridShape"]),
        pipes=pipes_for_solver,
        obstacles=obstacles_for_solver,
        bend_weight=cfg["bendWeight"],
        plot=False,
        time_limit=max(1, int(cfg["timeLimitSeconds"])),
    )
    if raw.get("status") != "SUCCESS":
        return {
            "status": "FAILED",
            "statusLabel": "求解失败",
            "solverName": "LP_Bend_3D MILP/PuLP/CBC",
            "errorMessage": raw.get("errorMessage", "LP_Bend_3D 未返回可行解。"),
            "params": cfg,
            "elapsedSeconds": round(time.time() - started, 3),
        }

    paths = []
    total_length = 0.0
    total_bends = 0
    for index, pipe in enumerate(cfg["pipes"]):
        points = [list(point) for point in raw["paths"].get(index, [])]
        length_cells = max(0, len(points) - 1)
        bend_count = count_bends(points)
        total_length += length_cells * cfg["gridUnitMm"]
        total_bends += bend_count
        paths.append({
            "pipeIndex": index,
            "name": pipe["name"],
            "color": pipe["color"],
            "start": pipe["start"],
            "end": pipe["end"],
            "points": points,
            "pointCount": len(points),
            "lengthCells": length_cells,
            "lengthMm": round(length_cells * cfg["gridUnitMm"], 3),
            "bendCount": bend_count,
        })

    return {
        "status": "SUCCESS",
        "statusLabel": "求解完成",
        "solverName": "LP_Bend_3D MILP/PuLP/CBC",
        "solverStatus": raw.get("solverStatus", "Optimal"),
        "algorithmKey": cfg["algorithmKey"],
        "algorithmCode": cfg["algorithmCode"],
        "algorithmSource": cfg["algorithmSource"],
        "algorithmName": cfg["algorithmName"],
        "gridShape": cfg["gridShape"],
        "gridUnitMm": cfg["gridUnitMm"],
        "bendWeight": cfg["bendWeight"],
        "pipeOuterDiameterMm": cfg["pipeOuterDiameterMm"],
        "pipeInnerDiameterMm": cfg["pipeInnerDiameterMm"],
        "bendRadiusMm": cfg["bendRadiusMm"],
        "objectives": cfg["objectives"],
        "constraints": cfg["constraints"],
        "objectiveWeights": cfg["objectiveWeights"],
        "constraintParams": cfg["constraintParams"],
        "constraintChecks": build_constraint_checks(cfg, paths),
        "droppedObjectiveConstraintCodes": cfg["droppedObjectiveConstraintCodes"],
        "pipes": cfg["pipes"],
        "obstacles": cfg["obstacles"],
        "boundaryWalls": cfg["boundaryWalls"],
        "paths": paths,
        "totals": {
            "pipeCount": len(paths),
            "lengthMm": round(total_length, 3),
            "lengthM": round(total_length / 1000.0, 4),
            "bendCount": total_bends,
        },
        "elapsedSeconds": round(time.time() - started, 3),
        "warnings": [],
        "errorMessage": "",
    }


def normalize_platform_payload(payload):
    defaults = default_routing_payload()
    data = {**defaults, **(payload or {})}
    grid_shape = normalize_grid_shape(data.get("gridShape", defaults["gridShape"]))
    pipes = normalize_pipes(data.get("pipes") or defaults["pipes"], grid_shape)
    obstacles = normalize_obstacles(data.get("obstacles") or defaults["obstacles"], grid_shape)
    algorithm_key = normalize_algorithm_key(data.get("algorithmKey") or data.get("algorithmCode"))
    constraint_params = normalize_constraint_params(data.get("constraintParams") or defaults["constraintParams"])
    bend_radius = max(
        float(data.get("bendRadiusMm") or defaults["bendRadiusMm"]),
        float(constraint_params.get("minBendRadiusMm") or 0),
    )
    return {
        "algorithmKey": algorithm_key,
        "algorithmCode": algorithm_key,
        "algorithmSource": "project2_service",
        "algorithmName": data.get("algorithmName") or defaults["algorithmName"],
        "gridShape": grid_shape,
        "gridUnitMm": float(data.get("gridUnitMm") or defaults["gridUnitMm"]),
        "bendWeight": float(data.get("bendWeight") or defaults["bendWeight"]),
        "solverMode": "milp",
        "timeLimitSeconds": int(float(data.get("timeLimitSeconds") or defaults["timeLimitSeconds"])),
        "pipeOuterDiameterMm": float(data.get("pipeOuterDiameterMm") or data.get("pipeDiameter") or defaults["pipeOuterDiameterMm"]),
        "pipeInnerDiameterMm": float(data.get("pipeInnerDiameterMm") or data.get("pipeInnerDiameter") or defaults["pipeInnerDiameterMm"]),
        "bendRadiusMm": bend_radius,
        "wallThicknessMm": float(data.get("wallThicknessMm") or defaults["wallThicknessMm"]),
        "boundaryWalls": list(data.get("boundaryWalls") or DEFAULT_BOUNDARY_WALLS),
        "objectives": normalize_workflow_items(data.get("objectives"), "objective"),
        "constraints": normalize_workflow_items(data.get("constraints"), "constraint"),
        "objectiveWeights": dict(data.get("objectiveWeights") or {}),
        "constraintParams": constraint_params,
        "droppedObjectiveConstraintCodes": list(data.get("droppedObjectiveConstraintCodes") or []),
        "pipes": pipes,
        "obstacles": obstacles,
    }


def normalize_algorithm_key(value):
    key = str(value or "lp_bend_3d").strip()
    if key in ("lp_bend_3d", "builtin_cable_router", "builtin", "default"):
        return "lp_bend_3d"
    return key


def normalize_constraint_params(value):
    defaults = default_routing_payload()["constraintParams"]
    data = {**defaults, **(value or {})}
    return {
        "minClearanceMm": float(data.get("minClearanceMm") or defaults["minClearanceMm"]),
        "minBendRadiusMm": float(data.get("minBendRadiusMm") or defaults["minBendRadiusMm"]),
        "clampSpacingMaxMm": float(data.get("clampSpacingMaxMm") or defaults["clampSpacingMaxMm"]),
        "serviceMarginMinMm": float(data.get("serviceMarginMinMm") or defaults["serviceMarginMinMm"]),
        "avoidForbiddenZones": bool(data.get("avoidForbiddenZones", True)),
        "enforceBoundary": bool(data.get("enforceBoundary", True)),
        "endpointsFixed": bool(data.get("endpointsFixed", True)),
    }


def normalize_workflow_items(value, item_type):
    result = []
    for item in value or []:
        if not isinstance(item, dict):
            continue
        result.append({
            "itemType": item.get("itemType") or item_type,
            "itemCode": str(item.get("itemCode") or ""),
            "itemName": str(item.get("itemName") or item.get("itemCode") or ""),
            "direction": str(item.get("direction") or ""),
            "weight": item.get("weight"),
            "limitValue": item.get("limitValue"),
            "numericValue": item.get("numericValue"),
            "unit": str(item.get("unit") or ""),
            "discipline": str(item.get("discipline") or ""),
            "sourceCodes": list(item.get("sourceCodes") or []),
        })
    return result


def build_constraint_checks(cfg, paths):
    checks = []
    constraints = cfg.get("constraints") or []
    params = cfg.get("constraintParams") or {}
    for item in constraints:
        code = item.get("itemCode")
        if code == "LAY_FORBIDDEN_ZONE_AVOID":
            checks.append(constraint_check(item, "PASS", "路径已避开障碍物网格。", "avoid", "no_intersection"))
        elif code == "LAY_PIPE_CLEARANCE_LIMIT":
            required = float(params.get("minClearanceMm") or 0)
            actual = float(cfg.get("gridUnitMm") or 0)
            checks.append(constraint_check(item, "PASS" if actual >= required else "WARN", required, actual))
        elif code == "LAY_CABLE_BEND_RADIUS_LIMIT":
            required = float(params.get("minBendRadiusMm") or 0)
            actual = float(cfg.get("bendRadiusMm") or 0)
            checks.append(constraint_check(item, "PASS" if actual >= required else "FAIL", required, actual))
        elif code == "LAY_CLAMP_SPACING_LIMIT":
            required = float(params.get("clampSpacingMaxMm") or 0)
            max_segment = max_straight_segment_length(paths, cfg["gridUnitMm"])
            checks.append(constraint_check(item, "PASS" if not required or max_segment <= required else "WARN", required, round(max_segment, 3)))
        elif code == "LAY_SERVICE_MARGIN_LIMIT":
            checks.append(constraint_check(item, "RESERVED", params.get("serviceMarginMinMm"), "待三维检修空间校核"))
        else:
            checks.append(constraint_check(item, "RESERVED", item.get("limitValue"), "随求解参数保留"))
    return checks


def constraint_check(item, status, *values):
    detail = ""
    if len(values) == 3:
        detail, required, actual = values
    elif len(values) == 2:
        required, actual = values
    else:
        raise ValueError(f"constraint_check expects 2 or 3 values after status, got {len(values)}")
    return {
        "itemCode": item.get("itemCode"),
        "itemName": item.get("itemName"),
        "name": item.get("itemName") or item.get("itemCode"),
        "status": status,
        "statusLabel": constraint_status_label(status),
        "required": required,
        "actual": actual,
        "requirement": required,
        "value": actual,
        "description": detail,
        "unit": item.get("unit") or "mm",
    }


def constraint_status_label(status):
    return {
        "PASS": "通过",
        "WARN": "警告",
        "FAIL": "失败",
        "RESERVED": "待复核",
    }.get(str(status or "").upper(), str(status or ""))


def max_straight_segment_length(paths, grid_unit):
    max_cells = 0
    for item in paths:
        points = item.get("points") or []
        if len(points) < 2:
            continue
        current_cells = 0
        last_direction = None
        for a, b in zip(points, points[1:]):
            direction = (b[0] - a[0], b[1] - a[1], b[2] - a[2])
            if last_direction is not None and direction != last_direction:
                max_cells = max(max_cells, current_cells)
                current_cells = 0
            current_cells += 1
            last_direction = direction
        max_cells = max(max_cells, current_cells)
    return max_cells * grid_unit


def normalize_grid_shape(value):
    raw = [value.get("x"), value.get("y"), value.get("z")] if isinstance(value, dict) else list(value or DEFAULT_GRID_SHAPE)
    if len(raw) < 3:
        raw = DEFAULT_GRID_SHAPE
    return [max(1, int(float(raw[index]))) for index in range(3)]


def normalize_pipes(value, grid_shape):
    result = []
    for index, item in enumerate(value or []):
        start = normalize_point(item.get("start"), grid_shape) if isinstance(item, dict) else normalize_point(item[0], grid_shape)
        end = normalize_point(item.get("end"), grid_shape) if isinstance(item, dict) else normalize_point(item[1], grid_shape)
        result.append({
            "name": str(item.get("name") or f"管路 {index + 1}") if isinstance(item, dict) else f"管路 {index + 1}",
            "start": start,
            "end": end,
            "color": str(item.get("color") or DEFAULT_PIPE_COLORS[index % len(DEFAULT_PIPE_COLORS)]) if isinstance(item, dict) else DEFAULT_PIPE_COLORS[index % len(DEFAULT_PIPE_COLORS)],
        })
    if not result:
        raise ValueError("至少需要设置 1 条管路。")
    return result


def normalize_obstacles(value, grid_shape):
    result = []
    for index, item in enumerate(value or []):
        if isinstance(item, dict):
            p_min = normalize_point(item.get("min"), grid_shape)
            p_max = normalize_point(item.get("max"), grid_shape)
            name = item.get("name") or f"障碍物 {index + 1}"
        else:
            p_min = normalize_point(item[:3], grid_shape)
            p_max = normalize_point(item[3:6], grid_shape)
            name = f"障碍物 {index + 1}"
        lower = [min(p_min[i], p_max[i]) for i in range(3)]
        upper = [max(p_min[i], p_max[i]) for i in range(3)]
        result.append({"name": str(name), "min": lower, "max": upper})
    return result


def normalize_point(value, grid_shape):
    raw = list(value or [0, 0, 0])
    if len(raw) < 3:
        raise ValueError("坐标点必须包含 x、y、z 三个值。")
    point = [int(float(raw[index])) for index in range(3)]
    return [min(max(point[index], 0), grid_shape[index] - 1) for index in range(3)]


def count_bends(points):
    bends = 0
    last_direction = None
    for a, b in zip(points, points[1:]):
        direction = (b[0] - a[0], b[1] - a[1], b[2] - a[2])
        if last_direction is not None and direction != last_direction:
            bends += 1
        last_direction = direction
    return bends


if __name__ == "__main__":
    sample_grid_shape = (12, 12, 8)
    sample_obstacles = [
        (3, 3, 1, 5, 5, 4),
        (7, 2, 0, 8, 8, 2),
        (1, 8, 2, 4, 10, 6),
    ]
    sample_pipes = [
        ((0, 0, 0), (11, 11, 7)),
        ((0, 1, 1), (10, 2, 6)),
        ((2, 0, 7), (9, 10, 0)),
    ]
    solve_pipe_routing_3d_with_bends(
        grid_shape=sample_grid_shape,
        pipes=sample_pipes,
        obstacles=sample_obstacles,
        bend_weight=2.0,
        plot=True,
    )
