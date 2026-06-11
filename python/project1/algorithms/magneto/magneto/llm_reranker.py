import os
import time
import warnings
import json_repair
from typing import Dict, List, Optional, Tuple
from litellm import completion

"""
LLMReranker 通过调用外部大模型，对检索阶段的候选列对重新打分，
以提高最终匹配精度。

支持三种提示词模式：
  1. 原始模式（MagnetoGPT）：列名 + 表名 + 样本值
  2. 图增强模式（MagnetoGPT_neo4j）：+ PK/FK 结构信息
  3. 语义边模式（MagnetoGPT_neo4j2 Pass2）：+ Confirmed correspondences
"""


class LLMReranker:
    """封装 LLM 调用、Prompt 构建与响应解析的逻辑。"""
    def __init__(self, llm_model="openai/gpt-4o-mini", **llm_model_kwargs):
        self.llm_model = llm_model
        self.api_base = llm_model_kwargs.pop(
            "api_base",
            os.getenv("MAGNETO_LLM_API_BASE"),
        )
        self.llm_model_kwargs = llm_model_kwargs
        self.llm_attempts = 5

    # ------------------------------------------------------------------
    # 全局 top-K 重排入口（跨表候选）
    # ------------------------------------------------------------------

    def rematch_global(
        self,
        src_col: str,
        candidates: List[Tuple[str, str, float]],
        source_values: Dict[str, list],
        all_target_values: Dict[str, Dict[str, list]],
        source_schema_context: Optional[Dict[str, str]] = None,
        target_schema_context: Optional[Dict[Tuple[str, str], str]] = None,
        semantic_edges: Optional[List[Dict]] = None,
    ) -> List[Tuple[str, str, float]]:
        """对单个源列的全局 top-K 候选做 LLM 重排。

        Args:
            src_col: 源列名
            candidates: [(tgt_table, tgt_col, emb_score), ...]
            source_values: {col: [sample_str, ...]}
            all_target_values: {tgt_table: {col: [sample_str, ...]}}
            source_schema_context: {src_col: "Table: ..., Role: ..."}
            target_schema_context: {(tgt_table, tgt_col): "Table: ..., Role: ..."}
            semantic_edges: [{"src_col", "src_table", "tgt_col", "tgt_table", "score"}]

        Returns:
            [(tgt_table, tgt_col, llm_score), ...] 重排后的候选
        """
        mode = "plain"
        if semantic_edges:
            mode = "semantic"
        elif source_schema_context is not None:
            mode = "graph"

        src_values_str = ",".join(source_values.get(src_col, []))

        if mode == "plain":
            cand_str = f"Column: {src_col}, Table: {source_schema_context['__table__'] if source_schema_context and '__table__' in source_schema_context else 'unknown'}, Sample values: [{src_values_str}]"
        else:
            src_ctx = source_schema_context.get(src_col, "") if source_schema_context else ""
            cand_str = f"Column: {src_col}, {src_ctx}, Sample values: [{src_values_str}]"

        # 处理 plain 模式下的 cand_str（使用 source_schema_context 中存的 __table__）
        if mode == "plain" and source_schema_context and "__table__" in source_schema_context:
            cand_str = f"Column: {src_col}, Table: {source_schema_context['__table__']}, Sample values: [{src_values_str}]"
        elif mode == "plain":
            cand_str = f"Column: {src_col}, Sample values: [{src_values_str}]"

        target_lines = []
        for tgt_table, tgt_col, _ in candidates:
            tgt_vals = all_target_values.get(tgt_table, {}).get(tgt_col, [])
            tgt_vals_str = ",".join(tgt_vals)
            if mode in ("graph", "semantic") and target_schema_context:
                tgt_ctx = target_schema_context.get((tgt_table, tgt_col), "")
                target_lines.append(
                    f"Column: {tgt_col}, {tgt_ctx}, Sample values: [{tgt_vals_str}]"
                )
            else:
                target_lines.append(
                    f"Column: {tgt_col}, Table: {tgt_table}, Sample values: [{tgt_vals_str}]"
                )
        targets_str = "\n".join(target_lines)

        attempts = 0
        while True:
            if attempts >= self.llm_attempts:
                warnings.warn(
                    f"Failed to parse response after {self.llm_attempts} attempts for {src_col}. Using embedding scores.",
                    UserWarning,
                )
                return candidates
            try:
                raw = self._get_matches_global(cand_str, targets_str, mode, semantic_edges)
            except Exception as api_err:
                warnings.warn(f"API error for {src_col}, retrying in 5s: {api_err}", UserWarning)
                time.sleep(5)
                try:
                    raw = self._get_matches_global(cand_str, targets_str, mode, semantic_edges)
                except Exception:
                    warnings.warn(f"Retry failed for {src_col}. Using embedding scores.", UserWarning)
                    return candidates
            parsed = self._parse_matches_global(raw, candidates)
            attempts += 1
            if parsed is not None:
                return parsed

    # ------------------------------------------------------------------
    # 提示词构建
    # ------------------------------------------------------------------

    def _get_prompt_plain(self, cand, targets):
        """MagnetoGPT：列名 + 表名 + 样本值，无结构信息。"""
        return (
            "Given a candidate column and a list of target columns, judge the similarity "
            "between the candidate and each target column. "
            "Return a JSON array of objects, each with 'column' (in the format table_name.column_name) "
            "and 'score' (a float between 0 and 1, two decimals, 1 is most similar). "
            "Do NOT provide any other output text or explanation. Only provide the JSON array.\n"
            "Example:\n"
            "Candidate Column: Column: EmployeeID, Table: hr_employees, Sample values: [100, 101, 102]\n"
            "Target Schemas:\n"
            "Column: WorkerID, Table: payroll_workers, Sample values: [100, 101, 102]\n"
            "Column: DeptCode, Table: payroll_departments, Sample values: [D01, D02, D03]\n"
            "Column: StaffName, Table: payroll_workers, Sample values: ['Alice', 'Bob', 'Charlie']\n"
            'Response: [\n  {"column": "payroll_workers.WorkerID", "score": 0.95},\n'
            '  {"column": "payroll_departments.DeptCode", "score": 0.10},\n'
            '  {"column": "payroll_workers.StaffName", "score": 0.05}\n]\n\n'
            "Candidate Column: " + cand +
            "\n\nTarget Schemas:\n" + targets +
            "\n\nResponse: "
        )

    def _get_prompt_graph(self, cand, targets):
        """MagnetoGPT_neo4j：+ PK/FK 结构信息。"""
        return (
            "Given a candidate column and a list of target columns, judge the similarity "
            "between the candidate and each target column. Each column includes its table name, "
            "structural role (primary key, foreign key), and related tables. Use both the column "
            "semantics and structural context to judge similarity. "
            "Return a JSON array of objects, each with 'column' (in the format table_name.column_name) "
            "and 'score' (a float between 0 and 1, two decimals, 1 is most similar). "
            "Do NOT provide any other output text or explanation. Only provide the JSON array.\n"
            "Example:\n"
            "Candidate Column: Column: EmployeeID, Table: hr_employees, Role: PRIMARY KEY, "
            "FK references: None, Referenced by: hr_salary.employee_id, Sample values: [100, 101, 102]\n"
            "Target Schemas:\n"
            "Column: WorkerID, Table: payroll_workers, Role: PRIMARY KEY, FK references: None, "
            "Referenced by: payroll_records.worker_id, Sample values: [100, 101, 102]\n"
            "Column: DeptCode, Table: payroll_departments, Role: None, FK references: None, "
            "Referenced by: None, Sample values: [D01, D02, D03]\n"
            'Response: [\n  {"column": "payroll_workers.WorkerID", "score": 0.95},\n'
            '  {"column": "payroll_departments.DeptCode", "score": 0.05}\n]\n\n'
            "Candidate Column: " + cand +
            "\n\nTarget Schemas:\n" + targets +
            "\n\nResponse: "
        )

    def _get_prompt_semantic(self, cand, targets, semantic_edges):
        """MagnetoGPT_neo4j2 Pass2：+ Confirmed correspondences。"""
        edge_lines = "; ".join(
            f"{e['src_col']} ({e['src_table']}) <-> {e['tgt_col']} ({e['tgt_table']}), confidence: {e['score']:.2f}"
            for e in semantic_edges
        )

        return (
            "Given a candidate column and a list of target columns, judge the similarity "
            "between the candidate and each target column. Each column includes its table name, "
            "structural role (primary key, foreign key), and related tables. You are also provided "
            "with confirmed column correspondences from a prior anchor column analysis - columns "
            "from confirmed corresponding tables are more likely to match. Use both the column "
            "semantics, structural context, and confirmed correspondences to judge similarity. "
            "Return a JSON array of objects, each with 'column' (in the format table_name.column_name) "
            "and 'score' (a float between 0 and 1, two decimals, 1 is most similar). "
            "Do NOT provide any other output text or explanation. Only provide the JSON array.\n"
            "Example:\n"
            "Confirmed correspondences: EmployeeID (hr_employees) <-> WorkerID (payroll_workers), confidence: 0.95\n"
            "Candidate Column: Column: salary_grade, Table: hr_employees, Role: None, "
            "FK references: None, Referenced by: None, Sample values: [A1, B2, C3]\n"
            "Target Schemas:\n"
            "Column: pay_level, Table: payroll_workers, Role: None, FK references: None, "
            "Referenced by: None, Sample values: [L1, L2, L3]\n"
            "Column: dept_name, Table: payroll_departments, Role: None, FK references: None, "
            "Referenced by: None, Sample values: [Sales, HR, IT]\n"
            'Response: [\n  {"column": "payroll_workers.pay_level", "score": 0.85},\n'
            '  {"column": "payroll_departments.dept_name", "score": 0.05}\n]\n\n'
            "Confirmed correspondences: " + edge_lines + "\n"
            "Candidate Column: " + cand +
            "\n\nTarget Schemas:\n" + targets +
            "\n\nResponse: "
        )

    # ------------------------------------------------------------------
    # LLM 调用
    # ------------------------------------------------------------------

    def _get_matches_global(self, cand, targets, mode, semantic_edges=None):
        if mode == "semantic" and semantic_edges:
            prompt = self._get_prompt_semantic(cand, targets, semantic_edges)
            system_msg = (
                "You are an AI trained to perform schema matching by providing column similarity scores. "
                "You also have access to database schema structural information (table names, primary keys, "
                "foreign key relationships) and confirmed column correspondences from prior analysis to help "
                "make more informed matching decisions."
            )
        elif mode == "graph":
            prompt = self._get_prompt_graph(cand, targets)
            system_msg = (
                "You are an AI trained to perform schema matching by providing column similarity scores. "
                "You also have access to database schema structural information (table names, primary keys, "
                "foreign key relationships) to help make more informed matching decisions."
            )
        else:
            prompt = self._get_prompt_plain(cand, targets)
            system_msg = "You are an AI trained to perform schema matching by providing column similarity scores."

        messages = [
            {"role": "system", "content": system_msg},
            {"role": "user", "content": prompt},
        ]
        kwargs = dict(self.llm_model_kwargs)
        if self.api_base:
            kwargs["api_base"] = self.api_base
        response = completion(model=self.llm_model, messages=messages, **kwargs)
        return response.choices[0].message.content

    # ------------------------------------------------------------------
    # 响应解析
    # ------------------------------------------------------------------

    def _parse_matches_global(self, raw_response, candidates):
        """解析 LLM 返回的 table.column 格式 JSON，映射回 (tgt_table, tgt_col, score)。"""
        try:
            matches_json = json_repair.loads(raw_response)
            cand_lookup = {(t, c): (t, c, s) for t, c, s in candidates}

            results = []
            for entry in matches_json:
                col_str = entry.get("column", "")
                score = float(entry.get("score", 0))

                if "." in col_str:
                    tgt_table, tgt_col = col_str.split(".", 1)
                else:
                    tgt_table, tgt_col = None, col_str

                if tgt_table and (tgt_table, tgt_col) in cand_lookup:
                    results.append((tgt_table, tgt_col, score))
                else:
                    for ct, cc, _ in candidates:
                        if cc == tgt_col:
                            results.append((ct, cc, score))
                            break

            if not results:
                return None
            return results

        except Exception as e:
            warnings.warn(
                f"Error parsing JSON response: {e}\nRaw: {raw_response}",
                UserWarning,
            )
            return None

    # ------------------------------------------------------------------
    # 旧接口保留（兼容现有 known_table 模式的按表对调用）
    # ------------------------------------------------------------------

    def rematch(
        self,
        source_table,
        target_table,
        source_values,
        target_values,
        matched_columns,
        score_based=True,
        source_schema_context: Optional[Dict[str, str]] = None,
        target_schema_context: Optional[Dict[str, str]] = None,
    ):
        """旧接口：按表对重排，兼容 known_table 模式。"""
        use_graph = source_schema_context is not None

        refined_matches = {}
        for source_col, target_col_scores in matched_columns.items():
            src_ctx = source_schema_context.get(source_col, "") if use_graph else ""
            values_str = ",".join(source_values[source_col])
            if use_graph and src_ctx:
                cand = f"Column: {source_col}, {src_ctx}, Sample values: [{values_str}]"
            else:
                cand = f"Column: {source_col}, Sample values: [{values_str}]"

            target_cols = []
            for target_col, _ in target_col_scores:
                tgt_ctx = target_schema_context.get(target_col, "") if use_graph else ""
                tgt_values = ",".join(target_values[target_col])
                if use_graph and tgt_ctx:
                    target_cols.append(
                        f"Column: {target_col}, {tgt_ctx}, Sample values: [{tgt_values}]"
                    )
                else:
                    target_cols.append(
                        f"Column: {target_col}, Sample values: [{tgt_values}]"
                    )
            targets = "\n".join(target_cols)

            attempts = 0
            while True:
                if attempts >= self.llm_attempts:
                    warnings.warn(
                        f"Failed to parse response after {self.llm_attempts} attempts. Skipping.",
                        UserWarning,
                    )
                    refined_match = [(tc, sc) for tc, sc in target_col_scores]
                    break
                raw = self._get_matches_legacy(cand, targets, use_graph)
                refined_match = self._parse_matches_legacy(raw)
                attempts += 1
                if refined_match is not None:
                    break
            refined_matches[source_col] = refined_match
        return refined_matches

    def _get_matches_legacy(self, cand, targets, use_graph):
        if use_graph:
            prompt = self._get_prompt_graph(cand, targets)
            system_msg = (
                "You are an AI trained to perform schema matching by providing column similarity scores. "
                "You also have access to database schema structural information (table names, primary keys, "
                "foreign key relationships) to help make more informed matching decisions."
            )
        else:
            prompt = (
                "Given a candidate column and a list of target columns, judge the similarity between the candidate and each target column. "
                "Return a JSON array of objects, each with 'column' (the target column name) and 'score' (a float between 0 and 1, two decimals, 1 is most similar). "
                "Do NOT provide any other output text or explanation. Only provide the JSON array.\n"
                "Example:\n"
                "Candidate Column: Column: EmployeeID, Sample values: [100, 101, 102]\n"
                "Target Schemas:\n"
                "Column: WorkerID, Sample values: [100, 101, 102]\n"
                "Column: EmpCode, Sample values: [001, 002, 003]\n"
                "Column: StaffName, Sample values: ['Alice', 'Bob', 'Charlie']\n"
                'Response: [\n  {"column": "WorkerID", "score": 0.95},\n  {"column": "EmpCode", "score": 0.30},\n  {"column": "StaffName", "score": 0.05}\n]\n\n'
                "Candidate Column: " + cand +
                "\n\nTarget Schemas:\n" + targets +
                "\n\nResponse: "
            )
            system_msg = "You are an AI trained to perform schema matching by providing column similarity scores."

        messages = [
            {"role": "system", "content": system_msg},
            {"role": "user", "content": prompt},
        ]
        kwargs = dict(self.llm_model_kwargs)
        if self.api_base:
            kwargs["api_base"] = self.api_base
        response = completion(model=self.llm_model, messages=messages, **kwargs)
        return response.choices[0].message.content

    def _parse_matches_legacy(self, raw_response):
        try:
            matches_json = json_repair.loads(raw_response)
            matched_columns = []
            for entry in matches_json:
                col_str = entry.get("column", "")
                if "." in col_str:
                    _, col_name = col_str.split(".", 1)
                else:
                    col_name = col_str
                score = float(entry.get("score", 0))
                matched_columns.append((col_name, score))
            return matched_columns
        except Exception as e:
            warnings.warn(
                f"Error parsing JSON response: {e}\nRaw: {raw_response}",
                UserWarning,
            )
            return None
