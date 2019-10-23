import java.io.*;
import java.net.Socket;

/**
 * 在线聊天室：客户端
 *
 * @Author Nino 2019/10/23
 */
public class Client {
    public static void main(String[] args) throws IOException {
        System.out.println("----Client----");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("请输入用户名：");
        String name = br.readLine();

        Socket client = new Socket("localhost", 8888);

        new Thread(new Send(client,name)).start();
        new Thread(new Receive(client)).start();

    }

    static class Utils {
        public static void close(Closeable... targets) {
            for (Closeable target : targets) {
                try {
                    if (null != target) {
                        target.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Receive implements Runnable {
        private DataInputStream dis;
        private Socket client;
        private boolean isRunning;

        public Receive(Socket client) {
            isRunning = true;
            this.client = client;
            try {
                dis = new DataInputStream(client.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                release();
            }
        }

        private void receive() {
            try {
                String msg = dis.readUTF();
                System.out.println(msg);
            } catch (IOException e) {
                e.printStackTrace();
                release();
            }
        }

        private void release() {
            isRunning = false;
            Utils.close(dis, client);
        }


        @Override
        public void run() {
            while (isRunning) {
                receive();
            }
        }
    }

    private static class Send implements Runnable {
        private BufferedReader console;
        private DataOutputStream dos;
        private Socket client;
        private boolean isRunning;
        private String name;

        public Send(Socket client, String name) {
            this.name = name;
            isRunning = true;
            this.client = client;
            console = new BufferedReader(new InputStreamReader(System.in));
            try {
                dos = new DataOutputStream(client.getOutputStream());
                // 发送名称
                send(name);
            } catch (IOException e) {
                e.printStackTrace();
                release();
            }
        }

        private void release() {
            isRunning = false;
            Utils.close(dos, client);
        }

        private String read() {
            String msg = "";
            try {
                msg = console.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                release();
            }
            return msg;
        }

        private void send(String msg) {
            try {
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
                release();
            }
        }

        @Override
        public void run() {
            while (isRunning) {
                String msg = read();
                if (!msg.equals("")) {
                    send(msg);
                }
            }
        }
    }
}
