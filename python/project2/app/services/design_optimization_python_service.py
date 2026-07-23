"""
设计制造协同优化平台 - 多目标约束选择 Python 业务服务

本文件用于表达平台中“多目标约束选择、设计变量配置、冲突校验、
任务解耦、代理模型求解编排、线缆/管路路径约束映射”等核心逻辑。

【提交材料定位】
1. 该文件是 Java 版 DesignOptimizationService 中核心业务段的 Python
   等价实现，适合作为 Python 语言申请材料中的业务源程序。
2. 代码重点展示原创的业务规则、数据组织和求解编排，不包含若依框架
   登录、菜单、字典、网关等通用后台代码。
3. 每个分区均用中文注释说明意义与作用，便于软著补正时对应功能说明。
"""

from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime
from typing import Any, Callable
import copy
import json
import math


# ============================================================
# 第一部分：领域常量与编码映射
# 意义：把多学科优化中的目标、约束、设计变量统一编码，
#      保证不同专业工程师选择的条目可以被后续求解器识别。
# 作用：解决“同一工程含义在不同阶段或不同表中编码不一致”的问题。
# ============================================================

DISCIPLINES = ("structure", "layout", "aero", "hydraulic", "manufacturing")

DISCIPLINE_NAMES = {
    "structure": "结构",
    "layout": "布局",
    "aero": "气动",
    "hydraulic": "液压",
    "manufacturing": "制造",
    "design": "总体设计",
}

OBJECTIVE_ITEM_CANONICAL_CODES = {
    "LAY_PIPE_LENGTH": "LAY_CABLE_LENGTH_MIN",
    "LAY_LENGTH_MIN": "LAY_CABLE_LENGTH_MIN",
    "LAY_INTERFERENCE_MIN": "LAY_INTERFERENCE_RISK_MIN",
    "LAY_PIPE_CABLE_DISTANCE": "LAY_PIPE_CLEARANCE_LIMIT",
    "LAY_MIN_CLEARANCE": "LAY_CLEARANCE_LIMIT",
    "LAY_FORBIDDEN_ZONE": "LAY_FORBIDDEN_ZONE_AVOID",
    "LAY_CLAMP_INTERVAL": "LAY_CLAMP_SPACING_LIMIT",
    "LAY_BEND_RADIUS_LIMIT": "LAY_CABLE_BEND_RADIUS_LIMIT",
    "LAY_BEND_LIMIT": "LAY_CABLE_BEND_RADIUS_LIMIT",
    "LAY_MAINTAINABILITY": "LAY_MAINTAINABILITY_MAX",
    "LAY_SPACE_OCCUPANCY": "LAY_COMPACTNESS_MAX",
    "AERO_GAP_LIMIT": "AERO_DOOR_GAP_CLEARANCE",
    "AERO_MOTION_BOUNDARY": "AERO_DOOR_GAP_CLEARANCE",
    "AERO_DRAG_DISTURBANCE": "AERO_ENVELOPE_IMPACT_MIN",
    "HYD_PRESSURE_DROP": "HYD_PRESSURE_DROP_MIN",
    "HYD_BEND_RADIUS": "HYD_MIN_BEND_RADIUS",
}

PIPE_IMPACT_LAYOUT_RECOMMENDED_ITEMS = {
    "HYD_STRESS_MIN",
    "HYD_DEFORMATION_MIN",
    "HYD_STRESS_LIMIT",
    "HYD_DEFORMATION_LIMIT",
    "HYD_MIN_BEND_RADIUS",
    "LAY_INTERFERENCE_RISK_MIN",
    "LAY_CABLE_LENGTH_MIN",
    "LAY_MAINTAINABILITY_MAX",
    "LAY_PIPE_CLEARANCE_LIMIT",
    "LAY_FORBIDDEN_ZONE_AVOID",
    "LAY_CABLE_BEND_RADIUS_LIMIT",
    "LAY_CLAMP_SPACING_LIMIT",
    "LAY_SERVICE_MARGIN_LIMIT",
    "AERO_ENVELOPE_IMPACT_MIN",
    "AERO_OUTER_ENVELOPE",
    "MFG_PROCESS_COMPLEXITY_MIN",
    "MFG_ASSEMBLY_EFFICIENCY_MAX",
    "MFG_BEND_RADIUS_LIMIT",
    "MFG_CLAMP_INSTALLABLE",
    "MFG_TOOL_ACCESS",
}

CABLE_ROUTING_OBJECTIVE_CODES = {
    "LAY_CABLE_LENGTH_MIN",
    "LAY_INTERFERENCE_RISK_MIN",
    "LAY_COMPACTNESS_MAX",
    "LAY_MAINTAINABILITY_MAX",
    "AERO_ENVELOPE_IMPACT_MIN",
    "MFG_ASSEMBLY_EFFICIENCY_MAX",
    "MFG_MAINTENANCE_ACCESS_MAX",
}

CABLE_ROUTING_CONSTRAINT_CODES = {
    "LAY_PIPE_CLEARANCE_LIMIT",
    "LAY_FORBIDDEN_ZONE_AVOID",
    "LAY_DOOR_ENVELOPE_AVOID",
    "LAY_CABLE_BEND_RADIUS_LIMIT",
    "LAY_CLAMP_SPACING_LIMIT",
    "LAY_SERVICE_MARGIN_LIMIT",
    "AERO_OUTER_ENVELOPE",
    "AERO_DOOR_GAP_CLEARANCE",
    "MFG_CLAMP_INSTALLABLE",
    "MFG_TOOL_ACCESS",
    "STR_INTERFACE_FIXED",
    "LAY_PIPE_ENDPOINT_FIXED",
    "LAY_PIPE_HORIZONTAL_SPAN",
    "LAY_PIPE_VERTICAL_SPAN",
}

CABLE_ROUTING_ITEM_CANONICAL_CODES = {
    "LAY_MIN_CLEARANCE": "LAY_PIPE_CLEARANCE_LIMIT",
    "LAY_CLEARANCE_LIMIT": "LAY_PIPE_CLEARANCE_LIMIT",
    "STR_PIPE_CLEARANCE": "LAY_PIPE_CLEARANCE_LIMIT",
    "STR_FORBIDDEN_ZONE": "LAY_FORBIDDEN_ZONE_AVOID",
    "HYD_MIN_BEND_RADIUS": "LAY_CABLE_BEND_RADIUS_LIMIT",
    "HYD_BEND_RADIUS": "LAY_CABLE_BEND_RADIUS_LIMIT",
    "MFG_BEND_RADIUS_LIMIT": "LAY_CABLE_BEND_RADIUS_LIMIT",
}

SURROGATE_VARIABLE_KEY_MAP = {
    "PIPE_L1": "L1",
    "PIPE_L2": "L2",
    "PIPE_THETA_1": "theta1",
    "PIPE_THETA_2": "theta2",
    "PIPE_BEND_RADIUS": "R",
}


# ============================================================
# 第二部分：数据结构
# 意义：用 Python 数据类表达任务、目标约束、设计变量和求解任务。
# 作用：替代 Java 代码中的 Map/数据库行对象，使代码结构更清晰。
# ============================================================


@dataclass
class DesignTask:
    """协同优化任务的运行状态。"""

    task_id: int
    task_name: str
    task_type: str = "LANDING_GEAR_DOOR"
    current_node_key: str = "structure_select"
    status: str = "OBJECTIVE_SELECTING"
    owner_user: str = "admin"
    updated_at: str = field(default_factory=lambda: datetime.now().isoformat(timespec="seconds"))


@dataclass
class ObjectiveConstraintItem:
    """工程师选择的一条目标或约束。"""

    discipline: str
    item_type: str
    item_code: str
    item_name: str
    direction: str = ""
    weight: int = 5
    limit_value: Any = ""
    unit: str = ""
    rule_type: str = ""
    operator_code: str = ""
    target_field: str = ""
    reference_field: str = ""
    rule_expression: str = ""

    def to_dict(self) -> dict[str, Any]:
        return {
            "discipline": self.discipline,
            "disciplineName": DISCIPLINE_NAMES.get(self.discipline, self.discipline),
            "itemType": self.item_type,
            "itemCode": self.item_code,
            "itemName": self.item_name,
            "direction": self.direction,
            "weight": self.weight,
            "limitValue": self.limit_value,
            "unit": self.unit,
            "ruleType": self.rule_type,
            "operatorCode": self.operator_code,
            "targetField": self.target_field,
            "referenceField": self.reference_field,
            "ruleExpression": self.rule_expression,
        }


@dataclass
class DesignVariable:
    """任务解耦后由工程师选择的设计变量。"""

    discipline: str
    subtask_code: str
    variable_code: str
    variable_name: str
    variable_type: str = "continuous"
    initial_value: Any = ""
    lower_bound: Any = ""
    upper_bound: Any = ""
    step_value: Any = ""
    unit: str = ""

    def to_dict(self) -> dict[str, Any]:
        return {
            "discipline": self.discipline,
            "disciplineName": DISCIPLINE_NAMES.get(self.discipline, self.discipline),
            "subtaskCode": self.subtask_code,
            "variableCode": self.variable_code,
            "variableName": self.variable_name,
            "variableType": self.variable_type,
            "initialValue": self.initial_value,
            "lowerBound": self.lower_bound,
            "upperBound": self.upper_bound,
            "stepValue": self.step_value,
            "unit": self.unit,
        }


@dataclass
class SolveTask:
    """代理模型或路径规划的异步求解状态。"""

    status: str = "NOT_SUBMITTED"
    params: dict[str, Any] = field(default_factory=dict)
    result: dict[str, Any] = field(default_factory=dict)
    error_message: str = ""
    updated_at: str = field(default_factory=lambda: datetime.now().isoformat(timespec="seconds"))

    def to_dict(self, labeler: Callable[[str], str]) -> dict[str, Any]:
        return {
            "status": self.status,
            "statusLabel": labeler(self.status),
            "params": copy.deepcopy(self.params),
            "result": copy.deepcopy(self.result),
            "errorMessage": self.error_message,
            "updatedAt": self.updated_at,
        }


class MemoryOptimizationRepository:
    """
    内存版仓储。

    意义：在正式系统中这些数据来自 MySQL 表；在 Python 源程序材料中，
    用仓储对象表达同样的数据读写边界，便于展示完整业务逻辑。
    作用：保存任务、目标约束、设计变量、冲突检查和求解结果。
    """

    def __init__(self) -> None:
        self.tasks: dict[int, DesignTask] = {}
        self.objective_constraints: dict[int, list[ObjectiveConstraintItem]] = {}
        self.design_variables: dict[int, list[DesignVariable]] = {}
        self.conflict_checks: dict[int, dict[str, Any]] = {}
        self.decomposed_tasks: set[int] = set()
        self.surrogate_solves: dict[int, SolveTask] = {}
        self.cable_routing_solves: dict[int, SolveTask] = {}

    def get_or_create_task(self, task_id: int) -> DesignTask:
        if task_id not in self.tasks:
            self.tasks[task_id] = DesignTask(task_id=task_id, task_name=f"协同优化任务-{task_id}")
        return self.tasks[task_id]


# ============================================================
# 第三部分：目录构建函数
# 意义：定义可被各专业选择的目标、约束和设计变量目录。
# 作用：对应 Java 中 catalogByDiscipline、fallbackCatalog、
#      variableCatalogByDiscipline、fallbackVariableCatalog 等方法。
# ============================================================


def rule_item(
    item_type: str,
    code: str,
    name: str,
    direction: str,
    unit: str,
    rule_type: str = "",
    operator_code: str = "",
    threshold_value: Any = "",
    target_field: str = "",
    reference_field: str = "",
    rule_expression: str = "",
    sort_order: int = 0,
) -> dict[str, Any]:
    """生成一条目标/约束目录记录，并携带规则表达信息。"""

    return {
        "itemType": item_type,
        "itemCode": code,
        "itemName": name,
        "direction": direction,
        "unit": unit,
        "weight": 50,
        "limitValue": threshold_value,
        "thresholdValue": threshold_value,
        "ruleType": rule_type,
        "operatorCode": operator_code,
        "targetField": target_field,
        "referenceField": reference_field,
        "ruleExpression": rule_expression,
        "executeMode": "reserved",
        "sortOrder": sort_order,
    }


def variable_item(
    subtask_code: str,
    code: str,
    name: str,
    variable_type: str,
    default_value: Any,
    lower_bound: Any,
    upper_bound: Any,
    step_value: Any,
    unit: str,
) -> dict[str, Any]:
    """生成一条设计变量目录记录。"""

    return {
        "subtaskCode": subtask_code,
        "variableCode": code,
        "variableName": name,
        "variableType": variable_type,
        "defaultValue": default_value,
        "initialValue": default_value,
        "lowerBound": lower_bound,
        "upperBound": upper_bound,
        "stepValue": step_value,
        "unit": unit,
    }


def fallback_objective_catalog() -> dict[str, list[dict[str, Any]]]:
    """
    目标约束目录。

    意义：建立结构、布局、气动、液压、制造五类专业的目标约束库。
    作用：支持工程师按专业选择目标和约束，并为后续冲突检查/求解映射
    提供统一规则表达式。
    """

    return {
        "structure": [
            rule_item("objective", "STR_DEFORMATION_RISK_MIN", "管线变形碰撞风险最小", "min", "risk",
                      "objective_metric", "minimize", "", "deformationCollisionRisk", "",
                      "min(deformationCollisionRisk)", 10),
            rule_item("constraint", "STR_FORBIDDEN_ZONE", "结构禁布区域不可穿越", "avoid", "",
                      "geometry_intersection", "none_intersect", "", "routeGeometry",
                      "structureForbiddenZones", "intersect(routeGeometry, structureForbiddenZones)=false", 20),
            rule_item("constraint", "STR_INTERFACE_FIXED", "铰链、锁机构、作动器接口位置不可更改", "fixed", "",
                      "boundary_lock", "equal", "", "interfacePoints", "baselineInterfacePoints",
                      "interfacePoints=baselineInterfacePoints", 30),
            rule_item("constraint", "STR_CLAMP_SUPPORT_VALID", "卡箍支撑点结构强度满足要求", "meet", "",
                      "strength_check", "pass", "", "clampSupportStrength", "requiredSupportStrength",
                      "clampSupportStrength>=requiredSupportStrength", 40),
        ],
        "layout": [
            rule_item("objective", "LAY_INTERFERENCE_RISK_MIN", "线缆与液压管干涉风险最小", "min", "risk",
                      "objective_metric", "minimize", "", "interferenceRisk", "", "min(interferenceRisk)", 10),
            rule_item("objective", "LAY_CABLE_LENGTH_MIN", "线缆路径长度最小", "min", "m",
                      "objective_metric", "minimize", "", "cablePathLength", "", "min(cablePathLength)", 20),
            rule_item("objective", "LAY_MAINTAINABILITY_MAX", "维护可达性最大", "max", "score",
                      "objective_metric", "maximize", "", "maintainabilityScore", "",
                      "max(maintainabilityScore)", 30),
            rule_item("constraint", "LAY_PIPE_CLEARANCE_LIMIT", "线缆与液压管保持安全间距", ">=", "mm",
                      "clearance_check", "greater_equal", 40, "minCablePipeClearance",
                      "CABLE_PIPE_CLEARANCE", "minCablePipeClearance>=CABLE_PIPE_CLEARANCE", 40),
            rule_item("constraint", "LAY_FORBIDDEN_ZONE_AVOID", "线缆不得穿越结构禁布区域", "avoid", "",
                      "geometry_intersection", "none_intersect", "", "cableRouteGeometry",
                      "structureForbiddenZones", "intersect(cableRouteGeometry, structureForbiddenZones)=false", 50),
            rule_item("constraint", "LAY_CABLE_BEND_RADIUS_LIMIT", "线缆弯曲半径满足要求", ">=", "mm",
                      "radius_check", "greater_equal", 50, "actualCableBendRadius",
                      "CABLE_BEND_RADIUS", "actualCableBendRadius>=CABLE_BEND_RADIUS", 60),
            rule_item("constraint", "LAY_CLAMP_SPACING_LIMIT", "线夹间距不超过上限", "<=", "mm",
                      "spacing_check", "less_equal", 250, "actualClampSpacing",
                      "CLAMP_SPACING", "actualClampSpacing<=CLAMP_SPACING", 70),
            rule_item("constraint", "LAY_SERVICE_MARGIN_LIMIT", "检修空间满足要求", ">=", "mm",
                      "clearance_check", "greater_equal", 30, "actualServiceMargin",
                      "SERVICE_MARGIN", "actualServiceMargin>=SERVICE_MARGIN", 80),
        ],
        "aero": [
            rule_item("objective", "AERO_ENVELOPE_IMPACT_MIN", "对舱门外形包络影响最小", "min", "score",
                      "objective_metric", "minimize", "", "envelopeImpactScore", "",
                      "min(envelopeImpactScore)", 10),
            rule_item("constraint", "AERO_OUTER_ENVELOPE", "管线与附件不得超出舱门外形包络", "<=", "mm",
                      "envelope_check", "inside_or_equal", "", "routeEnvelope", "doorOuterEnvelope",
                      "routeEnvelope inside doorOuterEnvelope", 20),
            rule_item("constraint", "AERO_DOOR_GAP_CLEARANCE", "不影响舱门缝隙和开闭间隙", "meet", "",
                      "clearance_check", "pass", "", "doorGapStatus", "", "doorGapStatus=pass", 30),
        ],
        "hydraulic": [
            rule_item("objective", "HYD_STRESS_MIN", "最大等效应力最小", "min", "MPa",
                      "objective_metric", "minimize", "", "maxEquivalentStress", "",
                      "min(maxEquivalentStress)", 10),
            rule_item("objective", "HYD_DEFORMATION_MIN", "最大变形量最小", "min", "mm",
                      "objective_metric", "minimize", "", "maxDeformation", "",
                      "min(maxDeformation)", 20),
            rule_item("constraint", "HYD_STRESS_LIMIT", "最大等效应力不超过许用应力", "<=", "MPa",
                      "strength_check", "less_equal", "", "maxEquivalentStress", "allowableStress",
                      "maxEquivalentStress<=allowableStress", 30),
            rule_item("constraint", "HYD_DEFORMATION_LIMIT", "最大变形量不超过允许变形", "<=", "mm",
                      "deformation_check", "less_equal", "", "maxDeformation", "allowableDeformation",
                      "maxDeformation<=allowableDeformation", 40),
            rule_item("constraint", "HYD_MIN_BEND_RADIUS", "液压管弯曲半径不小于下限", ">=", "mm",
                      "radius_check", "greater_equal", 20, "PIPE_BEND_RADIUS", "minPipeBendRadius",
                      "PIPE_BEND_RADIUS>=minPipeBendRadius", 50),
        ],
        "manufacturing": [
            rule_item("objective", "MFG_PROCESS_COMPLEXITY_MIN", "管线制造加工复杂度最小", "min", "score",
                      "objective_metric", "minimize", "", "processComplexityScore", "",
                      "min(processComplexityScore)", 10),
            rule_item("objective", "MFG_ASSEMBLY_EFFICIENCY_MAX", "装配效率最大", "max", "score",
                      "objective_metric", "maximize", "", "assemblyEfficiencyScore", "",
                      "max(assemblyEfficiencyScore)", 20),
            rule_item("constraint", "MFG_BEND_RADIUS_LIMIT", "弯曲半径满足制造下限", ">=", "mm",
                      "radius_check", "greater_equal", 20, "bendRadius", "manufacturingMinBendRadius",
                      "bendRadius>=manufacturingMinBendRadius", 30),
            rule_item("constraint", "MFG_CLAMP_INSTALLABLE", "管夹/线夹可安装", "meet", "",
                      "installability_check", "pass", "", "clampInstallability", "",
                      "clampInstallability=pass", 40),
            rule_item("constraint", "MFG_TOOL_ACCESS", "工具操作空间满足要求", "meet", "",
                      "accessibility_check", "pass", "", "toolAccessStatus", "",
                      "toolAccessStatus=pass", 50),
        ],
    }


def fallback_variable_catalog() -> dict[str, list[dict[str, Any]]]:
    """
    设计变量目录。

    意义：把总体目标解耦后的变量分配到液压抗冲击和线缆管路布局子任务。
    作用：为代理模型和路径规划求解器提供变量边界、步长、单位。
    """

    return {
        "structure": [],
        "aero": [],
        "manufacturing": [],
        "layout": [
            variable_item("cable_pipe_layout", "CABLE_ROUTE_SIDE", "线缆布置侧别", "enum", "upper", "upper", "outer", "", ""),
            variable_item("cable_pipe_layout", "CABLE_PIPE_CLEARANCE", "线缆与液压管最小隔离距离", "continuous", 40, 20, 80, 1, "mm"),
            variable_item("cable_pipe_layout", "CABLE_OFFSET", "线缆相对液压管中心线偏移距离", "continuous", 60, 30, 120, 1, "mm"),
            variable_item("cable_pipe_layout", "CABLE_BEND_RADIUS", "线缆最小弯曲半径", "continuous", 50, 30, 120, 1, "mm"),
            variable_item("cable_pipe_layout", "CLAMP_SPACING", "线夹/管夹布置间距", "continuous", 180, 100, 250, 5, "mm"),
            variable_item("cable_pipe_layout", "SERVICE_MARGIN", "检修操作预留空间", "continuous", 40, 30, 100, 1, "mm"),
        ],
        "hydraulic": [
            variable_item("hydraulic_impact", "PIPE_L1", "L1 第一段直管长度", "continuous", 300, 50, 550, 1, "mm"),
            variable_item("hydraulic_impact", "PIPE_L2", "L2 第二段直管长度", "continuous", 150, 50, 300, 1, "mm"),
            variable_item("hydraulic_impact", "PIPE_BEND_RADIUS", "R 两处弯管圆角半径", "continuous", 30, 20, 80, 1, "mm"),
            variable_item("hydraulic_impact", "PIPE_THETA_1", "theta1 第一处弯曲角度", "continuous", 110, 30, 150, 1, "deg"),
            variable_item("hydraulic_impact", "PIPE_THETA_2", "theta2 第二处弯曲角度", "continuous", 120, 30, 150, 1, "deg"),
        ],
    }


# ============================================================
# 第四部分：多目标约束选择服务
# 意义：把工程师选择、规则归并、冲突检查、任务解耦、求解参数组织
#      放在同一个业务服务中。
# 作用：对应 Java DesignOptimizationService 的核心业务段。
# ============================================================


class DesignOptimizationPythonService:
    """多目标约束选择与协同求解 Python 服务。"""

    def __init__(self, repository: MemoryOptimizationRepository | None = None) -> None:
        self.repo = repository or MemoryOptimizationRepository()
        self.objective_catalog_data = fallback_objective_catalog()
        self.variable_catalog_data = fallback_variable_catalog()

    # -------------------- 4.1 目录查询 --------------------
    # 意义：向前端或工程师展示当前专业可选目标/约束/变量。
    # 作用：让目标约束选择有固定数据来源，而不是自由文本录入。

    def objective_catalog(self, discipline: str, task_type: str = "LANDING_GEAR_DOOR") -> list[dict[str, Any]]:
        rows = copy.deepcopy(self.objective_catalog_data.get(discipline, []))
        rows = self._deduplicate_objective_catalog(rows, discipline)
        profile = self._recommendation_profile(task_type)
        for row in rows:
            canonical = self._canonical_objective_code(row.get("itemCode"))
            recommended = profile == "PIPE_IMPACT_LAYOUT" and canonical in PIPE_IMPACT_LAYOUT_RECOMMENDED_ITEMS
            row["discipline"] = discipline
            row["disciplineName"] = DISCIPLINE_NAMES.get(discipline, discipline)
            row["recommended"] = recommended
            row["matchLevel"] = "recommended" if recommended else "optional"
            row["couplingGroups"] = self._coupling_groups(canonical)
            row["hasCouplingRisk"] = bool(row["couplingGroups"])
        return sorted(rows, key=lambda item: (not item["recommended"], item.get("sortOrder", 0), item.get("itemName", "")))

    def design_variable_catalog(self, discipline: str) -> list[dict[str, Any]]:
        rows = copy.deepcopy(self.variable_catalog_data.get(discipline, []))
        for row in rows:
            row["discipline"] = discipline
            row["disciplineName"] = DISCIPLINE_NAMES.get(discipline, discipline)
        return rows

    # -------------------- 4.2 选择保存 --------------------
    # 意义：记录各专业工程师选择的目标、约束、权重和设计变量。
    # 作用：形成后续冲突检查、任务解耦和求解器输入的业务依据。

    def save_objective_constraints(self, task_id: int, discipline: str, items: list[dict[str, Any]]) -> dict[str, Any]:
        task = self.repo.get_or_create_task(task_id)
        if self._discipline_for_node(task.current_node_key) not in (None, discipline):
            raise ValueError(f"当前节点只允许提交{self._discipline_for_node(task.current_node_key)}专业目标约束")
        preserved = [item for item in self.repo.objective_constraints.get(task_id, []) if item.discipline != discipline]
        for row in items:
            preserved.append(ObjectiveConstraintItem(
                discipline=discipline,
                item_type=str(row.get("itemType", "")),
                item_code=str(row.get("itemCode", "")),
                item_name=str(row.get("itemName", "")),
                direction=str(row.get("direction", "")),
                weight=self._objective_weight_value(row.get("weight", 5)),
                limit_value=row.get("limitValue", ""),
                unit=str(row.get("unit", "")),
                rule_type=str(row.get("ruleType", "")),
                operator_code=str(row.get("operatorCode", "")),
                target_field=str(row.get("targetField", "")),
                reference_field=str(row.get("referenceField", "")),
                rule_expression=str(row.get("ruleExpression", "")),
            ))
        self.repo.objective_constraints[task_id] = preserved
        task.updated_at = datetime.now().isoformat(timespec="seconds")
        return self.task_detail(task_id)

    def save_objective_weights(self, task_id: int, items: list[dict[str, Any]]) -> dict[str, Any]:
        selected = self.repo.objective_constraints.get(task_id, [])
        weight_map = {
            (str(row.get("discipline", "")), str(row.get("itemCode", ""))): self._objective_weight_value(row.get("weight"))
            for row in items
        }
        for item in selected:
            key = (item.discipline, item.item_code)
            if item.item_type == "objective" and key in weight_map:
                item.weight = weight_map[key]
        return self.task_detail(task_id)

    def save_design_variables(self, task_id: int, subtask_code: str, variables: list[dict[str, Any]]) -> dict[str, Any]:
        preserved = [
            item for item in self.repo.design_variables.get(task_id, [])
            if item.subtask_code != subtask_code
        ]
        for row in variables:
            preserved.append(DesignVariable(
                discipline=str(row.get("discipline", "")),
                subtask_code=subtask_code or str(row.get("subtaskCode", "")),
                variable_code=str(row.get("variableCode", "")),
                variable_name=str(row.get("variableName", "")),
                variable_type=str(row.get("variableType", "continuous")),
                initial_value=row.get("initialValue", row.get("defaultValue", "")),
                lower_bound=row.get("lowerBound", ""),
                upper_bound=row.get("upperBound", ""),
                step_value=row.get("stepValue", ""),
                unit=str(row.get("unit", "")),
            ))
        self.repo.design_variables[task_id] = preserved
        return self.task_detail(task_id)

    # -------------------- 4.3 冲突检查与任务解耦 --------------------
    # 意义：识别专业目标之间的典型耦合冲突，并把总任务拆成子任务。
    # 作用：保证“先选目标约束，再做模型解耦求解”的业务流程完整。

    def conflict_check(self, task_id: int, complete: bool = True) -> dict[str, Any]:
        selected = self._selected_items(task_id)
        conflicts = self._potential_conflicts(selected)
        passed = not any(item["severity"] == "high" for item in conflicts)
        result = {
            "status": "PASSED" if passed else "WARNING",
            "passed": passed,
            "checkedAt": datetime.now().isoformat(timespec="seconds"),
            "summary": "未发现高风险目标约束冲突" if passed else "发现需要协调的目标约束冲突",
            "potentialConflicts": conflicts,
        }
        self.repo.conflict_checks[task_id] = result
        if complete:
            task = self.repo.get_or_create_task(task_id)
            task.current_node_key = "model_decompose_solve"
            task.status = "SOLVING"
        return result

    def decompose(self, task_id: int) -> dict[str, Any]:
        self.repo.decomposed_tasks.add(task_id)
        return {
            "taskId": task_id,
            "subtasks": self.subtask_definitions(task_id),
            "message": "已按照目标约束归属生成液压抗冲击与线缆管路布局两个子任务",
        }

    def subtask_definitions(self, task_id: int) -> list[dict[str, Any]]:
        classified = self.classify_selected_items(task_id)
        return [
            {
                "subtaskCode": "hydraulic_impact",
                "subtaskName": "液压弯管抗冲击性能优化",
                "description": "围绕最大等效应力、最大变形和弯曲半径约束构建代理模型优化任务。",
                "objectives": [x for x in classified["hydraulic_impact"] if x["itemType"] == "objective"],
                "constraints": [x for x in classified["hydraulic_impact"] if x["itemType"] == "constraint"],
            },
            {
                "subtaskCode": "cable_pipe_layout",
                "subtaskName": "线缆管路布局设计",
                "description": "围绕路径长度、干涉风险、间距、弯曲半径、线夹间距和检修空间构建路径规划任务。",
                "objectives": [x for x in classified["cable_pipe_layout"] if x["itemType"] == "objective"],
                "constraints": [x for x in classified["cable_pipe_layout"] if x["itemType"] == "constraint"],
            },
        ]

    # -------------------- 4.4 代理模型求解编排 --------------------
    # 意义：把数据库/界面中的设计变量转换为 Kriging/代理模型求解器入参。
    # 作用：让“目标约束选择结果”真正驱动液压弯管最优设计求解。

    def build_surrogate_solve_payload(
        self,
        task_id: int,
        model_name: str = "aero_pipe_kriging.pkl",
        max_iterations: int = 80,
        population_size: int = 15,
        seed: int = 42,
        fault_pipe_parameters: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        bounds = self.selected_variable_bounds(task_id)
        self.validate_surrogate_bounds(bounds)
        fixed_params = self.surrogate_fixed_params(fault_pipe_parameters or {})
        payload = {
            "taskId": task_id,
            "modelName": model_name,
            "variables": bounds,
            "fixedParams": fixed_params,
            "algorithm": {
                "type": "differential_evolution",
                "maxIterations": max_iterations,
                "populationSize": population_size,
                "seed": seed,
            },
        }
        self.repo.surrogate_solves[task_id] = SolveTask(status="QUEUED", params=payload)
        return payload

    def selected_variable_bounds(self, task_id: int) -> dict[str, dict[str, float]]:
        bounds: dict[str, dict[str, float]] = {}
        for variable in self.repo.design_variables.get(task_id, []):
            key = SURROGATE_VARIABLE_KEY_MAP.get(variable.variable_code)
            if not key or key in bounds:
                continue
            bounds[key] = {
                "lower": self._float_value(variable.lower_bound, 0.0),
                "upper": self._float_value(variable.upper_bound, 0.0),
                "step": self._float_value(variable.step_value, 0.0),
            }
        return bounds

    def validate_surrogate_bounds(self, bounds: dict[str, dict[str, float]]) -> None:
        for key in ("L1", "L2", "theta1", "theta2", "R"):
            if key not in bounds:
                raise ValueError(f"缺少代理模型设计变量边界：{key}")
            if bounds[key]["upper"] <= bounds[key]["lower"]:
                raise ValueError(f"代理模型设计变量边界不合法：{key}")

    def surrogate_fixed_params(self, values: dict[str, Any]) -> dict[str, float]:
        return {
            "totalHorizontal": self._float_value(values.get("PIPE_HORIZONTAL_SPAN"), 600.0),
            "totalVertical": self._float_value(values.get("PIPE_VERTICAL_SPAN"), 300.0),
            "pipeDiameter": self._float_value(values.get("PIPE_OUTER_DIAMETER"), 9.53),
            "wallThickness": self._float_value(values.get("PIPE_WALL_THICKNESS"), 0.9),
            "allowableStress": self._float_value(values.get("ALLOWABLE_STRESS"), 207.0),
            "allowableDeformation": self._float_value(values.get("ALLOWABLE_DEFORMATION"), 5.0),
        }

    # -------------------- 4.5 线缆/管路路径规划编排 --------------------
    # 意义：将目标约束选择结果转换为 LP_Bend_3D 路径规划算法能够识别的参数。
    # 作用：把“线缆路径长度最小、避让禁布区域、保持安全间距、弯曲半径、
    #      线夹间距、检修空间”等约束真正映射为求解参数。

    def default_cable_routing_params(self) -> dict[str, Any]:
        return {
            "algorithmKey": "lp_bend_3d",
            "algorithmName": "LP_Bend_3D 三维管线路径规划算法",
            "gridShape": [12, 12, 8],
            "gridUnitMm": 50.0,
            "bendWeight": 2.0,
            "solverMode": "milp",
            "timeLimitSeconds": 60,
            "pipeOuterDiameterMm": 9.53,
            "pipeInnerDiameterMm": 7.73,
            "bendRadiusMm": 20.0,
            "boundaryWalls": ["floor", "left", "back"],
            "pipes": [
                {"name": "管路1", "start": [0, 0, 0], "end": [11, 11, 7], "color": "#e14b4b"},
                {"name": "管路2", "start": [0, 1, 1], "end": [10, 2, 6], "color": "#2f80ed"},
                {"name": "管路3", "start": [2, 0, 7], "end": [9, 10, 0], "color": "#24a148"},
            ],
            "obstacles": [
                {"name": "障碍物1", "min": [3, 3, 1], "max": [5, 5, 4]},
                {"name": "障碍物2", "min": [7, 2, 0], "max": [8, 8, 2]},
                {"name": "障碍物3", "min": [1, 8, 2], "max": [4, 10, 6]},
            ],
            "constraintParams": self.default_cable_routing_constraint_params(),
        }

    def default_cable_routing_constraint_params(self) -> dict[str, Any]:
        return {
            "minClearanceMm": 40.0,
            "minBendRadiusMm": 20.0,
            "clampSpacingMaxMm": 250.0,
            "serviceMarginMinMm": 30.0,
            "avoidForbiddenZones": True,
            "enforceBoundary": True,
            "endpointsFixed": True,
        }

    def build_cable_routing_payload(self, task_id: int, body: dict[str, Any] | None = None) -> dict[str, Any]:
        params = self.default_cable_routing_params()
        params.update({key: value for key, value in (body or {}).items() if value is not None})
        workflow = self.cable_routing_workflow_inputs(task_id, body or {})
        params["subtaskCode"] = "cable_pipe_layout"
        params["objectives"] = workflow["objectives"]
        params["constraints"] = workflow["constraints"]
        params["objectiveWeights"] = workflow["objectiveWeights"]
        params["droppedObjectiveConstraintCodes"] = workflow["droppedObjectiveConstraintCodes"]
        merged_constraints = self.default_cable_routing_constraint_params()
        merged_constraints.update(params.get("constraintParams") or {})
        merged_constraints.update(workflow["constraintParams"])
        params["constraintParams"] = merged_constraints
        params["bendRadiusMm"] = max(
            self._float_value(params.get("bendRadiusMm"), 20.0),
            self._float_value(merged_constraints.get("minBendRadiusMm"), 20.0),
        )
        self.repo.cable_routing_solves[task_id] = SolveTask(status="QUEUED", params=params)
        return params

    def cable_routing_workflow_inputs(self, task_id: int, body: dict[str, Any]) -> dict[str, Any]:
        source_items: list[dict[str, Any]] = []
        for item in body.get("objectives") or []:
            row = dict(item)
            row.setdefault("itemType", "objective")
            source_items.append(row)
        for item in body.get("constraints") or []:
            row = dict(item)
            row.setdefault("itemType", "constraint")
            source_items.append(row)
        if not source_items:
            source_items = self.classify_selected_items(task_id)["cable_pipe_layout"]

        objectives_by_code: dict[str, dict[str, Any]] = {}
        constraints_by_code: dict[str, dict[str, Any]] = {}
        dropped_codes: list[str] = []
        for item in source_items:
            original_code = str(item.get("itemCode", "")).strip().upper()
            canonical = self._canonical_cable_routing_code(original_code)
            is_objective = item.get("itemType") == "objective" and canonical in CABLE_ROUTING_OBJECTIVE_CODES
            is_constraint = item.get("itemType") == "constraint" and canonical in CABLE_ROUTING_CONSTRAINT_CODES
            if not is_objective and not is_constraint:
                if original_code:
                    dropped_codes.append(original_code)
                continue
            normalized = self._normalize_cable_routing_item(item, canonical, "objective" if is_objective else "constraint")
            target = objectives_by_code if is_objective else constraints_by_code
            if canonical in target:
                target[canonical] = self._merge_cable_routing_item(target[canonical], normalized)
            else:
                target[canonical] = normalized

        objectives = self._order_items(objectives_by_code.values(), CABLE_ROUTING_OBJECTIVE_CODES)
        constraints = self._order_items(constraints_by_code.values(), CABLE_ROUTING_CONSTRAINT_CODES)
        objective_weights = {item["itemCode"]: self._objective_weight_value(item.get("weight")) for item in objectives}
        return {
            "subtaskCode": "cable_pipe_layout",
            "objectives": objectives,
            "constraints": constraints,
            "objectiveWeights": objective_weights,
            "constraintParams": self.cable_routing_constraint_params(constraints),
            "droppedObjectiveConstraintCodes": sorted(set(dropped_codes)),
        }

    def cable_routing_constraint_params(self, constraints: list[dict[str, Any]]) -> dict[str, Any]:
        result = self.default_cable_routing_constraint_params()
        for item in constraints:
            code = item.get("itemCode")
            value = self._numeric_constraint_value(item)
            if code == "LAY_PIPE_CLEARANCE_LIMIT" and value is not None:
                result["minClearanceMm"] = max(result["minClearanceMm"], value)
            elif code == "LAY_CABLE_BEND_RADIUS_LIMIT" and value is not None:
                result["minBendRadiusMm"] = max(result["minBendRadiusMm"], value)
            elif code == "LAY_CLAMP_SPACING_LIMIT" and value is not None:
                result["clampSpacingMaxMm"] = min(result["clampSpacingMaxMm"], value)
            elif code == "LAY_SERVICE_MARGIN_LIMIT" and value is not None:
                result["serviceMarginMinMm"] = max(result["serviceMarginMinMm"], value)
            elif code in {"LAY_FORBIDDEN_ZONE_AVOID", "LAY_DOOR_ENVELOPE_AVOID"}:
                result["avoidForbiddenZones"] = True
            elif code in {"AERO_OUTER_ENVELOPE", "AERO_DOOR_GAP_CLEARANCE"}:
                result["enforceBoundary"] = True
            elif code in {"STR_INTERFACE_FIXED", "LAY_PIPE_ENDPOINT_FIXED"}:
                result["endpointsFixed"] = True
        return result

    # -------------------- 4.6 结果查询 --------------------
    # 意义：把任务、目标约束、设计变量和求解状态组织成前端可展示结构。
    # 作用：对应 Java detail、surrogateSolve、cableRoutingSolve 等查询方法。

    def task_detail(self, task_id: int) -> dict[str, Any]:
        task = self.repo.get_or_create_task(task_id)
        return {
            "task": task.__dict__,
            "objectiveConstraints": self.selected_objective_constraints(task_id),
            "designVariables": self.selected_design_variables(task_id),
            "conflictCheck": self.repo.conflict_checks.get(task_id, {}),
            "decomposed": task_id in self.repo.decomposed_tasks,
            "subtasks": self.subtask_definitions(task_id) if task_id in self.repo.decomposed_tasks else [],
            "surrogateSolve": self.repo.surrogate_solves.get(task_id, SolveTask()).to_dict(self._surrogate_status_label),
            "cableRoutingSolve": self.repo.cable_routing_solves.get(task_id, SolveTask()).to_dict(self._cable_routing_status_label),
        }

    def selected_objective_constraints(self, task_id: int) -> list[dict[str, Any]]:
        grouped: dict[str, list[dict[str, Any]]] = {}
        for item in self.repo.objective_constraints.get(task_id, []):
            grouped.setdefault(item.discipline, []).append(item.to_dict())
        return [
            {
                "discipline": discipline,
                "disciplineName": DISCIPLINE_NAMES.get(discipline, discipline),
                "items": items,
            }
            for discipline, items in grouped.items()
        ]

    def selected_design_variables(self, task_id: int) -> list[dict[str, Any]]:
        grouped: dict[str, list[dict[str, Any]]] = {}
        for item in self.repo.design_variables.get(task_id, []):
            grouped.setdefault(item.discipline, []).append(item.to_dict())
        return [
            {
                "discipline": discipline,
                "disciplineName": DISCIPLINE_NAMES.get(discipline, discipline),
                "items": items,
            }
            for discipline, items in grouped.items()
        ]

    # ========================================================
    # 第五部分：内部规则函数
    # 意义：沉淀系统原创规则，包括编码归一、权重归一、冲突识别、
    #      子任务分类、线缆路径参数合并等。
    # 作用：这些函数是软著材料中最能体现“多目标约束选择系统”特色的部分。
    # ========================================================

    def classify_selected_items(self, task_id: int) -> dict[str, list[dict[str, Any]]]:
        result = {"hydraulic_impact": [], "cable_pipe_layout": []}
        for item in self._selected_items(task_id):
            for subtask_code in self._subtask_codes(item.get("itemCode", "")):
                row = dict(item)
                row["subtaskCode"] = subtask_code
                result.setdefault(subtask_code, []).append(row)
        return result

    def _selected_items(self, task_id: int) -> list[dict[str, Any]]:
        rows = [item.to_dict() for item in self.repo.objective_constraints.get(task_id, [])]
        return self._deduplicate_objective_catalog(rows, "")

    def _potential_conflicts(self, selected: list[dict[str, Any]]) -> list[dict[str, Any]]:
        codes = {self._canonical_objective_code(item.get("itemCode")) for item in selected}
        conflicts = []
        if {"LAY_PIPE_CLEARANCE_LIMIT", "LAY_CABLE_LENGTH_MIN"} <= codes:
            conflicts.append(self._conflict(
                "LAYOUT_CLEARANCE_LENGTH",
                "线缆安全间距与路径长度存在耦合",
                "medium",
                "安全间距越大，路径可能变长；需要通过权重和约束阈值协调。",
            ))
        if {"LAY_SERVICE_MARGIN_LIMIT", "AERO_OUTER_ENVELOPE"} <= codes:
            conflicts.append(self._conflict(
                "SERVICE_MARGIN_AERO_ENVELOPE",
                "检修空间与气动外形包络存在耦合",
                "medium",
                "检修空间越大，越可能挤占舱门内部包络。",
            ))
        if {"HYD_MIN_BEND_RADIUS", "MFG_BEND_RADIUS_LIMIT", "LAY_CABLE_LENGTH_MIN"} & codes and "LAY_FORBIDDEN_ZONE_AVOID" in codes:
            conflicts.append(self._conflict(
                "BEND_RADIUS_FORBIDDEN_ZONE",
                "弯曲半径、禁布区域与路径长度存在耦合",
                "high",
                "弯曲半径下限和禁布区域同时收紧时，路径规划可行域会明显缩小。",
            ))
        return conflicts

    def _conflict(self, code: str, title: str, severity: str, suggestion: str) -> dict[str, Any]:
        return {"conflictCode": code, "title": title, "severity": severity, "suggestion": suggestion}

    def _subtask_codes(self, item_code: str) -> list[str]:
        code = self._canonical_objective_code(item_code)
        hydraulic = {"HYD_STRESS_MIN", "HYD_DEFORMATION_MIN", "HYD_STRESS_LIMIT", "HYD_DEFORMATION_LIMIT", "HYD_MIN_BEND_RADIUS"}
        cable_layout = CABLE_ROUTING_OBJECTIVE_CODES | CABLE_ROUTING_CONSTRAINT_CODES
        result = []
        if code in hydraulic:
            result.append("hydraulic_impact")
        if self._canonical_cable_routing_code(code) in cable_layout:
            result.append("cable_pipe_layout")
        return result or ["hydraulic_impact"]

    def _deduplicate_objective_catalog(self, rows: list[dict[str, Any]], default_discipline: str) -> list[dict[str, Any]]:
        selected: dict[tuple[str, str, str], dict[str, Any]] = {}
        for row in rows:
            discipline = str(row.get("discipline") or default_discipline)
            item_type = str(row.get("itemType", ""))
            canonical = self._canonical_objective_code(row.get("itemCode"))
            key = (discipline, item_type, canonical)
            row["itemCode"] = canonical
            if key not in selected or int(row.get("sortOrder") or 0) < int(selected[key].get("sortOrder") or 9999):
                selected[key] = row
        return list(selected.values())

    def _canonical_objective_code(self, item_code: Any) -> str:
        code = str(item_code or "").strip().upper()
        return OBJECTIVE_ITEM_CANONICAL_CODES.get(code, code)

    def _canonical_cable_routing_code(self, item_code: Any) -> str:
        code = self._canonical_objective_code(item_code)
        return CABLE_ROUTING_ITEM_CANONICAL_CODES.get(code, code)

    def _normalize_cable_routing_item(self, item: dict[str, Any], canonical_code: str, item_type: str) -> dict[str, Any]:
        numeric_value = self._numeric_constraint_value(item)
        source_codes = {str(item.get("itemCode", canonical_code)).strip().upper(), canonical_code}
        return {
            "itemType": item_type,
            "itemCode": canonical_code,
            "itemName": item.get("itemName", canonical_code),
            "direction": item.get("direction", ""),
            "weight": self._objective_weight_value(item.get("weight")),
            "limitValue": item.get("limitValue", item.get("thresholdValue", "")),
            "numericValue": numeric_value,
            "unit": item.get("unit", ""),
            "discipline": item.get("discipline", ""),
            "ruleType": item.get("ruleType", ""),
            "operatorCode": item.get("operatorCode", ""),
            "targetField": item.get("targetField", ""),
            "referenceField": item.get("referenceField", ""),
            "ruleExpression": item.get("ruleExpression", ""),
            "sourceCodes": sorted(source_codes),
        }

    def _merge_cable_routing_item(self, current: dict[str, Any], incoming: dict[str, Any]) -> dict[str, Any]:
        merged = dict(current)
        merged["sourceCodes"] = sorted(set(current.get("sourceCodes", [])) | set(incoming.get("sourceCodes", [])))
        if current.get("itemType") == "objective":
            merged["weight"] = max(self._objective_weight_value(current.get("weight")), self._objective_weight_value(incoming.get("weight")))
            return merged
        merged_value = self._merge_constraint_value(current.get("itemCode"), current.get("numericValue"), incoming.get("numericValue"))
        if merged_value is not None:
            merged["numericValue"] = merged_value
            merged["limitValue"] = merged_value
        return merged

    def _merge_constraint_value(self, item_code: Any, current: Any, incoming: Any) -> float | None:
        a = self._float_or_none(current)
        b = self._float_or_none(incoming)
        if a is None:
            return b
        if b is None:
            return a
        return min(a, b) if item_code == "LAY_CLAMP_SPACING_LIMIT" else max(a, b)

    def _numeric_constraint_value(self, item: dict[str, Any]) -> float | None:
        raw = item.get("numericValue", item.get("limitValue", item.get("thresholdValue", item.get("value"))))
        return self._float_or_none(raw)

    def _order_items(self, items: Any, ordered_codes: set[str]) -> list[dict[str, Any]]:
        order = list(ordered_codes)
        return sorted(
            list(items),
            key=lambda item: (order.index(item.get("itemCode")) if item.get("itemCode") in order else 999, item.get("itemName", "")),
        )

    def _objective_weight_value(self, value: Any) -> int:
        try:
            raw = int(float(value))
        except (TypeError, ValueError):
            raw = 5
        normalized = round(raw / 10) if raw > 10 else raw
        return max(0, min(10, normalized))

    def _recommendation_profile(self, task_type: str) -> str:
        text = str(task_type or "").upper()
        if any(key in text for key in ("LANDING_GEAR_DOOR", "HYDRAULIC_PIPE", "PIPE", "CABLE", "弯管", "管路", "线缆")):
            return "PIPE_IMPACT_LAYOUT"
        return ""

    def _coupling_groups(self, canonical_code: str) -> list[dict[str, str]]:
        groups = []
        if canonical_code in {"LAY_PIPE_CLEARANCE_LIMIT", "LAY_CLEARANCE_LIMIT", "LAY_CABLE_LENGTH_MIN"}:
            groups.append({"groupId": "LAYOUT_CLEARANCE_SPACE", "title": "安全间距与布局空间", "role": "safety_clearance"})
        if canonical_code in {"AERO_OUTER_ENVELOPE", "AERO_ENVELOPE_IMPACT_MIN", "AERO_DOOR_GAP_CLEARANCE"}:
            groups.append({"groupId": "AERO_LAYOUT_ENVELOPE", "title": "气动包络与管线布局", "role": "aero_envelope"})
        if canonical_code in {"HYD_STRESS_MIN", "HYD_STRESS_LIMIT", "HYD_DEFORMATION_MIN", "HYD_DEFORMATION_LIMIT"}:
            groups.append({"groupId": "HYDRAULIC_SPACE_STRENGTH", "title": "液压抗冲击与空间强度", "role": "impact_strength"})
        if canonical_code in {"MFG_BEND_RADIUS_LIMIT", "HYD_MIN_BEND_RADIUS", "LAY_CABLE_BEND_RADIUS_LIMIT"}:
            groups.append({"groupId": "BEND_RADIUS_PACKAGING", "title": "弯曲半径与路径包络", "role": "manufacturing_radius"})
        return groups

    def _discipline_for_node(self, node_key: str) -> str | None:
        return {
            "structure_select": "structure",
            "layout_select": "layout",
            "aero_select": "aero",
            "hydraulic_select": "hydraulic",
            "manufacturing_select": "manufacturing",
        }.get(node_key)

    def _surrogate_status_label(self, status: str) -> str:
        return {"NOT_SUBMITTED": "未提交", "QUEUED": "排队中", "RUNNING": "求解中", "SUCCESS": "求解成功", "FAILED": "求解失败"}.get(status, status)

    def _cable_routing_status_label(self, status: str) -> str:
        return {"NOT_SUBMITTED": "未提交", "QUEUED": "排队中", "RUNNING": "路径规划中", "SUCCESS": "规划成功", "FAILED": "规划失败"}.get(status, status)

    def _float_or_none(self, value: Any) -> float | None:
        if value is None or value == "":
            return None
        if isinstance(value, (int, float)) and math.isfinite(float(value)):
            return float(value)
        text = "".join(ch for ch in str(value) if ch.isdigit() or ch in ".-")
        if not text:
            return None
        try:
            return float(text)
        except ValueError:
            return None

    def _float_value(self, value: Any, fallback: float) -> float:
        parsed = self._float_or_none(value)
        return fallback if parsed is None else parsed


# ============================================================
# 第六部分：示例入口
# 意义：展示本服务如何从目标约束选择生成求解参数。
# 作用：便于代码审查人员理解业务闭环，也便于开发调试。
# ============================================================


def demo_workflow() -> dict[str, Any]:
    """演示目标约束选择到任务解耦和求解入参生成的完整流程。"""

    service = DesignOptimizationPythonService()
    task_id = 1001
    layout_items = [item for item in service.objective_catalog("layout") if item["recommended"]]
    hydraulic_items = [item for item in service.objective_catalog("hydraulic") if item["recommended"]]

    service.repo.get_or_create_task(task_id).current_node_key = "layout_select"
    service.save_objective_constraints(task_id, "layout", layout_items)
    service.repo.get_or_create_task(task_id).current_node_key = "hydraulic_select"
    service.save_objective_constraints(task_id, "hydraulic", hydraulic_items)

    service.conflict_check(task_id)
    service.decompose(task_id)
    service.save_design_variables(task_id, "hydraulic_impact", service.design_variable_catalog("hydraulic"))
    surrogate_payload = service.build_surrogate_solve_payload(task_id)
    cable_payload = service.build_cable_routing_payload(task_id)

    return {
        "taskDetail": service.task_detail(task_id),
        "surrogatePayload": surrogate_payload,
        "cableRoutingPayload": cable_payload,
    }


if __name__ == "__main__":
    print(json.dumps(demo_workflow(), ensure_ascii=False, indent=2))
