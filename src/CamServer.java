
import io.humble.video.*;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;


public class CamServer {

    static int port=55004;

    public static void main (String[] args) throws IOException, InterruptedException {

        JFrame jframe = new JFrame("Mon moniteur de camera");
        jframe.setSize(800, 600);
        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jframe.setLayout(new BorderLayout());

        ImageDisplayPanel imageDisplayPanel = new ImageDisplayPanel();
        jframe.add(imageDisplayPanel, BorderLayout.CENTER);
        jframe.setVisible(true);


        //SOCKET
        //ServerSocket server = new ServerSocket(54339);
        ServerSocket server = new ServerSocket(port);
        System.out.println("En attente de connexion ...");
        Socket socket = server.accept();

        int width = 800;
        int height = 600;
        int snapsPerSecond = 25;
        String filename = "video2.mp4";

        // Initialize Humble Video
        /** First we create a muxer using the passed in filename and formatname if given. */
        Muxer muxer = Muxer.make(filename, null, "mp4");

        MuxerFormat format = muxer.getFormat();
        Codec codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());

        final Rational framerate = Rational.make(1, snapsPerSecond);
//        Now that we know what codec, we need to create an encoder
        Encoder encoder = Encoder.make(codec);

        // Configure encoder parameters
        encoder.setWidth(width);
        encoder.setHeight(height);
        // We are going to use 420P as the format because that's what most video formats these days use
        final PixelFormat.Type pixelformat = PixelFormat.Type.PIX_FMT_YUV420P;
        encoder.setPixelFormat(pixelformat);
        encoder.setTimeBase(framerate);

        if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER))
            encoder.setFlag(Coder.Flag.FLAG_GLOBAL_HEADER, true);

        // Open muxer and encoder
        encoder.open(null, null);
        /* Add this stream to the muxer. */
        muxer.addNewStream(encoder);
        muxer.open(null, null);


        try
        {

            DataInputStream rcv = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            MediaPictureConverter converter = null;
            final MediaPicture picture = MediaPicture.make(
                    encoder.getWidth(),
                    encoder.getHeight(),
                    pixelformat);
            picture.setTimeBase(framerate);

            /* Now begin our main loop of taking screen snaps.
             * We're going to encode and then write out any resulting packets. */
            final MediaPacket packet = MediaPacket.make();

            long startTime = System.currentTimeMillis() ;
//            long startTime = System.nanoTime() ;
            int itime= 0 ;
            while (true)
            {
                int frameWidth = rcv.readInt();
                int frameHeight = rcv.readInt();
                int[] pixelData = new int[frameWidth * frameHeight];

                for (int i = 0; i < pixelData.length; i++) {
                    pixelData[i] = rcv.readInt();
                }
                BufferedImage img = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
                img.setRGB(0, 0, frameWidth, frameHeight, pixelData, 0, frameWidth);

                /* Make the screen capture && convert image to TYPE_3BYTE_BGR */
                final BufferedImage screen = convertToType(img, BufferedImage.TYPE_3BYTE_BGR);
                /* This is LIKELY not in YUV420P format, so we're going to convert it using some handy utilities. */
                if (converter == null)
                    converter = MediaPictureConverterFactory.createConverter(screen, picture);
                converter.toPicture(picture, screen,itime++ );

                do {
                    encoder.encode(packet, picture);
                    if (packet.isComplete())
                        muxer.write(packet, false);
//                    System.out.println("Do packet complete");
                } while (packet.isComplete());

                imageDisplayPanel.setBackground(img);
            }


        }catch(Exception ex){
            System.out.println("***** "+ex.getMessage()+" *****") ;
            ex.printStackTrace() ;
        }
        finally{
            /* Encoders, like decoders, sometimes cache pictures so it can do the right key-frame optimizations.
             * So, they need to be flushed as well. As with the decoders, the convention is to pass in a null
             * input until the output is not complete.
             */
//            do {
//                encoder.encode(packet, null);
//                if (packet.isComplete())
//                    muxer.write(packet,  false);
//            } while (packet.isComplete());

            /** Finally, let's clean up after ourselves. */
            muxer.close();
            System.out.println("muxer fermeture ");
            socket.close();
            server.close();
            jframe.setVisible(false);

        }
    }


    private static class ImageDisplayPanel extends JPanel
    {
        private static final Object BACKGROUND_LOCK = new Object();
        private BufferedImage background = null;

        public ImageDisplayPanel () throws HeadlessException
        {
            this.setDoubleBuffered(true); //to avoid flicker
        }

        public void setBackground (Image newBackground)
        {
            synchronized (BACKGROUND_LOCK)
            {
                if (background == null)
                {
                    background = new BufferedImage(newBackground.getWidth(null), newBackground.getHeight(null), BufferedImage.TYPE_INT_RGB);
                }
                else if (background.getWidth() != newBackground.getWidth(null) || background.getHeight() != newBackground.getHeight(null))
                {
                    background.flush();//flush old resources first
                    background = new BufferedImage(newBackground.getWidth(null), newBackground.getHeight(null), BufferedImage.TYPE_INT_RGB);
                }
                Graphics graphics = background.createGraphics();
                graphics.drawImage(newBackground, 0, 0, null);
            }
            repaint();
        }

        public BufferedImage getImage() {
            synchronized (BACKGROUND_LOCK) {
                if (background == null) {
                    return null;
                }
                return new BufferedImage(background.getWidth(), background.getHeight(), BufferedImage.TYPE_INT_RGB);
            }
        }

        @Override
        public void paint (Graphics g)
        {
            super.paint(g);
            synchronized (BACKGROUND_LOCK)
            {
                if (background != null)
                {
                    g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
                }
            }
        }
    }

    public static BufferedImage convertToType(BufferedImage sourceImage,
                                              int targetType)
    {
        BufferedImage image;

        // if the source image is already the target type, return the source image

        if (sourceImage.getType() == targetType){
            image = sourceImage;
//            System.out.println("sourceImage.getType() == targetType");
        }
        else {
            image = new BufferedImage(800,600, targetType);
//            image = new BufferedImage(sourceImage.getWidth(),sourceImage.getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
//            System.out.println("sourceImage.getType() <> targetType");
        }

        return image;
    }
}
