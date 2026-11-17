package hbci4java.existdb;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import javax.imageio.ImageIO;

import org.kapott.hbci.manager.FlickerCode;
import org.kapott.hbci.manager.MatrixCode;
import org.kapott.hbci.manager.QRCode;

/**
 * Builds structured challenge messages with SVG preferred payloads.
 */
public final class ChallengeMedia {

    private ChallengeMedia() {
    }

    public static String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public static String buildChallengeXml(String type, String text, String hhduc, String svg, String fallbackB64, String fallbackMime) {
        StringBuilder sb = new StringBuilder();
        sb.append("<Challenge type=\"").append(escapeXml(type)).append("\">");
        sb.append("<text>").append(escapeXml(text == null ? "" : text)).append("</text>");
        if (hhduc != null && hhduc.length() > 0) {
            sb.append("<hhduc>").append(escapeXml(hhduc)).append("</hhduc>");
        }
        if (svg != null && svg.length() > 0) {
            sb.append("<svg mediaType=\"image/svg+xml\"><![CDATA[").append(svg).append("]]></svg>");
        }
        if (fallbackB64 != null && fallbackB64.length() > 0) {
            String mime = fallbackMime == null ? "image/png" : fallbackMime;
            sb.append("<fallback mediaType=\"").append(escapeXml(mime))
                    .append("\" encoding=\"base64\">").append(fallbackB64).append("</fallback>");
        }
        sb.append("</Challenge>");
        return sb.toString();
    }

    public static String flickerSvg(String hhduc) {
        String rendered = hhduc;
        try {
            FlickerCode code = FlickerCode.tryParse(hhduc, null);
            if (code == null) {
                code = new FlickerCode(hhduc);
            }
            rendered = code.render();
        } catch (Exception ignored) {
            // keep raw hhduc
        }
        if (rendered == null) {
            rendered = hhduc == null ? "" : hhduc;
        }
        // Five classic chipTAN bars; animation driven by CSS + data-hhduc for JS refinement.
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 220 80\" width=\"220\" height=\"80\" data-hhduc=\"")
                .append(escapeXml(rendered)).append("\">");
        svg.append("<rect width=\"220\" height=\"80\" fill=\"#111\"/>");
        for (int i = 0; i < 5; i++) {
            int x = 20 + i * 40;
            svg.append("<rect class=\"flicker-bar\" id=\"fb").append(i).append("\" x=\"").append(x)
                    .append("\" y=\"15\" width=\"24\" height=\"50\" fill=\"#222\" stroke=\"#888\">");
            svg.append("<animate attributeName=\"fill\" values=\"#222;#f5f5f5;#222\" dur=\"")
                    .append(0.4 + (i * 0.05)).append("s\" repeatCount=\"indefinite\" begin=\"")
                    .append(i * 0.08).append("s\"/>");
            svg.append("</rect>");
        }
        svg.append("</svg>");
        return svg.toString();
    }

    public static String imageAsSvg(byte[] image, String mime) {
        if (image == null || image.length == 0) {
            return null;
        }
        String m = mime == null ? "image/png" : mime;
        String b64 = Base64.getEncoder().encodeToString(image);
        // Prefer true SVG matrix when raster can be sampled; otherwise wrap PNG in SVG.
        String matrix = tryMatrixSvg(image);
        if (matrix != null) {
            return matrix;
        }
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"240\" height=\"240\" viewBox=\"0 0 240 240\">"
                + "<image href=\"data:" + escapeXml(m) + ";base64," + b64
                + "\" width=\"240\" height=\"240\"/>"
                + "</svg>";
    }

    private static String tryMatrixSvg(byte[] image) {
        try {
            BufferedImage bi = ImageIO.read(new ByteArrayInputStream(image));
            if (bi == null) {
                return null;
            }
            int w = bi.getWidth();
            int h = bi.getHeight();
            if (w <= 0 || h <= 0 || w > 400 || h > 400) {
                return null;
            }
            StringBuilder svg = new StringBuilder();
            svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ").append(w).append(' ').append(h)
                    .append("\" width=\"").append(Math.max(180, w)).append("\" height=\"")
                    .append(Math.max(180, h)).append("\">");
            svg.append("<rect width=\"").append(w).append("\" height=\"").append(h).append("\" fill=\"#fff\"/>");
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgb = bi.getRGB(x, y) & 0x00FFFFFF;
                    if (rgb < 0x808080) {
                        svg.append("<rect x=\"").append(x).append("\" y=\"").append(y)
                                .append("\" width=\"1\" height=\"1\" fill=\"#000\"/>");
                    }
                }
            }
            svg.append("</svg>");
            return svg.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String fromPhoto(String retData, String msg) {
        String svg = null;
        String fallback = null;
        String mime = "image/png";
        try {
            MatrixCode code = MatrixCode.tryParse(retData);
            if (code == null) {
                code = new MatrixCode(retData);
            }
            byte[] image = code.getImage();
            mime = code.getMimetype() == null ? "image/png" : code.getMimetype();
            fallback = Base64.getEncoder().encodeToString(image);
            svg = imageAsSvg(image, mime);
        } catch (Exception e) {
            // leave svg/fallback null
        }
        return buildChallengeXml("photo", msg, null, svg, fallback, mime);
    }

    public static String fromQr(String retData, String msg) {
        String svg = null;
        String fallback = null;
        String mime = "image/png";
        String displayMsg = msg;
        try {
            QRCode code = QRCode.tryParse(retData, msg);
            if (code == null) {
                code = new QRCode(retData, msg);
            }
            byte[] image = code.getImage();
            mime = code.getMimetype() == null ? "image/png" : code.getMimetype();
            fallback = Base64.getEncoder().encodeToString(image);
            svg = imageAsSvg(image, mime);
            if (code.getMessage() != null) {
                displayMsg = code.getMessage();
            }
        } catch (Exception e) {
            // leave svg/fallback null
        }
        return buildChallengeXml("qr", displayMsg, null, svg, fallback, mime);
    }

    public static String fromFlicker(String hhduc, String msg) {
        String svg = flickerSvg(hhduc);
        return buildChallengeXml("flicker", msg, hhduc, svg, null, null);
    }

    public static String fromText(String msg) {
        return buildChallengeXml("text", msg, null, null, null, null);
    }
}
