159\.conda\envs\py310-magneto\lib\site-packages\litellm\main.py", line 1648, in completion
    response = base_llm_http_handler.completion(
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\llm_http_handler.py", line 493, in completion
    response = self._make_common_sync_call(
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\llm_http_handler.py", line 215, in _make_common_sync_call
    raise self._handle_error(e=e, provider_config=provider_config)
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\llm_http_handler.py", line 3517, in _handle_error
    raise provider_config.get_error_class(
litellm.llms.openai.common_utils.OpenAIError: {"error":{"message":"Internal Server Error","type":"internal_error","param":null,"code":"internal_error"}}

During handling of the above exception, another exception occurred:

Traceback (most recent call last):
  File "E:\Desktop\magneto-test-1\experiments\benchmarks\faa_benchmark_sf.py", line 1390, in run_benchmark_global_unknown_table
    matches = matcher.match_gpt_neo4j2(global_topk, source_table_pure, src_schema_name, src_df, all_target_dfs, dataset_name=ds_name)
  File "E:\Desktop\magneto-test-1\algorithms\magneto\magneto\global_matcher.py", line 329, in match_gpt_neo4j2
    results = self._reranker.rematch_global(
  File "E:\Desktop\magneto-test-1\algorithms\magneto\magneto\llm_reranker.py", line 100, in rematch_global
    raw = self._get_matches_global(cand_str, targets_str, mode, semantic_edges)
  File "E:\Desktop\magneto-test-1\algorithms\magneto\magneto\llm_reranker.py", line 222, in _get_matches_global
    response = completion(model=self.llm_model, messages=messages, **kwargs)
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\utils.py", line 1376, in wrapper
    raise e
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\utils.py", line 1245, in wrapper
    result = original_function(*args, **kwargs)
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\main.py", line 3767, in completion
    raise exception_type(
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\litellm_core_utils\exception_mapping_utils.py", line 2274, in exception_type
    raise e
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\litellm_core_utils\exception_mapping_utils.py", line 503, in exception_type
    raise InternalServerError(
litellm.exceptions.InternalServerError: litellm.InternalServerError: InternalServerError: DeepseekException - {"error":{"message":"Internal Server Error","type":"internal_error","param":null,"code":"internal_error"}}
MagnetoGPT_neo4j2:  38%|████████████████▋                           | 14/37 [31:23<25:44, 67.14s/table] 
Give Feedback / Get Help: https://github.com/BerriAI/litellm/issues/new
LiteLLM.Info: If you need to debug this error, use `litellm._turn_on_debug()'.


Provider List: https://docs.litellm.ai/docs/providers

  mes_production_batch_tracking - 错误: litellm.InternalServerError: InternalServerError: DeepseekException - {"error":{"message":"Internal Server Error","type":"internal_error","param":null,"code":"internal_error"}}
MagnetoGPT_neo4j2:  38%|████████████████▋                           | 14/37 [31:40<25:44, 67.14s/table]Traceback (most recent call last):
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\llm_http_handler.py", line 190, in _make_common_sync_call
    response = sync_httpx_client.post(
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\http_handler.py", line 898, in post
    raise e
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\http_handler.py", line 880, in post
    response.raise_for_status()
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\httpx\_models.py", line 829, in raise_for_status
    raise HTTPStatusError(message, request=request, response=self)
httpx.HTTPStatusError: Server error '500 Internal Server Error' for url 'https://api.deepseek.com/beta/chat/completions'
For more information check: https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/500

During handling of the above exception, another exception occurred:

Traceback (most recent call last):
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\main.py", line 1675, in completion
    raise e
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\main.py", line 1648, in completion
    response = base_llm_http_handler.completion(
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\llm_http_handler.py", line 493, in completion
    response = self._make_common_sync_call(
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\llm_http_handler.py", line 215, in _make_common_sync_call
    raise self._handle_error(e=e, provider_config=provider_config)
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\llm_http_handler.py", line 3517, in _handle_error
    raise provider_config.get_error_class(
litellm.llms.openai.common_utils.OpenAIError: {"error":{"message":"Internal Server Error","type":"internal_error","param":null,"code":"internal_error"}}

During handling of the above exception, another exception occurred:

Traceback (most recent call last):
  File "E:\Desktop\magneto-test-1\experiments\benchmarks\faa_benchmark_sf.py", line 1390, in run_benchmark_global_unknown_table
    matches = matcher.match_gpt_neo4j2(global_topk, source_table_pure, src_schema_name, src_df, all_target_dfs, dataset_name=ds_name)
  File "E:\Desktop\magneto-test-1\algorithms\magneto\magneto\global_matcher.py", line 329, in match_gpt_neo4j2
    results = self._reranker.rematch_global(
  File "E:\Desktop\magneto-test-1\algorithms\magneto\magneto\llm_reranker.py", line 100, in rematch_global
    raw = self._get_matches_global(cand_str, targets_str, mode, semantic_edges)
  File "E:\Desktop\magneto-test-1\algorithms\magneto\magneto\llm_reranker.py", line 222, in _get_matches_global
    response = completion(model=self.llm_model, messages=messages, **kwargs)
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\utils.py", line 1376, in wrapper
    raise e
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\utils.py", line 1245, in wrapper
    result = original_function(*args, **kwargs)
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\main.py", line 3767, in completion
    raise exception_type(
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\litellm_core_utils\exception_mapping_utils.py", line 2274, in exception_type
    raise e
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\litellm_core_utils\exception_mapping_utils.py", line 503, in exception_type
    raise InternalServerError(
litellm.exceptions.InternalServerError: litellm.InternalServerError: InternalServerError: DeepseekException - {"error":{"message":"Internal Server Error","type":"internal_error","param":null,"code":"internal_error"}}
MagnetoGPT_neo4j2:  41%|█████████████████▊                          | 15/37 [31:40<19:05, 52.09s/table] 
Give Feedback / Get Help: https://github.com/BerriAI/litellm/issues/new
LiteLLM.Info: If you need to debug this error, use `litellm._turn_on_debug()'.


Provider List: https://docs.litellm.ai/docs/providers

  mes_production_line_master - 错误: litellm.InternalServerError: InternalServerError: DeepseekException - {"error":{"message":"Internal Server Error","type":"internal_error","param":null,"code":"internal_error"}}
MagnetoGPT_neo4j2:  41%|█████████████████▊                          | 15/37 [31:41<19:05, 52.09s/table]Traceback (most recent call last):
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\llm_http_handler.py", line 190, in _make_common_sync_call
    response = sync_httpx_client.post(
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\http_handler.py", line 898, in post
    raise e
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\http_handler.py", line 880, in post
    response.raise_for_status()
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\httpx\_models.py", line 829, in raise_for_status
    raise HTTPStatusError(message, request=request, response=self)
httpx.HTTPStatusError: Server error '500 Internal Server Error' for url 'https://api.deepseek.com/beta/chat/completions'
For more information check: https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/500

During handling of the above exception, another exception occurred:

Traceback (most recent call last):
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\main.py", line 1675, in completion
    raise e
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\main.py", line 1648, in completion
    response = base_llm_http_handler.completion(
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\llm_http_handler.py", line 493, in completion
    response = self._make_common_sync_call(
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\llm_http_handler.py", line 215, in _make_common_sync_call
    raise self._handle_error(e=e, provider_config=provider_config)
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\llm_http_handler.py", line 3517, in _handle_error
    raise provider_config.get_error_class(
litellm.llms.openai.common_utils.OpenAIError: {"error":{"message":"Internal Server Error","type":"internal_error","param":null,"code":"internal_error"}}

During handling of the above exception, another exception occurred:

Traceback (most recent call last):
  File "E:\Desktop\magneto-test-1\experiments\benchmarks\faa_benchmark_sf.py", line 1390, in run_benchmark_global_unknown_table
    matches = matcher.match_gpt_neo4j2(global_topk, source_table_pure, src_schema_name, src_df, all_target_dfs, dataset_name=ds_name)
  File "E:\Desktop\magneto-test-1\algorithms\magneto\magneto\global_matcher.py", line 329, in match_gpt_neo4j2
    results = self._reranker.rematch_global(
  File "E:\Desktop\magneto-test-1\algorithms\magneto\magneto\llm_reranker.py", line 100, in rematch_global
    raw = self._get_matches_global(cand_str, targets_str, mode, semantic_edges)
  File "E:\Desktop\magneto-test-1\algorithms\magneto\magneto\llm_reranker.py", line 222, in _get_matches_global
    response = completion(model=self.llm_model, messages=messages, **kwargs)
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\utils.py", line 1376, in wrapper
    raise e
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\utils.py", line 1245, in wrapper
    result = original_function(*args, **kwargs)
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\main.py", line 3767, in completion
    raise exception_type(
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\litellm_core_utils\exception_mapping_utils.py", line 2274, in exception_type
    raise e
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\litellm_core_utils\exception_mapping_utils.py", line 503, in exception_type
    raise InternalServerError(
litellm.exceptions.InternalServerError: litellm.InternalServerError: InternalServerError: DeepseekException - {"error":{"message":"Internal Server Error","type":"internal_error","param":null,"code":"internal_error"}}
MagnetoGPT_neo4j2:  43%|███████████████████                         | 16/37 [31:41<12:49, 36.66s/table] 
Give Feedback / Get Help: https://github.com/BerriAI/litellm/issues/new
LiteLLM.Info: If you need to debug this error, use `litellm._turn_on_debug()'.


Provider List: https://docs.litellm.ai/docs/providers

  mes_production_work_order - 错误: litellm.InternalServerError: InternalServerError: DeepseekException - {"error":{"message":"Internal Server Error","type":"internal_error","param":null,"code":"internal_error"}}
MagnetoGPT_neo4j2:  43%|███████████████████                         | 16/37 [32:00<12:49, 36.66s/table]Traceback (most recent call last):
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\llm_http_handler.py", line 190, in _make_common_sync_call
    response = sync_httpx_client.post(
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\http_handler.py", line 898, in post
    raise e
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\http_handler.py", line 880, in post
    response.raise_for_status()
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\httpx\_models.py", line 829, in raise_for_status
    raise HTTPStatusError(message, request=request, response=self)
httpx.HTTPStatusError: Server error '500 Internal Server Error' for url 'https://api.deepseek.com/beta/chat/completions'
For more information check: https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/500

During handling of the above exception, another exception occurred:

Traceback (most recent call last):
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\main.py", line 1675, in completion
    raise e
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\main.py", line 1648, in completion
    response = base_llm_http_handler.completion(
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\llm_http_handler.py", line 493, in completion
    response = self._make_common_sync_call(
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\llm_http_handler.py", line 215, in _make_common_sync_call
    raise self._handle_error(e=e, provider_config=provider_config)
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\llms\custom_httpx\llm_http_handler.py", line 3517, in _handle_error
    raise provider_config.get_error_class(
litellm.llms.openai.common_utils.OpenAIError: {"error":{"message":"Internal Server Error","type":"internal_error","param":null,"code":"internal_error"}}

During handling of the above exception, another exception occurred:

Traceback (most recent call last):
  File "E:\Desktop\magneto-test-1\experiments\benchmarks\faa_benchmark_sf.py", line 1390, in run_benchmark_global_unknown_table
    matches = matcher.match_gpt_neo4j2(global_topk, source_table_pure, src_schema_name, src_df, all_target_dfs, dataset_name=ds_name)
  File "E:\Desktop\magneto-test-1\algorithms\magneto\magneto\global_matcher.py", line 329, in match_gpt_neo4j2
    results = self._reranker.rematch_global(
  File "E:\Desktop\magneto-test-1\algorithms\magneto\magneto\llm_reranker.py", line 100, in rematch_global
    raw = self._get_matches_global(cand_str, targets_str, mode, semantic_edges)
  File "E:\Desktop\magneto-test-1\algorithms\magneto\magneto\llm_reranker.py", line 222, in _get_matches_global
    response = completion(model=self.llm_model, messages=messages, **kwargs)
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\utils.py", line 1376, in wrapper
    raise e
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\utils.py", line 1245, in wrapper
    result = original_function(*args, **kwargs)
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\main.py", line 3767, in completion
    raise exception_type(
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\litellm_core_utils\exception_mapping_utils.py", line 2274, in exception_type
    raise e
  File "C:\Users\29159\.conda\envs\py310-magneto\lib\site-packages\litellm\litellm_core_utils\exception_mapping_utils.py", line 503, in exception_type
    raise InternalServerError(
litellm.exceptions.InternalServerError: litellm.InternalServerError: InternalServerError: DeepseekException - {"error":{"message":"Internal Server Error","type":"internal_error","param":null,"code":"internal_error"}}
MagnetoGPT_neo4j2: 100%|█████████████████████████████████████████| 37/37 [1:42:00<00:00, 165.42s/table] 

================================================================================
计算 MagnetoGPT_neo4j2 整体指标...
================================================================================

  总源表: 37
  有GT表对数: 35
  总GT匹配数: 170
  运行时间: 5988.44s
  MRR: 0.9065
  Recall@20: 0.8529
  All RecallAtGT: 0.8059
  One2One Recall: 0.7529

  ✓ MagnetoGPT_neo4j2 完成！


es_default-deepseek_deepseek-chat-global_unknown\faa_fkp-mpnet-header_values_default-deepseek_deepseek-chat-global_unknown.csv
(py310-magneto) PS E:\Desktop\magneto-test-1>