//package com.happy.rocketmq;
//
//import org.apache.rocketmq.client.producer.DefaultMQProducer;
//import org.apache.rocketmq.client.producer.SendResult;
//import org.apache.rocketmq.common.message.Message;
//
//public class SimpleProducer {
//    public static void main(String[] args) throws Exception {
//        // 1. 创建生产者，指定生产者组名（不能用默认名）
//        DefaultMQProducer producer = new DefaultMQProducer("my_producer_group");
//
//        // 2. 设置 NameServer 地址
//        producer.setNamesrvAddr("127.0.0.1:9876");
//
//        // 3. 启动生产者
//        producer.start();
//
//        // 4. 发送消息
//        Message msg = new Message("MyTopic", "TagA", "Hello, RocketMQ!".getBytes());
//        SendResult result = producer.send(msg);
//
//        System.out.println("发送结果: " + result);
//
//        // 5. 关闭
//        producer.shutdown();
//    }
//}