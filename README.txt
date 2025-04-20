kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 2 \
  --topic sensor-stream

hadoop winutils 설치
      System.setProperty("hadoop.home.dir", "C:\\winutils\\hadoop-3.2.2\\bin");

local에서 데이터 추가시 => 나중에는 kafka로 변경 
Dataset<Row> kafkaStream = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "sensor-stream")
                .option("startingOffsets", "latest")
                .load();



pip install kafka-python
