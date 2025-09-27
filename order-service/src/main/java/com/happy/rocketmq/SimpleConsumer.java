//package com.happy.rocketmq;
//
//import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
//import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
//import org.apache.rocketmq.common.message.MessageExt;
//
//import java.util.List;
//
//public class SimpleConsumer {
//    public static void main(String[] args) throws Exception {
//        // 1. 创建消费者，指定消费者组名
//        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("my_consumer_group");
//
//        // 2. 设置 NameServer
//        consumer.setNamesrvAddr("127.0.0.1:9876");
//
//        // 3. 订阅 Topic
//        consumer.subscribe("MyTopic", "*"); // * 表示订阅所有 Tag
//
//        // 4. 注册监听器
//        consumer.registerMessageListener(new MessageListenerConcurrently() {
//            @Override
//            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
//                                                            ConsumeConcurrentlyContext context) {
//                for (MessageExt msg : msgs) {
//                    try {
//                        // 将字节数组转换为字符串，并指定字符集（例如 UTF-8）
//                        String messageBody = new String(msg.getBody(), "UTF-8");
//                        System.out.println("收到消息: " + messageBody);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
//                    }
//                }
//                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//            }
//        });
//
//        // 5. 启动
//        consumer.start();
//        System.out.println("消费者已启动...");
//    }
//}