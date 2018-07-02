package lesincs.june.websocket_demo

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import okio.ByteString

class MainActivity : AppCompatActivity() {

    lateinit var webSocket: WebSocket
    // 用于切换线程
    val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initview()
    }

    private fun initview() {
        btStartConnect.setOnClickListener {
            startConnect()
        }
    }

    private fun startConnect() {
        val okHttpClient = OkHttpClient()
        val request = Request.Builder()
                .url("ws://echo.websocket.org")
                .build()

        // 获取webSocket是一个异步过程 若还未连接，就使用webSocket发消息也不会报错，因为它只是将消息加入了消息队列中，然后循环读取发送
        webSocket = okHttpClient.newWebSocket(request, WebSocketListener())

        //发送普通消息
        if (webSocket.send("hello"))
            tvMessage.append("发送普通字符串 hello 成功\n")

        //发送二进制消息
        if (webSocket.send(ByteString.decodeHex("12")))
            tvMessage.append("发送二进制字符串 12 成功\n")

        webSocket.close(1000, "再见")

    }

    // 如果要在回调中刷新UI 需要回到主线程~
    inner class WebSocketListener : okhttp3.WebSocketListener() {

        // 远端接受连接后调用
        override fun onOpen(webSocket: WebSocket, response: Response?) {
            handler.post { tvMessage.append("建立连接成功!\n") }
        }

        // 连接失败的时候调用
        override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
            handler.post { tvMessage.append("建立连接失败!\n") }
        }

        // 在远端检测到没有要到来的消息时调用
        override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
            handler.post { tvMessage.append("即将关闭连接!\n") }
        }

        // 收到普通字符的时候调用
        override fun onMessage(webSocket: WebSocket?, text: String?) {
            handler.post { tvMessage.append("收到普通消息:" + " " + text + "\n") }

        }

        //收到二进制字符串的时候调用
        override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
            handler.post { tvMessage.append("收到二进制字符串:" + " " + bytes + "\n") }
        }

        // 连接关闭的时候调用
        override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
            handler.post { tvMessage.append("连接已关闭!\n") }
        }
    }
}

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
