package com.enuos.live.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author WangCaiWen Created on 2020/5/6 14:55
 */
@Slf4j
public class ImageUtil {

  /**
   * 图片的间隙
   */
  private static final int SIDE = 6;
  /**
   * 画板尺寸
   */
  private static final int CANVAS_WIDTH = 200;
  private static final int CANVAS_HEIGHT = 200;

  /**
   * 尺寸1 （小）
   */
  private static final int ONE_IMAGE_SIZE = CANVAS_HEIGHT - (2 * SIDE);
  /**
   * 尺寸2 （中）
   */
  private static final int TWO_IMAGE_SIZE = (CANVAS_HEIGHT - (3 * SIDE)) / 2;
  /**
   * 尺寸3 （大）
   */
  private static final int FIVE_IMAGE_SIZE = (CANVAS_HEIGHT - (4 * SIDE)) / 3;

  private ImageUtil() {
  }

  private static final String INDEX_HTTP = "http://";
  private static final String INDEX_HTTPS = "https://";
  private static final Integer INDEX_FIVE = 5;

  /**
   * 生成群组头像
   *
   * @param paths 图片链接
   * @return 字节数据
   * @throws IOException 错误信息
   */
  public static byte[] getCombinationOfHead(List<String> paths) throws IOException {
    List<BufferedImage> bufferedImages = new ArrayList<>();
    int imageSize = 0;
    if (paths.size() <= 1) {
      //若为一张图片
      imageSize = ONE_IMAGE_SIZE;
    } else if (paths.size() > 1 && paths.size() < INDEX_FIVE) {
      //若为2-4张图片
      imageSize = TWO_IMAGE_SIZE;
    } else {
      //若>=5张图片
      imageSize = FIVE_IMAGE_SIZE;
    }
    for (String path : paths) {
      BufferedImage resize2 = ImageUtil.resize2(path, imageSize, imageSize, true);
      bufferedImages.add(resize2);
    }
    BufferedImage outImage = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT,
        BufferedImage.TYPE_INT_RGB);
    // 生成画布
    Graphics graphics = outImage.getGraphics();
    Graphics2D g2d = (Graphics2D) graphics;
    // 设置背景色
    g2d.setBackground(new Color(231, 231, 231));
    // 通过使用当前绘图表面的背景色进行填充来清除指定的矩形。
    g2d.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    // 开始拼凑 根据图片的数量判断该生成那种样式的组合头像目前为九种
    for (int i = 1, length = bufferedImages.size(); i <= length; i++) {
      switch (length) {
        case 1:
          g2d.drawImage(bufferedImages.get(i - 1), SIDE, SIDE, null);
          break;
        case 2:
          if (i == 1) {
            g2d.drawImage(bufferedImages.get(i - 1), SIDE, (CANVAS_WIDTH - imageSize) / 2, null);
          } else {
            g2d.drawImage(bufferedImages.get(i - 1), 2 * SIDE + imageSize,
                (CANVAS_WIDTH - imageSize) / 2, null);
          }
          break;
        case 3:
          if (i == 1) {
            g2d.drawImage(bufferedImages.get(i - 1), (CANVAS_WIDTH - imageSize) / 2, SIDE, null);
          } else {
            g2d.drawImage(bufferedImages.get(i - 1), (i - 1) * SIDE + (i - 2) * imageSize,
                imageSize + (2 * SIDE), null);
          }
          break;
        case 4:
          if (i <= 2) {
            g2d.drawImage(bufferedImages.get(i - 1), i * SIDE + (i - 1) * imageSize, SIDE, null);

          } else {
            g2d.drawImage(bufferedImages.get(i - 1), (i - 2) * SIDE + (i - 3) * imageSize,
                imageSize + 2 * SIDE, null);
          }
          break;
        case 5:
          if (i <= 2) {
            g2d.drawImage(bufferedImages.get(i - 1),
                (CANVAS_WIDTH - 2 * imageSize - SIDE) / 2 + (i - 1) * imageSize + (i - 1) * SIDE,
                (CANVAS_WIDTH - 2 * imageSize - SIDE) / 2, null);
          } else {
            g2d.drawImage(bufferedImages.get(i - 1), (i - 2) * SIDE + (i - 3) * imageSize,
                ((CANVAS_WIDTH - 2 * imageSize - SIDE) / 2) + imageSize + SIDE, null);
          }
          break;
        case 6:
          if (i <= 3) {
            g2d.drawImage(bufferedImages.get(i - 1), SIDE * i + imageSize * (i - 1),
                (CANVAS_WIDTH - 2 * imageSize - SIDE) / 2, null);
          } else {
            g2d.drawImage(bufferedImages.get(i - 1), ((i - 3) * SIDE) + ((i - 4) * imageSize),
                ((CANVAS_WIDTH - 2 * imageSize - SIDE) / 2) + imageSize + SIDE, null);
          }
          break;
        case 7:
          if (i <= 1) {
            g2d.drawImage(bufferedImages.get(i - 1), 2 * SIDE + imageSize, SIDE, null);
          }
          if (i <= 4 && i > 1) {
            g2d.drawImage(bufferedImages.get(i - 1), ((i - 1) * SIDE) + ((i - 2) * imageSize),
                2 * SIDE + imageSize, null);
          }
          if (i <= 7 && i > 4) {
            g2d.drawImage(bufferedImages.get(i - 1), ((i - 4) * SIDE) + ((i - 5) * imageSize),
                3 * SIDE + 2 * imageSize, null);
          }
          break;
        case 8:
          if (i <= 2) {
            g2d.drawImage(bufferedImages.get(i - 1),
                (CANVAS_WIDTH - 2 * imageSize - SIDE) / 2 + (i - 1) * imageSize + (i - 1) * SIDE,
                SIDE, null);
          }
          if (i <= 5 && i > 2) {
            g2d.drawImage(bufferedImages.get(i - 1), ((i - 2) * SIDE) + ((i - 3) * imageSize),
                2 * SIDE + imageSize, null);
          }
          if (i <= 8 && i > 5) {
            g2d.drawImage(bufferedImages.get(i - 1), ((i - 5) * SIDE) + ((i - 6) * imageSize),
                3 * SIDE + 2 * imageSize, null);
          }
          break;
        case 9:
          if (i <= 3) {
            g2d.drawImage(bufferedImages.get(i - 1), (i * SIDE) + ((i - 1) * imageSize), SIDE,
                null);
          }
          if (i <= 6 && i > 3) {
            g2d.drawImage(bufferedImages.get(i - 1), ((i - 3) * SIDE) + ((i - 4) * imageSize),
                2 * SIDE + imageSize, null);
          }
          if (i <= 9 && i > 6) {
            g2d.drawImage(bufferedImages.get(i - 1), ((i - 6) * SIDE) + ((i - 7) * imageSize),
                3 * SIDE + 2 * imageSize, null);
          }
          break;
        default:
          break;
      }
    }
    //将BufferedImage转为byte[]字节数组
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(outImage, "jpg", byteArrayOutputStream);
    byteArrayOutputStream.flush();
    byte[] imageInByte = byteArrayOutputStream.toByteArray();
    byteArrayOutputStream.close();
    return imageInByte;
  }

  /**
   * 图片缩放
   *
   * @param filePath 图片路径
   * @param height 高度
   * @param width 宽度
   * @param fillUp 比例不对时是否需要补白
   */
  private static BufferedImage resize2(String filePath, int height, int width, boolean fillUp) {
    Image itemp = null;
    try {
      // 缩放比例
      double ratio = 0;
      BufferedImage bufferedImage = null;
      if (filePath.indexOf(INDEX_HTTP) == 0 || filePath.indexOf(INDEX_HTTPS) == 0) {
        bufferedImage = ImageIO.read(new URL(filePath));
      } else {
        bufferedImage = ImageIO.read(new File(filePath));
      }
      itemp = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
      // 计算比例
      if ((bufferedImage.getHeight() > height) || (bufferedImage.getWidth() > width)) {
        if (bufferedImage.getHeight() > bufferedImage.getWidth()) {
          ratio = (new Integer(height)).doubleValue() / bufferedImage.getHeight();
        } else {
          ratio = (new Integer(width)).doubleValue() / bufferedImage.getWidth();
        }
        AffineTransformOp affineTransformOp = new AffineTransformOp(
            AffineTransform.getScaleInstance(ratio, ratio), null);
        itemp = affineTransformOp.filter(bufferedImage, null);
      }
      if (fillUp) {
        BufferedImage image = new BufferedImage(width, height,
            BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, width, height);
        if (width == itemp.getWidth(null)) {
          graphics.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2,
              itemp.getWidth(null), itemp.getHeight(null),
              Color.white, null);
        } else {
          graphics.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0,
              itemp.getWidth(null), itemp.getHeight(null),
              Color.white, null);
        }
        graphics.dispose();
        itemp = image;
      }
    } catch (IOException e) {
      log.error(e.getMessage());
      log.error(ExceptionUtil.getStackTrace(e));
    }
    return (BufferedImage) itemp;
  }
}
