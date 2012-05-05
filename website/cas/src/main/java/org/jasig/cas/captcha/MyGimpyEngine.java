package org.jasig.cas.captcha;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.ImageFilter;
import com.jhlabs.image.WaterFilter;
import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator;
import com.octo.captcha.component.image.color.RandomRangeColorGenerator;
import com.octo.captcha.component.image.deformation.ImageDeformationByFilters;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.DecoratedRandomTextPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.textpaster.textdecorator.BaffleTextDecorator;
import com.octo.captcha.component.image.textpaster.textdecorator.TextDecorator;
import com.octo.captcha.component.image.wordtoimage.DeformedComposedWordToImage;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.word.wordgenerator.RandomWordGenerator;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;
import com.octo.captcha.engine.image.ListImageCaptchaEngine;
import com.octo.captcha.image.gimpy.GimpyFactory;

public class MyGimpyEngine extends ListImageCaptchaEngine
{
    public static final Integer WORD_MIN_LENGTH = new Integer(4);
    public static final Integer WORD_MAX_LENGTH = new Integer(4);

    public static final Integer IMAGE_WIDTH = new Integer(80);
    public static final Integer IMAGE_HEIGHT = new Integer(25);

    public static final Integer FONT_MIN_SIZE = new Integer(15);
    public static final Integer FONT_MAX_SIZE = new Integer(18);

    protected void buildInitialFactories()
    {
        WordGenerator wordGenerator = (new RandomWordGenerator("23456789ABCDEFGHJKMNPQRSTUVWXY"));

        BackgroundGenerator backgroundGenerator = new UniColorBackgroundGenerator(IMAGE_WIDTH, IMAGE_HEIGHT);

        Font[] fontsList = new Font[]{Font.decode("Arial"), Font.decode("Georgia"), Font.decode("Verdana"), Font.decode("Courier New")};
        FontGenerator fontGenerator = new RandomFontGenerator(FONT_MIN_SIZE, FONT_MAX_SIZE, fontsList);

        RandomRangeColorGenerator cgen = new RandomRangeColorGenerator(
                new int[] { 0, 128 },
                new int[] { 0, 128 },
                new int[] { 0, 196 }
            );

        TextDecorator[] textdecorators = new TextDecorator[]{new BaffleTextDecorator(new Integer(1), Color.WHITE)};

        TextPaster textPaster = new DecoratedRandomTextPaster(
                WORD_MIN_LENGTH,
                WORD_MAX_LENGTH,
                cgen,
                textdecorators
            );

        WaterFilter water = new WaterFilter();
        water.setAmplitude(2d);//振幅
        water.setAntialias(true);//锯齿或平滑
        water.setPhase(15d);//相位
        water.setWavelength(30d);

        WordToImage wordToImage = new DeformedComposedWordToImage(
                fontGenerator,
                backgroundGenerator,
                textPaster,
                new ImageDeformationByFilters(new ImageFilter[]{}),
                new ImageDeformationByFilters(new ImageFilter[]{}),
                //new ImageDeformationByFilters(new ImageFilter[]{water})
                new ImageDeformationByFilters(new ImageFilter[]{})
            );

        addFactory(new GimpyFactory(wordGenerator, wordToImage));
    }
}
