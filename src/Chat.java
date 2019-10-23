import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 在线聊天室：服务器端
 *
 * @Author Nino 2019/10/23
 */
public class Chat {
    private static CopyOnWriteArrayList<Channel> all = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException{
        System.out.println("----Server----");
        ServerSocket server = new ServerSocket(8888);
        while (true) {
            Socket client = server.accept();
            System.out.println("一个用户已连接");
            Channel c = new Channel(client);
            // 管理所有的成员
            all.add(c);
            new Thread(c).start();
        }
    }

    static class Channel implements Runnable{
        private Socket client;
        private DataInputStream dis;
        private DataOutputStream dos;
        private boolean isRunning;
        private String name;

        public Channel(Socket client) {
            this.client = client;
            try {
                dis = new DataInputStream(client.getInputStream());
                dos = new DataOutputStream(client.getOutputStream());
                isRunning = true;
                name = receive();
                this.send("欢迎");
                this.sendOthers(name + "来到了聊天室",true);
            } catch (IOException e) {
                e.printStackTrace();
                release();
            }
        }

        // 接收消息
        public String receive() {
            String msg = "";
            try {
                msg = dis.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
                release();
            }
            return msg;
        }

        // 发送消息
        public void send(String msg) {
            try {
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
                release();
            }
        }

        /**
         * 群聊 发给其他人
         * 私聊 @xxx:
         * @param msg
         */
        public void sendOthers(String msg,boolean isSys) {
            boolean isPrivate = msg.startsWith("@");
            if (isPrivate) {
                int index = -1;
                if ((index = msg.indexOf(":")) == -1) {
                    index = msg.indexOf("：");
                }
                String targetName = msg.substring(1, index);
                msg = msg.substring(index + 1);
                for (Channel other : all) {
                    if (other.name.equals(targetName)) {
                        other.send(name + "悄悄地对你说：" + msg);
                    }
                }
            } else {
                for (Channel other : all) {
                    if (other == this) {
                        continue;
                    } else if (!isSys) { // 系统消息
                        other.send(name + ": " + msg);
                    } else { // 群聊消息
                        other.send(msg);
                    }
                }
            }

        }

        // 释放资源
        public void release() {
            isRunning = false;
            Client.Utils.close(dos, dis, client);
            all.remove(this);
            sendOthers(name + "离开了", true);
        }

        @Override
        public void run() {
            while (isRunning) {
                // 接收消息
                String msg = receive();
                if (!msg.equals("")) {
                    // 返回消息
                    sendOthers(msg,false);
                }
            }
        }

        public String getName() {
            return name;
        }
    }
}
