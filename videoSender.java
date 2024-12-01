import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class VideoSender {
    public static void main(String[] args) {
        String videoPath = "path/to/video.mp4"; // Đường dẫn đến video
        String receiverAddress = "192.168.1.2"; // Địa chỉ IP của máy nhận
        int receiverPort = 5000; // Cổng máy nhận
        int packetSize = 1400; // Kích thước gói UDP

        try (DatagramSocket socket = new DatagramSocket();
             FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();

            InetAddress receiverInetAddress = InetAddress.getByName(receiverAddress);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            int frameNumber = 0;

            while (true) {
                AVPacket pkt = grabber.grabPacket();
                if (pkt == null) break;

                byte[] data = new byte[pkt.size()];
                pkt.data().get(data);

                for (int i = 0; i < data.length; i += packetSize) {
                    int length = Math.min(packetSize, data.length - i);
                    byte[] packetData = new byte[length + 8]; // 8 byte cho header
                    System.arraycopy(data, i, packetData, 8, length);

                    dos.writeInt(frameNumber);
                    dos.writeInt(i);
                    System.arraycopy(baos.toByteArray(), 0, packetData, 0, 8);

                    DatagramPacket packet = new DatagramPacket(packetData, packetData.length, receiverInetAddress, receiverPort);
                    socket.send(packet);
                    baos.reset();
                }

                frameNumber++;
            }

            grabber.stop();
            System.out.println("Video streaming finished.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}