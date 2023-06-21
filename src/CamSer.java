//import io.humble.video.*;
//import io.humble.video.awt.MediaPictureConverter;
//import io.humble.video.awt.MediaPictureConverterFactory;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.BufferedInputStream;
//import java.io.DataInputStream;
//import java.io.IOException;
//import java.net.ServerSocket;
//import java.net.Socket;
//
//public class CamSer {
//}
//
//import io.humble.video.*;
//        import io.humble.video.awt.MediaPictureConverter;
//        import io.humble.video.awt.MediaPictureConverterFactory;
//
//        import javax.swing.*;
//        import java.awt.*;
//        import java.awt.image.BufferedImage;
//        import java.io.BufferedInputStream;
//        import java.io.DataInputStream;
//        import java.io.IOException;
//        import java.net.ServerSocket;
//        import java.net.Socket;
//        import java.util.concurrent.TimeUnit;
//
//
//public class CamServer {
//
//    static int port=55004;
//
//    public static void main (String[] args) throws IOException, InterruptedException {
//        JFrame jframe = new JFrame("Mon moniteur de camera");
//        jframe.setSize(800, 600);
//        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        jframe.setLayout(new BorderLayout());
//
//        ImageDisplayPanel imageDisplayPanel = new ImageDisplayPanel();
//        jframe.add(imageDisplayPanel, BorderLayout.CENTER);
//        jframe.setVisible(true);
//
//
//        //SOCKET
//        //ServerSocket server = new ServerSocket(54339);
//        ServerSocket server = new ServerSocket(port);
//        System.out.println("En attente de connexion ...");
//        Socket socket = server.accept();
//
//        int width = 800;
//        int height = 600;
//        int frameRate = 25;
//        String outputFilename = "video.mp4";
//
//        // Initialize Humble Video
//        Demuxer demuxer = Demuxer.make();
//        try{
//            demuxer.open(outputFilename, null, false, true, null, null);
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        Muxer muxer = Muxer.make(outputFilename, null, "mp4");
//        MuxerFormat format = muxer.getFormat();
//        Codec codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());
//        Encoder encoder = Encoder.make(codec);
//
//        // Configure encoder parameters
//        encoder.setWidth(width);
//        encoder.setHeight(height);
//        encoder.setPixelFormat(PixelFormat.Type.PIX_FMT_YUV420P);
//        encoder.setTimeBase(Rational.make(1, frameRate));
//        if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER))
//            encoder.setFlag(Coder.Flag.FLAG_GLOBAL_HEADER, true);
//
//        // Open muxer and encoder
//        encoder.open(null, null);
//        muxer.addNewStream(encoder);
//        muxer.open(null, null);
//
//
//        try
//        {
//
//
//            DataInputStream rcv = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
//
//            MediaPacket mediaPacket = MediaPacket.make();
//            MediaPicture picture = MediaPicture.make(width, height, encoder.getPixelFormat());
//
//            while (true)
//            {
//                int frameWidth = rcv.readInt();
//                int frameHeight = rcv.readInt();
//                int[] pixelData = new int[frameWidth * frameHeight];
//
//                for (int i = 0; i < pixelData.length; i++) {
//                    pixelData[i] = rcv.readInt();
//                }
//                BufferedImage img = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
//                img.setRGB(0, 0, frameWidth, frameHeight, pixelData, 0, frameWidth);
//
//
//
//                if (img != null) {
////                    MediaPictureConverter converter = MediaPictureConverterFactory.createConverter(img, picture.getPixelFormat());
//                    final MediaPictureConverter converter = MediaPictureConverterFactory
//                            .createConverter(MediaPictureConverterFactory.HUMBLE_BGR_24, picture);
//                    converter.toPicture(picture, img, 0);
////                    converter.toPicture(null, img, 0);
//                    encoder.encode(mediaPacket, picture);
//                    muxer.write(mediaPacket, false);
//                }
//                imageDisplayPanel.setBackground(img);
//            }
//
//        }catch(Exception ex){
//            System.out.println("***** "+ex.getMessage()+" *****") ;
//            ex.printStackTrace() ;
//        }
//        finally{
//            try {
////                encoder.close() ;
//                muxer.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            socket.close();
//            server.close();
//            jframe.setVisible(false);
//
//        }
//    }
//
//
//    private static class ImageDisplayPanel extends JPanel
//    {
//        private static final Object BACKGROUND_LOCK = new Object();
//        private BufferedImage background = null;
//
//        public ImageDisplayPanel () throws HeadlessException
//        {
//            this.setDoubleBuffered(true); //to avoid flicker
//        }
//
//        public void setBackground (Image newBackground)
//        {
//            synchronized (BACKGROUND_LOCK)
//            {
//                if (background == null)
//                {
//                    background = new BufferedImage(newBackground.getWidth(null), newBackground.getHeight(null), BufferedImage.TYPE_INT_RGB);
//                }
//                else if (background.getWidth() != newBackground.getWidth(null) || background.getHeight() != newBackground.getHeight(null))
//                {
//                    background.flush();//flush old resources first
//                    background = new BufferedImage(newBackground.getWidth(null), newBackground.getHeight(null), BufferedImage.TYPE_INT_RGB);
//                }
//                Graphics graphics = background.createGraphics();
//                graphics.drawImage(newBackground, 0, 0, null);
//            }
//            repaint();
//        }
//
//        public BufferedImage getImage() {
//            synchronized (BACKGROUND_LOCK) {
//                if (background == null) {
//                    return null;
//                }
//                return new BufferedImage(background.getWidth(), background.getHeight(), BufferedImage.TYPE_INT_RGB);
//            }
//        }
//
//        @Override
//        public void paint (Graphics g)
//        {
//            super.paint(g);
//            synchronized (BACKGROUND_LOCK)
//            {
//                if (background != null)
//                {
//                    g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
//                }
//            }
//        }
//    }
//
//}
//
//import io.humble.video.*;
//        import io.humble.video.awt.MediaPictureConverter;
//        import io.humble.video.awt.MediaPictureConverterFactory;
//
//        import javax.swing.*;
//        import java.awt.*;
//        import java.awt.image.BufferedImage;
//        import java.io.BufferedInputStream;
//        import java.io.DataInputStream;
//        import java.io.IOException;
//        import java.net.ServerSocket;
//        import java.net.Socket;
//        import java.util.concurrent.TimeUnit;
//
//
//public class CamServer {
//
//    static int port=55004;
//
//    public static void main (String[] args) throws IOException, InterruptedException {
//        JFrame jframe = new JFrame("Mon moniteur de camera");
//        jframe.setSize(800, 600);
//        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        jframe.setLayout(new BorderLayout());
//
//        ImageDisplayPanel imageDisplayPanel = new ImageDisplayPanel();
//        jframe.add(imageDisplayPanel, BorderLayout.CENTER);
//        jframe.setVisible(true);
//
//
//        //SOCKET
//        //ServerSocket server = new ServerSocket(54339);
//        ServerSocket server = new ServerSocket(port);
//        System.out.println("En attente de connexion ...");
//        Socket socket = server.accept();
//
//        int width = 800;
//        int height = 600;
//        int frameRate = 25;
//        String outputFilename = "video.mp4";
//
//        // Initialize Humble Video
//        Demuxer demuxer = Demuxer.make();
//        try{
//            demuxer.open(outputFilename, null, false, true, null, null);
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        Muxer muxer = Muxer.make(outputFilename, null, "mp4");
//        MuxerFormat format = muxer.getFormat();
//        Codec codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());
//        Encoder encoder = Encoder.make(codec);
//
//        // Configure encoder parameters
//        encoder.setWidth(width);
//        encoder.setHeight(height);
//        encoder.setPixelFormat(PixelFormat.Type.PIX_FMT_YUV420P);
//        encoder.setTimeBase(Rational.make(1, frameRate));
//        if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER))
//            encoder.setFlag(Coder.Flag.FLAG_GLOBAL_HEADER, true);
//
//        // Open muxer and encoder
//        encoder.open(null, null);
//        muxer.addNewStream(encoder);
//        muxer.open(null, null);
//
//
//        try
//        {
//
//
//            DataInputStream rcv = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
//
//            MediaPacket mediaPacket = MediaPacket.make();
//            MediaPicture picture = MediaPicture.make(width, height, encoder.getPixelFormat());
//
//            while (true)
//            {
//                int frameWidth = rcv.readInt();
//                int frameHeight = rcv.readInt();
//                int[] pixelData = new int[frameWidth * frameHeight];
//
//                for (int i = 0; i < pixelData.length; i++) {
//                    pixelData[i] = rcv.readInt();
//                }
//                BufferedImage img = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
//                img.setRGB(0, 0, frameWidth, frameHeight, pixelData, 0, frameWidth);
//
//
//
//                if (img != null) {
////                    MediaPictureConverter converter = MediaPictureConverterFactory.createConverter(img, picture.getPixelFormat());
//                    final MediaPictureConverter converter = MediaPictureConverterFactory
//                            .createConverter(MediaPictureConverterFactory.HUMBLE_BGR_24, picture);
//                    converter.toPicture(picture, img, 0);
////                    converter.toPicture(null, img, 0);
//                    encoder.encode(mediaPacket, picture);
//                    muxer.write(mediaPacket, false);
//                }
//                imageDisplayPanel.setBackground(img);
//            }
//
//        }catch(Exception ex){
//            System.out.println("***** "+ex.getMessage()+" *****") ;
//            ex.printStackTrace() ;
//        }
//        finally{
//            try {
////                encoder.close() ;
//                muxer.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            socket.close();
//            server.close();
//            jframe.setVisible(false);
//
//        }
//    }
//
//
//    private static class ImageDisplayPanel extends JPanel
//    {
//        private static final Object BACKGROUND_LOCK = new Object();
//        private BufferedImage background = null;
//
//        public ImageDisplayPanel () throws HeadlessException
//        {
//            this.setDoubleBuffered(true); //to avoid flicker
//        }
//
//        public void setBackground (Image newBackground)
//        {
//            synchronized (BACKGROUND_LOCK)
//            {
//                if (background == null)
//                {
//                    background = new BufferedImage(newBackground.getWidth(null), newBackground.getHeight(null), BufferedImage.TYPE_INT_RGB);
//                }
//                else if (background.getWidth() != newBackground.getWidth(null) || background.getHeight() != newBackground.getHeight(null))
//                {
//                    background.flush();//flush old resources first
//                    background = new BufferedImage(newBackground.getWidth(null), newBackground.getHeight(null), BufferedImage.TYPE_INT_RGB);
//                }
//                Graphics graphics = background.createGraphics();
//                graphics.drawImage(newBackground, 0, 0, null);
//            }
//            repaint();
//        }
//
//        public BufferedImage getImage() {
//            synchronized (BACKGROUND_LOCK) {
//                if (background == null) {
//                    return null;
//                }
//                return new BufferedImage(background.getWidth(), background.getHeight(), BufferedImage.TYPE_INT_RGB);
//            }
//        }
//
//        @Override
//        public void paint (Graphics g)
//        {
//            super.paint(g);
//            synchronized (BACKGROUND_LOCK)
//            {
//                if (background != null)
//                {
//                    g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
//                }
//            }
//        }
//    }
//
//}
//
