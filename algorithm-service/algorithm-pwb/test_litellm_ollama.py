from litellm import completion

resp = completion(
    model="ollama/qwen2.5:32b",
    api_base="http://100.73.18.78:11434",
       messages=[{"role": "user", "content": "你好，你是谁？你是什么模型"}],
   )
print(resp.choices[0].message.content)