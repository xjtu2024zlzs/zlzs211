package com.ruoyi.system.python;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
public class PythonAlgorithmRunner {

    @Value("${project4.python-command}")
    private String pythonCommand;

    public void run(String scriptPath, String inputJsonPath, String outputJsonPath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonCommand,
                    scriptPath,
                    inputJsonPath,
                    outputJsonPath
            );

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder logBuilder = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    logBuilder.append(line).append(System.lineSeparator());
                }
            }

            boolean finished = process.waitFor(120, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Python算法执行超时");
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                throw new RuntimeException("Python算法执行失败，日志：" + logBuilder);
            }

            File outputFile = new File(outputJsonPath);
            if (!outputFile.exists()) {
                throw new RuntimeException("Python算法未生成输出文件：" + outputJsonPath + "，日志：" + logBuilder);
            }

        } catch (Exception e) {
            throw new RuntimeException("调用Python算法异常：" + e.getMessage(), e);
        }
    }
}