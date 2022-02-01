package org.jetbrains.plugins.dubbo.requests

import com.caucho.hessian.io.Hessian2Output
import com.caucho.hessian.io.HessianSerializerOutput
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class DubboRpcInvocation(
    private val serviceName: String,
    private val serviceVersion: String,
    private val methodName: String,
    paramsTypeArray: Array<String>,
    arguments: Array<Any>
) {
    private val methodStub = "\$invoke"
    private val parameterTypesDesc = "Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/Object;"
    private val stubArguments = arrayOfNulls<Any>(3)
    private val attachments: MutableMap<String, String> = HashMap()

    init {
        stubArguments[0] = methodName
        stubArguments[1] = paramsTypeArray // method signature
        stubArguments[2] = arguments // real arguments
        attachments["path"] = serviceName
        attachments["remote.application"] = "JetBrainsConsumerTest"
        attachments["interface"] = serviceName
        attachments["version"] = serviceVersion
        attachments["generic"] = "gson"
    }

    @Throws(Exception::class)
    fun toBytes(): ByteArray {
        val bos = ByteArrayOutputStream()
        val out: Hessian2Output = HessianSerializerOutput(bos)
        out.writeString("2.0.2")
        out.writeString(serviceName)
        out.writeString("0.0.0")
        out.writeString(methodStub)
        out.writeString(parameterTypesDesc)
        for (argument in stubArguments) {
            out.writeObject(argument)
        }
        out.writeObject(attachments)
        out.flush()
        return bos.toByteArray()
    }

    fun frameHeaderBytes(messageId: Long, length: Int): ByteArray {
        //2byte magic:类似java字节码文件里的魔数，用来判断是不是dubbo协议的数据包。魔数是常量0xdabb
        //1byte 消息标志位:16-20序列id,21 event,22 two way,23请求或响应标识
        // 1byte 状态，当消息类型为响应时，设置响应状态。24-31位。状态位, 设置请求响应状态，dubbo定义了一些响应的类型。具体类型见com.alibaba.dubbo.remoting.exchange.Response
        //8byte 消息ID,long类型，32-95位。每一个请求的唯一识别id（由于采用异步通讯的方式，用来把请求request和返回的response对应上）
        //4byte 消息长度，96-127位。消息体 body 长度, int 类型，即记录Body Content有多少个字节。
        val bb = ByteBuffer.allocate(16)
        bb.putShort(0xDABB.toShort()) //magic
        bb.put(0xC2.toByte()) //mark
        bb.put(0x00.toByte()) //status
        bb.putLong(messageId)
        bb.putInt(length)
        return bb.array()
    }
}