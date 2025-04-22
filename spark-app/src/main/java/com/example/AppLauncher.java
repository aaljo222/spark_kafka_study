package com.example;

import org.apache.spark.sql.streaming.StreamingQueryException;

import java.util.concurrent.TimeoutException;

public class AppLauncher {
    public static void main(String[] args) throws StreamingQueryException, TimeoutException {
        System.out.println("🟢 AppLauncher   구동 중...");
        if (args.length > 0 && args[0].equalsIgnoreCase("consumer")) {
            JsonConsumer.main(new String[]{});
        } else if (args.length > 0 && args[0].equalsIgnoreCase("spark")) {
            StreamingCombineByKeyApp.main(new String[]{}); // ✅ Spark 진입점
        } else {
            ProducerMain.main(new String[]{}); // 기본값: producer
        }

    }
}
