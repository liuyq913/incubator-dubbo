/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.remoting;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Adaptive;
import org.apache.dubbo.common.extension.SPI;
import org.apache.dubbo.remoting.buffer.ChannelBuffer;

import java.io.IOException;

@SPI
public interface Codec2 {

    @Adaptive({Constants.CODEC_KEY}) //客户端发送下消息的时候，需要将请求对象按照一定的格式将对象编程成二进制流，以便于接受的时候可以解析成一个完整的信息
    void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException;

    @Adaptive({Constants.CODEC_KEY}) //从二进制流里面解码出请求信息
    Object decode(Channel channel, ChannelBuffer buffer) throws IOException;


    enum DecodeResult {
        //NEED_MORE_INPUT:在解码过程中如果收到的字节流不是一个完整包时，结束此次读事件处理，等待更多数据到达
        //SKIP_SOME_INPUT：忽略掉一部分输入数据
        NEED_MORE_INPUT, SKIP_SOME_INPUT
    }

}

