import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

public class VideoReceiver {
    public static void main(String[] args) {
        int receiverPort = 5000; // Cổng nhận dữ liệu
        String outputPath = "path/to/output.mp4"; // Đường dẫn video xuất
        int packetSize = 1400; // Kích thước gói UDP

        try (DatagramSocket socket = new DatagramSocket(receiverPort);
             FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath, 640, 480)) {
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mp4");
            recorder.setFrameRate(30);
            recorder.start();

            byte[] buffer = new byte[packetSize + 8];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            Map<Integer, byte[]> frameMap = new HashMap<>();
            int lastFrame = -1;

            while (true) {
                socket.receive(packet);
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.getData()));

                int frameNumber = dis.readInt();
                int packetOffset = dis.readInt();

                byte[] data = new byte[packet.getLength() - 8];
                System.arraycopy(packet.getData(), 8, data, 0, data.length);

                frameMap.put(frameNumber, data);

                if (frameNumber > lastFrame) {
                    for (int i = lastFrame + 1; i <= frameNumber; i++) {
                        if (frameMap.containsKey(i)) {
                            recorder.recordImage(640, 480, 0, new ByteArrayInputStream(frameMap.get(i)));
                            frameMap.remove(i);
                        }
                    }
                    lastFrame = frameNumber;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}